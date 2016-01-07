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
package com.google.security.zynamics.binnavi.standardplugins.callresolver;

import com.google.security.zynamics.binnavi.API.debug.DebugException;
import com.google.security.zynamics.binnavi.API.disassembly.Address;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.API.disassembly.CouldntSaveDataException;
import com.google.security.zynamics.binnavi.API.disassembly.Function;
import com.google.security.zynamics.binnavi.API.disassembly.Module;
import com.google.security.zynamics.binnavi.API.disassembly.View;
import com.google.security.zynamics.binnavi.API.helpers.Settings;
import com.google.security.zynamics.binnavi.API.plugins.PluginInterface;
import com.google.security.zynamics.binnavi.yfileswrap.API.disassembly.View2D;
import com.google.security.zynamics.zylib.gui.GuiHelper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

/**
 * This dialog shows the call resolver progress. The user can use this dialog to step through the
 * individual steps of call resolving and to receive feedback of what is going on.
 */
public final class CallResolverDialog extends JDialog {

  /**
   * These panels show the individual steps necessary for resolving indirect calls. They give the
   * user a way to follow the progress of the call resolver operation.
   */
  private final IconPanel[] panels = new IconPanel[] {new IconPanel("1. Loading target"),
      new IconPanel("2. Find indirect calls"),
      new IconPanel("3. Start debugger", new ResumeButton()), new IconPanel("4. Set breakpoints"),
      new IconPanel("5. Resolving breakpoints"), new IconPanel("6. Stop call resolving")};

  /**
   * Information about the call resolving progress is printed here.
   */
  private final JTextArea outputArea = new JTextArea();

  /**
   * This button is used to show the resolves functions as a list in the output text field.
   */
  private final JButton listResolvedFunctionsButton =
      new JButton(new ListResolvedFunctionsAction());

  /**
   * This button is used to turn the resolved functions into a graph which is then opened in a new
   * window.
   */
  private final JButton graphResolvedFunctionsButton =
      new JButton(new GraphResolvedFunctionsAction());

  private final JButton portResultsButton = new JButton(new GraphAllFunctionsAction());

  /**
   * Action of the Next button.
   */
  private final NextAction nextAction = new NextAction();

  /**
   * Used to resolve the indirect function calls.
   */
  private final CallResolver callResolver;

