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
package com.google.security.zynamics.binnavi.debug.connection.interfaces;

import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Set;

/**
 * Connection interface that must be implemented by all connection classes that handle communication
 * with the debug clients.
 */
public interface DebugConnection {
  /**
   * Adds event listeners that want to be notified about incoming debug events.
   *
   * @param listener The listener to add.
   */
  void addEventListener(final DebugEventListener listener);

  /**
   * Removes an event listener from the debug connection.
   *
   * @param listener The listener to be removed from the debug connection.
   */
  void removeEventListener(DebugEventListener listener);

  /**
   * Sets a number of breakpoints.
   *
   * @param addresses Addresses of the breakpoints.
   * @param debugBreakPointType Type of the breakpoint.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendBreakpointsMessage(
      final Set<RelocatedAddress> addresses, final BreakpointType debugBreakPointType)
      throws IOException;

  /**
   * Sends a message to the debug client to signal that target selection was canceled by the user.
   *
   * @return The packet ID of the message that was sent to the debug client.
   * @throws IOException Thrown if sending the message failed.
   */
  int sendCancelTargetSelection() throws IOException;

  /**
   * Sends a message to the debug client to specify the debugger behavior when certain debug events
   * are encountered.
   *
   * @param eventSettings The debug event settings.
   */
  int sendDebuggerEventSettingsMessage(DebuggerEventSettings eventSettings) throws IOException;

  /**
   * Sends a Detach message to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendDetachMessage() throws IOException;

  /**
   * Sends a list of exceptions to be ignored by the debug client.
   *
   * @param exceptions The list of exceptions to be ignored.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendExceptionSettingsMessage(final Collection<DebuggerException> exceptions) throws IOException;

  /**
   * Sends a Halt message to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendHaltMessage() throws IOException;

  /**
   * Sends a memory map command to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  void sendMemoryMapMessage() throws IOException;

  /**
   * Sends a memory range message to the debug client.
   *
   * @param address The address that should be included in the memory range.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendMemoryRangeMessage(final IAddress address) throws IOException;

  /**
   * Sends a read memory message to the debug client.
   *
   * @param address The start address from where the memory is read.
   * @param length The number of bytes to read.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendReadMemoryMessage(IAddress address, int length) throws IOException;

  /**
   * Sends a request to read the register values to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendRegisterRequestMessage() throws IOException;

  /**
   * Sends a request to remove breakpoints to the debug client.
   *
   * @param addresses The addresses of the breakpoints.
   * @param type The type of the breakpoints.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendRemoveBreakpointsMessage(Set<RelocatedAddress> addresses, BreakpointType type)
      throws IOException;

  /**
   * Sends a message to request global file system information to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendRequestFileSystem() throws IOException;

  /**
   * Sends a message to request path-specific file system information to the debug client.
   *
   * @param path The path for which information is requested.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  void sendRequestFileSystem(String path) throws IOException;

  /**
   * Sends a message to the debug client requesting process information.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendRequestProcessList() throws IOException;

  /**
   * Sends a Resume message to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendResumeMessage() throws IOException;

  /**
   * Sends a Resume Thread message to the debug client.
   *
   * @param tid The thread ID of the thread to be resumed.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendResumeThreadMessage(long tid) throws IOException;

  /**
   * Sends a search request to the debug client.
   *
   * @param start The start of the memory range that is searched through.
   * @param size Number of bytes to search through.
   * @param data The search data.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSearchMessage(IAddress start, int size, byte[] data) throws IOException;

  /**
   * Sends a message to the debug client telling it to select a given file as the target file.
   *
   * @param name Full path to the target executable.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  void sendSelectFileMessage(String name) throws IOException;

  /**
   * Sends a message to the debug client telling it to select a given process as the target process.
   *
   * @param pid Process ID of the process to select.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSelectProcessMessage(int pid) throws IOException;

  /**
   * Sends a message to the debug client to set a breakpoint condition on an existing breakpoint.
   *
   * @param address The address of the breakpoint.
   * @param condition The new condition of the breakpoint. This can be null.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSetBreakpointConditionMessage(
      final RelocatedAddress address, final Condition condition) throws IOException;

  /**
   * Sends a set register value request to the debug client.
   *
   * @param tid Thread ID of the thread whose register is changed.
   * @param index The index of the register as it appears in the Info string.
   * @param value The new value of the register.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSetRegisterMessage(long tid, int index, BigInteger value) throws IOException;

  /**
   * Sends a single step request to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSingleStepMessage() throws IOException;

  /**
   * Sends a Suspend Thread message to the debug client.
   *
   * @param tid The thread ID of the thread to be suspended.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendSuspendThreadMessage(long tid) throws IOException;

  /**
   * Sends a termination request to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendTerminateMessage() throws IOException;

  /**
   * Sends a Write Memory request to the debug client.
   *
   * @param address Start address of the Write Memory operation.
   * @param data Data to write.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  int sendWriteMemoryMessage(final IAddress address, final byte[] data) throws IOException;

  /**
   * Shuts down the debug connection.
   */
  void shutdown();

  /**
   * Starts the connection to the debug client.
   *
   * @throws ConnectException Thrown if connecting to the debug client failed.
   */
  void startConnection() throws ConnectException;
}
