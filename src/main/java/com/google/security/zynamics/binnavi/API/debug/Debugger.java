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
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.APIHelpers.ApiObject;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AttachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointConditionSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.CancelTargetSelectionReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerClosedUnexpectedlyReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ExceptionOccurredReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.HaltReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListFilesReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ListProcessesReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleLoadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ModuleUnloadedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessStartReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.QueryDebuggerEventSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RegistersReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RequestTargetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ResumeThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SearchReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SelectFileReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SelectProcessReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetDebuggerEventSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetExceptionSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetRegisterReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointHitReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.StepBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SuspendThreadReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TerminateReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ThreadCreatedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ValidateMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.WriteMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.general.ListenerProvider;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// / Used to debug a target process.
/**
 * Debugger object that can be used to debug modules or projects.
 */
public final class Debugger implements ApiObject<IDebugger> {
  /**
   * The wrapped internal debugger object.
   */
  private final IDebugger m_debugger;

  /**
   * The simulated target process.
   */
  private final Process m_process;

  /**
   * Manages the bookmarks set by the debugger.
   */
  private final BookmarkManager m_bookmarkManager;

  /**
   * Managed the breakpoints set by the debugger.
   */
  private final BreakpointManager m_breakpointManager;

  /**
   * Keeps the API debugger object synchronized with the internal debugger object.
   */
  private final IDebugEventListener m_listener = new InternalDebugEventListener();

  /**
   * Listeners that are notified about changes in the debugger.
   */
  private final ListenerProvider<IDebuggerListener> m_listeners =
      new ListenerProvider<IDebuggerListener>();

  // / @cond INTERNAL
  /**
   * Creates a new API debugger object.
   *
   * @param debugger The internal debugger object to wrap.
   */
  // / @endcond
  public Debugger(final IDebugger debugger) {
    m_debugger = debugger;

    m_process = new Process(m_debugger.getProcessManager());
    m_bookmarkManager = new BookmarkManager(m_debugger.getBookmarkManager());
    m_breakpointManager = new BreakpointManager(m_debugger.getBreakpointManager());

    m_debugger.addListener(m_listener);
  }

  @Override
  public IDebugger getNative() {
    return m_debugger;
  }

  // ! Adds a debugger listener.
  /**
   * Adds a debugger listener object that is notified about incoming debug messages and changes in
   * the debugger state.
   *
   *  Note that this function adds very low-level listeners. If possible you should use higher-level
   * listeners like IThreadListener or IProcessListener.
   *
   * @param listener The listener object to add.
   */
  public void addListener(final IDebuggerListener listener) {
    m_listeners.addListener(listener);
  }

  /**
   * Cancels the target selection of the debugger and closes the connection.
   *
   * @throws DebugException Thrown if an error occurred during cancellation.
   */
  public void cancelTargetSelection() throws DebugException {
    try {
      m_debugger.cancelTargetSelection();
      m_debugger.setTerminated();
    } catch (final DebugExceptionWrapper exception) {
      throw new DebugException(exception);
    }
  }

