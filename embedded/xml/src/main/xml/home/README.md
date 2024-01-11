This contents of custom.properties was obtained form using jetty-home / jetty-base

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

[ssl10]$ cat start.ini
```