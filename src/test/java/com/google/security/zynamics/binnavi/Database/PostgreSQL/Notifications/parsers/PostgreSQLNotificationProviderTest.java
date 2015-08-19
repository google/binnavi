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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviCodeNode;
import com.google.security.zynamics.binnavi.disassembly.INaviEdge;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviGroupNode;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviTextNode;
import com.google.security.zynamics.binnavi.disassembly.INaviViewNode;
import com.google.security.zynamics.binnavi.disassembly.algorithms.CViewInserter;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Test class for all tests related to notification synchronization between multiple 
 * instances of BinNavi.  
 */
@RunWith(JUnit4.class)
public class PostgreSQLNotificationProviderTest {
  private CDatabase databaseOne;
  private INaviModule databaseOneModuleTwo;
  private INaviFunction databaseOneFunction;
  private INaviView databaseOneView;

  private CDatabase databaseTwo;
  private INaviModule databaseTwoModuleTwo;
  private INaviFunction databaseTwoFunction;
  private INaviView databaseTwoView;


  private final CountDownLatch lock = new CountDownLatch(1);
  private ICallgraphView databaseOneCallGraph;
  private ICallgraphView databaseTwoCallGraph;

  @Before
  public void setUp()
      throws IOException,
      CouldntLoadDriverException,
      CouldntConnectException,
      IllegalStateException,
      CouldntLoadDataException,
      InvalidDatabaseException,
      CouldntInitializeDatabaseException,
      InvalidExporterDatabaseFormatException,
      LoadCancelledException,
      CPartialLoadException,
      InvalidDatabaseVersionException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    databaseOne = new CDatabase("DATABASEONE",
        CJdbcDriverNames.jdbcPostgreSQLDriverName,
        parts[0],
        "test_disassembly",
        parts[1],
        parts[2],
        parts[3],
        false,
        false);

    databaseOne.connect();
    databaseOne.load();
    databaseOneModuleTwo = databaseOne.getContent().getModule(2);
    databaseOneModuleTwo.load();
    databaseOneFunction = databaseOneModuleTwo.getContent()
        .getFunctionContainer().getFunction(new CAddress("7C880394", 16));
    databaseOneView =
        databaseOneModuleTwo.getContent().getViewContainer().getView(databaseOneFunction);
    databaseOneView.load();


    databaseTwo = new CDatabase("DATABASETWO",
        CJdbcDriverNames.jdbcPostgreSQLDriverName,
        parts[0],
        "test_disassembly",
        parts[1],
        parts[2],
        parts[3],
        false,
        false);

