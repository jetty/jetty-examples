# Embedded Jetty Examples

* [`client/`](client/) - Jetty HttpClient with Dynamic Connection support supporting both HTTP/1 and HTTP/2.
* [`client-certificates/`](client-certificates/) - Enable the JVM handling of Client Certificates within Jetty Server
* [`compressed-encoding/`](compressed-encoding/) - GzipHandler support on Jetty Server for dynamically compressing responses
* [`connectors/`](connectors/) - Using Connector names and virtual hosts on webapps to bind specific webapps to specific connectors
* [`deploying/`](deploying/) - Setup a delayed webapp hot deployment.
* [`ee8-webapp-context/`](ee8-webapp-context/) - Setup an EE8 WebAppContext from File System or Classpath
* [`ee10-error-handling/`](ee10-error-handling/) - Setup and configure EE10 Servlet / WebApp Error Handling
* [`ee10-file-server/`](ee10-file-server/) - Serve static files in EE10 Servlet environment using one or more DefaultServlet
* [`ee10-form-post/`](ee10-form-post/) - Form submission in EE10 Servlet environment
* [`ee10-jersey-weld/`](ee10-jersey-weld/) - Enable Jersey (REST) and Weld (CDI) in the EE10 Servlet environment
* [`ee10-jsp/`](ee10-jsp/) - Enable JSP processing from EE10 Servlet environment (with Taglib)
* [`ee10-metainf-resources/`](ee10-metainf-resources/) - Supporting`META-INF/resources` with a ServletContextHandler
* [`ee10-servlet-config/`](ee10-servlet-config/) - Configuring EE10 Servlet environment (eg: multiple filter mappings)
* [`ee10-servlet-security/`](ee10-servlet-security/) - EE10 Servlet transport guarantee security constraint 
* [`ee10-servlet-server/`](ee10-servlet-server/) - Simple EE10 Servlet environment
* [`ee10-servlet-with-cdi/`](ee10-servlet-with-cdi/) - EE10 Servlet environment with CDI support
* [`ee10-uber-jar/`](ee10-uber-jar/) - Building a uber-jar with all dependencies integrated for Jetty Server
* [`ee10-uber-war/`](ee10-uber-war/) - Building an uber-WAR that can run as a deployed WebApp/WAR or as a standalone uber-jar with all dependencies integrated for Jetty Server.
* [`ee10-webapp-context/`](ee10-webapp-context/) - Setup an EE10 WebAppContext from a File System or ClassPath
* [`ee10-websocket-jakarta-api/`](ee10-websocket-jakarta-api/) - Using `jakarta.websocket` API from `ServletContextHandler`
* [`ee10-websocket-jetty-api/`](ee10-websocket-jetty-api/) - Using Jetty WebSocket API from `ServletContextHandler`
* [`file-server/`](file-server/) - Serving static files with `ResourceHandler`
* [`file-upload/`](file-upload/) - Handling `multipart/form-data` Form File Uploads via Jetty Core or Servlet APIs
* [`form-post/`](form-post/) - Handling forms (query, or `application/x-www-form-urlencoded` or `multipart/form-data`) with Jetty Core
* [`http-config/`](http-config/) - Using `HttpConfiguration`
* [`jndi/`](jndi/) - Using `JNDI` from EE10 Servlet environment
* [`logging-java-util-logging/`](logging-java-util-logging/) - Using `java.util.logging` from Jetty Core
* [`logging-mixed/`](logging-mixed/) - Using multiple Logging APIs from Jetty Core
* [`logging-slf4j/`](logging-slf4j/) - Using `org.slf4j` from Jetty Core
* [`logging-slf4j-and-log4j2/`](logging-slf4j-and-log4j2/) - Using `org.slf4j` API from Jetty Core, but outputting via log4j.
* [`logging-system-err/`](logging-system-err/) - Using `jetty-slf4j-impl` as output via Jetty Core
* [`path-mapping-handler/`](path-mapping-handler/) - Using `PathMappingsHandler` to control how your Jetty Core handlers are wired up
* [`redirect/`](redirect/) - Using `SecuredRedirectHandler` to ensure that https is used
* [`requestlog/`](requestlog/) - Setting up a `CustomRequestLog` to output to file or `org.slf4j` named logger
* [`rewrite/`](rewrite/) - Using `RewriteHandler` using Jetty Core
* [`simple-server/`](simple-server/) - Simple Server using Jetty Core
* [`virtual-hosts/`](virtual-hosts/) - Using Virtual Hosts with Contexts in Jetty Core
* [`websocket-jetty-api/`](websocket-jetty-api/) - Using the Jetty WebSocket API in Jetty Core
* [`xml/`](xml/) - Using Jetty `XmlConfiguration` to separate configuration from code