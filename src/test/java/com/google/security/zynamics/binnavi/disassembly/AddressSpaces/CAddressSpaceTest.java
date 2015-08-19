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
package com.google.security.zynamics.binnavi.disassembly.AddressSpaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.MockCreator;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;
import java.util.HashMap;

@RunWith(JUnit4.class)
public final class CAddressSpaceTest {
  private CAddressSpace m_addressSpace;

  private final MockAddressSpaceConfigurationListener m_listener =
      new MockAddressSpaceConfigurationListener();

  private MockSqlProvider m_sql;

  @Before
  public void setUp() {
    m_sql = new MockSqlProvider();

    m_addressSpace = MockCreator.createAddressSpace(m_sql);

    m_addressSpace.getConfiguration().addListener(m_listener);
  }

  @Test
  public void test_C_Constructors() {
    final MockSqlProvider sql = new MockSqlProvider();

    try {
      new CAddressSpace(0, "AS Name", "AS Description", new Date(), new Date(),
          new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, null, "AS Description", new Date(), new Date(),
          new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, "AS Name", null, new Date(), new Date(),
          new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, "AS Name", "AS Description", null, new Date(),
          new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, "AS Name", "AS Description", new Date(), null,
          new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, "AS Name", "AS Description", new Date(), new Date(), null, null, sql,
          new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    try {
      new CAddressSpace(1, "AS Name", "AS Description", new Date(), new Date(),
          new HashMap<INaviModule, IAddress>(), null, null, new MockProject());
      fail();
    } catch (final Exception exception) {
    }

    final CAddressSpace addressSpace =
        new CAddressSpace(1, "AS Name", "AS Description", new Date(), new Date(),
            new HashMap<INaviModule, IAddress>(), null, sql, new MockProject());

    assertEquals(1, addressSpace.getConfiguration().getId());
    assertEquals("AS Name", addressSpace.getConfiguration().getName());
    assertEquals("AS Description", addressSpace.getConfiguration().getDescription());
  }

  @Test
  public void testDebugger() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    assertNull(m_addressSpace.getConfiguration().getDebugger());
    assertNull(m_addressSpace.getConfiguration().getDebuggerTemplate());

    final DebuggerTemplate template = MockCreator.createDebuggerTemplate(m_sql);

    m_addressSpace.getConfiguration().setDebuggerTemplate(template);

    assertNull(m_addressSpace.getConfiguration().getDebugger());
    assertEquals(template, m_addressSpace.getConfiguration().getDebuggerTemplate());

    m_addressSpace.load();

    assertNotNull(m_addressSpace.getConfiguration().getDebugger());
    assertEquals(template, m_addressSpace.getConfiguration().getDebuggerTemplate());

    m_addressSpace.getConfiguration().setDebuggerTemplate(null);

    assertNull(m_addressSpace.getConfiguration().getDebugger());
    assertNull(m_addressSpace.getConfiguration().getDebuggerTemplate());
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    try {
      m_addressSpace.getConfiguration().setDescription(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_addressSpace.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription;changedModificationDate;", m_listener.events);

    // Check address space
    assertEquals("New Description", m_addressSpace.getConfiguration().getDescription());

    m_addressSpace.getConfiguration().setDescription("New Description");

    // Check listener events
    assertEquals("changedDescription;changedModificationDate;", m_listener.events);

    // Check address space
    assertEquals("New Description", m_addressSpace.getConfiguration().getDescription());
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    try {
      m_addressSpace.getConfiguration().setName(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_addressSpace.getConfiguration().setName("New Name");

    // Check listener events
    assertEquals("changedName;changedModificationDate;", m_listener.events);

    // Check address space
    assertEquals("New Name", m_addressSpace.getConfiguration().getName());

    m_addressSpace.getConfiguration().setName("New Name");

    // Check listener events
    assertEquals("changedName;changedModificationDate;", m_listener.events);

    // Check address space
    assertEquals("New Name", m_addressSpace.getConfiguration().getName());
  }
}
