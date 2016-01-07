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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.interfaces;

import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications.NotificationChannel;

import org.postgresql.PGNotification;

import java.util.Collection;

/**
 * This interface can be used when interested in any NOTIFICATION events from the back end database.
 */
public interface PostgreSQLNotificationListener {
  /**
   * This notification is used if new channels have been added the the set of channels the
   * notification provider is currently listening on.
   * 
   * @param provider The provider over which the channels where added.
   * @param channels The channels that where added to the set of listened channels.
   */
  void listenedChannelsAdded(SQLProvider provider, Collection<NotificationChannel> channels);

  /**
   * This notification is used if channels have been removed from the set of channels the
   * notification provider is currently listening on.
   * 
   * @param provider The provider over which the channels where removed.
   * @param channels The channels that where removed from the set of listened channels.
   */
  void listenedChannelsRemoved(SQLProvider provider, Collection<NotificationChannel> channels);

  /**
   * This notification is used if the table channel has received a notification.
   * 
   * @param provider The provider over which the notification has been received.
   * @param notifications The collection of notifications that have been received.
   */
  void receviedTableNotifications(SQLProvider provider, Collection<PGNotification> notifications);
}
