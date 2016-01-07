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
package com.google.security.zynamics.binnavi.Gui.Debug.Notifier;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Gui.Debug.BreakpointRemovalNotification.CBreakpointRemovalDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.GraphSelectionDialog.CGraphSelectionDialog;
import com.google.security.zynamics.binnavi.Gui.Debug.RemoteBrowser.Loader.CRemoteFileBrowserLoader;
import com.google.security.zynamics.binnavi.Gui.Debug.ToolbarPanel.Implementations.CDebuggerFunctions;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.CGraphWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphContainerWindow;
import com.google.security.zynamics.binnavi.Gui.GraphWindows.IGraphPanel;
import com.google.security.zynamics.binnavi.Gui.Loaders.CViewOpener;
import com.google.security.zynamics.binnavi.Gui.WindowManager.CWindowManager;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.Log.NaviLogger;
import com.google.security.zynamics.binnavi.Settings.CGlobalSettings;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AnyBreakpointRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AttachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.AuthenticationFailedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointConditionSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.BreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.DetachReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointSetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.EchoBreakpointsRemovedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.HaltReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.MemoryMapReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ProcessClosedReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.QueryDebuggerEventSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ReadMemoryReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RegistersReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.RequestTargetReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SetExceptionSettingsReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.SingleStepReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TargetInformationReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.TerminateReply;
import com.google.security.zynamics.binnavi.debug.connection.packets.replies.ValidateMemoryReply;
import com.google.security.zynamics.binnavi.debug.debugger.DebugEventListenerAdapter;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerHelpers;
import com.google.security.zynamics.binnavi.debug.debugger.DebuggerErrorCodes;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.BackEndDebuggerProvider;
import com.google.security.zynamics.binnavi.debug.debugger.DebugTargetSettings;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.BreakpointAddress;
import com.google.security.zynamics.binnavi.debug.models.breakpoints.enums.BreakpointType;
import com.google.security.zynamics.binnavi.debug.models.processmanager.MemoryModule;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManager;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ProcessManagerListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.TargetProcessThread;
import com.google.security.zynamics.binnavi.debug.models.processmanager.ThreadListenerAdapter;
import com.google.security.zynamics.binnavi.debug.models.processmanager.interfaces.ProcessManagerListener;
import com.google.security.zynamics.binnavi.debug.models.storage.DebuggerEventSettingsStorage;
import com.google.security.zynamics.binnavi.disassembly.RelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.UnrelocatedAddress;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.binnavi.disassembly.views.INaviView;
import com.google.security.zynamics.binnavi.disassembly.views.IViewContainer;
import com.google.security.zynamics.zylib.general.Pair;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Tells the user about events in the debugger.
 */
public final class CDebugEventNotifier {
  /**
   * Parent window used for dialogs.
   */
  private final JFrame m_parent;

  /**
   * Debugger whose messages are shown to the user.
   */
  private final IDebugger m_debugger;

  /**
   * The target debugged by the debugger.
   */
  private final DebugTargetSettings m_debugTarget;

  /**
   * View container of the debug target.
   */
  private final IViewContainer m_viewContainer;

  /**
   * Used to show debugger error messages to the user.
   */
  private final InternalDebuggerListener m_debuglistener = new InternalDebuggerListener();

  /**
   * Used to open views with the new PC address.
   */
  private final InternalThreadEventListener m_threadEventListener =
      new InternalThreadEventListener();

  /**
   * Used to display corrected base addresses during debugging.
   */
  private final ProcessManagerListener m_processListener = new InternalProcessListener();

