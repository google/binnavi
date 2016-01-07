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
package com.google.security.zynamics.binnavi.debug.connection;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.debug.connection.helpers.PacketIdGenerator;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.ClientReader;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugConnection;
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugEventListener;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.CancelTargetSelectionCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.DebugCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.DetachCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.HaltCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.MemoryRangeCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RemoveBreakpointsCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestFilesCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestFilesPathCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestMemoryCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestMemoryMapCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestProcessesCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.RequestRegistersCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.ResumeCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.ResumeThreadCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SearchCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SelectFileCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SelectProcessCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SetBreakpointCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SetBreakpointConditionCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SetDebuggerEventSettingsCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SetExceptionSettingsCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SetRegisterCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SingleStepCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.SuspendThreadCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.TerminateCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.commands.WriteMemoryCommand;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DebuggerReply;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Base class for all concrete connection classes.
 */
public abstract class AbstractConnection implements DebugConnection {
  /**
   * Packet ID generator for packets sent by the current connection.
   */
  private final static PacketIdGenerator packetIdGenerator = new PacketIdGenerator();

  /**
   * Worker thread that receives and handles data from the debug client.
   */
  private ReceiveWorker receiverThread = null;

  /**
   * Takes events out of the event queue and notifies the listeners about them.
   */
  private final PipeFetcher pipeFetcherThread;

  /**
   * Event queue that is used to communicate between the worker thread and the fetcher thread.
   */
  private final LinkedBlockingQueue<DebuggerReply> eventQueue = new LinkedBlockingQueue<>();

  /**
   * Thread object for the pipe fetcher thread.
   */
  private Thread fetcherThread;

  /**
   * Thread object for the worker thread.
   */
  private Thread workerThread;

  /**
   * Creates a new abstract connection object.
   */
  public AbstractConnection() {
    // Create the thread that fetches debug events from the queue
    // and notifies all objects that want to be notified about these events.
    pipeFetcherThread = new PipeFetcher(eventQueue);
  }

  /**
   * Generates the next available packet ID.
   *
   * @return The next available packet ID.
   */
  private static int getMessageId() {
    return packetIdGenerator.next();
  }

  /**
   * Sends a debug message to the debug client.
   *
   * @param message The message to send to the debug client.
   *
   * @return The message ID of the message.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  protected abstract int sendPacket(final DebugCommand message) throws IOException;

  /**
   * Starts the connection.
   *
   * @param reader Reader object that can read data from the debug client.
   */
  protected void startConnection(final ClientReader reader) {
    NaviLogger.info("Starting the connection to the debug client");
    Preconditions.checkNotNull(reader, "IE00738: Reader can not be null");
    fetcherThread = new Thread(pipeFetcherThread,
        "Pipe Fetcher (" + Thread.currentThread().getStackTrace()[6] + ")");
    pipeFetcherThread.reset();
    fetcherThread.start();
    receiverThread = new ReceiveWorker(reader, eventQueue);
    workerThread = new Thread(receiverThread, "Receive Worker");
    workerThread.start();
  }

  /**
   * Adds an event listener that wants to be notified about incoming debug events.
   *
   * @param listener The listener to add.
   */
  @Override
  public void addEventListener(final DebugEventListener listener) {
    pipeFetcherThread.addEventListener(listener);
  }

  /**
   * Adds an event listener that wants to be notified about incoming debug events and wants to
   * modify the debugger protocol state.
   *
   * @param listener The listener to add.
   */
  public void addProtocolEventListener(final DebugEventListener listener) {
    pipeFetcherThread.addProtocolEventListener(listener);
  }

  /**
   * Removes an event listener from the list of notified listeners.
   *
   * @param listener The listener to remove.
   */
  @Override
  public void removeEventListener(final DebugEventListener listener) {
    pipeFetcherThread.removeEventListener(listener);
  }

