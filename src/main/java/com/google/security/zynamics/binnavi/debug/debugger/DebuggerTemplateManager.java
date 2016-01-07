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
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntDeleteException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebuggerTemplateManagerListener;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.net.NetHelpers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages the debugger templates of a database.
 */
public final class DebuggerTemplateManager implements Iterable<DebuggerTemplate>,
    IDatabaseObject {
  /**
   * The debuggers templates that are managed by this manager.
   */
  private final List<DebuggerTemplate> debuggers = new ArrayList<>();

  /**
   * The SQL provider that is used to synchronize the debugger templates with the database.
   */
  private final SQLProvider sqlProvider;

  /**
   * Listener objects that are notified about changes in the debugger template manager.
   */
  private final ListenerProvider<IDebuggerTemplateManagerListener> listeners =
      new ListenerProvider<>();

  /**
   * Creates a new debugger template manager object for a given database.
   *
   * @param provider The SQL provider that synchronizes the debugger templates with the database.
   */
  public DebuggerTemplateManager(final SQLProvider provider) {
    sqlProvider =
        Preconditions.checkNotNull(provider, "IE00805: Provider argument can not be null");
  }

  /**
   * Adds a debugger template to the debugger template manager.
   *
   * @param debugger The debugger template to add to the template manager.
   */
  public void addDebugger(final DebuggerTemplate debugger) {
    Preconditions.checkNotNull(debugger, "IE00806: Debugger can not be null");
    Preconditions.checkArgument(!debuggers.contains(debugger),
        "IE00807: Can not add debugger description more than once");
    Preconditions.checkArgument(debugger.inSameDatabase(sqlProvider),
        "IE00808: Debugger template and debugger template manager are in different databases");

    debuggers.add(debugger);

    for (final IDebuggerTemplateManagerListener listener : listeners) {
      try {
        listener.addedDebugger(this, debugger);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Adds a listener object that is notified about changes in the template manager.
   *
   * @param listener The listener object.
   */
  public void addListener(final IDebuggerTemplateManagerListener listener) {
    listeners.addListener(listener);
  }

  /**
   * Creates a new debugger object.
   *
   * @param name Name of the debugger template.
   * @param host Host of the debug client.
   * @param port Port of the debug client.
   *
   * @return The created debugger template.
   *
   * @throws CouldntSaveDataException Thrown if the debugger template could not be stored to the
   *         database.
   */
  public DebuggerTemplate createDebugger(final String name, final String host, final int port)
      throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00809: Name argument can not be null");
    Preconditions.checkNotNull(host, "IE00810: Host argument can not be null");
    Preconditions.checkArgument(NetHelpers.isValidPort(port), "IE00811: Invalid port");

    final DebuggerTemplate debugger = sqlProvider.createDebuggerTemplate(name, host, port);

    addDebugger(debugger);

    return debugger;
  }

  /**
   * Returns the number of templates managed by this template manager.
   *
   * @return The number of templates managed by this template manager.
   */
  public int debuggerCount() {
    return debuggers.size();
  }

  /**
   * Finds a debugger with a given ID number.
   *
   * @param debuggerId The ID number to search for.
   *
   * @return The debugger object with the given ID or null if there is no such debugger.
   */
  public DebuggerTemplate findDebugger(final int debuggerId) {
    for (final DebuggerTemplate description : debuggers) {
      if (description.getId() == debuggerId) {
        return description;
      }
    }

    return null;
  }

  /**
   * Returns the debugger with the given index.
   *
   * @param index The index of the debugger (0 <= index < debuggerCount())
   *
   * @return The debugger with the given index.
   */
  public DebuggerTemplate getDebugger(final int index) {
    return debuggers.get(index);
  }

  /**
   * Returns all managed debugger templates.
   *
   * @return All managed debugger templates.
   */
  public List<DebuggerTemplate> getDebuggers() {
    return new ArrayList<DebuggerTemplate>(debuggers);
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(sqlProvider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return sqlProvider == provider;
  }

  @Override
  public Iterator<DebuggerTemplate> iterator() {
    return debuggers.iterator();
  }

  /**
   * Removes a debugger template from the database.
   *
   * @param debugger The debugger template to be removed.
   *
   * @throws CouldntDeleteException Thrown if the debugger template could not be removed from the
   *         database.
   */
  public void removeDebugger(final DebuggerTemplate debugger) throws CouldntDeleteException {
    Preconditions.checkNotNull(debugger, "IE00812: Debugger argument can not be null");
    Preconditions.checkArgument(debugger.inSameDatabase(sqlProvider),
        "IE00813: Debugger template and debugger template manager are not in the same database");

    sqlProvider.deleteDebugger(debugger);

    debuggers.remove(debugger);

    for (final IDebuggerTemplateManagerListener listener : listeners) {
      try {
        listener.removedDebugger(this, debugger);
      } catch (final Exception e) {
        CUtilityFunctions.logException(e);
      }
    }
  }

  /**
   * Removes a listener object from the template manager.
   *
   * @param listener The template manager to remove.
   */
  public void removeListener(final IDebuggerTemplateManagerListener listener) {
    listeners.removeListener(listener);
  }
}
