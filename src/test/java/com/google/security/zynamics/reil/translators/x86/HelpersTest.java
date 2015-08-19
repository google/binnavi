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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.security.zynamics.reil.translators.x86.Helpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HelpersTest {
  @Test
  public void testIsHigher8BitRegister() {
    assertFalse(Helpers.isHigher8BitRegister("al"));
    assertFalse(Helpers.isHigher8BitRegister("bl"));
    assertFalse(Helpers.isHigher8BitRegister("cl"));
    assertFalse(Helpers.isHigher8BitRegister("dl"));

    assertTrue(Helpers.isHigher8BitRegister("ah"));
    assertTrue(Helpers.isHigher8BitRegister("bh"));
    assertTrue(Helpers.isHigher8BitRegister("ch"));
    assertTrue(Helpers.isHigher8BitRegister("dh"));

    assertFalse(Helpers.isHigher8BitRegister("ax"));
    assertFalse(Helpers.isHigher8BitRegister("bx"));
    assertFalse(Helpers.isHigher8BitRegister("cx"));
    assertFalse(Helpers.isHigher8BitRegister("dx"));
    assertFalse(Helpers.isHigher8BitRegister("di"));
    assertFalse(Helpers.isHigher8BitRegister("si"));
    assertFalse(Helpers.isHigher8BitRegister("sp"));
    assertFalse(Helpers.isHigher8BitRegister("bp"));
    assertFalse(Helpers.isHigher8BitRegister("ip"));

    assertFalse(Helpers.isHigher8BitRegister("eax"));
    assertFalse(Helpers.isHigher8BitRegister("ebx"));
    assertFalse(Helpers.isHigher8BitRegister("ecx"));
    assertFalse(Helpers.isHigher8BitRegister("edx"));
    assertFalse(Helpers.isHigher8BitRegister("edi"));
    assertFalse(Helpers.isHigher8BitRegister("esi"));
    assertFalse(Helpers.isHigher8BitRegister("esp"));
    assertFalse(Helpers.isHigher8BitRegister("ebp"));
    assertFalse(Helpers.isHigher8BitRegister("eip"));

    assertFalse(Helpers.isHigher8BitRegister("cs"));
    assertFalse(Helpers.isHigher8BitRegister("ds"));
    assertFalse(Helpers.isHigher8BitRegister("es"));
    assertFalse(Helpers.isHigher8BitRegister("fs"));
    assertFalse(Helpers.isHigher8BitRegister("gs"));
  }
}
