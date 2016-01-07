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
package com.google.security.zynamics.binnavi.ZyGraph.Synchronizers;

/**
 * This package contains the classes that make sure the visible graph is always synchronized with
 * the graph model.
 *
 *  Basically what these classes do is keep track of what is going on in, for example, the raw data
 * of a node and make sure to update the visible graph on relevant changes. The same is true in the
 * other direction if the user changes an aspect of the visible graph.
 */
