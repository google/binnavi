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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.Right;

import com.google.security.zynamics.binnavi.Gui.Debug.OptionsPanel.COptionsPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTaggingPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.NodeTaggingTree.CTagsTree;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.IPerspectiveModelListener;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.PerspectiveType;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeEditorPanel;
import com.google.security.zynamics.binnavi.disassembly.types.TypeManager;
import com.google.security.zynamics.zylib.disassembly.GraphType;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.tree.DefaultTreeSelectionModel;


/**
 * The panel shown on the right side of the window that is updated depending on the selected
 * perspective.
 */
public final class CCombinedRightPanel extends JPanel {
  /**
   * Model of the graph the panel works on.
   */
  private final CGraphModel model;

  /**
   * Describes the selected perspective.requiresStackFramePanel
   */
  private final CPerspectiveModel perspectiveModel;

  /**
   * Panel where node tagging happens.
   */
  private final CTaggingPanel taggingPanel;

  /**
   * Inner panel that is updated on changes in the perspective.
   */
  private final JPanel debugPerspectivePanel;

  /**
   * Updates the panel on changes in the perspective.
   */
  private final IPerspectiveModelListener m_listener = new InternalPerspectiveListener();

  /**
   * Creates a new panel object.
   *
   * @param parent Parent window of the panel.
   * @param model Model of the graph the panel works on.
   * @param perspectiveModel Describes the selected perspective.
   */
  public CCombinedRightPanel(final JFrame parent, final CGraphModel model,
      final CPerspectiveModel perspectiveModel) {
    super(new BorderLayout());
    this.model = model;
    this.perspectiveModel = perspectiveModel;
    final TypeManager typeManager = model.getViewContainer().getModules().get(0).getTypeManager();

    taggingPanel = new CTaggingPanel(parent, model.getGraph(),
        model.getDatabase().getContent().getNodeTagManager());
    final JSplitPane taggingSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, taggingPanel,
        createTypePanels(parent, model, typeManager));
    taggingSplitPane.setOneTouchExpandable(true);
    add(taggingSplitPane);

    debugPerspectivePanel = new JPanel(new BorderLayout());
    add(debugPerspectivePanel, BorderLayout.SOUTH);
    perspectiveModel.addListener(m_listener);
    debugPerspectivePanel.setVisible(false);
  }

  private static JPanel createTypePanels(final JFrame parent, final CGraphModel model,
      final TypeManager typeManager) {
    final JPanel typePanel = new JPanel(new BorderLayout());
    final JPanel typeEditorPanel = TypeEditorPanel.CreateDefaultTypeEditor(parent, typeManager);
    if (isFunctionFlowgraph(model)) {
      final JSplitPane splitPaneTop = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPaneTop.setBorder(BorderFactory.createEmptyBorder());
      splitPaneTop.setOneTouchExpandable(true);
      final JSplitPane splitPaneBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      splitPaneBottom.setBorder(BorderFactory.createEmptyBorder());
      splitPaneBottom.setOneTouchExpandable(true);
      splitPaneTop.setTopComponent(createPrototypePanel(parent, model, typeManager));
      splitPaneTop.setBottomComponent(createStackFramePanel(parent, model, typeManager));
      splitPaneBottom.setTopComponent(splitPaneTop);
      splitPaneBottom.setBottomComponent(typeEditorPanel);
      typePanel.add(splitPaneBottom);
    } else {
      typePanel.add(typeEditorPanel, BorderLayout.CENTER);
    }
    return typePanel;
  }

  private static boolean isFunctionFlowgraph(final CGraphModel model) {
    return model.getGraph().getRawView().getContent().getGraphType().equals(GraphType.FLOWGRAPH)
        && model.getViewContainer().getFunction(model.getGraph().getRawView()) != null;
  }

  private static JPanel createStackFramePanel(final JFrame parent, final CGraphModel model,
      final TypeManager typeManager) {
    final JPanel stackFramePanel = new JPanel(new BorderLayout());
    stackFramePanel.add(TypeEditorPanel.CreateStackFrameEditor(parent, typeManager,
        model.getViewContainer().getFunction(model.getGraph().getRawView())), BorderLayout.CENTER);
    return stackFramePanel;
  }

  private static JPanel createPrototypePanel(final JFrame parent, final CGraphModel model,
      final TypeManager typeManager) {
    final JPanel prototypePanel = new JPanel(new BorderLayout());
    prototypePanel.add(TypeEditorPanel.CreatePrototypeEditor(parent, typeManager,
        model.getViewContainer().getFunction(model.getGraph().getRawView())), BorderLayout.CENTER);
    return prototypePanel;
  }

  /**
   * Returns the debug perspective model of the graph window.
   *
   * @return The debug perspective model.
   */
  private CDebugPerspectiveModel getDebugPerspective() {
    return ((CDebugPerspectiveModel) perspectiveModel.getModel(PerspectiveType.DebugPerspective));
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    taggingPanel.getTree().dispose();
    taggingPanel.getTree().setSelectionModel(new DefaultTreeSelectionModel());
  }

  /**
   * Returns the node tagging tree shown in the panel.
   *
   * @return The node tagging tree shown in the panel.
   */
  public CTagsTree getTree() {
    return taggingPanel.getTree();
  }

  /**
   * Updates the panel on changes in the perspective.
   */
  private class InternalPerspectiveListener implements IPerspectiveModelListener {
    @Override
    public void changedActivePerspective(final PerspectiveType activeView) {
      if ((activeView == PerspectiveType.DebugPerspective)
          && (getDebugPerspective().getCurrentSelectedDebugger() != null)) {
        final COptionsPanel optionsPanel = new COptionsPanel(model.getParent(),
            model.getDebuggerProvider().getDebugTarget(), model.getGraph(), getDebugPerspective());

        debugPerspectivePanel.add(optionsPanel);
      } else {
        debugPerspectivePanel.removeAll();
      }

      debugPerspectivePanel.setVisible(activeView == PerspectiveType.DebugPerspective);
    }
  }
}
