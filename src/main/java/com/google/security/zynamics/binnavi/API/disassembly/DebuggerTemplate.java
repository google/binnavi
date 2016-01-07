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
package com.google.security.zynamics.binnavi.API.disassembly;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;



// / Can be used to create debugger objects
/**
 * Represents a debugger template. Debugger templates can be assigned to projects and modules where
 * they are converted to Debugger objects that can be used to debug the targets.
 */
public final class DebuggerTemplate implements
    ApiObject<com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate> {
  /**
   * The wrapped internal debugger template object.
   */
  private final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate m_template;

  /**
   * Keeps the API debugger template object synchronized with the internal debugger template object.
   */
  private final InternalTemplateListener m_internalListener = new InternalTemplateListener();

  /**
   * Listeners that are notified about changes in the debugger template.
   */
  private final ListenerProvider<IDebuggerTemplateListener> m_listeners =
      new ListenerProvider<IDebuggerTemplateListener>();

  // / @cond INTERNAL
  /**
   * Creates a new debugger template.
   *
   * @param template The wrapped internal debugger template object.
   */
  // / @endcond
  public DebuggerTemplate(
      final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate template) {
    m_template = template;

    m_template.addListener(m_internalListener);
  }

  @Override
  public com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate getNative() {
    return m_template;
  }

  // ! Adds a debugger template listener.
  /**
   * Adds an object that is notified about changes in the debugger template.
   *
   * @param listener The listener object that is notified about changes in the debugger template.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the
   *         debugger template.
   */
  public void addListener(final IDebuggerTemplateListener listener) {
    m_listeners.addListener(listener);
  }

  // / @cond INTERNAL
  /**
   * Frees allocated resources.
   */
  // / @endcond
  public void dispose() {
    m_template.removeListener(m_internalListener);
  }

  // ! Host of the debug client.
  /**
   * Returns the host of the debug client used by the debugger template.
   *
   * @return The host of the debug client.
   */
  public String getHost() {
    return m_template.getHost();
  }

  // ! Name of the debugger template.
  /**
   * Returns the name of the debugger template.
   *
   * @return The name of the debugger template.
   */
  public String getName() {
    return m_template.getName();
  }

  // ! Port of the debug client.
  /**
   * Returns the port of the debug client used by the debugger template.
   *
   * @return The port of the debug client.
   */
  public int getPort() {
    return m_template.getPort();
  }

  // ! Removes a debugger template listener.
  /**
   * Removes a listener object from the debugger template.
   *
   * @param listener The listener object to remove from the debugger template.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the debugger
   *         template.
   */
  public void removeListener(final IDebuggerTemplateListener listener) {
    m_listeners.removeListener(listener);
  }

  // ! Changes the debug client host.
  /**
   * Changes the host of the debug client used by the debugger template.
   *
   * @param host The new debug client host.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be updated.
   */
  public void setHost(final String host) throws CouldntSaveDataException {
    try {
      m_template.setHost(host);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the debugger template name.
  /**
   * Changes the name of the debugger template.
   *
   * @param name The new name of the debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be updated.
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      m_template.setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the debug client port.
  /**
   * Changes the port of the debug client used by the debugger template.
   *
   * @param port The new debug client port.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be updated.
   */
  public void setPort(final int port) throws CouldntSaveDataException {
    try {
      m_template.setPort(port);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  @Override
  public String toString() {
    return String.format("Debugger Template '%s' (%s:%d)", getName(), getHost(), getPort());
  }

  /**
   * Keeps the API debugger template object synchronized with the internal debugger template object.
   */
  private class InternalTemplateListener implements
      com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateListener {
    @Override
    public void changedHost(
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      for (final IDebuggerTemplateListener listener : m_listeners) {
        try {
          listener.changedHost(DebuggerTemplate.this, debugger.getHost());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      for (final IDebuggerTemplateListener listener : m_listeners) {
        try {
          listener.changedName(DebuggerTemplate.this, debugger.getName());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedPort(
        final com.google.security.zynamics.binnavi.debug.debugger.DebuggerTemplate debugger) {
      for (final IDebuggerTemplateListener listener : m_listeners) {
        try {
          listener.changedPort(DebuggerTemplate.this, debugger.getPort());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
