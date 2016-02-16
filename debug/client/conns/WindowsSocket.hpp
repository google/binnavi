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

#ifndef WINDOWSSOCKET_HPP
#define WINDOWSSOCKET_HPP

#include <winsock2.h>
#include <Ws2tcpip.h>
#include <windows.h>
#include <Psapi.h>

#include "../SocketConnection.hpp"

SOCKET Bind_To_Socket(unsigned int port);
SOCKET Wait_For_Connection(SOCKET sock);

/**
 * Connection policy class for Windows sockets.
 */
class WindowsSocket : public SocketConnection {
 private:

  /**
   * The server socket on which the debug client listens for a BinNavi
   * connection.
   */
  SOCKET localSocket;

  /**
   * The remote socket that is used to communicate with a connected BinNavi.
   */
  SOCKET remoteSocket;

 protected:

  NaviError bindSocket();

  NaviError closeSocket();

  NaviError send(const char* buffer, unsigned int size) const;

  NaviError read(char* buffer, unsigned int size) const;

 public:
  /**
   * Creates a new WindowsSocket object at a given port.
   *
   * @param port The port on which the debug client listens for BinNavi
   * connections.
   **/
  WindowsSocket(unsigned int port)
      : SocketConnection(port) {
  }

  /**
   * Converts a value from host to network byte order.
   *
   * @param val The value in host order.
   *
   * @return The same value in network order.
   */
  int htonl(int val) const {
    return ::htonl(val);
  }

  /**
   * Converts a value from network to host byte order.
   *
   * @param val The value in network order.
   *
   * @return The same value in byte order.
   */
  int ntohl(int val) const {
    return ::ntohl(val);
  }

  bool hasData() const;

  NaviError waitForConnection();

  void printConnectionInfo();
};

#endif
