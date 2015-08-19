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

import com.google.common.collect.Lists;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;


@RunWith(JUnit4.class)
public final class MemoryMapTest {
  @Test
  public void testGetSections() {
    final com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap m_internalMap = new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap(Lists.newArrayList(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection(new CAddress(0), new CAddress(0x100)), new com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection(new CAddress(0x200), new CAddress(0x400))));

    final MemoryMap map = new MemoryMap(m_internalMap);

    final List<MemorySection> sections = map.getSections();

    assertEquals(2, sections.size());
    assertEquals(0, sections.get(0).getStart().toLong());
    assertEquals(0x100, sections.get(0).getEnd().toLong());
    assertEquals(0x200, sections.get(1).getStart().toLong());
    assertEquals(0x400, sections.get(1).getEnd().toLong());

    assertEquals("Memory Map (2 sections)", map.toString());
  }
}
