/*
Copyright 2014 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Debug.Debugger;

import com.google.security.zynamics.binnavi.Debug.Connection.MockDebugConnection;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.MessageParserException;
import com.google.security.zynamics.binnavi.debug.connection.packets.parsers.TargetInformationParser;
import com.google.security.zynamics.binnavi.debug.debugger.AbstractDebugger;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.zylib.disassembly.IAddress;

import java.math.BigInteger;
import java.util.Set;

public final class MockDebugger extends AbstractDebugger {
  private int counter = Integer.MAX_VALUE;

  public final MockDebugConnection connection;
  public String requests = "";

  private DebugTargetSettings m_debugSettings;

  public MockDebugger(final byte[][] data) {
    connection = new MockDebugConnection(this, data);
  }

  public MockDebugger(final DebugTargetSettings debugSettings) {
    m_debugSettings = debugSettings;
    connection = new MockDebugConnection(this);
  }

  private void processCounter() throws DebugExceptionWrapper {
    --counter;

    if (counter == 0) {
      throw new DebugExceptionWrapper(new Exception("No"));
    }
  }

  @Override
  public void cancelTargetSelection() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public boolean canDebug(final INaviModule module) {
    return true;
  }

  @Override
  public void close() {
    super.setTerminated();
  }

  @Override
  public void connect() throws DebugExceptionWrapper {
    super.connect(connection);

    requests += "CONNECT;";
  }

  public void connectNoTarget() throws DebugExceptionWrapper {
    super.connect(connection);
  }

  @Override
  public void detach() throws DebugExceptionWrapper {
    super.detach();

    requests += "DETACH;";
  }

  public MockDebugConnection getConnection() {
    return connection;
  }

  @Override
  public DebugTargetSettings getDebugTargetSettings() {
    return m_debugSettings;
  }

  @Override
  public int getId() {
    return 23;
  }

  @Override
  public void getMemoryMap() throws DebugExceptionWrapper {
    processCounter();

    requests += "MEMMAP;";
  }

  public MockDebugConnection getMockConnection() {
    return connection;
  }

  @Override
  public String getPrintableString() {
    return "Mock";
  }

  @Override
  public void halt() {
  }

  @Override
  public int readMemory(final IAddress address, final int size) throws DebugExceptionWrapper {
    super.readMemory(address, size);

    processCounter();

    requests += "READMEM/" + address.toString() + "/" + size + ";";

    return 0;
  }

  @Override
  public void readRegisters() throws DebugExceptionWrapper {
    super.readRegisters();

    processCounter();

    requests += "READREGS;";
  }

  public void receiveTargetInformation() {
    try {
      final String targetInformation =
          new String("<info>" + "<options>" + "</options>" + "<registers>"
              + "<register name=\"EAX\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EBX\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"ECX\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EDX\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"ESI\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EDI\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"ESP\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EBP\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EIP\" size=\"4\" editable=\"0\"/>"
              + "<register name=\"EFLAGS\" size=\"4\" editable=\"0\"/>" + "</registers>" + "<size>"
              + "</size>" + "</info>");

      getProcessManager().setTargetInformation(
          TargetInformationParser.parse(targetInformation.getBytes()));
    } catch (final MessageParserException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void removeBreakpoints(final Set<BreakpointAddress> breakpoints, final BreakpointType type)
      throws DebugExceptionWrapper {
    requests += "REMOVE_BREAKPOINTS/";

    for (final BreakpointAddress breakpointAddress : breakpoints) {
      requests += breakpointAddress.getAddress().getAddress().toHexString();
      requests += "/";
      requests += type;
    }

    requests += ";";

    processCounter();
  }

  @Override
  public int requestProcessList() {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void resume() throws DebugExceptionWrapper {
    super.resume();

    requests += "RESUME;";

    processCounter();
  }

  @Override
  public int search(final IAddress start, final int size, final byte[] data) throws DebugExceptionWrapper {
    processCounter();

    return 0;
  }

  @Override
  public void selectProcess(final int pid) {
    throw new IllegalStateException("Not yet implemented");
  }

  @Override
  public void setBreakPoints(final Set<BreakpointAddress> breakpoints, final BreakpointType type)
      throws DebugExceptionWrapper {
    requests += "SET_BREAKPOINTS/";

    for (final BreakpointAddress breakpointAddress : breakpoints) {
      requests += breakpointAddress.getAddress().getAddress().toHexString();
      requests += "/";
      requests += type;
    }

    requests += ";";

    processCounter();
  }

  public void setCounter(final int counter) {
    this.counter = counter;
  }

  @Override
  public int setRegister(final long tid, final int index, final BigInteger value)
      throws DebugExceptionWrapper {
    processCounter();

    return 0;
  }

  @Override
  public void singleStep() throws DebugExceptionWrapper {
    super.singleStep();

    requests += "STEP;";

    processCounter();
  }

  @Override
  public void terminate() throws DebugExceptionWrapper {
    super.terminate();

    requests += "TERMINATE;";

    processCounter();
  }
}
