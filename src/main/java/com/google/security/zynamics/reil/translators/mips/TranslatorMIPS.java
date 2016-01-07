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
package com.google.security.zynamics.reil.translators.mips;

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


public class TranslatorMIPS<InstructionType extends IInstruction> implements
    ITranslator<InstructionType> {
  /**
   * List of translator for all opcodes
   */
  private static HashMap<String, IInstructionTranslator> translators =
      new HashMap<String, IInstructionTranslator>();

  static {
    try {
      /*
       * arithmetic instructions
       */

      translators.put("add", new AddTranslator()); // ADD Add Word
      translators.put("addi", new AddiTranslator()); // ADDI Add Immediate Word
      translators.put("addiu", new AddiuTranslator()); // ADDIU Add Immediate Unsigned Word
      translators.put("addu", new AdduTranslator()); // ADDU Add Unsigned Word
      translators.put("clo", new CloTranslator()); // CLO Count Leading Ones in Word
      translators.put("clz", new ClzTranslator()); // CLZ Count Leading Zeros in Word
      translators.put("div", new DivTranslator()); // DIV Divide Word
      translators.put("divu", new DivuTranslator()); // DIVU Divide Unsigned Word
      translators.put("madd", new MaddTranslator()); // MADD Multiply and Add Word to Hi, Lo
      translators.put("maddu", new MadduTranslator()); // MADDU Multiply and Add Unsigned Word to
                                                       // Hi, Lo
      translators.put("msub", new MsubTranslator()); // MSUB Multiply and Subtract Word to Hi, Lo
      translators.put("msubu", new MsubuTranslator()); // MSUBU Multiply and Subtract Unsigned Word
                                                       // to Hi, Lo
      translators.put("mul", new MulTranslator()); // MUL Multiply Word to GPR
      translators.put("mult", new MultTranslator()); // MULT Multiply Word
      translators.put("multu", new MultuTranslator()); // MULTU Multiply Unsigned Word
      translators.put("negu", new NeguTranslator()); // NEGU Negate Unsigned Word
      translators.put("seb", new SebTranslator()); // SEB Sign-Extend Byte
      translators.put("seh", new SehTranslator()); // SEH Sign-Extend Halftword
      translators.put("slt", new SltTranslator()); // SLT Set on Less Than
      translators.put("slti", new SltiTranslator()); // SLTI Set on Less Than Immediate
      translators.put("sltiu", new SltiuTranslator()); // SLTIU Set on Less Than Immediate Unsigned
      translators.put("sltu", new SltuTranslator()); // SLTU Set on Less Than Unsigned
      translators.put("sub", new SubTranslator()); // SUB Subtract Word
      translators.put("subu", new SubuTranslator()); // SUBU Subtract Unsigned Word

      /*
       * CPU Branch and Jump Instructions
       */

      translators.put("b", new BTranslator()); // B Unconditional Branch
      translators.put("bal", new BalTranslator()); // BAL Branch and Link
      translators.put("beq", new BeqTranslator()); // BEQ Branch on Equal
      translators.put("bgez", new BgezTranslator()); // BGEZ Branch on Greater Than or Equal to Zero
      translators.put("bgezal", new BgezalTranslator()); // BGEZAL Branch on Greater Than or Equal
                                                         // to Zero and Link
      translators.put("bgtz", new BgtzTranslator()); // BGTZ Branch on Greater Than Zero
      translators.put("blez", new BlezTranslator()); // BLEZ Branch on Less Than or Equal to Zero
      translators.put("bltz", new BltzTranslator()); // BLTZ Branch on Less Than Zero
      translators.put("bltzal", new BltzalTranslator()); // BLTZAL Branch on Less Than Zero and Link
      translators.put("bne", new BneTranslator()); // BNE Branch on Not Equal
      translators.put("j", new JTranslator()); // J Jump
      translators.put("jal", new JalTranslator()); // JAL Jump and Link
      translators.put("jalr", new JalrTranslator()); // JALR Jump and Link Register
      translators.put("jalr.hb", new JalrDotHbTranslator());// JALR.HB Jump and Link Register with
                                                            // Hazard Barrier Release 2 Only
      translators.put("jr", new JrTranslator()); // JR Jump Register
      translators.put("jr.hb", new JrDotHbTranslator()); // JR.HB Jump Register with Hazard Barrier

      /*
       * CPU Instruction Control Instructions
       */

      translators.put("ehb", new EhbTranslator()); // EHB Execution Hazard Barrier Release 2 Only
      translators.put("nop", new NopTranslator()); // NOP No Operation
      translators.put("pause", new PauseTranslator()); // PAUSE Wait for LLBit to Clear Release 2.1
                                                       // Only
      translators.put("ssnop", new SsnopTranslator()); // SSNOP Superscalar No Operation

      /*
       * CPU Load, Store, and Memory Control Instructions
       */

      translators.put("lb", new LbTranslator()); // LB Load Byte
      translators.put("lbu", new LbuTranslator()); // LBU Load Byte Unsigned
      translators.put("lh", new LhTranslator()); // LH Load Halfword
      translators.put("lhu", new LhuTranslator()); // LHU Load Halfword Unsigned
      translators.put("ll", new LlTranslator()); // LL Load Linked Word
      translators.put("lw", new LwTranslator()); // LW Load Word
      translators.put("lwl", new LwlTranslator()); // LWL Load Word Left
      translators.put("lwr", new LwrTranslator()); // LWR Load Word Right
      translators.put("pref", new PrefTranslator()); // PREF Prefetch
      translators.put("sb", new SbTranslator()); // SB Store Byte
      translators.put("sc", new ScTranslator()); // SC Store Conditional Word
      translators.put("sh", new ShTranslator()); // SH Store Halfword
      translators.put("sw", new SwTranslator()); // SW Store Word
      translators.put("swl", new SwlTranslator()); // SWL Store Word Left
      translators.put("swr", new SwrTranslator()); // SWR Store Word Right
      translators.put("sync", new SyncTranslator()); // SYNC Synchronize Shared Memory
      translators.put("synci", new SynciTranslator()); // SYNCI Synchronize Caches to Make
                                                       // Instruction Writes Effective

      /*
       * CPU Logical Instructions
       */

      translators.put("and", new AndTranslator()); // AND And
      translators.put("andi", new AndiTranslator()); // ANDI And Immediate
      translators.put("lui", new LuiTranslator()); // LUI Load Upper Immediate
      translators.put("nor", new NorTranslator()); // NOR Not Or
      translators.put("or", new OrTranslator()); // OR Or
      translators.put("ori", new OriTranslator()); // ORI Or Immediate
      translators.put("xor", new XorTranslator()); // XOR Exclusive Or
      translators.put("xori", new XoriTranslator()); // XORI Exclusive Or Immediate

      /*
       * CPU Insert/Extract Instructions
       */

      translators.put("ext", new ExtTranslator()); // EXT Extract Bit Field Release 2 Only
      translators.put("ins", new InsTranslator()); // INS Insert Bit Field Release 2 Only
      translators.put("wsbh", new WsbhTranslator()); // WSBH Word Swap Bytes Within Halfwords
                                                     // Release 2 Only

      /*
       * CPU Move Instructions
       */

      translators.put("mfhi", new MfhiTranslator()); // MFHI Move From HI Register
      translators.put("mflo", new MfloTranslator()); // MFLO Move From LO Register
      translators.put("movf", new MovfTranslator()); // MOVF Move Conditional on Floating Point
                                                     // False
      translators.put("movn", new MovnTranslator()); // MOVN Move Conditional on Not Zero
      translators.put("movt", new MovtTranslator()); // MOVT Move Conditional on Floating Point True
      translators.put("movz", new MovzTranslator()); // MOVZ Move Conditional on Zero
      translators.put("mthi", new MthiTranslator()); // MTHI Move To HI Register
      translators.put("mtlo", new MtloTranslator()); // MTLO Move To LO Register
      translators.put("rdhwr", new RdhwrTranslator()); // RDHWR Read Hardware Register

      /*
       * CPU Shift Instructions
       */

      translators.put("rotr", new RotrTranslator()); // ROTR Rotate Word Right Release 2 Only
      translators.put("rotrv", new RotrvTranslator()); // ROTRV Rotate Word Right Variable Release 2
                                                       // Only
      translators.put("sll", new SllTranslator()); // SLL Shift Word Left Logical
      translators.put("sllv", new SllvTranslator()); // SLLV Shift Word Left Logical Variable
      translators.put("sra", new SraTranslator()); // SRA Shift Word Right Arithmetic
      translators.put("srav", new SravTranslator()); // SRAV Shift Word Right Arithmetic Variable
      translators.put("srl", new SrlTranslator()); // SRL Shift Word Right Logical
      translators.put("srlv", new SrlvTranslator()); // SRLV Shift Word Right Logical Variable

      /*
       * CPU Trap Instructions
       */

      translators.put("break", new BreakTranslator()); // BREAK Breakpoint
      translators.put("syscall", new SyscallTranslator());// SYSCALL System Call
      translators.put("teq", new TeqTranslator()); // TEQ Trap if Equal
      translators.put("teqi", new TeqiTranslator()); // TEQI Trap if Equal Immediate
      translators.put("tge", new TgeTranslator()); // TGE Trap if Greater or Equal
      translators.put("tgei", new TgeiTranslator()); // TGEI Trap if Greater of Equal Immediate
      translators.put("tgeiu", new TgeiuTranslator()); // TGEIU Trap if Greater or Equal Immediate
                                                       // Unsigned
      translators.put("tgeu", new TgeuTranslator()); // TGEU Trap if Greater or Equal Unsigned
      translators.put("tlt", new TltTranslator()); // TLT Trap if Less Than
      translators.put("tlti", new TltiTranslator()); // TLTI Trap if Less Than Immediate
      translators.put("tltiu", new TltiuTranslator()); // TLTIU Trap if Less Than Immediate Unsigned
      translators.put("tltu", new TltuTranslator()); // TLTU Trap if Less Than Unsigned
      translators.put("tne", new TneTranslator()); // TNE Trap if Not Equal
      translators.put("tnei", new TneiTranslator()); // TNEI Trap if Not Equal Immediate

      /*
       * Obsolete CPU Branch Instructions
       */

      translators.put("beql", new BeqlTranslator()); // BEQL Branch on Equal Likely
      translators.put("bgezall", new BgezallTranslator());// BGEZALL Branch on Greater Than or Equal
                                                          // to Zero and Link Likely
      translators.put("bgezl", new BgezlTranslator()); // BGEZL Branch on Greater Than or Equal to
                                                       // Zero Likely
      translators.put("bgtzl", new BgtzlTranslator()); // BGTZL Branch on Greater Than Zero Likely
      translators.put("blezl", new BlezlTranslator()); // BLEZL Branch on Less Than or Equal to Zero
                                                       // Likely
      translators.put("bltzall", new BltzallTranslator());// BLTZALL Branch on Less Than Zero and
                                                          // Link Likely
      translators.put("bltzl", new BltzlTranslator()); // BLTZL Branch on Less Than Zero Likely
      translators.put("bnel", new BnelTranslator()); // BNEL Branch on Not Equal Likely

      // Simplified instructions
      translators.put("la", new LaTranslator());
      translators.put("li", new LiTranslator());
      translators.put("move", new MoveTranslator());
      translators.put("beqz", new BeqzTranslator());
      translators.put("beqzl", new BeqzlTranslator());
      translators.put("bnez", new BnezTranslator());

      /*
       * FPU Arithmetic Instructions
       */

      // ABS.fmt Floating Point Absolute Value
      // ADD.fmt Floating Point Add
      // DIV.fmt Floating Point Divide
      // MADD.fmt Floating Point Multiply Add
      // MSUB.fmt Floating Point Multiply Subtract
      // MUL.fmt Floating Point Multiply
      // NEG.fmt Floating Point Negate
      // NMADD.fmt Floating Point Negative Multiply Add
      // NMSUB.fmt Floating Point Negative Multiply Subtract
      // RECIP.fmt Reciprocal Approximation
      // RSQRT.fmt Reciprocal Square Root Approximation
      // SQRT.fmt Floating Point Square Root
      // SUB.fmt Floating Point Subtract

      /*
       * FPU Branch Instructions
       */

      // BC1F Branch on FP False
      // BC1T Branch on FP True

      /*
       * FPU Compare Instructions
       */

      // C.cond.fmt Floating Point Compare

      /*
       * FPU Convert Instructions
       */

      // ALNV.PS Floating Point Align Variable
      // CEIL.L.fmt Floating Point Ceiling Convert to Long Fixed Point 64-bit FPU Only
      // CEIL.W.fmt Floating Point Ceiling Convert to Word Fixed Point
      // CVT.D.fmt Floating Point Convert to Double Floating Point
      // CVT.L.fmt Floating Point Convert to Long Fixed Point 64-bit FPU Only
      // CVT.PS.S Floating Point Convert Pair to Paired Single 64-bit FPU Only
      // CVT.S.PL Floating Point Convert Pair Lower to Single Floating Point 64-bit FPU Only
      // CVT.S.PU Floating Point Convert Pair Upper to Single Floating Point 64-bit FPU Only
      // CVT.S.fmt Floating Point Convert to Single Floating Point
      // CVT.W.fmt Floating Point Convert to Word Fixed Point
      // FLOOR.L.fmt Floating Point Floor Convert to Long Fixed Point 64-bit FPU Only
      // FLOOR.W.fmt Floating Point Floor Convert to Word Fixed Point
      // PLL.PS Pair Lower Lower 64-bit FPU Only
      // PLU.PS Pair Lower Upper 64-bit FPU Only
      // PUL.PS Pair Upper Lower 64-bit FPU Only
      // PUU.PS Pair Upper Upper 64-bit FPU Only
      // ROUND.L.fmt Floating Point Round to Long Fixed Point 64-bit FPU Only
      // ROUND.W.fmt Floating Point Round to Word Fixed Point
      // TRUNC.L.fmt Floating Point Truncate to Long Fixed Point 64-bit FPU Only
      // TRUNC.W.fmt Floating Point Truncate to Word Fixed Point

      /*
       * FPU Load, Store, and Memory Control Instructions
       */

      // LDC1 Load Doubleword to Floating Point
      // LDXC1 Load Doubleword Indexed to Floating Point 64-bit FPU Only
      // LUXC1 Load Doubleword Indexed Unaligned to Floating Point 64-bit FPU Only
      // LWC1 Load Word to Floating Point
      // LWXC1 Load Word Indexed to Floating Point 64-bit FPU Only
      // PREFX Prefetch Indexed
      // SDC1 Store Doubleword from Floating Point
      // SDXC1 Store Doubleword Indexed from Floating Point 64-bit FPU Only
      // SUXC1 Store Doubleword Indexed Unaligned from Floating Point 64-bit
      // SWC1 Store Word from Floating Point
      // SWXC1 Store Word Indexed from Floating Point

      /*
       * FPU Move Instructions
       */

      // CFC1 Move Control Word from Floating Point
      // CTC1 Move Control Word to Floating Point
      // MFC1 Move Word from Floating Point
      // MFHC1 Move Word from High Half of Floating Point Register Release 2 Only
      // MOV.fmt Floating Point Move
      // MOVF.fmt Floating Point Move Conditional on Floating Point False
      // MOVN.fmt Floating Point Move Conditional on Not Zero
      // MOVT.fmt Floating Point Move Conditional on Floating Point True
      // MOVZ.fmt Floating Point Move Conditional on Zero
      // MTC1 Move Word to Floating Point
      // MTHC1 Move Word to High Half of Floating Point Register Release 2 Only

      /*
       * Obsolete FPU Branch Instructions
       */

      // BC1FL Branch on FP False Likely
      // BC1TL Branch on FP True Likely

      /*
       * Coprocessor Branch Instructions
       */

      // BC2F Branch on COP2 False
      // BC2T Branch on COP2 True

      /*
       * Coprocessor Execute Instructions
       */

      // COP2 Coprocessor Operation to Coprocessor 2

      /*
       * Coprocessor Load and Store Instructions
       */

      // LDC2 Load Doubleword to Coprocessor 2
      // LWC2 Load Word to Coprocessor 2
      // SDC2 Store Doubleword from Coprocessor 2
      // SWC2 Store Word from Coprocessor 2

      /*
       * Coprocessor Move Instructions
       */

      // CFC2 Move Control Word from Coprocessor 2
      // CTC2 Move Control Word to Coprocessor 2
      // MFC2 Move Word from Coprocessor 2
      // MFHC2 Move Word from High Half of Coprocessor 2 Register Release 2 Only
      // MTC2 Move Word to Coprocessor 2
      // MTHC2 Move Word to High Half of Coprocessor 2 Register Release 2 Only

      /*
       * Obsolete Coprocessor Branch Instructions
       */

      // BC2FL Branch on COP2 False Likely
      // BC2TL Branch on COP2 True Likely

      /*
       * Privileged Instructions
       */

      // CACHE Perform Cache Operation
      // DI Disable Interrupts Release 2 Only
      // EI Enable Interrupts Release 2 Only
      // ERET Exception Return
      // MFC0 Move from Coprocessor 0
      // MTC0 Move to Coprocessor 0
      // RDPGPR Read GPR from Previous Shadow Set Release 2 Only
      // TLBP Probe TLB for Matching Entry
      // TLBR Read Indexed TLB Entry
      // TLBWI Write Indexed TLB Entry
      // TLBWR Write Random TLB Entry
      // WAIT Enter Standby Mode
      // WRPGPR Write GPR to Previous Shadow Set Release 2 Only

      /*
       * EJTAG Instructions
       */

      // DERET Debug Exception Return
      // SDBBP Software Debug Breakpoint
    } catch (final Exception e) {
      // TODO Handle this more gracefully
    }
  }

  /**
   * Translates a MIPS instruction to REIL code
   * 
   * @param environment A valid translation environment
   * @param instruction The MIPS instruction to translate
   * 
   * @return The list of REIL instruction the MIPS instruction was translated to
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

    // final long instLength = instruction.getLength();
    if (mnemonic == null) {
      return new ArrayList<ReilInstruction>();
    }

    final IInstructionTranslator translator = translators.get(mnemonic.toLowerCase());

    if (translators.containsKey(mnemonic.toLowerCase())) {
      final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

      translator.translate(environment, instruction, instructions);

      for (final ITranslationExtension<InstructionType> extension : extensions) {
        extension.postProcess(environment, instruction, instructions);
      }

      return instructions;
    } else {
      return Lists.newArrayList(ReilHelpers.createUnknown(ReilHelpers.toReilAddress(
          instruction.getAddress()).toLong()));
    }
  }
}
