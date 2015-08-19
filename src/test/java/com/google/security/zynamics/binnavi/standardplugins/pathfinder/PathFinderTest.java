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
package com.google.security.zynamics.binnavi.standardplugins.pathfinder;

import static org.junit.Assert.assertEquals;

import com.google.security.zynamics.binnavi.API.disassembly.BasicBlock;
import com.google.security.zynamics.binnavi.API.disassembly.CodeNode;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Database;
import com.google.security.zynamics.binnavi.API.disassembly.EdgeType;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.PartialLoadException;
import com.google.security.zynamics.binnavi.API.disassembly.TagManager;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.disassembly.ViewEdge;
import com.google.security.zynamics.binnavi.API.disassembly.ViewNode;
import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.zylib.types.trees.Tree;
import com.google.security.zynamics.zylib.types.trees.TreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

@RunWith(JUnit4.class)
public final class PathFinderTest {
  private Module m_notepad;
  private Module m_kernel32;

  private static BasicBlock findBlock(final Function function, final long offset)
      throws CouldntLoadDataException {
    if (!function.isLoaded()) {
      function.load();
    }

    for (final BasicBlock block : function.getGraph().getNodes()) {
      if (block.getAddress().toLong() == offset) {
        return block;
      }
    }

    return null;
  }

  private static ViewEdge findEdge(final List<ViewEdge> edges, final long source, final long target) {
    for (final ViewEdge edge : edges) {
      if ((((CodeNode) edge.getSource()).getAddress().toLong() == source)
          && (((CodeNode) edge.getTarget()).getAddress().toLong() == target)) {
        return edge;
      }
    }

    return null;
  }

  private static Function findFunction(final Module module, final long offset) {
    for (final Function function : module.getFunctions()) {
      if (function.getAddress().toLong() == offset) {
        return function;
      }
    }

    return null;
  }

  private static ViewNode findNode(final List<ViewNode> nodes, final long address) {
    for (final ViewNode node : nodes) {
      if (((CodeNode) node).getAddress().toLong() == address) {
        return node;
      }
    }

    return null;
  }

