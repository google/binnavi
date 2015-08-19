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

import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Common.CommonTestObjects;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockDatabase;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.CComment;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CommentDialogs.Interfaces.IComment;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.MockTagManager;
import com.google.security.zynamics.binnavi.disassembly.CFunction;
import com.google.security.zynamics.binnavi.disassembly.CFunctionNode;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.FunctionType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

@RunWith(JUnit4.class)
public final class FunctionNodeTest {
  @Test
  public void testConstructor() {
    final Database database = new Database(new MockDatabase());

    final TagManager nodeTagManager =
        new TagManager(new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final MockSqlProvider provider = new MockSqlProvider();

    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);

    final Module module = new Module(database, internalModule, nodeTagManager, viewTagManager);

    final MockView mockView = new MockView();

    final View view = new View(module, mockView, nodeTagManager, viewTagManager);

    final CFunction internalFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);
    final CFunctionNode internalFunctionNode =
        new CFunctionNode(0, internalFunction, 0, 0, 0, 0, Color.RED, false, false, null,
            new HashSet<CTag>(), provider);

    final CFunction parentFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final Function function = new Function(module, parentFunction);

    final FunctionNode node =
        new FunctionNode(view, internalFunctionNode, function, viewTagManager);

    assertEquals(function, node.getFunction());
    assertEquals("Function Node ['Mock Function']", node.toString());
  }

  @Test
  public void testSetComment() throws CouldntLoadDataException, CouldntLoadDataException,
  com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException, LoadCancelledException {
    final MockSqlProvider provider = new MockSqlProvider();

    final Database database = new Database(new MockDatabase());

    final CModule internalModule =
        new CModule(1, "", "", new Date(), new Date(), "00000000000000000000000000000000",
            "0000000000000000000000000000000000000000", 0, 0, new CAddress(0), new CAddress(0),
            null, null, Integer.MAX_VALUE, false, provider);
    internalModule.load();
    final CFunction internalFunction = new CFunction(
        internalModule, new MockView(), new CAddress(0x123), "Mock Function", "Mock Function",
        "Mock Description", 0, 0, 0, 0, FunctionType.NORMAL, "", 0, null, null, null, provider);

    final CView internalView = internalModule.getContent().getViewContainer().createView("", "");

    @SuppressWarnings("unused")
    final CFunctionNode internalFunctionNode =
    internalView.getContent().createFunctionNode(internalFunction);

    final TagManager nodeTagManager =
        new TagManager(new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.NODE_TAG));
    final TagManager viewTagManager =
        new TagManager(new MockTagManager(com.google.security.zynamics.binnavi.Tagging.TagType.VIEW_TAG));

    final Function function = new Function(ModuleFactory.get(), internalFunction);

    final Module module = new Module(database, internalModule, nodeTagManager, viewTagManager) {
      @Override
      public Function getFunction(final INaviFunction internalFunction) {
        return function;
      }

      @Override
      public boolean isLoaded() {
        return true;
      }
    };

    module.load();

    final View view = module.getViews().get(2);

    final FunctionNode node = (FunctionNode) view.getGraph().getNodes().get(0);

    final MockFunctionNodeListener listener = new MockFunctionNodeListener();

    node.addListener(listener);

    final ArrayList<IComment> comment =
        Lists
        .<IComment>newArrayList(new CComment(null, CommonTestObjects.TEST_USER_1, null, "Fark"));

    node.initializeComment(comment);

    assertEquals("initializedComment;", listener.events);
    assertEquals(comment, node.getComment());

    node.removeListener(listener);
  }
}
