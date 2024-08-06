# Coordinator

## Robots.txt
When you want to scrap web pages, it is important to take into account the robots.txt file of the domain you want to analyze.
Within this file are directives that indicate for which robots certain paths are available and unavailable.
In order to analyze them, a parser was created that takes as input the string contained in robots.txt and goes to convert the contents of the file into a list of disallowed paths.