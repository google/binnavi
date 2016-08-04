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
package com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.realizers;

import com.google.security.zynamics.zylib.gui.zygraph.nodes.ZyNodeData;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IRealizerUpdater;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.IZyNodeRealizerListener;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.ZyLabelContent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.nodes.ZyGraphNode;

import y.view.NodeRealizer;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

public interface IZyNodeRealizer {
  void addListener(IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener);

  void calcUnionRect(Rectangle2D rectangle);

  Double getBoundingBox();

  double getCenterX();

  double getCenterY();

  Color getFillColor();

  double getHeight();

  ZyLabelContent getNodeContent();

  NodeRealizer getRealizer();

  IRealizerUpdater<? extends ZyGraphNode<?>> getUpdater();

  ZyNodeData<?> getUserData();

  double getWidth();

  double getX();

  double getY();

  boolean isSelected();

  boolean isVisible();

  default int positionToRow(double y) {
    // TODO: This does not really work because line heights are not constant.

    final ZyLabelContent content = getNodeContent();

    final Rectangle2D contentBounds = getNodeContent().getBounds();
    final double yratio = getHeight() / contentBounds.getHeight();

    final int row = (int) ((y / yratio - content.getPaddingTop()) / content.getLineHeight());

    return row >= content.getLineCount() ? -1 : row;
  }

  void regenerate();

  void removeListener(IZyNodeRealizerListener<? extends ZyGraphNode<?>> listener);

  void repaint();

  default double rowToPosition(int row) {
    final ZyLabelContent content = getNodeContent();

    return content.getPaddingTop() + row * content.getLineHeight();
  }

  void setFillColor(Color color);

  void setHeight(double value);

  void setLineColor(Color borderColor);

  void setSelected(boolean selected);

  void setUpdater(IRealizerUpdater<? extends ZyGraphNode<?>> updater);

  void setUserData(final ZyNodeData<?> data);

  void setWidth(double value);

  void setX(double x);

  void setY(double y);

  default void updateContentSelectionColor() {
    // TODO: This is kind of a hack, should be improved
    final ZyLabelContent content = getNodeContent();

    if (content.isSelectable()) {
      content.updateContentSelectionColor(getFillColor(), isSelected());
    }
  }
}
