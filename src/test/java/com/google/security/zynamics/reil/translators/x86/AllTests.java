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
package com.google.security.zynamics.reil.translators.x86;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ HelpersTest.class, AboveGeneratorTest.class, AdcTranslatorTest.class,
    AddTranslatorTest.class, AndTranslatorTest.class, BelowEqualGeneratorTest.class,
    BelowGeneratorTest.class, BsfTranslatorTest.class, BswapTranslatorTest.class,
    BsrTranslatorTest.class, BtTranslatorTest.class, BtcTranslatorTest.class,
    BtrTranslatorTest.class, BtsTranslatorTest.class, CallTranslatorTest.class,
    CdqTranslatorTest.class, ClcTranslatorTest.class, CldTranslatorTest.class,
    CliTranslatorTest.class, CmcTranslatorTest.class, CMovccTranslatorTest.class,
    CmpsbTranslatorTest.class, CmpswTranslatorTest.class, CmpsdTranslatorTest.class,
    CmpTranslatorTest.class, CwdeTranslatorTest.class, CwdTranslatorTest.class,
    DecTranslatorTest.class, DivTranslatorTest.class, EcxZeroGeneratorTest.class,
    GreaterEqualGeneratorTest.class, GreaterGeneratorTest.class, ImulTranslatorTest.class,
    IncTranslatorTest.class, JccTranslatorTest.class, JmpTranslatorTest.class,
    LahfTranslatorTest.class, LeaTranslatorTest.class, LeaveTranslatorTest.class,
    LessEqualGeneratorTest.class, LessGeneratorTest.class, LodsbTranslatorTest.class,
    LodswTranslatorTest.class, LodsdTranslatorTest.class, LoopTranslatorTest.class,
    LoopeTranslatorTest.class, LoopneTranslatorTest.class, MovTranslatorTest.class,
    MovsbTranslatorTest.class, MovswTranslatorTest.class, MovsdTranslatorTest.class,
    MovsxTranslatorTest.class, MovzxTranslatorTest.class, NopTranslatorTest.class,
    PopTranslatorTest.class, PushTranslatorTest.class, PushfTranslatorTest.class,
    RclTranslatorTest.class, RepLodsbTranslatorTest.class, RepLodswTranslatorTest.class,
    RepLodsdTranslatorTest.class, RepMovsbTranslatorTest.class, RepStosbTranslatorTest.class,
    RepStoswTranslatorTest.class, RepStosdTranslatorTest.class, RepeCmpsbTranslatorTest.class,
    RepeScasbTranslatorTest.class, RepeScaswTranslatorTest.class, RepeScasdTranslatorTest.class,
    RepneCmpsbTranslatorTest.class, RetnTranslatorTest.class, RolTranslatorTest.class,
    RorTranslatorTest.class, ScasbTranslatorTest.class, ScaswTranslatorTest.class,
    ScasdTranslatorTest.class, SetalcTranslatorTest.class, ShldTranslatorTest.class,
    ShlTranslatorTest.class, ShrTranslatorTest.class, StosbTranslatorTest.class,
    StoswTranslatorTest.class, StosdTranslatorTest.class, SubTranslatorTest.class,
    XaddTranslatorTest.class, XorTranslatorTest.class })
public class AllTests {

}
