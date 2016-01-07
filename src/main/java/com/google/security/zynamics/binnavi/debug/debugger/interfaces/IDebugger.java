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
package com.google.security.zynamics.binnavi.debug.debugger.interfaces;

import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Interface that represents debuggers.
 */
public interface IDebugger {
  /**
   * Adds a new listener object that is notified about debug events.
   *
   * @param listener The listener object o add.
   */
  void addListener(IDebugEventListener listener);

  /**
   * Sends a message to the debug client to cancel the active target selection process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void cancelTargetSelection() throws DebugExceptionWrapper;

  /**
   * Determines whether the debugger can debug a given module.
   *
   * @param module The module to check.
   *
   * @return True, if the module can be debugged. False, otherwise.
   */
  boolean canDebug(INaviModule module);

  /**
   * Closes the connection to the debug client.
   */
  void close();

  /**
   * Connects to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if connecting to the debug client failed.
   */
  void connect() throws DebugExceptionWrapper;

  /**
   * Tells the debug client to detach from the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void detach() throws DebugExceptionWrapper;

  /**
   * Converts a file address to a relocated memory address.
   *
   * @param module The module the address belongs to.
   * @param address The address to convert.
   *
   * @return The relocated address.
   */
  RelocatedAddress fileToMemory(INaviModule module, UnrelocatedAddress address);

  /**
   * Returns the bookmark manager of the debugger.
   *
   * @return The bookmark manager of the debugger.
   */
  BookmarkManager getBookmarkManager();

  /**
   * Returns the breakpoint manager of the debugger.
   *
   * @return The breakpoint manager of the debugger.
   */
  BreakpointManager getBreakpointManager();

  DebugTargetSettings getDebugTargetSettings();

  /**
   * Gets the unique Id of the debugger.
   *
   * @return The debuggers' id
   */
  int getId();

  /**
   * Requests the memory map from the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void getMemoryMap() throws DebugExceptionWrapper;

  /**
   * Returns the memory provider of the debugger.
   *
   * @return The memory provider of the debugger.
   */
  IMemoryProvider getMemoryProvider();

  /**
   * Returns the module for a given memory address.
   *
   * @param memoryAddress The memory address to search for.
   *
   * @return The module the address belongs to.
   */
  INaviModule getModule(RelocatedAddress memoryAddress);

  /**
   * Returns the modules known to the debugger.
   *
   * @return The modules known to the debugger.
   */
  List<INaviModule> getModules();

  /**
   * Returns a printable string that describes the debugger.
   *
   * @return A printable string that describes the debugger.
   */
  String getPrintableString();

  /**
   * Returns the process manager of the debugger.
   *
   * @return The process manager of the debugger.
   */
  ProcessManager getProcessManager();

  /**
   * Tells the debug client to halt the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void halt() throws DebugExceptionWrapper;

  /**
   * Determines whether a connection to the debug client was established.
   *
   * @return True, if the debugger is connected. False, otherwise.
   */
  boolean isConnected();

  /**
   * Converts a relocated address to an unrelocated address.
   *
   * @param address The address to convert.
   *
   * @return The converted address.
   */
  UnrelocatedAddress memoryToFile(RelocatedAddress address);

  /**
   * Convers a relocated address to an unrelocated address.
   *
   * @param module The module the address belongs to.
   * @param address The address to convert.
   *
   * @return The converted address.
   */
  UnrelocatedAddress memoryToFile(INaviModule module, RelocatedAddress address);

  /**
   * Tells the debug client to read memory of the target process.
   *
   * @param address Start address of the target memory.
   * @param size Number of bytes to read.
   *
   * @return Packet ID of the packet sent to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  int readMemory(IAddress address, int size) throws DebugExceptionWrapper;

  /**
   * Tells the debug client to read the registers of the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void readRegisters() throws DebugExceptionWrapper;

  /**
   * Removes breakpoints from the target process.
   *
   * @param breakpoints The breakpoints to remove.
   * @param type The type of the breakpoints.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void removeBreakpoints(Set<BreakpointAddress> breakpoints, BreakpointType type)
      throws DebugExceptionWrapper;

  /**
   * Removes a listener that was previously notified about debug events.
   *
   * @param listener The listener to remove.
   */
  void removeListener(IDebugEventListener listener);

