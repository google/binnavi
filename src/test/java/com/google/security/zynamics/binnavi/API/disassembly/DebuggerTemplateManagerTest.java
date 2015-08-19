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

import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class DebuggerTemplateManagerTest {
  @Test
  public void testConstructor() {
    final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager
        internalManager =
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager(
            new MockSqlProvider());

    final DebuggerTemplateManager manager = new DebuggerTemplateManager(internalManager);

    assertEquals("Debugger Template Manager (0 templates)", manager.toString());
  }

  @Test
  public void testDispose() throws CouldntSaveDataException, CouldntDeleteException {
    final MockDebuggerTemplateManagerListener listener = new MockDebuggerTemplateManagerListener();

    final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager
        internalManager =
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager(
            new MockSqlProvider());

    final DebuggerTemplateManager manager = new DebuggerTemplateManager(internalManager);

    manager.addListener(listener);

    final DebuggerTemplate template1 = manager.createDebuggerTemplate("Hannes", "Host", 123);
    @SuppressWarnings("unused")
    final DebuggerTemplate template2 = manager.createDebuggerTemplate("Hannes", "Host", 123);

    manager.deleteDebugger(template1);

    manager.dispose();
  }

  @Test
  public void testLifeCycle() throws CouldntSaveDataException, CouldntDeleteException {
    final MockDebuggerTemplateManagerListener listener = new MockDebuggerTemplateManagerListener();

    final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager
        internalManager =
        new com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplateManager(
            new MockSqlProvider());

    final DebuggerTemplateManager manager = new DebuggerTemplateManager(internalManager);

    manager.addListener(listener);

    final DebuggerTemplate template = manager.createDebuggerTemplate("Hannes", "Host", 123);

    assertEquals(1, internalManager.debuggerCount());
    assertEquals(1, manager.getDebuggerTemplateCount());
    assertEquals(1, manager.getDebuggerTemplates().size());
    assertEquals("Hannes", internalManager.getDebugger(0).getName());
    assertEquals("Host", internalManager.getDebugger(0).getHost());
    assertEquals(123, internalManager.getDebugger(0).getPort());
    assertEquals("Hannes", manager.getDebuggerTemplate(0).getName());
    assertEquals("Host", manager.getDebuggerTemplate(0).getHost());
    assertEquals(123, manager.getDebuggerTemplate(0).getPort());

    manager.deleteDebugger(template);

    assertEquals(0, internalManager.debuggerCount());
    assertEquals(0, manager.getDebuggerTemplateCount());

    assertEquals("addedDebuggerTemplate;deletedDebuggerTemplate;", listener.events);

    manager.removeListener(listener);
  }
}
