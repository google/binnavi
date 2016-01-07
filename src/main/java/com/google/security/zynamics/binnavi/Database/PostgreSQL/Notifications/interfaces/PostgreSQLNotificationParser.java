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

import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;

import org.postgresql.PGNotification;

import java.util.Collection;

/**
 * Interface to define parsers for {@link PGNotification notifications} from am PostgreSQL back end.
 *
 * @param <T> The type of the container used to carry the parsed notifications.
 */
public interface PostgreSQLNotificationParser<T> {

  /**
   * This function parses the incoming {@link PGNotification notifications} and returns the parsed
   * result in a collection.
   *
   * @param notifications The {@link Collection collection} of {@link PGNotification notifications}
   *        to parse.
   * @param provider The {@link SQLProvider provider} over which the {@link PGNotification
   *        notifications} have been received.
   *
   * @return A {@link Collection collection} of Type T parser result objects.
   */
  Collection<T> parse(Collection<PGNotification> notifications, SQLProvider provider);

  /**
   * This function informs the necessary objects about the changes which is reads from the parser
   * results.
   *
   * @param container The {@link Collection collection} of parser results.
   * @param provider The {@link SQLProvider provider} associated with the results.
   *
   * @throws CouldntLoadDataException if loading data for the notified objects could not be loaded.
   */
  void inform(Collection<T> container, SQLProvider provider) throws CouldntLoadDataException;
}
