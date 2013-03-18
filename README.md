at-doclet
=========

Doclet for Annotation Driven Documentation

Abstract
========

(at)-doclet allows you to document your project inside your code with Javadoc and generate a userfriendly maven site
afterwards.

How it work's:

1. Define annotations for different types you want do document, and write appropriate Javadoc.

2. Define apt-templates for the different annotations.

3. Configure your Maven Project to use the doclet.

4. Generate your output.

Installation and usage
======================

1. Checkout the project.

2. Install maven (if not yet done).

3. run mvn install, to install the doclet in your local maven repository. (update about a central repository will be added soon).

4. include the doclet in your projects. 

A detailed documentation can be found in the project itself. Run mvn site && mvn site:run and you'll have a html page with all information you need.