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
package com.google.security.zynamics.binnavi.Database.PostgreSQL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.security.zynamics.binnavi.Database.Exceptions.CPartialLoadException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.CModule;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.views.CView;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Test class which tests querries that create elements in the database.
 */
@RunWith(JUnit4.class)
public class PostgreSQLProviderTestSetup extends ExpensiveBaseTest {
  @Test(expected = NullPointerException.class)
  public void testCreateAddressSpace1() throws CouldntSaveDataException {
    getProvider().createAddressSpace(null, "Test Address Space");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateAddressSpace2() throws CouldntSaveDataException {
    getProvider().createAddressSpace(loadProject(), null);
  }

  @Test
  public void testCreateAddressSpace() throws CouldntSaveDataException {
    final CAddressSpace addressSpace =
        getProvider().createAddressSpace(loadProject(), "Test Address Space");
    assertNotNull(addressSpace);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateDebuggerTemplate1() throws CouldntSaveDataException {
    getProvider().createDebuggerTemplate(null, "localhost", 2222);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateDebuggerTemplate2() throws CouldntSaveDataException {
    getProvider().createDebuggerTemplate("Test Debugger", null, 2222);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDebuggerTemplate3() throws CouldntSaveDataException {
    getProvider().createDebuggerTemplate("Test Debugger", "localhost", -1);
  }

  @Test
  public void testCreateDebuggerTemplate4() throws CouldntSaveDataException {
    getProvider().createDebuggerTemplate("Test Debugger", "localhost", 2222);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTag1() throws CouldntSaveDataException {
    getProvider().createTag(null, "Tag Name", "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTag2() throws CouldntSaveDataException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider()
        .createTag(tagManager.getRootTag().getObject(), null, "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTag3() throws CouldntSaveDataException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider()
        .createTag(tagManager.getRootTag().getObject(), "Tag Name", null, TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTag4() throws CouldntSaveDataException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider()
        .createTag(tagManager.getRootTag().getObject(), "Tag Name", "Tag Description", null);
  }

  @Test
  public void testCreateTag() throws CouldntSaveDataException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);

    final CTag newTag = getProvider().createTag(
        tagManager.getRootTag().getObject(), "Tag Name", "Tag Description", TagType.VIEW_TAG);

    assertEquals("Tag Name", newTag.getName());
    assertEquals("Tag Description", newTag.getDescription());
    assertEquals(TagType.VIEW_TAG, newTag.getType());

    getProvider().createTag(newTag, "Tag Name", "Tag Description", TagType.VIEW_TAG);

    // Create more tags for the delete test later
    getProvider().createTag(
        tagManager.getRootTag().getObject(), "Tag Name", "Tag Description", TagType.VIEW_TAG);
    final CTag tag4 = getProvider().createTag(
        tagManager.getRootTag().getObject(), "Tag Name", "Tag Description", TagType.VIEW_TAG);
    getProvider().createTag(tag4, "Tag Name", "Tag Description", TagType.VIEW_TAG);
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceModule1() throws CouldntSaveDataException {
    getProvider().createTrace((CModule) null, "Trace Name", "Trace Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceModule2() throws CouldntSaveDataException {
    getProvider().createTrace(new MockModule(), null, "Trace Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceModule3() throws CouldntSaveDataException {
    getProvider().createTrace(new MockModule(), "Trace Name", null);
  }

  @Test
  public void testCreateTraceModule4() throws CouldntSaveDataException, CouldntLoadDataException {
    final INaviModule module = getProvider().loadModules().get(0);
    final TraceList trace = getProvider().createTrace(module, "Trace Name", "Trace Description");
    assertEquals("Trace Name", trace.getName());
    assertEquals("Trace Description", trace.getDescription());
    assertEquals(0, trace.getEventCount());
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceProject1() throws CouldntSaveDataException {
    getProvider().createTrace((CProject) null, "Trace Name", "Trace Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceProject2() throws CouldntSaveDataException {
    getProvider().createTrace(new MockProject(), null, "Trace Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateTraceProject3() throws CouldntSaveDataException {
    getProvider().createTrace(new MockProject(), "Trace Name", null);
  }

  @Test
  public void testCreateTraceProject4() throws CouldntSaveDataException {
    final INaviProject project = loadProject();
    final TraceList trace = getProvider().createTrace(project, "Trace Name", "Trace Description");
    assertEquals("Trace Name", trace.getName());
    assertEquals("Trace Description", trace.getDescription());
    assertEquals(0, trace.getEventCount());
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewModule1() throws CouldntSaveDataException {
    getProvider().createView((CModule) null, new MockView(), "View Name", "View Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewModule2() throws CouldntSaveDataException {
    getProvider().createView(new MockModule(), null, "View Name", "View Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewModule3() throws CouldntSaveDataException {
    getProvider().createView(new MockModule(), new MockView(), null, "View Description");
  }

  @Test
  public void testCreateViewModule4() throws CouldntLoadDataException, CouldntSaveDataException,
      CPartialLoadException, LoadCancelledException {
    final INaviModule module = getProvider().loadModules().get(0);
    module.load();
    final INaviView view = module.getContent().getViewContainer().getViews().get(223);
    view.load();
    final CView newView =
        getProvider().createView(module, view, "Module View Name", "Module View Description");
    assertEquals("Module View Name", newView.getName());
    assertEquals("Module View Description", newView.getConfiguration().getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewProject1() throws CouldntSaveDataException {
    getProvider().createView((CProject) null, new MockView(), "View Name", "View Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewProject2() throws CouldntSaveDataException {
    getProvider().createView(new MockProject(), null, "View Name", "View Description");
  }

  @Test(expected = NullPointerException.class)
  public void testCreateViewProject3() throws CouldntSaveDataException {
    getProvider().createView(new MockProject(), new MockView(), null, "View Description");
  }

  @Test(expected = CouldntSaveDataException.class)
  public void testCreateViewProject4() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException, CPartialLoadException {
    final INaviView view = getKernel32Module().getContent().getViewContainer().getViews().get(223);
    view.load();
    getProvider().createView(new MockProject(), view, "View Name", null);
  }

  @Test
  public void testCreateViewProject5() throws CouldntLoadDataException, CouldntSaveDataException,
      CPartialLoadException, LoadCancelledException {
    final INaviModule module = getKernel32Module();
    module.load();
    final INaviView view = module.getContent().getViewContainer().getViews().get(200);
    view.load();
    final CView newView =
        getProvider().createView(loadProject(), view, "View Name", "View Description");
    assertEquals("View Name", newView.getName());
    assertEquals("View Description", newView.getConfiguration().getDescription());
  }
}
