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
package com.google.security.zynamics.binnavi.Gui.Debug.ThreadInformationPanel.Implementation;

import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadState;

/**
 * Contains implementations of thread-related functions.
 */
public final class CThreadFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CThreadFunctions() {
  }

  /**
   * Resumes a thread.
   *
   * @param thread The thread to resume.
   */
  public static void resume(final TargetProcessThread thread) {
    thread.setState(ThreadState.RUNNING);
  }

  /**
   * Suspends a thread.
   *
   * @param thread The thread to suspend.
   */
  public static void suspend(final TargetProcessThread thread) {
    thread.setState(ThreadState.SUSPENDED);
  }
}
