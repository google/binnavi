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

#include "WinCESocket.hpp"

#include <cstdio>
#include <iostream>

#include "../errors.hpp"
#include "../logger.hpp"

#include "GenericSocketFunctions.hpp"

//custom defines, please forgive...
#define EVENT_EXIT_NAME L"binnavi_exit"
#define INDEX_EXIT 1
#define INDEX_CONN 0
#define EXIT_PROGRAM 0xFFFFFFFF

/**
 * Binds a local socket to the specified port.
 *
 * @return 0 if everything went fine. Non-zero to indicate an error.
 */
NaviError WinCESocket::bindSocket() {


  localSocket = Bind_To_Socket(getPort());

  return localSocket ? NaviErrors::SUCCESS : NaviErrors::COULDNT_START_SERVER;
}

NaviError WinCESocket::closeSocket() {


  closesocket (remoteSocket);
  closesocket (localSocket);

  return NaviErrors::SUCCESS;
}

/**
 * @return 0 if everything went fine. Non-zero to indicate an error.
 */
NaviError WinCESocket::waitForConnection() {
  //user could click on "close" button while we're waiting for a connection
  //the following code stops waiting for a conn when this happens
  HANDLE hEventExit = OpenEvent(EVENT_ALL_ACCESS, false, EVENT_EXIT_NAME);


  HANDLE hConnThread = CreateThread(
      NULL, 0, (LPTHREAD_START_ROUTINE) & Wait_For_Connection,
      (void *) localSocket, 0, NULL);

  HANDLE lpHandles[2];
  lpHandles[INDEX_CONN] = hConnThread;
  lpHandles[INDEX_EXIT] = hEventExit;

  DWORD waitCode = WaitForMultipleObjects(2, lpHandles, false, INFINITE)
      - WAIT_OBJECT_0;
  if (waitCode == INDEX_EXIT) {
    TerminateThread(hConnThread, 0);  //kill connection thread
    return EXIT_PROGRAM;
  }
  GetExitCodeThread(hConnThread, (LPDWORD) & remoteSocket);
  return
      remoteSocket ?
          NaviErrors::SUCCESS : NaviErrors::COULDNT_CONNECT_TO_BINNAVI;
}

/**
 * Indicates whether data from BinNavi is incoming.
 *
 * @return True, if incoming data is ready to be read. False, otherwise.
 */
bool WinCESocket::hasData() const {
  //	echo_dbg( "Entering %s ...\n", __FUNCTION__);

  return SocketFunctions::hasData(remoteSocket);
}

/**
 * Sends a specified number of bytes from a given buffer to BinNavi.
 *
 * @param buffer The data to write.
 * @param size The size of the buffer.
 *
 * @return Zero if everything went fine. Non-zero if the data could not be read.
 */
NaviError WinCESocket::send(const char *buffer, unsigned int size) const {


  return
      (unsigned int) ::send(remoteSocket, buffer, size, 0) == size ?
          NaviErrors::SUCCESS : NaviErrors::SEND_ERROR;
}

/**
 * Reads an exact amount of incoming bytes from from BinNavi.
 *
 * @param buffer The buffer the bytes are written to.
 * @param size Number of bytes to read.
 *
 * @return Zero if everything went fine. Non-zero if the data could not be read.
 */
NaviError WinCESocket::read(char *buffer, unsigned int size) const {


  return
      SocketFunctions::read(remoteSocket, buffer, size) ?
          NaviErrors::SUCCESS : NaviErrors::CONNECTION_ERROR;
}

/**
 * Creates a SOCKET object for a given port.
 *
 * @param port The number of the port.
 */
SOCKET Bind_To_Socket(unsigned int port) {


  struct sockaddr_in sockadd;
  WSADATA WSAStruc;
  SOCKET sock;

  WSAStartup(MAKEWORD(2, 0), &WSAStruc);

  if (LOBYTE(WSAStruc.wVersion) != 2 || HIBYTE(WSAStruc.wVersion) != 0) {
    WSACleanup();
    msglog->log(LOG_ALWAYS, "Error: system does not support Winsock 2.0.");
    return 0;
  }

  sockadd.sin_family = AF_INET;
  sockadd.sin_port = htons((u_short) port);
  sockadd.sin_addr.s_addr = htonl(INADDR_ANY);

  memset(&(sockadd.sin_zero), 0x00, 8);

  sock = socket(AF_INET, SOCK_STREAM, 0);

  if (bind(sock, (struct sockaddr *) &sockadd, sizeof(sockadd))) {
    msglog->log(LOG_VERBOSE, "Error: bind() for local port failed.");
    return 0;
  }

  return sock;
}

/**
 * Waits until a connection is established at a given socket.
 *
 * @param sock The socket object.
 *
 * @return The accepted connection.
 */
SOCKET Wait_For_Connection(SOCKET sock) {


  SOCKET conn;
  struct sockaddr_in from;
  int sockaddlen = sizeof(from);

  memset(&from, 0, sizeof(from));

  if (listen(sock, 10)) {
    msglog->log(LOG_VERBOSE, "error: listen() for local port failed.");
    return 0;
  }

  conn = accept(sock, (struct sockaddr *) &from, (int *) &sockaddlen);

  if ((conn == 0) || (conn == INVALID_SOCKET)) {
    msglog->log(LOG_VERBOSE, "Accept returned null, how can that be ? (%d)\n",
                GetLastError());
    return 0;
  } else {
    return conn;
  }
}

void WinCESocket::printConnectionInfo() {
  WSAData wsaData;

  if (WSAStartup(MAKEWORD(1, 1), &wsaData) != 0) {
    return;
  }

  char ac[80];
  if (gethostname(ac, sizeof(ac)) == SOCKET_ERROR) {
    msglog->log(LOG_VERBOSE, "Error %d when getting local host name.",
                WSAGetLastError());
    return;
  }

  hostent *phe = gethostbyname(ac);

  if (phe == 0) {
    msglog->log(LOG_VERBOSE, "Error: Could not not look up host name");
    return;
  }

  for (int i = 0; phe->h_addr_list[i] != 0; ++i) {
    in_addr addr;
    memcpy(&addr, phe->h_addr_list[i], sizeof(in_addr));

    msglog->log(LOG_ALWAYS, "Opened server port on local IP address: %s",
                inet_ntoa(addr));
  }

  WSACleanup();
}
