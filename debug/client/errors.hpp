// Copyright 2011-2016 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#ifndef ERRORS_HPP
#define ERRORS_HPP

typedef unsigned int NaviError;

namespace NaviErrors {
const unsigned int SUCCESS = 0;
const unsigned int COULDNT_START_SERVER = 1;
const unsigned int COULDNT_CONNECT_TO_BINNAVI = 2;
const unsigned int CONNECTION_CLOSED = 3;
const unsigned int CONNECTION_ERROR = 4;
const unsigned int PAGE_NOT_WRITABLE = 5;
const unsigned int COULDNT_WRITE_MEMORY = 6;
const unsigned int COULDNT_ENTER_DEBUG_MODE = 7;
const unsigned int COULDNT_OPEN_TARGET_PROCESS = 8;
const unsigned int COULDNT_DEBUG_TARGET_PROCESS = 9;
const unsigned int WAITING_FOR_DEBUG_EVENTS_FAILED = 10;
const unsigned int ORIGINAL_DATA_NOT_AVAILABLE = 11;
const unsigned int COULDNT_READ_REGISTERS = 12;
const unsigned int COULDNT_WRITE_REGISTERS = 13;
const unsigned int PAGE_NOT_READABLE = 14;
const unsigned int COULDNT_READ_MEMORY = 15;
const unsigned int INVALID_MEMORY_RANGE = 16;
const unsigned int MEMORY_RANGE_TOO_LARGE = 17;
const unsigned int COULDNT_RESUME_THREAD = 18;
const unsigned int COULDNT_SINGLE_STEP = 19;
const unsigned int COULDNT_LEAVE_SINGLE_STEP_MODE = 20;
const unsigned int COULDNT_ENTER_SINGLE_STEP_MODE = 21;
const unsigned int COULDNT_TERMINATE_TARGET_PROCESS = 22;
const unsigned int INVALID_REGISTER_INDEX = 23;
const unsigned int NO_VALID_MEMORY = 24;
const unsigned int INVALID_PACKET = 25;
const unsigned int UNKNOWN_COMMAND = 26;
const unsigned int INVALID_DEBUG_EVENT = 27;
const unsigned int INVALID_BREAKPOINT_TYPE = 28;
const unsigned int UNKNOWN_DEBUG_EVENT = 29;
const unsigned int MALFORMED_PACKET = 30;
const unsigned int COULDNT_FIND_DATA = 31;
const unsigned int INVALID_BREAKPOINT = 32;
const unsigned int HIGHER_BREAKPOINT_EXISTS = 33;
const unsigned int INVALID_PARAMETER = 34;
const unsigned int DUPLICATE_BREAKPOINT = 35;
const unsigned int NO_BREAKPOINT_AT_ADDRESS = 36;
const unsigned int SEND_ERROR = 37;
const unsigned int COULDNT_SET_BREAKPOINT = 38;
const unsigned int COULDNT_DETACH = 39;
const unsigned int COULDNT_REMOVE_BREAKPOINT = 40;
const unsigned int INVALID_TARGET_SPECIFICATION = 41;
const unsigned int COULDNT_CONNECT_TO_GDBSERVER = 42;
const unsigned int UNEXPECTED_GDB_REPLY = 43;
const unsigned int UNSUPPORTED = 44;
const unsigned int INVALID_CONNECTION_STRING = 45;
const unsigned int INVALID_CPU_STRING = 46;
const unsigned int GDB_CONSOLE_OUTPUT = 47;
const unsigned int GENERIC_ERROR = 48;
const unsigned int COULDNT_GET_PROCESSLIST = 49;
const unsigned int COULDNT_GET_FILELIST = 50;
const unsigned int COULDNT_LIST_PROCESSES = 51;
const unsigned int COULDNT_DETERMINE_INSTRUCTION_POINTER = 52;
const unsigned int COULDNT_SUSPEND_THREAD = 53;
const unsigned int INVALID_CONDITION_TREE = 54;
const unsigned int NOTHING_TO_REFRESH = 55;
const unsigned int COULDNT_GET_ROOTS = 56;
const unsigned int COULDNT_OPEN_THREAD = 57;
const unsigned int COULDNT_GET_EXE_PATH = 58;
const unsigned int COULDNT_LIST_MEMORY = 59;
const unsigned int COULDNT_SET_EXCEPTION_ACTION = 60;
}

#endif
