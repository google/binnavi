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
package com.google.security.zynamics.binnavi.debug.models.processlist;

import com.google.common.base.Preconditions;

/**
 * Describes a single process.
 */
public final class ProcessDescription {
  /**
   * Process ID
   */
  private final int id;

  /**
   * Process name
   */
  private final String name;

  /**
   * Creates a new process description object.
   *
   * @param pid Process ID
   * @param name Process name
   */
  public ProcessDescription(final int pid, final String name) {
    Preconditions.checkNotNull(name, "IE00746: Name argument can not be null");

    id = pid;
    this.name = name;
  }

  /**
   * Returns the name of the process.
   *
   * @return The name of the process.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the process ID of the process.
   *
   * @return The process ID of the process.
   */
  public int getPID() {
    return id;
  }
}
