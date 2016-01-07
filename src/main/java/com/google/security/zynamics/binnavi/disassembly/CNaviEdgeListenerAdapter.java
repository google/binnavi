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
import com.google.security.zynamics.zylib.gui.zygraph.edges.CBend;
import com.google.security.zynamics.zylib.gui.zygraph.edges.CViewEdge;
import com.google.security.zynamics.zylib.gui.zygraph.edges.EdgeType;
import com.google.security.zynamics.zylib.gui.zygraph.edges.IViewEdge;

import java.awt.Color;
import java.util.List;



/**
 * Adapter class for classes that only want to listen on a few edge events.
 */
public class CNaviEdgeListenerAdapter implements INaviEdgeCommentListener {
  @Override
  public void addedBend(final IViewEdge<?> edge, final CBend path) {
    // Empty default implementation
  }

  @Override
  public void appendedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void appendedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void changedColor(final CViewEdge<?> edge, final Color color) {
    // Empty default implementation
  }

  @Override
  public void changedSelection(final IViewEdge<?> edge, final boolean selected) {
    // Empty default implementation
  }

  @Override
  public void changedSourceX(final CViewEdge<?> edge, final double sourceX) {
    // Empty default implementation
  }

  @Override
  public void changedSourceY(final CViewEdge<?> edge, final double sourceY) {
    // Empty default implementation
  }

  @Override
  public void changedTargetX(final CViewEdge<?> edge, final double targetX) {
    // Empty default implementation
  }

  @Override
  public void changedTargetY(final CViewEdge<?> edge, final double targetY) {
    // Empty default implementation
  }

  @Override
  public void changedType(final CViewEdge<?> edge, final EdgeType type) {
    // Empty default implementation
  }

  @Override
  public void changedVisibility(final IViewEdge<?> edge, final boolean visibility) {
    // Empty default implementation
  }

  @Override
  public void clearedBends(final IViewEdge<?> edge) {
    // Empty default implementation
  }

  @Override
  public void deletedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void deletedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void editedGlobalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void editedLocalEdgeComment(final INaviEdge edge, final IComment comment) {
    // Empty default implementation
  }

  @Override
  public void initializedGlobalEdgeComment(final INaviEdge edge, final List<IComment> comments) {
    // Empty default implementation
  }

  @Override
  public void initializedLocalEdgeComment(final INaviEdge edge, final List<IComment> comments) {
    // Empty default implementation
  }

  @Override
  public void insertedBend(final IViewEdge<?> edge, final int index, final CBend path) {
    // Empty default implementation
  }

  @Override
  public void removedBend(final CViewEdge<?> edge, final int index, final CBend path) {
    // Empty default implementation
  }
}
