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
package com.google.security.zynamics.binnavi.API.helpers;

import java.awt.Window;


import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;

// / Progress dialog class
/**
 * This class can be used to display a progress dialog while long operations are running.
 */
public final class ProgressDialog {
  /**
   * Do not create objects of this class.
   */
  private ProgressDialog() {
    // You are not supposed to instantiate this class
  }

  // ! Shows a progress dialog.
  /**
   * Shows a progress dialog.
   *
   * @param parent The parent window of the progress dialog.
   * @param title The title of the progress dialog.
   * @param thread The thread which implements the long-running operation.
   *
   * @return The exception thrown during the execution of IProgressThread::run or null if no
   *         exception was thrown.
   */
  public static Exception show(
      final Window parent, final String title, final IProgressThread thread) {
    Preconditions.checkNotNull(thread, "Error: Thread argument can't be null");

    final EndlessHelperWrapper helperThread = new EndlessHelperWrapper(thread);

    CProgressDialog.showEndless(parent, title, helperThread);

    return helperThread.getException();
  }

  /**
   * Wraps endless helper threads.
   */
  private static class EndlessHelperWrapper extends CEndlessHelperThread {
    /**
     * The wrapped API thread object.
     */
    private final IProgressThread m_thread;

    /**
     * Creates a new wrapper object.
     *
     * @param thread The wrapped API thread object.
     */
    private EndlessHelperWrapper(final IProgressThread thread) {
      m_thread = thread;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      m_thread.run();
    }

    @Override
    public void closeRequested() {
      if (m_thread.close()) {
        finish();
      }
    }
  }
}