  // ! Connects to the target process.
  /**
   * Connects to the target process.
   *
   * @throws DebugException Thrown if the debugger could not connect to the target process.
   */
  public void connect() throws DebugException {
    try {
      m_debugger.connect();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Detaches from the target process.
  /**
   * Detaches from the target process.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void detach() throws DebugException {
    try {
      m_debugger.detach();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! The bookmark manager of the debugger.
  /**
   * Returns the bookmark manager of the debugger. This manager can be used to set and remove memory
   * bookmarks.
   *
   * @return The bookmark manager of the debugger.
   */
  public BookmarkManager getBookmarkManager() {
    return m_bookmarkManager;
  }

  // ! The breakpoint manager of the debugger.
  /**
   * Returns the breakpoint manager of the debugger. This manager can be used to set and remove
   * breakpoints.
   *
   * @return The breakpoint manager of the debugger.
   */
  public BreakpointManager getBreakpointManager() {
    return m_breakpointManager;
  }

  // ! The process debugged by the debugger.
  /**
   * Returns the target process debugged by the debugger.
   *
   * @return The target process of the debugger.
   */
  public Process getProcess() {
    return m_process;
  }

  // ! Checks if the debugger is connected.
  /**
   * Returns a flag that indicates whether the debugger is active or not.
   *
   * @return True, if the debugger is debugging a target. False, otherwise.
   */
  public boolean isConnected() {
    return m_debugger.isConnected();
  }

  // ! Reads memory of the target process.
  /**
   * Reads memory of the target process.
   *
   * @param address Start address of the memory read operation.
   * @param size Number of bytes to read.
   *
   * @throws IllegalArgumentException Thrown if the address argument is null.
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void readMemory(final Address address, final int size) throws DebugException {
    try {
      m_debugger.readMemory(new CAddress(address.toLong()), size);
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Reads the registers of all threads.
  /**
   * Reads the current register values of all threads of the target process.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void readRegisters() throws DebugException {
    try {
      m_debugger.readRegisters();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Removes a debugger listener.
  /**
   * Removes a debugger listener from the debugger.
   *
   * @param listener The debugger listener to remove.
   */
  public void removeListener(final IDebuggerListener listener) {
    m_listeners.removeListener(listener);
  }

  /**
   * Request the file system listing from the debug client.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void requestFileSystem() throws DebugException {
    try {
      m_debugger.requestFileSystem();
    } catch (final DebugExceptionWrapper exception) {
      throw new DebugException(exception);
    }
  }

  /**
   * Request the file system listing for the given path.
   *
   * @param path The base directory to be listed.
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void requestFileSystem(final String path) throws DebugException {
    try {
      m_debugger.requestFileSystem(path);
    } catch (final DebugExceptionWrapper exception) {
      throw new DebugException(exception);
    }
  }

  // ! Resumes the active thread thread in the target process.
  /**
   * Executes a resume command in the active thread.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void resume() throws DebugException {
    try {
      m_debugger.resume();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  /**
   * Tells the debug client to select the given file as the target process executable file.
   *
   * @param file The path to the target file.
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void selectFile(final String file) throws DebugException {
    try {
      m_debugger.selectFile(file);
    } catch (final DebugExceptionWrapper exception) {
      throw new DebugException(exception);
    }
  }

  /**
   * Sets the exception handling settings for the debugger.
   *
   * @param exceptions The API exceptions settings collection.
   * @throws DebugException if an error occurred during exception setting.
   */
  public void setExceptionSettings(final Collection<DebuggerDebugException> exceptions)
      throws DebugException {
    final Collection<DebuggerException> nativeExceptions = Collections.emptyList();
    for (final DebuggerDebugException exception : exceptions) {
      nativeExceptions.add(exception.getNative());
    }
    try {
      m_debugger.setExceptionSettings(nativeExceptions);
    } catch (final DebugExceptionWrapper exception) {
      throw new DebugException(exception);
    }
  }

  // ! Single-steps thread in the target process.
  /**
   * Executes a single step operation in the active thread.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void singleStep() throws DebugException {
    try {
      m_debugger.singleStep();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Terminates the target process.
  /**
   * Terminates the target process and the debug client.
   *
   * @throws DebugException Thrown if the debug command could not be executed.
   */
  public void terminate() throws DebugException {
    try {
      m_debugger.terminate();
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Converts memory addresses to file addresses.
  /**
   * Converts a memory-relocated address to the same address in the unrelocated module.
   *
   * @param module The module the relocated address belongs to.
   * @param address The memory-relocated address to convert.
   *
   * @return The converted file address.
   */
  public Address toFilebase(final Module module, final Address address) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    return new Address(m_debugger
        .memoryToFile(module.getNative(), new RelocatedAddress(new CAddress(address.toLong())))
        .getAddress().toBigInteger());
  }

  // ! Converts file addresses to memory addresses.
  /**
   * Converts a file address to the same address in the relocated module.
   *
   * @param module The module the file address address belongs to.
   * @param address The file address to convert.
   *
   * @return The converted memory-relocated address.
   */
  public Address toImagebase(final Module module, final Address address) {
    Preconditions.checkNotNull(module, "Error: Module argument can not be null");
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");

    return new Address(m_debugger
        .fileToMemory(module.getNative(), new UnrelocatedAddress(new CAddress(address.toLong())))
        .getAddress().toBigInteger());
  }

  // ! Printable representation of the debugger.
  /**
   * Returns a string representation of the debugger.
   *
   * @return A string representation of the debugger.
   */
  @Override
  public String toString() {
    return String.format("Debugger '%s'", m_debugger.getPrintableString());
  }

  // ! Writes target process memory.
  /**
   * Writes to the memory of the target process.
   *
   * @param address Start address of the memory write operation.
   * @param data Data to be written to the target memory.
   *
   * @throws DebugException Thrown if the message could not be sent to the debug client.
   */
  public void writeMemory(final Address address, final byte[] data) throws DebugException {
    Preconditions.checkNotNull(address, "Error: Address argument can not be null");
    Preconditions.checkNotNull(data, "Error: Data argument can not be null");

    try {
      m_debugger.writeMemory(new CAddress(address.toLong()), data);
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  // ! Writes a register value.
  /**
   * Changes the value of a register in the given thread.
   *
   * @param tid Thread ID of the thread whose register value is changed.
   * @param register Name of the register to change.
   * @param value The new value of the register.
   *
   * @throws DebugException Thrown if the message could not be sent to the debug client.
   */
  public void writeRegister(final long tid, final String register, final long value)
      throws DebugException {
    Preconditions.checkNotNull(register, "Error: Register argument can not be null");
    Preconditions.checkNotNull(m_debugger.getProcessManager().getTargetInformation(),
        "Error: Target information string has not yet been received");

    final List<RegisterDescription> registers =
        m_debugger.getProcessManager().getTargetInformation().getRegisters();

    int index = 0;

    for (final RegisterDescription description : registers) {
      if (description.getName().equalsIgnoreCase(register)) {
        if (!description.isEditable()) {
          throw new IllegalArgumentException("Error: Selected register can not be edited");
        }
        break;
      }
      index++;
    }

    if (index == registers.size()) {
      throw new IllegalArgumentException("Error: Unknown register name");
    }

    try {
      m_debugger.setRegister(tid, index, BigInteger.valueOf(value));
    } catch (final DebugExceptionWrapper e) {
      throw new DebugException(e);
    }
  }

  /**
   * Keeps the API debugger object synchronized with the internal debugger object.
   */
  private class InternalDebugEventListener implements IDebugEventListener {
    @Override
    public void debugException(final DebugExceptionWrapper debugException) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debugException(debugException);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void debuggerClosed(final int errorCode) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debuggerClosed(errorCode);
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final AttachReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debuggerAttach(new DebuggerAttachReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final AuthenticationFailedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.authenticationFailed(new DebuggerAuthenticationFailedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final BreakpointConditionSetReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.breakpointConditionSet(new DebuggerBreakpointConditionSetReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final BreakpointHitReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.breakpointHit(new DebuggerBreakpointHitReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final BreakpointSetReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.breakpointSet(new DebuggerBreakpointSetReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final BreakpointsRemovedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.breakpointsRemoved(new DebuggerBreakpointsRemovedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final CancelTargetSelectionReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.cancelTargetSelection(new DebuggerCancelTargetSelectionReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final DebuggerClosedUnexpectedlyReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debuggerClosedUnexpectedly(new DebuggerDebuggerClosedUnexpectedlyReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final DetachReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debuggerDetached(new DebuggerDetachReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointHitReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.echoBreakpointHit(new DebuggerEchoBreakpointHitReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointSetReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.echoBreakpointSet(new DebuggerEchoBreakpointSetReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointsRemovedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.echoBreakpointsRemoved(new DebuggerEchoBreakpointsRemovedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ExceptionOccurredReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.exceptionOccurred(new DebuggerExceptionOccurredReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final HaltReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.debuggerHalt(new DebuggerHaltReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ListFilesReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final ListProcessesReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final MemoryMapReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.memoryMap(new DebuggerMemoryMapReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ModuleLoadedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.moduleLoaded(new DebuggerModuleLoadedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ModuleUnloadedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.moduleUnloaded(new DebuggerModuleUnloadedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ProcessClosedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.processClosed(new DebuggerProcessClosedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ProcessStartReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.processStart(new DebuggerProcessStartReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final QueryDebuggerEventSettingsReply reply) {
      // TODO (timkornau):
      try {
        // Send standard debugger events settings to client;
        // otherwise we would need to obtain the debug target in this class
        // in order to load the debugger event settings from the database.
        m_debugger.setDebuggerEventSettings(new DebuggerEventSettings(false, false));
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }

    @Override
    public void receivedReply(final ReadMemoryReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.readMemory(new DebuggerReadMemoryReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final RegistersReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.registersReply(new DebuggerRegistersReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final RequestTargetReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.requestTarget(new DebuggerRequestTargetReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ResumeReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.processResumed(new DebuggerResumeReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ResumeThreadReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.threadResumed(new DebuggerResumeThreadReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final SearchReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final SelectFileReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final SelectProcessReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final SetDebuggerEventSettingsReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final SetExceptionSettingsReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.setExceptionSettings(new DebuggerSetExceptionSettingsReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final SetRegisterReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.setRegister(new DebuggerSetRegisterReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final SingleStepReply reply) {
      Preconditions.checkNotNull(reply, "Error: reply argument can not be null");
      Preconditions.checkNotNull(reply.getAddress(),
          "Error: reply.getAddress() argument can not be null");
      Preconditions.checkNotNull(reply.getAddress().getAddress(),
          "Error: reply.getAddress().getAddress() argument can not be null");
      Preconditions.checkNotNull(reply.getRegisterValues(),
          "Error: reply.getRegisterValues() argument can not be null");

      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.singleStep(new DebuggerSingleStepReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final StepBreakpointHitReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final StepBreakpointSetReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final StepBreakpointsRemovedReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final SuspendThreadReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.threadSuspended(new DebuggerSuspendThreadReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final TargetInformationReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.targetInformation(new DebuggerTargetInformationReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final TerminateReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.terminated(new DebuggerTerminateReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ThreadClosedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.threadClosed(new DebuggerThreadClosedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ThreadCreatedReply reply) {
      for (final IDebuggerListener listener : m_listeners) {
        try {
          listener.threadCreated(new DebuggerThreadCreatedReply(reply));
        } catch (final Exception exception) {
          CUtilityFunctions.logException(exception);
        }
      }
    }

    @Override
    public void receivedReply(final ValidateMemoryReply reply) {
      // This should not be passed to the API
    }

    @Override
    public void receivedReply(final WriteMemoryReply reply) {
      // This should not be passed to the API
    }
  }
}
