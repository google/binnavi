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

// Provides BreakpointInfo, which holds breakpoint's parameters.
#ifndef BREAKPOINTINFO_H_
#define BREAKPOINTINFO_H_

#include <functional>
#include <memory>

#include "common.h"

// Note that instances of this class define an operator== that only takes the
// breakpoint id into account and ignores other fields.
class BreakpointInfo {
 public:
  BreakpointInfo(breakpoint_id_t id, bool auto_resume, bool send_registers) :
    : id_(id), auto_resume_(auto_resume), send_registers_(send_registers) {}

  breakpoint_id_t id() const { return id_; }
  bool auto_resume() const { return auto_resume_; }
  bool send_registers() const { return send_registers_; }

  bool operator==(const BreakpointInfo& other) const {
    return id_ == other.id_;
  }

  typedef std::function<bool(const std::unique_ptr<BreakpointInfo>&)>
    BreakpointComparator;

  // Returns a function which matches the thread with a given id.
  // Intended for use with STL functions, e.g. std::remove_if.
  static BreakpointComparator MakeBreakpointIdComparator(breakpoint_id_t id) {
    return [id](const std::unique_ptr<BreakpointInfo>& t)
      -> bool { return t->id() == id; };
  }

 private:
  breakpoint_id_t id_;
  bool auto_resume_;
  bool send_registers_;
};

#endif  // BREAKPOINTINFO_H_
