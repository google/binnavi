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
package com.google.security.zynamics.reil.translators.arm;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;


public final class AddressingModeFourGenerator {
  final static OperandSize wd = OperandSize.WORD;
  final static OperandSize dw = OperandSize.DWORD;

  private final static String matchLDM = "LDM";
  private final static String matchSTM = "STM";

  private AddressingModeFourGenerator() {

  }

  /**
   * Decrement after
   * 
   * DA ( LDMDA || LDMFA ) || ( STMDA || STMED )
   * 
   * start_address = Rn - (Number_Of_Set_Bits_In(register_list) * 4) + 4 end_address = Rn if
   * ConditionPassed(cond) and W == 1 then Rn = Rn - (Number_Of_Set_Bits_In(register_list) * 4)
   */
  private static String generateDA(final long offset, final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions, final String registerNodeValue, final String wBit,
      final IOperandTreeNode rootNodeOfRegisterList) {
    final String startAddress = environment.getNextVariableString();
    final String endAddress = environment.getNextVariableString();
    final String tmpStartAddress = environment.getNextVariableString();

    long baseOffset = offset;

    final Integer numberOfSetBits = rootNodeOfRegisterList.getChildren().size();

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, registerNodeValue, wd,
        String.valueOf((numberOfSetBits * 4)), dw, tmpStartAddress));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, tmpStartAddress, wd,
        String.valueOf(4), dw, startAddress));

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue, dw, endAddress));

    if (wBit.equals("2")) {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, tmpStartAddress, dw,
          registerNodeValue));
    }
    return startAddress;
  }

  /**
   * Decrement before
   * 
   * DB ( LDMDB || LDMEA ) || ( STMDB || STMFD ) start_address = Rn -
   * (Number_Of_Set_Bits_In(register_list) * 4) end_address = Rn - 4 if ConditionPassed(cond) and W
   * == 1 then Rn = Rn - (Number_Of_Set_Bits_In(register_list) * 4)
   */
  private static String generateDB(final long offset, final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions, final String registerNodeValue, final String wBit,
      final IOperandTreeNode rootNodeOfRegisterList) {
    final String startAddress = environment.getNextVariableString();

    final Integer numberOfSetBits = rootNodeOfRegisterList.getChildren().size();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createSub(baseOffset++, dw, registerNodeValue, wd,
        String.valueOf(numberOfSetBits * 4), dw, startAddress));

    if (wBit.equals("2")) {
      instructions
          .add(ReilHelpers.createStr(baseOffset++, dw, startAddress, dw, registerNodeValue));
    }
    return startAddress;
  }

  /**
   * Increment after
   * 
   * IA ( LDMIA || LDMFD ) || ( STMIA || STMEA )
   * 
   * start_address = Rn end_address = Rn + (Number_Of_Set_Bits_In(register_list) * 4) - 4 if
   * ConditionPassed(cond) and W == 1 then Rn = Rn + (Number_Of_Set_Bits_In(register_list) * 4)
   */
  private static String generateIA(final long offset, final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions, final String registerNodeValue, final String wBit,
      final IOperandTreeNode rootNodeOfRegisterList) {
    final String startAddress = environment.getNextVariableString();
    final String tmpRegisterVal = environment.getNextVariableString();

    final Integer numberOfSetBits = rootNodeOfRegisterList.getChildren().size();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createStr(baseOffset++, dw, registerNodeValue, dw, startAddress));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue, wd,
        String.valueOf(numberOfSetBits * 4), dw, tmpRegisterVal));

    if (wBit.equals("2")) {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, tmpRegisterVal, dw,
          registerNodeValue));
    }

    return startAddress;
  }

  /**
   * Increment before
   * 
   * IB ( LDMIB || LDMED ) || ( STMIB || STMFA )
   * 
   * start_address = Rn + 4 end_address = Rn + (Number_Of_Set_Bits_In(register_list) * 4) if
   * ConditionPassed(cond) and W == 1 then Rn = Rn + (Number_Of_Set_Bits_In(register_list) * 4)
   */
  private static String generateIB(final long offset, final ITranslationEnvironment environment,
      final List<ReilInstruction> instructions, final String registerNodeValue, final String wBit,
      final IOperandTreeNode rootNodeOfRegisterList) {
    final String startAddress = environment.getNextVariableString();
    final String endAddress = environment.getNextVariableString();

    final Integer numberOfSetBits = rootNodeOfRegisterList.getChildren().size();

    long baseOffset = offset;

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue, dw,
        String.valueOf(4), dw, startAddress));

    instructions.add(ReilHelpers.createAdd(baseOffset++, dw, registerNodeValue, dw,
        String.valueOf(numberOfSetBits * 4), dw, endAddress));

    if (wBit.equals("2")) {
      instructions.add(ReilHelpers.createStr(baseOffset++, dw, endAddress, dw, registerNodeValue));
    }
    return startAddress;
  }

  public static String generate(final long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String typeValue, final String registerNodeValue, final String wBit,
      final IOperandTreeNode rootNodeOfRegisterList) throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    /**
     * LDM|STM{<cond>}<addressing_mode> <Rn>{!}, <registers>{^}
     */

    if (typeValue.equals("DA")
        || (typeValue.equals("FA") && instruction.getMnemonic().startsWith(matchLDM))
        || (typeValue.equals("ED") && instruction.getMnemonic().startsWith(matchSTM))) {
      return generateDA(baseOffset, environment, instructions, registerNodeValue, wBit,
          rootNodeOfRegisterList);
    }

    else if (typeValue.equals("DB")
        || (typeValue.equals("EA") && instruction.getMnemonic().startsWith(matchLDM))
        || (typeValue.equals("FD") && instruction.getMnemonic().startsWith(matchSTM))) {
      return generateDB(baseOffset, environment, instructions, registerNodeValue, wBit,
          rootNodeOfRegisterList);
    }

    else if (typeValue.equals("IA")
        || (typeValue.equals("FD") && instruction.getMnemonic().startsWith(matchLDM))
        || (typeValue.equals("EA") && instruction.getMnemonic().startsWith(matchSTM))) {
      return generateIA(baseOffset, environment, instructions, registerNodeValue, wBit,
          rootNodeOfRegisterList);
    }

    else if (typeValue.equals("IB")
        || (typeValue.equals("ED") && instruction.getMnemonic().startsWith(matchLDM))
        || (typeValue.equals("FA") && instruction.getMnemonic().startsWith(matchSTM))) {
      return generateIB(baseOffset, environment, instructions, registerNodeValue, wBit,
          rootNodeOfRegisterList);
    }

    else {
      throw new InternalTranslationException("ERROR: there is no such AddressingMode :" + typeValue);
    }
  }
}
