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
package com.google.security.zynamics.binnavi.disassembly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.MockAddressSpaceListener;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.CProjectContainer;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.MockAddress;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Date;

@RunWith(JUnit4.class)
public class CProjectContainerTest {
  private MockSqlProvider m_provider;
  private MockModule m_module;
  private MockDatabase m_database;
  private CProject m_project;
  private CAddressSpace m_space;
  private MockFunction m_function;
  private DebuggerTemplate m_debugger;
  @SuppressWarnings("unused")
  private CView m_view1;
  @SuppressWarnings("unused")
  private CView m_view2;
  private MockAddressSpaceListener m_listener;

  @Before
  public void setUp() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, IllegalArgumentException, SecurityException,
      IllegalAccessException, NoSuchFieldException {
    m_provider = new MockSqlProvider();
    m_module = new MockModule(m_provider);
    m_database = new MockDatabase(m_provider);
    m_function = new MockFunction(m_provider);

    m_project =
        new CProject(123, "Name", "Comment", new Date(), new Date(), 55,
            new FilledList<DebuggerTemplate>(), m_provider);

    m_project.load();

    m_space = m_project.getContent().createAddressSpace("space");

    m_space.load();

    m_space.getContent().addModule(m_module);

    m_debugger = new DebuggerTemplate(2, "gdb", "local", 2222, m_provider);
    m_space.getConfiguration().setDebuggerTemplate(m_debugger);

    m_listener = new MockAddressSpaceListener();

    m_space.addListener(m_listener);

    m_module.load();

    CFunctionContainerHelper.addFunction(m_module.getContent().getFunctionContainer(), m_function);

    final CView view = m_module.getContent().getViewContainer().createView("foo", "bar");

    @SuppressWarnings("unused")
    final MockViewContainer mockViewContainer = new MockViewContainer();

    final MockViewListener listener = new MockViewListener();
    view.addListener(listener);

  }

  @Test
  public void testContainsModule() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertFalse(container.containsModule(new MockModule()));
    assertTrue(container.containsModule(m_module));

    try {
      container.containsModule(null);
      fail();
    } catch (final NullPointerException e) {

    }
  }

  @Test
  public void testCreateView() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);

    try {
      container.createView(null, null);
      fail();
    } catch (final NullPointerException e) {
    }
    try {
      container.createView("seppel", null);
      fail();
    } catch (final NullPointerException e) {
    }

    final INaviView view = container.createView("foo", "berT");
    assertNotNull(view);
  }

  @Test
  public void testDeleteView() throws CouldntDeleteException {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    final INaviView view = container.createView("foo", "berT");
    assertNotNull(view);

    try {
      container.deleteView(null);
      fail();
    } catch (final NullPointerException e) {
    }

    container.deleteView(view);

    assertEquals(2, container.getViews().size());
  }

  @Test
  public void testDispose() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project, m_space);

    container.dispose();
  }

  @Test
  public void testGetAddressSpaces() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project, m_space);

    assertEquals(2, container.getAddressSpaces().size());
  }

  @Test
  public void testGetDatabase() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertEquals(m_database, container.getDatabase());
  }

  @Test
  public void testGetDebuggerProvider() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertNotNull(container.getDebuggerProvider());
  }

  @Test
  public void testGetFunction() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    final INaviView view = container.createView("foo", "berT");

    try {
      container.getFunction(null);
      fail();
    } catch (final NullPointerException e) {
    }

    assertNull(container.getFunction(view));
  }

  @Test
  public void testGetFunctions() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertEquals(0, container.getFunctions().size());
  }

  @Test
  public void testGetModules() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertEquals(1, container.getModules().size());
  }

  @Test
  public void testGetName() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertEquals("Name", container.getName());
  }

  @Test
  public void testGetTaggedViews() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertEquals(0, container.getTaggedViews().size());
  }

  // @Test
  // public void testGetTaggedViews2()
  // {
  // final CProjectContainer container = new CProjectContainer(m_database, m_project);
  // assertEquals(0, container.getTaggedViews()));
  // }

  @Test
  public void testGetTraceProvider() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertNotNull(container.getTraceProvider());
  }

  @Test
  public void testGetView() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);

    @SuppressWarnings("unused")
    final INaviView view = container.createView("foo", "berT");

    assertNull(container.getView(m_function));
  }

  @Test
  public void testGetViews() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);

    final INaviView view = container.createView("view1", "information");

    assertEquals(3, container.getViews().size());
    assertTrue(container.getViews().contains(view));
  }

  @Test
  public void testGetViewsWithAddresses() throws CouldntLoadDataException {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);

    @SuppressWarnings("unused")
    final INaviView view = container.createView("view1", "information");

    final UnrelocatedAddress address = new UnrelocatedAddress(new MockAddress());

    final IFilledList<UnrelocatedAddress> addresses = new FilledList<UnrelocatedAddress>();
    addresses.add(address);

    try {
      container.getViewsWithAddresses(null, true);
      fail();
    } catch (final NullPointerException e) {
    }

    // assertNull(container.getViewsWithAddresses(addresses, true));
  }

  @Test
  public void testIsLoaded() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    assertTrue(container.isLoaded());
  }

  @Test
  public void testListenersSomehow() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException, CouldntDeleteException {
    @SuppressWarnings("unused")
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    final DebuggerTemplate template = new DebuggerTemplate(2, "bla", "horst", 2222, m_provider);
    m_space.getConfiguration().setDebuggerTemplate(template);
    m_space.getConfiguration().setDebuggerTemplate(null);

    assertEquals(1, m_space.getContent().getModules().size());

    m_space.getContent().removeModule(m_module);

    m_space.getContent().addModule(m_module);

    m_module.load();

    m_project.load();

    m_space.close();
    m_space.load();
  }

  @Test
  public void testRemoveListener() {
    final CProjectContainer container = new CProjectContainer(m_database, m_project);
    try {
      container.removeListener(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
