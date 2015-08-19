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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModelFactory;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedHashSet;

@RunWith(JUnit4.class)
public class CThreadInformationPanelTest {
  /**
   * This test makes sure that going from no debugger to a debugger works fine.
   */
  @Test
  public void test() throws IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final IGraphModel graphModel = new MockGraphModel(); // CGraphModelFactory.get(database,
                                                         // viewContainer);

    final CDebugPerspectiveModel model = CDebugPerspectiveModelFactory.get(graphModel);

    final CThreadInformationPanel panel = new CThreadInformationPanel(model);

    final IDebugger debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);
    debugger.getProcessManager().addThread(thread);

    final LinkedHashSet<?> perspectiveListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(model, "m_listeners"), "m_listeners");
    final LinkedHashSet<?> debuggerListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "processManager"),
            "listeners"), "m_listeners");
    final LinkedHashSet<?> threadListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(thread, "listeners"), "m_listeners");

    assertEquals(1, perspectiveListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(1, threadListeners.size());

    model.setActiveDebugger(debugger);

    assertEquals(2, threadListeners.size());
    assertEquals(2, debuggerListeners.size());

    panel.dispose();

    assertEquals(1, threadListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(0, perspectiveListeners.size());
  }

  /**
   * This test makes sure that going from no debugger to a debugger to no debugger works fine.
   */
  @Test
  public void test2() throws IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final IGraphModel graphModel = new MockGraphModel(); // CGraphModelFactory.get(database,
                                                         // viewContainer);

    final CDebugPerspectiveModel model = CDebugPerspectiveModelFactory.get(graphModel);

    final CThreadInformationPanel panel = new CThreadInformationPanel(model);

    final IDebugger debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);
    debugger.getProcessManager().addThread(thread);

    final LinkedHashSet<?> perspectiveListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(model, "m_listeners"), "m_listeners");
    final LinkedHashSet<?> debuggerListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "processManager"),
            "listeners"), "m_listeners");
    final LinkedHashSet<?> threadListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(thread, "listeners"), "m_listeners");

    assertEquals(1, perspectiveListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(1, threadListeners.size());

    model.setActiveDebugger(debugger);

    assertEquals(2, threadListeners.size());
    assertEquals(2, debuggerListeners.size());

    model.setActiveDebugger(null);

    assertEquals(1, threadListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(1, perspectiveListeners.size());

    panel.dispose();

    assertEquals(1, threadListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(0, perspectiveListeners.size());
  }

  /**
   * This test makes sure that going from no debugger to a debugger to more threads works fine.
   */
  @Test
  public void test3() throws IllegalArgumentException, SecurityException, IllegalAccessException,
      NoSuchFieldException {
    final IGraphModel graphModel = new MockGraphModel(); // CGraphModelFactory.get(database,
                                                         // viewContainer);

    final CDebugPerspectiveModel model = CDebugPerspectiveModelFactory.get(graphModel);

    final CThreadInformationPanel panel = new CThreadInformationPanel(model);

    final IDebugger debugger = new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final LinkedHashSet<?> perspectiveListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(model, "m_listeners"), "m_listeners");
    final LinkedHashSet<?> debuggerListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "processManager"),
            "listeners"), "m_listeners");

    assertEquals(1, perspectiveListeners.size());
    assertEquals(1, debuggerListeners.size());

    model.setActiveDebugger(debugger);

    final TargetProcessThread thread = new TargetProcessThread(0, ThreadState.RUNNING);
    debugger.getProcessManager().addThread(thread);

    final LinkedHashSet<?> threadListeners = (LinkedHashSet<?>) ReflectionHelpers.getField(
        ReflectionHelpers.getField(thread, "listeners"), "m_listeners");

    assertEquals(2, threadListeners.size());
    assertEquals(2, threadListeners.size());
    assertEquals(2, debuggerListeners.size());

    debugger.getProcessManager().removeThread(thread);

    assertEquals(0, threadListeners.size());

    panel.dispose();

    assertEquals(0, threadListeners.size());
    assertEquals(1, debuggerListeners.size());
    assertEquals(0, perspectiveListeners.size());
  }
}
