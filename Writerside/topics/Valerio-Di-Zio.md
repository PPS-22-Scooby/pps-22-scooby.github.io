# Valerio Di Zio

The parts I focused on most during development were:
- [Coordinator](Coordinator.md) actor;
- Utility that takes care of managing [Robots.txt](Coordinator.md#robots-txt) files on Websites;
- [Crawler](Crawler.md) actor;
- Configuration Class;
- [DSL](DSL.md) rules for using the crawler and headers;
- MockServer useful for testing;
- Tests suites related to previous topics.


Further details of implementation for the most relevant parts are described in the following sections.

### Coordinator
The coordinator is the actor with which the various crawlers interact to determine whether they can visit a URL. 
This decision is based on URLs that have been previously visited and the restrictions specified in the robots.txt file, which lists the URLs that crawlers are not authorized to access.

For the Coordinator component I've followed the Akka's [FSM design principle](https://doc.akka.io/docs/akka/current/typed/fsm.html) using the Behavior DSL. 
Is possible to define the following states:
- The `SetupRobots` state, called at application startup to allow the coordinator to have the updated list of "Disallow" links in the Robot.txt;
- The `CheckPages` state, used when a *crawler* needs to know whether it can visit a URL and to update the list of those already visited during execution.

```Scala
  def idle(crawledUrls: Set[URL], blackList: Set[String]): Behavior[CoordinatorCommand] =
    Behaviors.receiveMessage {
      case SetupRobots(url, replyTo) =>
        val disallowed = Robots.getDisallowedFromRobots(url)
        replyTo ! RobotsChecked(disallowed.nonEmpty)
        idle(crawledUrls, disallowed)

      case CheckPages(urls, replyTo) =>
        val checkResult = urls.filter(_.isAbsolute)
          .filter(page => policy(page, crawledUrls))

        val checkedUrlAndBlackList = checkResult.filter(url => Robots.canVisit(url.toString, blackList))
        replyTo ! CrawlerCoordinatorResponse(checkedUrlAndBlackList.iterator)
        idle(crawledUrls ++ checkResult.toSet, blackList)
        
      case null => Behaviors.same
    }
```

### Robots.txt
For the translation of the robots.txt file into a set of non-visitable paths,
a parser was created that takes the contents of the robots.txt file as input and translates it into a set of non-visitable paths
```Scala
  def parseRobotsTxt(robotsTxt: String): Set[String] =
    if robotsTxt.isEmpty then return Set.empty
    val lines = robotsTxt.split("\n").toList
    val initialState = (Option.empty[String], List.empty[String])

    val (_, disallowRules) = lines.foldLeft(initialState):
      case ((userAgent, disallowRules), line) =>
        val trimmedLine = line.trim
        if (trimmedLine.startsWith("#"))
          // Skip comments
          (userAgent, disallowRules)
        else
          trimmedLine.split(":", 2) match
            case Array("User-agent", ua) =>
              (Some(ua.trim), disallowRules)
            case Array("Disallow", path) if userAgent.contains("*") && path.trim.nonEmpty =>
              (userAgent, path.trim :: disallowRules)
            case _ =>
              (userAgent, disallowRules)
    disallowRules.toSet
```
Then the coordinator will retrieve this list and prevent a crawler from parsing the paths specified as ‘Disallow’ in robots.txt

### Crawler
My contribution to the creation of the crawler was to allow interaction with the coordinator, 
and based on the response, to go and "spawn" new crawlers.
```Scala
Behaviors.receiveMessage:
      case Crawl(url) => crawl(url)
      case CrawlerCoordinatorResponse(links) => visitChildren(links) 
```
`visitChildren()` method launches other crawlers based on the coordinator response.

```Scala
val documentEither: Either[HttpError, CrawlDocument] = GET(url)
documentEither match
    case Left(e) => handleError(e)
    case Right(document) =>
      scrape(document)
      if maxDepth > 0 then
        checkPages(document) // Communication with coordinator
        Behaviors.same
      else
        ... //Max depth reached
```
```Scala
def checkPages(document: CrawlDocument): Unit =
    this.coordinator ! CoordinatorCommand.CheckPages(...)
```

## DSL
Regarding DSL, my contribution is about the keyword: `crawl` and allowing `headers` to be defined directly in the config.

### Crawl keyword
The DSL operators defined in the **Crawl** object are designed to allow smooth and readable crawler configuration through natural language-like syntax. These operators are used to specify where to start browsing and what crawling policies to adopt.

```Scala
case class CrawlContext(var url: String, var policy: ExplorationPolicy)
```
A case class that represents the execution context for the crawl configuration. It contains two variables: _url_ and _policy_.
- url: represents the URL from which the crawler starts browsing.
- policy: an instance of ExplorationPolicy that defines the crawler's exploration strategy.

```Scala
inline def crawl[T](block: CrawlScope)(using globalScope: ConfigurationWrapper[T]): Unit =
  catchRecursiveCtx[CrawlContext]("crawl")
  crawlOp(block)
```
- Inline method to configure the crawl context. It uses `catchRecursiveCtx` to prevent recursive calls and establishes a _CrawlContext_.
- It is used in conjunction with a globalScope that represents the global configuration of the application.

### Headers keyword
The DSL provides a way to specify configurations such as network settings, headers for HTTP requests, and other options in a structured and readable manner.
My role was to provide support for specifying headers to be used in the HTTP request.
```Scala
case class HeadersContext(var headers: Map[String, String])
```
This class holds a mutable map that represents the headers. 
The map is initially empty when the context is created and is updated as headers are defined within the DSL block.

```Scala
extension (x: String)
  infix def to(value: String)(using context: HeadersContext): Unit = 
    context.headers = context.headers + (x -> value)
```
This syntax sugar allows the user to write "HeaderName" to "HeaderValue" within a headers block.

## Testing
### MockServer
During the development of our application, it became apparent that reproducibility was necessary, and using real websites to test the application’s functionality was impractical for two main reasons:
1. The structure of the HTML could change. 
2. Necessary tags and information might not consistently be available on a website to thoroughly test certain functionalities.

To address this issue, a MockServer was created. This server is specifically designed to provide HTML resources solely for testing purposes and is shut down once testing is complete.

### Cucumber and Unit Testing
Cucumber tests and unit tests were implemented to ensure the comprehensive verification of both the overall system behavior and the functionality of individual components. 

Specifically, Cucumber tests were used to validate the system’s behavior from an end-to-end perspective, ensuring that it meets the specified requirements and user expectations. 
On the other hand, unit tests were employed to rigorously check each component’s functionality in isolation, utilizing ScalaTest as the testing framework. 
This dual approach helps in identifying and addressing issues at different levels, thus contributing to the overall reliability and robustness of the application.


