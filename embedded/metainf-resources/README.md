# Using META-INF/resources with ServletContextHandler

There are many jars available to you on the Global Central Maven Repository System that
provide web resources in the `META-INF/resources` directories contained within those jars.

Using `META-INF/resources` with embedded-jetty can be accomplished with a full blown
`WebAppContext` and the use of WAR files, but that is often overkill when working
with embedded-jetty.

Users of embedded-jetty often want to work with the `ServletContextHandler` and
also take advantage of the `META-INF/resources` JAR files.

This project is an example of combining JAR files from [central.maven.org](https://search.maven.org/)
that contain `META-INF/resources` directories and the `ServletContextHandler` from embedded-jetty.

What you should pay attention to in this project.

1. [pom.xml](pom.xml) - the `maven-dependency-plugin` configuration that
   unpack's all of the dependencies that have `META-INF/resources` directories

2. [src/main/resources/META-INF/resources/MYREADME.txt](src/main/resources/META-INF/resources/MYREADME.txt) -
   this file is searched for, and should be uniquely named to your project.  It can be any resource
   a css, an html, a javascript, an image, whatever.  Just as long as its name is unique to your
   project.

3. [ExampleServer](src/main/java/examples/MetaInfResourceDemo.java) - this is where
   the actual embedded-jetty server resides.

   1. Create a `ServletContextHandler`
   2. Find the special resource URL location
   3. Create a "Base Resource" that points to the directory location of
      the special resource file
   4. Create a `DefaultServlet` that will serve the static files
      from the "Base Resource" location you provided.

