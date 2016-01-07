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
package com.google.security.zynamics.binnavi.API.reil;

import com.google.security.zynamics.reil.ReilHelpers;

// ! Contains constants for all native REIL mnemonics.
/**
 * Contains constants for all native REIL mnemonics. These constants should be used when creating
 * new REIL instructions or when comparing the mnemonics of REIL instructions during analysis
 * algorithms.
 */
public final class ReilMnemonics {
  // ! Standard ADD mnemonic.
  /**
   * Standard ADD (addition) mnemonic.
   */
  public static final String ADD = ReilHelpers.OPCODE_ADD;

  // ! Standard AND mnemonic.
  /**
   * Standard AND (bitwise AND) mnemonic.
   */
  public static final String AND = ReilHelpers.OPCODE_AND;

  // ! Standard BISZ mnemonic.
  /**
   * Standard BISZ (boolean is zero?) mnemonic.
   */
  public static final String BISZ = ReilHelpers.OPCODE_BISZ;

  // ! Standard BSH mnemonic.
  /**
   * Standard BSH (Bitwise Shift) mnemonic.
   */
  public static final String BSH = ReilHelpers.OPCODE_BSH;

  // ! Standard DIV mnemonic.
  /**
   * Standard DIV (unsigned division) mnemonic.
   */
  public static final String DIV = ReilHelpers.OPCODE_DIV;

  // ! Standard JCC mnemonic.
  /**
   * Standard JCC (conditional jump) mnemonic.
   */
  public static final String JCC = ReilHelpers.OPCODE_JCC;

  // ! Standard LDM mnemonic.
  /**
   * Standard LDM (load from memory) mnemonic.
   */
  public static final String LDM = ReilHelpers.OPCODE_LDM;

  // ! Standard MOD mnemonic.
  /**
   * Standard MOD (unsigned modulo) mnemonic.
   */
  public static final String MOD = ReilHelpers.OPCODE_MOD;

  // ! Standard MUL mnemonic.
  /**
   * Standard MUL (unsigned multiplication) mnemonic.
   */
  public static final String MUL = ReilHelpers.OPCODE_MUL;

  // ! Standard NOP mnemonic.
  /**
   * Standard NOP (no operation) mnemonic.
   */
  public static final String NOP = ReilHelpers.OPCODE_NOP;

  // ! Standard OR mnemonic.
  /**
   * Standard OR (bitwise OR) mnemonic.
   */
  public static final String OR = ReilHelpers.OPCODE_OR;

  // ! Standard STM mnemonic.
  /**
   * Standard STM (store to memory) mnemonic.
   */
  public static final String STM = ReilHelpers.OPCODE_STM;

  // ! Standard STR mnemonic.
  /**
   * Standard STR (store to register) mnemonic.
   */
  public static final String STR = ReilHelpers.OPCODE_STR;

  // ! Standard SUB mnemonic.
  /**
   * Standard SUB (subtraction) mnemonic.
   */
  public static final String SUB = ReilHelpers.OPCODE_SUB;

  // ! Standard UNDEF mnemonic.
  /**
   * Standard UNDEF (undefine register) mnemonic.
   */
  public static final String UNDEF = ReilHelpers.OPCODE_UNDEF;

  // ! Standard UNKNOWN mnemonic.
  /**
   * Standard UNKNOWN (unknown mnemonic) mnemonic.
   */
  public static final String UNKNOWN = ReilHelpers.OPCODE_UNKNOWN;

  // ! Standard XOR mnemonic.
  /**
   * Standard XOR (bitwise XOR) mnemonic.
   */
  public static final String XOR = ReilHelpers.OPCODE_XOR;

  /**
   * Do not instantiate this class.
   */
  private ReilMnemonics() {
    // YOu are not supposed to instantiate this class.
  }
}
