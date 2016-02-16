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

#ifndef TCPTRANSPORT_HPP
#define TCPTRANSPORT_HPP

#include <string>

#include "Transport.hpp"

#include "../conns/GenericSocketFunctions.hpp"

/**
 * Transport class that can be used to create TCP connections to the gbdserver.
 */
class TcpTransport : public Transport {
 private:
  // Address of the host where the gdbserver is running.
  std::string host;

  // Port where the gdbserver is listening.
  unsigned int port;

  // Socket that can be used to connect with the gdbserver.
  SOCKET gdbSocket;

 public:

  /**
   * Creates a new TCP/IP connection to the GDB server.
   *
   * @param host Host address of the GDB server connection.
   * @param port Port of the GDB server.
   */
  TcpTransport(const std::string& host, unsigned int port)
      : host(host),
        port(port) {
  }

  // Opens the TCP/IP connection to the GDB server.
  NaviError open();

  // Closes the TCP/IP connection to the GDB server.
  NaviError close();

  // Checks whether data is available from the GDB server.
  bool hasData() const;

  // Sends data to the GDB server.
  NaviError send(const char* buffer, unsigned int size) const;

  // Receives data from the GDB server.
  NaviError read(char* buffer, unsigned int size) const;
};

#endif
