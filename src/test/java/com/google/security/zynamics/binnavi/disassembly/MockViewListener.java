/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.disassembly.CNaviViewEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviViewListener;
import com.google.security.zynamics.binnavi.disassembly.views.ViewLoadEvents;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.nodes.IViewNode;
import com.google.security.zynamics.zylib.types.graphs.IDirectedGraph;

public final class MockViewListener implements INaviViewListener {
  public String eventList = "";

  public boolean m_closing = true;

  @Override
  public void addedEdge(final INaviView view, final INaviEdge edge) {
    eventList += "addedEdge/";
  }

  @Override
  public void addedNode(final INaviView view, final INaviViewNode node) {
    eventList += "addedNode/";
  }

  @Override
  public void addedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
    // TODO Auto-generated method stub

  }

  @Override
  public void appendedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void appendedLocalEdgeComment(final INaviView view, final INaviEdge edge) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void appendedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
      final IComment comment) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedBorderColor(final INaviView view, final IViewNode<?> node, final Color color) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedColor(final INaviView view, final CNaviViewEdge edge, final Color color) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedColor(final INaviView view, final IViewNode<?> node, final Color color) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedDescription(final INaviView view, final String description) {
    eventList += "changedDescription/";
  }

  @Override
  public void changedGraphType(final INaviView view, final GraphType type, final GraphType oldType) {
    eventList += "changedGraphType/";
  }

  @Override
  public void changedModificationDate(final INaviView view, final Date modificationDate) {
    eventList += "changedModificationDate/";
  }

  @Override
  public void changedModificationState(final INaviView view, final boolean value) {
    eventList += "changedModificationState/";
  }

  @Override
  public void changedName(final INaviView view, final String name) {
    eventList += "changedName/";
  }

  @Override
  public void changedParentGroup(final INaviView view, final INaviViewNode node,
      final INaviGroupNode groupNode) {
    eventList += "changedParentGroup/";
  }

  @Override
  public void changedSelection(final INaviView view, final IViewNode<?> node, final boolean selected) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedStarState(final INaviView view, final boolean isStared) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedVisibility(final INaviView view, final IViewEdge<?> edge) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void changedVisibility(final INaviView view, final IViewNode<?> node, final boolean visible) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void closedView(final INaviView view,
      final IDirectedGraph<INaviViewNode, INaviEdge> oldGraph) {
    eventList += "closedView/";
  }

  @Override
  public boolean closingView(final INaviView view) {
    eventList += "closingView/";

    return m_closing;
  }

  @Override
  public void deletedEdge(final INaviView view, final INaviEdge edge) {
    eventList += "deletedEdge/";
  }

  @Override
  public void deletedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void deletedLocalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void deletedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
      final IComment comment) {

  }

  @Override
  public void deletedNode(final INaviView view, final INaviViewNode node) {
    eventList += "deletedNode/";
  }

  @Override
  public void deletedNodes(final INaviView view, final Collection<INaviViewNode> nodes) {
    eventList += "deletedNodes/";
  }

  @Override
  public void editedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void editedLocalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void editedLocalFunctionNodeComment(final INaviView view, final INaviFunctionNode node,
      final IComment comment) {

  }

  @Override
  public void heightChanged(final INaviView view, final IViewNode<?> node, final double height) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void initializedGlobalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void initializedLoalFunctionNodeComment(final INaviView view,
      final INaviFunctionNode node, final ArrayList<IComment> comments) {

  }

  @Override
  public void initializedLocalEdgeComment(final INaviView view, final INaviEdge edge) {

  }

  @Override
  public void loadedView(final INaviView view) {
    eventList += "loadedView/";
  }

  @Override
  public boolean loading(final ViewLoadEvents event, final int counter) {
    return true;
  }

  @Override
  public void savedView(final INaviView view) {
    eventList += "savedView/";
  }

  @Override
  public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void taggedView(final INaviView view, final CTag tag) {
    eventList += "taggedView/";
  }

  @Override
  public void untaggedNodes(final INaviView view, final INaviViewNode node, final List<CTag> tags) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void untaggedView(final INaviView view, final CTag tag) {
    eventList += "untaggedView/";
  }

  @Override
  public void widthChanged(final INaviView view, final IViewNode<?> node, final double height) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void xposChanged(final INaviView view, final IViewNode<?> node, final double xpos) {

    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void yposChanged(final INaviView view, final IViewNode<?> node, final double ypos) {

    throw new IllegalStateException("Not yet implemented");
  }
}
