# Coordinator
The **Coordinator** is an actor that validates the URLs found by Crawlers. 
Usually the checks are based on a set of rules defined by the user, defining a policy that dictates which URLs are valid and which are not. 
Coordinators also control's if an url was already visited by a crawler and if it's allowed in the robot file of the website.

```plantuml
@startuml
participant Crawler
participant Robots
actor Coordinator

Coordinator -> Robots: <<setup robot>>
Robots -> Coordinator: <<disallowed list>>
Coordinator -> Coordinator: <<update disallowed>>

Crawler -> Crawler: <<root crawler creation>>
Crawler -> Crawler: <<find links>>

Crawler -> Coordinator: <<check links>>
Coordinator -> Coordinator: <<update already visited>>
Coordinator -> Crawler: <<allowed links>>

Crawler -> Crawler: <<spawn children>>
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
    
    protocol CoordinatorPolicy {
        defaultPolicy(): Policy
    }
    
    class Robots <<(O, #906901) Object>>{
        fetchRobotsTxt(url: URL): Option[String]
        parseRobotsTxt(robotsTxt: String): Set[String]
    }
    
    Crawler ..> Coordinator: <<signal>>
    Coordinator ..> Crawler: <<signal>>
    Coordinator ..> CoordinatorCommand: <<uses>>
    Coordinator ..> Robots: <<uses>>
    Coordinator *-- CoordinatorPolicy
@enduml
```


## Robots.txt
When undertaking web scraping, it is crucial to consider the robots.txt file associated with the domain under analysis. 
This file contains directives that specify which paths are permissible or restricted for various web crawlers.