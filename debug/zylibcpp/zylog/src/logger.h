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

#ifndef LOGGER_H
#define LOGGER_H

#include <vector>

#include "ilogtarget.h"

namespace zylib {
/**
* The zylog namespace provides a Logger class that can be used to log messages to stdout to files or to
* other targets.
*
* To create your own log targets, just subclass ILogTarget and pass it to the Logger object.
**/
namespace zylog {
/**
* The Logger class can be used to log messages.
*
* It is possible to specify a log level when creating the Logger object. Only messages that have
* a log level that is lower than or equal to the Logger's log level are logged. Messages with a higher
* log level are not logged.
**/
class Logger {
 private:

  /**
  * Log level of the logger.
  **/
  unsigned int logLevel;

  /**
  * List of log targets that are notified to log messages.
  **/
  std::vector<ILogTarget*> targets;

 public:
  /**
  * \brief Creates a new Logger object.
  *
  * Creates a new Logger object.
  *
  * @param logLevel Log level of the Logger object.
  **/
  explicit Logger(unsigned int logLevel) : logLevel(logLevel) {}

  /**
  * \brief Adds a new log target to the Logger object.
  *
  * Adds a new log target to the Logger object.
  *
  * @param target The log target.
  **/
  void addTarget(ILogTarget* target);

  /**
  * \brief Logs a message.
  *
  * The log function works with variable arguments just like printf.
  *
  * \warning The maximum message size is 4000 bytes. Don't pass larger messages or you'll get a buffer overflow.
  *
  * @param level Log level of the message.
  * @param message The message to log.
  **/
  void log(unsigned int level, const char* message, ...);
};
}
}

#endif
