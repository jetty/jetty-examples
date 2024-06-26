# Examples of Loosely Coupled WebApps

Sometimes the WebApp you are making is loosely coupled to another WebApp.

"Loosely Coupled" in this situation refers to a runtime dependency to another WebApp.
One that is not defined via a build tool dependency.

In the examples here, we have 2 WebApps.

## The WebApps

### WebApp A

This WebApp is standalone, and it:

* Provides the [`/css/main.css`](webapp-A/src/main/webapp/css/main.css) used by other webapps.
* Has a [`DebugFilter`](webapp-A/src/main/java/examples/a/DebugFilter.java) to show when this webapp is used by logging access
  to it (such as when a request to `/css/main.css` occurs)

### WebApp B

This WebApp is loosely coupled with WebApp A, and it:

* It has a [`CssServlet`](webapp-B/src/main/java/examples/b/CssServlet.java)
  on [url-pattern `/dyncss/*`](webapp-B/src/main/webapp/WEB-INF/web.xml)
* The `CssServlet` load on start and will initialize itself from a GET request to `http://localhost:8080/appA/css/`
* It has an [`index.html`](webapp-B/src/main/webapp/index.html) with a css reference
  to `<link rel="stylesheet" href="dyncss/main.css" />`

## Problems with Loosely Coupled WebApps

When you have a webapp that needs content from another webapp, then this means the webapp
deployment order is important.

The jetty-example project [`/standalone/deferred-deployment/`](../../standalone/deferred-deployment/) shows how
to setup a standalone environment for this kind of dependency.
