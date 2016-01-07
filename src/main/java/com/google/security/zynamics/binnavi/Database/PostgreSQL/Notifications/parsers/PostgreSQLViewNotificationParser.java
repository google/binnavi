/*
Copyright 2011-2016 Google Inc. All Rights Reserved.

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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.Database.CModuleViewGenerator;
import com.google.security.zynamics.binnavi.Database.CProjectViewGenerator;
import com.google.security.zynamics.binnavi.Database.CTableNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.ViewNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationParser;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.INaviProject;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.ImmutableNaviViewConfiguration;
import com.google.security.zynamics.binnavi.disassembly.views.ViewManager;
import com.google.security.zynamics.zylib.disassembly.GraphType;

import org.postgresql.PGNotification;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the parser which is invoked for database notifications related to views. It parses
 * the incoming message and informs the necessary classes about the changes.
 */
public class PostgreSQLViewNotificationParser
    implements PostgreSQLNotificationParser<ViewNotificationContainer> {

  /**
   * The regular expression pattern for a bn_views notification.
   */
  private static final String VIEW_NOTIFICATION_REGULAR_EXPRESSION =
      "^(" + CTableNames.VIEWS_TABLE + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)$";

  /**
   * The compiled pattern for a bn_views notification.
   */
  private static final Pattern viewNotificationPattern;

  /**
   * The regular expression pattern for a bn_module_views notification.
   */
  private static final String MODULE_VIEW_NOTIFICATION_REGULAR_EXPRESSION = "^("
      + CTableNames.MODULE_VIEWS_TABLE + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)"
      + "\\s(\\d*)$";

  /**
   * The compiled pattern for a bn_module_views notification.
   */
  private static Pattern moduleViewNotificationPattern;

  /**
   * The regular expression pattern for a bn_module_views notification.
   */
  private static final String PROJECT_VIEW_NOTIFICATION_REGULAR_EXPRESSION = "^("
      + CTableNames.PROJECT_VIEWS_TABLE + ")" + "\\s(INSERT|UPDATE|DELETE)" + "\\s(\\d*)"
      + "\\s(\\d*)$";

  /**
   * The compiled pattern for a bn_project_views notification.
   */
  private static Pattern projectViewNotificationPattern;


  /**
   * Static initializer to only compile the used patterns in the class once.
   */
  static {
    viewNotificationPattern = Pattern.compile(VIEW_NOTIFICATION_REGULAR_EXPRESSION);
    moduleViewNotificationPattern = Pattern.compile(MODULE_VIEW_NOTIFICATION_REGULAR_EXPRESSION);
    projectViewNotificationPattern = Pattern.compile(PROJECT_VIEW_NOTIFICATION_REGULAR_EXPRESSION);
  }

  /**
   * Parser for a bn_views notification. The function uses the viewNotificationPattern to parse the
   * incoming {@link PGNotification} into a {@link ViewNotificationContainer}.
   *
   * @param notification The {@link PGNotification} to parse.
   * @param provider The {@link SQLProvider} to access the database.
   *
   * @return A {@link ViewNotificationContainer} with the parsed information.
   */
  private ViewNotificationContainer parseViewNotification(
      final PGNotification notification, final SQLProvider provider) {

    final Matcher matcher = viewNotificationPattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("IE02742: compiled pattern: "
          + viewNotificationPattern.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final Integer viewId = Integer.parseInt(matcher.group(3));
    final Optional<INaviView> view =
        Optional.fromNullable(ViewManager.get(provider).getView(viewId));
    final String databaseOperation = matcher.group(2);

    return new ViewNotificationContainer(viewId,
        view,
        Optional.<Integer>absent(),
        Optional.<INaviModule>absent(),
        Optional.<INaviProject>absent(),
        databaseOperation);
  }

  /**
   * Parser for a bn_module_views notification. The function uses the moduleViewNotificationPattern
   * to parse the incoming {@link PGNotification} into a {@link ViewNotificationContainer}.
   *
   * @param notification The {@link PGNotification} to parse.
   * @param provider The {@link SQLProvider} to access the database.
   *
   * @return A {@link ViewNotificationContainer} with the parsed information.
   */
  private ViewNotificationContainer parseModuleViewNotification(
      final PGNotification notification, final SQLProvider provider) {

    final Matcher matcher = moduleViewNotificationPattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("IE02743: compiled pattern: "
          + moduleViewNotificationPattern.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final Integer viewId = Integer.parseInt(matcher.group(3));
    final Optional<INaviView> view =
        Optional.fromNullable(ViewManager.get(provider).getView(viewId));
    final Optional<Integer> moduleId = Optional.fromNullable(Integer.parseInt(matcher.group(4)));
    final Optional<INaviModule> module = Optional.fromNullable(provider.findModule(moduleId.get()));
    final String databaseOperation = matcher.group(2);

    return new ViewNotificationContainer(viewId,
        view,
        moduleId,
        module,
        Optional.<INaviProject>absent(),
        databaseOperation);
  }

  /**
   * Parser for the bn_project_views notification. The function uses the
   * projectViewNotificationPattern to parse the incoming {@link PGNotification} into a
   * {@link ViewNotificationContainer}
   *
   * @param notification The {@link PGNotification} to parse.
   * @param provider The {@link SQLProvider} to access the database.
   */
  private ViewNotificationContainer parseProjectViewNotification(
      final PGNotification notification, final SQLProvider provider) {

    final Matcher matcher = projectViewNotificationPattern.matcher(notification.getParameter());
    if (!matcher.find()) {
      throw new IllegalStateException("IE02744: compiled pattern: "
          + projectViewNotificationPattern.toString() + " did not match notification: "
          + notification.getParameter());
    }

    final Integer viewId = Integer.parseInt(matcher.group(3));
    final Optional<INaviView> view =
        Optional.fromNullable(ViewManager.get(provider).getView(viewId));
    final Optional<Integer> projectId = Optional.fromNullable(Integer.parseInt(matcher.group(4)));
    final Optional<INaviProject> project =
        Optional.fromNullable(provider.findProject(projectId.get()));
    final String databaseOperation = matcher.group(2);

    return new ViewNotificationContainer(viewId,
        view,
        projectId,
        Optional.<INaviModule>absent(),
        project,
        databaseOperation);
  }

  /**
   * The parser function for view notifications. The function checks for the database table the
   * notification originated from and dispatches the parsing appropriately to the right function.
   *
   * @param notifications The {@link Collection} of {@link PGNotification} to parse.
   * @param provider The {@link SQLProvider} to access the database with.
   * @return A {@link Collection} of {@link ViewNotificationContainer}.
   */
  @Override
  public Collection<ViewNotificationContainer> parse(
      final Collection<PGNotification> notifications, final SQLProvider provider) {
    Preconditions.checkNotNull(notifications, "IE02745: notifications argument can not be null");
    Preconditions.checkNotNull(provider, "IE02746: provider argument can not be null");

    final Collection<ViewNotificationContainer> containers = Lists.newArrayList();

    for (final PGNotification notification : notifications) {
      if (notification.getParameter().startsWith(CTableNames.VIEWS_TABLE)) {
        containers.add(parseViewNotification(notification, provider));
      } else if (notification.getParameter().startsWith(CTableNames.MODULE_VIEWS_TABLE)) {
        containers.add(parseModuleViewNotification(notification, provider));
      } else if (notification.getParameter().startsWith(CTableNames.PROJECT_VIEWS_TABLE)) {
        containers.add(parseProjectViewNotification(notification, provider));
      } else {
        throw new IllegalStateException("IE02747: Table name supplied in notification: "
            + notification.getParameter()
            + " does not match tables where view notifications are accepted on.");
      }
    }
    return containers;
  }

  /**
   * The inform function for parsed view notifications. The function takes the parsed
   * {@link ViewNotificationContainer} and dispatches to the appropriate inform functions.
   *
   * @param parsedViewNotifications The {@link Collection} of {@link ViewNotificationContainer}.
   * @param provider The {@link SQLProvider} to access the database.
   * @throws CouldntLoadDataException if the Information could not be stored to the database.
   */
  @Override
  public void inform(final Collection<ViewNotificationContainer> parsedViewNotifications,
      final SQLProvider provider) throws CouldntLoadDataException {

    Preconditions.checkNotNull(
        parsedViewNotifications, "Error: parsedViewNotifications argument can not be null");
    Preconditions.checkNotNull(provider, "IE02748: provider argument can not be null");

    for (final ViewNotificationContainer container : parsedViewNotifications) {
      if (container.getNotificationModule().isPresent()) {
        informModuleNotification(container, provider);
      } else if (container.getNotificationProject().isPresent()) {
        informProjectNotification(container, provider);
      } else if (!container.getNotificationObjectId().isPresent()) { // view case
        informViewNotification(container, provider);
      } else if (!container.getNotificationObjectId().isPresent()
          && !container.getNotificationModule().isPresent()
          && !container.getNotificationProject().isPresent()) {
        // in this case we have no idea this is not good. But we know we need to resync the complete
        // database as we do not know about either the module or the project.
        // TODO(timkornau): think about how to not loose the information which is encoded in the
        // table name
        // and what else has to be synced that this situation can not really happen.
        throw new IllegalStateException("IE02749: Not enough information available.");
      }
    }
  }

  /**
   * The inform function for notifications from the bn_module_views table. This function is used
   * when a module view is created or deleted.
   *
   * @param moduleViewNotificationContainer The {@link ViewNotificationContainer} to use as data
   *        source.
   * @param provider The {@link SQLProvider} to access the database.
   * @throws CouldntLoadDataException if the information could not be loaded from the database.
   */
  private void informModuleNotification(
      final ViewNotificationContainer moduleViewNotificationContainer, final SQLProvider provider)
      throws CouldntLoadDataException {

    if (moduleViewNotificationContainer.getDatabaseOperation().equals("INSERT")) {
      final INaviModule module = moduleViewNotificationContainer.getNotificationModule().get();
      if (!module.isLoaded()) {
        return;
      }
      final Integer viewId = moduleViewNotificationContainer.getViewId();
      final ImmutableNaviViewConfiguration databaseViewConfiguration =
          provider.loadFlowGraphInformation(module, viewId);
      final CModuleViewGenerator generator = new CModuleViewGenerator(provider, module);
      final INaviView view = generator.generate(databaseViewConfiguration, GraphType.FLOWGRAPH);
      module.getContent().getViewContainer().addView(view);
    }
    if (moduleViewNotificationContainer.getDatabaseOperation().equals("UPDATE")) {
      return; // updates will not happen as this is only a mapping of id to id.
    }
    if (moduleViewNotificationContainer.getDatabaseOperation().equals("DELETE")) {
      final INaviModule module = moduleViewNotificationContainer.getNotificationModule().get();
      if (!module.isLoaded()) {
        return;
      }
      final Integer viewId = moduleViewNotificationContainer.getViewId();
      final INaviView view = ViewManager.get(provider).getView(viewId);
      module.getContent().getViewContainer().deleteViewInternal(view);
    }
  }

  /**
   * The inform function for notifications from the bn_project_views table. This function is used
   * when a project view is created or deleted.
   *
   * @param projectNotificationContainer The {@link ViewNotificationContainer} to use as data
   *        source.
   * @param provider The {@link SQLProvider} to access the database.
   * @throws CouldntLoadDataException if the information could not be loaded from the database.
   */
  private void informProjectNotification(
      final ViewNotificationContainer projectNotificationContainer, final SQLProvider provider)
      throws CouldntLoadDataException {
    if (projectNotificationContainer.getDatabaseOperation().equals("INSERT")) {
      final INaviProject project = projectNotificationContainer.getNotificationProject().get();
      if (!project.isLoaded()) {
        return;
      }
      final Integer viewId = projectNotificationContainer.getViewId();
      final ImmutableNaviViewConfiguration databaseViewConfiguration =
          provider.loadFlowGraphInformation(project, viewId);
      final CProjectViewGenerator generator = new CProjectViewGenerator(provider, project);
      final INaviView view = generator.generate(databaseViewConfiguration);
      project.getContent().addView(view);
    }
    if (projectNotificationContainer.getDatabaseOperation().equals("UPDATE")) {
      return; // updates will not happen as this is only a mapping of id to id.
    }
    if (projectNotificationContainer.getDatabaseOperation().equals("DELETE")) {
      final INaviProject project = projectNotificationContainer.getNotificationProject().get();
      if (!project.isLoaded()) {
        return;
      }
      final Integer viewId = projectNotificationContainer.getViewId();
      final INaviView view = ViewManager.get(provider).getView(viewId);
      project.getContent().deleteViewInternal(view);
    }
  }

  /**
   * The inform function for notifications from the bn_views table. This function is used when a
   * view gets edited. But this function will not perform any action when a view is deleted or
   * created.
   *
   * @param viewNotificationContainer The {@link ViewNotificationContainer} to use as data source.
   * @param provider The {@link SQLProvider} to access the database with.
   * @throws CouldntLoadDataException if the information could not be loaded from the database.
   */
  private void informViewNotification(
      final ViewNotificationContainer viewNotificationContainer, final SQLProvider provider)
      throws CouldntLoadDataException {

    if (viewNotificationContainer.getDatabaseOperation().equals("INSERT")) {
      return; // we ignore inserts as we need to know if this was a module or project view.
    }
    if (viewNotificationContainer.getDatabaseOperation().equals("UPDATE")) {
      final Integer viewId = viewNotificationContainer.getViewId();
      final INaviView view = viewNotificationContainer.getView().get();
      final ImmutableNaviViewConfiguration databaseViewConfiguration =
          view.getConfiguration().getModule() == null ? provider.loadFlowGraphInformation(
          view.getConfiguration().getProject(), viewId)
          : provider.loadFlowGraphInformation(view.getConfiguration().getModule(), viewId);

      if (databaseViewConfiguration == null) {
        return;
      }
      if (!databaseViewConfiguration.getName().equals(view.getConfiguration().getName())) {
        view.getConfiguration().setNameInternal(databaseViewConfiguration.getName());
      }
      if (databaseViewConfiguration.getDescription()
          != view.getConfiguration().getDescription() && !databaseViewConfiguration.getDescription()
          .equals(view.getConfiguration().getDescription())) {
        view.getConfiguration().setDescriptionInternal(databaseViewConfiguration.getDescription());
      }
      if (databaseViewConfiguration.getStarState() != view.getConfiguration().isStared()) {
        view.getConfiguration().setStaredInternal(databaseViewConfiguration.getStarState());
      }
      view.getConfiguration()
          .setModificationDateInternal(databaseViewConfiguration.getModificationDate());
    }
    if (viewNotificationContainer.getDatabaseOperation().equals("DELETE")) {
      return; // we ignore deletes as we need to know if this was a module or project view.
    }
  }
}
