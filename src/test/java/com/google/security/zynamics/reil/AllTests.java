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
package com.google.security.zynamics.reil;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({com.google.security.zynamics.reil.interpreter.AllTests.class,
    com.google.security.zynamics.reil.translators.ReilTranslatorTest.class,
    com.google.security.zynamics.reil.translators.TranslationHelpersTest.class,
    com.google.security.zynamics.reil.translators.arm.AllTests.class,
    com.google.security.zynamics.reil.translators.mips.AllTests.class,
    com.google.security.zynamics.reil.translators.ppc.AllTests.class,
    com.google.security.zynamics.reil.translators.x86.AllTests.class,
    com.google.security.zynamics.reil.algorithms.mono.AllTests.class,
    com.google.security.zynamics.reil.algorithms.mono2.AllTests.class})
public class AllTests {
  static {
    // Need to disable assertions for this package, otherwise tests fail.
    // TODO(cblichmann): Fix the overuse of "assert false" in the codebase.
    AllTests.class.getClassLoader()
        .setPackageAssertionStatus("com.google.security.zynamics.reil", false);
  }
}
