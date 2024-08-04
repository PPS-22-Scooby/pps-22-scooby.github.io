# Requirements

## Business Requirements

The project aim to build an application that allow users to scrape and crawl web pages.

Main entities involved:
- **Crawler**: Entity which navigates through web pages' links, building a data structure which contains all visited pages; 
- **Scraper**: Entity which collects data from web pages;
- **Exporter**: Entity which exports collected data in a readable format.

Main application parameters should be provided by users in an efficient and easy manner, i.e. defining a DSL to
interact with the application.

## Functional Requirements

### Application

- Application must be able to crawl and scrape web pages from specified URLs;
- Support for crawling websites with various structures;
- Ability to extract specific data points, such as text, links, and metadata;
- Application should support multiple web scraping strategies, such as CSS selectors and Regular Expressions;
- Application should support distributed crawling, with the ability to scale horizontally by adding more nodes;
- Implement network configurations to enhance the possibility to run on a wider range of websites (i.e. the ones which require user authentication);
- Implement fault tolerance to handle errors gracefully;
- Graceful handling of network issues, server timeouts, and other potential disruptions;
- Application should log all errors and provide detailed reports for troubleshooting;
- Extracted data should be stored in a structured format, such as JSON;
- Support for exporting data in various formats, including JSON;
- Implement mechanisms to avoid multiple visits on same pages; 
- Application should include mechanisms to respect the robots.txt file;
- Application should support HTTPS protocol to interact with websites.

### Configuration

- All the application parameters should be easy to configure and customize;
- A DSL should be provided to interact with the application.

## Non-functional Requirements

- Application should be able to manage large amount of data;
- DSL configurations should be intuitive, user-friendly and easy to customize;
- The whole project should lay on a robust documentation for all the features;
- Codebase should be modular to facilitate future updates and maintenance;
- Application should include automated testing to ensure reliability during updates.

## Assumptions

Given the variety of policies that web servers adopt to avoid automatic traffic, we assume for a matter of
simplicity that target websites will not implement significant anti-scraping measures. There won't be workaround to
succeed CAPTCHA tests for instance.

## Implementation

- Scala v3.4.2
- ScalaTest v3.2.18
- JDK 11
- CucumberScala v8.23.0

## Optional Requirements

- Users should be able to stop and resume the application;
- Exporter phase should lay on multiple exporters to enhance system performances;
- A GUI should be implemented to enhance data visualization.
