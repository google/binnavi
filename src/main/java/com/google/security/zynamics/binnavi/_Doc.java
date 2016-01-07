/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.binnavi;

/**
 * The structure of BinNavi is as follows:
 *
 * - To get data from IDA Pro into com.google.security.zynamics.binnavi, the code from the Importers
 *   package is used.
 *
 * - The database is accessed through the Database package.
 *
 * - Once the data has been loaded, the next abstraction level is the so
 *   called raw data. All raw data is stored in the disassembly package.
 *
 * - What you see on the screen is stored in the GUI package.
 *
 * - The visible graph you can see in graph windows is stored in the ZyGraph
 *   package. This package takes data from the disassembly package and turns
 *   it into something that can be displayed.
 *
 * - Everything you need for debugging is stored in the Debug package.
 * - The plugin/scripting API is stored in the API package.
 * - All other packages contains only minor functionality.
 * 
 * 
 * Some general notes:
 * 
 * - Very often you will find so called synchronizers in the source code. The
 *   purpose of these synchronizers is to mediate between different layers of
 *   abstractions. An important example are the synchronizers that take
 *   information from debug client packets and update the internal debug target
 *   process simulator classes with the information from the packets.
 * 
 * - Take a look in the /helpers directory. This directory contains various
 *   Python helper scripts. The most important ones are check_es.py and
 *   check_ies.py which are used to assign unique error codes to exception
 *   messages.
 * 
 * - Language localization works by editing the *.po files in the lfiles
 *   directory using a tool called Poeditor. Once the files are edited, the BAT
 *   file in that directory must be executed. If you have any questions about
 *   this process, ask Nils. He knows everything about this.
 * 
 * - Useful Eclipse plugins for finding bugs and improving code quality are
 *   PMD, FindBugs, and UCDetector. CheckStyle is good too, but does not
 *   seem to work with the current version of Eclipse. ECLEmma is very useful
 *   for code coverage and unit tests.
 * 
 * - There is an Eclipse formatting stylesheet which you *must* use. Not using
 *   it screws up things in the SVN commit history when functions and classes
 *   are shuffled around by those that do use the style sheet. Ask around to
 *   find out what Eclipse formatting settings you have to use.
 */
