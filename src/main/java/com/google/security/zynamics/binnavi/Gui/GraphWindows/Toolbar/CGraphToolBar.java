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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Toolbar;

import java.awt.Insets;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CMain;
import com.google.security.zynamics.binnavi.Gui.HotKeys;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphPanel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionCircularLayout;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionColorNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionDeleteSelectedNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionDeleteSelectedNodesKeep;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionFreezeView;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionHierarchicLayout;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionInvertSelection;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionMagnifyingGlassViewMode;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionOrthogonalLayout;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectByCriteria;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectChildren;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSelectParents;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionZoomFit;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionZoomIn;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionZoomOut;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionZoomSelected;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Goto.CGotoAddressField;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Searchers.Text.Gui.CGraphSearchPanel;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.gui.zygraph.settings.IDisplaySettingsListener;

/**
 * Toolbar class that provides the toolbar of graph panels. Using this toolbar the user can access
 * the most common graph actions.
 */
public final class CGraphToolBar extends JToolBar {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 6849359199616953169L;

  /**
   * Settings object synchronized with the tool bar elements.
   */
  private final ZyGraphViewSettings m_settings;

  /**
   * Panel that contains the Goto and Search fields.
   */
  private final CGraphToolBarNavigationPanel m_navigationPanel;

  /**
   * Action used to switch magnifying mode on and off.
   */
  private CActionMagnifyingGlassViewMode m_magnifierModeAction;

  /**
   * Updates elements of the tool bar on changes in the settings of the underlying graph.
   */
  private final InternalListener m_internalListener = new InternalListener();

  /**
   * Creates a new graph toolbar object.
   * 
   * @param parent Parent window used for dialogs.
   * @param graphPanel Panel in which the graph is opened.
   * @param graph The graph the toolbar is for.
   * @param modules The list of modules present in the current graph.
   */
  public CGraphToolBar(final JFrame parent, final CGraphPanel graphPanel, final ZyGraph graph,
      final List<INaviModule> modules) {
    Preconditions.checkNotNull(parent, "IE01622: Parent argument can not be null");
    Preconditions.checkNotNull(graph, "IE01623: Graph argument can not be null");
    Preconditions.checkNotNull(graphPanel, "IE01624: Action provider argument can't be null");

    m_settings = graph.getSettings();

    createButtons(parent, graph, graphPanel);

    m_navigationPanel = new CGraphToolBarNavigationPanel(graph, modules, parent);

    add(m_navigationPanel);

    setFloatable(false);

    m_settings.getDisplaySettings().addListener(m_internalListener);
  }

  /**
   * Adds a hotkey to a button.
   * 
   * @param button The button the hotkey is added to.
   * @param keyStroke The hotkey keystroke.
   * @param action Action to be executed on the keystroke.
   * @param name Name of the action.
   */
  private static void addHotkey(final JButton button, final KeyStroke keyStroke,
      final AbstractAction action, final String name) {
    final InputMap windowImap = button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

    windowImap.put(keyStroke, name);
    button.getActionMap().put(name, action);
  }

  /**
   * Adds a button to the toolbar.
   * 
   * @param action The action associated with the button.
   * @param rolloverIcon Path to the icon to be used when the mouse hovers over the button.
   * @param pressedIcon Path to the icon to be used when the button is pressed.
   * 
   * @return The created button.
   */
  private JButton addButton(final AbstractAction action, final String rolloverIcon,
      final String pressedIcon) {
    final JButton button = add(CActionProxy.proxy(action));

    button.setFocusable(false);
    button.setBorder(new EmptyBorder(new Insets(1, 0, 1, 0)));
    button.setRolloverIcon(new ImageIcon(CMain.class.getResource(rolloverIcon)));
    button.setPressedIcon(new ImageIcon(CMain.class.getResource(pressedIcon)));

    return button;
  }

