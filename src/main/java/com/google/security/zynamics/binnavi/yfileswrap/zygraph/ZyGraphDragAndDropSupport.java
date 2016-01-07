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

import java.awt.Color;
import java.awt.Point;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.TooManyListenersException;

import javax.swing.TransferHandler;

import y.view.Graph2DTraversal;
import y.view.Graph2DView;
import y.view.HitInfo;
import y.view.HitInfoFactory;

import com.google.security.zynamics.binnavi.disassembly.INaviOperandTreeNode;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLineContent.ObjectWrapper;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyNodeContentHelpers;

/**
 * A supporting class that enables drag and drop operations for a ZyGraph. The given transfer
 * handler must not be registered with the graph view. Instead, client code has to call the
 * enableDndSupport method. Since the high level Swing API does not support drag over events, we
 * have to manually register an additional drop target listener. However, this can only be done
 * after the given transfer handler has been added (otherwise we get a null pointer exception).
 *
 *  This class could in the future be extended to enable client code to create pairs of classes,
 * i.e. transfer handler together with a corresponding renderer, in order to support dnd and visual
 * feedback while dragging objects from other components into the graph view.
 *
 *
 * @author jannewger (Jan Newger)
 *
 */
public class ZyGraphDragAndDropSupport {

  private final ZyGraph graph;
  private final TransferHandler transferHandler;
  private NaviNode lastNode;
  private int lastRow;

  private static final int HIGHLIGHTING_LEVEL = 0;
  private static final Color HIGHLIGHTING_COLOR = Color.ORANGE;

  /**
   * Creates a new instance to enable drag and drop support for the zygraph. The given transfer
   * handler replaces any other transfer handler as soon as enableDndSupport is invoked.
   *
   * @param graph The zygraph to enable drag and drop support for.
   * @param transferHandler The transfer handler that handles the drag operation and data transfer.
   */
  public ZyGraphDragAndDropSupport(final ZyGraph graph, final TransferHandler transferHandler) {
    this.graph = graph;
    this.transferHandler = transferHandler;
  }

  /**
   * Determine the drag over state for the given location, i.e. the object under the cursor.
   *
   * @param graph The zygraph for which to determine the drag over state.
   * @param location The location where to query the drag over state.
   * @return The drag over state for the zygraph at the given location.
   */
  public static DragOverState getDragOverState(final ZyGraph graph, final Point location) {
    final Graph2DView view = graph.getView();
    final double worldX = view.toWorldCoordX((int) location.getX());
    final double worldY = view.toWorldCoordY((int) location.getY());
    final HitInfoFactory factory = view.getHitInfoFactory();
    final HitInfo hit = factory.createHitInfo(worldX, worldY, Graph2DTraversal.NODES, true);
    if (hit.hasHitNodes()) {
      final NaviNode naviNode = graph.getMappings().getNode(hit.getHitNode());
      if (naviNode != null) {
        return new DragOverState(naviNode, ZyNodeContentHelpers.getObjectWrapper(
            naviNode, worldX, worldY));
      }
    }
    return new DragOverState(null, null);
  }

  private void clearHighlighting(final NaviNode cursorNode) {
    if (lastNode != null) {
      lastNode.clearHighlighting(HIGHLIGHTING_LEVEL, lastRow);
    }
    lastNode = cursorNode;
  }

  private void handleDragOver(final Point location) {
    final DragOverState state = getDragOverState(graph, location);
    final NaviNode node = state.getNode();
    if (node != null && state.getDragOverObject() != null) {
      final Object object = state.getDragOverObject().getObject();
      if (object instanceof INaviOperandTreeNode
          && ((INaviOperandTreeNode) object).getType() == ExpressionType.REGISTER) {
        final int row = node.positionToRow(graph.getView().toWorldCoordY(location.y) - node.getY());
        if (row != -1) {
          clearHighlighting(node);
          lastRow = row;
          final ObjectWrapper wrapper = state.getDragOverObject();
          node.setHighlighting(
              HIGHLIGHTING_LEVEL, row, wrapper.getStart(), wrapper.getLength(), HIGHLIGHTING_COLOR);
        }
      }
    } else {
      clearHighlighting(null);
    }
  }

  /**
   * Enables drag and drop support for the zygraph and transfer handler that were passed to the
   * constructor when this class was instantiated. Any existing transfer handler is replaced by this
   * operation.
   */
  public void enableDndSupport() {
    graph.getView().setTransferHandler(transferHandler);
    try {
      graph.getView().getDropTarget().addDropTargetListener(new DropTargetListener() {
        @Override
        public void dragEnter(final DropTargetDragEvent dtde) {
        }

        @Override
        public void dragExit(final DropTargetEvent dte) {
          clearHighlighting(null);
        }

        @Override
        public void dragOver(final DropTargetDragEvent dtde) {
          handleDragOver(dtde.getLocation());
        }

        @Override
        public void drop(final DropTargetDropEvent dtde) {
          clearHighlighting(null);
        }

        @Override
        public void dropActionChanged(final DropTargetDragEvent dtde) {
        }
      });
    } catch (final TooManyListenersException e) {
      e.printStackTrace();
    }
  }

  /**
   * Represents the objects under the cursor during an ongoing drag operation.
   *
   * @author jannewger (Jan Newger)
   *
   */
  public static class DragOverState {

    private final NaviNode node;
    private final ObjectWrapper dragOverObject;

    public DragOverState(final NaviNode node, final ObjectWrapper dragOverObject) {
      this.node = node;
      this.dragOverObject = dragOverObject;
    }

    public ObjectWrapper getDragOverObject() {
      return dragOverObject;
    }

    public NaviNode getNode() {
      return node;
    }
  }
}