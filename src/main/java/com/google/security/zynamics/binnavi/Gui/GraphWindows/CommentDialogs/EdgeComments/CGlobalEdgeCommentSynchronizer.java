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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.EdgeComments;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.Quad;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronizes global code node comments between all open views.
 */
public final class CGlobalEdgeCommentSynchronizer {
  /**
   * You are not supposed to instantiate this class.
   */
  private CGlobalEdgeCommentSynchronizer() {
  }

  /**
   * Collects the edges of a view.
   *
   * @param view The view whose edges are collected.
   * @param refEdgeData The edges to look for.
   *
   * @return The collected edges.
   *
   * @throws MaybeNullException
   */
  private static List<INaviEdge> collectEdges(
      final INaviView view, final Quad<Integer, IAddress, Integer, IAddress> refEdgeData)
      throws MaybeNullException {
    final List<INaviEdge> edgelist = new ArrayList<INaviEdge>();

    if (view.isLoaded()) {
      for (final INaviEdge otherEdge : view.getGraph().getEdges()) {
        final Quad<Integer, IAddress, Integer, IAddress> edgeData = getEdgeData(otherEdge);

        if (isEdgeDataValid(edgeData) && edgeData.equals(refEdgeData)) {
          edgelist.add(otherEdge);
        }
      }
    }

    return edgelist;
  }

  /**
   * Returns the edge data of a given edge.
   *
   * @param edge An edge.
   *
   * @return <Source Module ID, Source Address, Target Module ID, Target Address>
   *
   * @throws MaybeNullException Thrown if the edge data could not be determined.
   */
  private static Quad<Integer, IAddress, Integer, IAddress> getEdgeData(final INaviEdge edge)
      throws MaybeNullException {
    IAddress srcAddr = null;
    IAddress dstAddr = null;

    int srcModuleId = -1;
    int dstModuleId = -1;

    if (edge.getSource() instanceof INaviCodeNode) {
      srcAddr = ((INaviCodeNode) edge.getSource()).getAddress();
      srcModuleId = ((INaviCodeNode) edge.getSource()).getParentFunction()
          .getModule().getConfiguration().getId();
    } else if (edge.getSource() instanceof INaviFunctionNode) {
      srcAddr = ((INaviFunctionNode) edge.getSource()).getFunction().getAddress();
      srcModuleId = ((INaviFunctionNode) edge.getSource()).getFunction()
          .getModule().getConfiguration().getId();
    }

    if (edge.getTarget() instanceof INaviCodeNode) {
      dstAddr = ((INaviCodeNode) edge.getTarget()).getAddress();
      dstModuleId = ((INaviCodeNode) edge.getTarget()).getParentFunction()
          .getModule().getConfiguration().getId();
    } else if (edge.getTarget() instanceof INaviFunctionNode) {
      dstAddr = ((INaviFunctionNode) edge.getTarget()).getFunction().getAddress();
      dstModuleId = ((INaviFunctionNode) edge.getTarget()).getFunction()
          .getModule().getConfiguration().getId();
    }

    return new Quad<Integer, IAddress, Integer, IAddress>(
        srcModuleId, srcAddr, dstModuleId, dstAddr);

  }

  /**
   * Returns the modules the nodes of the edge belong to.
   *
   * @param edge The edge to check.
   *
   * @return Source module and target module of the edge.
   *
   * @throws MaybeNullException Thrown if the edge does not have source module or target module.
   */
  private static Pair<INaviModule, INaviModule> getModules(final INaviEdge edge)
      throws MaybeNullException {
    INaviModule srcModule = null;
    INaviModule tarModule = null;

    if (edge.getSource() instanceof INaviCodeNode) {
      srcModule = ((INaviCodeNode) edge.getSource()).getParentFunction().getModule();
    } else if (edge.getSource() instanceof INaviFunctionNode) {
      srcModule = ((INaviFunctionNode) edge.getSource()).getFunction().getModule();
    }

    if (edge.getTarget() instanceof INaviCodeNode) {
      tarModule = ((INaviCodeNode) edge.getTarget()).getParentFunction().getModule();
    } else if (edge.getTarget() instanceof INaviFunctionNode) {
      tarModule = ((INaviFunctionNode) edge.getTarget()).getFunction().getModule();
    }

    return new Pair<INaviModule, INaviModule>(srcModule, tarModule);

  }

  /**
   * Checks whether edge data is valid for a global edge comment.
   *
   * @param edgeData The edge data to check.
   *
   * @return True, if the data is valid. False, otherwise.
   */
  private static boolean isEdgeDataValid(
      final Quad<Integer, IAddress, Integer, IAddress> edgeData) {
    if ((edgeData.second() == null) || (edgeData.fourth() == null)) {
      return false;
    }

    if ((edgeData.first() == -1) || (edgeData.third() == -1)) {
      return false;
    }

    return true;
  }

  /**
   * Pushes a new global edge comment to all open views.
   *
   * @param edge The edge that has the new comment.
   * @param comments The new comment of the code node.
   *
   * @throws CouldntSaveDataException Thrown if updating the edge comments failed.
   */
  public static void updateOpenViews(final INaviEdge edge, final ArrayList<IComment> comments)
      throws CouldntSaveDataException {
    try {
      final Pair<INaviModule, INaviModule> modules = getModules(edge);
      if (modules.first() != modules.second()) {
        // TODO: Handle this
        return;
      }

      if ((modules.first() != null) && (modules.second() != null) && modules.first().isLoaded()) {
        final List<INaviEdge> edgelist = new ArrayList<INaviEdge>();
        final Quad<Integer, IAddress, Integer, IAddress> refEdgeData = getEdgeData(edge);
        for (final INaviView view : modules.first().getContent().getViewContainer().getViews()) {
          edgelist.addAll(collectEdges(view, refEdgeData));
        }
        for (final INaviEdge updateEdge : edgelist) {
          updateEdge.initializeGlobalComment(comments);
        }
      }
    } catch (final MaybeNullException exception) {
      // Trying to update global comments of code nodes without global comments.
    }
  }
}
