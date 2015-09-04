/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.CConnection;
import com.google.security.zynamics.binnavi.Database.CDatabaseConfiguration;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.FunctionNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypeInstancesNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.TypesNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.containers.ViewNotificationContainer;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces.PostgreSQLNotificationListener;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLCommentNotificationParser;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLFunctionNotificationParser;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLTypeInstancesNotificationParser;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLTypesNotificationParser;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.parsers.PostgreSQLViewNotificationParser;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.PostgreSQLHelpers;
import com.google.security.zynamics.binnavi.Database.PostgreSQLProvider;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import org.postgresql.PGNotification;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class purpose is to connect to the database, receive notifications and to pass them along to
 * the parsers. Notifications from the database describe changes on which the receiving instance of
 * BinNavi can but not must react.
 */
public class PostgreSQLNotificationProvider {
  /**
   * The set of channels for which we want to receive notifications.
   */
  private final Set<NotificationChannel> m_channels = new HashSet<NotificationChannel>();

  /**
   * The scheduler used to periodically schedule the poll task for the database notifications.
   */
  private final ScheduledExecutorService m_scheduler = Executors.newSingleThreadScheduledExecutor();

  /**
   * The Runnable which does the actual poll work on the database.
   */
  private final CNotificationQueuePoller m_notificationPoller;

  /**
   * The database configuration which is used to initially build our connection to the database
   * server.
   */
  private final CDatabaseConfiguration m_configuration;

  /**
   * The connection solely used by the notification provider.
   */
  private CConnection m_connection;

  /**
   * Listeners that are notified about changes in the notification provider.
   */
  private final ListenerProvider<PostgreSQLNotificationListener> m_listeners =
      new ListenerProvider<PostgreSQLNotificationListener>();

  /**
   * Keeps track of all notification providers. One for each database.
   */
  private static Map<SQLProvider, PostgreSQLNotificationProvider> m_notificationProviders =
      new HashMap<SQLProvider, PostgreSQLNotificationProvider>();

  /**
   * Connects this notification provider to a specific connection.
   */
  private final SQLProvider m_provider;

  /**
   * The back end PID of the provider BinNavi uses to get information from the database. This PID
   * can be used to verify if we are self notifying us or if another instance of BinNavi changed a
   * comment.
   *
   *  TODO (timkornau): this must be somehow synchronized if the connection from the provider has
   * terminated and was restarted.
   */
  private final int m_backendPID;

  /**
   * Creates a new Instance of the CPostgreSQLNotificationProvider. During initialization a new
   * connection which is only used within this class is established. This constructor only sets up
   * the connection and the worker thread but does not start the worker thread. The worker thread
   * can be started with the startPolling() method.
   *
   * @param configuration The database configuration used to set up the connection to the database.
   * @param provider The provider BinNavi uses to retrieve information from the database.
   * @throws CouldntLoadDriverException Thrown if the connection setup did not find the database
   *         driver.
   * @throws SQLException Thrown if the connection setup had an issue.
   */
  private PostgreSQLNotificationProvider(final CDatabaseConfiguration configuration,
      final SQLProvider provider) throws CouldntLoadDriverException, SQLException {
    m_configuration = configuration;
    m_provider = provider;
    m_backendPID =
        PostgreSQLHelpers.getBackendPID(((PostgreSQLProvider) m_provider).getConnection());
    m_connection = new CConnection(m_configuration);
    m_notificationPoller = new CNotificationQueuePoller();
  }

  public static synchronized boolean contains(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE02415: provider argument can not be null");
    return m_notificationProviders.containsKey(provider);
  }

  /**
   * Returns the instance of the {@link PostgreSQLNotificationProvider} if if exists for the
   * {@link SQLProvider} supplied as argument null otherwise.
   *
   * @param provider The {@link SQLProvider} for which the {@link PostgreSQLNotificationProvider} is
   *        queried.
   * @return The {@link PostgreSQLNotificationProvider} or null if it does not exist.
   */
  public static synchronized PostgreSQLNotificationProvider get(final SQLProvider provider) {
    Preconditions.checkNotNull(provider, "IE02416: provider argument can not be null");
    return m_notificationProviders.containsKey(provider) ? m_notificationProviders.get(provider)
        : null;
  }

