![jetty logo](https://raw.githubusercontent.com/jetty/jetty.project/jetty-12.0.x/logos/jetty-logo-200.png)

[![12.0.x](https://github.com/jetty/jetty-examples/actions/workflows/ci.yml/badge.svg?branch-12.0.x)](https://github.com/jetty/jetty-examples/actions/workflows/ci.yml)

# Eclipse Jetty® - Examples

This is a collection of examples of how to use various features
present in the Eclipse Jetty server and Eclipse Jetty client.

There are a few major categories of examples.
## Embedded Examples

Using Embedded Jetty is extremely powerful way to include Jetty
in your Java application using Code (instead of configuration) to
setup a Jetty server or Jetty client.

Here you will see answers to common questions about how to 
integrate various features of Jetty, and also how to enable features
of 3rd party libraries within Jetty (such as JSP, REST, and CDI).

See [embedded/README.md][7] for a breakdown of embedded examples.

## Standalone Examples

The Standalone examples present fully formed `${jetty.base}` directories
that can be used to understand how to configure the jetty module system
and its various components to reach an end goal.

See [standalone/README.md][8] for a breakdown of standalone examples.

## Webapp Examples

A collection of various WAR files that can be used to demonstrate
how to use web and JVM features in Jetty.

See [webapps/README.md][9] for a breakdown of webapp examples.

---

### All Example Branches

| Branch       | Min JDK | EE   | Servlet | Namespace         | Supported                               |
|--------------|---------|------|---------|-------------------|-----------------------------------------|
| [12.0.x][6]  | 17      | EE10 | 6.0     | `jakarta.servlet` | Yes                                     |
| [12.0.x][6]  | 17      | EE9  | 5.0     | `jakarta.servlet` | Yes                                     |
| [12.0.x][6]  | 17      | EE8  | 4.0     | `javax.servlet`   | Yes                                     |
| [11.0.x][1]  | 11      | EE9  | 5.0     | `jakarta.servlet` | No (as of January 2024) [See #10485][4] |
| [10.0.x][2]  | 11      | EE8  | 4.0     | `javax.servlet`   | No (as of January 2024) [See #10485][4] |
| [9.4.x][3]   | 8       | EE7  | 3.1     | `javax.servlet`   | No (as of June 2022) [See #7958][5]     |

[1]: https://github.com/jetty/jetty-examples/tree/11.0.x
[2]: https://github.com/jetty/jetty-examples/tree/10.0.x
[3]: https://github.com/jetty/jetty-examples/tree/9.4.x
[4]: https://github.com/jetty/jetty.project/issues/10485
[5]: https://github.com/jetty/jetty.project/issues/7958
[6]: https://github.com/jetty/jetty-examples/tree/12.0.x
[7]: embedded/README.md
[8]: standalone/README.md
[9]: webapps/README.md