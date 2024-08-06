# Coordinator
The **Coordinator** is an actor that validates the URLs found by Crawlers. 
The checks are based on a set of rules defined by the user, defining a policy that dictates which URLs are valid and which are not. 
Coordinators also control if a URL was already visited by a crawler and if it's allowed by the robot.txt file of the website.

```plantuml
@startuml
participant Crawler
actor Coordinator
activate Coordinator
Coordinator -> Coordinator: Setup robot
activate Coordinator
Coordinator -> Coordinator: Update disallowed list
deactivate Coordinator
?-> Crawler: Crawl(URL)
activate Crawler
Crawler -> Crawler: Find links

Crawler -> Coordinator: Check links
Coordinator -> Coordinator: Update already visited
Coordinator -> Crawler: Allowed links

Crawler -> Crawler: Spawn children
@enduml
```

## Structure

```plantuml
@startuml Coordinator
    hide empty members
    
    class Crawler <<(A, #FF7700) Actor>>
    
    class Coordinator <<(A, #FF7700) Actor>>{
        context: ActorContext[CoordinatorCommand]
        maxNumberOfLinks: Int
        policy: Policy
    }
    
    enum CoordinatorCommand{
        SetupRobots(page: URL, replyTo: ActorRef)
        CheckPages(pages: List[URL], replyTo: ActorRef)
    }
    
    class CoordinatorPolicies <<(O, #906901) Object>> {
        defaultPolicy(): Policy
    }
    
    class Robots <<(O, #906901) Object>>{
        fetchRobotsTxt(url: URL): Option[String]
        parseRobotsTxt(robotsTxt: String): Set[String]
    }
    
    protocol Policy 
    
    Crawler ..> Coordinator: <<signal>>
    Coordinator ..> Crawler: <<signal>>
    Coordinator ..> CoordinatorCommand: <<uses>>
    Coordinator ..> Robots: <<uses>>
    Coordinator ..> CoordinatorPolicies: <<uses>>
    CoordinatorPolicies ..> Policy: <<uses>>
@enduml
```


## Robots.txt
When undertaking web scraping, it is crucial to consider the robots.txt file associated with the domain under analysis. 
This file contains directives that specify which paths are permissible or restricted for various web crawlers.