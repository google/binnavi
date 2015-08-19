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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.CCallgraph;
import com.google.security.zynamics.binnavi.disassembly.ICallgraphView;
import com.google.security.zynamics.binnavi.disassembly.IFlowgraphView;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainer;
import com.google.security.zynamics.binnavi.disassembly.types.SectionContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainer;
import com.google.security.zynamics.binnavi.disassembly.types.TypeInstanceContainerBackend;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.disassembly.ViewType;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test class for all tests related to the module content.
 */
@RunWith(JUnit4.class)
public class CPostgreSQLModuleContentTest extends ExpensiveBaseTest {
  @Test
  public void testAddView() throws CouldntLoadDataException, LoadCancelledException,
      CPartialLoadException, CouldntSaveDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent1 = module.getContent();

    assertNotNull(moduleContent1);
    final CView view = moduleContent1.getViewContainer().createView("name", "desc");
    final CView view2 = (CView) moduleContent1.getViewContainer().getViews().get(1);

    assertNotNull(view);
    assertNotNull(view2);

    view2.load();

    view2.getContent()
        .createFunctionNode(module.getContent().getFunctionContainer().getFunctions().get(0));

    view2.getConfiguration().setDescription("foobar");

    view2.getConfiguration().setName("barfoo");

    try {
      moduleContent1.getViewContainer().createView(null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      moduleContent1.getViewContainer().createView("name", null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      moduleContent1.close();
      moduleContent1.getViewContainer().createView("name", "desc");
      fail();
    } catch (final IllegalStateException e) {
    }

  }

  @Test
  public void testCModuleContentConstructor()
      throws LoadCancelledException, CouldntLoadDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();
    final ListenerProvider<IModuleListener> listeners = new ListenerProvider<IModuleListener>();
    final CCallgraph callgraph = module.getContent().getNativeCallgraph();
    final IFilledList<INaviFunction> functions = new FilledList<INaviFunction>();
    functions.add(module.getContent().getFunctionContainer().getFunctions().get(0));
    final ICallgraphView nativeCallgraph =
        module.getContent().getViewContainer().getNativeCallgraphView();
    final ImmutableList<IFlowgraphView> nativeFlowgraphs =
        module.getContent().getViewContainer().getNativeFlowgraphViews();
    final List<INaviView> customViews = new ArrayList<INaviView>();
    final ImmutableBiMap<INaviView, INaviFunction> viewFunctionMap =
        new ImmutableBiMap.Builder<INaviView, INaviFunction>().build();
    new Pair<HashMap<INaviView, INaviFunction>, HashMap<INaviFunction, INaviView>>(null, null);
    final IFilledList<TraceList> traces = new FilledList<TraceList>();

    final SectionContainer sections =
        new SectionContainer(new SectionContainerBackend(getProvider(), module));
    final TypeInstanceContainer instances = new TypeInstanceContainer(
        new TypeInstanceContainerBackend(getProvider(), module, module.getTypeManager(), sections),
        getProvider());
    final CModuleContent moduleContent1 = new CModuleContent(
        module, getProvider(), listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
        customViews, viewFunctionMap, traces, sections, instances);

    assertNotNull(moduleContent1);

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          null, null, null, null, null, null, null, null, null, null, sections, instances);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, null, null, null, null, null, null, null, null, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), null, null, null, null, null, null, null, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, null, null, null, null, null, null, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, null, null, null, null, null, null, null,
          null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, null, null, null, null, null,
          null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, nativeCallgraph, null, null, null,
          null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
          null, null, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
          customViews, null, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
          customViews, viewFunctionMap, null, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      @SuppressWarnings("unused")
      final CModuleContent moduleContent = new CModuleContent(
          module, getProvider(), listeners, callgraph, functions, nativeCallgraph, nativeFlowgraphs,
          customViews, viewFunctionMap, traces, null, null);
      fail();
    } catch (final NullPointerException e) {
    }

  }

  @Test
  public void testCreateTrace()
      throws LoadCancelledException, CouldntLoadDataException, CouldntSaveDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();
    final CModuleContent moduleContent1 = module.getContent();
    assertNotNull(moduleContent1);
    final TraceList trace = moduleContent1.getTraceContainer().createTrace("name", "desc");
    assertNotNull(trace);

    try {
      moduleContent1.getTraceContainer().createTrace(null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      moduleContent1.getTraceContainer().createTrace("name", null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      moduleContent1.close();
      moduleContent1.getTraceContainer().createTrace("name2", "desc2");
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testDeleteTrace() throws LoadCancelledException, CouldntLoadDataException,
      CouldntSaveDataException, CouldntDeleteException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent1 = module.getContent();

    assertNotNull(moduleContent1);
    final CView view = moduleContent1.getViewContainer().createView("name", "desc");
    assertNotNull(view);

    final TraceList trace = moduleContent1.getTraceContainer().createTrace("name2", "desc2");
    assertNotNull(trace);

    moduleContent1.getTraceContainer().deleteTrace(trace);

    try {
      moduleContent1.getTraceContainer().deleteTrace(null);
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testDeleteView()
      throws LoadCancelledException, CouldntLoadDataException, CouldntDeleteException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent1 = module.getContent();

    assertNotNull(moduleContent1);
    final CView view = moduleContent1.getViewContainer().createView("name", "desc");
    assertNotNull(view);

    try {
      moduleContent1.getViewContainer().deleteView(null);
      fail();
    } catch (final NullPointerException e) {
    }

    assertEquals(1, moduleContent1.getViewContainer().getCustomViewCount());
    moduleContent1.getViewContainer().deleteView(view);
    assertEquals(0, moduleContent1.getViewContainer().getCustomViewCount());

    try {
      moduleContent1.getViewContainer().deleteView(view);
      fail();
    } catch (final IllegalArgumentException e) {
    }

    try {
      moduleContent1.getViewContainer().deleteView(null);
      fail();
    } catch (final NullPointerException e) {
    }

    CView nativeView = null;
    for (final INaviView view1 : module.getContent().getViewContainer().getViews()) {
      if (view1.getType() == ViewType.Native) {
        nativeView = (CView) view1;
      }
    }

    try {
      moduleContent1.getViewContainer().deleteView(nativeView);
      fail();
    } catch (final IllegalStateException e) {
    }

    try {
      moduleContent1.close();
      moduleContent1.getViewContainer().deleteView(view);
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetFunction1() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent = module.getContent();

    try {
      moduleContent.getFunctionContainer().getFunction((IAddress) null);
      fail();
    } catch (final NullPointerException e) {
    }

    moduleContent.getFunctionContainer().getFunction(new CAddress(0x12345));

    module.close();

    try {
      moduleContent.getFunctionContainer().getFunction(new CAddress(0x12345));
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetFunction2() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent = module.getContent();

    try {
      moduleContent.getViewContainer().getFunction((INaviView) null);
      fail();
    } catch (final NullPointerException e) {
    }

    moduleContent.getViewContainer()
        .getFunction(module.getContent().getViewContainer().getViews().get(0));

    module.close();

    try {
      moduleContent.getViewContainer()
          .getFunction(module.getContent().getViewContainer().getViews().get(0));
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetFunction3()
      throws LoadCancelledException, CouldntLoadDataException, MaybeNullException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent = module.getContent();

    try {
      moduleContent.getFunctionContainer().getFunction((String) null);
      fail();
    } catch (final NullPointerException e) {
    }

    moduleContent.getFunctionContainer()
        .getFunction(module.getContent().getFunctionContainer().getFunctions().get(0).getName());

    module.close();

    try {
      moduleContent.getFunctionContainer()
          .getFunction(module.getContent().getFunctionContainer().getFunctions().get(0).getName());
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetFunctions() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module = (CModule) getDatabase().getContent().getModules().get(0);
    module.load();

    final CModuleContent moduleContent = module.getContent();

    moduleContent.getFunctionContainer().getFunctionCount();
    assertNotNull(moduleContent.getFunctionContainer().getFunctions());

    module.close();

    try {
      moduleContent.getFunctionContainer().getFunctions();
      fail();
    } catch (final IllegalStateException e) {
    }

    final CModule module1 = (CModule) getDatabase().getContent().getModules().get(0);
    module1.load();

    @SuppressWarnings("unused")
    final CModuleContent moduleContent1 = module.getContent();

  }

  @Test
  public void testGetNativeCallGraph() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module2 = (CModule) getDatabase().getContent().getModules().get(0);
    module2.load();

    final CModuleContent moduleContent2 = module2.getContent();

    assertNotNull(moduleContent2.getNativeCallgraph());

    module2.close();

    try {
      moduleContent2.getViewContainer().getNativeCallgraphView();
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetNativeCallgraphView() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module3 = (CModule) getDatabase().getContent().getModules().get(0);
    module3.load();

    final CModuleContent moduleContent3 = module3.getContent();

    assertNotNull(moduleContent3.getViewContainer().getNativeCallgraphView());

    module3.close();

    try {
      moduleContent3.getViewContainer().getNativeFlowgraphViews();
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetNativeFlowgraphViews()
      throws LoadCancelledException, CouldntLoadDataException {
    final CModule module4 = (CModule) getDatabase().getContent().getModules().get(0);
    module4.load();

    final CModuleContent moduleContent4 = module4.getContent();

    assertNotNull(moduleContent4.getViewContainer().getNativeFlowgraphViews().get(0));

    module4.close();

    try {
      CViewFilter.getTaggedViews(moduleContent4.getViewContainer().getViews());
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetTaggedViews1() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module5 = (CModule) getDatabase().getContent().getModules().get(0);
    module5.load();

    final CModuleContent moduleContent5 = module5.getContent();

    CViewFilter.getTaggedViews(moduleContent5.getViewContainer().getViews());

    try {
      CViewFilter.getTaggedViews(moduleContent5.getViewContainer().getViews(), null);
    } catch (final IllegalArgumentException e) {
    }

    try {
      CViewFilter.getTaggedViews(moduleContent5.getViewContainer().getViews(),
          new CTag(0, "foo", "bar", TagType.NODE_TAG, new MockSqlProvider()));
    } catch (final IllegalArgumentException e) {
    }

    module5.close();
  }

  @Test
  public void testGetTraceCount() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    moduleContent6.getTraceContainer().getTraceCount();

    module6.close();

    try {
      moduleContent6.getTraceContainer().getTraceCount();
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetTraces() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(moduleContent6.getTraceContainer().getTraces());

    module6.close();

    try {
      moduleContent6.getTraceContainer().getTraces();
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetUserCallGrapViews() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(CViewFilter.getCallgraphViews(moduleContent6.getViewContainer().getUserViews()));

    module6.close();

    try {
      CViewFilter.getCallgraphViews(moduleContent6.getViewContainer().getUserViews());
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetUserFlowGrapViews() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(
        CViewFilter.getFlowgraphViewCount(moduleContent6.getViewContainer().getUserViews()));

    module6.close();

    try {
      CViewFilter.getFlowgraphViewCount(moduleContent6.getViewContainer().getUserViews());
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetUserMixedgraphViews() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(
        CViewFilter.getMixedgraphViewCount(moduleContent6.getViewContainer().getUserViews()));

    module6.close();

    try {
      CViewFilter.getMixedgraphViewCount(moduleContent6.getViewContainer().getUserViews());
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetUserViews() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(moduleContent6.getViewContainer().getUserViews());

    module6.close();

    try {
      moduleContent6.getViewContainer().getUserViews();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetView() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    try {
      module6.load();
    } catch (final CouldntLoadDataException e1) {
      try {
        module6.initialize();
      } catch (final CouldntSaveDataException e) {
      }
    } finally {
      module6.load();
    }

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(moduleContent6.getViewContainer()
        .getView(module6.getContent().getFunctionContainer().getFunctions().get(0)));

    try {
      moduleContent6.getViewContainer().getView((INaviFunction) null);
      fail();
    } catch (final NullPointerException e) {
    }

    module6.close();

    try {
      moduleContent6.getViewContainer()
          .getView(module6.getContent().getFunctionContainer().getFunctions().get(0));
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testGetViews() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertNotNull(moduleContent6.getViewContainer().getViews());

    module6.close();

    try {
      moduleContent6.getViewContainer().getViews();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testHasView() throws LoadCancelledException, CouldntLoadDataException {
    final CModule module6 = (CModule) getDatabase().getContent().getModules().get(0);
    module6.load();

    final CModuleContent moduleContent6 = module6.getContent();

    assertTrue(moduleContent6.getViewContainer()
        .hasView(module6.getContent().getViewContainer().getViews().get(0)));

    try {
      moduleContent6.getViewContainer().hasView(null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      moduleContent6.getViewContainer().hasView(new MockView());
      fail();
    } catch (final IllegalArgumentException e) {
    }

    module6.close();

    try {
      moduleContent6.getViewContainer().getViews();
      fail();
    } catch (final IllegalStateException e) {
    }
  }
}
