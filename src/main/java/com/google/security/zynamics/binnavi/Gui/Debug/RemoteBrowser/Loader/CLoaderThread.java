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
package com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.Loader;

import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.FileBrowser.CRemoteFileBrowser;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;



/**
 * Worker thread for requesting target file/process information from the debug client while a
 * progress dialog is active.
 */
public final class CLoaderThread extends CEndlessHelperThread implements ILoaderThread {
  /**
   * Connection to the debug client.
   */
  private final IDebugger m_debugger;

  /**
   * Path for which information is requested. This attribute can be null.
   */
  private final String m_path;

  /**
   * Keeps track of important debugger events while waiting.
   */
  private final CDebuggerListener m_listener;

  /**
   * Flag that indicates whether the thread should keep waiting.
   */
  private boolean m_finished = false;

  /**
   * Creates a new thread object.
   *
   * @param browser The browser that displays the simulated remote filesystem.
   * @param debugger Connection to the debug client.
   * @param path Path for which information is requested. This argument can be null.
   */
  public CLoaderThread(
      final CRemoteFileBrowser browser, final IDebugger debugger, final String path) {
    m_debugger = debugger;
    m_path = path;

    m_listener = new CDebuggerListener(browser, this);

    m_debugger.addListener(m_listener);
  }

  @Override
  protected void runExpensiveCommand() throws Exception {
    try {
      if (m_path == null) {
        m_debugger.requestFileSystem();
      } else {
        m_debugger.requestFileSystem(m_path);
      }

      while (!m_finished) {
        Thread.sleep(100);
      }
    } finally {
      m_debugger.removeListener(m_listener);
    }
  }

  @Override
  public void finished() {
    m_finished = true;
  }
}
