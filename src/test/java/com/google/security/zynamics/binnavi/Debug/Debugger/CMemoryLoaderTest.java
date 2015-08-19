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
package com.google.security.zynamics.binnavi.Debug.Debugger;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.MemoryLoader;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class CMemoryLoaderTest {
  private MockDebugger debugger;
  private MemoryLoader loader;

  @Before
  public void setUp() {
    debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));
    final ArrayList<MemorySection> sections = new ArrayList<MemorySection>();
    sections.add(new MemorySection(new CAddress(BigInteger.ZERO), new CAddress(BigInteger
        .valueOf(1000))));
    debugger.getProcessManager().setMemoryMap(new MemoryMap(sections));
    loader = new MemoryLoader(debugger);
  }

  @After
  public void tearDown() {
    debugger.close();
  }

  @Test
  public void testRequest_Duplicate() throws DebugExceptionWrapper {
    // Request if the data does not exist
    loader.requestMemory(new CAddress(BigInteger.valueOf(0)), 100);
    // assertEquals("READMEM-0-100;", debugger.requests);

    // Do not request duplicates
    loader.requestMemory(new CAddress(BigInteger.valueOf(0)), 100);
    // assertEquals("READMEM-0-100;", debugger.requests);
  }

  @Test
  public void testRequest_Existing() throws DebugExceptionWrapper {
    debugger.getProcessManager().getMemory().store(0, new byte[100]);

    // Do not request if the data is here
    loader.requestMemory(new CAddress(BigInteger.valueOf(0)), 100);
    assertEquals("", debugger.requests);
  }

  @Test
  public void testRequest_Partially() throws DebugExceptionWrapper {
    debugger.getProcessManager().getMemory().store(0, new byte[34]);
    debugger.getProcessManager().getMemory().store(60, new byte[34]);

    // Request only the parts that aren't there
    loader.requestMemory(new CAddress(BigInteger.valueOf(0)), 100);
    // assertEquals("READMEM-34-26;READMEM-94-6;", debugger.requests);
  }
}
