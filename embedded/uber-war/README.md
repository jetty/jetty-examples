Live/Executable War with Jetty Embedded
=======================================

This project should provide a baseline for those investigating the use of Embedded Jetty
from the point of view of a Self Executing WAR file.

The project has 4 main parts:

 1. `/thewebapp/` - this is the WAR file, the webapp, as it exists in its native format, with normal maven
    `<packaging>war</packaging>` and a produced artifact that is just a WAR file that isn't (yet) self-executing.
 2. `/theserver/` - this is the Embedded Jetty Server `jetty.livewar.ServerMain.main(String args[])` which you 
    customize to initialize your Jetty server and its WebApp.  This project also is the place where you customize
    for things like JDBC servers libraries, JNDI, logging, etc.   This project produces a uber-jar with all the
    dependencies needed to run the server.  Special care is taken with the `maven-shade-plugin` to merge
    `META-INF/services/` files.
 3. `/server-bootstrap/` - this contains 2 small classes that sets up a `LiveWarClassLoader` from the content
    in the live WAR and then runs `jetty.livewar.ServerMain.main(String args[])` from this new ClassLoader.
    This project also contains the live `META-INF/MANIFEST.MF` that the live WAR will need/use
 4. `/uber-war-assembly/` - this is the project that ties together the above 3 projects into a Live/Executable WAR file.
    The artifacts from from the above 3 projects are unpacked by the `maven-assembly-plugin` and put into
    place where they will be most functional (and safe).  For example, the server classes from
    `/theserver/` are placed in `/WEB-INF/jetty-server/` to make them inaccessible from Web Clients
    accessing the WAR file.

Note: there are 3 files present in your new assembled WAR file that you should be aware of, as these
files can be downloaded by a Web Client as static content if you use this setup.

 * `/jetty/bootstrap/JettyBootstrap.class`
 * `/jetty/bootstrap/LiveWarClassLoader.class`
 * `/META-INF/MANIFEST.MF`

The example project is setup in such a way that information present in these bootstrap files should not
reveal private or sensitive information about your Server or its operations.  Merely that the Webapp
can be started as a Live/Executable WAR file.

Example:

```shell
$ mvn clean install
...(snip lots of build output)...
$ java -jar uber-war-assembly/target/uber-war-example.war 
Using ClassLoader: jetty.bootstrap.LiveWarClassLoader[file:/home/joakim/code/jetty/github-jetty.project/embedded-jetty-live-war/uber-war-assembly/target/uber-war-example.war]
2021-08-10 15:38:48.696:INFO::main: Logging initialized @190ms to org.eclipse.jetty.util.log.StdErrLog
2021-08-10 15:38:48.759:INFO:oejs.Server:main: jetty-9.4.43.v20210629; built: 2021-06-30T11:07:22.254Z; git: 526006ecfa3af7f1a27ef3a288e2bef7ea9dd7e8; jvm 11.0.12+7
2021-08-10 15:38:49.154:INFO:oeja.AnnotationConfiguration:main: Scanning elapsed time=15ms
2021-08-10 15:38:49.162:INFO:oejw.StandardDescriptorProcessor:main: NO JSP Support for /, did not find org.eclipse.jetty.jsp.JettyJspServlet
2021-08-10 15:38:49.211:INFO:oejs.session:main: DefaultSessionIdManager workerName=node0
2021-08-10 15:38:49.211:INFO:oejs.session:main: No SessionScavenger set, using defaults
2021-08-10 15:38:49.212:INFO:oejs.session:main: node0 Scavenging every 660000ms
2021-08-10 15:38:49.242:INFO:oejsh.ContextHandler:main: Started o.e.j.w.WebAppContext@3bf7ca37{/,file:///tmp/jetty-0_0_0_0-8080-uber-war-example_war-_-any-1092283572399441055/webapp/,AVAILABLE}{/home/joakim/code/jetty/github-jetty.project/embedded-jetty-live-war/uber-war-assembly/target/uber-war-example.war}
2021-08-10 15:38:49.258:INFO:oejs.AbstractConnector:main: Started ServerConnector@52f759d7{HTTP/1.1, (http/1.1)}{0.0.0.0:8080}
2021-08-10 15:38:49.259:INFO:oejs.Server:main: Started @754ms
```


