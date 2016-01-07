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
package com.google.security.zynamics.binnavi.debug.connection;

/**
 * Class that contains identifiers for all commands that are sent between the debug client and
 * com.google.security.zynamics.binnavi.
 */
public final class DebugCommandType {
  // For more information about the meaning of the individual
  // commands please refer to the documentation of the debug
  // client. This documentation is intentionally not duplicated
  // here to avoid synchronization issues between the two
  // documentations.

  /**
   * Message identifier of Set Breakpoint messages.
   */
  public static final int CMD_SETBP = 1;

  /**
   * Message identifier of Set Echo Breakpoint messages.
   */
  public static final int CMD_SETBPE = 2;

  /**
   * Message identifier of Set Step Breakpoint messages.
   */
  public static final int CMD_SETBPS = 3;

  /**
   * Message identifier of Remove Breakpoint messages.
   */
  public static final int CMD_REMBP = 4;

  /**
   * Message identifier of Remove Echo Breakpoint messages.
   */
  public static final int CMD_REMBPE = 5;

  /**
   * Message identifier of Remove Step Breakpoint messages.
   */
  public static final int CMD_REMBPS = 6;

  /**
   * Message identifier of Read Memory messages.
   */
  public static final int CMD_READ_MEMORY = 7;

  /**
   * Message identifier of Read Registers messages.
   */
  public static final int CMD_REGISTERS = 8;

  /**
   * Message identifier of Resume messages.
   */
  public static final int CMD_RESUME = 9;

  /**
   * Message identifier of Detach messages.
   */
  public static final int CMD_DETACH = 10;

  /**
   * Message identifier of replies sent after a breakpoint was hit.
   */
  public static final int RESP_BP_HIT = 14;

  /**
   * Message identifier of replies sent after an echo breakpoint was hit.
   */
  public static final int RESP_BPE_HIT = 15;

  /**
   * Message identifier of replies sent after a step breakpoint was hit.
   */
  public static final int RESP_BPS_HIT = 16;

  /**
   * Message identifier of successful replies to Read Memory messages.
   */
  public static final int RESP_READ_MEMORY_SUCCESS = 17;

  /**
   * Message identifier of successful replies to Read Registers messages.
   */
  public static final int RESP_REGISTERS_SUCCESS = 18;

  /**
   * Message identifier of successful replies to Success messages.
   */
  public static final int RESP_RESUME_SUCCESS = 19;

  /**
   * Message identifier of unsuccessful replies to Attach messages.
   */
  public static final int RESP_ATTACH_ERROR = 23;

  /**
   * Message identifier of successful replies to Attach messages.
   */
  public static final int RESP_ATTACH_SUCCESS = 24;

  /**
   * Message identifier of successful replies to Set Breakpoint messages.
   */
  public static final int RESP_BP_SET_SUCCESS = 25;

  /**
   * Message identifier of unsuccessful replies to Set Breakpoint messages.
   */
  public static final int RESP_BP_SET_ERROR = 26;

  /**
   * Message identifier of unsuccessful replies to Resume messages.
   */
  public static final int RESP_RESUME_ERROR = 27;

  /**
   * Message identifier of successful replies to Set Echo Breakpoint messages.
   */
  public static final int RESP_BPE_SET_SUCCESS = 28;

  /**
   * Message identifier of unsuccessful replies to Set Echo Breakpoint messages.
   */
  public static final int RESP_BPE_SET_ERROR = 29;

  /**
   * Message identifier of successful replies to Set Step Breakpoint messages.
   */
  public static final int RESP_BP_REM_SUCCESS = 30;

  /**
   * Message identifier of unsuccessful replies to Remove Breakpoint messages.
   */
  public static final int RESP_BP_REM_ERROR = 31;

  /**
   * Message identifier of successful replies to Detach messages.
   */
  public static final int RESP_DETACH_SUCCESS = 32;

  /**
   * Message identifier of unsuccessful replies to Detach messages.
   */
  public static final int RESP_DETACH_ERROR = 33;

  /**
   * Message identifier of unsuccessful replies to Read Registers messages.
   */
  public static final int RESP_REGISTERS_ERROR = 34;

