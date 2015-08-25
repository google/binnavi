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
package com.google.security.zynamics.binnavi.Database;

import com.google.common.base.Preconditions;
import com.google.security.zynamics.zylib.general.Convert;

/**
 * Class that describes the version of a BinNavi database.
 */
public final class DatabaseVersion {
  /**
   * Version string.
   */
  private final String version;

  /**
   * Creates a new version object.
   * 
   * @param version Version string.
   */
  public DatabaseVersion(final String version) {
    Preconditions.checkArgument(isValidVersionNumber(version),
        "IE00707: Invalid database version string: " + version);
    this.version = version;
  }

  /**
   * Checks whether a given version string is a valid version string.
   * 
   * @param version The version string to check.
   * 
   * @return True, if the version string is valid. False, otherwise.
   */
  private static boolean isValidVersionNumber(final String version) {
    if (version == null) {
      return false;
    }

    final String[] parts = version.split("\\.");

    if (parts.length != 3) {
      return false;
    }

    for (final String part : parts) {
      if (!Convert.isDecString(part)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Compares a version number to the version number of the current
   * com.google.security.zynamics.binnavi.
   * 
   * @param version The version number to compare to.
   * 
   * @return Returns 0 if the version numbers are equal. Returns -1 if dbVersion is less than the
   *         BinNavi version. Returns 1 if the dbVersion is greater than the BinNavi version.
   */
  public int compareTo(final DatabaseVersion version) {
    // Each BinNavi version is made from three parts:
    // - Main Version Number
    // - Sub Version Number
    // - Build Version number

    final String[] naviSplit = this.version.split("\\.");

    final String[] dbSplit = version.getString().split("\\.");

    // Compare the main versions first
    final int mainVersionNavi = Integer.parseInt(naviSplit[0]);
    final int mainVersionDB = Integer.parseInt(dbSplit[0]);

    if (mainVersionNavi != mainVersionDB) {
      return mainVersionNavi - mainVersionDB;
    }

    // Compare the sub versions next
    final int subVersionNavi = Integer.parseInt(naviSplit[1]);
    final int subVersionDB = Integer.parseInt(dbSplit[1]);

    if (subVersionNavi != subVersionDB) {
      return subVersionNavi - subVersionDB;
    }

    // Compare the build versions last
    final int buildVersionNavi = Integer.parseInt(naviSplit[2]);
    final int buildVersionDB = Integer.parseInt(dbSplit[2]);

    return buildVersionNavi - buildVersionDB;
  }

  @Override
  public boolean equals(final Object rhs) {
    return (rhs instanceof DatabaseVersion) && (version.equals(((DatabaseVersion) rhs).version));
  }

  /**
   * Returns the version string.
   * 
   * @return The version string.
   */
  public String getString() {
    return version;
  }

  @Override
  public int hashCode() {
    return version.hashCode();
  }

  /**
   * Checks whether a given database version needs upgrading.
   * 
   * @param fromVersion The database version to check.
   * 
   * @return True, if the given database version needs to be upgraded.
   */
  public boolean needsUpgrading(final DatabaseVersion fromVersion) {
    return fromVersion.getString().equals("4.0.0") || fromVersion.getString().equals("5.0.0");
  }
}
