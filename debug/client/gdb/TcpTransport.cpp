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

#include "TcpTransport.hpp"

#include <iostream>

#include <zycon/src/zycon.h>

#include "../defs.hpp"
#include "../logger.hpp"

#include "../conns/GenericSocketFunctions.hpp"

/**
 * Determines whether data is available from the gdbserver or not.
 *
 * @return True, if data is available. False, otherwise.
 */
bool TcpTransport::hasData() const {
  return SocketFunctions::hasData(gdbSocket);
}

/**
 * Opens the connection to the gdbclient.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError TcpTransport::open() {

  gdbSocket = SocketFunctions::connect(host, port);
  return
      gdbSocket ? NaviErrors::SUCCESS : NaviErrors::COULDNT_CONNECT_TO_GDBSERVER;
}

/**
 * Closes the connection to the gdbserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError TcpTransport::close() {

  SocketFunctions::close (gdbSocket);
  return NaviErrors::SUCCESS;
}

/**
 * Sends data to the gdbserver.
 *
 * @param buffer The data to send.
 * @param size The size of the buffer.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError TcpTransport::send(const char* buffer, unsigned int size) const {
  return
      SocketFunctions::send(gdbSocket, buffer, size) == size ?
          NaviErrors::SUCCESS : NaviErrors::SEND_ERROR;
}

/**
 * Reads data from the gdbserver.
 *
 * @param buffer The buffer the data is written to.
 * @param size The number of bytes to read from the gdbserver.
 *
 * @return A NaviError code that indicates whether the operation succeeded or
 * not.
 */
NaviError TcpTransport::read(char* buffer, unsigned int size) const {
  return
      SocketFunctions::read(gdbSocket, buffer, size) ?
          NaviErrors::SUCCESS : NaviErrors::CONNECTION_ERROR;
}
