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
package com.google.security.zynamics.binnavi.debug.models.targetinformation;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class that describes what options are supported by a debug client.
 */
public final class DebuggerOptions {
  /**
   * Flag that indicates whether the debugger can detach from the debug target.
   */
  private final boolean canDetach;

  /**
   * Flag that indicates whether the debugger can attach to a target process.
   */
  private final boolean canAttach;

  /**
   * Flag that indicates whether the debugger can terminate from the debug target.
   */
  private final boolean canTerminate;

  /**
   * Flag that indicates whethedebugTargetSettingsr the debugger can determine the memory map of the
   * target process.
   */
  private final boolean canMemmap;

  /**
   * Flag that determines whether the target process is multi-threaded.
   */
  private final boolean canMultithread;

  /**
   * Flag that indicates whether the debug client can validate memory regions.
   */
  private final boolean canValidMemory;

  /**
   * Flag that indicates whether the debug client supports software breakpoints.
   */
  private final boolean canSoftwareBreakpoints;

  /**
   * Maximum number of breakpoints that can be set by the debug client.
   */
  private final int breakpointCounter;

  /**
   * Flag that indicates whether the debug client can arbitrarily halt the target process.
   */
  private final boolean canHalt;

  /**
   * Flag that indicates whether the debug client must always halt the target process before sending
   * other commands.
   */
  private final boolean haltBeforeCommunicating;

  /**
   * Flag that indicates whether the debug client supports a stack.
   */
  private final boolean stackAvailable;

  /**
   * The page size of the target process.
   */
  private final int pageSize;

  /**
   * Flag that indicates whether the debugger is able to break when a module is loaded.
   */
  private final boolean canBreakOnModuleLoad;

  /**
   * Flag that indicates whether the debugger is able to break when a module is unloaded.
   */
  private final boolean canBreakOnModuleUnload;

  /**
   * The set of exceptions that is supported by the debugging client
   */
  private Collection<DebuggerException> exceptions;

  /**
   * This flag indicates whether the debugger is able to handle breakpoint hit counters (during
   * trace mode) which are bigger than one (introduced to prevent "complete rewrite" of the Gdb
   * agent for the ARM architecture).
   */
  private final boolean canTraceCount;

  /**
   * Creates a new debugger options object.
   *
   * @param canDetach Flag that says whether detaching is supported.
   * @param canTerminate Flag that says whether terminating is supported.
   * @param canMemmap Flag that says whether memory maps are supported.
   * @param stackAvailable Flag that says whether stack is supported.
   * @param canValidMemory Flag that says whether memory ranges can be validated.
   * @param canHalt Flag that says whether the target process can be halted.
   * @param haltBeforeCommunicating Flag that says whether the target process must be halted.
   * @param canMultithread Flag that says whether the target process is multi-threaded.
   * @param canSoftwareBreakpoints Flag that says whether software breakpoints can be used.
   * @param breakpointCounter Maximum number of breakpoints that can be set.
   * @param pageSize The page size of the target process.
   * @param exceptions The list of platform exceptions supported by the debugging client
   * @param canBreakOnModuleUnload Specifies whether the debugger allows for breaking when a module
   *        is loaded.
   * @param canBreakOnModuleLoad specifies whether the debugger allows for breaking when a module is
   *        unloaded.
   * @param canTraceCount specifies whether the debugger is able to handle breakpoint hit counter
   *        bigger than one.
   */
  public DebuggerOptions(final boolean canDetach,
      final boolean canAttach,
      final boolean canTerminate,
      final boolean canMemmap,
      final boolean stackAvailable,
      final boolean canValidMemory,
      final boolean canHalt,
      final boolean haltBeforeCommunicating,
      final boolean canMultithread,
      final boolean canSoftwareBreakpoints,
      final int breakpointCounter,
      final int pageSize,
      final List<DebuggerException> exceptions,
      final boolean canBreakOnModuleLoad,
      final boolean canBreakOnModuleUnload,
      final boolean canTraceCount) {
    Preconditions.checkArgument(breakpointCounter > 0,
        "IE01036: Breakpoint counter argument must be positive");

    this.canDetach = canDetach;
    this.canAttach = canAttach;
    this.canTerminate = canTerminate;
    this.canMemmap = canMemmap;
    this.stackAvailable = stackAvailable;
    this.canValidMemory = canValidMemory;
    this.canMultithread = canMultithread;
    this.canHalt = canHalt;
    this.haltBeforeCommunicating = haltBeforeCommunicating;
    this.canSoftwareBreakpoints = canSoftwareBreakpoints;
    this.breakpointCounter = breakpointCounter;
    this.pageSize = pageSize;
    this.exceptions = new ArrayList<DebuggerException>(exceptions);
    this.canBreakOnModuleLoad = canBreakOnModuleLoad;
    this.canBreakOnModuleUnload = canBreakOnModuleUnload;
    this.canTraceCount = canTraceCount;
  }

