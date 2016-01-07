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
package com.google.security.zynamics.binnavi.ZyGraph;

/**
 * This package contains the classes that are necessary to display graphs in the graph windows.
 *
 *  What basically happens is that the classes in the sub-package Builders, especially
 * {@link ZyGraphBuilder}, take raw data of a graph and convert it into a {@link ZyGraph} object.
 * This {@link ZyGraph} object abstracts yFiles graph objects and can be displayed in the GUI.
 *
 *  The other sub-packages are responsible for things like keeping the raw data synchronized with
 * the {@link ZyGraph} object or for defining how exactly individual graph objects should be drawn.
 */