  /**
   * Message identifier of unsuccessful replies to Read Memory messages.
   */
  public static final int RESP_READ_MEMORY_ERROR = 35;

  /**
   * Message identifier of Terminate messages.
   */
  public static final int CMD_TERMINATE = 36;

  /**
   * Message identifier of successful replies to Terminate messages.
   */
  public static final int RESP_TERMINATE_SUCCESS = 37;

  /**
   * Message identifier of unsuccessful replies to Terminate messages.
   */
  public static final int RESP_TERMINATE_ERROR = 38;

  /**
   * Message identifier of successful replies to Remove Echo Breakpoint messages.
   */
  public static final int RESP_BPE_REM_SUCCESS = 39;

  /**
   * Message identifier of unsuccessful replies to Remove Echo Breakpoint messages.
   */
  public static final int RESP_BPE_REM_ERROR = 40;

  /**
   * Message identifier of successful replies to Set Step Breakpoint messages.
   */
  public static final int RESP_BPS_SET_SUCCESS = 41;

  /**
   * Message identifier of unsuccessful replies to Set Step Breakpoint messages.
   */
  public static final int RESP_BPS_SET_ERROR = 42;

  /**
   * Message identifier of successful replies to Remove Step Breakpoint messages.
   */
  public static final int RESP_BPS_REM_SUCCESS = 43;

  /**
   * Message identifier of unsuccessful replies to Remove Step Breakpoint messages.
   */
  public static final int RESP_BPS_REM_ERROR = 44;

  /**
   * Message identifier used when Target Information is sent by the debug client.
   */
  public static final int RESP_INFO = 45;

  /**
   * Message identifier of Set Register messages.
   */
  public static final int CMD_SET_REGISTER = 46;

  /**
   * Message identifier of successful replies to Set Register messages.
   */
  public static final int RESP_SET_REGISTER_SUCCESS = 47;

  /**
   * Message identifier of unsuccessful replies to Set Register messages.
   */
  public static final int RESP_SET_REGISTER_ERROR = 48;

  /**
   * Message identifier of Single Step messages.
   */
  public static final int CMD_SINGLE_STEP = 49;

  /**
   * Message identifier of successful replies to Single Step messages.
   */
  public static final int RESP_SINGLE_STEP_SUCCESS = 50;

  /**
   * Message identifier of unsuccessful replies to Single Step messages.
   */
  public static final int RESP_SINGLE_STEP_ERROR = 51;

  /**
   * Message identifier of Validate Memory messages.
   */
  public static final int CMD_VALID_MEMORY = 52;

  /**
   * Message identifier of successful replies to Validate Memory messages.
   */
  public static final int RESP_VALID_MEMORY_SUCCESS = 53;

  /**
   * Message identifier of unsuccessful replies to Validate Memory messages.
   */
  public static final int RESP_VALID_MEMORY_ERROR = 54;

  /**
   * Message identifier of messages sent after a new thread was created in the target process.
   */
  public static final int RESP_THREAD_CREATED = 55;

  /**
   * Message identifier of messages sent after a thread of the target process was closed.
   */
  public static final int RESP_THREAD_CLOSED = 56;

  /**
   * Message identifier of Search Memory messages.
   */
  public static final int CMD_SEARCH = 57;

  /**
   * Message identifier of successful replies to Search messages.
   */
  public static final int RESP_SEARCH_SUCCESS = 58;

  /**
   * Message identifier of unsuccessful replies to Search messages.
   */
  public static final int RESP_SEARCH_ERROR = 59;

  /**
   * Message identifier of Retrieve Memory Map messages.
   */
  public static final int CMD_MEMMAP = 60;

  /**
   * Message identifier of successful replies to Memory Map messages.
   */
  public static final int RESP_MEMMAP_SUCCESS = 61;

  /**
   * Message identifier of unsuccessful replies to Memory Map messages.
   */
  public static final int RESP_MEMMAP_ERROR = 62;

  /**
   * Message identifier of messages sent after the target process was closed.
   */
  public static final int RESP_PROCESS_CLOSED = 63;

  /**
   * Message identifier of messages sent after an exception occurred in the target process.
   */
  public static final int RESP_EXCEPTION_OCCURED = 64;

