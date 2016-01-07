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
import com.google.security.zynamics.binnavi.debug.connection.interfaces.DebugConnection;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IAddressConverter;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugEventListener;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IMemoryProvider;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.BreakpointSynchronizer;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.DebuggerSynchronizer;
import com.google.security.zynamics.binnavi.debug.debugger.synchronizers.ThreadStateSynchronizer;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointManager;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.interfaces.Condition;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerEventSettings;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.DebuggerException;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.models.Bookmarks.memory.BookmarkManager;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract base class that should be extended by all concrete debuggers.
 */
public abstract class AbstractDebugger implements IDebugger {
  /**
   * The debug connection that is used to communicate with the debug client.
   */
  private DebugConnection connection;

  /**
   * Updates the process manager when relevant debug events come in.
   */
  private final DebuggerSynchronizer synchronizer;

  /**
   * The process manager that tries to simulate the target process.
   */
  private final ProcessManager processManager = new ProcessManager();

  /**
   * The breakpoint manager that keeps track of the breakpoints set by the debugger.
   */
  private final BreakpointManager breakpointManager = new BreakpointManager();

  /**
   * The bookmark manager that keeps track of memory bookmarks used with the debugger.
   */
  private final BookmarkManager bookmarkManager = new BookmarkManager();

  /**
   * The address converter objects that are used to convert between file offsets and image offsets
   * of modules.
   */
  private final Map<INaviModule, IAddressConverter> addressConverters = new HashMap<>();

  /**
   * Map that contains the base addresses for all modules.
   */
  private final Map<INaviModule, IAddress> baseAddresses = new HashMap<>();

  /**
   * Synchronizes memory requests with the debugger.
   */
  private final MemorySynchronizer memorySynchronizer;

  /**
   * Updates the breakpoint manager when relevant debug events come in.
   *
   * This is actually used and needs to be kept alive.
   */
  @SuppressWarnings("unused")
  private final BreakpointSynchronizer breakpointSynchronizer = new BreakpointSynchronizer(this);

  /**
   * Updates the thread states when relevant debug events come in.
   *
   * This is actually used and needs to be kept alive.
   */
  @SuppressWarnings("unused")
  private final ThreadStateSynchronizer threadStateSynchronizer = new ThreadStateSynchronizer(this);

  /**
   * Creates a new abstract debugger object.
   */
  protected AbstractDebugger() {
    synchronizer = new DebuggerSynchronizer(this);
    memorySynchronizer = new MemorySynchronizer(this);
  }

  /**
   * Ensure that the debugger is connected.
   *
   * @throws DebugExceptionWrapper Throws if debugger is not connected.
   */
  private void ensureConnection() throws DebugExceptionWrapper {
    if (!isConnected()) {
      throw new DebugExceptionWrapper("Error: Debugger is not connected");
    }
  }

  /**
   * Relocates a bunch of file addresses.
   *
   * @param addresses The addresses to relocate.
   *
   * @return The relocated addresses.
   */
  private Set<RelocatedAddress> relocate(final Set<BreakpointAddress> addresses) {
    final Set<RelocatedAddress> relocatedAddresses = new HashSet<RelocatedAddress>();
    for (final BreakpointAddress breakpointAddress : addresses) {
      relocatedAddresses.add(
          fileToMemory(breakpointAddress.getModule(), breakpointAddress.getAddress()));
    }
    return relocatedAddresses;
  }

