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
package com.google.security.zynamics.binnavi.disassembly.views;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IViewListener;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



/**
 * Interface for objects that want to be notified about changes in views.
 */
public interface INaviViewListener extends IViewListener<INaviView> {
  /**
   * Invoked after an edge was added to a view.
   * 
   * @param view The view where the edge was added.
   * @param edge The edge that was added to the view.
   */
  void addedEdge(INaviView view, INaviEdge edge);

  /**
   * Invoked after a node was added to a view.
   * 
   * @param view The view where the node was added.
   * @param node The node that was added to the view.
   */
  void addedNode(INaviView view, INaviViewNode node);

  /**
   * Invoked after nodes were added to a view.
   * 
   * @param view The view where the nodes have been added.
   * @param nodes The nodes that have been added.
   */
  void addedNodes(INaviView view, Collection<INaviViewNode> nodes);

  /**
   * Invoked after a global comment has been appended to an edge.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge whose global comment changed.
   */
  void appendedGlobalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a local edge comment has been appended.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the local comment was appended.
   */
  void appendedLocalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a new local function node comment was appended.
   * 
   * @param view The view the function node belongs to.
   * @param node The node where the local function comment was appended.
   * @param comment The comment which was appended.
   */
  void appendedLocalFunctionNodeComment(INaviView view, INaviFunctionNode node, IComment comment);

  /**
   * Invoked after the border color of a node in a view was changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose border color changed.
   * @param color The new border color of the node.
   */
  void changedBorderColor(INaviView view, IViewNode<?> node, Color color);

  /**
   * Invoked after the color of an edge in a view was changed.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge whose color changed.
   * @param color The new color of the edge.
   */
  void changedColor(INaviView view, CNaviViewEdge edge, Color color);

  /**
   * Invoked after the color of a node in a view was changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose color changed.
   * @param color The new color of the node.
   */
  void changedColor(INaviView view, IViewNode<?> node, Color color);

  /**
   * Invoked after the type of a view graph changed.
   * 
   * @param view The view whose graph type changed.
   * @param type The new graph type of the view.
   * @param oldType The previous graph type of the view.
   */
  void changedGraphType(INaviView view, GraphType type, GraphType oldType);

  /**
   * Invoked after the modification state of the view changed.
   * 
   * @param view The view whose modification state changed.
   * @param value The new modification state.
   */
  void changedModificationState(INaviView view, boolean value);

  /**
   * Invoked after the parent group of a node in a view changed.
   * 
   * @param view The view the node belong sto.
   * @param node The node whose parent group changed.
   * @param groupNode The new parent group of the node. This argument can be null.
   */
  void changedParentGroup(INaviView view, INaviViewNode node, INaviGroupNode groupNode);

  /**
   * Invoked after the selection state of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose selection state changed.
   * @param selected The new selection state of the node.
   */
  void changedSelection(INaviView view, IViewNode<?> node, boolean selected);

  /**
   * Invoked after the star state of a view changed.
   * 
   * @param view The view whose star state changed.
   * @param isStared The new star state of the view.
   */
  void changedStarState(INaviView view, boolean isStared);

  /**
   * Invoked after the visibility state of an edge in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param edge The edge whose visibility state changed.
   */
  void changedVisibility(INaviView view, IViewEdge<?> edge);

  /**
   * Invoked after the visibility state of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose visibility state changed.
   * @param visible The new visibility state of the node.
   */
  void changedVisibility(INaviView view, IViewNode<?> node, boolean visible);

  /**
   * Invoked after a view was closed.
   * 
   * @param view The view that was closed.
   * @param oldGraph The graph of the closed view.
   */
  void closedView(INaviView view, IDirectedGraph<INaviViewNode, INaviEdge> oldGraph);

  /**
   * Invoked after an edge was deleted from a view.
   * 
   * @param view The view from which the edge was deleted.
   * @param edge The deleted edge.
   */
  void deletedEdge(INaviView view, INaviEdge edge);

