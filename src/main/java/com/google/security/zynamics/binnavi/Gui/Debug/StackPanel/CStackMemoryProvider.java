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
package com.google.security.zynamics.binnavi.Gui.Debug.StackPanel;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.debug.debugger.MemoryRangeCalculator;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IMemoryProvider;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemorySection;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.zylib.disassembly.CAddress;
import com.google.security.zynamics.zylib.disassembly.IAddress;
import com.google.security.zynamics.zylib.general.ByteHelpers;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.general.memmanager.IMemoryListener;
import com.google.security.zynamics.zylib.gui.JStackView.AddressMode;
import com.google.security.zynamics.zylib.gui.JStackView.IStackModel;
import com.google.security.zynamics.zylib.gui.JStackView.IStackModelListener;

import java.math.BigInteger;

/**
 * Provides the stack data that is displayed in the window.
 */
public final class CStackMemoryProvider implements IStackModel {
  /**
   * The debugger that communicates with the target process.
   */
  private IDebugger m_debugger;

  /**
   * Provides the stack memory.
   */
  private IMemoryProvider m_memoryProvider;

  /**
   * The thread whose stack is currently displayed.
   */
  private TargetProcessThread m_activeThread;

  /**
   * Listeners that are notified about changes in the stack data.
   */
  private final ListenerProvider<IStackModelListener> m_listeners =
      new ListenerProvider<IStackModelListener>();

  /**
   * Keeps track of the target process memory and updates the stack window if necessary.
   */
  private final InternalMemoryListener m_listener = new InternalMemoryListener();

  /**
   * Keeps track of the target process and updates the stack window if necessary.
   */
  private final InternalProcessListener m_internalProcessListener = new InternalProcessListener();

  /**
   * Determines how the stack data is displayed.
   */
  private StackDataLayout m_dataLayout = StackDataLayout.Bytes;

  /**
   * Determines how the stack addresses are displayed.
   */
  private AddressMode m_addressMode = AddressMode.BIT32;

  /**
   * Determines the value of the stack pointer.
   *
   * @param registers List of all register values.
   *
   * @return The value of the stack pointer.
   */
  private static BigInteger getStackValue(final ImmutableList<RegisterValue> registers) {
    for (final RegisterValue registerValue : registers) {
      if (registerValue.isSp()) {
        return registerValue.getValue();
      }
    }

    return null;
  }

  /**
   * Notifies listeners about changes in the stack memory.
   */
  private void notifyListeners() {
    for (final IStackModelListener listener : m_listeners) {
      // ESCA-JAVA0166: Catch Exception here because we are calling a listener function.
      try {
        listener.dataChanged();
      } catch (final Exception exception) {
        CUtilityFunctions.logException(exception);
      }
    }
  }

  @Override
  public void addListener(final IStackModelListener listener) {
    m_listeners.addListener(listener);
  }

  @Override
  public String getElement(final long address) {
    if (m_dataLayout == StackDataLayout.Bytes) {
      if (m_addressMode == AddressMode.BIT32) {
        final String unpaddedValue = BigInteger.valueOf(ByteHelpers.readDwordBigEndian(
            m_debugger.getProcessManager().getMemory().getData(address, 4), 0)).toString(16);
        return Strings.padStart(unpaddedValue, 8, '0').toUpperCase();
      } else if (m_addressMode == AddressMode.BIT64) {
        final String unpaddedValue = BigInteger.valueOf(ByteHelpers.readQwordBigEndian(
            m_debugger.getProcessManager().getMemory().getData(address, 8), 0)).toString(16);
        return Strings.padStart(unpaddedValue, 16, '0').toUpperCase();
      } else {
        throw new IllegalStateException("IE01137: Unknown address mode");
      }
    } else if (m_dataLayout == StackDataLayout.Dwords) {
      if (m_addressMode == AddressMode.BIT32) {
        final String unpaddedValue = BigInteger.valueOf(ByteHelpers.readDwordLittleEndian(
            m_debugger.getProcessManager().getMemory().getData(address, 4), 0)).toString(16);
        return Strings.padStart(unpaddedValue, 8, '0').toUpperCase();
      } else if (m_addressMode == AddressMode.BIT64) {
        final String unpaddedValue = BigInteger.valueOf(ByteHelpers.readQwordLittleEndian(
            m_debugger.getProcessManager().getMemory().getData(address, 8), 0)).toString(16);
        return Strings.padStart(unpaddedValue, 16, '0').toUpperCase();
      } else {
        throw new IllegalStateException("IE01138: Unknown address mode");
      }
    } else {
      throw new IllegalStateException("IE01139: Invalid data layout selected");
    }
  }