  /**
   * Connects to the debug client.
   *
   * @param connection The debug connection that is used to connect to the debug client.
   *
   * @throws DebugExceptionWrapper Thrown if connecting to the debug client failed.
   */
  protected final void connect(final DebugConnection connection) throws DebugExceptionWrapper {
    if (isConnected()) {
      throw new IllegalStateException("IE01270: Debugger is already connected");
    }
    this.connection = connection;
    try {
      this.connection.addEventListener(synchronizer);
      this.connection.startConnection();
    } catch (final ConnectException e) {
      this.connection.removeEventListener(synchronizer);
      this.connection = null;
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public final void addListener(final IDebugEventListener listener) {
    synchronizer.addListener(listener);
  }

  @Override
  public void cancelTargetSelection() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendCancelTargetSelection();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public boolean canDebug(final INaviModule module) {
    return addressConverters.keySet().contains(module);
  }

  @Override
  public void detach() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendDetachMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public RelocatedAddress fileToMemory(final INaviModule module, final UnrelocatedAddress address) {
    if (module == null) {
      return new RelocatedAddress(address.getAddress());
    }
    final IAddressConverter converter = addressConverters.get(module);
    if (converter == null) {
      throw new IllegalStateException(String.format(
          "Error: No address converter configured for module '%s'",
          module.getConfiguration().getName()));
    }
    return converter.fileToMemory(address);
  }

  @Override
  public final BookmarkManager getBookmarkManager() {
    return bookmarkManager;
  }

  @Override
  public final BreakpointManager getBreakpointManager() {
    return breakpointManager;
  }

  @Override
  public void getMemoryMap() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendMemoryMapMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public final IMemoryProvider getMemoryProvider() {
    return memorySynchronizer.getMemoryProvider();
  }

  @Override
  public INaviModule getModule(final RelocatedAddress address) {
    final List<Entry<INaviModule, IAddress>> baseList = new ArrayList<>(baseAddresses.entrySet());

    Collections.sort(baseList, new Comparator<Entry<INaviModule, IAddress>>() {

      @Override
      public int compare(final Entry<INaviModule, IAddress> lhs,
          final Entry<INaviModule, IAddress> rhs) {
        return lhs.getValue().toBigInteger().compareTo(rhs.getValue().toBigInteger());
      }
    });

    for (int i = 0; i < baseList.size(); i++) {
      final Entry<INaviModule, IAddress> current = baseList.get(i);
      if (i == (baseList.size() - 1)) {
        return current.getKey();
      }
      final Entry<INaviModule, IAddress> next = baseList.get(i + 1);
      if ((address.getAddress().toBigInteger().compareTo(current.getValue().toBigInteger())
          >= 0) && (address.getAddress().toBigInteger().compareTo(next.getValue().toBigInteger())
          == -1)) {
        return current.getKey();
      }
    }
    return null;
  }

  @Override
  public List<INaviModule> getModules() {
    return new ArrayList<INaviModule>(baseAddresses.keySet());
  }

  @Override
  public final ProcessManager getProcessManager() {
    return processManager;
  }

  @Override
  public void halt() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendHaltMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public final boolean isConnected() {
    return connection != null;
  }

  @Override
  public final UnrelocatedAddress memoryToFile(final RelocatedAddress address) {
    Preconditions.checkNotNull(address, "IE00790: Address argument can not be null");
    final List<Entry<INaviModule, IAddress>> baseList = new ArrayList<>(baseAddresses.entrySet());
    
    Collections.sort(baseList, new Comparator<Entry<INaviModule, IAddress>>() {
      @Override
      public int compare(final Entry<INaviModule, IAddress> lhs,
          final Entry<INaviModule, IAddress> rhs) {
        return lhs.getValue().toBigInteger().compareTo(rhs.getValue().toBigInteger());
      }
    });

    for (int i = 0; i < baseList.size(); i++) {
      final Entry<INaviModule, IAddress> current = baseList.get(i);
      if (i == (baseList.size() - 1)) {
        return memoryToFile(current.getKey(), address);
      }
      final Entry<INaviModule, IAddress> next = baseList.get(i + 1);
      if ((address.getAddress().toBigInteger().compareTo(current.getValue().toBigInteger())
          >= 0) && (address.getAddress().toBigInteger().compareTo(next.getValue().toBigInteger())
          == -1)) {
        return memoryToFile(current.getKey(), address);
      }
    }
    throw new IllegalStateException("IE00791: No module in the debugger");
  }

  @Override
  public final UnrelocatedAddress memoryToFile(final INaviModule module,
      final RelocatedAddress address) {
    Preconditions.checkNotNull(module, "IE00792: Module argument can not be null");
    Preconditions.checkNotNull(address, "IE00793: Address argument can not be null");
    final IAddressConverter converter = addressConverters.get(module);
    Preconditions.checkNotNull(converter,
        "IE00162: Module addresses can not be converted because the debugger has no relocation information for the module");
    return converter.memoryToFile(address);
  }

  @Override
  public int readMemory(final IAddress offset, final int size) throws DebugExceptionWrapper {
    Preconditions.checkNotNull(offset, "IE00794: Address argument can not be null");
    ensureConnection();
    try {
      return connection.sendReadMemoryMessage(offset, size);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void readRegisters() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendRegisterRequestMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void removeBreakpoints(final Set<BreakpointAddress> addresses, final BreakpointType type)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendRemoveBreakpointsMessage(relocate(addresses), type);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public final void removeListener(final IDebugEventListener listener) {
    synchronizer.removeListener(listener);
  }

  @Override
  public int requestFileSystem() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      return connection.sendRequestFileSystem();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void requestFileSystem(final String path) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendRequestFileSystem(path);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void requestMemoryRange(final IAddress address) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendMemoryRangeMessage(address);
    } catch (final IOException exception) {
      throw new DebugExceptionWrapper(exception);
    }
  }

  @Override
  public int requestProcessList() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      return connection.sendRequestProcessList();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void resume() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendResumeMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void resumeThread(final long tid) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendResumeThreadMessage(tid);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public int search(final IAddress start, final int size, final byte[] data)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      return connection.sendSearchMessage(start, size, data);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void selectFile(final String name) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendSelectFileMessage(name);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void selectProcess(final int pid) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendSelectProcessMessage(pid);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public final void setAddressTranslator(final INaviModule module, final IAddress fileBase,
      final IAddress imageBase) {
    baseAddresses.put(module, imageBase);
    addressConverters.put(module, new DefaultAddressConverter(imageBase, fileBase));
  }

  @Override
  public void setBreakPointCondition(final BreakpointAddress address, final Condition condition)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendSetBreakpointConditionMessage(
          fileToMemory(address.getModule(), address.getAddress()), condition);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void setBreakPoints(final Set<BreakpointAddress> addresses, final BreakpointType type)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendBreakpointsMessage(relocate(addresses), type);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void setDebuggerEventSettings(final DebuggerEventSettings eventSettings)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendDebuggerEventSettingsMessage(eventSettings);
    } catch (final IOException exception) {
      throw new DebugExceptionWrapper(exception);
    }
  }

  @Override
  public void setExceptionSettings(final Collection<DebuggerException> exceptions)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendExceptionSettingsMessage(exceptions);
    } catch (final IOException exception) {
      throw new DebugExceptionWrapper(exception);
    }
  }

  @Override
  public int setRegister(final long tid, final int index, final BigInteger value)
      throws DebugExceptionWrapper {
    ensureConnection();
    try {
      return connection.sendSetRegisterMessage(tid, index, value);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void setTerminated() {
    if (connection != null) {
      connection.removeEventListener(synchronizer);
    }
    if (connection != null) {
      connection.shutdown();
      connection = null;
    }
  }

  @Override
  public void singleStep() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendSingleStepMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void suspendThread(final long tid) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendSuspendThreadMessage(tid);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public void terminate() throws DebugExceptionWrapper {
    ensureConnection();
    try {
      connection.sendTerminateMessage();
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }

  @Override
  public int writeMemory(final IAddress address, final byte[] data) throws DebugExceptionWrapper {
    ensureConnection();
    try {
      return connection.sendWriteMemoryMessage(address, data);
    } catch (final IOException e) {
      throw new DebugExceptionWrapper(e);
    }
  }
}