  /**
   * Initializes a {@link PostgreSQLNotificationProvider}.
   *
   * @param provider The {@link SQLProvider} to which the {@link PostgreSQLNotificationProvider} is
   *        associated.
   * @param databaseConfiguration The {@link CDatabaseConfiguration} which is used for the
   *        connection to the database.
   *
   * @return The initialized {@link PostgreSQLNotificationProvider}.
   *
   * @throws CouldntLoadDriverException If the database driver could not be loaded.
   * @throws SQLException If there has been an error which initializing the
   *         {@link PostgreSQLNotificationProvider}.
   */
  public static synchronized PostgreSQLNotificationProvider initialize(final SQLProvider provider,
      final CDatabaseConfiguration databaseConfiguration) throws CouldntLoadDriverException,
      SQLException {
    Preconditions.checkNotNull(provider, "IE02417: provider argument can not be null");
    Preconditions.checkNotNull(databaseConfiguration,
        "IE02418: databaseConfiguration argument can not be null");
    m_notificationProviders.put(provider,
        new PostgreSQLNotificationProvider(databaseConfiguration, provider));
    return get(provider);
  }

  /**
   * Function sends a "LISTEN channel" message to the database server for all specified channels in
   * the set. There is currently no way to subscribe to multiple channels in a single query.
   *
   * @param channelNames The channel names to listen to.
   */
  private synchronized void sendListens(final Set<NotificationChannel> channelNames) {
    for (final NotificationChannel channel : channelNames) {
      try {
        m_connection.executeUpdate("LISTEN " + channel.name(), true);
      } catch (final SQLException exception) {
        NaviLogger.severe("Error: Could not send LISTEN command to database server: %s", exception);
      }
    }
    for (final PostgreSQLNotificationListener listener : m_listeners) {
      try {
        listener.listenedChannelsAdded(m_provider, channelNames);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Function sends a "UNLISTEN channel" message to the database server for all specified channels
   * in the set. There is currently no way to unsubscribe from multiple channels in a single query.
   *
   * @param channelNames The channel names to not listen to anymore.
   */
  private synchronized void sendUnlistens(final Set<NotificationChannel> channelNames) {
    for (final NotificationChannel channel : channelNames) {
      try {
        m_connection.executeUpdate("UNLISTEN " + channel.name(), true);
      } catch (final SQLException exception) {
        NaviLogger.severe("Error: Could not send UNLISTEN command to database server: %s",
            exception);
      }
    }
    for (final PostgreSQLNotificationListener listener : m_listeners) {
      try {
        listener.listenedChannelsRemoved(m_provider, channelNames);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Method that forwards the addListener() to the {@link ListenerProvider}.
   *
   * @param listener The listener to be added to the set of listeners.
   */
  public synchronized void addListener(final PostgreSQLNotificationListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Returns an immutable set of the currently listened channels.
   *
   * @return An immutable set of the currently listened channels.
   */
  public synchronized Set<NotificationChannel> getCurrentlyListenedChannels() {
    return ImmutableSet.copyOf(m_channels);
  }

  /**
   * Adds the set of channel names to the set of listened channels.
   *
   * @param channelNames The set of channel names to listen to.
   */
  public synchronized void listen(final Set<NotificationChannel> channelNames) {
    m_channels.addAll(channelNames);
  }

  /**
   * Method that forwards the removeListener() to the {@link ListenerProvider}.
   *
   * @param listener The listener to be removed from the set of listeners.
   */
  public synchronized void removeListener(final PostgreSQLNotificationListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Activates the scheduler and queues the notification poller.
   */
  public synchronized void startPolling() {
    m_scheduler.scheduleAtFixedRate(m_notificationPoller, 0, 500, TimeUnit.MILLISECONDS);
  }

  /**
   * Deactivates the scheduler nicely.
   */
  public synchronized void stopPolling() {
    m_scheduler.shutdown();
  }

  public synchronized boolean unInitialize() {
    sendUnlistens(m_channels);
    unlisten(m_channels);
    stopPolling();
    m_notificationProviders.remove(m_provider);
    m_connection.closeConnection();
    m_connection = null;

    return true;
  }

  /**
   * Removes the set of channel names from the set of listened channels.
   *
   * @param channelNames The set of channel names to not listen to anymore.
   */
  public synchronized void unlisten(final Set<NotificationChannel> channelNames) {
    m_channels.removeAll(channelNames);
  }

  private class CNotificationQueuePoller implements Runnable {
    private final Set<NotificationChannel> m_currentListenedChannels =
        new HashSet<NotificationChannel>();

    public CNotificationQueuePoller() {}

    private void poll() throws SQLException, CouldntLoadDataException {
      final org.postgresql.PGNotification notifications[] =
          ((org.postgresql.PGConnection) m_connection.getConnection()).getNotifications();

      if (notifications == null) {
        return;
      }

      final Collection<PGNotification> commentNotifications = Lists.newArrayList();
      final Collection<PGNotification> viewNotifications = Lists.newArrayList();
      final Collection<PGNotification> functionNotifications = Lists.newArrayList();
      final Collection<PGNotification> typeNotifications = Lists.newArrayList();
      final Collection<PGNotification> typeInstanceNotifications = Lists.newArrayList();

      for (final PGNotification notification : notifications) {
        if (notification.getPID() == m_backendPID) {
          continue; // In this case we are not really interested about the notification as we have
                    // triggered the change
        }
        if (notification.getName().equalsIgnoreCase(NotificationChannel.comment_changes.name())) {
          commentNotifications.add(notification);
          continue;
        }
        if (notification.getName().equalsIgnoreCase(NotificationChannel.view_changes.name())) {
          viewNotifications.add(notification);
          continue;
        }
        if (notification.getName().equalsIgnoreCase(NotificationChannel.function_changes.name())) {
          functionNotifications.add(notification);
          continue;
        }
        if (notification.getName().equalsIgnoreCase(NotificationChannel.types_changes.name())) {
          typeNotifications.add(notification);
          continue;
        }
        if (notification.getName().equalsIgnoreCase(
            NotificationChannel.type_instances_changes.name())) {
          typeInstanceNotifications.add(notification);
          continue;
        }
      }

      final PostgreSQLViewNotificationParser viewParser = new PostgreSQLViewNotificationParser();
      final Collection<ViewNotificationContainer> parsedViewNotifications =
          viewParser.parse(viewNotifications, m_provider);
      viewParser.inform(parsedViewNotifications, m_provider);

      final PostgreSQLFunctionNotificationParser functionParser =
          new PostgreSQLFunctionNotificationParser();
      final Collection<FunctionNotificationContainer> parsedFunctionNotifications =
          functionParser.parse(functionNotifications, m_provider);
      functionParser.inform(parsedFunctionNotifications, m_provider);

      final PostgreSQLCommentNotificationParser commentParser =
          new PostgreSQLCommentNotificationParser();
      commentParser.parse(commentNotifications, m_provider);

      final PostgreSQLTypesNotificationParser typesParser = new PostgreSQLTypesNotificationParser();
      final Collection<TypesNotificationContainer> parsedTypesNotifications =
          typesParser.parse(typeNotifications, m_provider);
      typesParser.inform(parsedTypesNotifications, m_provider);

      final PostgreSQLTypeInstancesNotificationParser typeInstancesParser =
          new PostgreSQLTypeInstancesNotificationParser();
      final Collection<TypeInstancesNotificationContainer> parsedTypeInstanceNotifications =
          typeInstancesParser.parse(typeInstanceNotifications, m_provider);
      typeInstancesParser.inform(parsedTypeInstanceNotifications, m_provider);
    }

    private void syncListenedChannels() {
      if (m_channels.equals(m_currentListenedChannels)) {
        return;
      }

      final Set<NotificationChannel> channels = Sets.newHashSet(m_channels);
      final SetView<NotificationChannel> toUnlisten =
          Sets.difference(m_currentListenedChannels, channels);
      final SetView<NotificationChannel> toListen =
          Sets.difference(channels, m_currentListenedChannels);

      if (!toUnlisten.isEmpty()) {
        sendUnlistens(toUnlisten);
      }
      if (!toListen.isEmpty()) {
        sendListens(toListen);
      }
      if (!toUnlisten.isEmpty() || !toListen.isEmpty()) {
        m_currentListenedChannels.clear();
        m_currentListenedChannels.addAll(channels);
      }
    }

    @Override
    public void run() {
      if (!m_connection.isConnectionValid()) {
        try {
          m_connection = new CConnection(m_configuration);
        } catch (CouldntLoadDriverException | SQLException exception) {
          NaviLogger.severe(
              "Error: Could not reestablish the connection with the database backend %s",
              exception);
        }
      }

      syncListenedChannels();

      try {
        poll();
      } catch (final Exception exception) {
        NaviLogger.severe(
            "Error: Could not get notifications from the PostgreSQL backend with exception %s",
            exception);
        exception.printStackTrace();
      }
    }
  }
}
