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
package com.google.security.zynamics.binnavi.API.disassembly;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.API.disassembly.Reference;
import com.google.security.zynamics.binnavi.disassembly.CReference;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ReferenceType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class ReferenceTest {
  @Test
  public void testConstructor() {
    final Reference reference =
        new Reference(new CReference(new CAddress(0), ReferenceType.CALL_DIRECT));

    assertEquals(com.google.security.zynamics.binnavi.API.disassembly.ReferenceType.CALL_DIRECT, reference.getType());
    assertEquals(0, reference.getTarget().toLong());
    assertEquals("CALL_DIRECT Reference to 0", reference.toString());
  }
}