  /**
   * Message identifier of Halt messages.
   */
  public static final int CMD_HALT = 65;

  /**
   * Message identifier of successful replies to Halt messages.
   */
  public static final int RESP_HALTED_SUCCESS = 66;

  /**
   * Message identifier of unsuccessful replies to Halt messages.
   */
  public static final int RESP_HALTED_ERROR = 67;

  /**
   * Message identifier of messages sent to tell BinNavi that no debug target was selected on the
   * debug client side.
   */
  public static final int RESP_REQUEST_TARGET = 68;

  /**
   * Message identifier of List Processes messages.
   */
  public static final int CMD_LIST_PROCESSES = 69;

  /**
   * Message identifier of successful replies to List Processes messages.
   */
  public static final int RESP_LIST_PROCESSES_SUCCESS = 70;

  /**
   * Message identifier of Cancel Target Selection messages.
   */
  public static final int CMD_CANCEL_TARGET_SELECTION = 71;

  /**
   * Message identifier of successful replies to Cancel Target Selection messages.
   */
  public static final int RESP_CANCEL_TARGET_SELECTION_SUCCESS = 72;

  /**
   * Message identifier of Select Process messages.
   */
  public static final int CMD_SELECT_PROCESS = 73;

  /**
   * Message identifier of successful replies to Select Process messages.
   */
  public static final int RESP_SELECT_PROCESS_SUCCESS = 74;

  /**
   * Message identifier of unsuccessful replies to Select Process messages.
   */
  public static final int RESP_SELECT_PROCESS_ERROR = 75;

  /**
   * Message identifier of List File Information messages.
   */
  public static final int CMD_LIST_FILES = 76;

  /**
   * Message identifier of List Path-specific File Information messages.
   */
  public static final int CMD_LIST_FILES_PATH = 77;

  /**
   * Message identifier of successful replies to List Files messages.
   */
  public static final int RESP_LIST_FILES_SUCCESS = 78;

  /**
   * Message identifier of unsuccessful replies to List Files messages.
   */
  public static final int RESP_LIST_FILES_ERROR = 79;

  /**
   * Message identifier of Select Target File messages.
   */
  public static final int CMD_SELECT_FILE = 80;

  /**
   * Message identifier of successful replies to Select File messages.
   */
  public static final int RESP_SELECT_FILE_SUCC = 81;

  /**
   * Message identifier of unsuccessful replies to Select Files messages.
   */
  public static final int RESP_SELECT_FILE_ERR = 82;

  /**
   * Message identifier of messages sent after a new module was loaded into the target process.
   */
  public static final int RESP_MODULE_LOADED = 83;

  /**
   * Message identifier of messages sent after a module was unloaded from the target process.
   */
  public static final int RESP_MODULE_UNLOADED = 84;

  /**
   * Message identifier of commands sent to resume a single thread.
   */
  public static final int CMD_RESUME_THREAD = 85;

  /**
   * Message identifier of messages sent after a thread was resumed.
   */
  public static final int RESP_RESUME_THREAD_SUCC = 86;

  /**
   * Message identifier of messages sent after a thread failed to resume.
   */
  public static final int RESP_RESUME_THREAD_ERR = 87;

  /**
   * Message identifier of commands sent to suspend a single thread.
   */
  public static final int CMD_SUSPEND_THREAD = 88;

  /**
   * Message identifier of messages sent after a thread was suspended.
   */
  public static final int RESP_SUSPEND_THREAD_SUCC = 89;

  /**
   * Message identifier of messages sent after a thread failed to suspend.
   */
  public static final int RESP_SUSPEND_THREAD_ERR = 90;

  /**
   * Message identifier of commands sent to change the active thread.
   */
  public static final int CMD_SET_ACTIVE_THREAD = 91;

  /**
   * Message identifier of messages sent after the active thread was changed.
   */
  public static final int RESP_SET_ACTIVE_THREAD_SUCC = 92;

  /**
   * Message identifier of messages sent after the active thread could not be changed.
   */
  public static final int RESP_SET_ACTIVE_THREAD_ERR = 93;

  /**
   * Message identifier of commands sent to set breakpoint conditions.
   */
  public static final int CMD_SET_BREAKPOINT_CONDITION = 94;

