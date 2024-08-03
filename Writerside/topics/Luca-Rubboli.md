# Luca Rubboli

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