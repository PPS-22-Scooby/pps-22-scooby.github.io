# Luca Rubboli

From an implementation point of view I mainly managed:
- Scraper;
- Scraper's Result;
- DSL Exporter section;.
- Tests suites related to previous topics.

Further details of implementation for the most relevant parts are described in the following sections.

### Scraper

Scraper has been implemented starting from a standard class design, then enriched after a working version has been done with its actor extension.

Given a proper implementation of result class and scraper policy, its implementation results quite trivial. After its set up, it manages the scrape action of one document; after finishing the policy application, it signals the Exporter component with result obtained and stops.

```Scala
/**
 * Class representing Scraper actor.
 *
 * @param exporterRouter the exporter router [[ActorRef]] where to redirect the scraping results
 * @param scrapePolicy the scraping policy used by the actor.
 * @tparam T type representing the [[DataResult]] type.
 */
class Scraper[T](exporterRouter: ActorRef[ExporterCommands], scrapePolicy: ScraperPolicy[T]):
  /**
   * Defines [[Scraper]]'s [[Behavior]].
   * @return the [[Behavior]]
   */
  def idle(): Behavior[ScraperCommands] =
    Behaviors.setup: context =>
      Behaviors.receiveMessage:
        case ScraperCommands.Scrape(doc: ScrapeDocument) =>
          Try:
            val res = resultFromPolicy(doc)
            exporterRouter ! ExporterCommands.Export(res)
          .fold(e => println(s"An error occurred while scraping: $e"), identity)
          Behaviors.stopped

  private def resultFromPolicy(argument: ScrapeDocument): Result[T] =
    Result(scrapePolicy(argument))
```

### Scraper Policies

The design phase involving scraper policies aimed to obtain a modular, extensible and customizable implementation.
In order to obtain a general, but yet effective, policy, it consists of a function that maps a document (so called
ScrapeDocument, which contains useful API for scraping) into an iterable of generic type.

```Scala
/**
* A type representing a function that extract an [[Iterable]] used
* to build [[Result]] from a [[Document]].
*/
type ScraperPolicy[T] = ScrapeDocument => Iterable[T]
```

During test phase, some common policies have arisen, so I decided to provide an easier way to access them; the same
approach has been followed even during DSL keyword definitions.

```Scala
/**
 * Utility for [[ScraperPolicy]] based on selectBy attribute, given selectors specified.
 * Admissible values for selectBy are id, tag, class, css and regex.
 *
 * @param selectors a [[Seq]] of selectors used in scraper policy.
 * @param selectBy a selector to specify the policy.
 * @return the selected policy with specified selectors.
 */
def scraperPolicy(selectors: Seq[String], selectBy: String): ScraperPolicy[String] =
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
        throw Error(s"Not yet implemented policy by $selectBy")
```

Moreover, following this approach, an extension method to concatenate policies has been added.

### Result

Result has been incrementally implemented, starting from a simple container, enriched with aggregation techniques to promote Exporter's scaling.
In details, it has been implemented through a case class, promoting immutability. Due to exporter requirements on both Batch and Streaming aggregation strategies, both kind of updates have been provided, as well as an aggregation method which
allows usage of multiple Exporters.

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

To give a brief taste of DSL implementation, I report the following snippet of code:

```Scala
/**
 * Build the [[Exporter]] batch context.
 * @param context the [[StrategiesContext]] containing exporting strategies.
 * @tparam T the [[Result]]'s type.
 * @return the [[BatchExportationContext]] built.
 */
def batch[T](using context: StrategiesContext[T]): BatchExportationContext[T] =
  BatchExportationContext[T](context)

/**
 * Type alias representing the "Batch" section under the "exports" part of the DSL
 * @tparam T type of results returned by the scraping.
 */
private type BatchDefinitionScope[T] = BatchSettingContext[T] ?=> Unit

/**
 * The exporter batch technique's context.
 * @param context the context used to set the [[BatchExporting]] configuration.
 * @tparam T the [[Result]]'s type.
 */
case class BatchExportationContext[T](context: StrategiesContext[T]):
  /**
   * Builder used to set the [[BatchExporting]] configuration.
   * @param block the function used to set the [[BatchExporting]] configuration.
   */
  inline infix def apply(block: BatchDefinitionScope[T]): Unit =
    catchRecursiveCtx[BatchSettingContext[?]]("batch")
    visitCtxUnsafe(block)

  /**
   * Unsafe version of [[BatchExportationContext.apply]].
   * @param block the function used to set the [[BatchExporting]] configuration.
   */
  private def visitCtxUnsafe(block: BatchDefinitionScope[T]): Unit =
    given batchStrategyContext: BatchSettingContext[T] = BatchSettingContext[T](
      ExportingBehaviors.writeOnConsole(Formats.string), AggregationBehaviors.default)
    block
    context.exportingStrategies ++= Seq(BatchExporting(
      batchStrategyContext.policy,
      batchStrategyContext.aggregation
    ))
```

