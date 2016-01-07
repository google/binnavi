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

import javax.swing.JMenu;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopyAddressAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopyLineAction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CCopyNodeAction;
import com.google.security.zynamics.binnavi.disassembly.CCodeNodeHelpers;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;


/**
 * Contains code the clipboard part of a code node menu.
 */
public final class CClipboardMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2169249273391457437L;

  /**
   * Creates a new clipboard menu.
   *
   * @param node The clicked node.
   * @param line Index of the clicked line.
   */
  public CClipboardMenu(final NaviNode node, final int line) {
    super("Clipboard");

    final INaviCodeNode cnode = (INaviCodeNode) node.getRawNode();

    if (line != -1) {
      final INaviInstruction instruction = CCodeNodeHelpers.lineToInstruction(cnode, line);

      if (instruction != null) {
        add(CActionProxy.proxy(new CCopyAddressAction(instruction)));
      }

      add(CActionProxy.proxy(new CCopyLineAction(node, line)));
    }

    add(CActionProxy.proxy(new CCopyNodeAction(node)));
  }
}
