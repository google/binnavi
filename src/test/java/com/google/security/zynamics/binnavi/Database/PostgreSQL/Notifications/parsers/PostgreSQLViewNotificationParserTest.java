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
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.MockClasses.MockSqlProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.ViewNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLViewNotificationParser;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.MockProject;
import com.google.security.zynamics.binnavi.disassembly.MockView;
import com.google.security.zynamics.binnavi.disassembly.Modules.MockModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.zylib.disassembly.ViewType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.postgresql.PGNotification;

import java.util.ArrayList;
import java.util.Collection;

@RunWith(JUnit4.class)
public class PostgreSQLViewNotificationParserTest {
  private final SQLProvider provider = new MockSqlProvider();
  private final INaviView view = MockView.getFullView(provider, ViewType.NonNative, null);
  private final Collection<PGNotification> notifications = new ArrayList<PGNotification>();

  private void testParser(final String table, final String databaseOperation, final String viewId,
      final String containerId) {

    final String NOTIFICATION =
        containerId != null ? table + " " + databaseOperation + " " + viewId + " " + containerId
            : table + " " + databaseOperation + " " + viewId;

    notifications.add(new MockPGNotification("view_changes", NOTIFICATION));

    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    final Collection<ViewNotificationContainer> result = parser.parse(notifications, provider);

    assertNotNull(result);
    assertTrue(!result.isEmpty());
    assertTrue(result.size() == 1);

    final ViewNotificationContainer container = Iterables.getFirst(result, null);
    assertNotNull(container);
    assertEquals(databaseOperation, container.getDatabaseOperation());
    assertEquals(viewId, container.getViewId().toString());
    if (containerId != null) {
      assertEquals(containerId, container.getNotificationObjectId().get().toString());
    }
  }

