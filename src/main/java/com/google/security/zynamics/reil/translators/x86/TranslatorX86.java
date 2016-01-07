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
package com.google.security.zynamics.reil.translators.x86;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilHelpers;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.translators.IInstructionTranslator;
import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.ITranslationExtension;
import com.google.security.zynamics.reil.translators.ITranslator;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Translator class that translates x86 code to REIL code
 * 
 * sp
 * 
 */
public class TranslatorX86<InstructionType extends IInstruction> implements
    ITranslator<InstructionType> {

  /**
   * List of translator for all opcodes
   */
  private static final HashMap<String, IInstructionTranslator> translators =
      new HashMap<String, IInstructionTranslator>();

  /**
   * Initializes the list of translators
   */
  static {
    try {
      translators.put("adc", new AdcTranslator());
      translators.put("add", new AddTranslator());
      translators.put("and", new AndTranslator());
      translators.put("bswap", new BswapTranslator());
      translators.put("bsf", new BsfTranslator());
      translators.put("bsr", new BsrTranslator());
      translators.put("bt", new BtTranslator());
      translators.put("btc", new BtcTranslator());
      translators.put("btr", new BtrTranslator());
      translators.put("bts", new BtsTranslator());
      translators.put("call", new CallTranslator());
      translators.put("cdq", new CdqTranslator());
      translators.put("clc", new ClcTranslator());
      translators.put("cld", new CldTranslator());
      translators.put("cli", new CliTranslator());
      translators.put("cmc", new CmcTranslator());
      translators.put("cmova", new CmovccTranslator(new AboveGenerator()));
      translators.put("cmovnb", new CmovccTranslator(new NotBelowGenerator()));
      translators.put("cmovb", new CmovccTranslator(new BelowGenerator()));
      translators.put("cmovbe", new CmovccTranslator(new BelowEqualGenerator()));
      translators.put("cmovz", new CmovccTranslator(new ZeroGenerator()));
      translators.put("cmovg", new CmovccTranslator(new GreaterGenerator()));
      translators.put("cmovge", new CmovccTranslator(new GreaterEqualGenerator()));
      translators.put("cmovl", new CmovccTranslator(new LessGenerator()));
      translators.put("cmovle", new CmovccTranslator(new LessEqualGenerator()));
      translators.put("cmovnz", new CmovccTranslator(new NotZeroGenerator()));
      translators.put("cmovno", new CmovccTranslator(new NotOverflowGenerator()));
      translators.put("cmovnp", new CmovccTranslator(new NotParityGenerator()));
      translators.put("cmovns", new CmovccTranslator(new NotSignGenerator()));
      translators.put("cmovo", new CmovccTranslator(new OverflowGenerator()));
      translators.put("cmovp", new CmovccTranslator(new ParityGenerator()));
      translators.put("cmovs", new CmovccTranslator(new SignGenerator()));
      translators.put("cmp", new CmpTranslator());
      translators.put("cmpsb", new CmpsbTranslator());
      translators.put("cmpsw", new CmpswTranslator());
      translators.put("cmpsd", new CmpsdTranslator());
      translators.put("cmpxchg", new CmpxchgTranslator());
      translators.put("cmpxchg8b", new Cmpxchg8bTranslator());
      translators.put("cwd", new CwdTranslator());
      translators.put("cwde", new CwdeTranslator());
      translators.put("dec", new DecTranslator());
      translators.put("div", new DivTranslator());
      translators.put("imul", new ImulTranslator());
      translators.put("idiv", new IdivTranslator());
      translators.put("inc", new IncTranslator());
      translators.put("ja", new JccTranslator(new AboveGenerator()));
      translators.put("jae", new JccTranslator(new NotBelowGenerator()));
      translators.put("jb", new JccTranslator(new BelowGenerator()));
      translators.put("jbe", new JccTranslator(new BelowEqualGenerator()));
      translators.put("jcxz", new JccTranslator(new EcxZeroGenerator()));
      translators.put("jecxz", new JccTranslator(new CxZeroGenerator()));
      translators.put("je", new JccTranslator(new ZeroGenerator()));
      translators.put("jg", new JccTranslator(new GreaterGenerator()));
      translators.put("jge", new JccTranslator(new GreaterEqualGenerator()));
      translators.put("jl", new JccTranslator(new LessGenerator()));
      translators.put("jle", new JccTranslator(new LessEqualGenerator()));
      translators.put("jmp", new JmpTranslator());
      translators.put("jnb", new JccTranslator(new NotBelowGenerator()));
      translators.put("jne", new JccTranslator(new NotZeroGenerator()));
      translators.put("jno", new JccTranslator(new NotOverflowGenerator()));
      translators.put("jnp", new JccTranslator(new NotParityGenerator()));
      translators.put("jns", new JccTranslator(new NotSignGenerator()));
      translators.put("jnz", new JccTranslator(new NotZeroGenerator()));
      translators.put("jo", new JccTranslator(new OverflowGenerator()));
      translators.put("jp", new JccTranslator(new ParityGenerator()));
      translators.put("js", new JccTranslator(new SignGenerator()));
      translators.put("jz", new JccTranslator(new ZeroGenerator()));
      translators.put("lahf", new LahfTranslator());
      translators.put("lea", new LeaTranslator());
      translators.put("leave", new LeaveTranslator());
      // We work with LOCK prefixes in the same way as with REP prefixes. The
      // LOCK prefix is only defined for ADD, ADC, AND, BTC, BTR, BTS, CMPXCHG, 
      // CMPXCH8B, DEC, INC, NEG, NOT, OR, SBB, SUB, XOR, XADD, and XCHG, so we
      // add handlers for them here.
      translators.put("lock add", new AddTranslator());
      translators.put("lock adc", new AdcTranslator());
      translators.put("lock and", new AndTranslator());
      translators.put("lock btc", new BtcTranslator());
      translators.put("lock btr", new BtrTranslator());
      translators.put("lock bts", new BtsTranslator());
      translators.put("lock cmpxchg", new CmpxchgTranslator());
      translators.put("lock cmpxchg8b", new Cmpxchg8bTranslator());
      translators.put("lock dec", new DecTranslator());
      translators.put("lock inc", new IncTranslator());
      translators.put("lock neg", new NegTranslator());
      translators.put("lock not", new NotTranslator());
      translators.put("lock or", new OrTranslator());
      translators.put("lock sbb", new SbbTranslator());
      translators.put("lock sub", new SubTranslator());
      translators.put("lock xor", new XorTranslator());
      translators.put("lock xadd", new XaddTranslator());
      translators.put("lock xchg", new XchgTranslator());
      translators.put("lodsb", new LodsbTranslator());
      translators.put("lodsw", new LodswTranslator());
      translators.put("lodsd", new LodsdTranslator());
      translators.put("loop", new LoopTranslator());
      translators.put("loope", new LoopeTranslator());
      translators.put("loopne", new LoopneTranslator());
      translators.put("mov", new MovTranslator());
      translators.put("movsb", new MovsbTranslator());
      translators.put("movsw", new MovswTranslator());
      translators.put("movsd", new MovsdTranslator());
      translators.put("movsx", new MovsxTranslator());
      translators.put("movzx", new MovzxTranslator());
      translators.put("mul", new MulTranslator());
      translators.put("or", new OrTranslator());
      translators.put("neg", new NegTranslator());
      translators.put("nop", new NopTranslator());
      translators.put("not", new NotTranslator());
      translators.put("pop", new PopTranslator());
      translators.put("popa", new PopaTranslator());
      translators.put("popaw", new PopawTranslator());
      translators.put("popf", new PopfTranslator());
      translators.put("popfw", new PopfwTranslator());
      translators.put("push", new PushTranslator());
      translators.put("pusha", new PushaTranslator());
      translators.put("pushaw", new PushawTranslator());
      translators.put("pushf", new PushfTranslator());
      translators.put("pushfw", new PushfwTranslator());
      translators.put("rep lodsb", new RepTranslator(new LodsGenerator(), OperandSize.BYTE));
      translators.put("rep lodsw", new RepTranslator(new LodsGenerator(), OperandSize.WORD));
      translators.put("rep lodsd", new RepTranslator(new LodsGenerator(), OperandSize.DWORD));
      translators.put("rep movsb", new RepTranslator(new MovsGenerator(), OperandSize.BYTE));
      translators.put("rep movsw", new RepTranslator(new MovsGenerator(), OperandSize.WORD));
      translators.put("rep movsd", new RepTranslator(new MovsGenerator(), OperandSize.DWORD));
      translators.put("rep stosb", new RepTranslator(new StosGenerator(), OperandSize.BYTE));
      translators.put("rep stosw", new RepTranslator(new StosGenerator(), OperandSize.WORD));
      translators.put("rep stosd", new RepTranslator(new StosGenerator(), OperandSize.DWORD));
      translators.put("repe cmpsb", new RepeTranslator(new CmpsGenerator(), OperandSize.BYTE));
      translators.put("repe cmpsw", new RepeTranslator(new CmpsGenerator(), OperandSize.WORD));
      translators.put("repe cmpsd", new RepeTranslator(new CmpsGenerator(), OperandSize.DWORD));
      translators.put("repe scasb", new RepeTranslator(new ScasGenerator(), OperandSize.BYTE));
      translators.put("repe scasw", new RepeTranslator(new ScasGenerator(), OperandSize.WORD));
      translators.put("repe scasd", new RepeTranslator(new ScasGenerator(), OperandSize.DWORD));
      translators.put("repne cmpsb", new RepneTranslator(new CmpsGenerator(), OperandSize.BYTE));
      translators.put("repne cmpsw", new RepneTranslator(new CmpsGenerator(), OperandSize.WORD));
      translators.put("repne cmpsd", new RepneTranslator(new CmpsGenerator(), OperandSize.DWORD));
      translators.put("repne scasb", new RepneTranslator(new ScasGenerator(), OperandSize.BYTE));
      translators.put("repne scasw", new RepneTranslator(new ScasGenerator(), OperandSize.WORD));
      translators.put("repne scasd", new RepneTranslator(new ScasGenerator(), OperandSize.DWORD));
      translators.put("ret", new RetnTranslator());
      translators.put("retn", new RetnTranslator());
      translators.put("rcl", new RclTranslator());
      translators.put("rcr", new RcrTranslator());
      translators.put("rol", new RolTranslator());
      translators.put("ror", new RorTranslator());
      translators.put("sahf", new SahfTranslator());
      translators.put("sal", new ShlTranslator());
      translators.put("sar", new SarTranslator());
      translators.put("sbb", new SbbTranslator());
      translators.put("scasb", new ScasbTranslator());
      translators.put("scasw", new ScaswTranslator());
      translators.put("scasd", new ScasdTranslator());
      translators.put("setalc", new SetalcTranslator());
      translators.put("setb", new SetccTranslator(new BelowGenerator()));
      translators.put("setbe", new SetccTranslator(new BelowEqualGenerator()));
      translators.put("sete", new SetccTranslator(new ZeroGenerator()));
      translators.put("setl", new SetccTranslator(new LessGenerator()));
      translators.put("setle", new SetccTranslator(new LessEqualGenerator()));
      translators.put("setnb", new SetccTranslator(new NotBelowGenerator()));
      translators.put("setnbe", new SetccTranslator(new NotBelowEqualGenerator()));
      translators.put("setne", new SetccTranslator(new NotZeroGenerator()));
      translators.put("setnl", new SetccTranslator(new NotLessGenerator()));
      translators.put("setnle", new SetccTranslator(new NotLessEqualGenerator()));
      translators.put("setno", new SetccTranslator(new NotOverflowGenerator()));
      translators.put("setnp", new SetccTranslator(new NotParityGenerator()));
      translators.put("setns", new SetccTranslator(new NotSignGenerator()));
      translators.put("setnz", new SetccTranslator(new NotZeroGenerator()));
      translators.put("seto", new SetccTranslator(new OverflowGenerator()));
      translators.put("setp", new SetccTranslator(new ParityGenerator()));
      translators.put("sets", new SetccTranslator(new SignGenerator()));
      translators.put("setz", new SetccTranslator(new ZeroGenerator()));
      translators.put("shl", new ShlTranslator());
      translators.put("shr", new ShrTranslator());
      translators.put("shld", new ShldTranslator());
      translators.put("shrd", new ShrdTranslator());
      translators.put("stc", new StcTranslator());
      translators.put("std", new StdTranslator());
      translators.put("sti", new StiTranslator());
      translators.put("stosb", new StosbTranslator());
      translators.put("stosw", new StoswTranslator());
      translators.put("stosd", new StosdTranslator());
      translators.put("sub", new SubTranslator());
      translators.put("test", new TestTranslator());
      translators.put("xadd", new XaddTranslator());
      translators.put("xchg", new XchgTranslator());
      translators.put("xlat", new XlatTranslator());
      translators.put("xor", new XorTranslator());
    } catch (final Exception e) {
      // TODO Handle this more gracefully
    }
  }

  /**
   * Translates an x86 instruction to REIL code
   *
   * @param environment A valid translation environment
   * @param instruction The x86 instruction to translate
   *
   * @return The list of REIL instruction the x86 instruction was translated to
   *
   * @throws InternalTranslationException An internal translation error occured
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

    if (translators.containsKey(mnemonic)) {
      final IInstructionTranslator translator = translators.get(mnemonic);
      final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();
      translator.translate(environment, instruction, instructions);

      for (final ITranslationExtension<InstructionType> extension : extensions) {
        extension.postProcess(environment, instruction, instructions);
      }

      return instructions;
    } else if (mnemonic == null) {
      return new ArrayList<ReilInstruction>();
    } else {
      System.out.println("Unknown mnemonic: " + mnemonic);
      return Lists.newArrayList(ReilHelpers.createUnknown(ReilHelpers.toReilAddress(
          instruction.getAddress()).toLong()));
    }
  }
}