    databaseTwo.connect();
    databaseTwo.load();
    databaseTwoModuleTwo = databaseTwo.getContent().getModule(2);
    databaseTwoModuleTwo.load();
    databaseTwoFunction = databaseTwoModuleTwo.getContent()
        .getFunctionContainer().getFunction(new CAddress("7C880394", 16));
    databaseTwoView =
        databaseTwoModuleTwo.getContent().getViewContainer().getView(databaseTwoFunction);
    databaseTwoView.load();
  }

  @After
  public void tearDown() {
    databaseOneView.close();
    databaseOne.close();

    databaseTwoView.close();
    databaseTwo.close();
  }


  @Test
  public void testAppendFunctionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {

    final List<IComment> oneBefore =
        databaseOneFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneFunction.getGlobalComment();
    final List<IComment> twoBefore =
        databaseTwoFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunction.getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneFunction.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneFunction.getGlobalComment();
    final List<IComment> twoAfter = databaseTwoFunction.getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendFunctionNodeCommentSync()
      throws CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      CouldntSaveDataException,
      InterruptedException {

    databaseOneCallGraph =
        databaseOneModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseOneCallGraph.load();
    final INaviFunctionNode databaseOneFunctionNode =
        (INaviFunctionNode) databaseOneCallGraph.getGraph().getNodes().get(1);

    databaseTwoCallGraph =
        databaseTwoModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseTwoCallGraph.load();
    final INaviFunctionNode databaseTwoFunctionNode =
        (INaviFunctionNode) databaseTwoCallGraph.getGraph().getNodes().get(1);

    final List<IComment> oneBefore =
        databaseOneFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseOneFunctionNode.getLocalFunctionComment();

    final List<IComment> twoBefore =
        databaseTwoFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunctionNode.getLocalFunctionComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneFunctionNode.appendLocalFunctionComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneFunctionNode.getLocalFunctionComment();
    final List<IComment> twoAfter = databaseTwoFunctionNode.getLocalFunctionComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }


  @Test
  public void testAppendGlobalCodeNodeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(1);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(1);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    databaseOnecodeNode.getComments().appendGlobalCodeNodeComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendGlobalEdgeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {

    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getGlobalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneEdge.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getGlobalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

  }

  @Test
  public void testAppendGlobalInstructionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore =
            Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoBefore =
            Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    Iterables.getFirst(databaseOnecodeNode.getInstructions(), null)
        .appendGlobalComment(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter =
        Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoAfter =
        Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendGroupNodeCommentSync()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      InterruptedException {

    final CView databaseOneGroupNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" GROUP NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneGroupNodeView);
    final INaviGroupNode databaseOneGroupNode = databaseOneGroupNodeView.getContent()
        .createGroupNode(databaseOneGroupNodeView.getGraph().getNodes());
    databaseOneGroupNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoGroupNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviGroupNode databaseTwoGroupNode = null;
    assertEquals(databaseOneGroupNodeView.getName(), databaseTwoGroupNodeView.getName());
    databaseTwoGroupNodeView.load();

    for (final INaviViewNode node : databaseTwoGroupNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviGroupNode) {
        databaseTwoGroupNode = (INaviGroupNode) node;
      }
    }
    assertNotNull(databaseTwoGroupNode);
    assertEquals(databaseTwoGroupNode.getId(), databaseOneGroupNode.getId());

    databaseOneGroupNode.appendComment(" TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneGroupNode.getComments();
    final List<IComment> twoAfter = databaseTwoGroupNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendLocalCodeNodeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(2);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(2);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    databaseOnecodeNode.getComments()
        .appendLocalCodeNodeComment(" TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendLocalEdgeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getLocalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getLocalComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneEdge.appendLocalComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getLocalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getLocalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendLocalInstructionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments()
            .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoBefore = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments()
            .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertEquals(oneBefore, twoBefore);

    databaseOnecodeNode.getComments().appendLocalInstructionComment(
        databaseOnecodeNode.getLastInstruction(),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoAfter = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testAppendTextNodeCommentSync()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      InterruptedException {

    final CView databaseOneTextNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" TEXT NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneTextNodeView);
    final INaviTextNode databaseOneTextNode =
        databaseOneTextNodeView.getContent().createTextNode(new ArrayList<IComment>());
    databaseOneTextNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoTextNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviTextNode databaseTwoTextNode = null;
    assertEquals(databaseOneTextNodeView.getName(), databaseTwoTextNodeView.getName());
    databaseTwoTextNodeView.load();

    for (final INaviViewNode node : databaseTwoTextNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviTextNode) {
        databaseTwoTextNode = (INaviTextNode) node;
      }
    }
    assertNotNull(databaseTwoTextNode);
    assertEquals(databaseTwoTextNode.getId(), databaseOneTextNode.getId());

    databaseOneTextNode.appendComment(" TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneTextNode.getComments();
    final List<IComment> twoAfter = databaseTwoTextNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);
  }

  @Test
  public void testDeleteFunctionCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final List<IComment> oneOne =
        databaseOneFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneFunction.getGlobalComment();
    final List<IComment> twoOne =
        databaseTwoFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunction.getGlobalComment();

    assertEquals(oneOne, twoOne);

    final List<IComment> comments = databaseOneFunction.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) DELETE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneTwo = databaseOneFunction.getGlobalComment();
    final List<IComment> twoTwo = databaseTwoFunction.getGlobalComment();

    assertNotNull(oneTwo);
    assertNotNull(twoTwo);
    assertEquals(oneOne.size() + 1, oneTwo.size());
    assertEquals(twoOne.size() + 1, twoTwo.size());
    assertEquals(oneTwo, twoTwo);

    final int oneTwoSize = oneTwo.size();
    final int twoTwoSize = twoTwo.size();

    databaseOneFunction.deleteGlobalComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneFunction.getGlobalComment();
    final List<IComment> twoThree = databaseTwoFunction.getGlobalComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteFunctionNodeCommentSync()
      throws CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      CouldntSaveDataException,
      InterruptedException,
      CouldntDeleteException {

    databaseOneCallGraph =
        databaseOneModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseOneCallGraph.load();
    final INaviFunctionNode databaseOneFunctionNode =
        (INaviFunctionNode) databaseOneCallGraph.getGraph().getNodes().get(1);

    databaseTwoCallGraph =
        databaseTwoModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseTwoCallGraph.load();
    final INaviFunctionNode databaseTwoFunctionNode =
        (INaviFunctionNode) databaseTwoCallGraph.getGraph().getNodes().get(1);

    final List<IComment> oneBefore =
        databaseOneFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseOneFunctionNode.getLocalFunctionComment();

    final List<IComment> twoBefore =
        databaseTwoFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunctionNode.getLocalFunctionComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOneFunctionNode.appendLocalFunctionComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneFunctionNode.getLocalFunctionComment();
    final List<IComment> twoAfter = databaseTwoFunctionNode.getLocalFunctionComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneFunctionNode.deleteLocalFunctionComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneFunctionNode.getLocalFunctionComment();
    final List<IComment> twoThree = databaseTwoFunctionNode.getLocalFunctionComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteGlobalCodeNodeCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(1);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(1);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    databaseOnecodeNode.getComments().appendGlobalCodeNodeComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().deleteGlobalCodeNodeComment(
        Iterables.getLast(databaseOnecodeNode.getComments().getGlobalCodeNodeComment()));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoThree = databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteGlobalEdgeCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getGlobalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneEdge.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getGlobalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneEdge.deleteGlobalComment(Iterables.getLast(databaseOneEdge.getGlobalComment()));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneEdge.getGlobalComment();
    final List<IComment> twoThree = databaseTwoEdge.getGlobalComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteGlobalInstructionCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore =
            Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoBefore =
            Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    Iterables.getFirst(databaseOnecodeNode.getInstructions(), null)
        .appendGlobalComment(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter =
        Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoAfter =
        Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);


    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    Iterables.getFirst(databaseOnecodeNode.getInstructions(), null)
        .deleteGlobalComment(Iterables.getLast(
            Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment()));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree =
        Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoThree =
        Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteGroupNodeComment()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      LoadCancelledException,
      CPartialLoadException,
      InterruptedException,
      CouldntDeleteException {
    final CView databaseOneGroupNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" GROUP NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneGroupNodeView);
    final INaviGroupNode databaseOneGroupNode = databaseOneGroupNodeView.getContent()
        .createGroupNode(databaseOneGroupNodeView.getGraph().getNodes());
    databaseOneGroupNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoGroupNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviGroupNode databaseTwoGroupNode = null;
    assertEquals(databaseOneGroupNodeView.getName(), databaseTwoGroupNodeView.getName());
    databaseTwoGroupNodeView.load();

    for (final INaviViewNode node : databaseTwoGroupNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviGroupNode) {
        databaseTwoGroupNode = (INaviGroupNode) node;
      }
    }
    assertNotNull(databaseTwoGroupNode);
    assertEquals(databaseTwoGroupNode.getId(), databaseOneGroupNode.getId());

    final List<IComment> comments = databaseOneGroupNode.appendComment(
        " TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneGroupNode.getComments();
    final List<IComment> twoAfter = databaseTwoGroupNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneGroupNode.deleteComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneGroupNode.getComments();
    final List<IComment> twoThree = databaseTwoGroupNode.getComments();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteLocalCodeNodeCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(2);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(2);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOnecodeNode.getComments()
        .appendLocalCodeNodeComment(" TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().deleteLocalCodeNodeComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoThree = databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testDeleteLocalEdgeCommentSync() throws CouldntDeleteException, InterruptedException,
      CouldntSaveDataException, CouldntLoadDataException {
    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getLocalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getLocalComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOneEdge.appendLocalComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getLocalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getLocalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneEdge.deleteLocalComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneEdge.getLocalComment();
    final List<IComment> twoThree = databaseTwoEdge.getLocalComment();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);

  }

  @Test
  public void testDeleteLocalInstructionCommentSync() throws CouldntSaveDataException,
      CouldntLoadDataException, InterruptedException, CouldntDeleteException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments()
            .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoBefore = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments()
            .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOnecodeNode.getComments().appendLocalInstructionComment(
        databaseOnecodeNode.getLastInstruction(),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT)");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoAfter = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().deleteLocalInstructionComment(
        databaseOnecodeNode.getLastInstruction(), Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoThree = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);

  }

  @Test
  public void testDeleteTextNodeCommentSync()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      LoadCancelledException,
      CPartialLoadException,
      InterruptedException,
      CouldntDeleteException {

    final CView databaseOneTextNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" TEXT NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneTextNodeView);
    final INaviTextNode databaseOneTextNode =
        databaseOneTextNodeView.getContent().createTextNode(new ArrayList<IComment>());
    databaseOneTextNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoTextNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviTextNode databaseTwoTextNode = null;
    assertEquals(databaseOneTextNodeView.getName(), databaseTwoTextNodeView.getName());
    databaseTwoTextNodeView.load();

    for (final INaviViewNode node : databaseTwoTextNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviTextNode) {
        databaseTwoTextNode = (INaviTextNode) node;
      }
    }
    assertNotNull(databaseTwoTextNode);
    assertEquals(databaseTwoTextNode.getId(), databaseOneTextNode.getId());

    final List<IComment> comments =
        databaseOneTextNode.appendComment(" TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneTextNode.getComments();
    final List<IComment> twoAfter = databaseTwoTextNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneTextNode.deleteComment(Iterables.getLast(comments));

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneTextNode.getComments();
    final List<IComment> twoThree = databaseTwoTextNode.getComments();

    assertEquals(oneTwoSize - 1, oneThree.size());
    assertEquals(twoTwoSize - 1, twoThree.size());
    assertEquals(oneThree, twoThree);
  }

  @Test
  public void testEditFunctionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final List<IComment> oneOne =
        databaseOneFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneFunction.getGlobalComment();
    final List<IComment> twoOne =
        databaseTwoFunction.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunction.getGlobalComment();

    assertEquals(oneOne, twoOne);

    final List<IComment> comments = databaseOneFunction.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneTwo = databaseOneFunction.getGlobalComment();
    final List<IComment> twoTwo = databaseTwoFunction.getGlobalComment();

    assertNotNull(oneTwo);
    assertNotNull(twoTwo);
    assertEquals(oneOne.size() + 1, oneTwo.size());
    assertEquals(twoOne.size() + 1, twoTwo.size());
    assertEquals(oneTwo, twoTwo);

    final int oneTwoSize = oneTwo.size();
    final int twoTwoSize = twoTwo.size();

    databaseOneFunction.editGlobalComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneFunction.getGlobalComment();
    final List<IComment> twoThree = databaseTwoFunction.getGlobalComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL FUNCTION COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditFunctionNodeCommentSync()
      throws CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      CouldntSaveDataException,
      InterruptedException {
    databaseOneCallGraph =
        databaseOneModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseOneCallGraph.load();
    final INaviFunctionNode databaseOneFunctionNode =
        (INaviFunctionNode) databaseOneCallGraph.getGraph().getNodes().get(1);

    databaseTwoCallGraph =
        databaseTwoModuleTwo.getContent().getViewContainer().getNativeCallgraphView();
    databaseTwoCallGraph.load();
    final INaviFunctionNode databaseTwoFunctionNode =
        (INaviFunctionNode) databaseTwoCallGraph.getGraph().getNodes().get(1);

    final List<IComment> oneBefore =
        databaseOneFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseOneFunctionNode.getLocalFunctionComment();

    final List<IComment> twoBefore =
        databaseTwoFunctionNode.getLocalFunctionComment() == null ? new ArrayList<IComment>()
            : databaseTwoFunctionNode.getLocalFunctionComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOneFunctionNode.appendLocalFunctionComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneFunctionNode.getLocalFunctionComment();
    final List<IComment> twoAfter = databaseTwoFunctionNode.getLocalFunctionComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneFunctionNode.editLocalFunctionComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneFunctionNode.getLocalFunctionComment();
    final List<IComment> twoThree = databaseTwoFunctionNode.getLocalFunctionComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL FUNCTION NODE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditGlobalCodeNodeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {

    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(1);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(1);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getGlobalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    databaseOnecodeNode.getComments().appendGlobalCodeNodeComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().editGlobalCodeNodeComment(
        Iterables.getLast(databaseOnecodeNode.getComments().getGlobalCodeNodeComment()),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments().getGlobalCodeNodeComment();
    final List<IComment> twoThree = databaseTwocodeNode.getComments().getGlobalCodeNodeComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL CODE NODE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditGlobalEdgeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getGlobalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getGlobalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    databaseOneEdge.appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL EDGE COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getGlobalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneEdge.editGlobalComment(Iterables.getLast(databaseOneEdge.getGlobalComment()),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL EDGE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneEdge.getGlobalComment();
    final List<IComment> twoThree = databaseTwoEdge.getGlobalComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL EDGE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL EDGE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditGlobalInstructionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore =
            Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoBefore =
            Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment()
            == null ? new ArrayList<IComment>()
            : Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertEquals(oneBefore, twoBefore);

    Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).appendGlobalComment(
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter =
        Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoAfter =
        Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);


    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    Iterables.getFirst(databaseOnecodeNode.getInstructions(), null)
        .editGlobalComment(Iterables.getLast(
            Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment()),
            " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree =
        Iterables.getFirst(databaseOnecodeNode.getInstructions(), null).getGlobalComment();
    final List<IComment> twoThree =
        Iterables.getFirst(databaseTwocodeNode.getInstructions(), null).getGlobalComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditGroupNodeComment()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      InterruptedException {
    final CView databaseOneGroupNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" GROUP NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneGroupNodeView);
    final INaviGroupNode databaseOneGroupNode = databaseOneGroupNodeView.getContent()
        .createGroupNode(databaseOneGroupNodeView.getGraph().getNodes());
    databaseOneGroupNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoGroupNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviGroupNode databaseTwoGroupNode = null;
    assertEquals(databaseOneGroupNodeView.getName(), databaseTwoGroupNodeView.getName());
    databaseTwoGroupNodeView.load();

    for (final INaviViewNode node : databaseTwoGroupNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviGroupNode) {
        databaseTwoGroupNode = (INaviGroupNode) node;
      }
    }
    assertNotNull(databaseTwoGroupNode);
    assertEquals(databaseTwoGroupNode.getId(), databaseOneGroupNode.getId());

    final List<IComment> comments = databaseOneGroupNode.appendComment(
        " TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) BEFORE ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneGroupNode.getComments();
    final List<IComment> twoAfter = databaseTwoGroupNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneGroupNode.editComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneGroupNode.getComments();
    final List<IComment> twoThree = databaseTwoGroupNode.getComments();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);

    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GROUP NODE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditLocalCodeNodeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(2);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(2);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoBefore = databaseTwocodeNode.getComments().getLocalCodeNodeComment()
        == null ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOnecodeNode.getComments().appendLocalCodeNodeComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoAfter = databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().editLocalCodeNodeComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments().getLocalCodeNodeComment();
    final List<IComment> twoThree = databaseTwocodeNode.getComments().getLocalCodeNodeComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);

    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL CODE NODE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditLocalEdgeCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviEdge databaseOneEdge = databaseOneView.getContent().getGraph().getEdges().get(3);
    final INaviEdge databaseTwoEdge = databaseTwoView.getContent().getGraph().getEdges().get(3);

    final List<IComment> oneBefore =
        databaseOneEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseOneEdge.getLocalComment();
    final List<IComment> twoBefore =
        databaseTwoEdge.getLocalComment() == null ? new ArrayList<IComment>()
            : databaseTwoEdge.getLocalComment();

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOneEdge.appendLocalComment(
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL EDGE COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneEdge.getLocalComment();
    final List<IComment> twoAfter = databaseTwoEdge.getLocalComment();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneEdge.editLocalComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (LOCAL EDGE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneEdge.getLocalComment();
    final List<IComment> twoThree = databaseTwoEdge.getLocalComment();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL EDGE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (LOCAL EDGE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditLocalInstructionCommentSync()
      throws CouldntSaveDataException, CouldntLoadDataException, InterruptedException {
    final INaviCodeNode databaseOnecodeNode = databaseOneView.getContent().getBasicBlocks().get(3);
    final INaviCodeNode databaseTwocodeNode = databaseTwoView.getContent().getBasicBlocks().get(3);

    final List<IComment> oneBefore = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseOnecodeNode.getComments()
            .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoBefore = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction()) == null
        ? new ArrayList<IComment>()
        : databaseTwocodeNode.getComments()
            .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertEquals(oneBefore, twoBefore);

    final List<IComment> comments = databaseOnecodeNode.getComments().appendLocalInstructionComment(
        databaseOnecodeNode.getLastInstruction(),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) BEFORE ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoAfter = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(oneBefore.size() + 1, oneAfter.size());
    assertEquals(twoBefore.size() + 1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOnecodeNode.getComments().editLocalInstructionComment(
        databaseOnecodeNode.getLastInstruction(), Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOnecodeNode.getComments()
        .getLocalInstructionComment(databaseOnecodeNode.getLastInstruction());
    final List<IComment> twoThree = databaseTwocodeNode.getComments()
        .getLocalInstructionComment(databaseTwocodeNode.getLastInstruction());

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (GLOBAL INSTRUCTION COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }

  @Test
  public void testEditTextNodeCommentSync()
      throws CouldntSaveDataException,
      CouldntLoadDataException,
      CPartialLoadException,
      LoadCancelledException,
      InterruptedException {
    final CView databaseOneTextNodeView = databaseOneModuleTwo.getContent()
        .getViewContainer().createView(" TEXT NODE TESTING VIEW ", "");
    CViewInserter.insertView(databaseOneView, databaseOneTextNodeView);
    final INaviTextNode databaseOneTextNode =
        databaseOneTextNodeView.getContent().createTextNode(new ArrayList<IComment>());
    databaseOneTextNodeView.save();

    databaseTwoModuleTwo.close();
    databaseTwoModuleTwo.load();
    databaseTwoView.load();

    final INaviView databaseTwoTextNodeView =
        Iterables.getLast(databaseTwoModuleTwo.getContent().getViewContainer().getUserViews());

    INaviTextNode databaseTwoTextNode = null;
    assertEquals(databaseOneTextNodeView.getName(), databaseTwoTextNodeView.getName());
    databaseTwoTextNodeView.load();

    for (final INaviViewNode node : databaseTwoTextNodeView.getContent().getGraph().getNodes()) {
      if (node instanceof INaviTextNode) {
        databaseTwoTextNode = (INaviTextNode) node;
      }
    }
    assertNotNull(databaseTwoTextNode);
    assertEquals(databaseTwoTextNode.getId(), databaseOneTextNode.getId());

    final List<IComment> comments = databaseOneTextNode.appendComment(
        " TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) BEFORE ");

    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneAfter = databaseOneTextNode.getComments();
    final List<IComment> twoAfter = databaseTwoTextNode.getComments();

    assertNotNull(oneAfter);
    assertNotNull(twoAfter);
    assertEquals(1, oneAfter.size());
    assertEquals(1, twoAfter.size());
    assertEquals(oneAfter, twoAfter);

    final int oneTwoSize = oneAfter.size();
    final int twoTwoSize = twoAfter.size();

    databaseOneTextNode.editComment(Iterables.getLast(comments),
        " TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) AFTER ");

    // The wait is necessary to have the poll function complete and propagate the changes from
    // database one to two over the PostgreSQL back end.
    synchronized (lock) {
      lock.await(1000, TimeUnit.MILLISECONDS);
    }

    final List<IComment> oneThree = databaseOneTextNode.getComments();
    final List<IComment> twoThree = databaseTwoTextNode.getComments();

    assertEquals(oneTwoSize, oneThree.size());
    assertEquals(twoTwoSize, twoThree.size());
    assertEquals(oneThree, twoThree);
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) AFTER ",
        Iterables.getLast(oneThree).getComment());
    assertEquals(" TEST NOTIFICATION PROVIDER TESTS (TEXT NODE COMMENT) AFTER ",
        Iterables.getLast(twoThree).getComment());
  }
}
