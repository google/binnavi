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
package com.google.security.zynamics.binnavi.API.debug;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.Project;
import com.google.security.zynamics.binnavi.API.disassembly.Trace;
import com.google.security.zynamics.binnavi.API.disassembly.TracePoint;
import com.google.security.zynamics.binnavi.APIHelpers.ObjectFinders;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.trace.ModuleTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.ProjectTraceProvider;
import com.google.security.zynamics.binnavi.debug.models.trace.TraceList;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// / Creates debug event trace logs.
/**
 * Using this class it is possible to create trace logs of echo breakpoint events.
 */
public final class TraceLogger {
  /**
   * Debugger used to record the traces.
   */
  private final Debugger debugger;

  /**
   * Project for which the trace is recorded (or null).
   */
  private final Project project;

  /**
   * Module for which the trace is recorded (or null).
   */
  private final Module module;

  /**
   * Wrapped internal logger object.
   */
  private com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger logger;

  // / Creates a new trace logger.
  /**
   * Creates a new trace logger.
   *
   * @param debugger The debugger that sets the echo breakpoints.
   * @param module The module the logged trace belongs to.
   */
  public TraceLogger(final Debugger debugger, final Module module) {
    Preconditions.checkNotNull(debugger, "Error: Debugger argument can not be null");
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");

    this.debugger = debugger;
    this.module = module;
    this.project = null;
  }

  // / Creates a new trace logger.
  /**
   * Creates a new trace logger.
   *
   * @param debugger The debugger that sets the echo breakpoints.
   * @param project The project the logged trace belongs to.
   */
  public TraceLogger(final Debugger debugger, final Project project) {
    Preconditions.checkNotNull(debugger, "Error: Debugger argument can not be null");
    Preconditions.checkNotNull(project, "Error: Project argument can not be null");

    this.debugger = debugger;
    this.project = project;
    this.module = null;
  }

  /**
   * Converts a list of API addresses to internal addresses.
   *
   * @param addresses The API addresses to convert.
   *
   * @return The converted internal addresses.
   */
  private static Set<BreakpointAddress> convertAddresses(final List<TracePoint> addresses) {
    final Set<BreakpointAddress> adds = new HashSet<BreakpointAddress>();

    for (final TracePoint address : addresses) {
      if (address.getModule() == null) {
        adds.add(new BreakpointAddress(null,
            new UnrelocatedAddress(new CAddress(address.getAddress().toLong()))));
      } else {
        adds.add(new BreakpointAddress(address.getModule().getNative(),
            new UnrelocatedAddress(new CAddress(address.getAddress().toLong()))));
      }
    }

    return adds;
  }

  /**
   * Creates a new internal trace logger object.
   *
   * @return The created object.
   */
  private com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger createLogger() {
    return new com.google.security.zynamics.binnavi.debug.models.trace.TraceLogger(
        project == null ? new ModuleTraceProvider(module.getNative())
            : new ProjectTraceProvider(project.getNative()), debugger.getNative());
  }

  /**
   * Creates a new internal trace list object.
   *
   * @param name Name of the new trace list.
   * @param description Description of the new trace list.
   *
   * @return The created trace list object.
   *
   * @throws com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException
   *         Thrown if the trace list object could not be saved to the database.
   */
  private TraceList createTrace(final String name, final String description)
      throws com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException {
    return project == null ? module.getNative().getContent().getTraceContainer()
        .createTrace(name, description)
        : project.getNative().getContent().createTrace(name, description);
  }

  /**
   * Returns the recorded traces for the module/project.
   *
   * @return The recorded traces.
   */
  private Iterable<Trace> getTraces() {
    return project == null ? module.getTraces() : project.getTraces();
  }

  // / Starts trace logging.
  /**
   * Starts trace logging with this logger.
   *
   * @param name The name of the new trace.
   * @param description The description of the new trace.
   * @param addresses List of addresses where echo breakpoints should be set.
   *
   * @return The created event trace.
   *
   * @throws CouldntSaveDataException Thrown if the event trace could not be written to the
   *         database.
   */
  public Trace start(final String name, final String description, final List<TracePoint> addresses)
      throws CouldntSaveDataException {
    Preconditions.checkArgument(debugger.isConnected(), "Error: Debugger must be connected");
    Preconditions.checkArgument(logger == null, "Error: Addresses argument can not be null");
    Preconditions.checkNotNull(addresses, "Error: Addresses argument can not be null");

    for (final TracePoint address : addresses) {
      Preconditions.checkNotNull(address, "Error: Addresses list contains null-elements");
    }

    try {
      final TraceList trace = createTrace(name, description);

      logger = createLogger();

      logger.start(trace, convertAddresses(addresses), 3);

      return ObjectFinders.getObject(trace, getTraces());
    } catch (final com.google.security.zynamics.binnavi.Database.Exceptions.CouldntSaveDataException e) {
      throw new CouldntSaveDataException(e);
    }
  }

  // / Stops trace logging.
  /**
   * Removes all echo breakpoints and stops trace logging.
   */
  public void stop() {
    Preconditions.checkNotNull(logger, "Error: Logger is already stopped");

    logger.stop();

    logger = null;
  }

  // ! Printable representation of the trace logger.
  /**
   * Returns a string representation of the trace logger.
   *
   * @return A string representation of the trace logger.
   */
  @Override
  public String toString() {
    return String.format("TraceLogger [%s : %s]", debugger.toString(),
        project == null ? module.getName() : project.getName());
  }
}
