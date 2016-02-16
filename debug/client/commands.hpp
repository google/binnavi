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

#ifndef COMMANDS_HPP
#define COMMANDS_HPP

enum commandtype_t {
  cmd_clearall = 0,
  cmd_setbp =
      1,  //< breakpoint, wait for client commands afterwards w/o resuming
  cmd_setbpe = 2,  //< breakpoint, echo address back, remove and resume
  cmd_setbps = 3,
  cmd_rembp = 4,   //< remove setbp
  cmd_rembpe = 5,  //< remove setbpe
  cmd_rembps = 6,
  cmd_read_memory = 7,  //< echo back memory range
  cmd_registers = 8,    //< echo register back
  cmd_resume = 9,       //< resumen nach nem breakpoint
  cmd_detach = 10,      //< detach from debugger
  cmd_LAST = 11,
  resp_ok = 12,
  resp_err = 13,
  resp_bp_hit = 14,
  resp_bpe_hit = 15,
  resp_bps_hit = 16,
  resp_read_memory = 17,
  resp_registers = 18,
  resp_resumed = 19,
  resp_procdead = 20,
  resp_attach_error = 23,
  resp_attach_success = 24,
  resp_bp_set_succ = 25,
  resp_bp_set_err = 26,
  resp_resume_err = 27,
  resp_bpe_set_succ = 28,
  resp_bpe_set_err = 29,
  resp_bp_rem_succ = 30,
  resp_bp_rem_err = 31,
  resp_detach_succ = 32,
  resp_detach_err = 33,
  resp_registers_err = 34,
  resp_read_memory_err = 35,
  cmd_terminate = 36,
  resp_terminate_succ = 37,
  resp_terminate_err = 38,
  resp_bpe_rem_succ = 39,
  resp_bpe_rem_err = 40,
  resp_bps_set_succ = 41,
  resp_bps_set_err = 42,
  resp_bps_rem_succ = 43,
  resp_bps_rem_err = 44,
  resp_info = 45,
  cmd_set_register = 46,
  resp_set_register_succ = 47,
  resp_set_register_err = 48,
  cmd_single_step = 49,
  resp_single_step_succ = 50,
  resp_single_step_err = 51,
  cmd_validmem = 52,
  resp_validmem_succ = 53,
  resp_validmem_err = 54,
  resp_thread_created = 55,
  resp_thread_closed = 56,
  cmd_search = 57,
  resp_search_succ = 58,
  resp_search_err = 59,
  cmd_memmap = 60,
  resp_memmap_succ = 61,
  resp_memmap_err = 62,
  resp_process_closed = 63,
  resp_exception_occured = 64,
  cmd_halt = 65,
  resp_halted_succ = 66,
  resp_halted_err = 67,
  resp_request_target = 68,
  cmd_list_processes = 69,
  resp_list_processes = 70,
  cmd_cancel_target_selection = 71,
  resp_cancel_target_selection_succ = 72,
  cmd_select_process = 73,
  resp_select_process_succ = 74,
  resp_select_process_err = 75,
  cmd_list_files = 76,
  cmd_list_files_path = 77,
  resp_list_files_succ = 78,
  resp_list_files_err = 79,
  cmd_select_file = 80,
  resp_select_file_succ = 81,
  resp_select_file_err = 82,
  resp_module_loaded = 83,
  resp_module_unloaded = 84,
  cmd_resume_thread = 85,
  resp_resume_thread_succ = 86,
  resp_resume_thread_err = 87,
  cmd_suspend_thread = 88,
  resp_suspend_thread_succ = 89,
  resp_suspend_thread_err = 90,
  cmd_set_active_thread = 91,
  resp_set_active_thread_succ = 92,
  resp_set_active_thread_err = 93,
  cmd_set_breakpoint_condition = 94,
  resp_set_breakpoint_condition_succ = 95,
  resp_set_breakpoint_condition_err = 96,
  cmd_write_memory = 97,
  resp_write_memory_succ = 98,
  resp_write_memory_err = 99,
  cmd_set_exceptions_options = 100,
  resp_set_exceptions_succ = 101,
  resp_set_exceptions_err = 102,
  cmd_set_debugger_event_settings = 103,
  resp_set_debugger_event_settings_succ = 104,
  resp_set_debugger_event_settings_err = 105,
  resp_query_debugger_event_settings = 106,
  resp_process_start = 107
};

extern const char* commandToString(commandtype_t cmd);

#endif
