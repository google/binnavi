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

#include "OSXSocket.hpp"

#include "../logger.hpp"

SOCKET Bind_To_Socket(unsigned int port) {
  int sd;
  struct sockaddr_in sin;
  int on = 1;

  sd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

  if (sd < 0) {
    return SOCKET_ERR;
  }

  if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on))) {
    return SOCKET_ERR;
  }

  sin.sin_addr.s_addr = 0;
  sin.sin_family = AF_INET;
  sin.sin_port = htons(port);

  if (bind(sd, (struct sockaddr*) &sin, sizeof(sin)) != 0) {
    close(sd);
    return SOCKET_ERR;
  }

  return sd;
}

NaviError OSXSocket::bindSocket() {
  msglog->log(LOG_ALL, "Entering: %s", __FUNCTION__);

  localSocket = Bind_To_Socket(getPort());

  if (localSocket == SOCKET_ERR) {
    msglog->log(LOG_ALL, "Error: Couldn't open server socket");
    return NaviErrors::COULDNT_START_SERVER;
  }

  return NaviErrors::SUCCESS;
}

NaviError OSXSocket::send(const char* buffer, unsigned int size) const {
  msglog->log(LOG_ALL, "Entering: %s", __FUNCTION__);

  return
      (unsigned int) ::send(remoteSocket, buffer, size, 0) == size ?
          NaviErrors::SUCCESS : NaviErrors::SEND_ERROR;
}

NaviError OSXSocket::read(char* buffer, unsigned int size) const {
  msglog->log(LOG_ALL, "Entering: %s", __FUNCTION__);

  int retval;
  unsigned int recvd = 0;

  while (recvd != size) {
    retval = recv(remoteSocket, buffer + recvd, size - recvd, 0);

    if (retval == 0) {
      // Connection closed gracefully
      return NaviErrors::CONNECTION_CLOSED;
    }

    if (retval == -1) {
      // Connection failure
      return NaviErrors::CONNECTION_ERROR;
    }

    recvd += retval;
  }

  return NaviErrors::SUCCESS;
}

// XXX Willem: this might kill responsiveness on some unices that seem to not
// deliver a signal when a debugging event is available
//  Also select will return an error if a signal arrives
bool Is_Data_Available_On_Sock(SOCKET inputsock) {
  fd_set fds;
  TIMEVAL tv;
  int i;

  tv.tv_sec = 0;
  tv.tv_usec = 1;

  FD_ZERO(&fds);
  FD_SET(inputsock, &fds);  //, inputsock );

  if ((i = select(inputsock + 1, &fds, NULL, NULL, &tv)) < 0) {
    msglog->log(LOG_ALL, "Error: Select failed");
  } else if (FD_ISSET(inputsock, &fds)) {
    return true;
  }

  return false;
}

bool OSXSocket::hasData() const {
  return Is_Data_Available_On_Sock(remoteSocket);
}

SOCKET Wait_For_Connection(SOCKET sock) {
  int sd;

  listen(sock, 5);
  sd = accept(sock, NULL, 0);

  if (sd < 0) {
    return SOCKET_ERR;
  }

  return sd;
}

NaviError OSXSocket::waitForConnection() {
  msglog->log(LOG_ALL, "Entering: %s", __FUNCTION__);

  remoteSocket = Wait_For_Connection(localSocket);

  if (remoteSocket == SOCKET_ERR) {
    msglog->log(LOG_ALL, "Error: Couldn't open connection to BinNavi");
    return NaviErrors::COULDNT_CONNECT_TO_BINNAVI;
  }

  return NaviErrors::SUCCESS;
}

NaviError OSXSocket::closeSocket() {
  close (remoteSocket);
  close (localSocket);

  return NaviErrors::SUCCESS;
}
