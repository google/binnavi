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
package com.google.security.zynamics.binnavi.Gui.ReilInstructionDialog;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.ZyGraph.Implementations.CNodeFunctions;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilGraph;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Dialog used to show the REIL code of a single instruction.
 */
public final class CReilInstructionDialog extends JDialog {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = -2412060565212653357L;

  /**
   * Text area where the REIL code is shown.
   */
  private final JTextArea m_textArea;

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window used for dialogs.
   * @param title Title of the dialog.
   * @param text Text to show in the dialog.
   *
   * @throws InternalTranslationException Thrown if the instruction could not be converted to REIL
   *         code.
   */
  private CReilInstructionDialog(final Window parent, final String title, final String text)
      throws InternalTranslationException {
    super(parent, title);

    m_textArea = new JTextArea(text);
    m_textArea.setEditable(false);
    m_textArea.setFont(GuiHelper.MONOSPACED_FONT);
    m_textArea.addMouseListener(new PopupListener());

    addMenuBar();

    setLayout(new BorderLayout());

    add(new JScrollPane(m_textArea));

    setSize(500, 300);
  }

  /**
   * Converts a given REIL graph to its text representation.
   *
   * @param graph The REIL graph
   * @return The text representation of the REIL graph
   */
  private static String reilGraphToText(final ReilGraph graph) {
    final List<ReilInstruction> reilInstructions = new ArrayList<ReilInstruction>();

    for (final ReilBlock reilBlock : graph.getNodes()) {
      reilInstructions.addAll(Lists.newArrayList(reilBlock.getInstructions()));
    }

    Collections.sort(reilInstructions, new Comparator<ReilInstruction>() {
      @Override
      public int compare(final ReilInstruction lhs, final ReilInstruction rhs) {
        return (int) (lhs.getAddress().toLong() - rhs.getAddress().toLong());
      }
    });

    final StringBuilder buffer = new StringBuilder();

    for (final ReilInstruction reilInstruction : reilInstructions) {
      buffer.append(reilInstruction.toString());
      buffer.append('\n');
    }

    return buffer.toString();
  }

  /**
   * Shows the REIL instruction dialog.
   *
   * @param parent Parent window used for dialogs.
   * @param node The node whose REIL code is shown.
   *
   * @throws InternalTranslationException Thrown if the instruction could not be converted to REIL
   *         code.
   */
  public static void show(final Window parent, final INaviCodeNode node)
      throws InternalTranslationException {
    final ReilGraph graph = CNodeFunctions.copyReilCode(parent, node);
    final String title = String.format("REIL code of %s", node.getAddress().toHexString());

    final String text = reilGraphToText(graph);
    final CReilInstructionDialog dialog = new CReilInstructionDialog(parent, title, text);

    GuiHelper.centerChildToParent(parent, dialog, true);

    dialog.setVisible(true);
  }

  /**
   * Shows an instruction dialog.
   *
   * @param parent Parent window used for dialogs.
   * @param instruction The instruction whose REIL code is shown.
   *
   * @throws InternalTranslationException Thrown if the instruction could not be converted to REIL
   *         code.
   */
  public static void show(final Window parent, final INaviInstruction instruction)
      throws InternalTranslationException {
    final ReilTranslator<INaviInstruction> translator = new ReilTranslator<INaviInstruction>();
    final ReilGraph reilGraph = translator.translate(new StandardEnvironment(), instruction);
    final String text = reilGraphToText(reilGraph);
    final String title = String.format("REIL code of '%s'", instruction.toString());

    final CReilInstructionDialog dialog = new CReilInstructionDialog(parent, title, text);

    GuiHelper.centerChildToParent(parent, dialog, true);

    dialog.setVisible(true);
  }

  /**
   * Adds a menu bar to the dialog.
   */
  private void addMenuBar() {
    final JMenu menu = new JMenu("REIL Code");
    final JMenuItem copyItem =
        new JMenuItem(CActionProxy.proxy(new CActionCopyAllReilCode(m_textArea)));
    menu.add(copyItem);

    final JMenuBar menuBar = new JMenuBar();

    menuBar.add(menu);

    setJMenuBar(menuBar);
  }

  /**
   * Listener to show the popup menu in the JTextArea
   */
  private class PopupListener extends MouseAdapter {
    /**
     * Shows a context menu after the user has clicked on the text area.
     *
     * @param event The mouse event that triggered the popup menu.
     */
    private void maybeShowPopup(final MouseEvent event) {
      if (event.isPopupTrigger()) {
        new CReilInstructionDialogMenu(m_textArea).show(
            event.getComponent(), event.getX(), event.getY());
      }
    }

    @Override
    public void mousePressed(final MouseEvent event) {
      maybeShowPopup(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
      maybeShowPopup(event);
    }
  }
}
