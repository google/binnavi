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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.Nodes;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;

import javax.swing.JPopupMenu;



/**
 * Interface for all nodes shown in the node tags tree.
 */
public interface ITagTreeNode {
  /**
   * Disposes the node and all of its children.
   */
  void dispose();

  /**
   * Returns the context menu shown when the user right-clicks the node.
   *
   * @return The context menu shown when the user right-clicks the node.
   */
  JPopupMenu getPopupMenu();

  /**
   * Returns the tag represented by the node.
   *
   * @return The tag represented by the node.
   */
  ITreeNode<CTag> getTag();
}
