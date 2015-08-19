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
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CEventTableModel;
import com.google.security.zynamics.binnavi.Gui.Debug.EventLists.CTraceFilterCreator;
import com.google.security.zynamics.binnavi.Gui.FilterPanel.IFilter;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceEventType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
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

@RunWith(JUnit4.class)
public class CEventTableModelTest {
  /**
   * General test whether the model is working as expected.
   *
   * @throws RecognitionException
   */
  @Test
  public void testSimple() throws RecognitionException {
    final INaviModule mockModule = new MockModule();
    final INaviModule mockModule2 = new MockModule(true);
    final INaviModule mockModule3 = new MockModule();

    final CEventTableModel model = new CEventTableModel();

    final TraceList list = new TraceList(1, "", "", new MockSqlProvider());
    list.addEvent(new TraceEvent(1,
        new BreakpointAddress(mockModule, new UnrelocatedAddress(new CAddress(0x123))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {05}))));
    list.addEvent(new TraceEvent(1,
        new BreakpointAddress(mockModule2, new UnrelocatedAddress(new CAddress(0x124))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {06}))));
    list.addEvent(new TraceEvent(1,
        new BreakpointAddress(mockModule3, new UnrelocatedAddress(new CAddress(0x125))),
        TraceEventType.REGULAR_BREAKPOINT,
        Lists.newArrayList(new TraceRegister("eax", new CAddress(0x123), new byte[] {07, 05}))));

    assertEquals(0, model.getRowCount());

    model.setEventList(list);

    assertEquals(3, model.getRowCount());

    final IFilter<ITraceEvent> filter = new CTraceFilterCreator().createFilter("mem == 05");

    model.setFilter(filter);

    assertEquals(2, model.getRowCount());
    assertEquals(2, model.getEvents().size());

    assertEquals(0x123, model.getEvents().get(0).getOffset().getAddress().getAddress().toLong());
    assertEquals(0x125, model.getEvents().get(1).getOffset().getAddress().getAddress().toLong());

    model.setFilter(null);

    assertEquals(3, model.getRowCount());

    for (int i = 0; i < model.getRowCount(); i++) {
      for (int j = 0; j < model.getColumnCount(); j++) {
        model.getValueAt(i, j);
      }
    }
  }
}
