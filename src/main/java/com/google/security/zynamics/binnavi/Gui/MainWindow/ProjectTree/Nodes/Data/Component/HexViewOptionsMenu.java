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
package com.google.security.zynamics.binnavi.Gui.MainWindow.ProjectTree.Nodes.Data.Component;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CBigEndiannessAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CLittleEndiannessAction;
import com.google.security.zynamics.binnavi.Gui.Debug.MemoryPanel.Actions.CSelectGroupingAction;
import com.google.security.zynamics.zylib.gui.JHexPanel.JHexView;

public class HexViewOptionsMenu {

  public static JMenu createHexViewOptionsMenu(final JHexView hexView) {
    final JMenu menu = new JMenu("Options");

    final JMenu groupingMenu = new JMenu("Byte Grouping");

    final ButtonGroup group = new ButtonGroup();

    final JRadioButtonMenuItem groupingByteMenu =
        new JRadioButtonMenuItem(CActionProxy.proxy(new CSelectGroupingAction(hexView, "Byte", 1)));
    group.add(groupingByteMenu);

    final JRadioButtonMenuItem groupingWordMenu =
        new JRadioButtonMenuItem(CActionProxy.proxy(new CSelectGroupingAction(hexView, "Word", 2)));
    group.add(groupingWordMenu);

    final JRadioButtonMenuItem groupingDwordMenu =
        new JRadioButtonMenuItem(CActionProxy.proxy(new CSelectGroupingAction(hexView,
            "Double Word", 4)));
    group.add(groupingDwordMenu);

    final int grouping = hexView.getBytesPerColumn();

    switch (grouping) {
      case 1:
        groupingByteMenu.setSelected(true);
        break;
      case 2:
        groupingWordMenu.setSelected(true);
        break;
      case 4:
        groupingDwordMenu.setSelected(true);
        break;
      default:
        throw new IllegalStateException("IE01123: Unknown grouping value: " + grouping);
    }

    groupingMenu.add(groupingByteMenu);
    groupingMenu.add(groupingWordMenu);
    groupingMenu.add(groupingDwordMenu);

    menu.add(groupingMenu);

    final JMenu endiannessMenu = new JMenu("Endianness");

    final ButtonGroup endiannessGroup = new ButtonGroup();

    final JRadioButtonMenuItem littleEndiannessMenu =
        new JRadioButtonMenuItem(CActionProxy.proxy(new CLittleEndiannessAction(hexView)));
    endiannessGroup.add(littleEndiannessMenu);

    final JRadioButtonMenuItem bigEndiannessMenu =
        new JRadioButtonMenuItem(CActionProxy.proxy(new CBigEndiannessAction(hexView)));
    endiannessGroup.add(bigEndiannessMenu);

    final boolean flip = hexView.doFlipBytes();

    if (flip) {
      littleEndiannessMenu.setSelected(true);
    } else {
      bigEndiannessMenu.setSelected(true);
    }

    endiannessMenu.add(bigEndiannessMenu);
    endiannessMenu.add(littleEndiannessMenu);

    menu.add(endiannessMenu);
    return menu;
  }
}