  /**
   * Creates a new debug event notifier.
   *
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger whose messages are shown to the user.
   * @param debugTarget The target debugged by the debugger.
   * @param viewContainer View container of the debug target.
   */
  public CDebugEventNotifier(final JFrame parent, final IDebugger debugger,
      final DebugTargetSettings debugTarget, final IViewContainer viewContainer) {
    m_parent = parent;
    m_debugTarget =
        Preconditions.checkNotNull(debugTarget, "IE02292: debugTarget argument can not be null");
    m_viewContainer = Preconditions.checkNotNull(viewContainer,
        "IE02293: viewContainer argument can not be null");
    m_debugger = Preconditions.checkNotNull(debugger, "IE02294: debugger argument can not be null");
  }

  /**
   * Activates the window of this debugger.
   *
   * @return The window of this debugger.
   */
  private JFrame activateWindow() {
    for (final IGraphContainerWindow window : CWindowManager.instance().getOpenWindows()) {
      for (final IGraphPanel graphPanel : window) {
        final BackEndDebuggerProvider debuggerProvider =
            graphPanel.getModel().getDebuggerProvider();

        for (final IDebugger d : debuggerProvider) {
          if (d == m_debugger) {
            window.activate(graphPanel);
            window.show();

            return window.getFrame();
          }
        }
      }
    }

    return null;
  }

  /**
   * Asks the user to select a view from a list of views. The selected view is then opened.
   *
   * @param views List of views to select from.
   */
  private void selectView(final List<INaviView> views) {
    final CGraphSelectionDialog dlg = new CGraphSelectionDialog(m_parent, views);
    dlg.setVisible(true);
    final INaviView result = dlg.getSelectionResult();
    if (result != null) {
      CViewOpener.showView(m_parent, m_viewContainer, result,
          CWindowManager.instance().getLastWindow());
    }
  }

  /**
   * Starts the event notifier.
   */
  public void start() {
    m_debugger.addListener(m_debuglistener);
    final ProcessManager processManager = m_debugger.getProcessManager();
    processManager.addListener(m_processListener);
    for (final TargetProcessThread thread : processManager.getThreads()) {
      thread.addListener(m_threadEventListener);
    }
  }

  /**
   * Stops the event notifier.
   */
  public void stop() {
    m_debugger.removeListener(m_debuglistener);
    final ProcessManager processManager = m_debugger.getProcessManager();
    processManager.removeListener(m_processListener);
    for (final TargetProcessThread thread : processManager.getThreads()) {
      thread.removeListener(m_threadEventListener);
    }
  }

  /**
   * Used to show debugger error messages to the user.
   */
  private class InternalDebuggerListener extends DebugEventListenerAdapter {
    private void internalReceivedReply(final AnyBreakpointRemovedReply reply) {
      final Iterable<Pair<RelocatedAddress, Integer>> failedAddresses = Collections2.filter(
          reply.getAddresses(), new Predicate<Pair<RelocatedAddress, Integer>>() {
            @Override
            public boolean apply(final Pair<RelocatedAddress, Integer> item) {
              return item.second().intValue() != 0;
            }
          });
      if (!Iterables.isEmpty(failedAddresses)) {
        CBreakpointRemovalDialog.show(m_parent, failedAddresses);
      }
    }

    /**
     * Send the settings describing how to handle certain debugger events to the debug client.
     */
    private void sendDebuggerEventSettings() {
      try {
        final DebuggerEventSettingsStorage eventSettingsStorage =
            new DebuggerEventSettingsStorage(m_debugger, m_debugTarget);
        m_debugger.setDebuggerEventSettings(eventSettingsStorage.deserialize());
      } catch (final DebugExceptionWrapper exception) {
        CUtilityFunctions.logException(exception);
        final String message = "Debugger event settings could not be sent to the debugger.";
        final String description = CUtilityFunctions.createDescription(String.format(
            "BinNavi could not send the debugger event settings to the debug client."),
            new String[] {},
            new String[] {"The default debugger event settings will be used during this session."});
        NaviErrorDialog.show(m_parent, message, description, exception);
      } catch (final CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
        final String message = "Debugger event settings could not be retrieved from the database.";
        final String description = CUtilityFunctions.createDescription(String.format(
            "BinNavi could not send the debugger event settings to the debug client."),
            new String[] {},
            new String[] {"The default debugger event settings will be used during this session."});
        NaviErrorDialog.show(m_parent, message, description, exception);
      }
    }

