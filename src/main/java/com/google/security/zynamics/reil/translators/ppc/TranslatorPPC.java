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


/**
 * Translator class that translates PPC code to REIL code
 * 
 * opti
 * 
 */
public class TranslatorPPC<InstructionType extends IInstruction> implements
    ITranslator<InstructionType> {

  /**
   * List of translator for all opcodes
   */
  private static HashMap<String, IInstructionTranslator> translators =
      new HashMap<String, IInstructionTranslator>();

  /**
   * Initializes the list of translators
   */
  static {
    try {
      translators.put("addc.", new AddcDotTranslator());
      translators.put("addco.", new AddcoDotTranslator());
      translators.put("addco", new AddcoTranslator());
      translators.put("addc", new AddcTranslator());
      translators.put("add.", new AddDotTranslator());
      translators.put("adde.", new AddeDotTranslator());
      translators.put("addeo.", new AddeoDotTranslator());
      translators.put("addeo", new AddeoTranslator());
      translators.put("adde", new AddeTranslator());
      translators.put("addic.", new AddicDotTranslator());
      translators.put("addic", new AddicTranslator());
      translators.put("addis", new AddisTranslator());
      translators.put("addi", new AddiTranslator());
      translators.put("addme.", new AddmeDotTranslator());
      translators.put("addmeo.", new AddmeoDotTranslator());
      translators.put("addmeo", new AddmeoTranslator());
      translators.put("addme", new AddmeTranslator());
      translators.put("addo.", new AddoDotTranslator());
      translators.put("addo", new AddoTranslator());
      translators.put("add", new AddTranslator());
      translators.put("addze.", new AddzeDotTranslator());
      translators.put("addzeo.", new AddzeoDotTranslator());
      translators.put("addzeo", new AddzeoTranslator());
      translators.put("addze", new AddzeTranslator());
      translators.put("andc.", new AndcDotTranslator());
      translators.put("andc", new AndcTranslator());
      translators.put("and.", new AndDotTranslator());
      translators.put("andi.", new AndiDotTranslator());
      translators.put("andis.", new AndisDotTranslator());
      translators.put("and", new AndTranslator());
      translators.put("ba", new BaTranslator());
      translators.put("bca", new BcaTranslator());
      translators.put("bcctrl", new BcctrlTranslator());
      translators.put("bcctr", new BcctrTranslator());
      translators.put("bcla", new BclaTranslator());
      translators.put("bclrl", new BclrlTranslator());
      translators.put("bclr", new BclrTranslator());
      translators.put("bcl", new BclTranslator());
      translators.put("bc", new BcTranslator());
      translators.put("bctrl", new BctrlTranslator());
      translators.put("bctr", new BctrTranslator());
      translators.put("bdnza", new BdnzaTranslator());
      translators.put("bdnzfa", new BdnzfaTranslator());
      translators.put("bdnzfla", new BdnzflaTranslator());
      translators.put("bdnzflrl", new BdnzflrlTranslator());
      translators.put("bdnzflr", new BdnzflrTranslator());
      translators.put("bdnzfl", new BdnzflTranslator());
      translators.put("bdnzf", new BdnzfTranslator());
      translators.put("bdnzla", new BdnzlaTranslator());
      translators.put("bdnzlrl", new BdnzlrlTranslator());
      translators.put("bdnzlr", new BdnzlrTranslator());
      translators.put("bdnzl", new BdnzlTranslator());
      translators.put("bdnzta", new BdnztaTranslator());
      translators.put("bdnztla", new BdnztlaTranslator());
      translators.put("bdnztlrl", new BdnztlrlTranslator());
      translators.put("bdnztlr", new BdnztlrTranslator());
      translators.put("bdnztl", new BdnztlTranslator());
      translators.put("bdnz", new BdnzTranslator());
      translators.put("bdnz+", new BdnzTranslator());
      translators.put("bdnz-", new BdnzTranslator());
      translators.put("bdnzt", new BdnztTranslator());
      translators.put("bdza", new BdzaTranslator());
      translators.put("bdzfa", new BdzfaTranslator());
      translators.put("bdzfla", new BdzflaTranslator());
      translators.put("bdzflrl", new BdzflrlTranslator());
      translators.put("bdzflr", new BdzflrTranslator());
      translators.put("bdzfl", new BdzflTranslator());
      translators.put("bdzf", new BdzfTranslator());
      translators.put("bdzla", new BdzlaTranslator());
      translators.put("bdzlrl", new BdzlrlTranslator());
      translators.put("bdzlr", new BdzlrTranslator());
      translators.put("bdzl", new BdzlTranslator());
      translators.put("bdzta", new BdztaTranslator());
      translators.put("bdztla", new BdztlaTranslator());
      translators.put("bdztlrl", new BdztlrlTranslator());
      translators.put("bdztlr", new BdztlrTranslator());
      translators.put("bdztl", new BdztlTranslator());
      translators.put("bdz", new BdzTranslator());
      translators.put("bdzt", new BdztTranslator());
      translators.put("beqa", new BeqaTranslator());
      translators.put("beqctrl", new BeqctrlTranslator());
      translators.put("beqctr", new BeqctrTranslator());
      translators.put("beqla", new BeqlaTranslator());
      translators.put("beqlrl", new BeqlrlTranslator());
      translators.put("beqlr", new BeqlrTranslator());
      translators.put("beqlr+", new BeqlrTranslator());
      translators.put("beqlr-", new BeqlrTranslator());
      translators.put("beql", new BeqlTranslator());
      translators.put("beq", new BeqTranslator());
      translators.put("beq+", new BeqTranslator());
      translators.put("beq-", new BeqTranslator());
      translators.put("bfa", new BfaTranslator());
      translators.put("bfctrl", new BfctrlTranslator());
      translators.put("bfctr", new BfctrTranslator());
      translators.put("bfla", new BflaTranslator());
      translators.put("bflrl", new BflrlTranslator());
      translators.put("bflr", new BflrTranslator());
      translators.put("bfl", new BflTranslator());
      translators.put("bf", new BfTranslator());
      translators.put("bgea", new BgeaTranslator());
      translators.put("bgectrl", new BgectrlTranslator());
      translators.put("bgectr", new BgectrTranslator());
      translators.put("bgela", new BgelaTranslator());
      translators.put("bgelrl", new BgelrlTranslator());
      translators.put("bgelr", new BgelrTranslator());
      translators.put("bgel", new BgelTranslator());
      translators.put("bge", new BgeTranslator());
      translators.put("bge+", new BgeTranslator());
      translators.put("bge-", new BgeTranslator());
      translators.put("bgta", new BgtaTranslator());
      translators.put("bgtctrl", new BgtctrlTranslator());
      translators.put("bgtctr", new BgtctrTranslator());
      translators.put("bgtla", new BgtlaTranslator());
      translators.put("bgtlrl", new BgtlrlTranslator());
      translators.put("bgtlr", new BgtlrTranslator());
      translators.put("bgtl", new BgtlTranslator());
      translators.put("bgt", new BgtTranslator());
      translators.put("bgt+", new BgtTranslator());
      translators.put("bgt-", new BgtTranslator());
      translators.put("bla", new BlaTranslator());
      translators.put("blea", new BleaTranslator());
      translators.put("blectrl", new BlectrlTranslator());
      translators.put("blectr", new BlectrTranslator());
      translators.put("blela", new BlelaTranslator());
      translators.put("blelrl", new BlelrlTranslator());
      translators.put("blelr", new BlelrTranslator());
      translators.put("blel", new BlelTranslator());
      translators.put("ble", new BleTranslator());
      translators.put("ble+", new BleTranslator());
      translators.put("ble-", new BleTranslator());
      translators.put("blrl", new BlrlTranslator());
      translators.put("blr", new BlrTranslator());
      translators.put("blta", new BltaTranslator());
      translators.put("bltctrl", new BltctrlTranslator());
      translators.put("bltctr", new BltctrTranslator());
      translators.put("bltla", new BltlaTranslator());
      translators.put("bltlrl", new BltlrlTranslator());
      translators.put("bltlr", new BltlrTranslator());
      translators.put("bltl", new BltlTranslator());
      translators.put("bl", new BlTranslator());
      translators.put("blt", new BltTranslator());
      translators.put("blt+", new BltTranslator());
      translators.put("blt-", new BltTranslator());
      translators.put("bnea", new BneaTranslator());
      translators.put("bnectrl", new BnectrlTranslator());
      translators.put("bnectr", new BnectrTranslator());
      translators.put("bnela", new BnelaTranslator());
      translators.put("bnelrl", new BnelrlTranslator());
      translators.put("bnelr", new BnelrTranslator());
      translators.put("bnelr+", new BnelrTranslator());
      translators.put("bnelr-", new BnelrTranslator());
      translators.put("bnel", new BnelTranslator());
      translators.put("bne", new BneTranslator());
      translators.put("bne+", new BneTranslator());
      translators.put("bne-", new BneTranslator());
      translators.put("bnga", new BngaTranslator());
      translators.put("bngctrl", new BngctrlTranslator());
      translators.put("bngctr", new BngctrTranslator());
      translators.put("bngla", new BnglaTranslator());
      translators.put("bnglrl", new BnglrlTranslator());
      translators.put("bnglr", new BnglrTranslator());
      translators.put("bngl", new BnglTranslator());
      translators.put("bng", new BngTranslator());
      translators.put("bnla", new BnlaTranslator());
      translators.put("bnlctrl", new BnlctrlTranslator());
      translators.put("bnlctr", new BnlctrTranslator());
      translators.put("bnlla", new BnllaTranslator());
      translators.put("bnllrl", new BnllrlTranslator());
      translators.put("bnllr", new BnllrTranslator());
      translators.put("bnll", new BnllTranslator());
      translators.put("bnl", new BnlTranslator());
      translators.put("bnsa", new BnsaTranslator());
      translators.put("bnsctrl", new BnsctrlTranslator());
      translators.put("bnsctr", new BnsctrTranslator());
      translators.put("bnsla", new BnslaTranslator());
      translators.put("bnslrl", new BnslrlTranslator());
      translators.put("bnslr", new BnslrTranslator());
      translators.put("bnsl", new BnslTranslator());
      translators.put("bns", new BnsTranslator());
      translators.put("bsoa", new BsoaTranslator());
      translators.put("bsoctrl", new BsoctrlTranslator());
      translators.put("bsoctr", new BsoctrTranslator());
      translators.put("bsola", new BsolaTranslator());
      translators.put("bsolrl", new BsolrlTranslator());
      translators.put("bsolr", new BsolrTranslator());
      translators.put("bsol", new BsolTranslator());
      translators.put("bso", new BsoTranslator());
      translators.put("bta", new BtaTranslator());
      translators.put("btctrl", new BtctrlTranslator());
      translators.put("btctr", new BtctrTranslator());
      translators.put("btla", new BtlaTranslator());
      translators.put("btlrl", new BtlrlTranslator());
      translators.put("btlr", new BtlrTranslator());
      translators.put("btl", new BtlTranslator());
      translators.put("b", new BTranslator());
      translators.put("bt", new BtTranslator());
      translators.put("clrlslwi.", new ClrlslwiDotTranslator());
      translators.put("clrlslwi", new ClrlslwiTranslator());
      translators.put("clrlwi.", new ClrlwiDotTranslator());
      translators.put("clrlwi", new ClrlwiTranslator());
      translators.put("clrrwi.", new ClrrwiDotTranslator());
      translators.put("clrrwi", new ClrrwiTranslator());
      translators.put("cmplwi", new CmplwiTranslator());
      translators.put("cmplw", new CmplwTranslator());
      translators.put("cmpwi", new CmpwiTranslator());
      translators.put("cmpw", new CmpwTranslator());
      // translators.put("cmpd", new CmpdTranslator());
      // translators.put("cmpdi", new CmpdiTranslator());
      // translators.put("cmpld", new CmpldTranslator());
      // translators.put("cmpldi", new CmpldiTranslator());
      translators.put("cntlzw.", new CntlzwDotTranslator());
      translators.put("cntlzw", new CntlzwTranslator());
      // translators.put("cntlzd.", new CntlzdDotTranslator());
      // translators.put("cntlzd", new CntlzdTranslator());
      translators.put("crandc", new CrandcTranslator());
      translators.put("crand", new CrandTranslator());
      translators.put("crclr", new CrclrTranslator());
      translators.put("creqv", new CreqvTranslator());
      translators.put("crmove", new CrmoveTranslator());
      translators.put("crnand", new CrnandTranslator());
      translators.put("crnor", new CrnorTranslator());
      translators.put("crnot", new CrnotTranslator());
      translators.put("crorc", new CrorcTranslator());
      translators.put("cror", new CrorTranslator());
      translators.put("crset", new CrsetTranslator());
      translators.put("crxor", new CrxorTranslator());
      translators.put("dcba", new DcbaTranslator());
      translators.put("dcbf", new DcbfTranslator());
      translators.put("dcbi", new DcbiTranslator());
      translators.put("dcbst", new DcbstTranslator());
      translators.put("dcbtst", new DcbtstTranslator());
      translators.put("dcbt", new DcbtTranslator());
      translators.put("dcbz", new DcbzTranslator());
      translators.put("divw.", new DivwDotTranslator());
      // translators.put("divd", new DivdTranslator());
      // translators.put("divd.", new DivdDotTranslator());
      translators.put("divwo.", new DivwoDotTranslator());
      translators.put("divwo", new DivwoTranslator());
      translators.put("divw", new DivwTranslator());
      translators.put("divwu.", new DivwuDotTranslator());
      translators.put("divwuo.", new DivwuoDotTranslator());
      translators.put("divwuo", new DivwuoTranslator());
      translators.put("divwu", new DivwuTranslator());
      translators.put("eciwx", new EciwxTranslator());
      translators.put("ecowx", new EcowxTranslator());
      translators.put("eieio", new EieioTranslator());
      translators.put("eqv.", new EqvDotTranslator());
      translators.put("eqv", new EqvTranslator());
      translators.put("extlwi.", new ExtlwiDotTranslator());
      translators.put("extlwi", new ExtlwiTranslator());
      translators.put("extrwi.", new ExtrwiDotTranslator());
      translators.put("extrwi", new ExtrwiTranslator());
      translators.put("extsb.", new ExtsbDotTranslator());
      translators.put("extsb", new ExtsbTranslator());
      translators.put("extsh.", new ExtshDotTranslator());
      translators.put("extsh", new ExtshTranslator());
      // translators.put("extsw", new ExtswTranslator());
      // translators.put("extsw.", new ExtswDotTranslator());
      translators.put("fabs.", new FabsDotTranslator());
      translators.put("fabs", new FabsTranslator());
      translators.put("fadd.", new FaddDotTranslator());
      translators.put("fadds.", new FaddsDotTranslator());
      translators.put("fadds", new FaddsTranslator());
      translators.put("fadd", new FaddTranslator());
      translators.put("fcmpo", new FcmpoTranslator());
      translators.put("fcmpu", new FcmpuTranslator());
      translators.put("fctiw.", new FctiwDotTranslator());
      translators.put("fctiw", new FctiwTranslator());
      translators.put("fctiwz.", new FctiwzDotTranslator());
      translators.put("fctiwz", new FctiwzTranslator());
      translators.put("fdiv.", new FdivDotTranslator());
      translators.put("fdivs.", new FdivsDotTranslator());
      translators.put("fdivs", new FdivsTranslator());
      translators.put("fdiv", new FdivTranslator());
      translators.put("fmadd.", new FmaddDotTranslator());
      translators.put("fmadds.", new FmaddsDotTranslator());
      translators.put("fmadds", new FmaddsTranslator());
      translators.put("fmadd", new FmaddTranslator());
      translators.put("fmr.", new FmrDotTranslator());
      translators.put("fmr", new FmrTranslator());
      translators.put("fmsub.", new FmsubDotTranslator());
      translators.put("fmsubs.", new FmsubsDotTranslator());
      translators.put("fmsubs", new FmsubsTranslator());
      translators.put("fmsub", new FmsubTranslator());
      translators.put("fmul.", new FmulDotTranslator());
      translators.put("fmuls.", new FmulsDotTranslator());
      translators.put("fmuls", new FmulsTranslator());
      translators.put("fmul", new FmulTranslator());
      translators.put("fnabs.", new FnabsDotTranslator());
      translators.put("fnabs", new FnabsTranslator());
      translators.put("fneg.", new FnegDotTranslator());
      translators.put("fneg", new FnegTranslator());
      translators.put("fnmadd.", new FnmaddDotTranslator());
      translators.put("fnmadds.", new FnmaddsDotTranslator());
      translators.put("fnmadds", new FnmaddsTranslator());
      translators.put("fnmadd", new FnmaddTranslator());
      translators.put("fnmsub.", new FnmsubDotTranslator());
      translators.put("fnmsubs.", new FnmsubsDotTranslator());
      translators.put("fnmsubs", new FnmsubsTranslator());
      translators.put("fnmsub", new FnmsubTranslator());
      translators.put("fres.", new FresDotTranslator());
      translators.put("fres", new FresTranslator());
      translators.put("frsp.", new FrspDotTranslator());
      translators.put("frsp", new FrspTranslator());
      translators.put("frsqrte.", new FrsqrteDotTranslator());
      translators.put("frsqrte", new FrsqrteTranslator());
      translators.put("fsel.", new FselDotTranslator());
      translators.put("fsel", new FselTranslator());
      translators.put("fsqrt.", new FsqrtDotTranslator());
      translators.put("fsqrts.", new FsqrtsDotTranslator());
      translators.put("fsqrts", new FsqrtsTranslator());
      translators.put("fsqrt", new FsqrtTranslator());
      translators.put("fsub.", new FsubDotTranslator());
      translators.put("fsubs.", new FsubsDotTranslator());
      translators.put("fsubs", new FsubsTranslator());
      translators.put("fsub", new FsubTranslator());
      translators.put("icbi", new IcbiTranslator());
      translators.put("inslwi.", new InslwiDotTranslator());
      translators.put("inslwi", new InslwiTranslator());
      translators.put("insrwi.", new InsrwiDotTranslator());
      translators.put("insrwi", new InsrwiTranslator());
      translators.put("isync", new IsyncTranslator());
      translators.put("lbz", new LbzTranslator());
      translators.put("lbzu", new LbzuTranslator());
      translators.put("lbzux", new LbzuxTranslator());
      translators.put("lbzx", new LbzxTranslator());
      // translators.put("ld", new LdTranslator());
      // translators.put("ldarx", new LdarxTranslator());
      // translators.put("ldu", new LduTranslator());
      // translators.put("ldx", new LdxTranslator());
      // translators.put("lwarx", new LwarxTranslator());
      translators.put("lfd", new LfdTranslator());
      translators.put("lfdu", new LfduTranslator());
      translators.put("lfdux", new LfduxTranslator());
      translators.put("lfdx", new LfdxTranslator());
      translators.put("lfs", new LfsTranslator());
      translators.put("lfsu", new LfsuTranslator());
      translators.put("lfsux", new LfsuxTranslator());
      translators.put("lfsx", new LfsxTranslator());
      translators.put("lha", new LhaTranslator());
      translators.put("lhau", new LhauTranslator());
      translators.put("lhaux", new LhauxTranslator());
      translators.put("lhax", new LhaxTranslator());
      translators.put("lhbrx", new LhbrxTranslator());
      translators.put("lhz", new LhzTranslator());
      translators.put("lhzu", new LhzuTranslator());
      translators.put("lhzux", new LhzuxTranslator());
      translators.put("lhzx", new LhzxTranslator());
      translators.put("lis", new LisTranslator());
      translators.put("li", new LiTranslator());
      translators.put("lmw", new LmwTranslator());
      translators.put("lswi", new LswiTranslator());
      // translators.put("lswx", new LswxTranslator());
      // translators.put("lwarx", new LwarxTranslator());
      translators.put("lwbrx", new LwbrxTranslator());
      translators.put("lwz", new LwzTranslator());
      translators.put("lwzu", new LwzuTranslator());
      translators.put("lwzux", new LwzuxTranslator());
      translators.put("lwzx", new LwzxTranslator());
      translators.put("mcrfs", new McrfsTranslator());
      translators.put("mcrf", new McrfTranslator());
      translators.put("mcrxr", new McrxrTranslator());
      translators.put("mfcr", new MfcrTranslator());
      translators.put("mfctr", new MfctrTranslator());
      translators.put("mfdar", new MfdarTranslator());
      translators.put("mfdec", new MfdecTranslator());
      translators.put("mfdsisr", new MfdsisrTranslator());
      translators.put("mfpvr", new MfpvrTranslator());
      translators.put("mfsprg0", new Mfsprg0Translator());
      translators.put("mfsrr0", new Mfsrr0Translator());
      translators.put("mfsrr1", new Mfsrr1Translator());
      // translators.put("mtmsrd", new MtmsrdTranslator());
      translators.put("mffs.", new MffsDotTranslator());
      translators.put("mffs", new MffsTranslator());
      translators.put("mflr", new MflrTranslator());
      translators.put("mfmsr", new MfmsrTranslator());
      translators.put("mfocrf", new MfocrfTranslator());
      translators.put("mfspr", new MfsprTranslator());
      translators.put("mfsrin", new MfsrinTranslator());
      translators.put("mfsr", new MfsrTranslator());
      translators.put("mftb", new MftbTranslator());
      translators.put("mftbu", new MftbuTranslator());
      translators.put("mfxer", new MfxerTranslator());
      translators.put("mr.", new MrDotTranslator());
      translators.put("mr", new MrTranslator());
      translators.put("mtcrf", new MtcrfTranslator());
      translators.put("mtcr", new MtcrTranslator());
      translators.put("mtctr", new MtctrTranslator());
      translators.put("mtfsb0.", new Mtfsb0DotTranslator());
      translators.put("mtfsb0", new Mtfsb0Translator());
      translators.put("mtfsb1.", new Mtfsb1DotTranslator());
      translators.put("mtfsb1", new Mtfsb1Translator());
      translators.put("mtfsf.", new MtfsfDotTranslator());
      translators.put("mtfsfi.", new MtfsfiDotTranslator());
      translators.put("mtfsfi", new MtfsfiTranslator());
      translators.put("mtfsf", new MtfsfTranslator());
      translators.put("mtlr", new MtlrTranslator());
      translators.put("mtmsr", new MtmsrTranslator());
      translators.put("mtocrf", new MtocrfTranslator());
      translators.put("mtspr", new MtsprTranslator());

      translators.put("mtdec", new MtdecTranslator());
      translators.put("mtsprg0", new Mtsprg0Translator());
      translators.put("mtsrr0", new Mtsrr0Translator());
      translators.put("mtsrr1", new Mtsrr1Translator());
      translators.put("mtsrin", new MtsrinTranslator());
      translators.put("mtsr", new MtsrTranslator());
      translators.put("mtxer", new MtxerTranslator());

      // translators.put("mulhd", new MulhdTranslator());
      // translators.put("mulhd.", new MulhdDotTranslator());
      // translators.put("mulhdu", new MulhdTranslator());
      // translators.put("mulhdu.", new MulhdDotTranslator());
      // translators.put("mulld", new MulhdTranslator());
      // translators.put("mulld.", new MulhdDotTranslator());
      translators.put("mulhw.", new MulhwDotTranslator());
      translators.put("mulhw", new MulhwTranslator());
      translators.put("mulhwu.", new MulhwuDotTranslator());
      translators.put("mulhwu", new MulhwuTranslator());
      translators.put("mulli", new MulliTranslator());
      translators.put("mullw.", new MullwDotTranslator());
      translators.put("mullwo.", new MullwoDotTranslator());
      translators.put("mullwo", new MullwoTranslator());
      translators.put("mullw", new MullwTranslator());

      translators.put("nand.", new NandDotTranslator());
      translators.put("nand", new NandTranslator());
      translators.put("neg.", new NegDotTranslator());
      translators.put("nego.", new NegoDotTranslator());
      translators.put("nego", new NegoTranslator());
      translators.put("neg", new NegTranslator());
      translators.put("nop", new NopTranslator());
      translators.put("nor.", new NorDotTranslator());
      translators.put("nor", new NorTranslator());
      translators.put("not", new NotTranslator());
      translators.put("orc.", new OrcDotTranslator());
      translators.put("orc", new OrcTranslator());
      translators.put("or.", new OrDotTranslator());
      translators.put("oris", new OrisTranslator());
      translators.put("ori", new OriTranslator());
      translators.put("or", new OrTranslator());
      translators.put("rfi", new RfiTranslator());

      // translators.put("rldic", new RldicTranslator());
      // translators.put("rldicl", new RldiclTranslator());
      // translators.put("rldicr", new RldicrTranslator());
      // translators.put("rldimi", new RldimiTranslator());

      translators.put("rlwimi.", new RlwimiDotTranslator());
      translators.put("rlwimi", new RlwimiTranslator());
      translators.put("rlwinm.", new RlwinmDotTranslator());
      translators.put("rlwinm", new RlwinmTranslator());
      translators.put("rlwnm.", new RlwnmDotTranslator());
      translators.put("rlwnm", new RlwnmTranslator());
      translators.put("rotlw.", new RotlwDotTranslator());
      translators.put("rotlwi.", new RotlwiDotTranslator());
      translators.put("rotlwi", new RotlwiTranslator());
      translators.put("rotlw", new RotlwTranslator());
      translators.put("rotrwi.", new RotrwiDotTranslator());
      translators.put("rotrwi", new RotrwiTranslator());
      translators.put("sc", new ScTranslator());

      // translators.put("sld", new SldTranslator());
      // translators.put("sld.", new SldDotTranslator());
      // translators.put("sldi", new SldiTranslator());
      // translators.put("sldi.", new SldiDotTranslator());

      translators.put("slw", new SlwTranslator());
      translators.put("slw.", new SlwDotTranslator());
      translators.put("slwi", new SlwiTranslator());
      translators.put("slwi.", new SlwiDotTranslator());

      // translators.put("srad", new SradTranslator());
      // translators.put("srad.", new SradDotTranslator());
      // translators.put("sradi.", new SradiDotTranslator());
      // translators.put("sradi", new SradiTranslator());

      translators.put("sraw", new SrawTranslator());
      translators.put("sraw.", new SrawDotTranslator());
      translators.put("srawi.", new SrawiDotTranslator());
      translators.put("srawi", new SrawiTranslator());

      // translators.put("srd", new SrdTranslator());
      // translators.put("srd.", new SrdDotTranslator());
      // translators.put("srdi.", new SrdiDotTranslator());
      // translators.put("srdi", new SrdiTranslator());

      translators.put("srw", new SrwTranslator());
      translators.put("srw.", new SrwDotTranslator());
      translators.put("srwi.", new SrwiDotTranslator());
      translators.put("srwi", new SrwiTranslator());

      translators.put("stb", new StbTranslator());
      translators.put("stbu", new StbuTranslator());
      translators.put("stbux", new StbuxTranslator());
      translators.put("stbx", new StbxTranslator());
      translators.put("stfd", new StfdTranslator());
      translators.put("stfdu", new StfduTranslator());
      translators.put("stfdux", new StfduxTranslator());
      translators.put("stfdx", new StfdxTranslator());
      translators.put("stfiwx", new StfiwxTranslator());
      translators.put("stfs", new StfsTranslator());
      translators.put("stfsu", new StfsuTranslator());
      translators.put("stfsux", new StfsuxTranslator());
      translators.put("stfsx", new StfsxTranslator());
      translators.put("sthbrx", new SthbrxTranslator());
      translators.put("sth", new SthTranslator());
      translators.put("sthu", new SthuTranslator());
      translators.put("sthux", new SthuxTranslator());
      translators.put("sthx", new SthxTranslator());
      translators.put("stmw", new StmwTranslator());
      translators.put("stswi", new StswiTranslator());
      // translators.put("stswx", new StswxTranslator());
      translators.put("stwbrx", new StwbrxTranslator());
      // translators.put("stwcx.", new StwcxDotTranslator());
      translators.put("stw", new StwTranslator());
      translators.put("stwu", new StwuTranslator());
      translators.put("stwux", new StwuxTranslator());
      translators.put("stwx", new StwxTranslator());

      // translators.put("std", new StdTranslator());
      // translators.put("stdu", new StduTranslator());
      // translators.put("stdux", new StduxTranslator());
      // translators.put("stdx", new StdxTranslator());
      // translators.put("stdcx.", new StdcxDotTranslator());

      translators.put("subfc.", new SubfcDotTranslator());
      translators.put("subfco.", new SubfcoDotTranslator());
      translators.put("subfco", new SubfcoTranslator());
      translators.put("subfc", new SubfcTranslator());
      translators.put("subf.", new SubfDotTranslator());
      translators.put("subfe.", new SubfeDotTranslator());
      translators.put("subfeo.", new SubfeoDotTranslator());
      translators.put("subfeo", new SubfeoTranslator());
      translators.put("subfe", new SubfeTranslator());
      translators.put("subfic", new SubficTranslator());
      translators.put("subfme.", new SubfmeDotTranslator());
      translators.put("subfmeo.", new SubfmeoDotTranslator());
      translators.put("subfmeo", new SubfmeoTranslator());
      translators.put("subfme", new SubfmeTranslator());
      translators.put("subfo.", new SubfoDotTranslator());
      translators.put("subfo", new SubfoTranslator());
      translators.put("subf", new SubfTranslator());
      translators.put("subfze.", new SubfzeDotTranslator());
      translators.put("subfzeo.", new SubfzeoDotTranslator());
      translators.put("subfzeo", new SubfzeoTranslator());
      translators.put("subfze", new SubfzeTranslator());
      translators.put("sync", new SyncTranslator());
      translators.put("tlbia", new TlbiaTranslator());
      translators.put("tlbie", new TlbieTranslator());
      translators.put("tlbsync", new TlbsyncTranslator());
      translators.put("trap", new TrapTranslator());
      translators.put("tweq", new TweqTranslator());
      translators.put("twgti", new TwgtiTranslator());
      translators.put("twi", new TwiTranslator());
      translators.put("twlge", new TwlgeTranslator());
      translators.put("twllei", new TwlleiTranslator());
      translators.put("tw", new TwTranslator());
      translators.put("xor.", new XorDotTranslator());
      translators.put("xoris", new XorisTranslator());
      translators.put("xori", new XoriTranslator());
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
