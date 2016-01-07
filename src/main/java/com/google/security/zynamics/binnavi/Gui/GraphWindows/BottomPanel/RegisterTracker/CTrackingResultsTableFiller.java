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
package com.google.security.zynamics.binnavi.Gui.GraphWindows.BottomPanel.RegisterTracker;

import com.google.security.zynamics.zylib.strings.Commafier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Contains helper functions for filling the cells of the tracking results table.
 */
public final class CTrackingResultsTableFiller {
  /**
   * You are not supposed to instantiate this class.
   */
  private CTrackingResultsTableFiller() {
  }

  /**
   * Turns a collection of registers into a CSV string. Needed because we return collections instead
   * of Lists now...
   *
   * @param registers The registers.
   *
   * @return The CSV string that contains the registers.
   */
  private static String listify(final Collection<String> registers) {
    final List<String> registerList = new ArrayList<String>(registers);
    Collections.sort(registerList);
    return Commafier.commafy(registerList);
  }

  /**
   * Generates the text of an address column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getAddressColumnText(final CInstructionResult result) {
    return result.getInstruction().getAddress().toHexString();
  }

  /**
   * Generates the text of a Defined column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getDefinedColumnText(final CInstructionResult result) {
    return listify(result.getDefinedRegisters());
  }

  /**
   * Generates the text of an Instruction column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getInstructionColumnText(final CInstructionResult result) {
    return result.getInstruction().getInstructionString();
  }

  /**
   * Generates the text of a Reads column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getReadsColumnText(final CInstructionResult result) {
    return listify(result.getInputRegisters());
  }

  /**
   * Generates the text of an Undefines column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getUndefinesColumnText(final CInstructionResult result) {
    return listify(result.getUndefinedRegisters());
  }

  public static String getUpdatedColumnText(final CInstructionResult result) {
    return listify(result.getUpdatedRegisters());
  }

  /**
   * Generates the text of a Writes column of a tracking result.
   *
   * @param result The tracking result.
   *
   * @return The generated column text.
   */
  public static String getWritesColumnText(final CInstructionResult result) {
    return listify(result.getNewlyTaintedRegisters());
  }
}
