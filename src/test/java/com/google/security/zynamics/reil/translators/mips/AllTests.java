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
package com.google.security.zynamics.reil.translators.mips;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.google.security.zynamics.reil.translators.ppc.BTranslatorTest;

@RunWith(Suite.class)
@SuiteClasses({
    AddiuTranslatorTest.class,
    AdduTranslatorTest.class,
    AndiTranslatorTest.class,
    AndTranslatorTest.class,
    com.google.security.zynamics.reil.translators.mips.BTranslatorTest.class,
    BalTranslatorTest.class,
    BeqlTranslatorTest.class,
    BeqTranslatorTest.class,
    BeqzlTranslatorTest.class,
    BeqzTranslatorTest.class,
    BgezallTranslatorTest.class,
    BgezalTranslatorTest.class,
    BgezlTranslatorTest.class,
    BgezTranslatorTest.class,
    BgtzlTranslatorTest.class,
    BgtzTranslatorTest.class,
    BlezlTranslatorTest.class,
    BlezTranslatorTest.class,
    BltzallTranslatorTest.class,
    BltzalTranslatorTest.class,
    BltzlTranslatorTest.class,
    BltzTranslatorTest.class,
    BnelTranslatorTest.class,
    BneTranslatorTest.class,
    BnezTranslatorTest.class,
    BnezlTranslatorTest.class,
    BteqzTranslatorTest.class,
    BtnezTranslatorTest.class,
    BTranslatorTest.class,
    CloTranslatorTest.class,
    ClzTranslatorTest.class,
    // CmpiTranslatorTest.class,
    // CmpTranslatorTest.class,
    DaddiTranslatorTest.class,
    DaddiuTranslatorTest.class,
    // DaddTranslatorTest.class,
    // DadduTranslatorTest.class,
    // DdivTranslatorTest.class,
    // DdivuTranslatorTest.class,
    DivTranslatorTest.class,
    DivuTranslatorTest.class,
    // DlaTranslatorTest.class,
    // DliTranslatorTest.class,
    // DmultTranslatorTest.class,
    // DmultuTranslatorTest.class,
    // Dsll32TranslatorTest.class,
    // DsllTranslatorTest.class,
    // DsllvTranslatorTest.class,
    JalrTranslatorTest.class, JalTranslatorTest.class, JrTranslatorTest.class,
    JTranslatorTest.class, LaTranslatorTest.class, LbTranslatorTest.class, LbuTranslatorTest.class,
    LhTranslatorTest.class, LhuTranslatorTest.class, LiTranslatorTest.class,
    LlTranslatorTest.class, LuiTranslatorTest.class, LwlTranslatorTest.class,
    LwrTranslatorTest.class,
    LwTranslatorTest.class,
    MaddTranslatorTest.class,
    MadduTranslatorTest.class,
    MfhiTranslatorTest.class,
    MfloTranslatorTest.class,
    MoveTranslatorTest.class,
    MsubTranslatorTest.class,
    MsubuTranslatorTest.class,
    MthiTranslatorTest.class,
    MtloTranslatorTest.class,
    MulTranslatorTest.class,
    MultTranslatorTest.class,
    MultuTranslatorTest.class,
    NeguTranslatorTest.class,
    NorTranslatorTest.class,
    OriTranslatorTest.class,
    OrTranslatorTest.class,
    SbTranslatorTest.class,
    ScTranslatorTest.class, // this instruction could not really be tested because this instruction
                            // is not implemented in spim
    SebTranslatorTest.class, // this instruction could not really be tested because this instruction
                             // is not implemented in spim
    SehTranslatorTest.class, // this instruction could not really be tested because this instruction
                             // is not implemented in spim
    SllTranslatorTest.class, SllvTranslatorTest.class, SltTranslatorTest.class,
    SltiTranslatorTest.class, SltiuTranslatorTest.class, SltiTranslatorTest.class,
    SltuTranslatorTest.class, SraTranslatorTest.class, SravTranslatorTest.class,
    SrlTranslatorTest.class, SrlvTranslatorTest.class, SubuTranslatorTest.class,
    SwlTranslatorTest.class, SwrTranslatorTest.class, SwTranslatorTest.class,
    XoriTranslatorTest.class, XorTranslatorTest.class
// ZebTranslatorTest.class,
// ZehTranslatorTest.class
})
public class AllTests {

}
