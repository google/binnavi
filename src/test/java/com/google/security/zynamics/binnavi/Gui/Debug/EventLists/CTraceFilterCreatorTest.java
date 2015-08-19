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
package com.google.security.zynamics.binnavi.Gui.Debug.EventLists;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CTraceFilterCreator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class CTraceFilterCreatorTest {
  @Test
  public void test() throws RecognitionException {
    final INaviModule mockModule = new MockModule();
    final INaviModule mockModule2 = new MockModule(true);
    final INaviModule mockModule3 = new MockModule();

    final List<ITraceEvent> events = new ArrayList<ITraceEvent>();

    final TraceEvent event1 = new TraceEvent(77,
        new BreakpointAddress(mockModule, new UnrelocatedAddress(new CAddress(0x123))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {05})));
    final TraceEvent event2 = new TraceEvent(88,
        new BreakpointAddress(mockModule2, new UnrelocatedAddress(new CAddress(0x124))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {06})));
    final TraceEvent event3 = new TraceEvent(99,
        new BreakpointAddress(mockModule3, new UnrelocatedAddress(new CAddress(0x125))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {07, 05})));

    events.add(event1);
    events.add(event2);
    events.add(event3);

    final CTraceFilterCreator creator = new CTraceFilterCreator();

    final IFilter<ITraceEvent> defaultFilter = creator.createFilter("99");

    assertEquals(1, defaultFilter.get(events).size());
    assertEquals(event3, defaultFilter.get(events).get(0));

    final IFilter<ITraceEvent> memoryFilter = creator.createFilter("mem == 06");

    assertEquals(1, memoryFilter.get(events).size());
    assertEquals(event2, memoryFilter.get(events).get(0));

    final IFilter<ITraceEvent> registerFilter = creator.createFilter("eax == 291");

    assertEquals(3, registerFilter.get(events).size());
  }

  @Test
  public void testAlternativeInput() throws RecognitionException {
    final INaviModule mockModule = new MockModule();
    final INaviModule mockModule2 = new MockModule(true);
    final INaviModule mockModule3 = new MockModule();

    final List<ITraceEvent> events = new ArrayList<ITraceEvent>();

    final TraceEvent event1 = new TraceEvent(77,
        new BreakpointAddress(mockModule, new UnrelocatedAddress(new CAddress(0x123))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {05})));
    final TraceEvent event2 = new TraceEvent(88,
        new BreakpointAddress(mockModule2, new UnrelocatedAddress(new CAddress(0x124))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {06})));
    final TraceEvent event3 = new TraceEvent(99,
        new BreakpointAddress(mockModule3, new UnrelocatedAddress(new CAddress(0x125))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {07, 05})));

    events.add(event1);
    events.add(event2);
    events.add(event3);

    final CTraceFilterCreator creator = new CTraceFilterCreator();

    final IFilter<ITraceEvent> memoryFilter = creator.createFilter("mem == 6");

    assertEquals(1, memoryFilter.get(events).size());
    assertEquals(event2, memoryFilter.get(events).get(0));

    final IFilter<ITraceEvent> registerFilter = creator.createFilter("eax == 0x123");

    assertEquals(3, registerFilter.get(events).size());

    final IFilter<ITraceEvent> brokenRegisterFilter =
        creator.createFilter("eax == 0x123111111111111111111111111111");

    assertEquals(0, brokenRegisterFilter.get(events).size());
  }
}
