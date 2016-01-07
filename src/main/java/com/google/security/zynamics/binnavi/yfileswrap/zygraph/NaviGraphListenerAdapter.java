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
package com.google.security.zynamics.binnavi.yfileswrap.zygraph;

import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.ZyGraph.INaviGraphListener;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.proximity.ZyProximityNode;

import java.awt.event.MouseEvent;

import y.view.EdgeLabel;


/**
 * Adapter class that can be used by classes that want to listen only on a few select graph events.
 */
public class NaviGraphListenerAdapter implements INaviGraphListener {
  @Override
  public void addedNode(final ZyGraph graph, final NaviNode zyNode) {
    // Empty default implementation
  }

  @Override
  public void changedModel(final ZyGraph zyGraph, final NaviNode node) {
    // Empty default implementation
  }

  @Override
  public void changedView(final INaviView oldView, final INaviView newView) {
    // Empty default implementation
  }

  @Override
  public void edgeClicked(
      final NaviEdge node, final MouseEvent event, final double x, final double y) {
    // Empty default implementation
  }

  @Override
  public void edgeLabelEntered(final EdgeLabel label, final MouseEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void edgeLabelExited(final EdgeLabel label) {
    // TODO Auto-generated method stub

  }

  @Override
  public void nodeClicked(
      final NaviNode node, final MouseEvent event, final double x, final double y) {
    // Empty default implementation
  }

  @Override
  public void nodeEntered(final NaviNode node, final MouseEvent event) {
    // Empty default implementation
  }

  @Override
  public void nodeHovered(final NaviNode node, final double x, final double y) {
    // Empty default implementation
  }

  @Override
  public void nodeLeft(final NaviNode node) {
    // Empty default implementation
  }

  @Override
  public void proximityBrowserNodeClicked(final ZyProximityNode<?> proximityNode,
      final MouseEvent event, final double x, final double y) {
    // Empty default implementation
  }

  @Override
  public void taggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
    // Empty default implementation
  }

  @Override
  public void untaggedNode(final INaviView view, final INaviViewNode node, final CTag tag) {
    // Empty default implementation
  }
}
