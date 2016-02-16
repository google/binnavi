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

#ifndef SOCKETCONNECTION_H
#define SOCKETCONNECTION_H

#include "BaseConnection.hpp"

/**
 * Base class for all socket connection policies.
 **/
class SocketConnection : public BaseConnection {
 private:

  /**
   * Port on which the debug client listens for BinNavi connections.
   **/
  unsigned int port;

 protected:

  /**
   * Returns the port on which the debug client listens for BinNavi connections.
   *
   * @return The port on which the debug client listens for BinNavi connections.
   */
  unsigned int getPort() const {
    return port;
  }

  /**
   * Binds a socket to the port on which the debug client listens for BinNavi
   * connections.
   *
   * @return A NaviError code that describes whether the operation was 
   * successful or not.
   */
  virtual NaviError bindSocket() = 0;

  /**
   * Closes the socket to BinNavi
   *
   * @return A NaviError code that describes whether the operation was 
   * successful or not.
   */
  virtual NaviError closeSocket() = 0;

 public:

  /**
   * Creates a new socket connection policy.
   *
   * @param port The port on which the debug client listens for BinNavi
   * connections.
   */
  SocketConnection(unsigned int port)
      : port(port) {
  }

  //! Initializes the connection to BinNavi
  NaviError initializeConnection();

  //! Closes the connection to BinNavi
  NaviError closeConnection();

  virtual void printConnectionInfo() = 0;
};

#endif
