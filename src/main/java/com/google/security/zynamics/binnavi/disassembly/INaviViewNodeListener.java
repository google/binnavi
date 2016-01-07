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

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNodeListener;

import java.util.List;



/**
 * Interface for objects that want to be notified about changes in view nodes.
 */
public interface INaviViewNodeListener extends IViewNodeListener {
  /**
   * Invoked after the parent group of a node changed.
   * 
   * @param node The node whose parent group changed.
   * @param groupNode The new parent group of the node. This argument can be null.
   */
  void changedParentGroup(INaviViewNode node, INaviGroupNode groupNode);

  /**
   * Invoked after a node was tagged with a tag.
   * 
   * @param node The node that was tagged.
   * @param tag The tag the node was tagged with.
   */
  void taggedNode(INaviViewNode node, CTag tag);

  /**
   * Invoked after a node was untagged.
   * 
   * @param node The node that was untagged.
   * @param tags The tags removed from the node.
   */
  void untaggedNodes(INaviViewNode node, List<CTag> tags);
}
