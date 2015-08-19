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
package com.google.security.zynamics.binnavi.API.debug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.ModuleFactory;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.API.disassembly.Trace;
import com.google.security.zynamics.binnavi.API.disassembly.TracePoint;
import com.google.security.zynamics.binnavi.Database.Interfaces.IDatabase;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Debug.Debugger.MockDebugger;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.ModuleTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValues;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.ThreadRegisters;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.types.trees.ITreeNode;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;

@RunWith(JUnit4.class)
public final class TraceLoggerTest {
  private TraceLogger m_logger;

  private MockDebugger m_mockDebugger;

  private TraceLogger m_projectLogger;

  private final MockModule mockModule = new MockModule();

  private Module m_mockModule = ModuleFactory.get(mockModule);

  private ModuleTargetSettings m_debugSettings;

  @Before
  public void setUp() throws CouldntLoadDataException, InvalidDatabaseVersionException {

    final SQLProvider mockProvider = new MockSqlProvider();

    final IDatabase internalDatabase = new MockDatabase(); // CDatabase("", "", "", "", "", false,
                                                           // false);

    final Database database = new Database(internalDatabase);

    database.load();

    final MockProject mockProject = new MockProject();

    final ITreeNode<CTag> nodeRootNode =
        new TreeNode<CTag>(new CTag(0, "", "", TagType.NODE_TAG, mockProvider));
    final Tree<CTag> nodeTagTree = new Tree<CTag>(nodeRootNode);
    final TagManager nodeTagManager =
        new TagManager(new CTagManager(nodeTagTree, TagType.NODE_TAG, mockProvider));

    final ITreeNode<CTag> viewRootNode =
        new TreeNode<CTag>(new CTag(0, "", "", TagType.VIEW_TAG, mockProvider));
    final Tree<CTag> viewTagTree = new Tree<CTag>(viewRootNode);
    final TagManager viewTagManager =
        new TagManager(new CTagManager(viewTagTree, TagType.VIEW_TAG, mockProvider));

    final Module module = new Module(database, mockModule, nodeTagManager, viewTagManager);
    final Project project = new Project(database, mockProject, nodeTagManager, viewTagManager);

    m_mockModule = new Module(database, mockModule, viewTagManager, nodeTagManager);

    m_debugSettings = new ModuleTargetSettings(m_mockModule.getNative());

    m_mockDebugger = new MockDebugger(m_debugSettings);
    m_mockDebugger.setAddressTranslator(mockModule, new CAddress(0), new CAddress(0x1000));
    final Debugger debugger = new Debugger(m_mockDebugger);

    m_logger = new TraceLogger(debugger, module);
    m_projectLogger = new TraceLogger(debugger, project);
  }

  @After
  public void tearDown() {
    m_mockDebugger.close();
  }

  @Test
  public void testEmptyHits() throws CouldntSaveDataException, DebugExceptionWrapper {
    m_mockDebugger.connect();

    final Trace trace = m_logger.start("Name", "Description",
        Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));
    m_logger.stop();

    assertEquals("Name", trace.getName());
    assertEquals("Description", trace.getDescription());
    assertTrue(trace.getEvents().isEmpty());
  }

  @Test
  public void testHits() throws CouldntSaveDataException, DebugExceptionWrapper {
    m_mockDebugger.connect();

    final Trace trace = m_logger.start("Name", "Description", Lists.newArrayList(
        new TracePoint(m_mockModule, new Address(0x100)),
        new TracePoint(m_mockModule, new Address(0x100))));

    m_mockDebugger.connection.m_synchronizer.receivedEvent(new EchoBreakpointHitReply(0, 0,
        0, new RegisterValues(Lists.<ThreadRegisters>newArrayList(new ThreadRegisters(
            0, Lists.newArrayList(new RegisterValue("esp", BigInteger.valueOf(0x1100), new byte[0],
                true, false)))))));

    m_logger.stop();

    assertEquals("Name", trace.getName());
    assertEquals("Description", trace.getDescription());
    assertEquals(1, trace.getEvents().size());
    assertEquals(0x100, trace.getEvents().get(0).getAddress().toLong());

    assertEquals("TraceLogger [Debugger 'Mock' : Mock Module]", m_logger.toString());
  }

  @Test
  public void testHitsProject() throws CouldntSaveDataException, DebugExceptionWrapper {
    m_mockDebugger.connect();

    final Trace trace = m_projectLogger.start("Name", "Description",
        Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));

    m_mockDebugger.connection.m_synchronizer.receivedEvent(new EchoBreakpointHitReply(0, 0,
        0, new RegisterValues(Lists.<ThreadRegisters>newArrayList(new ThreadRegisters(
            0, Lists.newArrayList(new RegisterValue("esp", BigInteger.valueOf(0x1100), new byte[0],
                true, false)))))));

    m_projectLogger.stop();

    assertEquals("Name", trace.getName());
    assertEquals("Description", trace.getDescription());
    assertEquals(1, trace.getEvents().size());
    assertEquals(0x100, trace.getEvents().get(0).getAddress().toLong());

    assertEquals("TraceLogger [Debugger 'Mock' : Mock Project]", m_projectLogger.toString());
  }

  @Test
  public void testStartErrors() throws CouldntSaveDataException, DebugExceptionWrapper {
    try {
      @SuppressWarnings("unused")
      final Trace trace = m_logger.start("Name", "Description",
          Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    m_mockDebugger.connect();

    try {
      @SuppressWarnings("unused")
      final Trace trace = m_logger.start(null, "Description",
          Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      @SuppressWarnings("unused")
      final Trace trace = m_logger.start("Name", null,
          Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      @SuppressWarnings("unused")
      final Trace trace = m_logger.start("Name", "Description", null);
      fail();
    } catch (final NullPointerException exception) {
    }

    try {
      @SuppressWarnings("unused")
      final Trace trace = m_logger.start("Name", "Description",
          Lists.newArrayList(new TracePoint(m_mockModule, null)));
      fail();
    } catch (final NullPointerException exception) {
    }

    @SuppressWarnings("unused")
    final Trace trace = m_logger.start("Name", "Description",
        Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));

    try {
      m_logger.start("Name", "Description",
          Lists.newArrayList(new TracePoint(m_mockModule, new Address(0x100))));
      fail();
    } catch (final IllegalArgumentException exception) {
    }

    m_logger.stop();

    try {
      m_logger.stop();
      fail();
    } catch (final NullPointerException exception) {
    }
  }
}
