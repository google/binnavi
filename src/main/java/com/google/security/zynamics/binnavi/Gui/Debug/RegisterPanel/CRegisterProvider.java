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
package com.google.security.zynamics.binnavi.Gui.Debug.RegisterPanel;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterDescription;
import com.google.security.zynamics.binnavi.debug.models.targetinformation.RegisterValue;
import com.google.security.zynamics.zylib.general.ListenerProvider;
import com.google.security.zynamics.zylib.gui.JRegisterView.IRegisterModel;
import com.google.security.zynamics.zylib.gui.JRegisterView.IRegistersChangedListener;
import com.google.security.zynamics.zylib.gui.JRegisterView.RegisterInformationInternal;

import java.math.BigInteger;
import java.util.List;

/**
 * Provides data for the debugger GUI components. For example, the hex data and the register data
 * shown by the debugger GUI comes from here.
 */
public final class CRegisterProvider implements IRegisterModel {
  /**
   * Listeners that are notified about changes in the register values that come from the debug
   * client.
   */
  private final ListenerProvider<IRegistersChangedListener> reglisteners =
      new ListenerProvider<IRegistersChangedListener>();

  /**
   * Listeners that are notified about changes in the register values that come from user input in
   * the GUI.
   */
  private final ListenerProvider<IDataEnteredListener> enterlisteners =
      new ListenerProvider<IDataEnteredListener>();

  /**
   * Register information that is currently displayed in the register view.
   */
  private RegisterInformationInternal[] registerInformation = new RegisterInformationInternal[0];

  /**
   * Gives information about the registers shown in the view.
   */
  private List<RegisterDescription> m_information = null;

  /**
   * Makes sure to highlight a register if its value changed between the last update and the current
   * update.
   *
   * @param counter Index of the register.
   * @param registerValue New value of the register.
   * @param oldRegisterInformation Register information from previous update.
   * @param newRegisterInformation Register information from current update.
   */
  private static void highlightChangedRegister(final int counter,
      final RegisterValue registerValue,
      final RegisterInformationInternal[] oldRegisterInformation,
      final RegisterInformationInternal[] newRegisterInformation) {
    if (counter < oldRegisterInformation.length) {
      // Highlight if the new value is different from
      // the old value of the register.
      if (!oldRegisterInformation[counter].getValue().equals(registerValue.getValue())) {
        newRegisterInformation[counter].setModified(true);
      }
    } else {
      // If the register is new, highlight it.
      newRegisterInformation[counter].setModified(true);
    }
  }

  /**
   * Returns the index of a register identified by name.
   *
   * @param registerName Name of the register.
   *
   * @return The index of the specified register or -1 if no such register exists.
   */
  private int findRegisterIndex(final String registerName) {
    int counter = 0;

    for (final RegisterInformationInternal info : registerInformation) {
      if (info.getRegisterName().equals(registerName)) {
        return counter;
      }

      ++counter;
    }

    return -1;
  }

  /**
   * Returns the register description of a register identified by a name.
   *
   * @param name The name of the register.
   *
   * @return The register description of the register or null if the register is unknown.
   */
  private RegisterDescription getDescription(final String name) {
    for (final RegisterDescription description : m_information) {
      if (description.getName().equals(name)) {
        return description;
      }
    }

    return null;
  }

  /**
   * Notifies the register listeners about changes in the register values.
   */
  private void notifyRegisterChanged() {
    for (final IRegistersChangedListener listener : reglisteners) {
      listener.registerDataChanged();
    }
  }

  /**
   * Notifies listeners about changes in the register values that came from the GUI (aka user
   * input).
   *
   * @param index Index of the register.
   * @param previousValue Old value of the register.
   * @param newValue New value of the register.
   */
  private void notifyRegisterEntered(
      final int index, final BigInteger previousValue, final BigInteger newValue) {
    for (final IDataEnteredListener listener : enterlisteners) {
      listener.registerChanged(index, previousValue, newValue);
    }
  }

  /**
   * Adds a listener that is notified when the user enters register values through the GUI.
   *
   * @param listener The listener that is notified when the user enters register values.
   */
  public void addListener(final IDataEnteredListener listener) {
    enterlisteners.addListener(listener);
  }

  @Override
  public void addListener(final IRegistersChangedListener listener) {
    reglisteners.addListener(listener);
  }

  @Override
  public int getNumberOfRegisters() {
    return registerInformation.length;
  }

  @Override
  public RegisterInformationInternal[] getRegisterInformation() {
    return registerInformation.clone();
  }

  @Override
  public RegisterInformationInternal getRegisterInformation(final int index) {
    return registerInformation[index];
  }

  /**
   * Changes the register description information.
   *
   * @param information The new register information.
   */
  public void setRegisterDescription(final List<RegisterDescription> information) {
    m_information = information;
  }

  /**
   * Updates the register information.
   *
   * @param information The new register information to display.
   */
  public void setRegisterInformation(final List<RegisterValue> information) {
    Preconditions.checkNotNull(information, "IE01475: Information argument can not be null");

    if (!information.isEmpty() && (m_information == null)) {
      throw new IllegalStateException(
          "IE01124: Can not set register values if no target information is given");
    }

    final RegisterInformationInternal[] oldRegisterInformation = registerInformation;

    final RegisterInformationInternal[] newRegisterInformation =
        new RegisterInformationInternal[information.size()];

    int counter = 0;

    if (!information.isEmpty()) {
      Preconditions.checkNotNull(
          m_information, "IE01125: Target information should not be null at this point");

      for (final RegisterValue registerValue : information) {
        final RegisterDescription regInfo = getDescription(registerValue.getName());

        Preconditions.checkNotNull(regInfo, "IE01476: Unknown register");

        newRegisterInformation[counter] =
            new RegisterInformationInternal(regInfo.getName(), regInfo.getSize());
        newRegisterInformation[counter].setValue(registerValue.getValue());

        // Make sure to highlight modified register values.
        highlightChangedRegister(
            counter, registerValue, oldRegisterInformation, newRegisterInformation);

        counter++;
      }
    }

    registerInformation = newRegisterInformation;

    notifyRegisterChanged();
  }

  @Override
  public void setValue(final String registerName, final BigInteger editValue) {
    // This function overwrites register values with a new value. Listeners are
    // notified about the change and receive the old value and the new value of
    // the register to roll back the changes if necessary.

    final int index = findRegisterIndex(registerName);

    if ((index == -1) || (index >= registerInformation.length)) {
      // Fail silently if the index is out of bounds
      return;
    }

    // Save the old value of the register
    final BigInteger oldValue = registerInformation[index].getValue();

    // Update the register with the new value
    registerInformation[index].setValue(editValue);

    // Notify the listeners about the change
    notifyRegisterEntered(index, oldValue, editValue);
  }
}
