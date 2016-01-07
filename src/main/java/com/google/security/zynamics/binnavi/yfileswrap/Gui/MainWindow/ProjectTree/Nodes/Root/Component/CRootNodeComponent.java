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
package com.google.security.zynamics.binnavi.yfileswrap.Gui.MainWindow.ProjectTree.Nodes.Root.Component;

import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.CAbstractNodeComponent;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.ScrollPaneConstants;

import y.view.DefaultBackgroundRenderer;
import y.view.Graph2D;
import y.view.Graph2DView;


/**
 * Component that is shown in the right side of the main window when the root node of the project
 * tree is selected.
 */
public final class CRootNodeComponent extends CAbstractNodeComponent {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 8966402427627112682L;

  /**
   * Background image that shows the BinNavi logo.
   */
  private final Image BACKGROUND_IMAGE = new ImageIcon(
      CMain.class.getResource("data/binnavi_logo3.png")).getImage();

  /**
   * Creates a new component object.
   */
  public CRootNodeComponent() {
    super(new BorderLayout());

    createGui();
  }

  /**
   * Creates the GUI of this component.
   */
  private void createGui() {
    final Graph2DView view = new ZyGraph2DView(new Graph2D());
    view.setScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    final DefaultBackgroundRenderer backgroundRenderer = new DefaultBackgroundRenderer(view);
    backgroundRenderer.setImage(BACKGROUND_IMAGE);
    backgroundRenderer.setMode(DefaultBackgroundRenderer.CENTERED);
    backgroundRenderer.setColor(Color.white);

    view.setBackgroundRenderer(backgroundRenderer);

    add(view, BorderLayout.CENTER);
  }

  @Override
  public void dispose() {
    // No listeners to dispose
  }
}
