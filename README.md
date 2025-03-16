# TROLIE Java Client SDK

This project is a Java client for using the server APIs exposed by 
the Transmission Ratings and Operating Limits Information Exchange
([TROLIE](https://trolie.energy/)).  Specifically, it provides an 
idiomatic Java API to the TROLIE OpenAPI specification, as defined 
at https://trolie.energy/spec.  It also provides extensions for key 
behaviors of specific ISO/RC implementations, such as authentication.  

## Documentation and Usage
Users are encouraged to start with usage examples or interacting with a TROLIE server:

* [Submitting Forecast Ratings](java-client-examples/src/main/java/energy/trolie/client/examples/PublishForecastProposals.java)
* [Subscribing to Forecast Snapshots](java-client-examples/src/main/java/energy/trolie/client/examples/SubscribeToForecastSnapshots.java)

Also see the [JavaDoc](https://trolie.github.io/java-client-sdk/).  
The [TrolieClient](https://trolie.github.io/java-client-sdk/energy/trolie/client/TrolieClient.html) class is a good starting point.

## Minimal Footprint

This client SDK is designed with the intent that it is easy to use
in a variety of frameworks, with a low dependency footprint.  It should
be easy to adapt in most popular Java frameworks, such as Spring Boot or
Quarkus, but does not require use of any specific framework.

It is also built with Maven, specifically because it offers the 
broadest array of compatibility.  Maven artifacts can easily be used
by other build tools such as Gradle and SBT.  

## Best Practices

This library will include best practices around usage.  This includes:

* Supporting automatic retries on I/O failures.  
* Built-in support for compression.
* Built-in support for [conditional GETs](https://trolie.energy/articles/conditional-GET.html) in TROLIE.

