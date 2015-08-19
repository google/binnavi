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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.COperandTree;
import com.google.security.zynamics.binnavi.disassembly.COperandTreeNode;
import com.google.security.zynamics.binnavi.disassembly.CStringReplacement;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockCreator;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.GraphType;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.IReference;
import com.google.security.zynamics.zylib.disassembly.MockAddress;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.types.lists.FilledList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(JUnit4.class)
public final class CModuleTest {
  private final SQLProvider m_sql = new MockSqlProvider();
  private MockModuleListener m_listener;
  private CModule m_module;
  private final String md5 = CommonTestObjects.MD5;
  private final String sha1 = CommonTestObjects.SHA1;

  @Before
  public void setUp() {
    m_listener = new MockModuleListener();
    m_module = MockCreator.createModule(m_sql);
    m_module.addListener(m_listener);
  }

  @Test
  public void test_C_Constructors() {
    try {
      new CModule(0, "Name", "Comment", new Date(), new Date(), md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CModule(1, null, "Comment", new Date(), new Date(), md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", null, new Date(), new Date(), md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", null, new Date(), md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), null, md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), null, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(),
          "123456781234567812345678123456789", sha1, 0, 0, new CAddress(0), new CAddress(0), null,
          null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, null, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5,
          "12345678123456781234567812345678123456789", 0, 0, new CAddress(0), new CAddress(0),
          null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, sha1, -1, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, sha1, 0, -1, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, sha1, 0, 0, null,
          new CAddress(0), null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, sha1, 0, 0, new CAddress(0),
          null, null, null, Integer.MAX_VALUE, false, new MockSqlProvider());
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      new CModule(1, "Name", "Comment", new Date(), new Date(), md5, sha1, 0, 0, new CAddress(0),
          new CAddress(0), null, null, Integer.MAX_VALUE, false, null);
      fail();
    } catch (final NullPointerException exception) {
    }

    final MockSqlProvider sql = new MockSqlProvider();

    final CModule module =
        new CModule(123, "Name", "Comment", new Date(), new Date(), md5, sha1, 55, 66,
            new CAddress(0x555), new CAddress(0x666), new DebuggerTemplate(1, "Mock Debugger",
                "localhaus", 88, sql), null, Integer.MAX_VALUE, false, sql);

