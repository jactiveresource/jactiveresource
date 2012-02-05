jactiveresource
===============

Ruby on Rails includes ActiveResource, a module for for consuming services provided by other Rails applications. jactiveresource helps you easily consume these same services in your Java based applications.

Ryan Daigle says: “ActiveResource provides a large piece of the REST puzzle by basically implementing the client side of a RESTful system – the parts of a decoupled system that consume RESTful services. At its essence, ActiveResource provides a way to utilize model objects as REST-based client proxies to remote services.”

jactiveresource creates Java objects from RESTful services quickly and easily.  Give it a URL, and you get POJO's (no hash maps) with automatic type conversions.  When talking to Rails resources, jactiveresource includes specializations that make Java as DRY as it can get.

Building
---------
There are some prerequisites in order to compile and use jactiveresource.  Here's what you'll need:

 * a java virtual machine > 1.6 
 * maven
 * ruby - 1.9.2 or higher recommended
 * rubygems

Use Maven to compile and build jactiveresource.  After getting the source, compile it by:

    $ mvn compile

To run the unit tests, you need to start up a rails application first.  If you use rvm, which is recommended, you might want to do something like:

    $ rvm use 1.9.2
    $ rvm gemset create jactiveresource
    $ rvm use 1.9.2@jactiveresource

If you don't want to use rvm, you can skip the above steps, and just go straight to:

    $ cd src/test/service-rails3
    $ gem install bundler
    $ bundle install
    $ rake db:setup
    $ rails server

You'll now have a running rails application against which the tests can run.  Then in another session, you can run the tests:

    $ mvn test

To make a jar file that you can use in your java application, while skipping the unit tests, simply do:

    $ mvn package -D skipTests

and then go look in the target directory and you'll have a jar file, and a clean src and binary distribution files as well.

To create the javadocs, you can do:

    $ mvn javadoc:javadoc

The resulting documentation will be found in target/site/apidocs.

How To Use
----------
There are some tutorial documents in the github wiki at https://github.com/jactiveresource/jactiveresource/wiki
