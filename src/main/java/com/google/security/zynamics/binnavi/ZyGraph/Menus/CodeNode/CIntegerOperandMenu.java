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

import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CDisplayOperandAction;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviReplacement;
import com.google.security.zynamics.binnavi.disassembly.OperandDisplayStyle;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

/**
 * Contains the integer operand part of a code node menu.
 */
public final class CIntegerOperandMenu extends JMenu {
  /**
   * Adds a menu for selecting the way integer literal operands are displayed.
   *
   * @param treeNode The tree node that contains the integer literal operand.
   * @param replacement The replacement object for the node.
   */
  public CIntegerOperandMenu(final COperandTreeNode treeNode, final INaviReplacement replacement) {
    super("Operand Type");

    final JCheckBoxMenuItem unsignedDecimalMenu = new JCheckBoxMenuItem(new CDisplayOperandAction(
        treeNode, OperandDisplayStyle.UNSIGNED_DECIMAL, "Unsigned Decimal"));
    final JCheckBoxMenuItem signedDecimalMenu = new JCheckBoxMenuItem(
        new CDisplayOperandAction(treeNode, OperandDisplayStyle.SIGNED_DECIMAL, "Signed Decimal"));

    final JCheckBoxMenuItem unsignedHexadecimalMenu =
        new JCheckBoxMenuItem(new CDisplayOperandAction(treeNode,
            OperandDisplayStyle.UNSIGNED_HEXADECIMAL, "Unsigned Hexadecimal"));
    final JCheckBoxMenuItem signedHexadecimalMenu = new JCheckBoxMenuItem(new CDisplayOperandAction(
        treeNode, OperandDisplayStyle.SIGNED_HEXADECIMAL, "Signed Hexadecimal"));

    final JCheckBoxMenuItem unsignedOctalMenu = new JCheckBoxMenuItem(
        new CDisplayOperandAction(treeNode, OperandDisplayStyle.OCTAL, "Octal"));

    final JCheckBoxMenuItem unsignedBinaryMenu = new JCheckBoxMenuItem(
        new CDisplayOperandAction(treeNode, OperandDisplayStyle.BINARY, "Binary"));

    final JCheckBoxMenuItem charMenu = new JCheckBoxMenuItem(
        new CDisplayOperandAction(treeNode, OperandDisplayStyle.CHAR, "Char"));
    final JCheckBoxMenuItem offsetMenu = new JCheckBoxMenuItem(
        new CDisplayOperandAction(treeNode, OperandDisplayStyle.OFFSET, "Offset"));

    add(unsignedHexadecimalMenu);
    add(signedHexadecimalMenu);

    add(unsignedDecimalMenu);
    add(signedDecimalMenu);

    add(unsignedOctalMenu);

    add(unsignedBinaryMenu);

    add(charMenu);

    if (replacement != null) {
      add(offsetMenu);
    }

    switch (treeNode.getDisplayStyle()) {

      case UNSIGNED_HEXADECIMAL:
        unsignedHexadecimalMenu.setSelected(true);
        break;
      case SIGNED_HEXADECIMAL:
        signedHexadecimalMenu.setSelected(true);
        break;
      case UNSIGNED_DECIMAL:
        unsignedDecimalMenu.setSelected(true);
        break;
      case SIGNED_DECIMAL:
        signedDecimalMenu.setSelected(true);
        break;
      case OCTAL:
        unsignedOctalMenu.setSelected(true);
        break;
      case BINARY:
        unsignedBinaryMenu.setSelected(true);
        break;
      case CHAR:
        charMenu.setSelected(true);
        break;
      case OFFSET:
        offsetMenu.setSelected(true);
        break;
      default:
        throw new IllegalStateException("IE02248: Unknown selection");
    }
  }
}
