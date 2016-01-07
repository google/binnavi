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

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.API.debug.Debugger;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.Loader.CRemoteFileBrowserLoader;


/**
 * Wrapper class to expose the remote file browser dialog to the API. The dialog can be used to
 * select a target process when the debugger is started.
 */
public class RemoteFileBrowserLoader implements ApiObject<CRemoteFileBrowserLoader> {
  private final CRemoteFileBrowserLoader remoteFileBrowser;

  /**
   * Creates a new instance of the remote file browser class.
   *
   * @param parent The parent window of the dialog.
   * @param debugger The debugger object used to communicate with the debug client during target
   *        selection.
   */
  public RemoteFileBrowserLoader(final JFrame parent, final Debugger debugger) {
    remoteFileBrowser = new CRemoteFileBrowserLoader(parent, debugger.getNative());
  }

  @Override
  public CRemoteFileBrowserLoader getNative() {
    return remoteFileBrowser;
  }

  /**
   * Shows the remote file browser dialog.
   */
  public boolean load() {
    return remoteFileBrowser.load();
  }
}