  /**
   * Requests file system information from the debug client.
   *
   * @return The message ID of the message sent to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  int requestFileSystem() throws DebugExceptionWrapper;

  /**
   * Requests file system information for a remote directory from the debug client.
   *
   * @param path The directory for which information is requested.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void requestFileSystem(String path) throws DebugExceptionWrapper;

  /**
   * Requests the memory range surrounding an address in the target memory.
   *
   * @param address The address to search for.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void requestMemoryRange(IAddress address) throws DebugExceptionWrapper;

  /**
   * Requests information about the running processes from the debug client.
   *
   * @return Message ID of the message sent to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  int requestProcessList() throws DebugExceptionWrapper;

  /**
   * Asks the debug client to resume a thread.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void resume() throws DebugExceptionWrapper;

  /**
   * Asks the debug client to resume a thread.
   *
   * @param tid Thread ID of the thread to resume.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void resumeThread(long tid) throws DebugExceptionWrapper;

  /**
   * Searches through the memory of the target process.
   *
   * @param start Start address of the search.
   * @param size Maximum number of bytes to search through.
   * @param data Data to search for.
   * @return Message ID of the message sent to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  int search(IAddress start, int size, byte[] data) throws DebugExceptionWrapper;

  /**
   * Tells the debug client to select a given file as the target file.
   *
   * @param filename Path to the target file.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void selectFile(String filename) throws DebugExceptionWrapper;

  /**
   * Tells the debug client to select a given process as the target process.
   *
   * @param pid Process ID of the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void selectProcess(int pid) throws DebugExceptionWrapper;

  /**
   * Sets the address translator for a given module.
   *
   * @param module The module for which the translator is created.
   * @param fileBase The file base of the module.
   * @param imageBase The relocated image base of the module.
   */
  void setAddressTranslator(INaviModule module, IAddress fileBase, IAddress imageBase);

  /**
   * Tells the debug client to set a condition onto a breakpoint.
   *
   * @param address The breakpoint where the condition is set.
   * @param condition The new condition of the breakpoint. This can be null.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void setBreakPointCondition(BreakpointAddress address, Condition condition)
      throws DebugExceptionWrapper;

  /**
   * Sets a list of breakpoints.
   *
   * @param addresses The addresses of the breakpoints.
   * @param type The type of the breakpoints.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void setBreakPoints(Set<BreakpointAddress> addresses, BreakpointType type) throws DebugExceptionWrapper;

  /**
   * Sets the event handling settings which are used to determine the debugger behavior on certain
   * debug events.
   *
   * @param eventSettings The debugger event settings instance.
   *
   * @throws DebugExceptionWrapper thrown if the message could not be sent to the debug client.
   */
  void setDebuggerEventSettings(DebuggerEventSettings eventSettings) throws DebugExceptionWrapper;

  /**
   * Sets a list of exceptions which need special handling by the debugger (e.g. are passed back to
   * the application).
   *
   * @param exceptions The list of exceptions.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void setExceptionSettings(Collection<DebuggerException> exceptions) throws DebugExceptionWrapper;

  /**
   * Tells the debug client to change the value of a register in the target process.
   *
   * @param tid Thread ID whose register value is changed.
   * @param index Index of the register to change.
   * @param value The new value of the register.
   *
   * @return Message ID of the message sent to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  int setRegister(long tid, int index, BigInteger value) throws DebugExceptionWrapper;

  /**
   * Tells the debugger that the target process terminated.
   */
  void setTerminated();

  /**
   * Tells the debug client to do a single step in the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void singleStep() throws DebugExceptionWrapper;

  /**
   * Asks the debug client to suspend a thread.
   *
   * @param tid Thread ID of the thread to suspend.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void suspendThread(long tid) throws DebugExceptionWrapper;

  /**
   * Tells the debug client to terminate the target process.
   *
   * @throws DebugExceptionWrapper Thrown if the message could not be sent to the debug client.
   */
  void terminate() throws DebugExceptionWrapper;

  /**
   * Writes binary data into the memory of the debugged process.
   *
   * @param address Start address of the overwrite.
   * @param data Data to write.
   *
   * @return ID of the message sent to the debugger.
   *
   * @throws DebugExceptionWrapper Thrown if the debugger could not send the message.
   */
  int writeMemory(IAddress address, byte[] data) throws DebugExceptionWrapper;
}
