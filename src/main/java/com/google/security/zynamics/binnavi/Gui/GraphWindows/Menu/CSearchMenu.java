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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.Menu;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSearchCaseSensitive;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSearchOnlySelectedNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSearchOnlyVisibleNodes;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Actions.CActionSearchRegEx;
import com.google.security.zynamics.binnavi.ZyGraph.Settings.ZyGraphSearchSettings;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;


/**
 * Code for the search menu of the graph window menu bar.
 */
public final class CSearchMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -5545587894059763275L;

  /**
   * Creates the Search menu.
   *
   * @param graph The graph to search through.
   */
  public CSearchMenu(final ZyGraph graph) {
    super("Search");

    final ZyGraphSearchSettings settings = graph.getSettings().getSearchSettings();

    setMnemonic("HK_MENU_SEARCH".charAt(0));

    final JCheckBoxMenuItem searchVisibleMenu =
        new JCheckBoxMenuItem(new CActionSearchOnlyVisibleNodes(graph));
    searchVisibleMenu.setSelected(settings.getSearchVisibleNodesOnly());
    add(searchVisibleMenu);

    final JCheckBoxMenuItem searchSelectedOnly =
        new JCheckBoxMenuItem(new CActionSearchOnlySelectedNodes(graph));
    searchSelectedOnly.setSelected(settings.getSearchSelectedNodesOnly());
    add(searchSelectedOnly);

    final JCheckBoxMenuItem searchCaseSensitiveMenu =
        new JCheckBoxMenuItem(new CActionSearchCaseSensitive(graph));
    searchCaseSensitiveMenu.setSelected(settings.getSearchCaseSensitive());
    add(searchCaseSensitiveMenu);

    final JCheckBoxMenuItem searchRegexMenu = new JCheckBoxMenuItem(new CActionSearchRegEx(graph));
    searchRegexMenu.setSelected(settings.getSearchRegEx());
    add(searchRegexMenu);
  }
}
