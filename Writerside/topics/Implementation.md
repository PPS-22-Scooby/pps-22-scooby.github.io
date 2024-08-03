# Implementation

### Technologies

- [Akka](https://akka.io/)
- [Play-json](https://www.playframework.com/documentation/2.9.x/ScalaJson?)
- [jsoup](https://jsoup.org/)
- [sttp](https://sttp.softwaremill.com/en/stable/)
- [monocle](https://www.optics.dev/Monocle/)

## Giovanni Antonioni

## Valerio Di Zio

## Francesco Magnani

The main points where I've worked on implementation side are:

* [HTTP library](HTTP.md)
* [Document library](Document.md)
* Scooby's start/stop mechanisms
* DSL general structure, Scraper section and safety mechanisms
* Scooby testing class

### Scooby start/stop mechanisms

Given that the Scooby system uses Akka actor system under the hood, finding a way to start and stop the application
gracefully is one aspect to tackle. More that the start of the application, the **end** (stopping) aspect is more
challenging, as it require to ensure that all involved actors doesn't need any more time, nor they are still computing
some data. 

To face this aspect, we need to understand what are the macro steps of execution and how they depend on each other. 
The full description of how this is managed is reported [here](Scooby-application-lifecycle.md) 

### DSL Scraper keywords

The Scraping keywords are, in reality, just composed of a single instruction `scrape` that opens a `Context` where it's
possible to define the Scraping Policy. 

```Scala
/**
 * Top level keyword for defining the scraping behavior.
 * @param block definition of the scraping behavior
 * @param globalScope global Scooby scope (i.g. "scooby: ...")
 * @tparam T type of the result returned by this scraping behavior
 */
inline def scrape[T](block: ScrapeBehaviorScope[T])(using globalScope: ConfigurationWrapper[T]): Unit =
  catchRecursiveCtx[ScrapeDocument]("scrape")
  scrapeOp(block)

/**
 * Unsafe version of the one inside [[SafeOps]]
 * @param block definition of the scraping behavior
 * @param globalScope global Scooby scope (i.g. "scooby: ...")
 * @tparam T type of the result returned by this scraping behavior
 */
def scrapeOp[T](block: ScrapeBehaviorScope[T])(using globalScope: ConfigurationWrapper[T]): Unit =
  globalScope.configuration = globalScope.configuration.focus(_.scraperConfiguration.scrapePolicy).replace:
    doc =>
      given ScrapeDocument = doc
      block
  globalScope.scrapingResultSetting = ScrapingResultSetting[T]()
```

All the other keywords that can be used under this scope are also available elsewhere, depending on the **type** of 
what we are scraping. To let the user have the more freedom as possible, Scraping Policies can have results of virtually
any type that Scala has, not just HTML elements (Tuples and String for example). 
However, as what we are scraping is typically a HTML document, other keywords work specifically on HTML elements to let
the user customize the scraping policy in a more language assisted way.

For example, in the following snippet:

```Scala
scrape:
    elements that haveId("exampleId")
```

Where `that` is simply an alias for Scala collection method `filter`, `haveId` is a method that generated a **predicate**
for HTML elements, making the entire expression pass Scala compiling. Here it's possible to see how these keywords are
implemented:

```Scala
def elements[D <: Document & CommonHTMLExplorer](using documentContext: D): Iterable[HTMLElement] =
    documentContext.getAllElements
    
extension [T](x: Iterable[T])
    inline infix def that(predicate: T => Boolean): Iterable[T] = x filter predicate
    
infix def haveId(id: String): HTMLElementRule = _.id == id
```

The same approach has been taken in a lot of other parts of the DSL, as it is highly customizable and makes
it possible to create multiple, interesting keywords. 

### DSL safety mechanism

Using Scala, `given`s can be dangerous if used improperly. In this case, each of the keywords like `scrape`, `scooby`
and so on create scopes where a `given` context is defined. This could potentially lead to programs we would want to 
be invalid pass the Scala compile. For example, the problem of **nested repeated scopes**:

```Scala
scrape:
    scrape: 
        scrape:
            elements that haveId("exampleId")
```
This program is clearly invalid, but it can be tricky, however, to catch multiple nested `given`s inside Scala. 
For example, [ScalaTest](https://www.scalatest.org/) throws an exception at runtime if a `... should ... in` keyword is 
mistakenly used inside another one.

In this project, the taken approach to solve this issue uses Scala 3 **Macros**. Each of the dangerous operators like 
`scrape` has a **safe version**, exposed outside the DSL package, and an **unsafe** one that is instead private. 
The safe version checks the unwanted behavior using Scala `inline` methods and `scala.compiletime` utilities, simply 
intercepting a repeated `given` at compile time. 

The used macro is implemented as follows:

```Scala
private inline def isInContext[T]: Boolean =
	summonFrom:
		case given T => true
		case _ => false

inline def catchRecursiveCtx[T](inline contextName: String): Unit =
	if isInContext[T] then error("\"" + contextName +"\" keyword cannot be placed inside another \"" + contextName +"\"")
```

### Scooby testing class

In order to better test the DSL, a `ScoobyTest` class has been implemented. This class contains methods that acts as 
a Scooby application but instead can simulate certain behaviors to make it easier to be tested.


## Luca Rubboli

From an implementation point of view I mainly managed:
- Scraper design and its incremental implementation, starting from a standard class design, enriching after a working version has been done with its actor extension. Moreover, some template Policies has been provided.
- Scraper's Result design and its incremental implementation, starting from a simple container, enriched with aggregation techniques to promote Exporter scaling.
- DSL Exporter section, designing and implementing DSL keywords.
- Design and implementation of tests suites.

Further details of implementation for the most relevant parts are described in the following sections.

### Scraper Policies

The design phase involving scraper policies aimed to obtain a modular, extensible and customizable implementation.
In order to obtain a general, but yet effective, policy, it consists of a function that maps a document (so called 
ScrapeDocument, which contains useful utils for scraping) into an iterable of generic type.

```Scala
/**
* A type representing a function that extract an [[Iterable]] used
* to build [[DataResult]] from a [[Document]].
*/
type ScraperPolicy[T] = ScrapeDocument => Iterable[T]
```

During test phase, some common policies have arisen, so I decided to provide an easier way to access them; the same
approach has been followed even during DSL keyword definitions.  

```Scala
/**
* Utility for scraper's rules based on selectBy attribute,
* given selectors specified.
* Admissible values are id, tag, class and css.
*
* @param selectors a [[Seq]] of selectors used in scraper rule.
* @param selectBy a selector to specify the rule.
* @return the selected rule with specified selectors.
*/
def scraperRule(selectors: Seq[String], selectBy: String): ScraperPolicy[String] =
  (scraper: ScrapeDocument) =>
    selectBy match
      case "id" =>
        selectors.map(scraper.getElementById).map(_.fold("")(_.outerHtml)).filter(_.nonEmpty)
      case "tag" =>
        selectors.flatMap(scraper.getElementsByTag).map(_.outerHtml)
      case "class" =>
        selectors.flatMap(scraper.getElementsByClass).map(_.outerHtml)
      case "css" =>
        selectors.flatMap(scraper.select(_)).map(_.outerHtml)
      case "regex" =>
        selectors.flatMap(scraper.find)
      case _ =>
        throw Error(s"Not yet implemented rule by $selectBy")
```

Moreover, following this approach, an extension method to concatenate policies has been added.

### Result

Result has been implemented through a case class, promoting immutability. Due to exporter requirements on both Batch
and Streaming aggregation strategies, both kind of updates have been provided, as well as an aggregation method which
allows Exporter scaling.

```Scala
/**
* Class representing [[Scraper]]'s results implementation.
*
* @param data
*   representing actual result.
* @tparam T
*   representing result's type.
*/
final case class Result[T] (data: Iterable[T]) extends DataResult[T]:
    
  override def updateStream(data: T)(using aggregator: ItAggregator[T]): Result[T] =
    Result(aggregator.aggregateStream(this.data, data))
    
  override def updateBatch(data: Iterable[T])(using aggregator: ItAggregator[T]): Result[T] =
    Result(aggregator.aggregateBatch(this.data, data))
    
  override def aggregate[A <: DataResult[T]](result: A)(using aggregator: ItAggregator[T]): Result[T] =
    updateBatch(result.data)
```

Once again, as matter of usability, standard Iterable aggregators has been provided.

### DSL

By DSL side, I managed Exporter entity, defining keywords and implementation.

An example of Exporter configuration with DSL is:

```Scala
exports:
  batch:
    strategy:
      results get(el => (el.tag, el.text)) output:
        toFile("test.txt") withFormat json

    aggregate:
       _ ++ _
          
  streaming:
    results get tag output:
      toConsole withFormat text
```

To achieve this, we used different technologies
- Monocle: to maintain immutability but at the same time being able to build up Configurations correctly
- Macros: to check if DSL syntax has been correctly used, i.e. context not defined more than 1 time

To give a brief taste of DSL implementation, I report this snippet of code

```Scala
/**
* Context used to parse the exporting strategies given in configuration.
* @param builder the [[ConfigurationBuilder]] containing all application parameters.
* @tparam T the [[Result]]'s type.
*/
case class ExportContext[T](builder: ConfigurationBuilder[T]):

  /**
   * Builder used to summon the [[StrategiesContext]] containing exporting strategies and parse them in application
   * configuration.
   * @param block function which set the exporting strategies in [[StrategiesContext]].
   */
  inline infix def apply(block: ExportDefinitionScope[T]): Unit =
    catchRecursiveCtx[StrategiesContext[?]]("export")
    visitCtxUnsafe(block)

  /**
   * Unsafe version of [[ExportContext.apply]]
   * @param block function which set the exporting strategies in [[StrategiesContext]].
   */
  private def visitCtxUnsafe(block: ExportDefinitionScope[T]): Unit =
    given context: StrategiesContext[T] = StrategiesContext[T](Seq.empty[SingleExporting[T]])
    block
    builder.configuration = builder.configuration
      .focus(_.exporterConfiguration.exportingStrategies).replace(context.exportingStrategies)
```

### Tests



#### Cucumber features

All features implementation have been preceded by a robust test phase as 