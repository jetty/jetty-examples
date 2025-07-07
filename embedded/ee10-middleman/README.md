Example: Embedded Jetty w/ AsyncMiddleManServlet Support 
========================================================

This is a maven project, to build it:

    $ mvn clean package

To run the example `examples.MiddleManMain`:

    $ mvn exec:java

Open your web browser to:

    http://localhost:8080/  

To stop Jetty:

  use <kbd>CTRL</kbd>+<kbd>C</kbd>


Code Of Interest
----------------

See [examples.MiddleManMain](src/main/java/examples/MiddleManMain.java)