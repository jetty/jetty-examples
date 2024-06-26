# Deferred WebApp Deployment

This example jetty.base demonstrates how to setup a deferred deployment of select webapps.


## WebApps

The webapps in use on this jetty.base

* `webapps/webapp-A.war` comes from the [`webapps/loosely-coupled-webapps/webapp-A/`](../../webapps/loosely-coupled-webapps/webapp-A) project.
* `webapps-late/webapp-B.war` comes from the [`webapps/loosely-coupled-webapps/webapp-B/`](../../webapps/loosely-coupled-webapps/webapp-B) project.


## New Modules

There is a new `deploy-late` module present in this jetty.base in the `modules/deploy-late.mod`.

This `deploy-late` module:

* provides a Jetty Deployment AppProvider that monitors for deployable
content in the `${jetty.base}/webapps-late/` directory.
* has `deferInitialDeploy` configuration set to `true`

This module is enabled via the `start.d/deploy-late.ini`.