  /**
   * Creates a new dialog object.
   *
   * @param parent Parent window of the dialog.
   * @param target The target whose calls are resolved.
   */
  private CallResolverDialog(final JFrame parent, final ICallResolverTarget target) {
    super(parent, "Call Resolver");

    assert parent != null;
    assert target != null;

    callResolver = new InternalCallResolver(target, parent);

    setLayout(new BorderLayout());

    add(new LabelPanel(), BorderLayout.NORTH);
    add(new OutputPanel());
    add(new ButtonPanel(), BorderLayout.SOUTH);

    setSize(700, 600);
    setResizable(false);
    setLocationRelativeTo(parent);

    // We need to disable all dialogs to smoothen the lookup.
    Settings.setShowDialogs(false);

    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(final WindowEvent e) {
        close();
      }

      @Override
      public void windowClosing(final WindowEvent e) {
        setVisible(false);

        dispose();
      }
    });

    updateGui();
  }

  /**
   * Shows a call resolver dialog.
   *
   * @param parent Parent window of the dialog.
   * @param target The target whose calls are resolved.
   */
  public static void show(final JFrame parent, final ICallResolverTarget target) {
    final CallResolverDialog dialog = new CallResolverDialog(parent, target);

    dialog.setVisible(true);
  }

  /**
   * Adds a line of text to the output panel.
   *
   * @param string The text to add.
   */
  private void appendOutput(final String string) {
    outputArea.setText(outputArea.getText() + string + "\n");
    outputArea.setCaretPosition(outputArea.getDocument().getLength());
  }

  private void close() {
    Settings.setShowDialogs(true);

    callResolver.dispose();

    setVisible(false);
  }

  private void graphAllFunctions() {
    new Thread() {
      @Override
      public void run() {
        final View view = OutputGraphGenerator.createCompleteView(
            callResolver.getTarget(), callResolver.getIndirectAddresses(),
            callResolver.getResolvedAddresses());

        final View2D view2d = PluginInterface.instance().showInLastWindow(view);

        view2d.doHierarchicalLayout();

        try {
          view.save();
        } catch (final CouldntSaveDataException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Shows the results of the call resolver operation in a new call graph view.
   */
  private void graphResolvedFunctions() {
    new Thread() {
      @Override
      public void run() {
        final View view = OutputGraphGenerator.createLoggedView(
            callResolver.getTarget(), callResolver.getIndirectAddresses(),
            callResolver.getResolvedAddresses());

        final View2D view2d = PluginInterface.instance().showInLastWindow(view);

        view2d.doHierarchicalLayout();

        try {
          view.save();
        } catch (final CouldntSaveDataException e) {
          e.printStackTrace();
        }
      }
    }.start();
  }

  /**
   * Shows the results of the call resolver operation in the text output field.
   */
  private void listResolvedFunctions() {
    outputArea.setText(OutputListGenerator.generate(callResolver.getResolvedAddresses()));

    outputArea.setCaretPosition(0);
  }

  /**
   * Updates the GUI depending on the state of the resolver process.
   */
  private void updateGui() {
    final int currentStep = callResolver.getCurrentStep();

    for (int i = 0; i < panels.length; i++) {
      panels[i].setEnabled(i <= currentStep);
      panels[i].setDone(i < currentStep);
    }

    listResolvedFunctionsButton.setEnabled(currentStep == panels.length);
    graphResolvedFunctionsButton.setEnabled(currentStep == panels.length);
    portResultsButton.setEnabled(currentStep == panels.length);

    nextAction.putValue(Action.NAME, currentStep == panels.length ? "Reset" : "Next");
  }

  /**
   * This panel contains the Next/Reset and Cancel buttons shown at the bottom of the dialog.
   */
  private class ButtonPanel extends JPanel {

    public ButtonPanel() {
      super(new BorderLayout());
      final JPanel innerButtonPanel = new JPanel(new BorderLayout());

      innerButtonPanel.add(new JButton(nextAction), BorderLayout.WEST);
      innerButtonPanel.add(new JButton(new CancelAction()), BorderLayout.EAST);

      add(innerButtonPanel, BorderLayout.EAST);
    }
  }

  /**
   * Action that is used to close the dialog when the user clicks on the Cancel button.
   */
  private class CancelAction extends AbstractAction {

    public CancelAction() {
      super("Cancel");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      close();
    }
  }

  /**
   * Action that is used to show all resolver results in a new graph.
   */
  private class GraphAllFunctionsAction extends AbstractAction {

    public GraphAllFunctionsAction() {
      super("Create complete call graph view");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      graphAllFunctions();
    }
  }

  /**
   * Action that is used to show the resolver results in a new graph.
   */
  private class GraphResolvedFunctionsAction extends AbstractAction {

    public GraphResolvedFunctionsAction() {
      super("Create limited call graph view");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      graphResolvedFunctions();
    }
  }

  /**
   * Class for displaying the panels that show the progress.
   */
  private static class IconPanel extends JPanel {

    /**
     * Shows text that describes the step.
     */
    private final JLabel textLabel;

    private JComponent additionalComponent;

    /**
     * Image shown when the step is complete.
     */
    private static ImageIcon ACCEPT_IMAGE;

    /**
     * Image shown when the step is active.
     */
    private static ImageIcon BULLET_IMAGE;

    public IconPanel(final String text) {
      this(text, null);
    }

    /**
     * Creates a new panel object.
     *
     * @param text Text that describes the step.
     */
    public IconPanel(final String text, final JComponent additionalComponent) {
      super(new BorderLayout());

      if (ACCEPT_IMAGE == null) {
        try {
          ACCEPT_IMAGE =
              new ImageIcon(CallResolverDialog.class.getResource("accept.png").toURI().toURL());
          BULLET_IMAGE = new ImageIcon(
              CallResolverDialog.class.getResource("bullet_blue.png").toURI().toURL());
        } catch (MalformedURLException | URISyntaxException e) {
          e.printStackTrace();
        }
      }

      textLabel = new JLabel(text);
      textLabel.setEnabled(false);

      add(textLabel);

      if (additionalComponent != null) {
        this.additionalComponent = additionalComponent;
        add(additionalComponent, BorderLayout.EAST);
      }

      setPreferredSize(new Dimension(200, 20));
    }

    /**
     * Sets a flag that says whether the step is done or not.
     *
     * @param done True, if the step is done. False, if it is not.
     */
    public void setDone(final boolean done) {
      textLabel.setIcon(done ? ACCEPT_IMAGE : BULLET_IMAGE);
    }

    @Override
    public void setEnabled(final boolean enabled) {
      super.setEnabled(enabled);

      if (additionalComponent != null) {
        additionalComponent.setEnabled(enabled);
      }

      textLabel.setEnabled(enabled);
    }
  }

  /**
   * Extended call resolver class that updates the dialog on relevant events.
   */
  private class InternalCallResolver extends CallResolver {
    /**
     * Creates a new call resolver object.
     *
     * @param target The target whose calls are resolved.
     * @param parent
     */
    public InternalCallResolver(final ICallResolverTarget target, final JFrame parent) {
      super(target, parent);
    }

    @Override
    protected void debuggerChanged() {
      appendOutput("Error: Target debugger changed. Resetting.");

      updateGui();
    }

    @Override
    protected void debuggerClosed() {
      appendOutput("Target debugger was closed.");

      updateGui();
    }

    @Override
    protected void errorConnectingDebugger(final DebugException e) {
      appendOutput("Error: Could not start the debugger.");
    }

    @Override
    protected void errorLoadingModule(final Module module, final CouldntLoadDataException e) {
      appendOutput(
          String.format("Error loading module '%s' (%s)", module.getName(), e.getMessage()));
    }

    @Override
    protected void errorNoDebugger() {
      appendOutput("Error: No debugger configured for the selected target.");
    }

    @Override
    protected void errorNotAttached() {
      appendOutput("Error: The debugger is not attached to the target process.");
    }

    @Override
    protected void errorResuming(final DebugException e) {
      appendOutput("Error: Debugger could not be resumed after a breakpoint was hit.");
    }

    @Override
    protected void foundIndirectCallAddresses(final List<IndirectCall> indirectCallAddresses) {
      appendOutput(String.format("Found %d indirect calls", indirectCallAddresses.size()));

      if (indirectCallAddresses.isEmpty()) {
        appendOutput("No indirect function calls found: The resolving process is complete");
      }
    }

    @Override
    protected void resolvedCall(
        final BigInteger lastIndirectCall, final ResolvedFunction resolvedFunction) {
      final Function function = resolvedFunction.getFunction();

      final Address functionAddress =
          function == null ? resolvedFunction.getAddress() : function.getAddress();
      final String functionName =
          function == null ? resolvedFunction.getMemoryModule().getName() + "!???"
              : function.getModule().getName() + "!" + function.getName();

      appendOutput(String.format("Done resolving: %08X -> %08X (%s)", lastIndirectCall.longValue(),
          functionAddress.toLong(), functionName));
    }
  }

  /**
   * The upper part of the dialog that contains the progress labels and the results buttons.
   */
  private class LabelPanel extends JPanel {

    public LabelPanel() {
      super(new GridLayout(panels.length + 1, 1));

      for (final IconPanel panel : panels) {
        add(panel);
      }

      final JPanel resultsPanel = new JPanel();

      resultsPanel.add(listResolvedFunctionsButton);
      resultsPanel.add(graphResolvedFunctionsButton);
      resultsPanel.add(portResultsButton);

      add(resultsPanel);

      setBorder(new TitledBorder(""));
    }
  }

  /**
   * Action class used to show the resolved functions in the output list.
   */
  private class ListResolvedFunctionsAction extends AbstractAction {

    public ListResolvedFunctionsAction() {
      super("Show resolved functions");
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      listResolvedFunctions();
    }
  }

  /**
   * Action class that handles clicks on the Next button.
   */
  private class NextAction extends AbstractAction {

    /**
     * Creates a new action object.
     */
    public NextAction() {
      super("Next");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      callResolver.next();

      updateGui();
    }
  }

  /**
   * The part of the panel that contains the output field.
   */
  private class OutputPanel extends JPanel {

    public OutputPanel() {
      super(new BorderLayout());

      outputArea.setEditable(false);
      outputArea.setFont(GuiHelper.MONOSPACED_FONT);

      add(new JScrollPane(outputArea));
    }
  }

  /**
   * Action class for the Resume button.
   */
  private class ResumeAction extends AbstractAction {

    public ResumeAction() {
      super("Resume");
    }

    @Override
    public void actionPerformed(final ActionEvent event) {
      try {
        callResolver.getTarget().getDebugger().resume();
      } catch (final DebugException exception) {
        appendOutput(
            String.format("Error: Could not resume the debugger (%s)", exception.toString()));
      }
    }
  }

  /**
   * Button for resuming the debugger.
   */
  private class ResumeButton extends JButton {

    public ResumeButton() {
      super(new ResumeAction());

      setPreferredSize(new Dimension(100, 20));
    }
  }
}