  /**
   * Creates the toolbar.
   * 
   * @param parent Parent window used for dialogws.
   * @param graph The graph the toolbar is for.
   * @param graphPanel Panel in which the graph is shown.
   */
  private void createButtons(final JFrame parent, final ZyGraph graph, final CGraphPanel graphPanel) {
    final CActionZoomIn zoomInAction = new CActionZoomIn(graph);
    final JButton zoomInButton =
        addButton(zoomInAction, "data/magnify_hover.jpg", "data/magnify_down.jpg");
    addHotkey(zoomInButton, HotKeys.GRAPH_TOOLBAR_ZOOM_IN.getKeyStroke(), zoomInAction, "Zoom In");

    final CActionZoomOut zoomOutAction = new CActionZoomOut(graph);
    final JButton zoomOutButton =
        addButton(zoomOutAction, "data/reduce_hover.jpg", "data/reduce_down.jpg");
    addHotkey(zoomOutButton, HotKeys.GRAPH_TOOLBAR_ZOOM_OUT.getKeyStroke(), zoomOutAction,
        "Zoom Out");

    final CActionZoomSelected zoomSelectedAction = new CActionZoomSelected(graph);
    final JButton zoomSelectedButton =
        addButton(zoomSelectedAction, "data/frameall_hover.jpg", "data/frameall_down.jpg");
    addHotkey(zoomSelectedButton, HotKeys.GRAPH_TOOLBAR_ZOOM_SELECTED.getKeyStroke(),
        zoomSelectedAction, "Zoom Selected");

    final CActionZoomFit zoomFitAction = new CActionZoomFit(graph);
    final JButton zoomFitButton =
        addButton(zoomFitAction, "data/centerview_hover.jpg", "data/centerview_down.jpg");
    addHotkey(zoomFitButton, HotKeys.GRAPH_TOOLBAR_ZOOM_FIT.getKeyStroke(), zoomFitAction,
        "Fit Graph to Screen");

    m_magnifierModeAction = new CActionMagnifyingGlassViewMode(graph);
    final JButton magnifierModeButton =
        addButton(m_magnifierModeAction, "data/nomagnifieingglass_hover.jpg",
            "data/nomagnifieingglass_down.jpg");
    m_magnifierModeAction.setButton(magnifierModeButton);
    addHotkey(magnifierModeButton, HotKeys.GRAPH_TOOLBAR_TOGGLE_MAGNIFY.getKeyStroke(),
        m_magnifierModeAction, "Magnifying Glass");

    final CActionFreezeView freezeAction = new CActionFreezeView(graph);
    final JButton freezeButton =
        addButton(freezeAction, "data/viewnavi_hover.jpg", "data/viewnavi_down.jpg");
    freezeAction.setButton(freezeButton);
    addHotkey(freezeButton, HotKeys.GRAPH_TOOLBAR_FREEZE.getKeyStroke(), freezeAction,
        "Freeze view");

    final CActionCircularLayout circularAction = new CActionCircularLayout(parent, graph);
    final JButton circularButton =
        addButton(circularAction, "data/laycirc_hover.jpg", "data/laycirc_down.jpg");
    addHotkey(circularButton, HotKeys.GRAPH_TOOLBAR_CIRCULAR_LAYOUT.getKeyStroke(), circularAction,
        "Circular Layout");

    final CActionOrthogonalLayout orthogonalAction = new CActionOrthogonalLayout(parent, graph);
    final JButton orthogonalButton =
        addButton(orthogonalAction, "data/layorth_hover.jpg", "data/layorth_down.jpg");
    addHotkey(orthogonalButton, HotKeys.GRAPH_TOOLBAR_ORTHOGONAL_LAYOUT.getKeyStroke(),
        orthogonalAction, "Orthogonal Layout");

    final CActionHierarchicLayout hierarchicAction = new CActionHierarchicLayout(parent, graph);
    final JButton hierarchicButton =
        addButton(hierarchicAction, "data/layhier_hover.jpg", "data/layhier_down.jpg");
    addHotkey(hierarchicButton, HotKeys.GRAPH_TOOLBAR_HIERARCHIC_LAYOUT.getKeyStroke(),
        hierarchicAction, "Hierarchical Layout");

    addButton(new CActionSelectChildren(graph, true), "data/selallchild_hover.jpg",
        "data/selallchild_down.jpg");
    addButton(new CActionSelectParents(graph, true), "data/selparent_hover.jpg",
        "data/selparent_down.jpg");
    addButton(new CActionInvertSelection(graph, true), "data/selinvert_hover.jpg",
        "data/selinvert_down.jpg");
    addButton(new CActionSelectByCriteria(graphPanel, true), "data/selcriteria_hover.jpg",
        "data/selcriteria_down.jpg");

    final CActionDeleteSelectedNodes deleteSelectedAction =
        new CActionDeleteSelectedNodes(graph, true);
    final JButton deletedSelectedButton =
        addButton(deleteSelectedAction, "data/deleteselectednodes_hover.png",
            "data/deleteselectednodes_down.png");
    addHotkey(deletedSelectedButton, HotKeys.GRAPH_TOOLBAR_DELETE_SELECTED.getKeyStroke(),
        deleteSelectedAction, HotKeys.GRAPH_TOOLBAR_DELETE_SELECTED.getDescription());

    final CActionDeleteSelectedNodesKeep deleteSelectedKeepAction =
        new CActionDeleteSelectedNodesKeep(graph);
    final JButton deletedSelectedKeepButton =
        addButton(deleteSelectedKeepAction, "data/deleteselectednodeskeepedges_hover.png",
            "data/deleteselectednodeskeepedges_down.png");
    addHotkey(deletedSelectedKeepButton,
        HotKeys.GRAPH_TOOLBAR_DELETE_SELECTED_KEEP_EDGES.getKeyStroke(), deleteSelectedKeepAction,
        HotKeys.GRAPH_TOOLBAR_DELETE_SELECTED_KEEP_EDGES.getDescription());

    addButton(new CActionColorNodes(graphPanel), "data/nodecolor_hover.jpg",
        "data/nodecolor_down.jpg");
  }

  /**
   * Frees allocated resources.
   */
  public void dispose() {
    m_settings.getDisplaySettings().removeListener(m_internalListener);
    m_navigationPanel.dispose();
  }

  /**
   * Returns the address field that can be used to jump to addresses in graphs.
   * 
   * @return The address field.
   */
  public CGotoAddressField getGotoAddressField() {
    return m_navigationPanel.getGotoAddressField();
  }

  /**
   * Returns the search panel that can be used to search for texts in graph.
   * 
   * @return The search panel.
   */
  public CGraphSearchPanel getSearchPanel() {
    return m_navigationPanel.getSearchPanel();
  }

  /**
   * Listener class that updates the behavior of the search field to reflect changes in the view
   * settings.
   */
  private class InternalListener implements IDisplaySettingsListener {
    @Override
    public void changedMagnifyingGlass(final boolean value) {
      m_magnifierModeAction.updateButton();
    }
  }
}
