//// Copyright 2011-2016 Google Inc. All Rights Reserved.
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

// Provides DebugState, holds current debugger state and manages state
// transitions. It holds only the part of debugger state that is shared
// between DynamoRIO callbacks and debugger loop, thus needs access
// synchronization.
#ifndef DEBUGSTATE_H_
#define DEBUGSTATE_H_

#include <map>
#include <memory>
#include <set>
#include <vector>

#include "BreakpointInfo.h"
#include "common.h"
#include "dr_api.h"
#include "drdebug.pb.h"

class DebugState {
 public:
  enum State {
    NOT_SET,  // Default value after DebugState construction.
    RUNNING,  // Debuggee is currently running.
    WAITING,  // Waiting after BP hit/exception.
    HALTED,   // Halted by user, waiting for resume.
    EXITING,  // After receiving exit_event, but before termination.
  };

  DebugState();
  ~DebugState();

  // Acquire internal mutex
  void Lock();

  // Free internal mutex
  void Unlock();

  security::drdebug::ExceptionAction GetExceptionAction(
      exception_code_t exc_code);
  void SetExceptionAction(exception_code_t exc_code,
                          security::drdebug::ExceptionAction action);

  // Getters/setters
  State state() const { return state_; }

  void set_state(State new_state) { state_ = new_state; }

  std::set<app_pc>& breakpoint_addresses() { return breakpoint_addresses_; }

  std::map<app_pc, std::vector<std::unique_ptr<BreakpointInfo>>>&
  breakpoints() {
    return breakpoints_;
  }

  std::vector<thread_id_t>& debuggee_threads() { return debugee_threads_; }

  // No copy/assignment
  DebugState(const DebugState&) = delete;
  DebugState& operator =(const DebugState&) = delete;

private:
  // Mutex for accessing fields
  void* mutex_;
  // Current state of debugged process
  State state_;
  // All debuggee addresses with breakpoints
  std::set<app_pc> breakpoint_addresses_;
  // Maps debuggee adress to a list of breakpoints id set on it
  // We need a pointer, not an object, because we rely on that the info struct
  // address won't change (we pass it in clean call to BPHit handler)
  std::map<app_pc, std::vector<std::unique_ptr<BreakpointInfo>>> breakpoints_;
  // Maps exception codes to its handling action
  std::map<exception_code_t, security::drdebug::ExceptionAction>
      exception_actions_;
  // List of all debugee threads
  std::vector<thread_id_t> debugee_threads_;
};

#endif  // DEBUGSTATE_H_