    /**
     * Send the exception settings to be used by the debug client.
     *
     * @param reply
     */
    private void sendExceptionSettings(final TargetInformationReply reply) {
      try {
        m_debugger.setExceptionSettings(CDebuggerFunctions.mergeExceptionsSettings(m_debugTarget,
            reply.getTargetInformation().getDebuggerOptions().getExceptions(), m_debugger.getId()));
      } catch (final DebugExceptionWrapper exception) {
        CUtilityFunctions.logException(exception);
        final String message = "Exception settings could not be sent to the debugger.";
        final String description =
            CUtilityFunctions.createDescription(
                String.format(
                    "BinNavi could not send the exception settings to the debug client."),
                new String[] {"BinNavi is unable to show the debugger options "
                    + "dialog until the problem is resolved."},
                new String[] {"The default debugger options will be used during this session."});
        NaviErrorDialog.show(m_parent, message, description, exception);
      } catch (final CouldntLoadDataException exception) {
        CUtilityFunctions.logException(exception);
        final String message = "Exception settings could not be loaded.";
        final String description = CUtilityFunctions.createDescription(
            String.format("BinNavi could not load the exception settings from the database."),
            new String[] {"Communication error while contacting the database"}, new String[] {
                "BinNavi is unable to read the exception settings from the database.",
                "The default exception settings will be used during this session."});
        NaviErrorDialog.show(m_parent, message, description, exception);
      }
    }

    @Override
    public void debugException(final DebugExceptionWrapper debugException) {
      final String innerMessage = "E00074: " + "An exception occurred during debugging";
      final String innerDescription =
          "An exception occurred in the BinNavi debugger. You may want to check the stack trace to "
          + "get more information.";
      NaviErrorDialog.show(activateWindow(), innerMessage, innerDescription, debugException);
    }

    @Override
    public void debuggerClosed(final int code) {
      if (code != 0) {
        CMessageBox.showError(activateWindow(),
            "The connection to the debug client terminated unexpectedly.");
      }
      stop();
    }

