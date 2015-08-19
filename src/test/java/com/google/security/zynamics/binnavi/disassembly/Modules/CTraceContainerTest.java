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
package com.google.security.zynamics.binnavi.disassembly.Modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.MockCreator;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.CTraceContainer;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class CTraceContainerTest {
  private final SQLProvider m_sql = new MockSqlProvider();

  private MockTraceContainerListener m_listener;

  private CTraceContainer m_content;

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException {
    m_listener = new MockTraceContainerListener();

    final CModule m_module = MockCreator.createModule(m_sql);
    m_module.load();

    m_content = new CTraceContainer(m_module, new FilledList<TraceList>(), m_sql);

    m_content.addListener(m_listener);
  }

  /**
   * Tests trace creation.
   */
  @Test
  public void testTraces() throws CouldntSaveDataException, CouldntDeleteException {
    assertEquals(0, m_content.getTraceCount());

    try {
      m_content.createTrace(null, "New Trace Description");

      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_content.createTrace("New Trace", null);

      fail();
    } catch (final NullPointerException exception) {
    }

    final TraceList newTrace = m_content.createTrace("New Trace", "New Trace Description");

    // Check listener events
    assertEquals("addedTrace/", m_listener.eventList);
    assertEquals(newTrace, m_listener.addedTraces.get(0));

    // Check module
    assertEquals(1, m_content.getTraceCount());
    assertEquals(newTrace, m_content.getTraces().get(0));

    // Check trace
    assertEquals("New Trace", newTrace.getName());
    assertEquals("New Trace Description", newTrace.getDescription());

    final TraceList newTrace2 = m_content.createTrace("New Trace II", "New Trace Description II");

    // Check listener events
    assertEquals("addedTrace/addedTrace/", m_listener.eventList);
    assertEquals(newTrace2, m_listener.addedTraces.get(1));

    // Check module
    assertEquals(2, m_content.getTraceCount());
    assertEquals(newTrace, m_content.getTraces().get(0));
    assertEquals(newTrace2, m_content.getTraces().get(1));

    // Check trace
    assertEquals("New Trace II", newTrace2.getName());
    assertEquals("New Trace Description II", newTrace2.getDescription());

    // ----------------------------------------- Delete the traces again
    // ------------------------------------------------

    m_content.deleteTrace(newTrace);

    // Check listener events
    assertEquals("addedTrace/addedTrace/deletedTrace/", m_listener.eventList);
    assertEquals(newTrace, m_listener.deletedTraces.get(0));

    // Check module
    assertEquals(1, m_content.getTraceCount());
    assertEquals(newTrace2, m_content.getTraces().get(0));

    m_content.deleteTrace(newTrace2);

    // Check listener events
    assertEquals("addedTrace/addedTrace/deletedTrace/deletedTrace/", m_listener.eventList);
    assertEquals(newTrace2, m_listener.deletedTraces.get(1));

    // Check module
    assertEquals(0, m_content.getTraceCount());

    try {
      m_content.deleteTrace(newTrace2);
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      m_content.deleteTrace(null);
      fail();
    } catch (final NullPointerException exception) {
    }
  }

}
