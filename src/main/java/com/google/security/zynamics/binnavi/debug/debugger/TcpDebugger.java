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
package com.google.security.zynamics.binnavi.debug.debugger;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.connection.DebugConnection;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener;

/**
 * Debugger class that communicates with a debug client via TCP/IP.
 */
public final class TcpDebugger extends AbstractDebugger {
  /**
   * The debug connection that is used to communicate with the debug client.
   */
  private DebugConnection debuggerConnection;

  /**
   * Template that was used to create the debugger.
   */
  private final DebuggerTemplate template;

  /**
   * Listens on relevant changes in the template.
   */
  private final IDebuggerTemplateListener internalTemplateListener = new InternalTemplateListener();

  /**
   * The settings for the debug target.
   */
  private final DebugTargetSettings debugTargetSettings;

  /**
   * Creates a new TCP/IP debugger object from the given debugger template. If the debugger template
   * changes, the debugger is updated automatically.
   *
   * @param template The debugger template that describes the debug connection information.
   * @param targetSettings The target settings.
   */
  public TcpDebugger(final DebuggerTemplate template, final DebugTargetSettings targetSettings) {
    this.template =
        Preconditions.checkNotNull(template, "IE00818: Debugger template argument can not be null");
    debuggerConnection = new DebugConnection(this.template.getHost(), this.template.getPort());
    debugTargetSettings = Preconditions.checkNotNull(targetSettings,
        "IE01670: targetSettings argument can not be null");
    this.template.addListener(internalTemplateListener);
  }

  /**
   * Updates the debugger connection if possible after new template data was received.
   */
  private void updateConnection() {
    if ((debuggerConnection == null) || !isConnected()) {
      debuggerConnection = new DebugConnection(template.getHost(), template.getPort());
    }
  }

  @Override
  public void close() {
    super.setTerminated();
    debuggerConnection = null;
  }

  @Override
  public void connect() throws DebugExceptionWrapper {
    super.connect(debuggerConnection);
  }

  @Override
  public DebugTargetSettings getDebugTargetSettings() {
    return debugTargetSettings;
  }

  /**
   * Returns the template Id from the database so this debugger instance can be uniquely identified.
   */
  @Override
  public int getId() {
    return template.getId();
  }

  @Override
  public String getPrintableString() {
    return String.format("%s (%s:%d)", template.getName(), template.getHost(), template.getPort());
  }

  @Override
  public String toString() {
    return getPrintableString();
  }

  /**
   * Updates the debugger object when the debugger template changes.
   */
  private class InternalTemplateListener implements IDebuggerTemplateListener {
    @Override
    public void changedHost(final DebuggerTemplate debugger) {
      updateConnection();
    }

    @Override
    public void changedName(final DebuggerTemplate debugger) {
      // Updating the connection is not necessary if the name of the
      // debugger changed.
    }

    @Override
    public void changedPort(final DebuggerTemplate debugger) {
      updateConnection();
    }
  }
}
