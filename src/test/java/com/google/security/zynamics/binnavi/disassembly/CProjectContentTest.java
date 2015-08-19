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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Tagging.CTag;
import com.google.security.zynamics.binnavi.Tagging.TagType;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.AddressSpaces.CAddressSpace;
import com.google.security.zynamics.binnavi.disassembly.views.CViewFilter;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.types.lists.FilledList;
import com.google.security.zynamics.zylib.types.lists.IFilledList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class CProjectContentTest {
  private final INaviProject m_project = new MockProject();
  private final ListenerProvider<IProjectListener> m_listeners =
      new ListenerProvider<IProjectListener>();
  private final IProjectListener m_listener = new MockProjectListener();
  private final SQLProvider m_provider = new MockSqlProvider();
  private final List<CAddressSpace> m_addressSpaces = new ArrayList<CAddressSpace>();
  private final List<INaviView> m_views = new ArrayList<INaviView>();
  private final IFilledList<TraceList> m_traces = new FilledList<TraceList>();

  @Before
  public void setUp() {
    m_listeners.addListener(m_listener);

  }

  @Test
  public void testClose()
      throws CouldntLoadDataException, LoadCancelledException, CouldntSaveDataException {

    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    assertNotNull(projectContent);

    final CAddressSpace spaceOne = projectContent.createAddressSpace("Address Space 1");
    spaceOne.load();
    final CAddressSpace spaceTwo = projectContent.createAddressSpace("Address Space 2");
    spaceTwo.load();
    final CAddressSpace spaceThree = projectContent.createAddressSpace("Address Space 3");
    spaceThree.load();
    final CAddressSpace spaceFour = projectContent.createAddressSpace("Address Space 4");
    spaceFour.load();

    @SuppressWarnings("unused")
    final INaviView viewOne =
        projectContent.createView(new MockView(m_provider), "View 1 Name", "View 1 description");

    projectContent.close();

  }

  @Test
  public void testCreateAddressSpace()
      throws CouldntSaveDataException, CouldntLoadDataException, LoadCancelledException {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    final CAddressSpace spaceOne = projectContent.createAddressSpace("Address Space 1");
    spaceOne.load();

    try {
      projectContent.createAddressSpace(null);
      fail();
    } catch (final NullPointerException e) {
    }

  }

  @Test
  public void testCreateTrace() throws CouldntSaveDataException {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    assertNotNull(projectContent);

    final TraceList trace = projectContent.createTrace("Trace Name", "Trace Description");
    assertNotNull(trace);

    try {
      projectContent.createTrace(null, null);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      projectContent.createTrace("", null);
      fail();
    } catch (final NullPointerException e) {
    }

  }

  @Test
  public void testCreateView() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    final INaviView view = new MockView(m_provider);

    final INaviView view2 = projectContent.createView(view, "Name", "Description");

    assertNotNull(view2);

    try {
      projectContent.createView(null, "Name", "Description");
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      projectContent.createView(view, null, "Description");
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      projectContent.createView(view, "Name", null);
      fail();
    } catch (final NullPointerException e) {
    }

    final INaviView wrongView = new MockView(new MockSqlProvider());
    try {
      projectContent.createView(wrongView, "Name", "test");
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testCreateView2() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(view2);

    try {
      projectContent.createView(null, "description");
      fail();
    } catch (final NullPointerException e) {
    }
  }

  @Test
  public void testDeleteView() throws CouldntDeleteException {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(view2);

    try {
      projectContent.deleteView(null);
      fail();
    } catch (final NullPointerException e) {
    }

    projectContent.deleteView(view2);

    final INaviView view3 = new MockView(new MockSqlProvider());

    try {
      projectContent.deleteView(view3);
      fail();
    } catch (final IllegalArgumentException e) {
    }
  }

  @Test
  public void testGetAddressSpaces() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    final List<INaviAddressSpace> addressSpaces = projectContent.getAddressSpaces();

    assertNotNull(addressSpaces);

    projectContent.close();
  }

  @Test
  public void testGetFlowgraphViews() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getFlowgraphViewCount(projectContent.getViews()));
  }

  @Test
  public void testGetMixedGraphViews() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getMixedgraphViewCount(projectContent.getViews()));
  }

  @Test
  public void testGetTaggedViews() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getTaggedViews(projectContent.getViews()));
  }

  @Test
  public void testGetTaggedViews2() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getTaggedViews(
        projectContent.getViews(), new CTag(4, "foo", "bar", TagType.VIEW_TAG, m_provider)));
  }

  @Test
  public void testGetTraceCount() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);
    assertEquals(0, projectContent.getTraceCount());
  }

  @Test
  public void testGetTraces() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);
    assertNotNull(projectContent.getTraces());
  }

  @Test
  public void testGetViews() {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    final List<INaviView> views = projectContent.getViews();

    assertEquals(1, views.size());
  }

  @Test
  public void testRemoveAddressSpace() throws CouldntSaveDataException, CouldntLoadDataException,
      LoadCancelledException, CouldntDeleteException {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getTaggedViews(
        projectContent.getViews(), new CTag(4, "foo", "bar", TagType.VIEW_TAG, m_provider)));

    final CAddressSpace spaceOne = projectContent.createAddressSpace("Address Space 1");
    spaceOne.load();
    final CAddressSpace spaceTwo = projectContent.createAddressSpace("Address Space 2");
    spaceTwo.load();
    final CAddressSpace spaceThree = projectContent.createAddressSpace("Address Space 3");
    spaceThree.load();
    final CAddressSpace spaceFour = projectContent.createAddressSpace("Address Space 4");
    spaceFour.load();

    m_project.load();
    try {
      assertFalse(projectContent.removeAddressSpace(spaceThree));
      fail();
    } catch (final IllegalStateException e) {
    }

    spaceThree.close();
    assertTrue(projectContent.removeAddressSpace(spaceThree));
    try {
      assertFalse(projectContent.removeAddressSpace(spaceThree));
      fail();
    } catch (final IllegalArgumentException e) {
    }

    try {
      assertFalse(projectContent.removeAddressSpace(null));
      fail();
    } catch (final NullPointerException e) {
    }

    m_project.close();
    try {
      assertFalse(projectContent.removeAddressSpace(spaceFour));
      fail();
    } catch (final IllegalStateException e) {
    }
  }

  @Test
  public void testRemoveTrace() throws CouldntLoadDataException, LoadCancelledException,
      CouldntSaveDataException, CouldntDeleteException {
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    @SuppressWarnings("unused")
    final INaviView view = new MockView(m_provider);

    @SuppressWarnings("unused")
    final INaviView view2 = projectContent.createView("Name", "description");

    assertNotNull(CViewFilter.getTaggedViews(
        projectContent.getViews(), new CTag(4, "foo", "bar", TagType.VIEW_TAG, m_provider)));

    final CAddressSpace spaceOne = projectContent.createAddressSpace("Address Space 1");
    spaceOne.load();
    final CAddressSpace spaceTwo = projectContent.createAddressSpace("Address Space 2");
    spaceTwo.load();
    final CAddressSpace spaceThree = projectContent.createAddressSpace("Address Space 3");
    spaceThree.load();
    final CAddressSpace spaceFour = projectContent.createAddressSpace("Address Space 4");
    spaceFour.load();

    m_project.load();

    final TraceList trace = new TraceList(3, "name", "desc", m_provider);
    projectContent.removeTrace(trace);

    try {
      projectContent.removeTrace(null);
      fail();
    } catch (final NullPointerException e) {
    }

    final TraceList trace2 = new TraceList(3, "name", "desc", new MockSqlProvider());
    try {
      projectContent.removeTrace(trace2);
      fail();
    } catch (final Exception e) {
    }
  }

  @Test
  public void testSimple1() {
    @SuppressWarnings("unused")
    final CProjectContent projectContent =
        new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);

    try {
      new CProjectContent(null, m_listeners, m_provider, m_addressSpaces, m_views, m_traces);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CProjectContent(m_project, null, m_provider, m_addressSpaces, m_views, m_traces);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CProjectContent(m_project, m_listeners, null, m_addressSpaces, m_views, m_traces);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CProjectContent(m_project, m_listeners, m_provider, null, m_views, m_traces);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, null, m_traces);
      fail();
    } catch (final NullPointerException e) {
    }

    try {
      new CProjectContent(m_project, m_listeners, m_provider, m_addressSpaces, m_views, null);
      fail();
    } catch (final NullPointerException e) {
    }
  }
}
