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
package com.google.security.zynamics.binnavi.API.disassembly;

import java.awt.Color;

// / Used to listen on view nodes.
/**
 * Listener interface that must be implemented by all objects that want to be notified about changes
 * in a view node.
 */
public interface IViewNodeListener {
  // ! Signals that the node was tagged.
  /**
   * Invoked after a tag was added to the node.
   *
   * @param node The node the tag was added to.
   * @param tag The tag that was added to the node.
   */
  void addedTag(ViewNode node, Tag tag);

  // ! Signals a change of the border color.
  /**
   * Invoked after the border color of the node changed.
   *
   * @param node The node whose color changed.
   * @param color The new color of the node.
   */
  void changedBorderColor(ViewNode node, Color color);

  // ! Signals a change of the background color.
  /**
   * Invoked after the color of the node changed.
   *
   * @param node The node whose color changed.
   * @param color The new color of the node.
   */
  void changedColor(ViewNode node, Color color);

  // ! Signals a changing parent group.
  /**
   * Invoked after the parent group of the node changed.
   *
   * @param node The node whose parent group changed.
   * @param parentGroup The new parent group of the node or null if the node was removed from all
   *        groups.
   */
  void changedParentGroup(ViewNode node, GroupNode parentGroup);

  // ! Signals a change of the selection state.
  /**
   * Invoked after the selection state of the node changed.
   *
   * @param node The node whose color changed.
   * @param selected The new selection state of the node.
   */
  void changedSelection(ViewNode node, boolean selected);

  // ! Signals a change of the visibility state.
  /**
   * Invoked after the visibility state of the node changed.
   *
   * @param node The node whose color changed.
   * @param visible The new visibility state of the node.
   */
  void changedVisibility(ViewNode node, boolean visible);

  // ! Signals a change of the X-position.
  /**
   * Invoked after the X-position of the node changed.
   *
   * @param node The node whose X-position changed.
   * @param xpos The new X-position of the node.
   */
  void changedX(ViewNode node, double xpos);

  // ! Signals a change of the Y-position.
  /**
   * Invoked after the Y-position of the node changed.
   *
   * @param node The node whose Y-position changed.
   * @param ypos The new Y-position of the node.
   */
  void changedY(ViewNode node, double ypos);

  // ! Signals that the node was untagged.
  /**
   * Invoked after a tag was removed from the node.
   *
   * @param node The node a tag was removed from.
   * @param tag The tag that was removed from the node.
   */
  void removedTag(ViewNode node, Tag tag);
}