  /**
   * Message identifier of messages sent after setting breakpoint conditions.
   */
  public static final int RESP_SET_BREAKPOINT_CONDITION_SUCC = 95;

  /**
   * Message identifier of messages sent after setting a breakpoint condition failed.
   */
  public static final int RESP_SET_BREAKPOINT_CONDITION_ERR = 96;

  /**
   * Message identifier of messages sent to write target process memory.
   */
  public static final int CMD_WRITE_MEMORY = 97;

  /**
   * Message identifier of messages received after writing target process memory was successful.
   */
  public static final int RESP_WRITE_MEMORY_SUCC = 98;

  /**
   * Message identifier of messages received after writing target process memory failed.
   */
  public static final int RESP_WRITE_MEMORY_ERR = 99;

  /**
   * Message identifier of messages sent to specify exceptions settings.
   */
  public static final int CMD_SET_EXCEPTIONS = 100;

  /**
   * Message identifier of messages received after the set exceptions command was successful.
   */
  public static final int RESP_SET_EXCEPTIONS_SUCC = 101;

  /**
   * Message identifier of messages received after the set exceptions command failed.
   */
  public static final int RESP_SET_EXCEPTIONS_ERR = 102;

  /**
   * Message identifier of messages sent to specify whether the debugger should be halted when a dll
   * is loaded.
   */
  public static final int CMD_SET_DEBUGGER_EVENT_SETTINGS = 103;

  /**
   * Message identifier of messages received if the set event settings command was successful.
   */
  public static final int RESP_SET_DEBUGGER_EVENT_SETTINGS_SUCC = 104;

  /**
   * Message identifier of messages received if the set event settings command could not be applied.
   */
  public static final int RESP_SET_DEBUG_EVENT_SETTINGS_ERR = 105;

  /**
   * Message identifier of query messages received when the process starts and asks BinNavi for the
   * debugger event settings.
   */
  public static final int RESP_QUERY_DEBUGGER_EVENT_SETTINGS = 106;

  /**
   * Message identifier of messages received when the process has been started.
   */
  public static final int RESP_PROCESS_START = 107;

