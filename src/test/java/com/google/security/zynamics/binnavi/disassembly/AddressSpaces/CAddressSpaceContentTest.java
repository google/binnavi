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
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.MockCreator;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public class CAddressSpaceContentTest {
  private CAddressSpace m_addressSpace;

  private final MockAddressSpaceContentListener m_listener = new MockAddressSpaceContentListener();

  private MockSqlProvider m_sql;

  @Before
  public void setUp() {
    m_sql = new MockSqlProvider();

    m_addressSpace = MockCreator.createAddressSpace(m_sql);
  }

  @Test
  public void testAddModule() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final CModule module = MockCreator.createModule(m_sql);

    module.getConfiguration().setImageBase(new CAddress(12322));

    try {
      m_addressSpace.getContent().addModule(module);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_addressSpace.load();
    m_addressSpace.getContent().addListener(m_listener);

    try {
      m_addressSpace.getContent().addModule(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_addressSpace.getContent().addModule(module);

    // Check listener
    assertEquals("addedModule;changedImageBase;", m_listener.events);

    // Check address space
    assertEquals(1, m_addressSpace.getModuleCount());
    assertEquals(module, m_addressSpace.getContent().getModules().get(0));
    assertEquals(BigInteger.valueOf(12322), m_addressSpace.getContent().getImageBase(module)
        .toBigInteger());

    try {
      m_addressSpace.getContent().addModule(module);
      fail();
    } catch (final IllegalArgumentException exception) {
    }
  }

  @Test
  public void testRelocation() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final CModule module = MockCreator.createModule(m_sql);

    m_addressSpace.load();
    m_addressSpace.getContent().addListener(m_listener);

    try {
      m_addressSpace.getContent().setImageBase(module, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_addressSpace.getContent().setImageBase(null, new CAddress(0x123));
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_addressSpace.getContent().setImageBase(module, new CAddress(0x123));
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    m_addressSpace.getContent().addModule(module);

    // Check the address space
    assertEquals("00000666", m_addressSpace.getContent().getImageBase(module).toHexString());

    m_addressSpace.getContent().setImageBase(module, new CAddress(0x123));

    // Check the listener events
    assertEquals("addedModule;changedImageBase;changedImageBase;", m_listener.events);

    // Check the address space
    assertEquals("00000123", m_addressSpace.getContent().getImageBase(module).toHexString());
    assertEquals("00000666", module.getConfiguration().getImageBase().toHexString());
  }

  @Test
  public void testRemoveModule() throws CouldntSaveDataException, CouldntLoadDataException,
      CouldntDeleteException, LoadCancelledException {
    final CModule module = MockCreator.createModule(m_sql);

    m_addressSpace.load();
    m_addressSpace.getContent().addListener(m_listener);

    try {
      m_addressSpace.getContent().removeModule(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_addressSpace.getContent().removeModule(module);
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    m_addressSpace.getContent().addModule(module);

    m_addressSpace.getContent().removeModule(module);

    // Check the listener events
    assertEquals("addedModule;changedImageBase;removedModule;", m_listener.events);

    // Check the address space
    assertEquals(0, m_addressSpace.getModuleCount());
  }

}
