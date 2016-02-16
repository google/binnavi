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

#ifndef TRANSPORT_HPP
#define TRANSPORT_HPP

#define NOMINMAX

#include "../errors.hpp"

/**
 * Base class for various options to communicate with the gdbserver.
 */
class Transport {
 public:
  /**
   * Opens the connection.
   */
  virtual NaviError open() = 0;

  /**
   * Closes the connection.
   */
  virtual NaviError close() = 0;

  /**
   * Determines whether data from gdbserver is available.
   *
   * @return True, if data is available. False, otherwise.
   */
  virtual bool hasData() const = 0;

  /**
   * Sends data to the gdbserver.
   *
   * @param buffer The data to send.
   * @param size The size of the data in bytes.
   */
  virtual NaviError send(const char* buffer, unsigned int size) const = 0;

  /**
   * Reads data from the gdbserver.
   *
   * @param buffer Buffer where the data from gdbserver is written to.
   * @param size Number of bytes to read from gdbserver.
   */
  virtual NaviError read(char* buffer, unsigned int size) const = 0;

  virtual ~Transport() {
  }
};

#endif
