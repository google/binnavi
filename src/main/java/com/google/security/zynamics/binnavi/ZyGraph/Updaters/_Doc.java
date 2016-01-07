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
package com.google.security.zynamics.binnavi.ZyGraph.Updaters;

/**
 * This package contains classes that update nodes and edges
 * when events happen that require nodes and edges to be updated.
 *
 * Imagine the user sets a breakpoint somewhere. This requires a
 * graph to be redrawn because the breakpoint has to be displayed
 * in all nodes that contain the breakpoint address.
 */
