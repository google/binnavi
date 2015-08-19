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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerOptions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

@RunWith(JUnit4.class)
public final class ProcessTest {
  @Test
  public void testGetMemory() {
    final MockProcessListener listener = new MockProcessListener();

    final ProcessManager manager = new ProcessManager();

    final Process process = new Process(manager);

    process.addListener(listener);

    final Memory m1 = process.getMemory();
    final Memory m2 = process.getMemory();

    assertEquals(m1, m2);

    process.removeListener(listener);
  }

  @Test
  public void testGetMemoryMap() {
    final MockProcessListener listener = new MockProcessListener();
    final ProcessManager manager = new ProcessManager();
    final Process process = new Process(manager);
    process.addListener(listener);
    assertEquals(0, process.getMemoryMap().getSections().size());

    manager.setMemoryMap(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap(Lists.newArrayList(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection(new CAddress(0), new CAddress(0x100)))));

    assertEquals("changedMemoryMap;", listener.events);

    assertEquals(1, process.getMemoryMap().getSections().size());

    final MemoryMap m1 = process.getMemoryMap();
    final MemoryMap m2 = process.getMemoryMap();

    assertEquals(m1, m2);

    process.removeListener(listener);
  }

  @Test
  public void testGetTargetInformation() {
    final MockProcessListener listener = new MockProcessListener();
    final ProcessManager manager = new ProcessManager();
    final Process process = new Process(manager);
    process.addListener(listener);
    assertNull(process.getTargetInformation());

    manager.setTargetInformation(
        new com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation(5,
            new FilledList<RegisterDescription>(), new DebuggerOptions(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                12,
                0,
                new ArrayList<DebuggerException>(),
                false,
                false,
                false)));

    final TargetInformation t1 = process.getTargetInformation();
    final TargetInformation t2 = process.getTargetInformation();

    assertNotNull(t1);
    assertEquals(t1, t2);

    assertEquals("changedTargetInformation;", listener.events);

    assertEquals(5, process.getTargetInformation().getAddressSize());
    assertEquals(false, process.getTargetInformation().canTerminate());

    process.removeListener(listener);
  }

  @Test
  public void testLifeCycle() {
    final MockProcessListener listener = new MockProcessListener();

    final ProcessManager manager = new ProcessManager();

    final Process process = new Process(manager);

    process.addListener(listener);

    manager.setAttached(true);

    assertEquals("attached;", listener.events);

    process.removeListener(listener);
    manager.setTargetInformation(
        new com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation(5,
            new FilledList<RegisterDescription>(), new DebuggerOptions(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                12,
                0,
                new ArrayList<DebuggerException>(),
                false,
                false,
                false)));
    manager.addThread(new TargetProcessThread(0, ThreadState.RUNNING));
    manager.addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x100)), 0x100));
    manager.setMemoryMap(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryMap(Lists.newArrayList(new com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection(new CAddress(0), new CAddress(0x100)))));
    manager.getMemory().store(0, new byte[] {0, 1, 2, 3});
    process.addListener(listener);

    manager.setTargetInformation(
        new com.google.security.zynamics.binnavi.debug.models.targetinformation.TargetInformation(5,
            new FilledList<RegisterDescription>(), new DebuggerOptions(false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                12,
                0,
                new ArrayList<DebuggerException>(),
                false,
                false,
                false)));

    assertEquals(0, process.getThreads().size());
    assertEquals(0, process.getModules().size());
    assertEquals(0, process.getMemoryMap().getSections().size());
    assertEquals(false, process.getMemory().hasData(0, 4));

    manager.setAttached(false);

    assertEquals("attached;changedTargetInformation;detached;", listener.events);

    assertEquals(0, process.getThreads().size());
    assertEquals(0, process.getModules().size());
    assertEquals(null, process.getTargetInformation());
    assertEquals(0, process.getMemoryMap().getSections().size());
    assertEquals(false, process.getMemory().hasData(0, 4));

    process.removeListener(listener);
  }

  @Test
  public void testModules() {
    final MockProcessListener listener = new MockProcessListener();

    final ProcessManager manager = new ProcessManager();

    manager.addModule(new MemoryModule("Hannes", "C:\\Hannes.dll",
        new RelocatedAddress(new CAddress(0x100)), 0x100));

    final Process process = new Process(manager);

    assertEquals(1, process.getModules().size());

    process.addListener(listener);

    final MemoryModule dll = new MemoryModule("Foobert.dll", "C:\\Foobert.dll",
        new RelocatedAddress(new CAddress(0x100)), 0x100);

    manager.addModule(dll);

    assertEquals("addedModule/Foobert.dll;", listener.events);
    assertEquals(2, process.getModules().size());

    manager.removeModule(dll);

    assertEquals("addedModule/Foobert.dll;removedModule/Foobert.dll;", listener.events);
    assertEquals(1, process.getModules().size());

    process.removeListener(listener);
  }

  @Test
  public void testThreads() {
    final MockProcessListener listener = new MockProcessListener();

    final ProcessManager manager = new ProcessManager();

    manager.addThread(new TargetProcessThread(0, ThreadState.RUNNING));

    final Process process = new Process(manager);

    process.addListener(listener);

    final Thread thread1 = process.getThreads().get(0);
    assertEquals(0, thread1.getThreadId());

    final TargetProcessThread thread = new TargetProcessThread(1, ThreadState.RUNNING);

    manager.addThread(thread);

    assertEquals("addedThread/1;", listener.events);
    assertEquals(2, process.getThreads().size());

    manager.removeThread(thread);

    assertEquals("addedThread/1;removedThread/1;", listener.events);
    assertEquals(1, process.getThreads().size());

    process.removeListener(listener);
  }
}
