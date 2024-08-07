# Crawler

A **Crawler** is a system entity responsible for searching explorable links inside a web page. It interacts with the 
[Coordinator](Coordinator.md) to validate the found urls and is responsible for creating scrapers, for extracting data from a page
and new crawlers exploring new urls.

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

Each time a Crawler found a new valid url it spawns a new child crawler that will analyze it. When the analysis of the page 
is complete, a crawler will continue to signal it to the parent, then when a crawler no longer has an active child
it's removed from the system.

## Structure

```plantuml
@startuml Crawler
    hide empty members
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