  @Override
  public int getNumberOfEntries() {
    if (m_activeThread == null || m_debugger == null) {
      return 0;
    }

    final BigInteger stackValue = getStackValue(m_activeThread.getRegisterValues());

    if (stackValue == null) {
      return 0;
    }

    final MemorySection section =
        m_debugger.getProcessManager().getMemoryMap().findOffset(stackValue);

    if (section == null) {
      if (m_debugger.isConnected()) {
        return m_debugger.getProcessManager().getTargetInformation().getDebuggerOptions()
            .getPageSize();
      } else {
        return 0;
      }
    } else {
      return (int) ((section.getEnd().toLong() - section.getStart().toLong()) / 4);
    }
  }

  @Override
  public long getStackPointer() {
    if (m_activeThread == null) {
      return -1;
    }

    final BigInteger stackValue = getStackValue(m_activeThread.getRegisterValues());

    if (stackValue == null) {
      return -1;
    }

    return stackValue.longValue();
  }

  @Override
  public long getStartAddress() {
    if (m_activeThread == null || m_activeThread.getRegisterValues().size() == 0
        || m_debugger == null) {
      return -1;
    }

    final BigInteger stackValue = getStackValue(m_activeThread.getRegisterValues());

    if (stackValue == null) {
      return -1;
    }

    final MemorySection section =
        m_debugger.getProcessManager().getMemoryMap().findOffset(stackValue);

    if (section == null) {
      if (m_debugger.isConnected()) {
        return stackValue.and(BigInteger.valueOf(~0xFFF)).longValue();
      } else {
        return -1;
      }
    } else {
      return section.getStart().toLong();
    }
  }

  @Override
  public boolean hasData(final long startAddress, final long numberOfBytes) {
    if (m_debugger == null) {
      return false;
    }
    if (m_debugger.getProcessManager().getTargetInformation().getDebuggerOptions().canMemmap()) {
      return m_memoryProvider != null
          && m_memoryProvider.hasData(BigInteger.valueOf(startAddress), (int) numberOfBytes);
    } else {
      final CAddress stackStart = new CAddress(getStartAddress());
      final int stackSize = getNumberOfEntries() * 4;
      final CAddress stackEnd = new CAddress(getStartAddress() + stackSize);

      final Pair<IAddress, Integer> realRange = MemoryRangeCalculator.calculateRequestRange(
          BigInteger.valueOf(startAddress), (int) numberOfBytes, stackStart, stackEnd);

      final long realStart = realRange.first().toLong();
      final long realSize = realRange.second();

      return m_memoryProvider != null && m_memoryProvider.hasData(BigInteger.valueOf(startAddress),
          (int) numberOfBytes, BigInteger.valueOf(realStart), (int) realSize);
    }
  }

  @Override
  public boolean keepTrying() {
    return m_debugger != null && m_debugger.isConnected();
  }

  /**
   * Sets the thread whose stack is displayed.
   *
   * @param activeThread The new thread.
   */
  public void setActiveThread(final TargetProcessThread activeThread) {
    m_activeThread = activeThread;

    notifyListeners();
  }

  /**
   * Sets the address mode of the stack memory provider.
   *
   * @param addressMode The new address model.
   */
  public void setAddressMode(final AddressMode addressMode) {
    Preconditions.checkNotNull(addressMode, "IE01501: Address mode argument can not be null");

    if (addressMode == m_addressMode) {
      return;
    }

    m_addressMode = addressMode;

    notifyListeners();
  }

  /**
   * Updates the way the stack values are displayed.
   *
   * @param dataLayout The new stack value layout.
   */
  public void setDataLayout(final StackDataLayout dataLayout) {
    Preconditions.checkNotNull(dataLayout, "IE01502: Layout argument can not be null");

    m_dataLayout = dataLayout;

    notifyListeners();
  }

  /**
   * Sets the debugger used to read the stack memory.
   *
   * @param debugger The new debugger.
   */
  public void setDebugger(final IDebugger debugger) {
    if (m_debugger != null) {
      m_debugger.getProcessManager().getMemory().removeMemoryListener(m_listener);
      m_debugger.getProcessManager().removeListener(m_internalProcessListener);
    }

    m_debugger = debugger;

    m_memoryProvider = debugger == null ? null : debugger.getMemoryProvider();

    if (debugger != null) {
      m_debugger.getProcessManager().getMemory().addMemoryListener(m_listener);
      m_debugger.getProcessManager().addListener(m_internalProcessListener);
    }

    notifyListeners();
  }

  /**
   * Keeps track of the target process memory and updates the stack window if necessary.
   */
  private class InternalMemoryListener implements IMemoryListener {
    @Override
    public void memoryChanged(final long address, final int size) {
      notifyListeners();
    }

    @Override
    public void memoryCleared() {
      notifyListeners();
    }
  }

  /**
   * Keeps track of the target process and updates the stack window if necessary.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void detached() {
      setActiveThread(null);
    }
  }
}