  /**
   * Removes an event listener from the list of protocol event listeners.
   *
   * @param listener The listener to remove.
   */
  public void removeProtocolEventListener(final DebugEventListener listener) {
    pipeFetcherThread.removeProtocolEventListener(listener);
  }

  /**
   * Sets a number of breakpoints.
   *
   * @param addresses Addresses of the breakpoints.
   * @param type Type of the breakpoint.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendBreakpointsMessage(final Set<RelocatedAddress> addresses,
      final BreakpointType type) throws IOException {
    Preconditions.checkArgument(addresses.size() != 0, "ERROR: addresses can not be empty");
    NaviLogger.info("Sending \"Set Breakpoint\" message to the debug client");
    return sendPacket(new SetBreakpointCommand(getMessageId(), addresses, type));
  }

  @Override
  public int sendCancelTargetSelection() throws IOException {
    NaviLogger.info("Sending \"Cancel Target Selection\" message to the debug client");
    return sendPacket(new CancelTargetSelectionCommand(getMessageId()));
  }

  @Override
  public int sendDebuggerEventSettingsMessage(final DebuggerEventSettings eventSettings)
      throws IOException {
    NaviLogger.info("Sending \"Set Debugger Event Settings\" message to the debug client");
    return sendPacket(new SetDebuggerEventSettingsCommand(getMessageId(), eventSettings));
  }

  /**
   * Sends a Detach message to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendDetachMessage() throws IOException {
    NaviLogger.info("Sending \"Detach\" message to the debug client");
    return sendPacket(new DetachCommand(getMessageId()));
  }

  /**
   * Sends a a message specifying the exceptions which need special handling by the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if the sending the message failed.
   */
  @Override
  public int sendExceptionSettingsMessage(final Collection<DebuggerException> exceptions)
      throws IOException {
    NaviLogger.info("Sending \"Exception Settings\" message to the debug client");
    return sendPacket(new SetExceptionSettingsCommand(getMessageId(), exceptions));
  }

  /**
   * Sends a halt request to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if the sending the message failed.
   */
  @Override
  public int sendHaltMessage() throws IOException {
    NaviLogger.info("Sending \"Halt\" message to the debug client");
    return sendPacket(new HaltCommand(getMessageId()));
  }

