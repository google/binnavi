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
package com.google.security.zynamics.binnavi.debug.models.trace;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.Database.Interfaces.SQLProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListListener;
import com.google.security.zynamics.binnavi.disassembly.IDatabaseObject;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of trace events.
 */
public final class TraceList implements Iterable<ITraceEvent>, IDatabaseObject {
  /**
   * The database id of the trace list.
   */
  private final int id;

  /**
   * The name of the trace list.
   */
  private String traceName;

  /**
   * The description of the trace list.
   */
  private String traceDescription;

  /**
   * List of individual events managed by the event list.
   */
  private final List<ITraceEvent> traceEvents = new ArrayList<>();

  /**
   * Listeners that are notified about changes in the event list.
   */
  private final ListenerProvider<ITraceListListener> listeners = new ListenerProvider<>();

  /**
   * SQL provider that is used to synchronize the trace list with the database.
   */
  private final SQLProvider sqlProvider;

  /**
   * Creates a new trace list.
   *
   * @param id The database id of the trace list.
   * @param name The name of the trace list.
   * @param description The description of the trace list.
   * @param provider The SQL provider that is used to synchronize the event list with the database.
   */
  public TraceList(final int id, final String name, final String description,
      final SQLProvider provider) {
    Preconditions.checkArgument(id >= 0, "IE00777: ID argument can not be null");
    traceName = Preconditions.checkNotNull(name, "IE00778: Name can not be null");
    traceDescription = Preconditions.checkNotNull(description, "IE00779: Comment can not be null");
    sqlProvider =
        Preconditions.checkNotNull(provider, "IE00780: Provider argument can not be null");
    this.id = id;
  }

  /**
   * Adds a trace event to the end of the list.
   *
   * @param event The trace event to add.
   */
  public void addEvent(final ITraceEvent event) {
    Preconditions.checkNotNull(event, "IE00781: Trace event can not be null");
    if (traceEvents.contains(event)) {
      throw new IllegalStateException("IE00782: Trace event already belongs to the trace list");
    }
    traceEvents.add(event);
    for (final ITraceListListener listener : listeners) {
      try {
        listener.eventAdded(this, event);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  /**
   * Adds a listener object that is notified about changes in the event list.
   *
   * @param listener The listener object.
   */
  public void addListener(final ITraceListListener listener) {
    listeners.addListener(listener);
  }

  public String getDescription() {
    return traceDescription;
  }

  public int getEventCount() {
    return traceEvents.size();
  }

  public List<ITraceEvent> getEvents() {
    return new ArrayList<ITraceEvent>(traceEvents);
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return traceName;
  }

  @Override
  public boolean inSameDatabase(final IDatabaseObject provider) {
    return provider.inSameDatabase(sqlProvider);
  }

  @Override
  public boolean inSameDatabase(final SQLProvider provider) {
    return provider.equals(sqlProvider);
  }

  @Override
  public Iterator<ITraceEvent> iterator() {
    return traceEvents.iterator();
  }

  public void removeListener(final ITraceListListener listener) {
    listeners.removeListener(listener);
  }

  public void save() throws CouldntSaveDataException {
    sqlProvider.save(this);
  }

  public void setDescription(final String description) throws CouldntSaveDataException {
    Preconditions.checkNotNull(description, "IE00783: Comment can not be null");
    if (traceDescription.equals(description)) {
      return;
    }
    sqlProvider.setDescription(this, description);
    traceDescription = description;
    for (final ITraceListListener listener : listeners) {
      try {
        listener.changedDescription(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  public void setName(final String name) throws CouldntSaveDataException {
    Preconditions.checkNotNull(name, "IE00784: Name can not be null");
    if (traceName.equals(name)) {
      return;
    }
    sqlProvider.setName(this, name);
    traceName = name;
    for (final ITraceListListener listener : listeners) {
      try {
        listener.changedName(this);
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }
}
