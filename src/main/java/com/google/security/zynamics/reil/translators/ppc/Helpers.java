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
package com.google.security.zynamics.reil.translators.ppc;

/**
 * Collection of helper functions for translating ppc code to REIL code.
 */
public class Helpers {

  /**
   * CR0LT constant to be used in REIL code
   */
  public static final String CR0_LESS_THEN = "CR0LT";

  /**
   * CR0GT constant to be used in REIL code
   */
  public static final String CR0_GREATER_THEN = "CR0GT";

  /**
   * CR0EQ constant to be used in REIL code
   */
  public static final String CR0_EQUAL = "CR0EQ";

  /**
   * CR0SO constant to be used in REIL code
   */
  public static final String CRO_SUMMARY_OVERFLOW = "CR0SO";

  /**
   * XERSO constant to be used in REIL code
   */
  public static final String XER_SUMMARY_OVERFLOW = "XERSO";

  /**
   * XEROV constant to be used in REIL code
   */
  public static final String XER_OVERFLOW = "XEROV";

  /**
   * XERCA constant to be used in REIL code
   */
  public static final String XER_CARRY_BIT = "XERCA";

  /**
   * XERBC constant to be used in REIL code
   */
  public static final String XER_COUNT_REGISTER = "XERBC";

  /**
   * LR constant to be used in REIL code
   */
  public static final String LINK_REGISTER = "lr";

  /**
   * CTR constant to be used in REIL code
   */
  public static final String COUNT_REGISTER = "ctr";

  /**
   * Get Flag name by index
   */
  public static String getCRBit(final int index) {
    switch (index) {
      case 0:
        return "CR0LT";
      case 1:
        return "CR0GT";
      case 2:
        return "CR0EQ";
      case 3:
        return "CR0SO";
      case 4:
        return "CR1LT";
      case 5:
        return "CR1GT";
      case 6:
        return "CR1EQ";
      case 7:
        return "CR1SO";
      case 8:
        return "CR2LT";
      case 9:
        return "CR2GT";
      case 10:
        return "CR2EQ";
      case 11:
        return "CR2SO";
      case 12:
        return "CR3LT";
      case 13:
        return "CR3GT";
      case 14:
        return "CR3EQ";
      case 15:
        return "CR3SO";
      case 16:
        return "CR4LT";
      case 17:
        return "CR4GT";
      case 18:
        return "CR4EQ";
      case 19:
        return "CR4SO";
      case 20:
        return "CR5LT";
      case 21:
        return "CR5GT";
      case 22:
        return "CR5EQ";
      case 23:
        return "CR5SO";
      case 24:
        return "CR6LT";
      case 25:
        return "CR6GT";
      case 26:
        return "CR6EQ";
      case 27:
        return "CR6SO";
      case 28:
        return "CR7LT";
      case 29:
        return "CR7GT";
      case 30:
        return "CR7EQ";
      case 31:
        return "CR7SO";
      default:
        return "";
    }
  }

  /**
   * Generate CRM Mask
   */
  public static String getCRM(final int crm) {
    Long mask = 0L;
    for (int i = 0; i < 8; i++) {
      mask = (mask << 4);
      if (((crm >> (7 - i)) & 1) == 1) {
        mask |= 15;
      }
    }
    return mask.toString();
  }

  /**
   * Get the CR register index
   * 
   * @param register as "cr5"
   * @return index as 5
   */
  public static int getCRRegisterIndex(final String register) {
    Integer retval = 0;
    try {
      retval = Integer.decode(register);
    } catch (final NumberFormatException e) {
      final String registerNumber = register.substring(2);
      retval = Integer.decode(registerNumber);
    }
    return retval;
  }

  /**
   * Return the register name that is used in REIL
   */
  public static String getRealRegisterName(final String register) {
    String name = "";
    if (register.startsWith("%")) {
      name = register;
    } else {
      name = "%" + register;
    }
    if ((register.equals("rtoc")) || (register.equals("%rtoc"))) {
      name = "%r2";
    } else if ((register.equals("sp")) || (register.equals("%sp"))) {
      name = "%r1";
    }
    return name;
  }

  /**
   * Get the register index
   */
  public static int getRegisterIndex(final String register) {
    final String registerNumber = getRealRegisterName(register).substring(2);
    return Integer.decode(registerNumber);
  }

  /**
   * Generate Rotate Mask
   */
  public static String getRotateMask(final String MB, final String ME) {
    Long mask = 0L;
    final int mb = Integer.decode(MB);
    final int me = Integer.decode(ME);
    if ((mb == 0) && (me == 31)) {
      return String.valueOf(0xFFFFFFFFL);
    }
    for (int i = mb; i != ((me + 1) % 32); i = (i + 1) % 32) {
      mask |= ((mask >> (31 - i)) | 1) << (31 - i);
    }
    return mask.toString();
  }
}
