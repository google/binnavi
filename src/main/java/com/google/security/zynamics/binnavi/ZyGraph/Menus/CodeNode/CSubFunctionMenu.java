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
package com.google.security.zynamics.binnavi.ZyGraph.Menus.CodeNode;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.COpenFunctionAction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;



/**
 * Contains the subfunction part of a code node menu.
 */
public final class CSubFunctionMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 3893106540270669287L;

  /**
   * Adds menus for opening subfunctions.
   *
   * @param model The graph model that provides information about the graph.
   * @param functions The functions called from the clicked node.
   * @param allowUninlining True, if uninlining should be allowed.
   */
  public CSubFunctionMenu(final CGraphModel model,
      final List<Pair<INaviInstruction, INaviFunction>> functions, final boolean allowUninlining) {
    super("Open Subfunction");

    final Set<INaviFunction> added = new HashSet<INaviFunction>();

    for (final Pair<INaviInstruction, INaviFunction> p : functions) {
      if (added.contains(p.second())) {
        continue;
      }

      added.add(p.second());

      add(new JMenuItem(CActionProxy.proxy(
          new COpenFunctionAction(model.getParent(), model.getViewContainer(), p.second()))));
    }

    setEnabled(allowUninlining || !functions.isEmpty());
  }
}
