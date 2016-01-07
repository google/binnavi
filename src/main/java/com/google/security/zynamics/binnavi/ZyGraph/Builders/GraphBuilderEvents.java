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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

/**
 * Events issued while the visible graph of a view is built.
 */
public enum GraphBuilderEvents {
  /**
   * Signals that building has started.
   */
  Started,

  /**
   * Signals that the graph was initialized.
   */
  InitializedGraph,

  /**
   * Signals that the visible nodes were created.
   */
  ConvertedNodes,

  /**
   * Signals that the visible edges were created.
   */
  ConvertedEdges,

  /**
   * Signals that the group nodes were initialized.
   */
  CreatedGroupNodes,

  /**
   * Signals that graph creation finished.
   */
  Finished
}
