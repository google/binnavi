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
package com.google.security.zynamics.binnavi.API.debug;

/* ! \file ThreadState.java \brief Contains the ThreadState enumeration * */

// / Describes the state of target process threads.
/**
 * Enumeration of potential thread states.
 */
public enum ThreadState {
  /**
   * The thread is running.
   */
  Running,

  /**
   * The thread is suspended.
   */
  Suspended;

  // / @cond INTERNAL
  /**
   * Converts an internal thread state value to an API thread state value.
   *
   * @param state The internal thread state value.
   *
   * @return The API thread state value.
   */
  public static ThreadState convert(final com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState state) {
    switch (state) {
      case RUNNING:
        return Running;
      case SUSPENDED:
        return Suspended;
      default:
        throw new IllegalStateException("Error: Unknown thread state");
    }
  }

  /**
   * Converts an API thread state value into an internal thread state value.
   *
   * @return The internal thread state value.
   */
  public final com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState getNative() {
    switch (this) {
      case Running:
        return com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState.RUNNING;
      case Suspended:
        return com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState.SUSPENDED;
      default:
        throw new IllegalStateException("Error: Unknown thread state");
    }
  }
  // / @endcond
}