  /**
   * Returns a deep copy of the given CDebuggerOptions instance
   *
   * @param options The instance to be copied
   * @return A copy of the given instance
   */
  public static DebuggerOptions newInstance(final DebuggerOptions options) {
    return new DebuggerOptions(options.canDetach,
        options.canAttach,
        options.canTerminate,
        options.canMemmap,
        options.stackAvailable,
        options.canValidMemory,
        options.canHalt,
        options.haltBeforeCommunicating,
        options.canMultithread,
        options.canSoftwareBreakpoints,
        options.breakpointCounter,
        options.pageSize,
        new ArrayList<DebuggerException>(options.exceptions),
        options.canBreakOnModuleLoad,
        options.canBreakOnModuleUnload,
        options.canTraceCount);
  }

  /**
   * This builder is used to construct {@link DebuggerOptions debugger options}. It uses the default
   * values which have been used in the past to not destroy assumptions in debug clients about these
   * defaults.
   */
  public static class DebuggerOptionsBuilder {
    boolean canDetach = true;
    boolean canAttach = true;
    boolean canTerminate = true;
    boolean canMemmap = true;
    boolean stackAvailable = true;
    boolean canValidateMemory = true;
    boolean canHalt = false;
    boolean canHaltBeforeCommunicating = false;
    boolean canMultithread = true;
    boolean canSoftwareBreakpoints = true;
    int maximumBreakpoints = Integer.MAX_VALUE;
    int pageSize = 0;
    List<DebuggerException> exceptions = new ArrayList<>();
    boolean canBreakOnModuleLoad = false;
    boolean canBreakOnModuleUnload = false;
    boolean canTraceCount = true;

    public DebuggerOptionsBuilder canDetach(boolean value) {
      this.canDetach = value;
      return this;
    }

    public DebuggerOptionsBuilder canAttach(boolean value) {
      this.canAttach = value;
      return this;
    }

    public DebuggerOptionsBuilder canTerminate(boolean value) {
      this.canTerminate = value;
      return this;
    }

    public DebuggerOptionsBuilder canMemmap(boolean value) {
      this.canMemmap = value;
      return this;
    }

    public DebuggerOptionsBuilder stackAvailable(boolean value) {
      this.stackAvailable = value;
      return this;
    }

    public DebuggerOptionsBuilder canValidateMemory(boolean value) {
      this.canValidateMemory = value;
      return this;
    }

    public DebuggerOptionsBuilder canHalt(boolean value) {
      this.canHalt = value;
      return this;
    }

    public DebuggerOptionsBuilder canHaltBeforeCommunicating(boolean value) {
      this.canHaltBeforeCommunicating = value;
      return this;
    }

    public DebuggerOptionsBuilder canMultithread(boolean value) {
      this.canMultithread = value;
      return this;
    }

    public DebuggerOptionsBuilder canSoftwareBreakpoints(boolean value) {
      this.canSoftwareBreakpoints = value;
      return this;
    }

    public DebuggerOptionsBuilder breakpointCounter(int value) {
      this.maximumBreakpoints = value;
      return this;
    }

    public DebuggerOptionsBuilder pageSize(int value) {
      this.pageSize = value;
      return this;
    }

    public DebuggerOptionsBuilder addException(DebuggerException value) {
      this.exceptions.add(value);
      return this;
    }

    public DebuggerOptionsBuilder canBreakOnModuleLoad(boolean value) {
      this.canBreakOnModuleLoad = value;
      return this;
    }

