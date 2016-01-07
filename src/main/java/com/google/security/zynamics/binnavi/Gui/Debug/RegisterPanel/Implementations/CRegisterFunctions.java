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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel.Implementations;

import java.math.BigInteger;

import javax.swing.JFrame;

import com.google.security.zynamics.binnavi.CUtilityFunctions;
import com.google.security.zynamics.binnavi.Gui.errordialog.NaviErrorDialog;
import com.google.security.zynamics.binnavi.debug.debugger.DebugExceptionWrapper;
import com.google.security.zynamics.binnavi.debug.debugger.interfaces.IDebugger;
import com.google.security.zynamics.zylib.gui.JRegisterView.JRegisterView;



/**
 * Contains the implementations of the register actions.
 */
public final class CRegisterFunctions {
  /**
   * You are not supposed to instantiate this class.
   */
  private CRegisterFunctions() {
  }

  /**
   * Changes the value of a register in the target process.
   * 
   * @param parent Parent window used for dialogs.
   * @param debugger Debugger that is used to change the register value.
   * @param registerView Register view that shows the values.
   * @param currentTid TID of the thread where the register is changed.
   * @param registerIndex Index of the register to be changed.
   * @param value New value of the register.
   * 
   * @return Message ID of the message sent to the debug client or null the message was not sent.
   */
  public static Integer changeRegister(final JFrame parent, final IDebugger debugger,
      final JRegisterView registerView, final long currentTid, final int registerIndex,
      final BigInteger value) {
    try {
      registerView.setEnabled(false);

      return debugger.setRegister(currentTid, registerIndex, value);
    } catch (final DebugExceptionWrapper exception) {
      registerView.setEnabled(true);

      CUtilityFunctions.logException(exception);
      final String innerMessage = "E00105: " + "Could not send register change request";
      final String innerDescription =
          CUtilityFunctions
              .createDescription(
                  "BinNavi could not send the request to change the register value to the debug client.",
                  new String[] {"There was a problem with the connection to the debug client."},
                  new String[] {"The value of the register did not change."});

      NaviErrorDialog.show(parent, innerMessage, innerDescription, exception);

      return null;
    }
  }
}
