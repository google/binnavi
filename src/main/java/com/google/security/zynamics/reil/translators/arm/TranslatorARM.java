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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.ITranslationExtension;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IInstruction;


public class TranslatorARM<InstructionType extends IInstruction> implements
    ITranslator<InstructionType> {
  /**
   * List of translator for all opcodes
   */
  private static HashMap<String, IInstructionTranslator> translators =
      new HashMap<String, IInstructionTranslator>();

  private final static String matchLDR = "LDR";
  private final static String matchSTR = "STR";
  /**
   * Initializes the list of translators
   */
  static {
    try {
      /**
       * pseudo instructions that do not take conditions
       */

      translators.put("NOP", new ARMNopTranslator());

      /**
       * instructions that take conditions
       */
      final String[] conditions =
          {"", "EQ", "NE", "CS", "HS", "CC", "LO", "MI", "PL", "VS", "VC", "HI", "LS", "GE", "LT",
              "GT", "LE", "AL", "NV"};
      final String[] multiRegisterInstructions = {"DA", "DB", "IA", "IB", "EA", "FA", "FD", "ED"};

      for (final String condition : conditions) {
        translators.put("ADC" + condition, new ARMAdcTranslator());
        translators.put("ADC" + condition + "S", new ARMAdcTranslator());
        translators.put("ADD" + condition, new ARMAddTranslator());
        translators.put("ADD" + condition + "S", new ARMAddTranslator());
        translators.put("ADDW" + condition + "S", new ARMAddTranslator());
        translators.put("ADDW" + condition, new ARMAddTranslator());
        translators.put("ADDS" + condition, new ARMAddTranslator());
        translators.put("ADRL" + condition, new ARMAdrlTranslator());
        translators.put("ADR" + condition, new ARMAdrTranslator());
        translators.put("ALIGN" + condition, new ARMAlignTranslator());
        translators.put("AND" + condition, new ARMAndTranslator());
        translators.put("AND" + condition + "S", new ARMAndTranslator());
        translators.put("BIC" + condition, new ARMBicTranslator());
        translators.put("BIC" + condition + "S", new ARMBicTranslator());
        translators.put("BKPT", new ARMBkptTranslator());
        translators.put("BL" + condition, new ARMBlTranslator());
        translators.put("B" + condition, new ARMBlTranslator());
        translators.put("BLX" + condition, new ARMBlxTranslator());
        translators.put("BXJ" + condition, new ARMBxjTranslator());
        translators.put("BX" + condition, new ARMBxTranslator());
        translators.put("CDP" + condition, new ARMCdpTranslator());
        translators.put("CLZ" + condition, new ARMClzTranslator());
        translators.put("CMN" + condition, new ARMCmnTranslator());
        translators.put("CMP" + condition, new ARMCmpTranslator());
        translators.put("CPS" + condition, new ARMCpsTranslator());
        translators.put("CPY" + condition, new ARMCpyTranslator());
        translators.put("DCB" + condition, new ARMDcTranslator());
        translators.put("DCW" + condition, new ARMDcTranslator());
        translators.put("DCD" + condition, new ARMDcTranslator());
        translators.put("DCS" + condition, new ARMDcTranslator());
        translators.put("EOR" + condition, new ARMEorTranslator());
        translators.put("EOR" + condition + "S", new ARMEorTranslator());
        translators.put("EQUB" + condition, new ARMEorTranslator());
        translators.put("EQUW" + condition, new ARMEorTranslator());
        translators.put("EQUD" + condition, new ARMEorTranslator());
        translators.put("EQUS" + condition, new ARMEorTranslator());
        translators.put("LDC" + condition, new ARMLdcTranslator());

        for (final String multiRegisterInstruction : multiRegisterInstructions) {
          translators.put("LDM" + condition + multiRegisterInstruction, new ARMLdmTranslator());
        }

        translators.put(matchLDR + condition + "B", new ARMLdrbTranslator());
        translators.put(matchLDR + condition + "BT", new ARMLdrbtTranslator());
        translators.put(matchLDR + condition + "D", new ARMLdrdTranslator());
        translators.put("LDREX" + condition, new ARMLdrexTranslator());
        translators.put(matchLDR + condition + "H", new ARMLdrhTranslator());
        translators.put(matchLDR + condition + "SB", new ARMLdrsbTranslator());
        translators.put(matchLDR + condition + "SH", new ARMLdrshTranslator());
        translators.put(matchLDR + condition, new ARMLdrTranslator());
        translators.put(matchLDR + condition + "T", new ARMLdrtTranslator());
        translators.put("MCRR" + condition, new ARMMcrrTranslator());
        translators.put("MCR" + condition, new ARMMcrTranslator());
        translators.put("MLA" + condition, new ARMMlaTranslator());
        translators.put("MLA" + condition + "S", new ARMMlaTranslator());
        translators.put("MOV" + condition, new ARMMovTranslator());
        translators.put("MOV" + condition + "S", new ARMMovTranslator());
        translators.put("MRC" + condition, new ARMMrcTranslator());
        translators.put("MRRC" + condition, new ARMMrrcTranslator());
        translators.put("MRS" + condition, new ARMMrsTranslator());
        translators.put("MSR" + condition, new ARMMsrTranslator());
        translators.put("MUL" + condition, new ARMMulTranslator());
        translators.put("MUL" + condition + "S", new ARMMulTranslator());
        translators.put("MVN" + condition, new ARMMvnTranslator());
        translators.put("MVN" + condition + "S", new ARMMvnTranslator());
        translators.put("ORR" + condition, new ARMOrrTranslator());
        translators.put("ORR" + condition + "S", new ARMOrrTranslator());
        translators.put("PKHBT" + condition, new ARMPkhbtTranslator());
        translators.put("PKHTB" + condition, new ARMPkhtbTranslator());
        translators.put("PLD" + condition, new ARMPldTranslator());
        translators.put("QADD16" + condition, new ARMQadd16Translator());
        translators.put("QADD8" + condition, new ARMQadd8Translator());
        translators.put("QADDSUBX" + condition, new ARMQaddsubxTranslator());
        translators.put("QASX" + condition, new ARMQaddsubxTranslator());
        translators.put("QADD" + condition, new ARMQaddTranslator());
        translators.put("QDADD" + condition, new ARMQdaddTranslator());
        translators.put("QDSUB" + condition, new ARMQdsubTranslator());
        translators.put("QSUB16" + condition, new ARMQsub16Translator());
        translators.put("QSUb8" + condition, new ARMQsub8Translator());
        translators.put("QSUBADDX" + condition, new ARMQsubaddxTranslator());
        translators.put("QSAX" + condition, new ARMQsubaddxTranslator());
        translators.put("QSUB" + condition, new ARMQsubTranslator());
        translators.put("REV16" + condition, new ARMRev16Translator());
        translators.put("REVSH" + condition, new ARMRevshTranslator());
        translators.put("REV" + condition, new ARMRevTranslator());
        translators.put("RFE" + condition, new ARMRfeTranslator());
        translators.put("RSB" + condition, new ARMRsbTranslator());
        translators.put("RSB" + condition + "S", new ARMRsbTranslator());
        translators.put("RSC" + condition, new ARMRscTranslator());
        translators.put("RSC" + condition + "S", new ARMRscTranslator());
        translators.put("SADD16" + condition, new ARMSadd16Translator());
        translators.put("SADD8" + condition, new ARMSadd8Translator());
        translators.put("SADDSUBX" + condition, new ARMSaddsubxTranslator());
        translators.put("SASX" + condition, new ARMSaddsubxTranslator());
        translators.put("SBC" + condition, new ARMSbcTranslator());
        translators.put("SBC" + condition + "S", new ARMSbcTranslator());
        translators.put("SEL" + condition, new ARMSelTranslator());
        translators.put("SETEND" + condition, new ARMSetendTranslator());
        translators.put("SHADD16" + condition, new ARMShadd16Translator());
        translators.put("SHADD8" + condition, new ARMShadd8Translator());
        translators.put("SHADDSUBX" + condition, new ARMShaddsubxTranslator());
        translators.put("SHASX" + condition, new ARMShaddsubxTranslator());
        translators.put("SHSUB16" + condition, new ARMShsub16Translator());
        translators.put("SHSUB8" + condition, new ARMShsub8Translator());
        translators.put("SHSUBADDX" + condition, new ARMShsubaddxTranslator());
        translators.put("SHSAX" + condition, new ARMShsubaddxTranslator());
        translators.put("SMLAD" + condition, new ARMSmladTranslator());
        translators.put("SMLALD" + condition, new ARMSmlaldTranslator());
        translators.put("SMLAL" + condition, new ARMSmlalTranslator());
        translators.put("SMLAL" + condition + "S", new ARMSmlalTranslator());
        translators.put("SMLALXY" + condition, new ARMSmlalXYTranslator());
        translators.put("SMLAWY" + condition, new ARMSmlawYTranslator());
        translators.put("SMLAXY" + condition, new ARMSmlaXYTranslator());
        translators.put("SMLSD" + condition, new ARMSmlsdTranslator());
        translators.put("SMLSDX" + condition, new ARMSmlsdTranslator());
        translators.put("SMLSLD" + condition, new ARMSmlsldTranslator());
        translators.put("SMLSLDX" + condition, new ARMSmlsldTranslator());
        translators.put("SMMLA" + condition, new ARMSmmlaTranslator());
        translators.put("SMMLAR" + condition, new ARMSmmlaTranslator());
        translators.put("SMMLS" + condition, new ARMSmmlsTranslator());
        translators.put("SMMLSR" + condition, new ARMSmmlsTranslator());
        translators.put("SMMUL" + condition, new ARMSmmulTranslator());
        translators.put("SMMUL" + condition + ".W", new ARMSmmulTranslator());
        translators.put("SMMULR" + condition, new ARMSmmulTranslator());
        translators.put("SMMULR" + condition + ".W", new ARMSmmulTranslator());
        translators.put("SMUAD" + condition, new ARMSmuadTranslator());
        translators.put("SMUADX" + condition, new ARMSmuadTranslator());
        translators.put("SMULL" + condition, new ARMSmullTranslator());
        translators.put("SMULL" + condition + "S", new ARMSmullTranslator());
        translators.put("SMULWY" + condition, new ARMSmulwYTranslator());
        translators.put("SMULXY" + condition, new ARMSmulXYTranslator());
        translators.put("SMUSD" + condition, new ARMSmusdTranslator());
        translators.put("SMUSDX" + condition, new ARMSmusdTranslator());
        translators.put("SRS" + condition, new ARMSrsTranslator());
        translators.put("SSAT16" + condition, new ARMSsat16Translator());
        translators.put("SSAT" + condition, new ARMSsatTranslator());
        translators.put("SSUB16" + condition, new ARMSsub16Translator());
        translators.put("SSUB8" + condition, new ARMSsub8Translator());
        translators.put("SSUBADDX" + condition, new ARMSsubaddxTranslator());
        translators.put("SSAX" + condition, new ARMSsubaddxTranslator());
        translators.put("STC" + condition, new ARMStcTranslator());

        for (final String multiRegisterInstruction : multiRegisterInstructions) {
          translators.put("STM" + condition + multiRegisterInstruction, new ARMStmTranslator());
        }

        translators.put(matchSTR + condition + "B", new ARMStrbTranslator());
        translators.put(matchSTR + condition + "BT", new ARMStrbtTranslator());
        translators.put(matchSTR + condition + "D", new ARMStrdTranslator());
        translators.put("STREX" + condition, new ARMStrexTranslator());
        translators.put(matchSTR + condition + "H", new ARMStrhTranslator());
        translators.put(matchSTR + condition, new ARMStrTranslator());
        translators.put(matchSTR + condition + "T", new ARMStrtTranslator());
        translators.put("SUB" + condition, new ARMSubTranslator());
        translators.put("SUB" + condition + "S", new ARMSubTranslator());
        translators.put("SWI" + condition, new ARMSwiTranslator());
        translators.put("SWP" + condition + "B", new ARMSwpbTranslator());
        translators.put("SWP" + condition, new ARMSwpTranslator());
        translators.put("SXTAB16" + condition, new ARMSxtab16Translator());
        translators.put("SXTAB" + condition, new ARMSxtabTranslator());
        translators.put("SXTAB" + condition + ".W", new ARMSxtabTranslator());
        translators.put("SXTAH" + condition, new ARMSxtahTranslator());
        translators.put("SXTB16" + condition, new ARMSxtb16Translator());
        translators.put("SXTB" + condition, new ARMSxtbTranslator());
        translators.put("SXTH" + condition, new ARMSxthTranslator());
        translators.put("TEQ" + condition, new ARMTeqTranslator());
        translators.put("TST" + condition, new ARMTstTranslator());
        translators.put("UADD16" + condition, new ARMUadd16Translator());
        translators.put("UADD8" + condition, new ARMUadd8Translator());
        translators.put("UADDSUBX" + condition, new ARMUaddsubxTranslator());
        translators.put("UASX" + condition, new ARMUaddsubxTranslator());
        translators.put("UHADD16" + condition, new ARMUhadd16Translator());
        translators.put("UHADD8" + condition, new ARMUhadd8Translator());
        translators.put("UHADDSUBX" + condition, new ARMUhaddsubxTranslator());
        translators.put("UHASX" + condition, new ARMUhaddsubxTranslator());
        translators.put("UHSUB16" + condition, new ARMUhsub16Translator());
        translators.put("UHSUB8" + condition, new ARMUhsub8Translator());
        translators.put("UHSUBADDX" + condition, new ARMUhsubaddxTranslator());
        translators.put("UHSAX" + condition, new ARMUhsubaddxTranslator());
        translators.put("UMAAL" + condition, new ARMUmaalTranslator());
        translators.put("UMLAL" + condition, new ARMUmlalTranslator());
        translators.put("UMLAL" + condition + "S", new ARMUmlalTranslator());
        translators.put("UMULL" + condition, new ARMUmullTranslator());
        translators.put("UMULL" + condition + "S", new ARMUmullTranslator());
        translators.put("UQADD16" + condition, new ARMUqadd16Translator());
        translators.put("UQADD8" + condition, new ARMUqadd8Translator());
        translators.put("UQADDSUBX" + condition, new ARMUqaddsubxTranslator());
        translators.put("UQASX" + condition, new ARMUqaddsubxTranslator());
        translators.put("UQSUB16" + condition, new ARMUqsub16Translator());
        translators.put("UQSUB8" + condition, new ARMUqsub8Translator());
        translators.put("UQSUBADDX" + condition, new ARMUqsubaddxTranslator());
        translators.put("UQSAX" + condition, new ARMUqsubaddxTranslator());
        translators.put("USAD8" + condition, new ARMUsad8Translator());
        translators.put("USADA8" + condition, new ARMUsada8Translator());
        translators.put("USAT16" + condition, new ARMUsat16Translator());
        translators.put("USAT" + condition, new ARMUsatTranslator());
        translators.put("USUB16" + condition, new ARMUsub16Translator());
        translators.put("USUB8" + condition, new ARMUsub8Translator());
        translators.put("USUBADDX" + condition, new ARMUsubaddxTranslator());
        translators.put("USAX" + condition, new ARMUsubaddxTranslator());
        translators.put("UXTAB16" + condition, new ARMUxtab16Translator());
        translators.put("UXTAB" + condition, new ARMUxtabTranslator());
        translators.put("UXTAH" + condition, new ARMUxtahTranslator());
        translators.put("UXTB16" + condition, new ARMUxtb16Translator());
        translators.put("UXTB" + condition, new ARMUxtbTranslator());
        translators.put("UXTH" + condition, new ARMUxthTranslator());

        /**
         * THUMB translators ( 16 Bit instruction set )
         */

        translators.put("THUMBADCS" + condition, new THUMBAdcTranslator());
        translators.put("THUMBADC" + condition, new THUMBAdcTranslator());
        translators.put("THUMBADDS" + condition, new THUMBAddTranslator());
        translators.put("THUMBADD" + condition, new THUMBAddTranslator());
        translators.put("THUMBADDW" + condition, new THUMBAddTranslator());
        translators.put("THUMBADR" + condition, new ARMAdrTranslator());
        translators.put("THUMBANDS" + condition, new THUMBAndTranslator());
        translators.put("THUMBAND" + condition, new THUMBAndTranslator());
        translators.put("THUMBASRS" + condition, new THUMBAsrTranslator());
        translators.put("THUMBASR" + condition, new THUMBAsrTranslator());
        translators.put("THUMBBICS" + condition, new THUMBBicTranslator());
        translators.put("THUMBBIC" + condition, new THUMBBicTranslator());
        translators.put("THUMBBKPT" + condition, new THUMBBkptTranslator());
        translators.put("THUMBBL" + condition, new THUMBBlTranslator());
        translators.put("THUMBBLX" + condition, new THUMBBlTranslator());
        translators.put("THUMBB" + condition, new THUMBBTranslator());
        translators.put("THUMBBX" + condition, new THUMBBxTranslator());
        translators.put("THUMBCMN" + condition, new THUMBCmnTranslator());
        translators.put("THUMBCMP" + condition, new THUMBCmpTranslator());
        translators.put("THUMBCPS" + condition, new THUMBCpsTranslator());
        translators.put("THUMBCPY" + condition, new THUMBCpyTranslator());
        translators.put("THUMBEORS" + condition, new THUMBEorTranslator());
        translators.put("THUMBEOR" + condition, new THUMBEorTranslator());
        translators.put("THUMBLDMIA" + condition, new THUMBLdmiaTranslator());
        translators.put("THUMBLDRB" + condition, new THUMBLdrbTranslator());
        translators.put("THUMBLDRH" + condition, new THUMBLdrhTranslator());
        translators.put("THUMBLDRSB" + condition, new THUMBLdrsbTranslator());
        translators.put("THUMBLDRSH" + condition, new THUMBLdrshTranslator());
        translators.put("THUMBLDR" + condition, new THUMBLdrTranslator());
        translators.put("THUMBLSLS" + condition, new THUMBLslTranslator());
        translators.put("THUMBLSL" + condition, new THUMBLslTranslator());
        translators.put("THUMBLSRS" + condition, new THUMBLsrTranslator());
        translators.put("THUMBLSR" + condition, new THUMBLsrTranslator());
        translators.put("THUMBMOVS" + condition, new THUMBMovTranslator());
        translators.put("THUMBMOV" + condition, new THUMBMovTranslator());
        translators.put("THUMBMUL" + condition, new THUMBMulTranslator());
        translators.put("THUMBMULS" + condition, new THUMBMulTranslator());
        translators.put("THUMBMVNS" + condition, new THUMBMvnTranslator());
        translators.put("THUMBNEGS" + condition, new THUMBNegTranslator());
        translators.put("THUMBNEG" + condition, new THUMBNegTranslator());
        translators.put("THUMBORRS" + condition, new THUMBOrrTranslator());
        translators.put("THUMBORR" + condition, new THUMBOrrTranslator());
        translators.put("THUMBPOP" + condition, new THUMBPopTranslator());
        translators.put("THUMBPUSH" + condition, new THUMBPushTranslator());
        translators.put("THUMBREV16" + condition, new THUMBRev16Translator());
        translators.put("THUMBREVSH" + condition, new THUMBRevshTranslator());
        translators.put("THUMBREV" + condition, new THUMBRevTranslator());
        translators.put("THUMBRORS" + condition, new THUMBRorTranslator());
        translators.put("THUMBSBCS" + condition, new THUMBSbcTranslator());
        translators.put("THUMBSETEND" + condition, new THUMBSetendTranslator());
        translators.put("THUMBSTMIA" + condition, new THUMBStmiaTranslator());
        translators.put("THUMBSTR" + condition + "B", new THUMBStrbTranslator());
        translators.put("THUMBSTRH" + condition, new THUMBStrhTranslator());
        translators.put("THUMBSTR" + condition, new THUMBStrTranslator());
        translators.put("THUMBSUBS" + condition, new THUMBSubTranslator());
        translators.put("THUMBSUB" + condition, new THUMBSubTranslator());
        translators.put("THUMBSWI" + condition, new THUMBSwiTranslator());
        translators.put("THUMBSXTB" + condition, new THUMBSxtbTranslator());
        translators.put("THUMBSXTH" + condition, new THUMBSxthTranslator());
        translators.put("THUMBUXTB" + condition, new THUMBUxtbTranslator());
        translators.put("THUMBUXTH" + condition, new THUMBUxthTranslator());
        translators.put("THUMBTST" + condition, new THUMBTstTranslator());
        translators.put("THUMBNOP" + condition, new THUMBNopTranslator());

        /**
         * THUMB-2 translators !!! Warning !!! these translators are all now very well tested and
         * are in here because of the iPhone 3GS libraries which we wanted to analyze it could be
         * that the is a need to refine a lot of the currently available ARM translators for the
         * ARMv7 ABI.
         */

        translators.put("ADD" + condition + ".W", new ARMAddTranslator());
        translators.put("ADD" + condition + "S.W", new ARMAddTranslator());
        translators.put("ADC" + condition + ".W", new ARMAdcTranslator());
        translators.put("ADC" + condition + "S.W", new ARMAdcTranslator());
        translators.put("ADC" + condition + "S.W", new ARMAdcTranslator());
        translators.put("ADR" + condition + ".W", new ARMAdrTranslator());
        translators.put("AND" + condition + ".W", new ARMAndTranslator());
        translators.put("AND" + condition + "S.W", new ARMAndTranslator());
        translators.put("AST" + condition + ".W", new THUMBAsrTranslator());
        translators.put("ASR" + condition + ".W", new THUMBAsrTranslator());
        translators.put("ASR" + condition + "S.W", new THUMBAsrTranslator());
        translators.put("ASR" + condition, new THUMBAsrTranslator());
        translators.put("B" + condition + ".W", new THUMBBTranslator());
        translators.put("BIC" + condition + ".W", new THUMBBicTranslator());
        translators.put("BIC" + condition + "S.W", new THUMBBicTranslator());
        translators.put("BFC" + condition, new THUMB2BFCTranslator());
        translators.put("BFC" + condition + ".W", new THUMB2BFCTranslator());
        translators.put("BFC" + condition + "S.W", new THUMB2BFCTranslator());
        translators.put("BFI" + condition, new THUMB2BFITranslator());
        translators.put("BFI" + condition + ".W", new THUMB2BFITranslator());
        translators.put("BFI" + condition + "S.W", new THUMB2BFITranslator());
        translators.put("THUMBCBZ", new THUMB2CbzTranslator());
        translators.put("THUMBCBNZ", new THUMB2CbnzTranslator());
        translators.put("CLZ" + condition + ".W", new ARMClzTranslator());
        translators.put("CMN" + condition + ".W", new ARMCmnTranslator());
        translators.put("CMN" + condition + "S.W", new ARMCmnTranslator());
        translators.put("CMP" + condition + ".W", new ARMCmpTranslator());
        translators.put("EOR" + condition + ".W", new ARMEorTranslator());
        translators.put("EOR" + condition + "S.W", new ARMEorTranslator());
        translators.put("THUMBIT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTTT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITEE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITEEE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITEET " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITETE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITETT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITET " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTEE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTET " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTT " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTTE " + condition, new THUMB2ItTranslator());
        translators.put("THUMBITTTT " + condition, new THUMB2ItTranslator());

        for (final String multiRegisterInstruction : multiRegisterInstructions) {
          translators.put("LDM" + condition + multiRegisterInstruction + ".W",
              new ARMLdmTranslator());
        }

        translators.put(matchLDR + condition + ".W", new ARMLdrTranslator());
        translators.put(matchLDR + condition + "B.W", new ARMLdrbTranslator());
        translators.put(matchLDR + condition + "D.W", new THUMB2LDRDTranslator());
        translators.put(matchLDR + "D" + condition + ".W", new THUMB2LDRDTranslator());
        translators.put("LDRD" + condition + ".W", new THUMB2LDRDTranslator());
        translators.put("THUMBLDR" + condition + "B", new ARMLdrbTranslator());
        translators.put(matchLDR + condition + "H.W", new ARMLdrhTranslator());
        translators.put("THUMBLDR" + condition + "H", new ARMLdrhTranslator());
        translators.put("LDRHT" + condition + "S.W", new ARMLdrhtTranslator());
        translators.put("LDRHT" + condition + ".W", new ARMLdrhtTranslator());
        translators.put("LDRHT" + condition, new ARMLdrhtTranslator());
        translators.put("LDRSB" + condition + ".W", new ARMLdrsbTranslator());
        translators.put("LDRSBT" + condition + "S.W", new ARMLdrsbTranslator());
        translators.put("LDRSBT" + condition + ".W", new ARMLdrsbTranslator());
        translators.put("LDRSBT" + condition, new ARMLdrsbTranslator());
        translators.put("LDRSH" + condition, new ARMLdrshTranslator());
        translators.put("LDRSH" + condition + ".W", new ARMLdrshTranslator());
        translators.put("LDRSHT" + condition, new ARMLdrshTranslator());
        translators.put("LDRSHT" + condition + ".W", new ARMLdrshTranslator());
        translators.put("LDRSHT" + condition + "S.W", new ARMLdrshTranslator());
        translators.put("LSL" + condition + ".W", new THUMBLslTranslator());
        translators.put("LSL" + condition + "S.W", new THUMBLslTranslator());
        translators.put("LSR" + condition + ".W", new THUMBLsrTranslator());
        translators.put("LSR" + condition + "S.W", new THUMBLsrTranslator());
        translators.put("MLA" + condition + ".W", new ARMMlaTranslator());
        translators.put("MLS" + condition + ".W", new THUMB2MlsTranslator());
        translators.put("MLS" + condition, new THUMB2MlsTranslator());
        translators.put("MOV" + condition + ".W", new ARMMovTranslator());
        translators.put("MOV" + condition + "S.W", new ARMMovTranslator());
        translators.put("MOV" + condition + "W", new ARMMovTranslator());
        translators.put("MOVT" + condition + ".W", new THUMB2MovtTranslator());
        translators.put("MOVT" + condition + "S.W", new THUMB2MovtTranslator());
        translators.put("MOVT" + condition, new THUMB2MovtTranslator());
        translators.put("MUL" + condition + ".W", new ARMMulTranslator());
        translators.put("MVN" + condition + ".W", new ARMMvnTranslator());
        translators.put("MVN" + condition + "S.W", new ARMMvnTranslator());
        translators.put("ORR" + condition + ".W", new ARMOrrTranslator());
        translators.put("ORR" + condition + "S.W", new ARMOrrTranslator());
        translators.put("ORN" + condition + ".W", new ARMOrnTranslator());
        translators.put("ORN" + condition + "S.W", new ARMOrnTranslator());
        translators.put("ORN" + condition, new ARMOrnTranslator());
        translators.put("ORN" + condition + "S", new ARMOrnTranslator());
        translators.put("POP" + condition + ".W", new THUMBPopTranslator());
        translators.put("PUSH" + condition + ".W", new THUMBPushTranslator());
        translators.put("RBIT" + condition, new THUMB2RBITTranslator());
        translators.put("RBIT" + condition + ".W", new THUMB2RBITTranslator());
        translators.put("RBIT" + condition + "S.W", new THUMB2RBITTranslator());
        translators.put("ROR" + condition + ".W", new THUMBRorTranslator());
        translators.put("ROR" + condition + "S.W", new THUMBRorTranslator());
        translators.put("RRX" + condition, new ARMRrxTranslator());
        translators.put("RRX" + condition + "S.W", new ARMRrxTranslator());
        translators.put("RRX" + condition + "S.W", new ARMRrxTranslator());
        translators.put("RRX" + condition + "S", new ARMRrxTranslator());
        translators.put("RRXS" + condition, new ARMRrxTranslator());
        translators.put("RSB" + condition + ".W", new ARMRsbTranslator());
        translators.put("RSB" + condition + "S.W", new ARMRsbTranslator());
        translators.put("TEQ" + condition + ".W", new ARMTeqTranslator());
        translators.put("TBB" + condition + ".W", new THUMB2TbbTranslator());
        translators.put("TBB" + condition, new THUMB2TbbTranslator());
        translators.put("TBH" + condition + ".W", new THUMB2TbhTranslator());
        translators.put("TBH" + condition, new THUMB2TbhTranslator());
        translators.put("SBC" + condition + ".W", new ARMSbcTranslator());
        translators.put("SBC" + condition + "S.W", new ARMSbcTranslator());
        translators.put("SBFX" + condition + "S.W", new ARMSBFXTranslator());
        translators.put("SBFX" + condition + ".W", new ARMSBFXTranslator());
        translators.put("SBFX" + condition, new ARMSBFXTranslator());
        translators.put("SDIV" + condition, new ARMSdivTranslator());
        translators.put("SDIV" + condition + ".W", new ARMSdivTranslator());
        translators.put("SDIV" + condition + "S.W", new ARMSdivTranslator());
        translators.put("SMLAL" + condition + ".W", new ARMSmlalTranslator());
        translators.put("SMULL" + condition + ".W", new ARMSmullTranslator());

        for (final String multiRegisterInstruction : multiRegisterInstructions) {
          translators.put("STM" + condition + multiRegisterInstruction + ".W",
              new ARMStmTranslator());
        }

        translators.put(matchSTR + "B" + condition + ".W", new ARMStrbTranslator());
        translators.put(matchSTR + "H" + condition + ".W", new ARMStrhTranslator());
        translators.put(matchSTR + "D" + condition + ".W", new THUMB2STRDTranslator());
        translators.put(matchSTR + condition + "D.W", new THUMB2STRDTranslator());
        translators.put(matchSTR + condition + "B.W", new ARMStrbTranslator());
        translators.put(matchSTR + condition + "H.W", new ARMStrhTranslator());
        translators.put(matchSTR + condition + ".W", new ARMStrTranslator());
        translators.put("STRHT" + condition + "S.W", new ARMStrhTranslator());
        translators.put("STRHT" + condition + "W", new ARMStrhTranslator());
        translators.put("STRHT" + condition, new ARMStrhTranslator());
        translators.put("SUB" + condition + ".W", new ARMSubTranslator());
        translators.put("SUBW" + condition, new ARMSubTranslator());
        translators.put("SUBS" + condition + ".W", new ARMSubTranslator());
        translators.put("SUB" + condition + "S.W", new ARMSubTranslator());
        translators.put("SXTB" + condition + ".W", new ARMSxtbTranslator());
        translators.put("SXTH" + condition + ".W", new ARMSxthTranslator());
        translators.put("TST" + condition + ".W", new ARMTstTranslator());
        translators.put("UBFX" + condition, new ARMUBFXTranslator());
        translators.put("UBFX" + condition + "S.W", new ARMUBFXTranslator());
        translators.put("UBFX" + condition + ".W", new ARMUBFXTranslator());
        translators.put("UDIV" + condition, new ARMUdivTranslator());
        translators.put("UDIV" + condition + ".W", new ARMUdivTranslator());
        translators.put("UDIV" + condition + "S.W", new ARMUdivTranslator());
        translators.put("UMLAL" + condition + ".W", new ARMUmlalTranslator());
        translators.put("UMULL" + condition + ".W", new ARMUmullTranslator());
        translators.put("UXTAH" + condition + ".W", new ARMUxtahTranslator());
        translators.put("UXTB" + condition + ".W", new ARMUxtbTranslator());
        translators.put("UXTH" + condition + ".W", new ARMUxthTranslator());
      }

      translators.put("FABSD", new ARMNopTranslator());
      translators.put("FABSDEQ", new ARMNopTranslator());
      translators.put("FABSS", new ARMNopTranslator());
      translators.put("FADDD", new ARMNopTranslator());
      translators.put("FADDDGT", new ARMNopTranslator());
      translators.put("FADDDLT", new ARMNopTranslator());
      translators.put("FADDDNE", new ARMNopTranslator());
      translators.put("FADDDPL", new ARMNopTranslator());
      translators.put("FADDS", new ARMNopTranslator());
      translators.put("FCMPD", new ARMNopTranslator());
      translators.put("FCMPED", new ARMNopTranslator());
      translators.put("FCMPEZD", new ARMNopTranslator());
      translators.put("FCMPEZS", new ARMNopTranslator());
      translators.put("FCMPS", new ARMNopTranslator());
      translators.put("FCMPZD", new ARMNopTranslator());
      translators.put("FCPYD", new ARMNopTranslator());
      translators.put("FCPYDGT", new ARMNopTranslator());
      translators.put("FCPYDLE", new ARMNopTranslator());
      translators.put("FCPYDLT", new ARMNopTranslator());
      translators.put("FCPYDNE", new ARMNopTranslator());
      translators.put("FCPYS", new ARMNopTranslator());
      translators.put("FCVTDS", new ARMNopTranslator());
      translators.put("FCVTSD", new ARMNopTranslator());
      translators.put("FDIVD", new ARMNopTranslator());
      translators.put("FDIVDNE", new ARMNopTranslator());
      translators.put("FDIVS", new ARMNopTranslator());
      translators.put("FLDD", new ARMNopTranslator());
      translators.put("FLDMEAD", new ARMNopTranslator());
      translators.put("FLDMIAX", new ARMNopTranslator());
      translators.put("FLDS", new ARMNopTranslator());
      translators.put("FMACD", new ARMNopTranslator());
      translators.put("FMDHR", new ARMNopTranslator());
      translators.put("FMDLR", new ARMNopTranslator());
      translators.put("FMDRR", new ARMNopTranslator());
      translators.put("FMRRD", new ARMNopTranslator());
      translators.put("FMRRDGE", new ARMNopTranslator());
      translators.put("FMRS", new ARMNopTranslator());
      translators.put("FMRSNE", new ARMNopTranslator());
      translators.put("FMRSPL", new ARMNopTranslator());
      translators.put("FMRX", new ARMNopTranslator());
      translators.put("FMSCD", new ARMNopTranslator());
      translators.put("FMSR", new ARMNopTranslator());
      translators.put("FMSRNE", new ARMNopTranslator());
      translators.put("FMSTAT", new ARMNopTranslator());
      translators.put("FMULD", new ARMNopTranslator());
      translators.put("FMULDNE", new ARMNopTranslator());
      translators.put("FMULS", new ARMNopTranslator());
      translators.put("FMXR", new ARMNopTranslator());
      translators.put("FNEGD", new ARMNopTranslator());
      translators.put("FNEGDLE", new ARMNopTranslator());
      translators.put("FNEGDLT", new ARMNopTranslator());
      translators.put("FNEGDMI", new ARMNopTranslator());
      translators.put("FNMACD", new ARMNopTranslator());
      translators.put("FNMACDLT", new ARMNopTranslator());
      translators.put("FSITOD", new ARMNopTranslator());
      translators.put("FSITOS", new ARMNopTranslator());
      translators.put("FSQRTD", new ARMNopTranslator());
      translators.put("FSQRTS", new ARMNopTranslator());
      translators.put("FSTD", new ARMNopTranslator());
      translators.put("FSTMIAX", new ARMNopTranslator());
      translators.put("FSUBD", new ARMNopTranslator());
      translators.put("FSUBDEQ", new ARMNopTranslator());
      translators.put("FSUBDGT", new ARMNopTranslator());
      translators.put("FSUBDPL", new ARMNopTranslator());
      translators.put("FSUBS", new ARMNopTranslator());
      translators.put("FTOSID", new ARMNopTranslator());
      translators.put("FTOSIS", new ARMNopTranslator());
      translators.put("FTOSIZD", new ARMNopTranslator());
      translators.put("FTOSIZDPL", new ARMNopTranslator());
      translators.put("FTOSIZS", new ARMNopTranslator());
      translators.put("FTOUIZD", new ARMNopTranslator());
      translators.put("FTOUIZS", new ARMNopTranslator());
      translators.put("FUITOD", new ARMNopTranslator());
      translators.put("FUITOS", new ARMNopTranslator());

    } catch (final Exception e) {
      // TODO Handle this more gracefully
    }
  }

  /**
   * Translates an ARM or THUMB instruction to REIL code
   * 
   * @param environment A valid translation environment
   * @param instruction The ARM or THUMB instruction to translate
   * 
   * @return The list of REIL instruction the ARM instruction was translated to
   * 
   * @throws InternalTranslationException An internal translation error occurred
   * @throws IllegalArgumentException Any of the arguments passed to the function are invalid
   * 
   */
  @Override
  public List<ReilInstruction> translate(final ITranslationEnvironment environment,
      final InstructionType instruction,
      final List<ITranslationExtension<InstructionType>> extensions)
      throws InternalTranslationException {
    Preconditions.checkNotNull(environment, "Error: Argument environment can't be null");
    Preconditions.checkNotNull(instruction, "Error: Argument instruction can't be null");

    final String mnemonic = instruction.getMnemonic();

    final long instLength = instruction.getLength();

    // TODO: >= 4 is a workaround because IDA merges multiple instructions into 1
    final String normalizedMnemonic = instLength >= 4 ? mnemonic : "THUMB" + mnemonic;

    final IInstructionTranslator translator = translators.get(normalizedMnemonic);

    if (translators.containsKey(normalizedMnemonic)) {
      final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

      translator.translate(environment, instruction, instructions);

      // for (final ITranslationExtension<InstructionType> extension : extensions)
      // {
      // extension.postProcess(environment, instruction, instructions);
      // }

      return instructions;
    } else if (mnemonic == null) {
      return new ArrayList<ReilInstruction>();
    } else {
      return Lists.newArrayList(ReilHelpers.createUnknown(ReilHelpers.toReilAddress(
          instruction.getAddress()).toLong()));
    }
  }
}
