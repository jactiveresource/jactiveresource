jactiveresource
===============

Ruby on Rails includes ActiveResource, a module for for consuming services provided by other Rails applications. jactiveresource helps you easily consume these same services in your Java based applications.

Ryan Daigle says: “ActiveResource provides a large piece of the REST puzzle by basically implementing the client side of a RESTful system – the parts of a decoupled system that consume RESTful services. At its essence, ActiveResource provides a way to utilize model objects as REST-based client proxies to remote services.”

jactiveresource creates Java objects from RESTful services quickly and easily.  Give it a URL, and you get POJO's (no hash maps) with automatic type conversions.  When talking to Rails resources, jactiveresource includes specializations that make Java as DRY as it can get.

Building
---------
Use Maven to compile and build jactiveresource.  If you aren't familiar with Maven, here's a quick primer.

After getting the source, compile it by:

    $ mvn compile

To run the unit tests, you need to start up a rails application first:

    $ cd src/test/service-rails3
    $ rails server

Then in another session, you can run the tests:

    $ mvn test

To make a jar file that you can use in your java application, while skipping the unit tests, simply do:

    $ mvn package -D skipTests

and then go look in the target directory and you'll have a jar file, and a clean src and binary distribution files as well.

To create the javadocs, you can do:

    $ mvn javadoc:javadoc

The resulting documentation will be found in target/site/apidocs