  public static String getMessageName(final int messageId) {
    switch (messageId) {
      case CMD_SETBP:
        return "CMD_SETBP";
      case CMD_SETBPE:
        return "CMD_SETBPE";
      case CMD_SETBPS:
        return "CMD_SETBPS";
      case CMD_REMBP:
        return "CMD_REMBP";
      case CMD_REMBPE:
        return "CMD_REMBPE";
      case CMD_REMBPS:
        return "CMD_REMBPS";
      case CMD_READ_MEMORY:
        return "CMD_READ_MEMORY";
      case CMD_REGISTERS:
        return "CMD_REGISTERS";
      case CMD_RESUME:
        return "CMD_RESUME";
      case CMD_DETACH:
        return "CMD_DETACH";
      case RESP_BP_HIT:
        return "RESP_BP_HIT";
      case RESP_BPE_HIT:
        return "RESP_BPE_HIT";
      case RESP_BPS_HIT:
        return "RESP_BPS_HIT";
      case RESP_READ_MEMORY_SUCCESS:
        return "RESP_READ_MEMORY_SUCCESS";
      case RESP_REGISTERS_SUCCESS:
        return "RESP_REGISTERS_SUCCESS";
      case RESP_RESUME_SUCCESS:
        return "RESP_RESUME_SUCCESS";
      case RESP_ATTACH_ERROR:
        return "RESP_ATTACH_ERROR";
      case RESP_ATTACH_SUCCESS:
        return "RESP_ATTACH_SUCCESS";
      case RESP_BP_SET_SUCCESS:
        return "RESP_BP_SET_SUCCESS";
      case RESP_BP_SET_ERROR:
        return "RESP_BP_SET_ERROR";
      case RESP_RESUME_ERROR:
        return "RESP_RESUME_ERROR";
      case RESP_BPE_SET_SUCCESS:
        return "RESP_BPE_SET_SUCCESS";
      case RESP_BPE_SET_ERROR:
        return "RESP_BPE_SET_ERROR";
      case RESP_BP_REM_SUCCESS:
        return "RESP_BP_REM_SUCCESS";
      case RESP_BP_REM_ERROR:
        return "RESP_BP_REM_ERROR";
      case RESP_DETACH_SUCCESS:
        return "RESP_DETACH_SUCCESS";
      case RESP_DETACH_ERROR:
        return "RESP_DETACH_ERROR";
      case RESP_REGISTERS_ERROR:
        return "RESP_REGISTERS_ERROR";
      case RESP_READ_MEMORY_ERROR:
        return "RESP_READ_MEMORY_ERROR";
      case CMD_TERMINATE:
        return "CMD_TERMINATE";
      case RESP_TERMINATE_SUCCESS:
        return "RESP_TERMINATE_SUCCESS";
      case RESP_TERMINATE_ERROR:
        return "RESP_TERMINATE_ERROR";
      case RESP_BPE_REM_SUCCESS:
        return "RESP_BPE_REM_SUCCESS";
      case RESP_BPE_REM_ERROR:
        return "RESP_BPE_REM_ERROR";
      case RESP_BPS_SET_SUCCESS:
        return "RESP_BPS_SET_SUCCESS";
      case RESP_BPS_SET_ERROR:
        return "RESP_BPS_SET_ERROR";
      case RESP_BPS_REM_SUCCESS:
        return "RESP_BPS_REM_SUCCESS";
      case RESP_BPS_REM_ERROR:
        return "RESP_BPS_REM_ERROR";
      case RESP_INFO:
        return "RESP_INFO";
      case CMD_SET_REGISTER:
        return "CMD_SET_REGISTER";
      case RESP_SET_REGISTER_SUCCESS:
        return "RESP_SET_REGISTER_SUCCESS";
      case RESP_SET_REGISTER_ERROR:
        return "RESP_SET_REGISTER_ERROR";
      case CMD_SINGLE_STEP:
        return "CMD_SINGLE_STEP";
      case RESP_SINGLE_STEP_SUCCESS:
        return "RESP_SINGLE_STEP_SUCCESS";
      case RESP_SINGLE_STEP_ERROR:
        return "RESP_SINGLE_STEP_ERROR";
      case CMD_VALID_MEMORY:
        return "CMD_VALID_MEMORY";
      case RESP_VALID_MEMORY_SUCCESS:
        return "RESP_VALID_MEMORY_SUCCESS";
      case RESP_VALID_MEMORY_ERROR:
        return "RESP_VALID_MEMORY_ERROR";
      case RESP_THREAD_CREATED:
        return "RESP_THREAD_CREATED";
      case RESP_THREAD_CLOSED:
        return "RESP_THREAD_CLOSED";
      case CMD_SEARCH:
        return "CMD_SEARCH";
      case RESP_SEARCH_SUCCESS:
        return "RESP_SEARCH_SUCCESS";
      case RESP_SEARCH_ERROR:
        return "RESP_SEARCH_ERROR";
      case CMD_MEMMAP:
        return "CMD_MEMMAP";
      case RESP_MEMMAP_SUCCESS:
        return "RESP_MEMMAP_SUCCESS";
      case RESP_MEMMAP_ERROR:
        return "RESP_MEMMAP_ERROR";
      case RESP_PROCESS_CLOSED:
        return "RESP_PROCESS_CLOSED";
      case RESP_EXCEPTION_OCCURED:
        return "RESP_EXCEPTION_OCCURED";
      case CMD_HALT:
        return "CMD_HALT";
      case RESP_HALTED_SUCCESS:
        return "RESP_HALTED_SUCCESS";
      case RESP_HALTED_ERROR:
        return "RESP_HALTED_ERROR";
      case RESP_REQUEST_TARGET:
        return "RESP_REQUEST_TARGET";
      case CMD_LIST_PROCESSES:
        return "CMD_LIST_PROCESSES";
      case RESP_LIST_PROCESSES_SUCCESS:
        return "RESP_LIST_PROCESSES_SUCCESS";
      case CMD_CANCEL_TARGET_SELECTION:
        return "CMD_CANCEL_TARGET_SELECTION";
      case RESP_CANCEL_TARGET_SELECTION_SUCCESS:
        return "RESP_CANCEL_TARGET_SELECTION_SUCCESS";
      case CMD_SELECT_PROCESS:
        return "CMD_SELECT_PROCESS";
      case RESP_SELECT_PROCESS_SUCCESS:
        return "RESP_SELECT_PROCESS_SUCCESS";
      case RESP_SELECT_PROCESS_ERROR:
        return "RESP_SELECT_PROCESS_ERROR";
      case CMD_LIST_FILES:
        return "CMD_LIST_FILES";
      case CMD_LIST_FILES_PATH:
        return "CMD_LIST_FILES_PATH";
      case RESP_LIST_FILES_SUCCESS:
        return "RESP_LIST_FILES_SUCCESS";
      case RESP_LIST_FILES_ERROR:
        return "RESP_LIST_FILES_ERROR";
      case CMD_SELECT_FILE:
        return "CMD_SELECT_FILE";
      case RESP_SELECT_FILE_SUCC:
        return "RESP_SELECT_FILE_SUCC";
      case RESP_SELECT_FILE_ERR:
        return "RESP_SELECT_FILE_ERR";
      case RESP_MODULE_LOADED:
        return "RESP_MODULE_LOADED";
      case RESP_MODULE_UNLOADED:
        return "RESP_MODULE_UNLOADED";
      case CMD_RESUME_THREAD:
        return "CMD_RESUME_THREAD";
      case RESP_RESUME_THREAD_SUCC:
        return "RESP_RESUME_THREAD_SUCC";
      case RESP_RESUME_THREAD_ERR:
        return "RESP_RESUME_THREAD_ERR";
      case CMD_SUSPEND_THREAD:
        return "CMD_SUSPEND_THREAD";
      case RESP_SUSPEND_THREAD_SUCC:
        return "RESP_SUSPEND_THREAD_SUCC";
      case RESP_SUSPEND_THREAD_ERR:
        return "RESP_SUSPEND_THREAD_ERR";
      case CMD_SET_ACTIVE_THREAD:
        return "CMD_SET_ACTIVE_THREAD";
      case RESP_SET_ACTIVE_THREAD_SUCC:
        return "RESP_SET_ACTIVE_THREAD_SUCC";
      case RESP_SET_ACTIVE_THREAD_ERR:
        return "RESP_SET_ACTIVE_THREAD_ERR";
      case CMD_SET_BREAKPOINT_CONDITION:
        return "CMD_SET_BREAKPOINT_CONDITION";
      case RESP_SET_BREAKPOINT_CONDITION_SUCC:
        return "RESP_SET_BREAKPOINT_CONDITION_SUCC";
      case RESP_SET_BREAKPOINT_CONDITION_ERR:
        return "RESP_SET_BREAKPOINT_CONDITION_ERR";
      case CMD_WRITE_MEMORY:
        return "CMD_WRITE_MEMORY";
      case RESP_WRITE_MEMORY_SUCC:
        return "RESP_WRITE_MEMORY_SUCC";
      case RESP_WRITE_MEMORY_ERR:
        return "RESP_WRITE_MEMORY_ERR";
      case CMD_SET_EXCEPTIONS:
        return "CMD_SET_EXCEPTIONS";
      case RESP_SET_EXCEPTIONS_SUCC:
        return "RESP_SET_EXCEPTIONS_SUCC";
      case RESP_SET_EXCEPTIONS_ERR:
        return "RESP_SET_EXCEPTIONS_ERR";
      case CMD_SET_DEBUGGER_EVENT_SETTINGS:
        return "CMD_SET_DEBUGGER_EVENT_SETTINGS";
      case RESP_SET_DEBUGGER_EVENT_SETTINGS_SUCC:
        return "RESP_SET_DEBUGGER_EVENT_SETTINGS_SUCC";
      case RESP_SET_DEBUG_EVENT_SETTINGS_ERR:
        return "RESP_SET_DEBUG_EVENT_SETTINGS_ERR";
      case RESP_QUERY_DEBUGGER_EVENT_SETTINGS:
        return "RESP_QUERY_DEBUGGER_EVENT_SETTINGS";
      case RESP_PROCESS_START:
        return "RESP_PROCESS_START";
      default:
        return "ERROR_INVALID_PACKET";
    }
  }
}
