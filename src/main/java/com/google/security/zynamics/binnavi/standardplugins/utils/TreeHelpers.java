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
package com.google.security.zynamics.binnavi.standardplugins.utils;


import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 * Helper class that provides JTree functions that are often used.
 */
public final class TreeHelpers {
  /**
   * Finds the node at a given mouse cursor position.
   * 
   * @param tree The tree where the node is located.
   * @param x The x coordinate of the mouse position.
   * @param y The y coordinate of the mouse position.
   * 
   * @return The node at the given cursor position or null if there is no node at that position.
   */
  public static Object getNodeAt(final JTree tree, final int x, final int y) {
    final TreePath selPath = tree.getPathForLocation(x, y);

    if (selPath == null) {
      return null;
    }

    return selPath.getLastPathComponent();
  }
}
