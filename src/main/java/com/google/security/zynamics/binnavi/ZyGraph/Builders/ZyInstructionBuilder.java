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
package com.google.security.zynamics.binnavi.ZyGraph.Builders;

import com.google.common.base.Strings;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.ZyGraph.Builders.Modifiers.INodeModifier;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.zygraph.realizers.CStyleRunData;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains methods for building the instruction lines of code nodes.
 */
public final class ZyInstructionBuilder {
  /**
   * Padding between the instruction address and the mnemonic.
   */
  private static final String PADDING_AFTER_ADDRESS = "   ";

  /**
   * Number of characters between the mnemonic and the first operand.
   */
  private static final int MINIMUM_MNEMONIC_SIZE = 12;

  /**
   * You are not supposed to instantiate this class.
   */
  private ZyInstructionBuilder() {}

  /**
   * Builds the address of an instruction.
   *
   * @param instruction The instruction in question.
   * @param line String buffer where the address string is added.
   * @param styleRun Style runs list where the formatting information is added.
   * @param modifier Calculates the address string (this argument can be null).
   */
  private static void buildAddress(final INaviInstruction instruction, final StringBuffer line,
      final List<CStyleRunData> styleRun, final INodeModifier modifier) {
    final String normalAddress = instruction.getAddress().toHexString();

    final String address = modifier == null ? normalAddress : modifier.getAddress(instruction);

    line.append(address);

    if (address.equals(normalAddress)) {
      styleRun.add(new CStyleRunData(
          0, address.length(), ConfigManager.instance().getColorSettings().getAddressColor()));
    } else {
      styleRun.add(new CStyleRunData(0, address.length(), Color.RED));
    }

    line.append(PADDING_AFTER_ADDRESS);
  }

  /**
   * Builds the mnemonic of an instruction.
   *
   * @param instruction The instruction in question.
   * @param line String buffer where the mnemonic string is added.
   * @param styleRun Style runs list where the formatting information is added.
   */
  private static void buildMnemonic(
      final IInstruction instruction, final StringBuffer line, final List<CStyleRunData> styleRun) {
    final String mnemonic = instruction.getMnemonic();
    styleRun.add(new CStyleRunData(line.length(), mnemonic.length(),
        ConfigManager.instance().getColorSettings().getMnemonicColor()));
    line.append(Strings.padEnd(mnemonic, MINIMUM_MNEMONIC_SIZE, ' '));
  }

  /**
   * Builds the node content of a single instruction line.
   *
   * @param instruction The instruction that provides the raw data.
   * @param graphSettings Provides settings that influence node formatting.
   * @param modifier Calculates the address strings. This argument can be null.
   *
   * @return The line content of the instruction.
   */
  public static Pair<String, List<CStyleRunData>> buildInstructionLine(
      final INaviInstruction instruction, final ZyGraphViewSettings graphSettings,
      final INodeModifier modifier) {
    // Formatting information for the individual parts of the instruction
    // line is first collected and then applied at the end of the function.

    final List<CStyleRunData> styleRun = new ArrayList<CStyleRunData>();

    final StringBuffer line = new StringBuffer();

    // First part: The address of the instruction.
    buildAddress(instruction, line, styleRun, modifier);

    // Second part: The mnemonic
    buildMnemonic(instruction, line, styleRun);

    // Third part: The operands of the instruction
    ZyOperandBuilder.buildOperands(instruction, graphSettings, line, styleRun, modifier);

    return new Pair<String, List<CStyleRunData>>(line.toString(), styleRun);
  }
}
