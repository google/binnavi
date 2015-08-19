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
package com.google.security.zynamics.reil.translators.arm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
    ARMAdcTranslatorTest.class,
    ARMAddTranslatorTest.class,
    ARMAndTranslatorTest.class,
    ARMBicTranslatorTest.class,
    ARMBlTranslatorTest.class,
    ARMBlxTranslatorTest.class,
    ARMBxTranslatorTest.class,
    ARMClzTranslatorTest.class,
    ARMCmnTranslatorTest.class,
    ARMCmpTranslatorTest.class,
    ARMCpyTranslatorTest.class,
    ARMEorTranslatorTest.class,
    // ARMLdcTranslatorTest.class,
    ARMLdmTranslatorTest.class,
    ARMLdrbTranslatorTest.class,
    ARMLdrbtTranslatorTest.class,
    ARMLdrdTranslatorTest.class,
    // ARMLdrexTranslatorTest.class,
    ARMLdrhTranslatorTest.class,
    ARMLdrsbTranslatorTest.class,
    ARMLdrshTranslatorTest.class,
    ARMLdrTranslatorTest.class,
    ARMLdrtTranslatorTest.class,
    ARMMlaTranslatorTest.class,
    ARMMovTranslatorTest.class,
    ARMMulTranslatorTest.class,
    ARMMvnTranslatorTest.class,
    ARMOrrTranslatorTest.class,
    ARMPkhbtTranslatorTest.class,
    ARMPkhtbTranslatorTest.class,
    ARMQadd16TranslatorTest.class,
    ARMQadd8TranslatorTest.class,
    ARMQaddsubxTranslatorTest.class,
    ARMQaddTranslatorTest.class,
    ARMQdaddTranslatorTest.class,
    ARMQdsubTranslatorTest.class,
    ARMQsub16TranslatorTest.class,
    ARMQsub8TranslatorTest.class,
    ARMQsubaddxTranslatorTest.class,
    ARMQsubTranslatorTest.class,
    ARMRev16TranslatorTest.class,
    ARMRevshTranslatorTest.class,
    ARMRevTranslatorTest.class,
    ARMRsbTranslatorTest.class,
    ARMRscTranslatorTest.class,
    ARMSadd16TranslatorTest.class,
    ARMSadd8TranslatorTest.class,
    ARMSaddsubxTranslatorTest.class,
    ARMSbcTranslatorTest.class,
    ARMSelTranslatorTest.class,
    ARMShadd16TranslatorTest.class,
    ARMShadd8TranslatorTest.class,
    ARMShaddsubxTranslatorTest.class,
    ARMShsub16TranslatorTest.class,
    ARMShsub8TranslatorTest.class,
    ARMShsubaddxTranslatorTest.class,
    ARMSmladTranslatorTest.class,
    ARMSmlaldTranslatorTest.class,
    ARMSmlalTranslatorTest.class,
    ARMSmlsdTranslatorTest.class,
    ARMSmlsldTranslatorTest.class,
    ARMSmmlaTranslatorTest.class,
    ARMSmmlsTranslatorTest.class,
    ARMSmmulTranslatorTest.class,
    ARMSmuadTranslatorTest.class,
    ARMSmullTranslatorTest.class,
    ARMSmusdTranslatorTest.class,
    ARMSsat16TranslatorTest.class,
    ARMSsatTranslatorTest.class,
    ARMSsub16TranslatorTest.class,
    ARMSsub8TranslatorTest.class,
    ARMSsubaddxTranslatorTest.class,
    // ARMStcTranslatorTest.class,
    ARMStmTranslatorTest.class,
    ARMStrbTranslatorTest.class,
    ARMStrbtTranslatorTest.class,
    ARMStrdTranslatorTest.class,
    // ARMStrexTranslatorTest.class,
    ARMStrhTranslatorTest.class, ARMStrTranslatorTest.class, ARMStrtTranslatorTest.class,
    ARMSubTranslatorTest.class, ARMSxtab16TranslatorTest.class, ARMSxtabTranslatorTest.class,
    ARMSxtahTranslatorTest.class, ARMSxtb16TranslatorTest.class, ARMSxtbTranslatorTest.class,
    ARMSxthTranslatorTest.class, ARMUadd16TranslatorTest.class, ARMUadd8TranslatorTest.class,
    ARMUaddsubxTranslatorTest.class, ARMUhadd16TranslatorTest.class, ARMUhadd8TranslatorTest.class,
    ARMUhaddsubxTranslatorTest.class, ARMUhsub16TranslatorTest.class,
    ARMUhsub8TranslatorTest.class, ARMUhaddsubxTranslatorTest.class,
    ARMUhsubaddxTranslatorTest.class, ARMUxthTranslatorTest.class, ARMUmaalTranslatorTest.class,
    ARMUmlalTranslatorTest.class, ARMUmullTranslatorTest.class, ARMUqadd16TranslatorTest.class,
    ARMUqadd8TranslatorTest.class, ARMUqaddsubxTranslatorTest.class,
    ARMUqsub16TranslatorTest.class, ARMUqsub8TranslatorTest.class,
    ARMUqsubaddxTranslatorTest.class, ARMUsad8TranslatorTest.class, ARMUsada8TranslatorTest.class,
    ARMUsat16TranslatorTest.class, ARMUsatTranslatorTest.class, ARMUsub16TranslatorTest.class,
    ARMUsub8TranslatorTest.class, ARMUsubaddxTranslatorTest.class, ARMUxtab16TranslatorTest.class,
    ARMUxtabTranslatorTest.class, ARMUxtahTranslatorTest.class, ARMUxtb16TranslatorTest.class,
    ARMUxtbTranslatorTest.class, ARMTeqTranslatorTest.class, ARMTstTranslatorTest.class,
    /**
     * thumb translators
     */
    THUMBAdcTranslatorTest.class, THUMBAddTranslatorTest.class, THUMBAndTranslatorTest.class,
    THUMBAsrTranslatorTest.class, THUMBBicTranslatorTest.class, THUMBCmnTranslatorTest.class,
    THUMBCmpTranslatorTest.class, THUMBCpyTranslatorTest.class, THUMBEorTranslatorTest.class,
    THUMBLdrbTranslatorTest.class, THUMBLdrhTranslatorTest.class, THUMBLdrTranslatorTest.class,
    THUMBLslTranslatorTest.class, THUMBLsrTranslatorTest.class, THUMBMovTranslatorTest.class,
    THUMBMvnTranslatorTest.class, THUMBNegTranslatorTest.class, THUMBOrrTranslatorTest.class,
    THUMBRev16TranslatorTest.class, THUMBRevshTranslatorTest.class, THUMBRevTranslatorTest.class,
    THUMBRorTranslatorTest.class, THUMBSbcTranslatorTest.class, THUMBStrbTranslatorTest.class,
    THUMBStrhTranslatorTest.class, THUMBStrTranslatorTest.class, THUMBSubTranslatorTest.class,
    THUMBSxtbTranslatorTest.class, THUMBSxthTranslatorTest.class, THUMBTstTranslatorTest.class,
    THUMBUxtbTranslatorTest.class, THUMBUxthTranslatorTest.class,
    /**
     * thumb2 translators.
     */
    THUMB2MovtTranslatorTest.class, THUMB2BFITranslatorTest.class, THUMB2BFCTranslatorTest.class,
    THUMB2RBITTranslatorTest.class})
public class AllTests {

}
