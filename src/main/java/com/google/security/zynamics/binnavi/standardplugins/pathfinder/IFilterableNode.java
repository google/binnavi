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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

/**
 * Each node which lends itself for simple text pattern based filtering needs to implement this
 * interface
 */
public interface IFilterableNode {
  /**
   * The node can decide whether it should be visible or not (possibly via the filter mechanism)
   * 
   * @return True if node should be visible, False otherwise
   */
  boolean isVisible();

  /**
   * set a new filter for the node
   * 
   * @param filter The new filter object
   */
  void setFilter(TextPatternFilter filter);
}
