# Logging and ErrorHandling with Jetty

This demo project shows a few things that are of use for working with
logging libraries and error handling.

## Prerequisites for this project

You will need

1. Java 11+
2. Maven 3.9.6+
3. An unpacked `jetty-home` somewhere on your system (**not** within this cloned project!)

### To get a copy of `jetty-home`

Download it and unpack it.

``` shell
$ cd ~/tmp
$ curl -O http://central.maven.org/maven2/org/eclipse/jetty/jetty-home/11.0.20/jetty-home-11.0.20.tar.gz
$ tar -zxvf jetty-home-11.0.20.tar.gz
```

Using the above directories as an example, that means your `$JETTY_HOME` is  
now `$HOME/tmp/jetty-home-11.0.20/`

## To compile the project use 

``` shell
$ mvn clean install
```

The result is a webapp in `/webapps/root.war`

## To run webapp

``` shell
$ cd demobase
$ java -jar $JETTY_HOME/start.jar 
```

## To test webapp

Simply make requests for content against the following URLs

* `http://localhost:8080/normal/` - this will trigger a simple logging event from
the webapp (and `text/plain` response indicating as such)
* `http://localhost:8080/normal/naughty/` - this will trigger an unhandled exception from a webapp,
causing the standard Servlet error handling to kick in

## Files of interest in project

This project contains an example Jetty `jetty.base` configuration.

The maven project builds a war file as normal, but has an additional step to put
that war file into `demobase/webapps/logging-webapp.war`, so that the `jetty.base` configuration 
can use it. 

* `logging-webapp/src/main/webapp/WEB-INF/web.xml` - this contains the servlets + error handling definition
* `demobase/start.ini` - this contains the `jetty.base` configuration for this instance of jetty
 (it has modules for `http`, `deploy`, and `resources` along with manual lib entries for slf4j and log4j2)
* `demobase/lib/slf4j/` - this is the `jetty.base` server libs for slf4j, Using a version newer then what the
 webapp has. 
* `demobase/lib/log4j/` - this is the `jetty.base` server libs for log4j, Using a version newer then what the
 webapp has.
* `demobase/webapps/normal.xml` - this is the configuration used to control the classloader for the
 deployed webapp at the root context (context-path of `/`).
* `demobase/resources/logback.xml` - this is the server logback configuration
* `logging-webapp/src/main/resources/log4j2.xml` - this is the webapp log4j2 configuration
 (also found within the `webapps/logging-webapp.war` when compiled)
