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

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IGroupNodeListener;



/**
 * Interface for listeners that want to be notified about changes in group nodes.
 */
public interface INaviGroupNodeListener extends IGroupNodeListener<INaviGroupNode, IComment> {
  /**
   * Invoked after an element was added to a group node.
   * 
   * @param groupNode The group node where the element was added.
   * @param node The element that was added to the group node.
   */
  void addedElement(INaviGroupNode groupNode, INaviViewNode node);

  /**
   * Invoked after a group node was collapsed or expanded.
   * 
   * @param node The group node that was collapsed or expanded.
   */
  void changedState(INaviGroupNode node);

  /**
   * Invoked after an element was removed from a group node.
   * 
   * @param groupNode The group node from which the element was removed.
   * @param node The element removed from the group node.
   */
  void removedElement(INaviGroupNode groupNode, INaviViewNode node);
}
