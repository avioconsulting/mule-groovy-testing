For dealing with the fact that sometimes we need to inject partial Maven dependencies into a test run (main use case here is when we have a SOAP service and a domain. The SOAP service does not have an HTTP connector dependency and the testing framework does not include domain dependencies, so we have to include it). If you include it as a normal dependency though, then Studio complains.

Approaches:
1) Testing framework just copies actual domain in
* Simple
* More stuff to disable
* Domain is not included in target/repository so we'd have to resolve it via Maven
2) Testing framework resolves domain dependencies for us
* Could take us down a recursive path
* Domain is not included in target/repository so we'd have to resolve it via Maven
3) Modify the POM using a profile to selectively include the HTTP connector
* Simple solution
* Maven takes care of resolving the connector
* We'd have to re-run Maven every time we do the testing run to get the connector into the classloader model
* Could see if we can directly invoke the plugin goals to generate what we need
4) Use exclusions somehow
* Didn't make Studio error go away
5) Have the testing framework inject additional dependencies into the classloader model
* Fine but what if we don't have the dependency in .m2/repo yet?
* Creates an .m2/repo coupling

Not preferences:
5 - .m2
2 - .m2
1 - .m2

Preferences:
4 - not possible
3 - Keeps .m2 coupling to a minimum
