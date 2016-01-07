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
package com.google.security.zynamics.binnavi.API.reil;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Instruction;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.REIL.InstructionFinders;
import com.google.security.zynamics.binnavi.REIL.ReilGraphConverter;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.reil.translators.ITranslationExtension;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.arm.TranslatorARM;
import com.google.security.zynamics.reil.translators.ppc.TranslatorPPC;
import com.google.security.zynamics.reil.translators.reil.TranslatorREIL;
import com.google.security.zynamics.reil.translators.x86.TranslatorX86;

// / Translates native code to REIL code
/**
 * Translator class that can be used to translate native assembler code into REIL code.
 */
public final class ReilTranslator {
  /**
   * The wrapped internal REIL translator.
   */
  private static final
      com.google.security.zynamics.reil.translators.ReilTranslator<INaviInstruction> m_translator =
          new com.google.security.zynamics.reil.translators.ReilTranslator<INaviInstruction>();

  /**
   * You are not supposed to instantiate this class.
   */
  private ReilTranslator() {}

  /**
   * Creates new architecture translators.
   *
   * @param architecture Architecture for which the REIL translator is created.
   *
   * @return The created REIL translator.
   */
  private static ITranslator<INaviInstruction> getTranslator(
      final NativeArchitecture architecture) {
    switch (architecture) {
      case X86_32:
        return new TranslatorX86<INaviInstruction>();
      case PPC_32:
        return new TranslatorPPC<INaviInstruction>();
      case ARM_32:
        return new TranslatorARM<INaviInstruction>();
      case REIL:
        return new TranslatorREIL<INaviInstruction>();
      default:
        throw new IllegalArgumentException("Error: Unknown REIL architecture");
    }
  }

  // ! Translates a basic block to REIL code.
  /**
   * Translates a single native code block to REIL code.
   *
   * @param block The input block.
   *
   * @return The REIL code of the input block.
   *
   * @throws InternalTranslationException Thrown if something goes wrong during translation.
   */
  public static ReilGraph translate(final BasicBlock block) throws InternalTranslationException {
    Preconditions.checkNotNull(block, "Error: Block argument can't be null");

    try {
      return ReilGraphConverter.createReilGraph(
          m_translator.translate(new StandardEnvironment(), block.getNative()));
    } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
      throw new InternalTranslationException(e,
          InstructionFinders.findInstruction(block, e.getInstruction()));
    }
  }

  // ! Translates a function to REIL code.
  /**
   * Translates a complete function to REIL code.
   *
   * @param function The function to translate.
   *
   * @return The translated REIL function object.
   *
   * @throws InternalTranslationException Thrown if something goes wrong during translation.
   */
  public static ReilFunction translate(final Function function)
      throws InternalTranslationException {
    Preconditions.checkNotNull(function, "Error: Function argument can't be null");

    try {
      return new ReilFunction(
          m_translator.translate(new StandardEnvironment(), function.getNative()));
    } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
      throw new InternalTranslationException(e,
          InstructionFinders.findInstruction(function, e.getInstruction()));
    }
  }

  // ! Translates a view to REIL code.
  /**
   * Translates a complete view to REIL code.
   *
   * @param view The view to translate.
   *
   * @return The translated REIL function object.
   *
   * @throws InternalTranslationException Thrown if something goes wrong during translation.
   */
  public static ReilFunction translate(final View view) throws InternalTranslationException {
    Preconditions.checkNotNull(view, "Error: View argument can't be null");

    try {
      return new ReilFunction(m_translator.translate(new StandardEnvironment(), view.getNative()));
    } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
      throw new InternalTranslationException(e,
          InstructionFinders.findInstruction(view, e.getInstruction()));
    }
  }

  // ! Translates an instruction to REIL code.
  /**
   * Translates a single instruction to REIL code.
   *
   * @param architecture The source architecture of the instruction.
   * @param instruction The instruction to translate.
   *
   * @return The generated REIL code for that instruction.
   *
   * @throws InternalTranslationException Thrown if something goes wrong during translation.
   */
  public static List<ReilInstruction> translateInstruction(final NativeArchitecture architecture,
      final Instruction instruction) throws InternalTranslationException {
    Preconditions.checkNotNull(architecture, "Error: Architecture argument can not be null");

    Preconditions.checkNotNull(instruction, "Error: Instruction argument can't be null");

    final List<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

    final StandardEnvironment environment = new StandardEnvironment();

    try {
      for (final com.google.security.zynamics.reil.ReilInstruction reilInstruction : getTranslator(
          architecture).translate(environment, instruction.getNative(),
          new ArrayList<ITranslationExtension<INaviInstruction>>())) {
        instructions.add(new ReilInstruction(reilInstruction));
      }

      return instructions;
    } catch (final com.google.security.zynamics.reil.translators.InternalTranslationException e) {
      throw new InternalTranslationException(e, instruction);
    }
  }
}
