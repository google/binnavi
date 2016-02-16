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

#include "boost/assign.hpp"
#include "commands.hpp"
#include <map>

std::map<commandtype_t, const char*> CommandMap = boost::assign::map_list_of(
    cmd_clearall,
    "cmd_clearall")(cmd_setbp, "cmd_setbp")(cmd_setbpe, "cmd_setbpe")(
    cmd_setbps,
    "cmd_setbps")(cmd_rembp, "cmd_rembp")(cmd_rembpe, "cmd_rembpe")(
    cmd_rembps,
    "cmd_rembps")(cmd_read_memory, "cmd_read_memory")(
    cmd_registers,
    "cmd_registers")(cmd_resume, "cmd_resume")(cmd_detach, "cmd_detach")(
    cmd_LAST,
    "cmd_LAST")(resp_ok, "resp_ok")(resp_err, "resp_err")(
    resp_bp_hit,
    "resp_bp_hit")(resp_bpe_hit, "resp_bpe_hit")(resp_bps_hit, "resp_bps_hit")(
    resp_read_memory,
    "resp_read_memory")(resp_registers, "resp_registers")(
    resp_resumed,
    "resp_resumed")(resp_procdead, "resp_procdead")(resp_attach_error,
                                                    "resp_attach_error")(
    resp_attach_success,
    "resp_attach_success")(resp_bp_set_succ, "resp_bp_set_succ")(
    resp_bp_set_err,
    "resp_bp_set_err")(resp_resume_err, "resp_resume_err")(resp_bpe_set_succ,
                                                           "resp_bpe_set_succ")(
    resp_bpe_set_err,
    "resp_bpe_set_err")(resp_bp_rem_succ, "resp_bp_rem_succ")(
    resp_bp_rem_err,
    "resp_bp_rem_err")(resp_detach_succ, "resp_detach_succ")(resp_detach_err,
                                                             "resp_detach_err")(
    resp_registers_err,
    "resp_registers_err")(resp_read_memory_err, "resp_read_memory_err")(
    cmd_terminate,
    "cmd_terminate")(resp_terminate_succ, "resp_terminate_succ")(
    resp_terminate_err,
    "resp_terminate_err")(resp_bpe_rem_succ, "resp_bpe_rem_succ")(
    resp_bpe_rem_err,
    "resp_bpe_rem_err")(resp_bps_set_succ, "resp_bps_set_succ")(
    resp_bps_set_err,
    "resp_bps_set_err")(resp_bps_rem_succ, "resp_bps_rem_succ")(
    resp_bps_rem_err,
    "resp_bps_rem_err")(resp_info, "resp_info")(cmd_set_register,
                                                "cmd_set_register")(
    resp_set_register_succ,
    "resp_set_register_succ")(resp_set_register_err, "resp_set_register_err")(
    cmd_single_step,
    "cmd_single_step")(resp_single_step_succ, "resp_single_step_succ")(
    resp_single_step_err,
    "resp_single_step_err")(cmd_validmem, "cmd_validmem")(resp_validmem_succ,
                                                          "resp_validmem_succ")(
    resp_validmem_err,
    "resp_validmem_err")(resp_thread_created, "resp_thread_created")(
    resp_thread_closed,
    "resp_thread_closed")(cmd_search, "cmd_search")(resp_search_succ,
                                                    "resp_search_succ")(
    resp_search_err,
    "resp_search_err")(cmd_memmap, "cmd_memmap")(resp_memmap_succ,
                                                 "resp_memmap_succ")(
    resp_memmap_err,
    "resp_memmap_err")(resp_process_closed, "resp_process_closed")(
    resp_exception_occured,
    "resp_exception_occured")(cmd_halt, "cmd_halt")(resp_halted_succ,
                                                    "resp_halted_succ")(
    resp_halted_err,
    "resp_halted_err")(resp_request_target, "resp_request_target")(
    cmd_list_processes,
    "cmd_list_processes")(resp_list_processes, "resp_list_processes")(
    cmd_cancel_target_selection,
    "cmd_cancel_target_selection")(resp_cancel_target_selection_succ,
                                   "resp_cancel_target_selection_succ")(
    cmd_select_process,
    "cmd_select_process")(resp_select_process_succ, "resp_select_process_succ")(
    resp_select_process_err,
    "resp_select_process_err")(cmd_list_files, "cmd_list_files")(
    cmd_list_files_path,
    "cmd_list_files_path")(resp_list_files_succ, "resp_list_files_succ")(
    resp_list_files_err,
    "resp_list_files_err")(cmd_select_file, "cmd_select_file")(
    resp_select_file_succ,
    "resp_select_file_succ")(resp_select_file_err, "resp_select_file_err")(
    resp_module_loaded,
    "resp_module_loaded")(resp_module_unloaded, "resp_module_unloaded")(
    cmd_resume_thread,
    "cmd_resume_thread")(resp_resume_thread_succ, "resp_resume_thread_succ")(
    resp_resume_thread_err,
    "resp_resume_thread_err")(cmd_suspend_thread, "cmd_suspend_thread")(
    resp_suspend_thread_succ,
    "resp_suspend_thread_succ")(
    resp_suspend_thread_err,
    "resp_suspend_thread_err")(cmd_set_active_thread, "cmd_set_active_thread")(
    resp_set_active_thread_succ,
    "resp_set_active_thread_succ")(resp_set_active_thread_err,
                                   "resp_set_active_thread_err")(
    cmd_set_breakpoint_condition,
    "cmd_set_breakpoint_condition")(resp_set_breakpoint_condition_succ,
                                    "resp_set_breakpoint_condition_succ")(
    resp_set_breakpoint_condition_err,
    "resp_set_breakpoint_condition_err")(cmd_write_memory, "cmd_write_memory")(
    resp_write_memory_succ,
    "resp_write_memory_succ")(resp_write_memory_err, "resp_write_memory_err")(
    cmd_set_exceptions_options,
    "cmd_set_exceptions_options")(resp_set_exceptions_succ,
                                  "resp_set_exceptions_succ")(
    resp_set_exceptions_err,
    "resp_set_exceptions_err")(cmd_set_debugger_event_settings,
                               "cmd_set_debugger_event_settings")(
    resp_set_debugger_event_settings_succ,
    "resp_set_debugger_event_settings_succ")(
    resp_set_debugger_event_settings_err,
    "resp_set_debugger_event_settings_err")(
    resp_query_debugger_event_settings,
    "resp_query_debugger_event_settings")(resp_process_start,
                                          "resp_process_start");

const char* commandToString(commandtype_t cmd) {
  std::map<commandtype_t, const char*>::const_iterator cit =
      CommandMap.find(cmd);
  if (cit != CommandMap.end())
    return cit->second;
  else
    return "Unknown command type";
}
