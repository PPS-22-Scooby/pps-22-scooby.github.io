# In-Depth Design

## Project Structure
The structure of the project is divided into three principal components: the DSL, the core, and the utils:

- The **core** component is the heart of the system, containing the main entities and the logic to manage them.
- The **DSL** components manages the way the user configures the system using our custom internal domain-specific language.
- The **utils** component contains utility classes and functions.

```plantuml
@startuml
     
    component DSL {
        [Syntax]
        [Configuration]
        
        [Configuration] ..> [Syntax]
        [Syntax] ..> [Configuration]
    }
    
    component core {
        [Crawler]
        [Scraper]
        [Coordinator]
        [Exporter]
        [Scooby]
        
        [Crawler] ..> [Coordinator]
        [Coordinator] ..> [Crawler]
        
        [Crawler] ..> [Scraper]
        [Scraper] ..> [Crawler]
        
        [Exporter] ..> [Scraper]
        
        [Scooby] ..> [Crawler]
        [Scooby] ..> [Scraper]
        [Scooby] ..> [Exporter]
        [Scooby] ..> [Coordinator]
    }
    
    component utils {
        [Document]
        [HTTP]
    }
    
    core --> scoobyConfiguration
    core .u.> utils
    DSL ..> scoobyConfiguration
  
    DSL -d-> userDSL
@enduml
```

# Components

## Core
The core package of the Scooby library contains the main entities that are involved in the scraping process. These are  the `Crawler`, `Scraper`, `Coordinator`, and `Exporter`. `Scooby` 
is the entity responsible for the system start-up and management.

### Crawler
```plantuml
@startuml Crawler
    hide empty members
    interface Document <<trait>>
    interface CrawlDocument <<trait>> extends Document 
    interface HTTP <<trait>>
    
    class Crawler <<(A, #FF7700) Actor>>
    
    Crawler ..> CrawlDocument: <<uses>>
    Crawler ..> HTTP: <<uses>>
@enduml
```
A [Crawler](Crawler.md) is the actor responsible for searching and exploring links on web pages. It interacts with a coordinator to validate found URLs, creates [scrapers](Scraper.md) to extract data, and 
spawns new crawlers to explore new URLs.A is able to download the content of a web page using the [HTTP](HTTP.md) utility class and parse it with the [Document](Document.md) component of the `utils` package.

#### Crawler Messages
| Message | Description                                                                                                |
|---------|------------------------------------------------------------------------------------------------------------|
| `Crawl(url: URL)` | Start crawling a specific url.                                                                             |
| `CrawlerCoordinatorResponse(result: Iterator[URL])` | Receive a reponse from the coordinator. This message should be sent only by the Coordinator of the systekm |
| `ChildTerminated()` | Signal this crawler that one of it's sub-cralwer has terminated its computation.                           |

### Scraper
A [Scraper](Scraper.md) is an actor responsible for extracting data from a web page. It receives a document from a crawler, 
extracts the relevant information, and sends the results to an exporter.

```plantuml
@startuml Scraper
    hide empty members
    interface Document <<trait>>
    interface ScrapeDocument <<trait>> extends Document 
    interface Result <<trait>>
    
    class Scraper <<(A, #FF7700) Actor>>
    class Exporter <<(A, #FF7700) Actor>>
    
    Scraper ..> ScrapeDocument: <<uses>>
    Scraper ..> Result: <<creates>>
    Scraper ..> Exporter: <<signal>>
    
    Exporter ..> Result: <<uses>>
@enduml
```

#### Scraper Messages
| Message                                             | Description                                                              |
|-----------------------------------------------------|--------------------------------------------------------------------------|
| `Scrape(document: ScrapeDocument)`                  | Starts to scrape a specific document |

### Coordinator
The Coordinator is an actor that validates the URLs found by Crawlers. Usually the checks are based on a set of rules defined by the user, defining a 
policy that dictates which URLs are valid and which are not. Coordinators also control's if a url was already visited by a crawler and if it's allowed in the 
robot file of the website.


```plantuml
@startuml Coordinator
    hide empty members
    class Crawler <<(A, #FF7700) Actor>>
    class Coordinator <<(A, #FF7700) Actor>>
    interface CoordinatorPolicy <<trait>>
    
    class RobotParser
    
    Crawler ..> Coordinator: <<signal>>
    Coordinator ..> Crawler: <<signal>>
    Coordinator ..> RobotParser: <<uses>>
    Coordinator *-- CoordinatorPolicy
@enduml

```

#### Coordinator Messages
| Message                                                                       | Description                                          |
|-------------------------------------------------------------------------------|------------------------------------------------------|
| `SetupRobots(url: URL)`                                                       | Parse and obtain rules for the robot file of the url |
| `CheckPages(pages: List[URL], replyTo: ActorRef[CrawlerCoordinatorResponse])` | Check the pages fetched by a Crawler                 |
| `SetCrawledPages()`                                                           | Set a predefined set of crawled page.                |

### Exporter
The [Exporter](Exporter.md) is an actor responsible for exporting the scraped data. It receives the results from a [Scraper](Scraper.md) and exports them in a specific format.
Scooby supports two types of exporters: `StreamExporter` and `BatchExporter`. The former exports data as soon as it is scraped, while the latter aggregates the results and exports them all at once.
For both kind of exporters, is possible to define a behaviour that specify the format of the output and how to export it.


