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
package com.google.security.zynamics.reil.translators.ppc;

import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.IOperandTreeNode;

import java.util.List;


public class RlwinmGenerator {
  public static void generate(long baseOffset, final ITranslationEnvironment environment,
      final IInstruction instruction, final List<ReilInstruction> instructions,
      final String mnemonic, final boolean isSimplified, final boolean setCr)
      throws InternalTranslationException {
    TranslationHelpers.checkTranslationArguments(environment, instruction, instructions, mnemonic);

    final IOperandTreeNode registerOperand1 =
        instruction.getOperands().get(0).getRootNode().getChildren().get(0);
    final IOperandTreeNode registerOperand2 =
        instruction.getOperands().get(1).getRootNode().getChildren().get(0);
    final IOperandTreeNode integerOperand1 =
        instruction.getOperands().get(2).getRootNode().getChildren().get(0);
    final IOperandTreeNode integerOperand2 =
        (instruction.getOperands().size() >= 4) ? instruction.getOperands().get(3).getRootNode()
            .getChildren().get(0) : null;
    final IOperandTreeNode integerOperand3 =
        (instruction.getOperands().size() == 5) ? instruction.getOperands().get(4).getRootNode()
            .getChildren().get(0) : null;

    final String targetRegister = registerOperand1.getValue();
    final String sourceRegister = registerOperand2.getValue();

    String SH = "";
    String MB = "";
    String ME = "";

    final String crTemp = environment.getNextVariableString();

    final String rotateVar1 = environment.getNextVariableString();
    final String rotateVar2 = environment.getNextVariableString();
    final String rotateVar3 = environment.getNextVariableString();
    final String rotateVar4 = environment.getNextVariableString();

    /**
     * 
     * possible workaround for ida exporter issues with powerpc
     * 
     * try { int intOp1 = Integer.decode(integerOperand1.getValue()); }
     * 
     * catch (final NumberFormatException e) { final String [] foo =
     * integerOperand1.getValue().split(","); int intOp1 = Integer.decode(foo[2]); }
     */

    if (isSimplified) {
      if (instruction.getMnemonic().equals("extlwi") || instruction.getMnemonic().equals("extlwi.")) {
        // extlwi rA,rS,n,b (n > 0) equivalent to rlwinm rA,rS,b,0,n - 1
        SH = integerOperand2.getValue();
        MB = String.valueOf(0L);
        ME = String.valueOf(Integer.decode(integerOperand1.getValue()) - 1);
      } else if (instruction.getMnemonic().equals("extrwi")
          || instruction.getMnemonic().equals("extrwi.")) {
        // extrwi rA,rS,n,b (n > 0) equivalent to rlwinm rA,rS,b + n,32 - n,31
        SH =
            String.valueOf(Integer.decode(integerOperand2.getValue())
                + Integer.decode(integerOperand1.getValue()));
        MB = String.valueOf(32 - Integer.decode(integerOperand1.getValue()));
        ME = String.valueOf(31L);
      } else if (instruction.getMnemonic().equals("rotlwi")
          || instruction.getMnemonic().equals("rotlwi.")) {
        // rotlwi rA,rS,n equivalent to rlwinm rA,rS,n,0,31
        SH = integerOperand1.getValue();
        MB = String.valueOf(0L);
        ME = String.valueOf(31L);
      } else if (instruction.getMnemonic().equals("rotrwi")
          || instruction.getMnemonic().equals("rotrwi.")) {
        // rotrwi rA,rS,n equivalent to rlwinm rA,rS,32 - n,0,31
        SH = String.valueOf(32 - Integer.decode(integerOperand1.getValue()));
        MB = String.valueOf(0L);
        ME = String.valueOf(31L);
      } else if (instruction.getMnemonic().equals("slwi")
          || instruction.getMnemonic().equals("slwi.")) {
        // slwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,n,0,31 - n
        SH = integerOperand1.getValue();
        MB = String.valueOf(0L);
        ME = String.valueOf(31 - Integer.decode(integerOperand1.getValue()));
      } else if (instruction.getMnemonic().equals("srwi")
          || instruction.getMnemonic().equals("srwi.")) {
        // srwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,32 - n,n,31
        SH = String.valueOf(32 - Integer.decode(integerOperand1.getValue()));
        MB = integerOperand1.getValue();
        ME = String.valueOf(31L);
      } else if (instruction.getMnemonic().equals("clrlwi")
          || instruction.getMnemonic().equals("clrlwi.")) {
        // clrlwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,0,n,31
        SH = String.valueOf(0L);
        MB = integerOperand1.getValue();
        ME = String.valueOf(31L);
      } else if (instruction.getMnemonic().equals("clrrwi")
          || instruction.getMnemonic().equals("clrrwi.")) {
        // clrrwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,0,0,31 - n
        SH = String.valueOf(0L);
        MB = String.valueOf(0L);
        ME = String.valueOf(31 - Integer.decode(integerOperand1.getValue()));
      } else if (instruction.getMnemonic().equals("clrlslwi")
          || instruction.getMnemonic().equals("clrlslwi.")) {
        // clrlslwi rA,rS,b,n (n <= b < 32) equivalent to rlwinm rA,rS,n,b - n,31 - n
        SH = integerOperand2.getValue();
        MB =
            String.valueOf(Integer.decode(integerOperand1.getValue())
                - Integer.decode(integerOperand2.getValue()));
        ME = String.valueOf(31 - Integer.decode(integerOperand2.getValue()));
      } else {
        SH = integerOperand1.getValue();
        MB = integerOperand2.getValue();
      }
    } else {
      SH = integerOperand1.getValue();
      MB = integerOperand2.getValue();
      ME = integerOperand3.getValue();
    }

    final String normalMask =
        (instruction.getOperands().size() == 4) && !isSimplified ? integerOperand2.getValue()
            : Helpers.getRotateMask(MB, ME);

    final OperandSize dw = OperandSize.DWORD;

    /**
     * rlwinm Rotate Left Word Immediate then AND with Mask (x'5400 0000') rlwinm rA,rS,SH,MB,ME (Rc
     * = 0) rlwinm. rA,rS,SH,MB,ME (Rc = 1) n <- SH r <- ROTL(rS, n) m <- MASK(MB , ME) rA <- r & m
     * 
     * Simplified: extlwi rA,rS,n,b (n > 0) equivalent to rlwinm rA,rS,b,0,n - 1 extrwi rA,rS,n,b (n
     * > 0) equivalent to rlwinm rA,rS,b + n,32 - n,31 rotlwi rA,rS,n equivalent to rlwinm
     * rA,rS,n,0,31 rotrwi rA,rS,n equivalent to rlwinm rA,rS,32 - n,0,31 slwi rA,rS,n (n < 32)
     * equivalent to rlwinm rA,rS,n,0,31 - n srwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,32 -
     * n,n,31 clrlwi rA,rS,n (n < 32) equivalent to rlwinm rA,rS,0,n,31 clrrwi rA,rS,n (n < 32)
     * equivalent to rlwinm rA,rS,0,0,31 - n clrlslwi rA,rS,b,n (n <= b < 32) equivalent to rlwinm
     * rA,rS,n,b - n,31 - n
     * 
     */

    // generate rotate through shift
    instructions.add(ReilHelpers
        .createBsh(baseOffset++, dw, sourceRegister, dw, SH, dw, rotateVar1));
    instructions.add(ReilHelpers.createBsh(baseOffset++, dw, sourceRegister, dw,
        String.valueOf(-(32 - Integer.decode(SH))), dw, rotateVar2));
    instructions.add(ReilHelpers.createOr(baseOffset++, dw, rotateVar1, dw, rotateVar2, dw,
        rotateVar3));
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, rotateVar3, dw,
        String.valueOf(0xFFFFFFFFL), dw, rotateVar4));

    // apply normal mask
    instructions.add(ReilHelpers.createAnd(baseOffset++, dw, rotateVar4, dw, normalMask, dw,
        targetRegister));

    if (setCr) {
      // EQ CR0
      instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.DWORD, targetRegister,
          OperandSize.BYTE, Helpers.CR0_EQUAL));

      // LT CR0
      instructions.add(ReilHelpers.createBsh(baseOffset++, OperandSize.DWORD, targetRegister,
          OperandSize.WORD, "-31", OperandSize.BYTE, Helpers.CR0_LESS_THEN));

      // GT CR0
      instructions.add(ReilHelpers.createOr(baseOffset++, OperandSize.BYTE, Helpers.CR0_EQUAL,
          OperandSize.BYTE, Helpers.CR0_LESS_THEN, OperandSize.BYTE, crTemp));
      instructions.add(ReilHelpers.createBisz(baseOffset++, OperandSize.BYTE, crTemp,
          OperandSize.BYTE, Helpers.CR0_GREATER_THEN));

      // SO CR0
      instructions.add(ReilHelpers.createStr(baseOffset, OperandSize.BYTE,
          Helpers.XER_SUMMARY_OVERFLOW, OperandSize.BYTE, Helpers.CRO_SUMMARY_OVERFLOW));
    }
  }
}
