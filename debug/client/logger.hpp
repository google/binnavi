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

#ifndef DBLOGGER_HPP  // Don't change this to LOGGER_HPP
#define DBLOGGER_HPP

#include <zylog/src/logger.h>

extern zylib::zylog::Logger* msglog;

enum {
  LOG_ALWAYS = 0,
  LOG_VERBOSE = 1,
  LOG_ALL = 5
};

#endif
