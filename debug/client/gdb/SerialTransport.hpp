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

#ifdef NAVI_GDB_WINDOWS

#ifndef SERIALTRANSPORT_HPP
#define SERIALTRANSPORT_HPP

#include <windows.h>
#include <string>

#include "Transport.hpp"

/**
 * Class that is used to communicate with the GDB server via a COM port.
 */
class SerialTransport : public Transport {
private:

  // Handle of the COM port
  HANDLE port;

  // String name of the COM port
  const std::string comport;

  // Bauds that are supported by the COM port
  const unsigned int bauds;

public:

  /**
   * Creates a new SerialTransport object.
   *
   * @param comport The name of the COM port.
   * @param bauds The number of bauds supported by the COM port.
   */
  SerialTransport(const std::string& comport, unsigned int bauds)
  : comport(comport), bauds(bauds) {}

  // Opens the COM port to the GDB server.
  NaviError open();

  // Closes the COM port to the GDB server.
  NaviError close();

  // Checks whether data is available on the GDB server.
  bool hasData() const;

  // Sends data to the GDB server.
  NaviError send(const char* buffer, unsigned int size) const;

  // Reads data from the GDB server.
  NaviError read(char* buffer, unsigned int size) const;
};

#endif
#endif
