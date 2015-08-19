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
package com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Gui.Debug.MemorySectionPanel.CMemorySectionWrapper;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class CMemorySectionWrapperTest {
  @Test
  public void testSimple() {
    assertEquals("00000000 - 000003FE (1023 bytes)", new CMemorySectionWrapper(new MemorySection(
        new CAddress(0), new CAddress(1022))).toString());
    assertEquals("00000000 - 000003FF (1.00 KB)", new CMemorySectionWrapper(new MemorySection(
        new CAddress(0), new CAddress(1023))).toString());
    assertEquals("00000000 - 000005DC (1.47 KB)", new CMemorySectionWrapper(new MemorySection(
        new CAddress(0), new CAddress(1500))).toString());
    assertEquals("00000000 - 00102710 (1.01 MB)", new CMemorySectionWrapper(new MemorySection(
        new CAddress(0), new CAddress((1024 * 1024) + 10000))).toString());
  }
}