  @Before
  public void setUp() throws IOException, IllegalStateException, CouldntLoadDataException,
      InvalidDatabaseException, InvalidExporterDatabaseFormatException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException,
      com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException,
      InvalidDatabaseVersionException, LoadCancelledException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    final CDatabase database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0],
            "test_disassembly", parts[1], parts[2], parts[3], false, false);

    final Database db = new Database(database);

    database.connect();
    database.load();

    final MockSqlProvider provider = new MockSqlProvider();

    final TagManager nodeTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.NODE_TAG, provider))), TagType.NODE_TAG, provider));
    final TagManager viewTagManager =
        new TagManager(new CTagManager(new Tree<CTag>(new TreeNode<CTag>(new CTag(0, "", "",
            TagType.VIEW_TAG, provider))), TagType.VIEW_TAG, provider));

    m_notepad = new Module(db, database.getContent().getModule(1), nodeTagManager, viewTagManager);
    m_notepad.load();

    m_kernel32 = new Module(db, database.getContent().getModule(2), nodeTagManager, viewTagManager);
    m_kernel32.load();
  }

  @Test
  public void testFirstBlock() throws CouldntLoadDataException, PartialLoadException {
    // Tests 100337E -> 1005179 -> 1007568 where all calls are in the first block
    // of the respective functions.
    // Tests path finding from the beginning to the end of a single function

    final Function startFunction = findFunction(m_notepad, 0x100337E);
    final BasicBlock startBlock = findBlock(startFunction, 0x10033C2);

    final Function endFunction = findFunction(m_notepad, 0x1007568);
    final BasicBlock endBlock = findBlock(endFunction, 0x1007568);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(3, view.getGraph().nodeCount());
    assertEquals(2, view.getGraph().edgeCount());
  }

  // @Test
  // public void testFoo() throws CouldntLoadDataException, CouldntSaveDataException
  // {
  // // TODO: Bring this test back in msw3prt.idb
  //
  // final Function startFunction = findFunction(m_foo, 0x5FEF8426);
  // final BasicBlock startBlock = findBlock(startFunction, 0x5FEF8426);
  //
  // final Function endFunction = findFunction(m_foo, 0x5FEFF06D);
  // final BasicBlock endBlock = findBlock(endFunction, 0x5FEFF0DB);
  //
  // final View view = PathFinder.createPath(m_foo, startBlock, endBlock, null, null);
  //
  // assertEquals(46, view.getGraph().nodeCount());
  // assertEquals(49, view.getGraph().edgeCount());
  // }

  @Test
  public void testInsideFunction() throws CouldntLoadDataException, PartialLoadException {
    // Tests path finding from the beginning to the end of a single function
    final Function startFunction = findFunction(m_notepad, 0x01002B87);

    final BasicBlock startBlock = findBlock(startFunction, 0x1002B87);
    final BasicBlock endBlock = findBlock(startFunction, 0x100336A);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(96, view.getGraph().nodeCount());
    assertEquals(150, view.getGraph().edgeCount());
  }

  @Test
  public void testInsideFunctionPartial() throws CouldntLoadDataException, PartialLoadException {
    // Tests path finding somewhere inside a function
    final Function startFunction = findFunction(m_notepad, 0x01002452);

    final BasicBlock startBlock = findBlock(startFunction, 0x10024C2);
    final BasicBlock endBlock = findBlock(startFunction, 0x10026FB);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(9, view.getGraph().nodeCount());
    assertEquals(11, view.getGraph().edgeCount());

    final List<ViewEdge> edges = view.getGraph().getEdges();
    final List<ViewNode> nodes = view.getGraph().getNodes();

    assertEquals(EdgeType.JumpConditionalFalse, findEdge(edges, 0x10024C2, 0x1002523).getType());
    assertEquals(EdgeType.JumpConditionalTrue, findEdge(edges, 0x10024C2, 0x1002539).getType());
    assertEquals(EdgeType.JumpUnconditional, findEdge(edges, 0x100253F, 0x10026F9).getType());

    assertEquals(Color.GREEN, findNode(nodes, 0x10024C2).getColor());
    assertEquals(Color.YELLOW, findNode(nodes, 0x10026FB).getColor());
  }

  @Test
  public void testPassingFunctionContinue() throws CouldntLoadDataException, PartialLoadException {
    // Tests pathfinding from one function to another function while passing one function
    // and having a target block that is NOT a RETURN block.
    //
    // What should happen here is that multiple paths are generated because the target
    // block is not necessarily hit the first time the function is entered.

    // 0x1004565 -> 0x1003C92 -> 0x100398D

    final Function startFunction = findFunction(m_notepad, 0x1004565);
    final BasicBlock startBlock = findBlock(startFunction, 0x1004629);

    final Function endFunction = findFunction(m_notepad, 0x100398D);
    final BasicBlock endBlock = findBlock(endFunction, 0x10039D1);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(37, view.getGraph().nodeCount());
    assertEquals(64, view.getGraph().edgeCount());
  }

  @Test
  public void testPassingFunctionReturn() throws CouldntLoadDataException, PartialLoadException {
    // Tests pathfinding from one function to another function while passing one function
    // and having a target block that is a RETURN block.
    //
    // What should happen here is that the pathfinding algorithm stops when it reaches
    // the RETURN node. That is consecutive calls to the target function should not
    // be part of the pathfinding result.

    // 0x1004565 -> 0x1003C92 -> 0x100398D

    final Function startFunction = findFunction(m_notepad, 0x1004565);
    final BasicBlock startBlock = findBlock(startFunction, 0x1004629);

    final Function endFunction = findFunction(m_notepad, 0x100398D);
    final BasicBlock endBlock = findBlock(endFunction, 0x10039D9);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(14, view.getGraph().nodeCount());
    assertEquals(19, view.getGraph().edgeCount());
  }

  @Test
  public void testRecursivePath() throws CouldntLoadDataException, PartialLoadException {
    // Tests pathfinding from a simple function to a simple function through
    // a recursive path

    final Function startFunction = findFunction(m_kernel32, 0x7C82E8B2); // GetVolumePathNameA
    final BasicBlock startBlock = findBlock(startFunction, 0x7C82E8B2);

    final Function endFunction = findFunction(m_kernel32, 0x7C8092B0);
    final BasicBlock endBlock = findBlock(endFunction, 0x7C8092B0);

    final View view = PathFinder.createPath(m_kernel32, startBlock, endBlock, null, null);

    assertEquals(1247, view.getGraph().nodeCount());
    assertEquals(1988, view.getGraph().edgeCount());
  }

  @Test
  public void testRecursiveTarget() throws CouldntLoadDataException, PartialLoadException {
    // Tests pathfinding from a simple function to a self-recursive function

    final Function startFunction = findFunction(m_kernel32, 0x7C866E7B); // SetCommConfig
    final BasicBlock startBlock = findBlock(startFunction, 0x7C866EF3);

    final Function endFunction = findFunction(m_kernel32, 0x7C865E16); // SetCommState
    final BasicBlock endBlock = findBlock(endFunction, 0x7C866106);

    final View view = PathFinder.createPath(m_kernel32, startBlock, endBlock, null, null);

    assertEquals(2 /** calling function **/
    + 66 /** called function **/
    + 3 /** split blocks **/
    , view.getGraph().nodeCount());
    assertEquals(99 /** called function **/
    + 1 /** calling target function **/
    + 3 + 3 /** recursive calls and returns **/
    , view.getGraph().edgeCount());
  }

  @Test
  public void testRegularFunction() throws CouldntLoadDataException, PartialLoadException {
    // Tests pathfinding between two simple functions

    // 0x1004565
    // 0x1003CD7

    final Function startFunction = findFunction(m_notepad, 0x1004565);
    final BasicBlock startBlock = findBlock(startFunction, 0x1004629);

    final Function endFunction = findFunction(m_notepad, 0x1003C92);
    final BasicBlock endBlock = findBlock(endFunction, 0x1003CD7);

    final View view = PathFinder.createPath(m_notepad, startBlock, endBlock, null, null);

    assertEquals(7, view.getGraph().nodeCount());
    assertEquals(8, view.getGraph().edgeCount());
  }

  @Test
  public void testToImportedFunction() throws CouldntLoadDataException, PartialLoadException {
    // Tests from the beginning of a function to an imported function
    final Function startFunction = findFunction(m_notepad, 0x0100398D);

    final BasicBlock startBlock = findBlock(startFunction, 0x100398D);

    final Function endFunction = findFunction(m_notepad, 0x1001000);
    endFunction.load();

    final View view = PathFinder.createPath(m_notepad, startBlock, null, null, endFunction);

    assertEquals(3, view.getGraph().nodeCount());
    assertEquals(2, view.getGraph().edgeCount());
  }
}
