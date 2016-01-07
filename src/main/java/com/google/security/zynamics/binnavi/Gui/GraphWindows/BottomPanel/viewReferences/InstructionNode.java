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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.viewReferences;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.types.TypeSystemIcons;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.ZyInstructionBuilder;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.CDefaultModifier;
import com.google.security.zynamics.binnavi.config.GraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.gui.jtree.IconNode;

import javax.swing.Icon;

/**
 * Represents a single instruction node that references a local or global variable. This node is
 * part of a Jtree used in the bottom panel of the graph view.
 */
public class InstructionNode extends IconNode {
  /**
   * The instruction represented by the node.
   */
  private final INaviInstruction instruction;

  /**
   * Creates a new instruction node object.
   *
   * @param instruction The instruction represented by the node.
   * @param isLocal A Flag describing which kind of variable this node references.
   */
  public InstructionNode(final INaviInstruction instruction, final boolean isLocal) {
    this.instruction =
        Preconditions.checkNotNull(instruction, "Error: instruction argument can not be null.");
    setIcon(determineIcon(isLocal));
  }

  private Icon determineIcon(final boolean isLocal) {
    return isLocal ? TypeSystemIcons.LOCAL_VARIABLE_ICON : TypeSystemIcons.GLOBAL_VARIABLE_ICON;
  }

  /**
   * Returns the instruction represented by the node.
   *
   * @return The instruction represented by the node.
   */
  public INaviInstruction getInstruction() {
    return instruction;
  }

  @Override
  public String toString() {
    final ZyGraphViewSettings settings = new ZyGraphViewSettings(new GraphSettingsConfigItem());
    final DebuggerProvider provider =
        new DebuggerProvider(new ModuleTargetSettings(instruction.getModule()));
    return ZyInstructionBuilder.buildInstructionLine(
        instruction, settings, new CDefaultModifier(settings, provider)).first();
  }
}
