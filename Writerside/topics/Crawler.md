# Crawler

A Crawler is a system entity responsible for searching explorable links inside a web page. It interacts with the 
coordinator to validate the found urls and is responsible for creating scrapers, for extracting data from a page, 
and new crawlers to continue analysing the website.

We can summarize the interaction between the Crawler and the other components with the following:
```plantuml
@startuml Crawler Sequence
 
    ?-> Crawler: Crawl(URL)
    activate Crawler
    Crawler -> Crawler: Extract link urls 
    Crawler -> Coordinator: CheckPages(list of extracted url)
    activate Coordinator
    Coordinator -> Coordinator: Check links
    Coordinator -> Crawler: CrawlerCoordinatorResponse(valid links)
    deactivate Coordinator
    
    create Scraper
    Crawler -> Scraper: Scrape(Document)
    
    loop valid links
        create Subcrawler
        Crawler -> Subcrawler: Crawl(URL)
    end

@enduml
```

## Structure

```plantuml
@startuml Crawler
    enum CrawlerCommand {
        Crawl(url:URL)
        CrawlerCoordinatorResponse(result: Iterator[URL])
        ChildTerminated()
    }
    
    class ClientConfiguration <<Case Class>>
    
    protocol ExplorationPolicy
    
    class Crawler <<(A, #FF7700) Actor>>{
        coordinator: ActorRef[CoordinatorCommand],
        exporter: ActorRef[ExporterCommands],
        explorationPolicy: ExplorationPolicy
        clientConfiguration: ClientConfiguration,
    }
    
    class Coordinator <<(A, #FF7700) Actor>>
    class Scraper <<(A, #FF7700) Actor>>
    
    Crawler ..> CrawlerCommand: <<uses>>
    Crawler ..> ClientConfiguration: <<uses>>
    Crawler ..> ExplorationPolicy: <<uses>>
    
    Crawler .u.> Coordinator: <<signal>>
    Coordinator ..> Crawler: <<signal>>
    
    
    Crawler .l.> Scraper: <<creates>>
    Crawler ..> Crawler: <<creates>>
    
    
@enduml
```

## Exploration Policy

An Exploration Policy describe the way crawlers fetch links from a page. It's represented by a function that receive as input a
HTML Document (Crawl Document) and that returns an iterable of URLs.

As example, we can describe an exploration policy that only fetch same domain urls:

```Scala
def sameDomainLinks: ExplorationPolicy = (document: CrawlDocument) =>
    document.frontier.filter(_.domain == document.url.domain)
```
It's important to note that all crawlers will assume the same exploration policy inside the system and that should be configured
at application startup.
