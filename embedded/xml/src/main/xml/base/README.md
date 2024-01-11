This list of XML to use, and the order to use them in, was obtained from using jetty-home.

``` shell
[tmp]$ mkdir ssl10
[ssl10]$ cd ssl10
[ssl10]$ java -jar /opt/jetty-home-10.0.7/start.jar --create-start-ini --add-modules=http,https,requestlog
INFO  : create ${jetty.base}/start.ini
INFO  : server          transitively enabled, ini template available with --add-module=server
INFO  : logging-jetty   transitively enabled
INFO  : requestlog      initialized in ${jetty.base}/start.ini
INFO  : http            initialized in ${jetty.base}/start.ini
INFO  : resources       transitively enabled
INFO  : https           initialized in ${jetty.base}/start.ini
INFO  : threadpool      transitively enabled, ini template available with --add-module=threadpool
INFO  : ssl             transitively enabled, ini template available with --add-module=ssl
INFO  : logging/slf4j   transitive provider of logging/slf4j for logging-jetty
INFO  : logging/slf4j   dynamic dependency of logging-jetty
INFO  : bytebufferpool  transitively enabled, ini template available with --add-module=bytebufferpool
INFO  : mkdir ${jetty.base}/resources
INFO  : copy ${jetty.home}/modules/logging/jetty/resources/jetty-logging.properties to ${jetty.base}/resources/jetty-logging.properties
INFO  : mkdir ${jetty.base}/logs
INFO  : Base directory was modified

[ssl10]$ java -jar /opt/jetty-home-10.0.7/start.jar --list-config

Enabled Modules:
----------------
    0) resources       transitive provider of resources for logging-jetty
    1) logging/slf4j   transitive provider of logging/slf4j for logging-jetty
                       dynamic dependency of logging-jetty
    2) logging-jetty   transitive provider of logging for threadpool
                       transitive provider of logging for bytebufferpool
                       transitive provider of logging for server
    3) bytebufferpool  transitive provider of bytebufferpool for server
                       init template available with --add-module=bytebufferpool
    4) threadpool      transitive provider of threadpool for server
                       init template available with --add-module=threadpool
    5) server          transitive provider of server for http
                       transitive provider of server for ssl
                       transitive provider of server for requestlog
                       init template available with --add-module=server
    6) http            ${jetty.base}/start.ini
    7) ssl             transitive provider of ssl for https
                       init template available with --add-module=ssl
    8) https           ${jetty.base}/start.ini
    9) requestlog      ${jetty.base}/start.ini

Java Environment:
-----------------
 java.home = /home/joakim/java/jvm/jdk-11.0.12+7 (null)
 java.vm.vendor = Eclipse Foundation (null)
 java.vm.version = 11.0.12+7 (null)
 java.vm.name = OpenJDK 64-Bit Server VM (null)
 java.vm.info = mixed mode (null)
 java.runtime.name = OpenJDK Runtime Environment (null)
 java.runtime.version = 11.0.12+7 (null)
 java.io.tmpdir = /tmp (null)
 user.dir = /home/joakim/code/jetty/distros/bases/ssl10 (null)
 user.language = en (null)
 user.country = US (null)

Jetty Environment:
------------------
 jetty.version = 10.0.7
 jetty.tag.version = jetty-10.0.7
 jetty.build = da8a4553af9dd84080931fa0f8c678cd2d60f3d9
 jetty.home = /opt/jetty-home-10.0.7
 jetty.base = /home/joakim/code/jetty/distros/bases/ssl10

Config Search Order:
--------------------
 <command-line>
 ${jetty.base} -> /home/joakim/code/jetty/distros/bases/ssl10
 ${jetty.home} -> /opt/jetty-home-10.0.7

System Properties:
------------------
 (no system properties specified)

Properties:
-----------
 java.version = 11.0.12
 java.version.major = 11
 java.version.micro = 12
 java.version.minor = 0
 java.version.platform = 11
 jetty.base = /home/joakim/code/jetty/distros/bases/ssl10
 jetty.base.uri = file:///home/joakim/code/jetty/distros/bases/ssl10
 jetty.home = /opt/jetty-home-10.0.7
 jetty.home.uri = file:///opt/jetty-home-10.0.7
 jetty.requestlog.dir = logs
 jetty.webapp.addServerClasses = org.eclipse.jetty.logging.,${jetty.home.uri}/lib/logging/,org.slf4j.
 runtime.feature.alpn = true
 slf4j.version = 2.0.0-alpha5

Jetty Server Classpath:
-----------------------
Version Information on 9 entries in the classpath.
Note: order presented here is how they would appear on the classpath.
      changes to the --module=name command line options will be reflected here.
 0:                    (dir) | ${jetty.base}/resources
 1:             2.0.0-alpha5 | ${jetty.home}/lib/logging/slf4j-api-2.0.0-alpha5.jar
 2:                   10.0.7 | ${jetty.home}/lib/logging/jetty-slf4j-impl-10.0.7.jar
 3:                    4.0.6 | ${jetty.home}/lib/jetty-servlet-api-4.0.6.jar
 4:                   10.0.7 | ${jetty.home}/lib/jetty-http-10.0.7.jar
 5:                   10.0.7 | ${jetty.home}/lib/jetty-server-10.0.7.jar
 6:                   10.0.7 | ${jetty.home}/lib/jetty-xml-10.0.7.jar
 7:                   10.0.7 | ${jetty.home}/lib/jetty-util-10.0.7.jar
 8:                   10.0.7 | ${jetty.home}/lib/jetty-io-10.0.7.jar

Jetty Active XMLs:
------------------
 ${jetty.home}/etc/jetty-bytebufferpool.xml
 ${jetty.home}/etc/jetty-threadpool.xml
 ${jetty.home}/etc/jetty.xml
 ${jetty.home}/etc/jetty-http.xml
 ${jetty.home}/etc/jetty-ssl.xml
 ${jetty.home}/etc/jetty-ssl-context.xml
 ${jetty.home}/etc/jetty-https.xml
 ${jetty.home}/etc/jetty-requestlog.xml
```