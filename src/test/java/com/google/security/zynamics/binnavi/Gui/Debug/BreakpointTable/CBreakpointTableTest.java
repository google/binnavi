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
package com.google.security.zynamics.binnavi.Gui.Debug.BreakpointTable;

import com.google.common.collect.Sets;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.ZyGraph.LayoutStyle;
import com.google.security.zynamics.binnavi.ZyGraph.ZyGraphViewSettings;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.FileReadException;
import com.google.security.zynamics.binnavi.config.FlowGraphSettingsConfigItem;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.MockViewContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviEdge;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.NaviNode;
import com.google.security.zynamics.binnavi.yfileswrap.zygraph.ZyGraph;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.yfileswrap.gui.zygraph.ZyGraph2DView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import y.base.Edge;
import y.base.Node;

import java.util.LinkedHashMap;

@RunWith(JUnit4.class)
public class CBreakpointTableTest {
  /**
   * This test is making sure that listeners are correctly attached to new debuggers.
   *
   * @throws CouldntSaveDataException
   * @throws FileReadException
   */
  @Test
  public void testAddedDebugger() throws CouldntSaveDataException, FileReadException {
    ConfigManager.instance().read();

    final INaviModule mockModule = new MockModule();
    final DebugTargetSettings target = new ModuleTargetSettings(mockModule);
    final DebuggerProvider debuggerProvider = new DebuggerProvider(target);
    final ZyGraphViewSettings settings = new ZyGraphViewSettings(new FlowGraphSettingsConfigItem());
    settings.getLayoutSettings().setDefaultGraphLayout(LayoutStyle.CIRCULAR);
    final ZyGraph graph =
        new ZyGraph(new MockView(), new LinkedHashMap<Node, NaviNode>(),
            new LinkedHashMap<Edge, NaviEdge>(), settings, new ZyGraph2DView());
    final IViewContainer viewContainer = new MockViewContainer();

    final CBreakpointTable table = new CBreakpointTable(debuggerProvider, graph, viewContainer);

    final MockDebugger debugger = new MockDebugger(new ModuleTargetSettings(mockModule));
    final MockModule module = new MockModule();
    debugger.getBreakpointManager().addBreakpoints(BreakpointType.REGULAR,
        Sets.newHashSet(new BreakpointAddress(module, new UnrelocatedAddress(new CAddress(0)))));
    debuggerProvider.addDebugger(debugger);

    table.dispose();
  }
}