    assertEquals(123, module.getConfiguration().getId());
    assertEquals("Name", module.getConfiguration().getName());
    assertEquals("Comment", module.getConfiguration().getDescription());
    assertEquals(md5, module.getConfiguration().getMD5());
    assertEquals(sha1, module.getConfiguration().getSha1());
    assertEquals(55, module.getFunctionCount());
    assertEquals(66, module.getCustomViewCount());
    assertEquals("00000555", module.getConfiguration().getFileBase().toHexString());
    assertEquals("00000666", module.getConfiguration().getImageBase().toHexString());
    assertEquals("Mock Debugger", module.getConfiguration().getDebuggerTemplate().getName());
    assertNotNull(module.getConfiguration().getDebugger());
    assertTrue(module.inSameDatabase(sql));
  }

  @Test
  public void test_C_getFunction() throws CouldntLoadDataException, MaybeNullException,
      LoadCancelledException {
    try {
      m_module.getContent().getFunctionContainer().getFunction((IAddress) null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_module.load();

    for (final INaviFunction function : m_module.getContent().getFunctionContainer().getFunctions()) {
      assertEquals(function,
          m_module.getContent().getFunctionContainer().getFunction(function.getAddress()));
      assertEquals(function,
          m_module.getContent().getFunctionContainer().getFunction(function.getName()));
    }
  }

  @Test
  public void test_C_getFunction2() throws CouldntLoadDataException, LoadCancelledException {
    try {
      m_module.getContent().getViewContainer().getFunction((INaviView) null);
      fail();
    } catch (final NullPointerException exception) {
    }

    m_module.load();

    int counter = 0;

    final List<INaviFunction> functions =
        m_module.getContent().getFunctionContainer().getFunctions();

    for (final INaviView view : m_module.getContent().getViewContainer().getNativeFlowgraphViews()) {
      assertEquals(functions.get(counter),
          m_module.getContent().getViewContainer().getFunction(view));

      counter++;
    }
  }

  @Test
  public void test_C_getterFunctions() throws CouldntLoadDataException, LoadCancelledException {
    assertEquals(66, m_module.getCustomViewCount());
    assertEquals(55, m_module.getFunctionCount());
    assertEquals(122, m_module.getViewCount());

    m_module.load();

    assertEquals(0, m_module.getCustomViewCount());
    assertEquals(1, m_module.getFunctionCount());
    assertEquals(2, m_module.getViewCount());
  }

  @Test
  public void testAddresses() throws CouldntSaveDataException {
    assertEquals("00000555", m_module.getConfiguration().getFileBase().toHexString());

    m_module.getConfiguration().setFileBase(new CAddress(0x02000000));

    // Check listener events
    assertEquals("changedFileBase=02000000/", m_listener.eventList);

    // Check module
    assertEquals("02000000", m_module.getConfiguration().getFileBase().toHexString());

    m_module.getConfiguration().setFileBase(new CAddress(0x02000000));

    // Check listener events
    assertEquals("changedFileBase=02000000/", m_listener.eventList);

    // ------------------------------------------ Image Base
    // -------------------------------------------------

    assertEquals("00000666", m_module.getConfiguration().getImageBase().toHexString());

    m_module.getConfiguration().setImageBase(new CAddress(0xFFFFFFFFL));

    // Check listener events
    assertEquals("changedFileBase=02000000/changedImageBase=FFFFFFFF/", m_listener.eventList);

    // Check module
    assertEquals("FFFFFFFF", m_module.getConfiguration().getImageBase().toHexString());

    m_module.getConfiguration().setImageBase(new CAddress(0xFFFFFFFFL));

    // Check listener events
    assertEquals("changedFileBase=02000000/changedImageBase=FFFFFFFF/", m_listener.eventList);
  }

  @Test
  public void testClose() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    m_listener.canClose = false;
    assertFalse(m_module.close());

    m_listener.canClose = true;
    assertTrue(m_module.close());

    try {
      m_module.close();
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testCreateInstruction() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    final byte[] data = {(byte) 0x90};
    assertNotNull(m_module.createInstruction(new MockAddress(), "add",
        new ArrayList<COperandTree>(), data, "ARM"));

  }

  @Test
  public void testCreateOperand() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    final List<IReference> references = new ArrayList<IReference>();
    final COperandTreeNode node =
        new COperandTreeNode(1, 2, "2", new CStringReplacement("bar"), references, m_sql,
            m_module.getTypeManager(), m_module.getContent().getTypeInstanceContainer());
    assertNotNull(m_module.createOperand(node));
  }

  @Test
  public void testCreateOperandExpression() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    assertNotNull(m_module.createOperandExpression("foo", ExpressionType.SYMBOL));
  }

  @Test
  public void testDebugger() throws CouldntSaveDataException {
    assertNull(m_module.getConfiguration().getDebugger());
    assertNull(m_module.getConfiguration().getDebuggerTemplate());

    final DebuggerTemplate template = MockCreator.createDebuggerTemplate(m_sql);

    m_module.getConfiguration().setDebuggerTemplate(template);

    assertNotNull(m_module.getConfiguration().getDebugger());
    assertEquals(template, m_module.getConfiguration().getDebuggerTemplate());

    m_module.getConfiguration().setDebuggerTemplate(null);

    assertNull(m_module.getConfiguration().getDebugger());
    assertNull(m_module.getConfiguration().getDebuggerTemplate());
  }

  @Test
  public void testGetData() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    assertNotNull(m_module.getData());
  }

  @Test
  public void testGetUserViews() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();

    assertNotNull(m_module.getUserViews());
  }

  @Test
  public void testGetViewsWithAddresses() throws CouldntLoadDataException, LoadCancelledException {
    final FilledList<UnrelocatedAddress> addresses = new FilledList<UnrelocatedAddress>();
    addresses.add(new UnrelocatedAddress(new CAddress(12345678)));
    m_module.load();

    assertNotNull(m_module.getViewsWithAddresses(addresses, true));
  }

  @Test
  public void testInitialize() throws CouldntSaveDataException {
    m_module.setInitialized();

    assertTrue(m_module.isInitialized());

    m_module.initialize();

    final MockModule module = new MockModule();

    assertFalse(module.isInitialized());

    module.initialize();
  }

  @Test
  public void testInSameDatabase() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    assertFalse(m_module.inSameDatabase(new MockSqlProvider()));
    assertTrue(m_module.inSameDatabase(m_sql));

    try {
      m_module.inSameDatabase((SQLProvider) null);
      fail();
    } catch (final NullPointerException e) {
    }

    final MockDatabase database = new MockDatabase();
    assertFalse(m_module.inSameDatabase(database));

    try {
      m_module.inSameDatabase((IDatabaseObject) null);
      fail();
    } catch (final NullPointerException e) {
    }

  }

  @Test
  public void testIsStared() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    assertFalse(m_module.isStared());
  }

  @Test
  public void testMiscReaders() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    m_module.loadData();
    m_module.readSetting("foo");
    m_module.isLoading();
    m_module.isInitialized();
    m_module.isInitializing();
    m_module.isLoading();

  }

  @Test
  public void testRemoveListener() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    m_module.removeListener(m_listener);
  }

  @Test
  public void testSaveData() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    m_module.load();
    m_module.saveData();
  }

  @Test
  public void testSetData() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    final byte[] bytes = {(byte) 0xFF};
    m_module.setData(bytes);

    try {
      m_module.setData(null);
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testSetDescription() throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setDescription(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals("Mock Comment", m_module.getConfiguration().getDescription());

    m_module.getConfiguration().setDescription("Test Description");

    // Check listener events
    assertEquals("changedDescription=Test Description/", m_listener.eventList);

    // Check module
    assertEquals("Test Description", m_module.getConfiguration().getDescription());

    m_module.getConfiguration().setDescription("Imported by ida2sql from BinNavi");

    assertEquals(
        "changedDescription=Test Description/changedDescription=Imported by ida2sql from BinNavi/",
        m_listener.eventList);

    m_module.getConfiguration().setDescription("Imported by ida2sql from BinNavi");

    assertEquals(
        "changedDescription=Test Description/changedDescription=Imported by ida2sql from BinNavi/",
        m_listener.eventList);

    assertEquals("Imported by ida2sql from BinNavi", m_module.getConfiguration().getDescription());
  }

  @Test
  public void testSetName() throws CouldntSaveDataException {
    try {
      m_module.getConfiguration().setName(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    assertEquals("Mock Name", m_module.getConfiguration().getName());

    m_module.getConfiguration().setName("Test Name");

    // Check listener events
    assertEquals("changedName=Test Name/", m_listener.eventList);

    // Check module
    assertEquals("Test Name", m_module.getConfiguration().getName());

    m_module.getConfiguration().setName("NOTEPAD.EXE");

    assertEquals("changedName=Test Name/changedName=NOTEPAD.EXE/", m_listener.eventList);

    m_module.getConfiguration().setName("NOTEPAD.EXE");

    assertEquals("changedName=Test Name/changedName=NOTEPAD.EXE/", m_listener.eventList);

    assertEquals("NOTEPAD.EXE", m_module.getConfiguration().getName());
  }

  @Test
  public void testTags() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException {
    final CTag newTag = MockCreator.createViewTag(m_sql);

    m_module.load();

    m_module.getContent().getViewContainer().createView("Foo", "Bar");
    m_module.getContent().getViewContainer().createView("Foo", "Bar");

    m_module.getContent().getViewContainer().getViews().get(0).getConfiguration().tagView(newTag);
    m_module.getContent().getViewContainer().getViews().get(1).getConfiguration().tagView(newTag);

    assertEquals(m_module.getContent().getViewContainer().getViews().get(0), CViewFilter
        .getTaggedViews(m_module.getContent().getViewContainer().getViews()).get(0).first());
    assertEquals(m_module.getContent().getViewContainer().getViews().get(1), CViewFilter
        .getTaggedViews(m_module.getContent().getViewContainer().getViews()).get(1).first());

    assertEquals(2,
        CViewFilter.getTaggedViews(m_module.getContent().getViewContainer().getViews(), newTag)
            .size());
  }

  @Test
  public void testToString() throws CouldntLoadDataException, LoadCancelledException {
    m_module.load();
    m_module.toString();
  }

  @Test
  public void testViews() throws CouldntLoadDataException, CouldntDeleteException,
      LoadCancelledException {
    // ---------------------------------------------- CREATE VIEWS
    // -----------------------------------------------

    try {
      m_module.getContent().getViewContainer().createView(null, "New Trace Description");
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_module.getContent().getViewContainer().createView("New Trace", null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      m_module.getContent().getViewContainer().createView("New View", "New Trace Description");
      fail();
    } catch (final NullPointerException exception) {
    }

    m_module.load();

    final CView newView =
        m_module.getContent().getViewContainer().createView("New View", "New View Description");

    // Check listener events
    assertEquals("addedView/", m_listener.eventList);
    assertEquals(newView, m_listener.addedViews.get(0));

    // Check module
    assertEquals(1, m_module.getCustomViewCount());
    assertEquals(1, m_module.getFunctionCount());
    assertEquals(3, m_module.getViewCount());

    // Check view
    assertTrue(newView.isLoaded());
    assertEquals("New View", newView.getName());
    assertEquals("New View Description", newView.getConfiguration().getDescription());
    assertEquals(0, newView.getNodeCount());
    assertEquals(0, newView.getEdgeCount());
    assertEquals(ViewType.NonNative, newView.getType());
    assertEquals(GraphType.MIXED_GRAPH, newView.getGraphType());

    // ---------------------------------------------- DELETE VIEWS
    // --------------------------------------------------

    try {
      m_module.getContent().getViewContainer().deleteView(null);
      fail();
    } catch (final NullPointerException exception) {
    }

    // Check module
    assertEquals(1, m_module.getCustomViewCount());
    assertEquals(1, m_module.getFunctionCount());
    assertEquals(3, m_module.getViewCount());

    m_module.getContent().getViewContainer().deleteView(newView);

    // Check listener events
    assertEquals("addedView/deletedView/", m_listener.eventList);
    assertEquals(newView, m_listener.deletedViews.get(0));

    // Check module
    assertEquals(0, m_module.getCustomViewCount());
    assertEquals(1, m_module.getFunctionCount());
    assertEquals(2, m_module.getViewCount());
  }

  @Test
  public void testWriteSettings() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException {
    m_module.load();
    m_module.writeSetting("foo", "bar");
  }
}
