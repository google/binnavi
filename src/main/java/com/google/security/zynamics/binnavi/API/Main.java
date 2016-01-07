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
package com.google.security.zynamics.binnavi.API;

/*
 * ! \mainpage Zynamics BinNavi Plugin API
 *
 * \section Introduction
 *
 * Many parts of BinNavi can be extended by plugins or scripts. This plugin API documentation lists
 * the methods available to BinNavi plugin and how you can extend BinNavi using plugins.
 *
 * \section provides What BinNavi provides
 *
 * BinNavi gives plugin access to many parts of its GUI and its internal data. The main window and
 * graph windows can be extended and the underlying data can be used and modified from the high
 * level of databases and projects down to the lowest level of instructions and operands.
 *
 * \section extend How to extend com.google.security.zynamics.binnavi
 *
 * \subsection mainwindow Extending the main window
 *
 * There are two different ways to extend the main window. For once it it possible to add menu items
 * to the main menu of the main window. This can be done by writing plugins that implement the
 * IMainWindowMenuPlugin interface. On the other hand it is possible to extend the context menus of
 * the project tree through a set of different interfaces like IDatabaseMenuPlugin or
 * IModuleMenuPlugin.
 *
 * \subsection graphwindow Extending the graph windows
 *
 * Right now it is only possible to extend the main menu of the graph windows by creating plugins
 * that implement the interface IGraphMenuPlugin.
 *
 * \subsection disassembly Using the disassembly data
 *
 * Once you have extended the window of your choice it is possible to access disassembly data from
 * your plugin. How to get access to this data depends on exactly what part of the BinNavi GUI your
 * plugin extends. Generally speaking all classes and interfaces necessary for accessing the
 * disassembly data can be found in the com.google.security.zynamics.binnavi.API.disassembly package.
 */
