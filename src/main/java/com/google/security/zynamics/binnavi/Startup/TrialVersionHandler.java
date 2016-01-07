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
package com.google.security.zynamics.binnavi.Startup;

import com.google.security.zynamics.binnavi.Resources.Constants;
import com.google.security.zynamics.zylib.date.DateHelpers;
import com.google.security.zynamics.zylib.gui.CMessageBox;

import java.awt.Window;
import java.util.GregorianCalendar;



/**
 * Handles trial versions expiry dates.
 */
public final class TrialVersionHandler {
  /**
   * If this is a valid date, then BinNavi turns into a trial version that expires at the given
   * date.
   */
  private static final GregorianCalendar EXPIRATION_DATE = null ;

  /**
   * You are not supposed to instantiate this class.
   */
  private TrialVersionHandler() {
  }

  /**
   * Checks whether the trial version has expired.
   * 
   * @return True, if the trial version has expired. False, otherwise.
   */
  private static boolean hasExpired() {
    if (EXPIRATION_DATE == null) {
      return false;
    }

    return DateHelpers.getCurrentDate().after(EXPIRATION_DATE.getTime());
  }

  /**
   * Checks whether the current version of BinNavi is a beta version.
   * 
   * @return True, if the BinNavi version is a beta version.
   */
  private static boolean isBetaVersion() {
    return Constants.PROJECT_NAME_VERSION.toLowerCase().contains("beta");
  }

  /**
   * Handles everything that is necessary for expired and non-expired evaluation version.
   * 
   * @param parent Parent window used for dialogs.
   * 
   * @return True, if the evaluation version is still valid. False, if the trial period has expired.
   */
  public static boolean handleEvaluationVersion(final Window parent) {
    if (isBetaVersion() && hasExpired()) {
      CMessageBox.showInformation(parent,
          String.format("Your beta version of %s has expired.", Constants.PROJECT_NAME_VERSION));
      return false;
    }

    return true;
  }
}
