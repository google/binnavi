BinNavi [![Build Status](https://api.travis-ci.org/google/binnavi.svg?branch=master)](https://travis-ci.org/google/binnavi)
=======

Copyright 2015 Google Inc.

Disclaimer: This is not an official Google product (experimental or otherwise),
it is just code that happens to be owned by Google.

#Introduction

BinNavi is a binary analysis IDE - an environment that allows users to inspect,
navigate, edit, and annotate control-flow-graphs of disassembled code, do the
same for the callgraph of the executable, collect and combine execution traces,
and generally keep track of analysis results among a group of analysts.

#Complications from a third-party dependency

BinNavi uses a commercial third-party graph visualisation library (yFiles) for
displaying and laying out graphs. This library is immensely powerful, and not
easily replaceable.

In order to perform direct development using yFiles, you need a developer
license for it. At the same time, we want the community to be able to contribute to
BinNavi without needing a commercial yFiles license. In order to do this and
conform to the yFiles license, all interfaces to yFiles need to be properly
obfuscated.

In order to achieve this, we did the following:

1) BinNavi and all the libraries have been split into two: The parts of the
project that directly depend on yFiles were split into subpackages called
"yfileswrap":

  * com.google.security.zynamics.binnavi
  * com.google.security.zynamics.binnavi.yfileswrap
  * com.google.security.zynamics.zylib
  * com.google.security.zynamics.zylib.yfileswrap
  * com.google.security.zynamics.reil
  * com.google.security.zynamics.reil.yfileswrap

We are distributing a pre-built JAR file with all the code in the "yfileswrap"
subpackages - pre-linked and obfuscated against yFiles. If you wish to change
or add code in BinNavi and do not have a yFiles license, you can freely do 
pretty much  whatever you want in the non-yfileswrap packages - you can simply
put the ``lib/yfileswrap-obfuscated.jar`` into your classpath to test and see
the results.

If you wish to make changes to the "yfileswrap" subdirectories, please be aware
that you will need a valid yFiles license - and any contribution that you make
to the BinNavi project has to honor their license agreement. This means that
you can't simply expose their inner APIs under different names etc.

We will enforce this - we're very happy to have found a way to open-source
BinNavi with the yFiles dependency, and we will make sure that any code we pull
in respects the yFiles license.

#Building BinNavi from scratch

BinNavi uses Maven for its dependency management, but not for the actual build
yet. To build from scratch use these commands:

    mvn dependency:copy-dependencies
    ant -f src/main/java/com/google/security/zynamics/build.xml \
      build-binnavi-fat-jar

#Running BinNavi for the first time

Please be aware that BinNavi makes use of a central PostgreSQL database for
storing disassemblies/comments/traces - so you need to have such an instance
running somewhere accessible to you. You can build/launch BinNavi as follows:

    ant -f src/main/java/com/google/security/zynamics/build.xml \
      build-binnavi-fat-jar
    java -jar target/binnavi-all.jar

##Loading the project into Eclipse

Loading the code into Eclipse for further development requires a little bit of
configuration.

1. Download the dependencies (as described above) and make sure you have a
   Java SDK with 1.8 language compliance installed.
2. Create a new "Java Project From Existing Ant Buildfile" and use the file
   src/main/java/com/google/security/zynamics/build.xml
3. Select '"javac" task found in target "build-binnavi-jar"
4. Open the "Project Properties" dialog.
5. Edit the source folders to have the following properties:
   * Linked Folder Location: ``$SRCDIR/src/main/java``
   * Folder Name: ``java``
   * Click on "Next"
6. Add ``binnavi/yfileswrap``, ``zylib/yfileswrap``, and ``reil/yfileswrap`` to
   the list of directories to exclude.
7. Go to Run->Debug Configurations, select "Java Application" and then search
   for "CMain". 

You should be ready to go from here.

#Exporting disassemblies from IDA

As part of this project, we are distributing a binary-only (sorry!) IDA pro
plugin that exports disassemblies from IDA into the Postgresql database format
that BinNavi requires. When running BinNavi, simply configure the right path
for IDA, click on the "install plugin" button if necessary -- you should now
be able to import disassemblies.

#Using other disassemblers than IDA

Right now, we only have the IDA export plugin - but we are hoping very much
that someone will help us build export functionality for other disassemblers
in the near future.
