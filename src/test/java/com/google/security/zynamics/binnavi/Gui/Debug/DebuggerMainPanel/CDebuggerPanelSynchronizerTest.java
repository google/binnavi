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
package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerMainPanel;
//package com.google.security.zynamics.binnavi.Gui.Debug.DebuggerMainPanel;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import com.google.security.zynamics.binnavi.Database.MockProject;
//import com.google.security.zynamics.binnavi.debug.CDebuggerSynchronizer;
//import com.google.security.zynamics.binnavi.debug.MockDebugConnection;
//import com.google.security.zynamics.binnavi.debug.MockDebugger;
//import com.google.security.zynamics.binnavi.debug.Breakpoints.CBreakpointManager;
//import com.google.security.zynamics.binnavi.debug.connection.CDebugCommandType;
//import com.google.security.zynamics.binnavi.debug.connection.CMockAddressConverter;
//import com.google.security.zynamics.binnavi.debug.Events.CDebugEvent;
//import com.google.security.zynamics.binnavi.debug.Packets.CDebugMessageAddressArgument;
//import com.google.security.zynamics.binnavi.debug.ProcessManager.CThread;
//import com.google.security.zynamics.binnavi.debug.ProcessManager.ThreadState;
//
//public final class CDebuggerPanelSynchronizerTest
//{
//	private MockProject project;
//	private MockPanelProvider panel;
//	private CDebuggerSynchronizer debuggerSynchronizer;
//
//	@Before
//	public void setUp()
//	{
//		project = new MockProject();
//		panel = new MockPanelProvider();
//
//		final MockDebugger debugger = (MockDebugger) project.getDebugger();
//
//		final MockDebugConnection connection = debugger.getMockConnection();
//
//		debugger.getProcessManager().addThread(new CThread(123, ThreadState.SUSPENDED));
//
//		connection.addEventListener(new CDebuggerSynchronizer(debugger, new CBreakpointManager()));
//
//		debuggerSynchronizer = connection.getSynchronizer();
//	}
//
//	@Test
//	public void testDynamicAttach()
//	{
//		project.setDebugger(null);
//
//		final CDebuggerPanelSynchronizer synchronizer = new CDebuggerPanelSynchronizer(project, panel);
//
//		project.setDebugger(new MockDebugger(new CMockAddressConverter(), new CBreakpointManager()));
//		project.startConnection();
//
//		debuggerSynchronizer.notify(new CDebugEvent(CDebugCommandType.RESP_MEMRANGE, 0, new DebugMessageAddressArgument(400), 123, "Hannes".getBytes()));
//
//		assertEquals("UPDATE_GUI;", panel.events);
//	}
//
//	@Test
//	public void testMemoryChanged()
//	{
//		new CDebuggerPanelSynchronizer(project, panel);
//
//		debuggerSynchronizer.notify(new CDebugEvent(CDebugCommandType.RESP_MEMRANGE, 0, new DebugMessageAddressArgument(400), 123, "Hannes".getBytes()));
//
//		assertEquals("THREAD_ADDED-123;UPDATE_GUI;", panel.events);
//	}
//
//	@Test
//	public void testRegistersChanged()
//	{
//		new CDebuggerPanelSynchronizer(project, panel);
//
//		debuggerSynchronizer.notify(new CDebugEvent(CDebugCommandType.RESP_REGISTERS, 0, (DebugMessageAddressArgument) null, 123, "<Registers><Register name=\"EAX\" value=\"123\" /><Register name=\"EBX\" value=\"456\" /><Register name=\"EIP\" value=\"999\" pc=\"true\" /></Registers>".getBytes()));
//
//		assertEquals("THREAD_ADDED-123;UPDATE_REGISTERS;", panel.events);
//	}
//
//	@Test
//	public void testStop()
//	{
//		final CDebuggerPanelSynchronizer synchronizer = new CDebuggerPanelSynchronizer(project, panel);
//
//		synchronizer.stop();
//	}
//
//	@Test
//	public void testTerminate()
//	{
//		new CDebuggerPanelSynchronizer(project, panel);
//
//		debuggerSynchronizer.notify(new CDebugEvent(CDebugCommandType.RESP_TERMINATE_SUCCESS, 0, (DebugMessageAddressArgument) null, 123, "<Registers><Register name=\"EAX\" value=\"123\" /><Register name=\"EBX\" value=\"456\" /><Register name=\"EIP\" value=\"999\" pc=\"true\" /></Registers>".getBytes()));
//
//		// 1. Add thread 123
//		// 2. Terminate => Update GUI
//		// 3. Clear the memory map
//		// 4. Remove thread 123
//
//		assertEquals("THREAD_ADDED-123;UPDATE_GUI;UPDATE_MEMORY;THREAD_REMOVED-123;UPDATE_GUI;", panel.events);
//	}
//}