```plantuml
@startuml Exporter
    hide empty members
    interface Result <<trait>>
    
    class Exporter <<(A, #FF7700) Actor>>
    
    class Scraper <<(A, #FF7700) Actor>>
    
    class StreamExporter <<(A, #FF7700) Actor>> implements Exporter {
        exportingBehavior: Result[T] => Unit
    }
    
    class BatchExporter <<(A, #FF7700) Actor>> implements Exporter {
        exportingBehavior: Result[T] => Unit
        aggregationBehavior: (Result[T], Result[T]) => Result[T]
    }

    class Result {
        data: Iterable[T]
    }
    
    Exporter ..> Result: <<uses>>
    Scraper ..> Result: <<creates>>
    Scraper ..> Exporter: <<signal>>
@enduml
```
#### Exporter Messages
| Message | Description |
|---------|-------------|
| `Export(result: Result[T])` | Export the result of a scraping operation. |
| `SignalEnd(replyTo: ActorRef[ScoobyCommand])` | Signal the end of the export process. |

### Scooby
Scooby is the main entity of the system, responsible for starting the system and managing the entities. 
It receives a `Configuration` object that describes the desired settings and starts the system accordingly.

```plantuml
@startuml
    hide empty members
    class Scooby <<(A, #FF7700) Actor>>
    class Configuration
    
    
    class CrawlerConfiguration extends Configuration
    class ScraperConfiguration extends Configuration
    class ExporterConfiguration extends Configuration
    class CoordinatorConfiguration extends Configuration
    
    Scooby ..> Configuration: <<uses>>  
@enduml
```

#### Scooby Messages
| Message | Description                                                          |
|---------|----------------------------------------------------------------------|
| `Start` | Starts the application.                                              |
| `RobotsChecked(found: Boolean)` | Signal that the operations for checking the Robot file are finished. |
| `ExportFinished` | Signal the end of exporting operations.                              |


## DSL
The [DSL](DSL.md) component is responsible for managing the way the user configures the system using our custom internal domain-specific 
language. Every main entity in the system has a corresponding set of operations that can be used for produce a desired configuration
described by a `Configuration` object, that will then be used by the `Scooby` entity to start the system.

```plantuml
@startuml
    
    [Entrypoint] ..> [Macros]
    
    [Entrypoint] ..> [DSL]
    [Entrypoint] ..> [Config]
    [Crawl] ..> [DSL]
    [Scrape] ..> [DSL]
    [Export] ..> [DSL]
    [HTML] ..> [DSL]
@enduml
```

## Utils
The `utils` component contains utility classes and functions that are used by the core entities. 
`Document` represents an HTML document that is being fetched from a URL while the `HTTP` trait is used to download
the content of a web page.

### Document
```plantuml
@startuml Document
    hide empty members
    class Document
    
    class ScrapeDocument extends Document 
    class CrawlDocument extends Document 
    
    interface HTMLExplorer <<trait>>
    
    interface LinkExplorer <<trait>> extends HTMLExplorer, RegExpExplorer
    interface EnhancedLinkExplorer <<trait>> extends HTMLExplorer
    
    interface SelectorExplorer <<trait>> extends HTMLExplorer
    
    interface CommonHTMLExplorer <<trait>> extends HTMLExplorer
    
    interface RegExpExplorer <<trait>> extends HTMLExplorer
    
    LinkExplorer .u.> CrawlDocument: <<mixin>>
    
    EnhancedLinkExplorer .u.> CrawlDocument: <<mixin>>
    
    SelectorExplorer .u.> ScrapeDocument: <<mixin>>
    CommonHTMLExplorer .u.> ScrapeDocument: <<mixin>>
    RegExpExplorer .u.> ScrapeDocument: <<mixin>>
    
@enduml
```

[Document](Document.md) allows to easily retrieve and work with the content of a web page. Crawlers and Scrapers use this 
feature to parse the content of a page and extract the relevant information. The operation that are allowed on a document are
defined by the Explorer mixins that are used. In our application we've two kind of document:
- `CrawlDocument`: For crawling operations
- `ScrapeDocument`: For scraping operations

The former should only have operations for fetching links, so the `LinkExplorer` and `EnhancedLinkExplorer` mixins are used
while, for the latter, we used the `SelectorExplorer`, `CommonHTMLExplorer` and `RegExpExplorer` mixins, enabling document
to extract data from the page.

### HTTP

[HTTP](HTTP.md) is a utility component that allows to wrap a HTTP client library for download and parse the content of a 
web page with a given simple and easy to use API.

```plantuml
@startuml
hide empty members
class Request

class HttpError
interface Backend<R> << trait >>

class RequestBuilder
RequestBuilder ..> Request: <<uses>>

class HttpClient

Backend --|> HttpClient
Backend <.. Request: <<uses>>
HttpClient <. Request: <<uses>>
HttpClient ..> ClientConfiguration: <<uses>>
Request ..> HttpError: <<uses>>
class ClientConfiguration
@enduml
```
