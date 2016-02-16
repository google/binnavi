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

#include "GenericSocketFunctions.hpp"
#include "../logger.hpp"

#include <cstdlib>

#include <zycon/src/zycon.h>

namespace SocketFunctions {
bool hasData(SOCKET socket) {
  fd_set fds;
  TIMEVAL tv;
  int i;

  tv.tv_sec = 0;
  tv.tv_usec = 1;

  FD_ZERO(&fds);
  FD_SET(socket, &fds);

  if ((i = select(socket + 1, &fds, NULL, NULL, &tv)) < 0) {
    msglog->log(LOG_ALWAYS, "Error: select returned error %lx!",
                LAST_ERROR_FUNCTION);
    std::exit(0);
  } else {
    return FD_ISSET(socket, &fds) ? true : false;
  }
}

NaviError read(SOCKET socket, char* buffer, unsigned int size) {
  int retval;
  unsigned int recvd = 0;

  while (recvd != size) {
    retval = recv(socket, buffer + recvd, size - recvd, 0);

    if (retval == 0) {
      // Connection closed gracefully
      return NaviErrors::CONNECTION_CLOSED;
    }

    if (retval == SOCKET_ERROR) {
      msglog->log(LOG_ALWAYS, "Error: Reading data from BinNavi (Code %d)",
                  LAST_ERROR_FUNCTION);

      return NaviErrors::CONNECTION_ERROR;
    }

    recvd += retval;
  }

  return NaviErrors::SUCCESS;
}

unsigned int send(SOCKET socket, const char* buffer, unsigned int size) {
  return ::send(socket, buffer, size, 0);
}

#ifdef SOME_GDB_AGENT
SOCKET connect(const std::string& host, unsigned int port) {
#ifdef NAVI_GDB_WINDOWS
  WORD wVersionRequested;
  WSADATA wsaData;
  int wsaerr;

  // Using MAKEWORD macro, Winsock version request 2.2

  wVersionRequested = MAKEWORD(2, 2);
  wsaerr = WSAStartup(wVersionRequested, &wsaData);
#endif

  SOCKET socket = ::socket(AF_INET, SOCK_STREAM, 0);

  addrinfo* info = 0;

  if (getaddrinfo(host.c_str(), zylib::zycon::toString(port).c_str(), 0,
          &info)) {
    msglog->log(LOG_ALWAYS, "Error: Couldn't get address information",
        LAST_ERROR_FUNCTION);
  }

  if (connect(socket, info->ai_addr, info->ai_addrlen) < 0) {
    msglog->log(LOG_ALWAYS,
        "Error: Couldn't connect to the GDB server socket (Code %d)",
        LAST_ERROR_FUNCTION);

    freeaddrinfo(info);
    return 0;
  }

  freeaddrinfo(info);

  return socket;
}

void close(SOCKET socket) {
#ifdef NAVI_GDB_WINDOWS
  closesocket(socket);
#endif
#if defined(NAVI_GDB_LINUX) || defined(NAVI_GDB_OSX)
  close(socket);
#endif
}
#endif
}
