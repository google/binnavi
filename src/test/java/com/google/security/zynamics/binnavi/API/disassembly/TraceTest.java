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

import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Trace;
import com.google.security.zynamics.binnavi.API.disassembly.TraceEventType;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public final class TraceTest {
  @Test
  public void testConstructor() {
    final Trace trace = new Trace(new TraceList(1, "Name", "Description", new MockSqlProvider()));

    assertEquals("Name", trace.getName());
    assertEquals("Description", trace.getDescription());
    assertEquals("Trace 'Name' [0 events]", trace.toString());
    assertEquals(0, trace.getEvents().size());
  }

  @Test
  public void testEvent() {
    final Trace trace = new Trace(new TraceList(1, "Name", "Description", new MockSqlProvider()));

    final MockTraceListener listener = new MockTraceListener();

    trace.addListener(listener);

    final MockModule module = new MockModule();
    final Module m = ModuleFactory.get(module);

    trace.addEvent(0, m, new Address(123), TraceEventType.Breakpoint);
    trace.addEvent(0, m, new Address(124), TraceEventType.EchoBreakpoint);

    assertEquals(2, trace.getEvents().size());
    assertEquals(123, trace.getEvents().get(0).getAddress().toLong());
    assertEquals(TraceEventType.Breakpoint, trace.getEvents().get(0).getType());
    assertEquals(124, trace.getEvents().get(1).getAddress().toLong());
    assertEquals(TraceEventType.EchoBreakpoint, trace.getEvents().get(1).getType());
    assertEquals("addedEvent;addedEvent;", listener.events);

    trace.removeListener(listener);
  }

  @Test
  public void testSave() throws CouldntSaveDataException {
    final Trace trace = new Trace(new TraceList(1, "Name", "Description", new MockSqlProvider()));

    trace.save();
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    final Trace trace = new Trace(new TraceList(1, "Name", "Description", new MockSqlProvider()));

    final MockTraceListener listener = new MockTraceListener();

    trace.addListener(listener);

    trace.setDescription("D1");

    assertEquals("D1", trace.getDescription());
    assertEquals("changedDescription;", listener.events);

    trace.removeListener(listener);
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    final Trace trace = new Trace(new TraceList(1, "Name", "Description", new MockSqlProvider()));

    final MockTraceListener listener = new MockTraceListener();

    trace.addListener(listener);

    trace.setName("N1");

    assertEquals("N1", trace.getName());
    assertEquals("changedName;", listener.events);

    trace.removeListener(listener);
  }
}
