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

import com.google.common.collect.Iterables;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Tagging.CTagManager;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate;
import com.google.security.zynamics.binnavi.disassembly.CProject;
import com.google.security.zynamics.binnavi.disassembly.types.ExpensiveBaseTest;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PostgreSQLProviderTestTeardown extends ExpensiveBaseTest {
  @Test(expected = NullPointerException.class)
  public void testDeleteAddressSpace1() throws CouldntDeleteException {
    getProvider().deleteAddressSpace(null);
  }

  @Test
  public void testDeleteAddressSpace2()
      throws CouldntDeleteException, CouldntLoadDataException, CouldntSaveDataException {
    final CProject project = getProvider().createProject("DELETE_ADDRESS_SPACE_PROJECT");
    getProvider().createAddressSpace(project, "DELETE_ADDRESS_SPACE_ADDRESS_SPACE");
    getProvider().deleteAddressSpace(getProvider().loadAddressSpaces(project).get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteDebugger1() throws CouldntDeleteException {
    getProvider().deleteDebugger(null);
  }
  
  @Test
  public void testDeleteDebugger2() throws CouldntDeleteException, CouldntLoadDataException {
    final DebuggerTemplate debuggerTemplate = getProvider().loadDebuggers().getDebugger(0);
    getProvider().deleteDebugger(debuggerTemplate);
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteProject1() throws CouldntDeleteException {
    getProvider().deleteProject(null);
  }
  
  @Test
  public void testDeleteProject2() throws CouldntDeleteException, CouldntLoadDataException {
    getProvider().deleteProject(getProvider().loadProjects().get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteTag1() throws CouldntDeleteException {
    getProvider().deleteTag(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testDeleteTag2() throws CouldntDeleteException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().deleteTag(tagManager.getRootTag());
  }
  
  @Test
  public void testDeleteTag3() throws CouldntDeleteException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().deleteTag(tagManager.getRootTag().getChildren().get(0));
    getProvider().deleteTag(tagManager.getRootTag().getChildren().get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteTagSubtree1() throws CouldntDeleteException {
    getProvider().deleteTagSubtree(null);
  }
  
  @Test(expected = NullPointerException.class)
  public void testDeleteTagSubtree2() throws CouldntDeleteException, CouldntLoadDataException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().deleteTagSubtree(tagManager.getRootTag());
  }
  
  @Test
  public void testDeleteTagSubtree() throws CouldntLoadDataException, CouldntDeleteException {
    final CTagManager tagManager = getProvider().loadTagManager(TagType.VIEW_TAG);
    getProvider().deleteTagSubtree(tagManager.getRootTag().getChildren().get(0));
    getProvider().deleteTagSubtree(tagManager.getRootTag().getChildren().get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteTrace1() throws CouldntDeleteException {
    getProvider().deleteTrace(null);
  }
  
  @Test
  public void testDeleteTrace2()
      throws CouldntDeleteException, CouldntLoadDataException, LoadCancelledException {
    getProvider()
        .deleteTrace(getNotepadModule().getContent().getTraceContainer().getTraces().get(0));
  }

  @Test(expected = NullPointerException.class)
  public void testDeleteView1() throws CouldntDeleteException {
    getProvider().deleteView(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDeleteView2()
      throws CouldntDeleteException, CouldntLoadDataException, LoadCancelledException {
    getProvider().deleteView(
        getNotepadModule().getContent().getViewContainer().getNativeFlowgraphViews().get(0));
  }

  @Test
  public void testDeleteView()
      throws CouldntLoadDataException, CouldntDeleteException, LoadCancelledException {
    getProvider().deleteView(Iterables.getFirst(CViewFilter.getFlowgraphViews(
        getNotepadModule().getContent().getViewContainer().getUserViews()), null));
  }
}