By means of BatchExportationContext case class, a BatchSettingContext is built, checking if not previously built,
failing otherwise, and filled with default behavior writeOnConsole and default aggregation.
After that, BatchDefinitionScope is consumed; it applies policies and aggregation functions defined by users
in batchStrategyContext, that is then exported in global context configuration.

It is worth mentioning that multiple batch and streaming strategies can be specified, resulting in the execution of
each one, while assertions on DSL configuration's structure are still done, in fact for example it's not possible to
define an export block inside an already defined one.

### Tests

All features implementation have been preceded by a robust test phase. In details, all my main implementations have
been tested with ScalaTest suite. Beyond my implementation, I tested also the application with standard configurations
against the one configured using DSL syntax:

```Scala
"Application with DSL configuration and standard configurations"
  should "obtain the same result" in :
    val appDSL = scooby:
      config:
        network:
          Timeout is timeout
          MaxRequests is maxRequest
          headers:
            auth._1 to auth._2
        options:
          MaxDepth is maxDepth
          MaxLinks is maxLinks

      crawl:
        url:
          this.url
        policy:
          linksPolicy
      scrape:
        scrapePolicyDSL(scrapeToIter)
      exports:
        batch:
          strategy:
            batchStrategyDSL(filePathDSL.toString)
          aggregate:
            batchAggregation

    val appStandard = ScoobyRunnable(
      Configuration(
        CrawlerConfiguration(
          URL(url),
          ExplorationPolicies.allLinks,
          maxDepth,
          ClientConfiguration(timeout, maxRequest, Map(auth))
        ),
        ScraperConfiguration(scrapeToIter),
        ExporterConfiguration(Seq(
          BatchExporting(
            (res: Result[HTMLElement]) =>
              batchStrategy(filePathStandard.toString)(res.data),
            (res1: Result[HTMLElement], res2: Result[HTMLElement]) => 
              Result(batchAggregation(res1.data, res2.data))
          ))),
        CoordinatorConfiguration(maxLinks)
      )
    )

    val resultDSL: Map[String, Int] = resultsToCheckAsMapWithSize(
      Await.result(appDSL.run(), 10.seconds), _.tag
      )
    val resultStandard: Map[String, Int] = resultsToCheckAsMapWithSize(
      Await.result(appStandard.run(), 10.seconds), _.tag
      )

    resultDSL shouldBe resultStandard
```

#### Cucumber features

The domain entity Scraper has been tested also using cucumber suite test, by defining features and their implementation.

```Scala
Feature: Scraper data filtering.

  Scenario: No matching after data filtering
  Given I have a scraper with a proper configuration
  And   I have a document with no matching
  When  The scraper applies the policy
  Then  It should send an empty result

Given("""I have a scraper with a proper configuration""") : () =>
  val selectors: Seq[String] = Seq("li", "p")
  scraperActor = testKit.spawn(Scraper(exporterProbe.ref,
   ScraperPolicies.scraperPolicy(selectors, "tag")))
   
And("""I have a document with no matching""") : () =>
  docContent =
    s"""
     |<html lang="en">
     |<head>
     |  <title>Basic HTML Document</title>
     |</head>
     |</html>
     |""".stripMargin
  docUrl = URL.empty
  scrapeDocument = ScrapeDocument(docContent, docUrl)
  result = Result.empty[String]
  
When("""The scraper applies the policy""") : () =>
  scraperActor ! ScraperCommands.Scrape(scrapeDocument)
  
Then("""It should send an empty result""") : () =>
  exporterProbe.expectMessage(ExporterCommands.Export(result))
```
