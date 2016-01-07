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


public final class ConditionGenerator {
  private ConditionGenerator() {
  }

  public static void generate(final long offset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String extension, final String jumpGoal) throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");
    Preconditions.checkNotNull(instructions, "Error: Argument instructions can't be null");

    /*
     * 2.5.2 Execution conditions The relation of condition code suffixes to the N, Z, C and V flags
     * is shown in Table 2-1. Table 2-1 Condition code suffixes Suffix Flags Meaning EQ Z set Equal
     * NE Z clear Not equal CS/HS C set Higher or same (unsigned >= ) CC/LO C clear Lower (unsigned
     * < ) MI N set Negative PL N clear Positive or zero VS V set Overflow VC V clear No overflow HI
     * C set and Z clear Higher (unsigned > ) LS C clear or Z set Lower or same (unsigned <= ) GE N
     * and V the same Signed >= LT N and V differ Signed < GT Z clear, N and V the same Signed > LE
     * Z set, N and V differ Signed <= AL Any Always. This suffix is normally omitted.
     */

    final OperandSize bt = OperandSize.BYTE;
    // final OperandSize dw = OperandSize.DWORD;

    final String tmpVar1 = environment.getNextVariableString();
    final String tmpVar2 = environment.getNextVariableString();
    final String tmpVar3 = environment.getNextVariableString();

    final String jumpCondition = environment.getNextVariableString();

    long baseOffset = offset;

    final String[] meta = new String[0];

    if (extension.compareTo("EQ") == 0) {
      /*
       * z set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "Z", bt, jumpCondition));
    } else if (extension.compareTo("NE") == 0) {
      /*
       * z not set
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, "Z", bt, jumpCondition));
    } else if ((extension.compareTo("CS") == 0) || (extension.compareTo("HS") == 0)) {
      /*
       * c set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "C", bt, jumpCondition));
    } else if ((extension.compareTo("CC") == 0) || (extension.compareTo("LO") == 0)) {
      /*
       * c not set
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, "C", bt, jumpCondition));
    } else if (extension.compareTo("MI") == 0) {
      /*
       * n set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "N", bt, jumpCondition));
    } else if (extension.compareTo("PL") == 0) {
      /*
       * n not set
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, "N", bt, jumpCondition));
    } else if (extension.compareTo("VS") == 0) {
      /*
       * v set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "V", bt, jumpCondition));
    } else if (extension.compareTo("VC") == 0) {
      /*
       * v not set
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, "V", bt, jumpCondition));
    } else if (extension.compareTo("HI") == 0) {
      /*
       * c set and z not set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "C", bt, tmpVar1));
      instructions
          .add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar1, bt, "Z", bt, jumpCondition));
    } else if (extension.compareTo("LS") == 0) {
      /*
       * c not set and z set
       */
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "Z", bt, tmpVar1));
      instructions
          .add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar1, bt, "C", bt, jumpCondition));
    } else if (extension.compareTo("GE") == 0) {
      /*
       * n equal v
       */
      instructions.add(ReilHelpers.createXor(baseOffset++, bt, "N", bt, "V", bt, tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar1, bt, jumpCondition));

    } else if (extension.compareTo("LT") == 0) {
      /*
       * n is not equal v
       */
      instructions.add(ReilHelpers.createXor(baseOffset++, bt, "N", bt, "V", bt, tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar1, bt, jumpCondition));
    } else if (extension.compareTo("GT") == 0) {
      /*
       * z clear and n equals v
       */
      instructions.add(ReilHelpers.createXor(baseOffset++, bt, "N", bt, "V", bt, tmpVar1));
      instructions
          .add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar1, bt, "Z", bt, jumpCondition));
    } else if (extension.compareTo("LE") == 0) {
      /*
       * z set and n is not equal v
       */
      instructions.add(ReilHelpers.createXor(baseOffset++, bt, "N", bt, "V", bt, tmpVar1));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, tmpVar1, bt, tmpVar3));
      instructions.add(ReilHelpers.createBisz(baseOffset++, bt, "Z", bt, tmpVar2));
      instructions.add(ReilHelpers.createAnd(baseOffset++, bt, tmpVar3, bt, tmpVar2, bt,
          jumpCondition));
    } else if (extension.compareTo("AL") == 0) {
      /*
       * any
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(0L), bt,
          jumpCondition));
    } else if (extension.compareTo("NV") == 0) {
      /*
       * none
       */
      instructions.add(ReilHelpers.createStr(baseOffset++, bt, String.valueOf(1L), bt,
          jumpCondition));
    } else {
      throw new InternalTranslationException("ERROR: unknown condition " + extension);
    }
    instructions.add(ReilHelpers.createJcc(baseOffset++, bt, jumpCondition, OperandSize.ADDRESS,
        jumpGoal, meta));
  }
}