  /**
   * Sends a memory map command to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public void sendMemoryMapMessage() throws IOException {
    NaviLogger.info("Sending \"Memory Map\" message to the debug client");
    sendPacket(new RequestMemoryMapCommand(getMessageId()));
  }

  /**
   * Sends a memory range message to the debug client.
   *
   * @param address The address that should be included in the memory range.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendMemoryRangeMessage(final IAddress address) throws IOException {
    NaviLogger.info("Sending \"Memory Range\" message to the debug client");
    return sendPacket(new MemoryRangeCommand(getMessageId(), address));
  }

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
  @Override
  public int sendReadMemoryMessage(final IAddress address, final int length) throws IOException {
    NaviLogger.info(String.format("Sending \"Read Memory %s/%X\" message to the debug client",
        address.toHexString(), length));
    return sendPacket(new RequestMemoryCommand(getMessageId(), address, new CAddress(length)));
  }

  /**
   * Sends a request to read the register values to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendRegisterRequestMessage() throws IOException {
    NaviLogger.info("Sending \"Get Registers\" message to the debug client");
    return sendPacket(new RequestRegistersCommand(getMessageId()));
  }

  @Override
  public int sendRemoveBreakpointsMessage(final Set<RelocatedAddress> addresses,
      final BreakpointType type) throws IOException {
    NaviLogger.info("Sending \"Remove Breakpoint\" message to the debug client");
    return sendPacket(new RemoveBreakpointsCommand(getMessageId(), addresses, type));
  }

  @Override
  public int sendRequestFileSystem() throws IOException {
    NaviLogger.info("Sending \"Request File System\" message to the debug client");
    return sendPacket(new RequestFilesCommand(getMessageId()));
  }

  @Override
  public void sendRequestFileSystem(final String path) throws IOException {
    NaviLogger.info("Sending \"Request File System + Path\" message to the debug client");
    sendPacket(new RequestFilesPathCommand(getMessageId(), path));
  }

  @Override
  public int sendRequestProcessList() throws IOException {
    NaviLogger.info("Sending \"Request Process List\" message to the debug client");
    return sendPacket(new RequestProcessesCommand(getMessageId()));
  }

  /**
   * Sends a Resume message to the debug client.
   *
   * @return The packet ID of the message that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendResumeMessage() throws IOException {
    NaviLogger.info("Sending \"Resume\" message to the debug client");
    return sendPacket(new ResumeCommand(getMessageId()));
  }

  @Override
  public int sendResumeThreadMessage(final long tid) throws IOException {
    NaviLogger.info("Sending \"Resume Thread\" message to the debug client");
    return sendPacket(new ResumeThreadCommand(getMessageId(), tid));
  }

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
  @Override
  public int sendSearchMessage(final IAddress start, final int size, final byte[] data)
      throws IOException {
    NaviLogger.info("Sending \"Search Memory\" message to the debug client");
    return sendPacket(new SearchCommand(getMessageId(), start, new CAddress(size), data));
  }

  @Override
  public void sendSelectFileMessage(final String name) throws IOException {
    NaviLogger.info("Sending \"Select File\" message to the debug client");
    sendPacket(new SelectFileCommand(getMessageId(), name));
  }

  @Override
  public int sendSelectProcessMessage(final int pid) throws IOException {
    NaviLogger.info("Sending \"Select Process\" message to the debug client");
    return sendPacket(new SelectProcessCommand(getMessageId(), pid));
  }

  @Override
  public int sendSetBreakpointConditionMessage(final RelocatedAddress address,
      final Condition condition) throws IOException {
    NaviLogger.info("Sending \"Set Breakpoint Condition\" message to the debug client");
    return sendPacket(new SetBreakpointConditionCommand(getMessageId(), address, condition));
  }

  /**
   * Sends a set register value request to the debug client.
   *
   * @param index The index of the register as it appears in the Info string.
   * @param value The new value of the register.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendSetRegisterMessage(final long tid, final int index, final BigInteger value)
      throws IOException {
    NaviLogger.info("Sending \"Set Register\" message to the debug client");
    return sendPacket(new SetRegisterCommand(getMessageId(), tid, index, new CAddress(value)));
  }

  /**
   * Sends a single step request to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendSingleStepMessage() throws IOException {
    NaviLogger.info("Sending \"Single Step\" message to the debug client");
    return sendPacket(new SingleStepCommand(getMessageId()));
  }

  @Override
  public int sendSuspendThreadMessage(final long tid) throws IOException {
    NaviLogger.info("Sending \"Suspend Thread\" message to the debug client");
    return sendPacket(new SuspendThreadCommand(getMessageId(), tid));
  }

  /**
   * Sends a termination request to the debug client.
   *
   * @return The packet ID of the packet that was sent to the debug client.
   *
   * @throws IOException Thrown if sending the message failed.
   */
  @Override
  public int sendTerminateMessage() throws IOException {
    NaviLogger.info("Sending \"Terminate\" message to the debug client");
    return sendPacket(new TerminateCommand(getMessageId()));
  }

  @Override
  public int sendWriteMemoryMessage(final IAddress address, final byte[] data) throws IOException {
    NaviLogger.info("Sending \"Write Memory\" message to the debug client");
    return sendPacket(new WriteMemoryCommand(getMessageId(), address, data));
  }

  /**
   * Shuts down debug connection.
   */
  @Override
  public void shutdown() {
    NaviLogger.info("Shutting down the connection to the debug client");

    if (receiverThread != null) {
      receiverThread.shutdown();
      workerThread.interrupt();
    }

    if (pipeFetcherThread != null) {
      pipeFetcherThread.shutdown();
      fetcherThread.interrupt();
    }
  }
}
