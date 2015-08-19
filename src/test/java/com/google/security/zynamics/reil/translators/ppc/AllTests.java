/*
Copyright 2014 Google Inc. All Rights Reserved.

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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({AddcDotTranslatorTest.class, AddcoDotTranslatorTest.class,
    AddcoTranslatorTest.class, AddcTranslatorTest.class, AddDotTranslatorTest.class,
    AddeDotTranslatorTest.class, AddeoDotTranslatorTest.class, AddeoTranslatorTest.class,
    AddeTranslatorTest.class, AddicDotTranslatorTest.class, AddicTranslatorTest.class,
    AddisTranslatorTest.class, AddiTranslatorTest.class, AddmeDotTranslatorTest.class,
    AddmeoDotTranslatorTest.class, AddmeoTranslatorTest.class, AddmeTranslatorTest.class,
    AddoDotTranslatorTest.class, AddoTranslatorTest.class, AddTranslatorTest.class,
    AddzeDotTranslatorTest.class, AddzeoDotTranslatorTest.class, AddzeoTranslatorTest.class,
    AddzeTranslatorTest.class, AndcDotTranslatorTest.class, AndcTranslatorTest.class,
    AndDotTranslatorTest.class, AndiDotTranslatorTest.class, AndisDotTranslatorTest.class,
    AndTranslatorTest.class, BaTranslatorTest.class, BlaTranslatorTest.class,
    BlTranslatorTest.class, BTranslatorTest.class, ClrlwiDotTranslatorTest.class,
    ClrlslwiDotTranslatorTest.class, ClrlslwiTranslatorTest.class, ClrlwiTranslatorTest.class,
    ClrrwiDotTranslatorTest.class, ClrrwiTranslatorTest.class, CmplwiTranslatorTest.class,
    CmplwTranslatorTest.class, CmpwiTranslatorTest.class, CmpwTranslatorTest.class,
    CntlzwDotTranslatorTest.class, CntlzwTranslatorTest.class, CrandcTranslatorTest.class,
    CrandTranslatorTest.class, CrclrTranslatorTest.class, CreqvTranslatorTest.class,
    CrmoveTranslatorTest.class, CrnandTranslatorTest.class, CrnorTranslatorTest.class,
    CrnotTranslatorTest.class, CrorcTranslatorTest.class, CrorTranslatorTest.class,
    CrsetTranslatorTest.class, CrxorTranslatorTest.class, DivwDotTranslatorTest.class,
    DivwoDotTranslatorTest.class, DivwoTranslatorTest.class, DivwTranslatorTest.class,
    DivwuDotTranslatorTest.class, DivwuoDotTranslatorTest.class, DivwuoTranslatorTest.class,
    DivwuTranslatorTest.class, EqvDotTranslatorTest.class, EqvTranslatorTest.class,
    ExtlwiDotTranslatorTest.class, ExtlwiTranslatorTest.class, ExtrwiDotTranslatorTest.class,
    ExtrwiTranslatorTest.class, ExtsbTranslatorTest.class, ExtsbDotTranslatorTest.class,
    ExtshDotTranslatorTest.class, ExtshTranslatorTest.class, InslwiDotTranslatorTest.class,
    InslwiTranslatorTest.class, InsrwiDotTranslatorTest.class, InsrwiTranslatorTest.class,
    LbzTranslatorTest.class, LbzuTranslatorTest.class, LbzuxTranslatorTest.class,
    LbzxTranslatorTest.class, LhaTranslatorTest.class, LhauTranslatorTest.class,
    LhauxTranslatorTest.class, LhaxTranslatorTest.class, LhbrxTranslatorTest.class,
    LhzTranslatorTest.class, LhzuTranslatorTest.class, LhzuxTranslatorTest.class,
    LhzxTranslatorTest.class, LwbrxTranslatorTest.class, LwzTranslatorTest.class,
    LwzuTranslatorTest.class, LwzuxTranslatorTest.class, LwzxTranslatorTest.class,
    LisTranslatorTest.class, LiTranslatorTest.class, LmwTranslatorTest.class,
    LswiTranslatorTest.class, McrfTranslatorTest.class, McrxrTranslatorTest.class,
    MfcrTranslatorTest.class, MfmsrTranslatorTest.class, McrfTranslatorTest.class,
    McrxrTranslatorTest.class, MtcrfTranslatorTest.class, MtcrTranslatorTest.class,
    MulhwDotTranslatorTest.class, MulhwTranslatorTest.class, MulhwuDotTranslatorTest.class,
    MulhwuTranslatorTest.class, MulliTranslatorTest.class, MullwDotTranslatorTest.class,
    MullwoDotTranslatorTest.class, MullwoTranslatorTest.class, MullwTranslatorTest.class,
    NandDotTranslatorTest.class, NandTranslatorTest.class, NegDotTranslatorTest.class,
    NegoDotTranslatorTest.class, NegoTranslatorTest.class, NegTranslatorTest.class,
    NorDotTranslatorTest.class, NorTranslatorTest.class, OrcDotTranslatorTest.class,
    OrcTranslatorTest.class, OrDotTranslatorTest.class, OrisTranslatorTest.class,
    OriTranslatorTest.class, OrTranslatorTest.class, RlwnmDotTranslatorTest.class,
    RlwimiDotTranslatorTest.class, RlwimiTranslatorTest.class, RlwnmTranslatorTest.class,
    RotlwDotTranslatorTest.class, RotlwTranslatorTest.class, RotlwiDotTranslatorTest.class,
    RotlwiTranslatorTest.class, RotrwiDotTranslatorTest.class, RotrwiTranslatorTest.class,
    SlwDotTranslatorTest.class, SlwiDotTranslatorTest.class, SlwiTranslatorTest.class,
    SlwTranslatorTest.class, SrawDotTranslatorTest.class, SrawiDotTranslatorTest.class,
    SrawiTranslatorTest.class, SrawTranslatorTest.class, SrwDotTranslatorTest.class,
    SrwiDotTranslatorTest.class, SrwiTranslatorTest.class, SrwTranslatorTest.class,
    StbTranslatorTest.class, StbuTranslatorTest.class, StbuxTranslatorTest.class,
    StbxTranslatorTest.class, SthbrxTranslatorTest.class, SthTranslatorTest.class,
    SthuTranslatorTest.class, SthuxTranslatorTest.class, SthxTranslatorTest.class,
    StmwTranslatorTest.class, StwbrxTranslatorTest.class, StwTranslatorTest.class,
    StwuTranslatorTest.class, StwuxTranslatorTest.class, StwxTranslatorTest.class,
    SubfcDotTranslatorTest.class, SubfcTranslatorTest.class, SubfcoDotTranslatorTest.class,
    SubfcoTranslatorTest.class, SubfDotTranslatorTest.class, SubfeDotTranslatorTest.class,
    SubfeoTranslatorTest.class, SubfeTranslatorTest.class, SubficTranslatorTest.class,
    SubfmeDotTranslatorTest.class, SubfmeoDotTranslatorTest.class, SubfmeoTranslatorTest.class,
    SubfmeTranslatorTest.class, SubfoDotTranslatorTest.class, SubfoTranslatorTest.class,
    SubfTranslatorTest.class, SubfzeDotTranslatorTest.class, SubfzeoDotTranslatorTest.class,
    SubfzeTranslatorTest.class, SubfzeoTranslatorTest.class, XorDotTranslatorTest.class,
    XorisTranslatorTest.class, XoriTranslatorTest.class, XorTranslatorTest.class})
public class AllTests {

}
