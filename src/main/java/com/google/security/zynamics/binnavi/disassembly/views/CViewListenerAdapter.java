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
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Listener adapter that can be used by listener objects that do not want to listen on all view
 * notification methods.
 */
public class CViewListenerAdapter implements INaviViewListener {
  @Override
  public void addedEdge(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void addedNode(final INaviView view, final INaviViewNode node) {
    // Empty default implementation
  }

  @Override
  public void addedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
    // Empty default implementation
  }

  @Override
  public void appendedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void appendedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void appendedLocalFunctionNodeComment(
      final INaviView view, final INaviFunctionNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void changedBorderColor(final INaviView view, final IViewNode<?> node, final Color color) {
    // Empty default implementation
  }

  @Override
  public void changedColor(final INaviView view, final CNaviViewEdge edge, final Color color) {
    // Empty default implementation
  }

  @Override
  public void changedColor(final INaviView view, final IViewNode<?> node, final Color color) {
    // Empty default implementation
  }

  @Override
  public void changedDescription(final INaviView view, final String description) {
    // Empty default implementation
  }

  @Override
  public void changedGraphType(
      final INaviView view, final GraphType type, final GraphType oldType) {
    // Empty default implementation
  }

  @Override
  public void changedModificationDate(final INaviView view, final Date modificationDate) {
    // Empty default implementation
  }

  @Override
  public void changedModificationState(final INaviView view, final boolean value) {
    // Empty default implementation
  }

  @Override
  public void changedName(final INaviView view, final String name) {
    // Empty default implementation
  }

  @Override
  public void changedParentGroup(
      final INaviView view, final INaviViewNode node, final INaviGroupNode groupNode) {
    // Empty default implementation
  }

  @Override
  public void changedSelection(
      final INaviView view, final IViewNode<?> node, final boolean selected) {
    // Empty default implementation
  }

  @Override
  public void changedStarState(final INaviView view, final boolean isStared) {
    // Empty default implementation
  }

  @Override
  public void changedVisibility(final INaviView view, final IViewEdge<?> edge) {
    // Empty default implementation
  }

  @Override
  public void changedVisibility(
      final INaviView view, final IViewNode<?> node, final boolean visible) {
    // Empty default implementation
  }

  @Override
  public void closedView(
      final INaviView view, final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
    // Empty default implementation
  }

  @Override
  public boolean closingView(final INaviView view) {
    // Empty default implementation
    return true;
  }

  @Override
  public void deletedEdge(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void deletedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void deletedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void deletedLocalFunctionNodeComment(
      final INaviView view, final INaviFunctionNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void deletedNode(final INaviView view, final INaviViewNode node) {
    // Empty default implementation
  }

  @Override
  public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
    // Empty default implementation
  }

  @Override
  public void editedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void editedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void editedLocalFunctionNodeComment(
      final INaviView view, final INaviFunctionNode node, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void heightChanged(final INaviView view, final IViewNode<?> node, final double height) {
    // Empty default implementation
  }

  @Override
  public void initializedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void initializedLoalFunctionNodeComment(
      final INaviView view, final INaviFunctionNode node, final ArrayList<IComment> comments) {
    // Empty default implementation
  }

  @Override
  public void initializedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
    // Empty default implementation
  }

  @Override
  public void loadedView(final INaviView view) {
    // Empty default implementation
  }

  @Override
  public boolean loading(final ViewLoadEvents event, final int counter) {
    // Empty default implementation
    return true;
  }

  @Override
  public void savedView(final INaviView view) {
    // Empty default implementation
  }

  @Override
  public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
    // Empty default implementation
  }

  @Override
  public void taggedView(final INaviView view, final CTag tag) {
    // Empty default implementation
  }

  @Override
  public void untaggedNodes(final INaviView view, final INaviViewNode node, final List<CTag> tags) {
    // Empty default implementation
  }

  @Override
  public void untaggedView(final INaviView view, final CTag tag) {
    // Empty default implementation
  }

  @Override
  public void widthChanged(final INaviView view, final IViewNode<?> node, final double height) {
    // Empty default implementation
  }

  @Override
  public void xposChanged(final INaviView view, final IViewNode<?> node, final double xpos) {
    // Empty default implementation
  }

  @Override
  public void yposChanged(final INaviView view, final IViewNode<?> node, final double ypos) {
    // Empty default implementation
  }
}
