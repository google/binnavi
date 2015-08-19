/*
Copyright 2015 Google Inc. All Rights Reserved.

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
package com.google.security.zynamics.binnavi.Gui.ErrorDialog.Implementations;



import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.CProgressDialog;
import com.google.security.zynamics.binnavi.Gui.MainWindow.Implementations.CSettingsDialogFunctions;
import com.google.security.zynamics.binnavi.config.ConfigManager;
import com.google.security.zynamics.binnavi.config.GeneralSettingsConfigItem;
import com.google.security.zynamics.zylib.general.StackTrace;
import com.google.security.zynamics.zylib.gui.CMessageBox;
import com.google.security.zynamics.zylib.gui.ProgressDialogs.CEndlessHelperThread;
import com.google.security.zynamics.zylib.net.WebsiteReader;

import java.awt.Desktop;
import java.awt.Window;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JOptionPane;



/**
 * This class contains the concrete implementations for the actions available in the error dialog.
 */
public final class CErrorDialogFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CErrorDialogFunctions() {
  }

  /**
   * Opens the Bugtracker URL.
   * 
   * @param parent Parent window used for dialogs.
   */
  public static void reportBug(final Window parent) {
    // parent can be null because errors appear before windows are created.

    try {
      Desktop.getDesktop().browse(new URL("http://bugs.zynamics.com/BinNavi").toURI());
    } catch (final Exception e) {
      CUtilityFunctions.logException(e);
      CMessageBox.showError(parent, "Could not open the bugtracker URL in the browser.");
    }
  }

  /**
   * Automatically sends error details to the BinNavi bug tracker.
   * 
   * @param owner Parent window used for dialogs.
   * @param description Short description of the error.
   * @param message Detailed message of the error.
   * @param exception Exception argument that contains the stack trace. This argument can be null.
   */
  public static void send(final Window owner, final String description, final String message,
      final Throwable exception) {
    final GeneralSettingsConfigItem globalSettings = ConfigManager.instance().getGeneralSettings();
    if ("".equals(globalSettings.getSupportEmailAddress())
        && (JOptionPane.YES_OPTION == CMessageBox.showYesNoQuestion(owner,
            "It is recommended to configure an email address when reporting bugs.\n"
                + "This makes it possible to get back to you when we found a solution for your"
                + " problem.\n\nDo you want to configure an email address now?"))) {
      CSettingsDialogFunctions.showSettingsDialog(owner);
    }

    final String email = globalSettings == null ? "" : globalSettings.getSupportEmailAddress();

    final SendThread thread = new SendThread(email, description, message, exception);

    CProgressDialog.showEndless(owner, "Sending error details ...", thread);

    if (thread.getException() == null) {
      CMessageBox.showInformation(owner,
          "Successfully submitted the error details to the BinNavi issue tracker. "
              + "Someone from the BinNavi support team will contact you soon.");
    } else {
      CUtilityFunctions.logException(thread.getException());

      CMessageBox.showError(owner,
          "The error details could not be submitted to the BinNavi issue tracker. "
              + "Please try to submit the data manually.");
    }
  }

  /**
   * Thread used to display a progress dialog while sending error details to the bug tracker.
   */
  private static class SendThread extends CEndlessHelperThread {
    /**
     * Email Address of the correspondent.
     */
    private final String m_email;

    /**
     * Short description of the error.
     */
    private final String m_description;

    /**
     * Detailed message of the error.
     */
    private final String m_message;

    /**
     * Exception argument that contains the stack trace. This argument can be null.
     */
    private final Throwable m_exception;

    /**
     * Creates a new SendThread object.
     * 
     * @param email Email Address of the correspondent.
     * @param description Short description of the error.
     * @param message Detailed message of the error.
     * @param exception Exception argument that contains the stack trace. This argument can be null.
     */
    public SendThread(final String email, final String description, final String message,
        final Throwable exception) {
      m_email = email;
      m_description = description;
      m_message = message;
      m_exception = exception;
    }

    @Override
    protected void runExpensiveCommand() throws Exception {
      final String urlString = "http://bugs.zynamics.com/BinNavi/index.php";

      final String content =
          m_description + "\n\n"
              + (m_exception == null ? "" : StackTrace.toString(m_exception.getStackTrace()));

      final String data =
          "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(m_email, "UTF-8")
              + "&" + URLEncoder.encode("subject", "UTF-8") + "="
              + URLEncoder.encode(m_message + " (" + m_email + ")", "UTF-8") + "&"
              + URLEncoder.encode("body", "UTF-8") + "=" + URLEncoder.encode(content, "UTF-8");

      WebsiteReader.sendPost(urlString, data);
    }
  }
}
