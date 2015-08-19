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
package com.google.security.zynamics.binnavi.API.debug;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class MemoryModuleTest {
  @Test
  public void testConstructor() {
    final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule
        internalModule =
        new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule("dll",
            "C:\\dll", new RelocatedAddress(new CAddress(0x100)), 0x200);

    final MemoryModule module = new MemoryModule(internalModule);

    assertEquals("dll", module.getName());
    assertEquals(0x100, module.getBaseAddress().toLong());
    assertEquals(0x200, module.getSize());
  }
}
