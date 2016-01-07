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
package com.google.security.zynamics.binnavi.Database.PostgreSQL.Notifications;

import java.util.Set;

import com.google.common.collect.Sets;

/**
 * Contains all possible PostgreSQL notifications channels currently implemented.
 */
public enum NotificationChannel {

  /**
   * PostgreSQL notification channel for all comment related changes.
   */
  comment_changes,

  /**
   * PostgreSQL notification channel for all view related changes.
   */
  view_changes,

  /**
   * PostgreSQL notification channel for all function related changes.
   */
  function_changes,

  /**
   * PostgreSQL notification channel for all section related changes.
   */
  section_changes,

  /**
   * PostgreSQL notification channel for all type instance related changes.
   */
  type_instances_changes,

  /**
   * PostgreSQL notification channel for all type related changes.
   */
  types_changes;

  public static Set<NotificationChannel> all() {
    return Sets.newHashSet(comment_changes, view_changes, function_changes, section_changes,
        type_instances_changes, types_changes);
  }
}
