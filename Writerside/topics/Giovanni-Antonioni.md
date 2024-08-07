# Giovanni Antonioni

The main areas that I've contributed to on the implementation side include:

- [Crawler](Crawler.md);
- [Exporter](Exporter.md);
- Scraper ruling system on the [DSL](DSL.md) component

As side works I've also worked on setting up the CI/CD pipeline and the documentation system for the project.

### Crawler

For the [Crawler](Crawler.md) component I've followed the Akka's [FSM design principle](https://doc.akka.io/docs/akka/current/typed/fsm.html) 

using the Behavior DSL. Is possible to identifying the following two states:
- The `idle` state, where the actor receives the url to crawl and starts the process of checking the documents frontier, starts a scraper children actor and the sub crawlers.
- The `waitForChildren` state, where the actor awaits if the spawned children to terminate their computations 

Each of the following state is managed by a specific function that handle the state transition and the message processing.

```Scala
    def idle(): Behavior[Command] = 
        Behaviors.receiveMessage:
          case Crawl(url) => crawl(url)
          case x: ChildTerminated => visitChildren(links)
            buffer.stash(x)
            Behaviors.same
        
    private def waitingForChildren(alive: Int): Behavior[CrawlerCommand] =
        context.log.info(s"${context.self.path.name} -> Children alive: $alive")
        if alive == 0 then
          context.log.info(s"Crawler ${context.self.path.name} has no child -> Terminating")
          Behaviors.stopped
        else
          Behaviors.receiveMessage:
            case ChildTerminated() =>
              context.log.info(s"Child terminated")
              waitingForChildren(alive - 1)
            case _ => Behaviors.same 
        
```

#### Crawler's Exploration Policy

An Exploration Policy describe the way crawlers fetch links from a page. It's represented by a function that receive as input a
HTML Document (Crawl Document) and that returns an iterable of URLs.

```Scala
type ExplorationPolicy = CrawlDocument => Iterable[URL]
```

Defining an exploration policy in terms of a function allows to easily change the behavior of the crawler and to extend
it with new functionalities.

As example, we can describe an exploration policy that only fetch same domain urls:

```Scala
def sameDomainLinks: ExplorationPolicy = (document: CrawlDocument) =>
    document.frontier.filter(_.domain == document.url.domain)
```

It's important to note that all crawlers will assume the same exploration policy inside the system and that should be configured
at application startup.

### Exporter
Similar to [Crawler](Crawler.md) the [Exporters](Exporter.md) is also designed as an actor entity that awaits to receive a Result message from a [Scraper](Scraper.md) 
containing partial data from the scraping process. Based on the type of Exporter the final result is handled differently: with the `StreamExporter` is 
processed immediatelly:

```Scala
def stream[A](exportingFunction: ExportingBehavior[A]): Behavior[ExporterCommands] =
    Behaviors.setup : context =>
      Behaviors.receiveMessage :
        case Export(result: Result[A]) =>
          Try:
            exportingFunction(result)
          .fold(e => println(s"An error occurred while exporting in stream config: $e"), identity)
          Behaviors.same
        case SignalEnd(replyTo) =>
          context.log.warn("Ignoring batch results inside Stream exporter")
          replyTo ! ExportFinished
          Behaviors.stopped
```

while with the `BatchExporter` is accumulated until the end of the scraping process:

```Scala
def fold[A](result: Result[A])
         (exportingFunction: ExportingBehavior[A])
         (aggregation: AggregationBehavior[A]): Behavior[ExporterCommands] =
Behaviors.setup : context =>
  Behaviors.receiveMessage :
    case Export(newResult: Result[A]) =>
      fold(aggregation(result, newResult))(exportingFunction)(aggregation)
    case SignalEnd(replyTo) =>
      Try:
        exportingFunction(result)
      .fold(e => println(s"An error occurred while exporting in batch config: $e"), identity)
      replyTo ! ExportFinished
      Behaviors.stopped

def batch[A](exportingFunction: ExportingBehavior[A])
          (aggregation: AggregationBehavior[A]): Behavior[ExporterCommands] =
fold(Result.empty[A])(exportingFunction)(aggregation)
```

We can control different aspects for exporters, as the aggregation behavior, the exporting behavior and the output format.
These are all defined as custom Scala types that are passed to the constructor of the Exporter during its creation:

```Scala
type ExportingBehavior[A] = Result[A] => Unit
type AggregationBehavior[A] = (Result[A], Result[A]) => Result[A]
type FormattingBehavior[A] = Result[A] => String
```

### Rule system DSL
The part I've implemented on the DSL side is the rule system for the scraper. 
The rule system is a set of keywords that allow to define which elements of the page should be scraped based on different 
conditions.

When an user define a `scrape` block on the DSL snippet, a ScrapingContext is opened and it's possible to sets a series
of rules to define the scraping policy. 

An example of DSL:

```Scala
scrape:
  elements that :
    haveAttributeValue("href", "level1.1.html") and haveClass("amet") or followRule {
      element.id == "ipsum"
    }
```
In the example above, the `that` keyword is an alias for the Scala collection method `filter` while, 
`haveAttributeValue`, `haveClass` and `followRule` are methods that generates a predicate for HTML elements.

```Scala
inline def followRule(block: RuleDefinitionScope): HTMLElementRule =
    catchRecursiveCtx[HTMLElement]("rule")
    el =>
      given HTMLElement = el
      block

infix def haveAttributeValue(attributeName: String, attributeValue: String): HTMLElementRule =
    _.attr(attributeName) == attributeValue

infix def haveClass(cssClass: String): HTMLElementRule = _.classes.contains(cssClass)
```
Note that the `followRule` uses a method (`catchRecursiveCtx[HTMLElement]("rule")`) for checking the context in which it's used.
This prevent to recursively use the `followRule` keyword inside another `followRule` block.
