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

import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.REIL.COperandsDeterminer;
import com.google.security.zynamics.binnavi.REIL.CTranslatorFactory;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.general.Pair;

import java.util.List;
import java.util.Set;

import javax.swing.JMenu;

/**
 * Contains the operands part of a code node menu.
 */
public final class COperandsMenu extends JMenu {
  /**
   * Creates the operands menu for the code node menu.
   *
   * @param node The code node that was clicked.
   * @param instruction The instruction that was clicked.
   * @param extensions The list of code node extensions that extend the menu.
   *
   * @throws InternalTranslationException Thrown in case the instruction could not be translated to
   *         REIL.
   * @throws MaybeNullException
   */
  public COperandsMenu(final INaviCodeNode node, final INaviInstruction instruction,
      final List<ICodeNodeExtension> extensions) throws InternalTranslationException,
      MaybeNullException {
    super("Operands");

    final ITranslator<INaviInstruction> translator =
        CTranslatorFactory.getTranslator(instruction.getArchitecture());

    if (translator == null) {
      throw new MaybeNullException();
    }

    final Pair<Set<String>, Set<String>> operands = COperandsDeterminer.getRegisters(instruction);

    final Set<String> inSet = operands.first();
    final Set<String> outSet = operands.second();

    final JMenu inRegisters = new JMenu("Incoming Registers");
    final JMenu outRegisters = new JMenu("Outgoing Registers");

    if (!inSet.isEmpty()) {
      add(inRegisters);
    }

    if (!outSet.isEmpty()) {
      add(outRegisters);
    }

    for (final String register : inSet) {
      final JMenu operandMenu = new JMenu(register);

      for (final ICodeNodeExtension extension : extensions) {
        extension.extendIncomingRegistersMenu(operandMenu, node, instruction, register);
      }

      inRegisters.add(operandMenu);
    }

    for (final String register : outSet) {
      final JMenu operandMenu = new JMenu(register);

      for (final ICodeNodeExtension extension : extensions) {
        extension.extendOutgoingRegistersMenu(operandMenu, node, instruction, register);
      }

      outRegisters.add(operandMenu);
    }
  }

}
