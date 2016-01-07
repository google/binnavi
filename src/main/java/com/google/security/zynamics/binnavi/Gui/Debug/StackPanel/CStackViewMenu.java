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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel;

import java.awt.Point;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.Actions.CCopyStackValueAction;
import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.Actions.CGotoStackAction;
import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.Actions.CLayoutBytesAction;
import com.google.security.zynamics.binnavi.Gui.Debug.StackPanel.Actions.CLayoutDwordAction;
import com.google.security.zynamics.zylib.gui.JStackView.JStackView;

/**
 * Menu that is shown when the user right-clicks on the stack window.
 */
public final class CStackViewMenu extends JPopupMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -4966159689063234257L;

  /**
   * Creates a new stack menu.
   *
   * @param stackView Stack view that was clicked.
   * @param model Model of the stack view that was clicked.
   * @param point Location of the mouse click.
   */
  public CStackViewMenu(
      final JStackView stackView, final CStackMemoryProvider model, final Point point) {
    Preconditions.checkNotNull(stackView, "IE01504: Stack view argument can not be null");

    Preconditions.checkNotNull(model, "IE01505: Model argument can not be null");

    add(CActionProxy.proxy(new CGotoStackAction(stackView, model)));

    addSeparator();

    final String value = stackView.getValueAt(point);

    if (value != null) {
      add(CActionProxy.proxy(new CCopyStackValueAction(value)));
      addSeparator();
    }

    final JMenu layoutMenu = new JMenu("Hex");
    layoutMenu.add(CActionProxy.proxy(new CLayoutBytesAction(model)));
    layoutMenu.add(CActionProxy.proxy(new CLayoutDwordAction(model)));

    add(layoutMenu);
  }
}
