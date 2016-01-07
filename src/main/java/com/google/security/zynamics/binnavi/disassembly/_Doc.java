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
package com.google.security.zynamics.binnavi.disassembly;

/**
 * This package contains the so called raw data. Raw data is basically the back-end that drives
 * com.google.security.zynamics.binnavi. It is the data everything else relies on.
 * 
 * There are raw data classes for projects, modules, code nodes, instructions, operands, operand
 * tree nodes, and so on. Basically any kind of data that is stored in the database has a
 * corresponding raw data class.
 * 
 * That means that things like the graph shown in the GUI or the objects accessible from the plugin
 * API are simply abstraction layers that operate on this raw data.
 * 
 * Raw data is read from the database using the classes in the Database package. Changes in the raw
 * data are instantly written back to the database.
 * 
 * TODO: The biggest problem of this package is that individual raw data classes easily become very
 * big and difficult to understand. In the future it is a good idea to create sub-packages for
 * individual raw data objects.
 * 
 * I have already started doing this by breaking up the formerly huge {@link CModule} and
 * {@link CAddressSpace} classes into many different classes that are easier to understand
 * individually.
 */
