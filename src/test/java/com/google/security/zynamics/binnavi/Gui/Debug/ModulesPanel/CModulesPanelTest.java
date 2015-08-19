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
package com.google.security.zynamics.binnavi.Gui.Debug.ModulesPanel;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.MockGraphModel;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.Panels.CDebugPerspectiveModel;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.zylib.reflection.ReflectionHelpers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedHashSet;

@RunWith(JUnit4.class)
public final class CModulesPanelTest {
  @Test
  public void testLifeCycle() throws IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    final MockDebugger debugger =
        new MockDebugger(new ModuleTargetSettings(CommonTestObjects.MODULE));

    final CDebugPerspectiveModel model = new CDebugPerspectiveModel(new MockGraphModel());

    model.setActiveDebugger(debugger);

    final CModulesPanel panel = new CModulesPanel(model);

    panel.dispose();

    final LinkedHashSet<?> debuggerListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "synchronizer"),
            "listeners"), "m_listeners");
    final LinkedHashSet<?> processListeners =
        (LinkedHashSet<?>) ReflectionHelpers.getField(ReflectionHelpers.getField(
            ReflectionHelpers.getField(AbstractDebugger.class, debugger, "processManager"),
            "listeners"), "m_listeners");

    // The debugger only has one internal memory synchronizer
    assertEquals(1, debuggerListeners.size());
    // The process manager only has one thread state synchronizer
    assertEquals(1, processListeners.size());
  }

  @Test
  public void testLifeCycleNoDebugger() throws IllegalArgumentException, SecurityException {
    final CDebugPerspectiveModel model = new CDebugPerspectiveModel(new MockGraphModel());

    final CModulesPanel panel = new CModulesPanel(model);

    panel.dispose();
  }
}
