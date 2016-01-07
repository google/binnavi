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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.net.NetHelpers;

/**
 * A debugger template describes the location of a debug client.
 */
public final class DebuggerTemplate implements IDatabaseObject {
  /**
   * Used to synchronize the debugger template object with the database.
   */
  private final SQLProvider sqlProvider;

  /**
   * Listeners that are notified about changes in the debugger template.
   */
  private final ListenerProvider<IDebuggerTemplateListener> listeners = new ListenerProvider<>();

  /**
   * The ID of the debugger template in the database.
   */
  private final int debuggerTemplateId;

  /**
   * The name of the debugger template.
   */
  private String debuggerTemplateName;
  /**
   * The host address of the debug client.
   */
  private String debugClientHost;

  /**
   * The port of the debug client.
   */
  private int debugClientPort;

  /**
   * Creates a new debugger template object.
   *
   * @param debuggerId The ID of the debugger template in the database.
   * @param name The name of the debugger template.
   * @param host The host address of the debug client.
   * @param port The port of the debug client.
   * @param sqlProvider Used to synchronize the debugger template object with the database.
   */
  public DebuggerTemplate(final int debuggerId, final String name, final String host,
      final int port, final SQLProvider sqlProvider) {
    Preconditions.checkArgument(debuggerId > 0, "IE00796: ID argument must be positive");
    debuggerTemplateName =
        Preconditions.checkNotNull(name, "IE00797: Name argument can not be null");
    debugClientHost = Preconditions.checkNotNull(host, "IE00798: Host argument can not be null");
    Preconditions.checkArgument(NetHelpers.isValidPort(port), "IE00799: Invalid port argument");
    this.sqlProvider =
        Preconditions.checkNotNull(sqlProvider, "IE00800: SQL provider argument can not be null");

    debuggerTemplateId = debuggerId;
    debugClientPort = port;
  }

  /**
   * Adds a listener that is notified about changes in the debugger template.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IDebuggerTemplateListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Returns the host location of the debug client.
   *
   * @return The host location of the debug client.
   */
  public String getHost() {
    return debugClientHost;
  }

  /**
   * Returns the database ID of the debugger template.
   *
   * @return The database ID of the debugger template.
   */
  public int getId() {
    return debuggerTemplateId;
  }

  /**
   * Returns the name of the debugger template.
   *
   * @return The name of the debugger template.
   */
  public String getName() {
    return debuggerTemplateName;
  }

  /**
   * Returns the port of the debug client.
   *
   * @return The port of the debug client.
   */
  public int getPort() {
    return debugClientPort;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject object) {
    Preconditions.checkNotNull(object, "IE00801: Object argument can not be null");
    return object.inSameDatabase(sqlProvider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return sqlProvider == provider;
  }

  /**
   * Removes a listener from the template.
   *
   * @param listener The listener to remove.
   */
  public void removeListener(final IDebuggerTemplateListener listener) {
    listeners.removeListener(listener);
  }

  /**
   * Sets the host of the debug client.
   *
   * @param host The host of the debug client.
   *
   * @throws CouldntSaveDataException Thrown if the debug host could not be updated.
   */
  public void setHost(final String host) throws CouldntSaveDataException {
    Preconditions.checkNotNull(host, "IE00802: Host argument can not be null");

    if (debugClientHost.equals(host)) {
      return;
    }

    sqlProvider.setHost(this, host);

    debugClientHost = host;

    for (final IDebuggerTemplateListener listener : listeners) {
      try {
        listener.changedHost(this);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Changes the name of the debugger template.
   *
   * @param name The new name of the debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the name of the debugger template could not be
   *         updated.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00803: Name argument can not be null");

    if (debuggerTemplateName.equals(name)) {
      return;
    }

    sqlProvider.setName(this, name);

    debuggerTemplateName = name;

    for (final IDebuggerTemplateListener listener : listeners) {
      try {
        listener.changedName(this);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Updates the port of the debug client.
   *
   * @param port The new debug client port.
   *
   * @throws CouldntSaveDataException Thrown if the port could not be updated.
   */
  public void setPort(final int port) throws CouldntSaveDataException {
    Preconditions.checkArgument(NetHelpers.isValidPort(port), "IE00804: Invalid port");

    if (debugClientPort == port) {
      return;
    }

    sqlProvider.setPort(this, port);

    debugClientPort = port;

    for (final IDebuggerTemplateListener listener : listeners) {
      try {
        listener.changedPort(this);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  @Override
  public String toString() {
    return debuggerTemplateName + " - " + debugClientHost + ":" + debugClientPort;
  }
}
