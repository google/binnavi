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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.google.security.zynamics.binnavi.Tagging.ITagManager;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Panel that shows the node tagging tree.
 */
public final class CTaggingPanel extends JPanel {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1584298280294554416L;

  /**
   * The node tagging tree.
   */
  private final CTagsTree m_tagsTree;

  /**
   * Creates a new panel object.
   *
   * @param parent Parent window used for dialogs.
   * @param graph Graph whose nodes are tagged.
   * @param manager Provides tag information.
   */
  public CTaggingPanel(final JFrame parent, final ZyGraph graph, final ITagManager manager) {
    super(new BorderLayout());

    m_tagsTree = new CTagsTree(parent, graph, manager);

    final JScrollPane pane = new JScrollPane(m_tagsTree);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    add(pane);

    setBorder(new TitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), "Tagging"));

    setDoubleBuffered(true);
  }

  /**
   * Returns the tag tree.
   *
   * @return The tag tree.
   */
  public CTagsTree getTree() {
    return m_tagsTree;
  }
}