    public DebuggerOptionsBuilder canBreakOnModuleUnload(boolean value) {
      this.canBreakOnModuleUnload = value;
      return this;
    }

    public DebuggerOptionsBuilder canTraceCounts(boolean value) {
      this.canTraceCount = value;
      return this;
    }

    public DebuggerOptions build() {
      return new DebuggerOptions(canDetach,
          canAttach,
          canTerminate,
          canMemmap,
          stackAvailable,
          canValidateMemory,
          canHalt,
          canHaltBeforeCommunicating,
          canMultithread,
          canSoftwareBreakpoints,
          maximumBreakpoints,
          pageSize,
          exceptions,
          canBreakOnModuleLoad,
          canBreakOnModuleUnload,
          canTraceCount);
    }
  }

  /**
   * Specifies whether the debugger is able to break when a module is loaded.
   *
   * @return True if the debugger supports break on module load events.
   */
  public boolean canBreakOnModuleLoad() {
    return canBreakOnModuleLoad;
  }

  /**
   * Specifies whether the debugger is able to break when a module is unloaded.
   *
   * @return True if the debugger supports break on module unload events.
   */
  public boolean canBreakOnModuleUnload() {
    return canBreakOnModuleUnload;
  }

  /**
   * Returns whether the debug client can detach from the debugged process.
   *
   * @return True if the debugger supports detaching from the debugger process.
   */
  public boolean canDetach() {
    return canDetach;
  }

  /**
   * Returns whether the debug client can attach to a running process.
   *
   * @return True, if the debugger supports attaching to a process.
   */
  public boolean canAttach() {
    return canAttach;
  }

  /**
   * Returns whether the debug client can halt the process which is being debugged.
   *
   * @return True, if the debugger supports halting the debugged process.
   */
  public boolean canHalt() {
    return canHalt;
  }

  /**
   * Returns whether the debug client can create a memory map of the target process.
   *
   * @return True, if the debugger can create a memory map of the debug process.
   */
  public boolean canMemmap() {
    return canMemmap;
  }

  /**
   * Returns whether the target process is using multiple threads.
   *
   * @return True, if the target process uses multiple threads.
   */
  public boolean canMultithread() {
    return canMultithread;
  }

  /**
   * Returns whether the debug client can set software breakpoints.
   *
   * @return True, if the debug client supports software breakpoints.
   */
  public boolean canSoftwareBreakpoint() {
    return canSoftwareBreakpoints;
  }

  /**
   * Returns whether the debug client can terminate the target process.
   *
   * @return True, if the debug client can terminate the target process.
   */
  public boolean canTerminate() {
    return canTerminate;
  }

  /**
   * Returns if the debug client supports echo breakpoints with a counter larger 1.
   *
   * @return True if the debug client supports echo breakpoints counts larger 1.
   */
  public boolean canTraceCount() {
    return canTraceCount;
  }

  /**
   * Returns whether the debug client can validate memory in the target process.
   *
   * @return True, if the debug client can validate memory in the target process.
   */
  public boolean canValidMemory() {
    return canValidMemory;
  }

  /**
   * Returns the maximum number of breakpoints that can be set by the debug client in the target
   * process.
   *
   * @return The maximum number of available breakpoints.
   */
  public int getBreakpointCounter() {
    return breakpointCounter;
  }

  /**
   * Get the list of supported exceptions Each exception name maps to a platform exception type
   *
   * @return The list of exceptions
   */
  public Collection<DebuggerException> getExceptions() {
    return exceptions;
  }

  /**
   * Returns the page size of the target process.
   *
   * @return The page size of the target process.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Returns whether the debug client supports an explicit stack view.
   *
   * @return True, if it does. False, if it does not.
   */
  public boolean isStackAvailable() {
    return stackAvailable;
  }

  /**
   * Returns whether the debug client must halt before the debugged process before any further
   * communication.
   *
   * @return True, if the debug client must halt before any further communication.
   */
  public boolean mustHaltBeforeCommunicating() {
    return haltBeforeCommunicating;
  }

  /**
   * Sets list of exception settings
   *
   * @param exceptions The list of exceptions
   */
  public void setExceptions(final Collection<DebuggerException> exceptions) {
    this.exceptions = exceptions;
  }
}