  /**
   * Invoked after a global edge comment has been deleted.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the comment was deleted.
   */
  void deletedGlobalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a local edge comment has been deleted.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the local comment has been deleted.
   */
  void deletedLocalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a local function node comment has been deleted.
   * 
   * @param view The view where the function node belongs to.
   * @param node The node where the local function comment was deleted.
   * @param comment The comment which was deleted.
   */
  void deletedLocalFunctionNodeComment(INaviView view, INaviFunctionNode node, IComment comment);

  /**
   * Invoked after a node was deleted from a view.
   * 
   * @param view The view from which the node was deleted.
   * @param node The deleted node.
   */
  void deletedNode(INaviView view, INaviViewNode node);

  /**
   * Invoked after nodes were deleted from a view.
   * 
   * @param view The view from which the node was deleted.
   * @param nodes The deleted nodes.
   */
  void deletedNodes(INaviView view, Collection<INaviViewNode> nodes);

  /**
   * Invoked after a global edge comment has been edited.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the comment has been edited.
   */
  void editedGlobalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a local edge comment was edited.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the local comment was edited.
   */
  void editedLocalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after a local function node comment has been edited.
   * 
   * @param view The view where the function node belongs to.
   * @param node The node where the local function comment was edited.
   * @param comment The comment which was edited.
   */
  void editedLocalFunctionNodeComment(INaviView view, INaviFunctionNode node, IComment comment);

  /**
   * Invoked after the height of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose height changed.
   * @param height The new height of the node.
   */
  void heightChanged(INaviView view, IViewNode<?> node, double height);

  /**
   * Invoked after the global edge comments have been initialized.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the comments have been initialized.
   */
  void initializedGlobalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after the local function node comments have been initialized.
   * 
   * @param view The view where the function node belongs to.
   * @param node The node where the local function comments where initialized.
   * @param comments The comments with which the function node was initialized.
   */
  void initializedLoalFunctionNodeComment(INaviView view, INaviFunctionNode node,
      ArrayList<IComment> comments);

  /**
   * Invoked after the local edge comments have been initialized.
   * 
   * @param view The view the edge belongs to.
   * @param edge The edge where the local comments have been initialized.
   */
  void initializedLocalEdgeComment(INaviView view, INaviEdge edge);

  /**
   * Invoked after each event of a view load operation.
   * 
   * @param event The load event.
   * @param counter The index of the load event.
   * 
   * @return True, to continue loading. False, to cancel it.
   */
  boolean loading(ViewLoadEvents event, int counter);

  /**
   * Invoked after a view was saved.
   * 
   * @param view The saved view.
   */
  void savedView(INaviView view);

  /**
   * Invoked after a node of a view was tagged with a tag.
   * 
   * @param view The view where a node was tagged.
   * @param node The node that was tagged.
   * @param tag The tag the node was tagged with.
   */
  void taggedNode(INaviView view, INaviViewNode node, CTag tag);

  /**
   * Invoked after a view was tagged.
   * 
   * @param view The view that was tagged.
   * @param tag The tag added to the view.
   */
  void taggedView(INaviView view, CTag tag);

  /**
   * Invoked after a node of a view was untagged.
   * 
   * @param view The view where the node was untagged.
   * @param node The node that was untagged.
   * @param tags The tags removed from the node.
   */
  void untaggedNodes(INaviView view, INaviViewNode node, List<CTag> tags);

  /**
   * Invoked after a view was untagged.
   * 
   * @param view The view that was untagged.
   * @param tag The tag removed from the view.
   */
  void untaggedView(INaviView view, CTag tag);

  /**
   * Invoked after the width of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose width changed.
   * @param width The new width of the node.
   */
  void widthChanged(INaviView view, IViewNode<?> node, double width);

  /**
   * Invoked after the x-coordinate of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose x-coordinate changed.
   * @param xpos The new x-coordinate of the node.
   */
  void xposChanged(INaviView view, IViewNode<?> node, double xpos);

  /**
   * Invoked after the y-coordinate of a node in a view changed.
   * 
   * @param view The view the node belongs to.
   * @param node The node whose y-coordinate changed.
   * @param ypos The new y-coordinate of the node.
   */
  void yposChanged(INaviView view, IViewNode<?> node, double ypos);
}
