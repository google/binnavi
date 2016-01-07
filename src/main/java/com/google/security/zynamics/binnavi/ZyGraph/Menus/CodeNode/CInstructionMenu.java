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

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Gui.Actions.CActionProxy;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Extensions.ICodeNodeExtension;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Implementations.CGraphDebugger;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyInstructionBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionDeleteInstruction;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionShowReilCode;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionSplitAfter;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionToggleBreakpoint;
import com.google.security.zynamics.binnavi.ZyGraph.Menus.Actions.CActionToggleBreakpointStatus;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.reil.translators.InternalTranslationException;

import java.util.List;

import javax.swing.JMenu;

/**
 * Contains code for the instruction part of a code node menu.
 */
public final class CInstructionMenu extends JMenu {
  /**
   * Used for serialization.
   */
  private static final long serialVersionUID = 1467406495056009235L;

  /**
   * Adds the instruction menu for the clicked instruction.
   *
   * @param model The graph model that provides information about the graph.
   * @param node The node whose menu is created.
   * @param instruction The instruction that was clicked on.
   * @param extensions The list of code node extensions that extend the menu.
   */
  public CInstructionMenu(final CGraphModel model, final NaviNode node,
      final INaviInstruction instruction, final List<ICodeNodeExtension> extensions) {
    super("Instruction" + " " + ZyInstructionBuilder.buildInstructionLine(instruction,
        model.getGraph().getSettings(),
        new CDefaultModifier(model.getGraph().getSettings(), model.getDebuggerProvider())).first());

    final INaviCodeNode codeNode = (INaviCodeNode) node.getRawNode();

    final IDebugger debugger = CGraphDebugger.getDebugger(model.getDebuggerProvider(), instruction);

    if (debugger != null) {
      final INaviModule module = instruction.getModule();

      final UnrelocatedAddress instructionAddress =
          new UnrelocatedAddress(instruction.getAddress());

      add(CActionProxy.proxy(new CActionToggleBreakpoint(
          debugger.getBreakpointManager(), module, instructionAddress)));

      final BreakpointAddress relocatedAddress =
          new BreakpointAddress(module, instructionAddress);

      if (debugger.getBreakpointManager().hasBreakpoint(BreakpointType.REGULAR, relocatedAddress)) {
        add(CActionProxy.proxy(new CActionToggleBreakpointStatus(
            debugger.getBreakpointManager(), module, instructionAddress)));
      }

      addSeparator();
    }

    for (final ICodeNodeExtension extension : extensions) {
      extension.extendInstruction(this, codeNode, instruction);
    }

    try {
      try {
        final JMenu operandsMenu = new COperandsMenu(codeNode, instruction, extensions);

        if (operandsMenu.getItemCount() != 0) {
          add(operandsMenu);
        }
      } catch (final MaybeNullException exception) {
        // No operands menu to add
      }
    } catch (final InternalTranslationException e) {
      CUtilityFunctions.logException(e);
    }

    addSeparator();

    final JMenu advancedMenu = new JMenu("Advanced");

    advancedMenu.add(CActionProxy.proxy(
        new CActionDeleteInstruction(model.getParent(), model.getGraph(), node, instruction)));
    advancedMenu.add(CActionProxy.proxy(
        new CActionSplitAfter(model.getGraph().getRawView(), codeNode, instruction)));
    advancedMenu.add(CActionProxy.proxy(new CActionShowReilCode(model.getParent(), instruction)));

    add(advancedMenu);
  }
}