    @Override
    public void receivedReply(final AttachReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(),
            "The debug client could not attach to the target process.");
        stop();
      }
    }

    @Override
    public void receivedReply(final AuthenticationFailedReply reply) {
      CMessageBox.showError(m_parent,
          "Debug client did not correctly authenticate itself and can not be used.");
    }

    @Override
    public void receivedReply(final BreakpointConditionSetReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), "Could not set the breakpoint condition");
      }
    }

    @Override
    public void receivedReply(final BreakpointSetReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not set a breakpoint at address %s.\n\nReason: %s", reply.getAddresses(),
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final BreakpointsRemovedReply reply) {
      internalReceivedReply(reply);
    }

    @Override
    public void receivedReply(final DetachReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not detach from the target process.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointSetReply reply) {
      final List<Pair<RelocatedAddress, Integer>> receivedAddresses = reply.getAddresses();
      for (final Pair<RelocatedAddress, Integer> addressErrorPair : receivedAddresses) {
        final RelocatedAddress relocatedAddress = addressErrorPair.first();
        final BreakpointAddress breakPointAddress =
            DebuggerHelpers.getBreakpointAddress(m_debugger, relocatedAddress);
        if (!m_debugger.getBreakpointManager().hasBreakpoint(BreakpointType.ECHO,
            breakPointAddress)) {
          CMessageBox.showError(activateWindow(), String.format(
              "Breakpoint manager does not know break point with address: %x",
              breakPointAddress.getAddress().getAddress().toLong()));
          break;
        }
      }
    }

    @Override
    public void receivedReply(final EchoBreakpointsRemovedReply reply) {
      internalReceivedReply(reply);
    }

    @Override
    public void receivedReply(final HaltReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not halt the target process.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final MemoryMapReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not determine the memory layout of the target process.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final ProcessClosedReply reply) {
      if (reply.success()) {
        stop();
        CMessageBox.showInformation(activateWindow(), "The target process terminated.");
      }
    }

    @Override
    public void receivedReply(final QueryDebuggerEventSettingsReply reply) {
      sendDebuggerEventSettings();
    }

    @Override
    public void receivedReply(final ReadMemoryReply reply) {
      if (!reply.success()) {
        NaviLogger.info(String.format("Could not read memory from the target process since the "
            + "memory map might have changed. Refreshing the memory map.\nError code was: %d",
            reply.getErrorCode()));
      }
    }

    @Override
    public void receivedReply(final RegistersReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not read the registers of the target thread.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final RequestTargetReply reply) {
      if (reply.success()) {
        SwingUtilities.invokeLater(new Thread() {
          @Override
          public void run() {
            final CRemoteFileBrowserLoader loader =
                new CRemoteFileBrowserLoader(m_parent, m_debugger);
            if (!loader.load()) {
              try {
                m_debugger.cancelTargetSelection();
                CDebugEventNotifier.this.stop();
              } catch (final DebugExceptionWrapper e) {
                CUtilityFunctions.logException(e);
              }
            }
          }
        });
      }
    }

    @Override
    public void receivedReply(final SetExceptionSettingsReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(),
            "Debug client was unable to set the specified exception settings.");
      }
    }

    @Override
    public void receivedReply(final SingleStepReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not do single-step operation.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final TargetInformationReply reply) {
      sendExceptionSettings(reply);
    }

    @Override
    public void receivedReply(final TerminateReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not terminate the target process.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }

    @Override
    public void receivedReply(final ValidateMemoryReply reply) {
      if (!reply.success()) {
        CMessageBox.showError(activateWindow(), String.format(
            "Could not validate the memory of the target process.\n\nReason: %s",
            DebuggerErrorCodes.codeToMessage(reply.getErrorCode())));
      }
    }
  }

  /**
   * Used to display corrected base addresses during debugging.
   */
  private class InternalProcessListener extends ProcessManagerListenerAdapter {
    @Override
    public void addedModule(final MemoryModule module) {
      CRelocationNotifier.checkBaseAddresses(m_parent, m_debugger, m_viewContainer,
          Lists.newArrayList(module));
    }

    @Override
    public void addedThread(final TargetProcessThread thread) {
      thread.addListener(m_threadEventListener);
    }

    @Override
    public void removedThread(final TargetProcessThread thread) {
      thread.removeListener(m_threadEventListener);
    }
  }

  /**
   * Used to open views with the new PC address.
   */
  private class InternalThreadEventListener extends ThreadListenerAdapter {
    @Override
    public void instructionPointerChanged(final TargetProcessThread thread,
        final RelocatedAddress oldAddress) {
      if (thread != m_debugger.getProcessManager().getActiveThread()) {
        return;
      }
      final RelocatedAddress threadAddress = thread.getCurrentAddress();
      if (threadAddress == null) {
        return;
      }
      if ((oldAddress != null) && oldAddress.equals(threadAddress)) {
        return;
      }
      if (CGlobalSettings.SHOW_DIALOGS) {
        final UnrelocatedAddress address = m_debugger.memoryToFile(threadAddress);
        final INaviModule naviModule = m_debugger.getModule(threadAddress);
        final IGraphPanel panel = CPanelFinder.getPanelWithAddress(m_debugger, address);
        if (panel == null) {
          try {
            final List<INaviView> views =
                naviModule.getViewsWithAddresses(Lists.newArrayList(address), true);
            if (views.size() > 0) {
              selectView(views);
            }
          } catch (final CouldntLoadDataException e) {
            CUtilityFunctions.logException(e);
          }
        } else {
          final CGraphWindow parent = panel.getModel().getParent();
          parent.toFront();
          parent.activate(panel);
        }
      }
    }
  }
}
