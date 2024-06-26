# Deferred WebApp Deployment

This example jetty.base demonstrates how to setup a deferred deployment of select webapps.


## WebApps

The webapps in use on this jetty.base

* `webapps/webapp-A.war` comes from the [`webapps/loosely-coupled-webapps/webapp-A/`](../../webapps/loosely-coupled-webapps/webapp-A) project.
* `webapps-late/webapp-B.war` comes from the [`webapps/loosely-coupled-webapps/webapp-B/`](../../webapps/loosely-coupled-webapps/webapp-B) project.


## New Modules

There are 4 new modules present in this jetty.base in the `modules/` directory.

* `core-deploy-late` - a deployment app provider for `core` environment applications (based on default `core-deploy` module)
* `ee10-deploy-late` - a deployment app provider for `ee10` environment applications (based on default `ee10-deploy` module)
* `ee9-deploy-late` - a deployment app provider for `ee9` environment applications (based on default `ee9-deploy` module)
* `ee8-deploy-late` - a deployment app provider for `ee8` environment applications (based on default `ee8-deploy` module)

We are going to use only one of these modules for purposes of this example.

This `ee10-deploy-late` module:

* provides a Jetty Deployment AppProvider that monitors for deployable `ee10`
content in the `${jetty.base}/webapps-late/` directory.
* has `deferInitialDeploy` configuration set to `true`

This module is enabled via the `start.d/ee10-deploy-late.ini` file.


## Testing Deployment

You can make requests to 2 different URLs and look at the output.

``` shell
$ curl -vvv http://localhost:8080/appB/
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /appB/ HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.81.0
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: Jetty(12.0.10)
< Last-Modified: Wed, 26 Jun 2024 15:27:42 GMT
< Content-Type: text/html
< Accept-Ranges: bytes
< Content-Length: 173
< 
<html>
<head>
    <title>Webapp B</title>
    <link rel="stylesheet" href="dyncss/main.css"/>
</head>
<body>
<h1>WebApp B</h1>
<p>You've reached WebApp B</p>
</body>
</html>
* Connection #0 to host localhost left intact
```

This shows that `/appB/` is responding with the `index.html`

Now lets request `/appB/dyncss/main.css` ...

``` shell
$ curl -vvv http://localhost:8080/appB/dyncss/main.css
*   Trying 127.0.0.1:8080...
* Connected to localhost (127.0.0.1) port 8080 (#0)
> GET /appB/dyncss/main.css HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.81.0
> Accept: */*
> 
* Mark bundle as not supporting multiuse
< HTTP/1.1 200 OK
< Server: Jetty(12.0.10)
< Content-Type: text/css;charset=utf-8
< Content-Length: 82
< 
// This is the main.css from Web Application A

body {
  text-style: sans-serif;
}
* Connection #0 to host localhost left intact
```

We can see that requesting content from `/appB/` is instead showing content from `/appA/`
that was requested by `appB` during the startup of that webapp. 