  @Test
  public void testModuleViewParsing1() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "INSERT",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test
  public void testModuleViewParsing2() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "UPDATE",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test
  public void testModuleViewParsing3() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "DELETE",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testModuleViewParsing4() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "XXXXXX",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testModuleViewParsing5() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "DELETE", "0xDEADBEEF", "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testModuleViewParsing6() {
    testParser(CTableNames.MODULE_VIEWS_TABLE, "DELETE",
        String.valueOf(view.getConfiguration().getId()), "XXXX");
  }

  @Test(expected = IllegalStateException.class)
  public void testModuleViewParsing7() {
    testParser("bn_module_view", "INSERT", String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test
  public void testProjectViewParsing1() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "INSERT",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test
  public void testProjectViewParsing2() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "UPDATE",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test
  public void testProjectViewParsing3() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "DELETE",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testProjectViewParsing4() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "XXXXXX",
        String.valueOf(view.getConfiguration().getId()), "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testProjectViewParsing5() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "DELETE", "0xDEADBEEF", "1");
  }

  @Test(expected = IllegalStateException.class)
  public void testProjectViewParsing6() {
    testParser(CTableNames.PROJECT_VIEWS_TABLE, "DELETE",
        String.valueOf(view.getConfiguration().getId()), "XXXX");
  }

  @Test(expected = IllegalStateException.class)
  public void testProjectViewParsing7() {
    testParser("bn_project_view", "INSERT", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test
  public void testViewParsing1() {
    testParser(
        CTableNames.VIEWS_TABLE, "INSERT", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test
  public void testViewParsing2() {
    testParser(
        CTableNames.VIEWS_TABLE, "UPDATE", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test
  public void testViewParsing3() {
    testParser(
        CTableNames.VIEWS_TABLE, "DELETE", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test(expected = IllegalStateException.class)
  public void testViewParsing4() {
    testParser(
        CTableNames.VIEWS_TABLE, "XXXXXX", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test(expected = IllegalStateException.class)
  public void testViewParsing5() {
    testParser(CTableNames.VIEWS_TABLE, "DELETE", "0xDEADBEEF", null);
  }


  @Test(expected = IllegalStateException.class)
  public void testViewParsing7() {
    testParser("bn_view", "INSERT", String.valueOf(view.getConfiguration().getId()), null);
  }

  @Test
  public void testViewInform0() throws CouldntLoadDataException {
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.<Integer>absent(),
            Optional.<INaviModule>absent(),
            Optional.<INaviProject>absent(),
            "INSERT");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test
  public void testViewInform1() throws CouldntLoadDataException {
    final String description2 = "TEST DESCRIPTION STATE CHANGE";
    view.getConfiguration().setDescriptionInternal(description2);
    assertEquals(description2, view.getConfiguration().getDescription());

    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.<Integer>absent(),
            Optional.<INaviModule>absent(),
            Optional.<INaviProject>absent(),
            "UPDATE");

    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);

    assertEquals("DB PROJECT VIEW DESCRIPTION", view.getConfiguration().getDescription());
  }

  @Test
  public void testViewInform2() throws CouldntLoadDataException {
    final String name2 = "TEST NAME STATE CHANGE";
    view.getConfiguration().setNameInternal(name2);
    assertEquals(name2, view.getConfiguration().getName());

    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.<Integer>absent(),
            Optional.<INaviModule>absent(),
            Optional.<INaviProject>absent(),
            "UPDATE");

    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);

    assertEquals("DB PROJECT VIEW NAME", view.getConfiguration().getName());
  }

  @Test
  public void testViewInform3() throws CouldntLoadDataException {
    final boolean starState2 = true;
    view.getConfiguration().setStaredInternal(starState2);
    assertEquals(starState2, view.getConfiguration().isStared());

    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.<Integer>absent(),
            Optional.<INaviModule>absent(),
            Optional.<INaviProject>absent(),
            "UPDATE");

    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);

    assertEquals(false, view.getConfiguration().isStared());
  }

  @Test
  public void testViewInform4() throws CouldntLoadDataException {
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.<Integer>absent(),
            Optional.<INaviModule>absent(),
            Optional.<INaviProject>absent(),
            "DELETE");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test
  public void testModuleViewInform0() throws CouldntLoadDataException {
    final INaviModule module = new MockModule(provider);
    final int currentUserViewSize = module.getContent().getViewContainer().getUserViews().size();
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(module.getConfiguration().getId()),
            Optional.of(module),
            Optional.<INaviProject>absent(),
            "INSERT");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
    assertEquals(
        currentUserViewSize + 1, module.getContent().getViewContainer().getUserViews().size());
  }

  @Test
  public void testModuleViewInform1() throws CouldntLoadDataException {
    final INaviModule module = new MockModule(provider);
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(module.getConfiguration().getId()),
            Optional.of(module),
            Optional.<INaviProject>absent(),
            "UPDATE");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testModuleViewInform2() throws CouldntLoadDataException {
    final INaviModule module = new MockModule(provider);
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(module.getConfiguration().getId()),
            Optional.of(module),
            Optional.<INaviProject>absent(),
            "DELETE");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test
  public void testModuleViewInform3() throws CouldntLoadDataException {
    final INaviModule module = new MockModule(provider);
    final int currentUserViewSize = module.getContent().getViewContainer().getUserViews().size();
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(module.getConfiguration().getId()),
            Optional.of(module),
            Optional.<INaviProject>absent(),
            "INSERT");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
    assertEquals(
        currentUserViewSize + 1, module.getContent().getViewContainer().getUserViews().size());

    final ViewNotificationContainer container2 =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(module.getConfiguration().getId()),
            Optional.of(module),
            Optional.<INaviProject>absent(),
            "DELETE");
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container2), provider);
    assertEquals(currentUserViewSize, module.getContent().getViewContainer().getUserViews().size());
  }

  @Test
  public void testProjectViewInform0() throws CouldntLoadDataException {
    final INaviProject project = new MockProject(provider);
    final int currentUserViewSize = project.getContent().getViews().size();
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(project.getConfiguration().getId()),
            Optional.<INaviModule>absent(),
            Optional.of(project),
            "INSERT");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
    assertEquals(currentUserViewSize + 1, project.getContent().getViews().size());
  }

  @Test
  public void testProjectViewInform1() throws CouldntLoadDataException {
    final INaviProject project = new MockProject(provider);
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(project.getConfiguration().getId()),
            Optional.<INaviModule>absent(),
            Optional.of(project),
            "UPDATE");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProjectViewInform2() throws CouldntLoadDataException {
    final INaviProject project = new MockProject(provider);
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(project.getConfiguration().getId()),
            Optional.<INaviModule>absent(),
            Optional.of(project),
            "DELETE");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
  }

  @Test
  public void testProjectViewInform3() throws CouldntLoadDataException {
    final INaviProject project = new MockProject(provider);
    final int currentUserViewSize = project.getContent().getViews().size();
    final ViewNotificationContainer container =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(project.getConfiguration().getId()),
            Optional.<INaviModule>absent(),
            Optional.of(project),
            "INSERT");
    final PostgreSQLViewNotificationParser parser = new PostgreSQLViewNotificationParser();
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container), provider);
    assertEquals(currentUserViewSize + 1, project.getContent().getViews().size());


    final ViewNotificationContainer container2 =
        new ViewNotificationContainer(view.getConfiguration().getId(),
            Optional.fromNullable(view),
            Optional.of(project.getConfiguration().getId()),
            Optional.<INaviModule>absent(),
            Optional.of(project),
            "DELETE");
    parser.inform(Lists.<ViewNotificationContainer>newArrayList(container2), provider);
    assertEquals(currentUserViewSize, project.getContent().getViews().size());
  }
}
