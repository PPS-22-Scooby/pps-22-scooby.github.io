# Sprint 8: Refactoring and bug fixing

| Ticket Id | Summary                                    | Backlog item          | Assigned to                                                         | Estimation | Actual | Type          |
|-----------|--------------------------------------------|-----------------------|---------------------------------------------------------------------|------------|--------|---------------|
| PPS-90    | JSON format DSL fix                        | DSL fix               | Giovanni Antonioni                                                  | 5h         | 5h     | Fix           |
| PPS-86    | Implement scrape tests                     | DSL test              | Giovanni Antonioni                                                  | 4h         | 4h     | Test          |
| PPS-80    | Spawn entities anonymously                 | Actor implementation  | Giovanni Antonioni                                                  | 2h         | 2h 30m | Fix           |
| PPS-81    | Remove rules                               | Utilities refactoring | Giovanni Antonioni                                                  | 10m        | 10m    | Refactoring   |
| PPS-72    | Implement Ruling keywords in the DSL       | DSL                   | Giovanni Antonioni                                                  | 2h         | 2h     | Dev           |
| PPS-97    | Implement test of whole application        | Test                  | Luca Rubboli                                                        | 3h         | 3h     | Test          |
| PPS-116   | Fix Robot.txt                              | Enhancement           | Francesco Magnani                                                   | 30m        | 2h     | Fix           |
| PPS-98    | Build the fat JAR                          | CI                    | Francesco Magnani                                                   | 30m        | 1h 30m | Configuration |
| PPS-78    | Implement safety mechanisms for DSL usage  | DSL                   | Francesco Magnani                                                   | 3h         | 2h     | Enhancement   |
| PPS-88    | Implement ScoobyTest                       | Test                  | Francesco Magnani                                                   | 1h         | 1h     | Test          |
| PPS-82    | Remove compilation warnings                | Enhancement           | Valerio Di Zio, Luca Rubboli                                        | 1h         | 1h     | Fix           |
| PPS-75    | Check and refactoring                      | Enhancement           | Giovanni Antonioni, Valerio Di Zio, Luca Rubboli, Francesco Magnani | 4h         | 4h     | Refactoring   |
| PPS-74    | Implement Headers logic in DSL             | DSL                   | Valerio Di Zio                                                      | 3h 30m     | 5h     | Dev           |
| PPS-56    | clean up your code before dsl              | Enhancement           | Giovanni Antonioni, Valerio Di Zio, Luca Rubboli, Francesco Magnani | 3h         | 3h 30m | Refactoring   |
| PPS-92    | Remove some warning inside code            | Enhancement           | Valerio Di Zio                                                      | 1h         | 1h     | Refactoring   |
| PPS-91    | Unused import removal                      | Enhancement           | Valerio Di Zio                                                      | 1h         | 1h     | Refactoring   |
| PPS-120   | Apply export refactor and scraper refactor | Enhancement           | Luca Rubboli                                                        | 1h         | 1h     | Refactoring   |
| PPS-85    | Implement crawl tests                      | DSL test              | Valerio Di Zio                                                      | 2h         | 2h     | Test          |
| PPS-79    | Implement HTML element method "parent"     | Document library      | Francesco Magnani                                                   | 30m        | 30m    | Enhancement   |
| PPS-84    | Implement exporter tests                   | DSL test              | Luca Rubboli                                                        | 7h         | 6h     | Test          |
| PPS-87    | Implement config tests                     | DSL test              | Francesco Magnani                                                   | 1h         | 1h     | Test          |
| PPS-89    | Scooby termination fix                     | Actor orchestration   | Francesco Magnani                                                   | 30m        | 2h     | Fix           |
| PPS-73    | Implement Export keywords in the DSL       | DSL                   | Luca Rubboli                                                        | 1d         | 1d 2h  | Dev           |

## Sprint goal

Goals of this sprint:
- Code clean-up and refactoring;
- DSL application test against standard configuration and application usage in common websites;
- Arising bug fixing;
- DSL refinements;
- Fat JAR build.

## Sprint deadline

28/07/2024

## Sprint review

All tasks but exception management of actors execution has been completed. All bugs arisen from application's test usage have been promptly fixed due to clean code. Some DSL refinements in backlog took a bit of time.

## Sprint retrospective

Given the variety of bugs arisen from application usage, this sprint focused on bug fixing and application usage. The most challenging part was about setting-up the wider variety of edge cases possible.
