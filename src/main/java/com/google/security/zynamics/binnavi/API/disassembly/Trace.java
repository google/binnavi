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

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceEvent;
import com.google.security.zynamics.binnavi.debug.models.trace.interfaces.ITraceListListener;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.util.ArrayList;
import java.util.List;

// / A single debug trace.
/**
 * Represents a single debug trace. A debug trace is a list of debug events (generally echo
 * breakpoint hits) which is recorded by the debugger and saved to the database.
 */
public final class Trace implements
    ApiObject<com.google.security.zynamics.binnavi.debug.models.trace.TraceList> {
  /**
   * Wrapped internal trace list object.
   */
  private final TraceList trace;

  /**
   * Events of the trace.
   */
  private final List<TraceEvent> events = new ArrayList<>();

  /**
   * Keeps the API trace object synchronized with the wrapped trace object.
   */
  private final InternalTraceListener listener = new InternalTraceListener();

  /**
   * Listeners that are notified about changes in the trace.
   */
  private final ListenerProvider<ITraceListener> listeners = new ListenerProvider<>();

  // / @cond INTERNAL
  /**
   * Creates a new API trace object.
   *
   * @param trace The wrapped internal trace object.
   */
  // / @endcond
  public Trace(final com.google.security.zynamics.binnavi.debug.models.trace.TraceList trace) {
    this.trace = trace;
    for (final ITraceEvent event : this.trace) {
      events.add(new TraceEvent(event));
    }
    this.trace.addListener(listener);
  }

  @Override
  public TraceList getNative() {
    return trace;
  }

  // ! Adds an event to the trace.
  /**
   * Adds a regular breakpoint event to the trace.
   *
   * @param tid The thread ID of the thread that caused the event.
   * @param module The module the address belongs to. This argument can be null.
   * @param address The address of the event.
   * @param type Type of the event.
   */
  public void addEvent(final int tid, final Module module, final Address address,
      final TraceEventType type) {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    trace.addEvent(new com.google.security.zynamics.binnavi.debug.models.trace.TraceEvent(
        tid, new BreakpointAddress(module == null ? null : module.getNative(),
            new UnrelocatedAddress(new CAddress(address.toLong()))), type.getNative(),
        new ArrayList<com.google.security.zynamics.binnavi.debug.models.trace.TraceRegister>()));
  }

  // ! Adds a trace listener.
  /**
   * Adds an object that is notified about changes in the trace.
   *
   * @param listener The listener object that is notified about changes in the trace.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object is already listening on the trace.
   */
  public void addListener(final ITraceListener listener) {
    listeners.addListener(listener);
  }

  // ! Trace description.
  /**
   * Returns the description of the trace.
   *
   * @return The description of the trace.
   */
  public String getDescription() {
    return trace.getDescription();
  }

  // ! Events that belong to the trace.
  /**
   * Returns the debug events that were recorded during the trace.
   *
   * @return A list of trace events.
   */
  public List<TraceEvent> getEvents() {
    return new ArrayList<TraceEvent>(events);
  }

  // ! Trace name.
  /**
   * Returns the name of the trace.
   *
   * @return The name of the trace.
   */
  public String getName() {
    return trace.getName();
  }

  // ! Removes a trace listener.
  /**
   * Removes a listener object from the trace.
   *
   * @param listener The listener object to remove from the trace.
   *
   * @throws IllegalArgumentException Thrown if the listener argument is null.
   * @throws IllegalStateException Thrown if the listener object was not listening on the trace.
   */
  public void removeListener(final ITraceListener listener) {
    listeners.removeListener(listener);
  }

  // ! Saves the trace to the database.
  /**
   * Saves the trace to the database.
   *
   * @throws CouldntSaveDataException Thrown if the trace could not be saved.
   */
  public void save() throws CouldntSaveDataException {
    try {
      trace.save();
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the trace description.
  /**
   * Changes the description of the trace.
   *
   * @param description The new description of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the new description could not be saved to the
   *         database.
   */
  public void setDescription(final String description) throws CouldntSaveDataException {
    try {
      trace.setDescription(description);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Changes the trace name.
  /**
   * Changes the name of the trace.
   *
   * @param name The new name of the trace.
   *
   * @throws CouldntSaveDataException Thrown if the new name could not be saved to the database
   */
  public void setName(final String name) throws CouldntSaveDataException {
    try {
      trace.setName(name);
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // ! Printable representation of the trace.
  /**
   * Returns a string representation of the trace.
   *
   * @return A string representation of the trace.
   */
  @Override
  public String toString() {
    return String.format("Trace '%s' [%d events]", getName(), trace.getEventCount());
  }

  /**
   * Keeps the API trace object synchronized with the wrapped trace object.
   */
  private class InternalTraceListener implements ITraceListListener {
    @Override
    public void changedDescription(final TraceList traceList) {
      for (final ITraceListener listener : listeners) {
        // ESCA-JAVA0166:
        try {
          listener.changedDescription(Trace.this, traceList.getDescription());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void changedName(final TraceList traceList) {
      for (final ITraceListener listener : listeners) {
        try {
          listener.changedName(Trace.this, traceList.getName());
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void eventAdded(final TraceList trace, final ITraceEvent event) {
      final TraceEvent newEvent = new TraceEvent(event);

      events.add(newEvent);

      for (final ITraceListener listener : listeners) {
        try {
          listener.addedEvent(Trace.this, newEvent);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }
  }
}
