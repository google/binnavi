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

#define NOMINMAX
#include <windowsx.h>
#include <aygshell.h>
#include <winsock.h>
#include "resource.h"
#include <commctrl.h>
#include <sipapi.h>
#include <Commdlg.h>
#include <tlhelp32.h>

#include <memory>

#include "../includer.hpp"
#include "../DebugClient.hpp"
#include "../logger.hpp"

#include "ListBoxWMTarget.hpp"
#include <zylog/src/FileTarget.hpp>

zylib::zylog::Logger* msglog;

// WinCE is a pure unicode platform
// and needs WinMain() as entrypoint

#define MAX_LOADSTRING 400
#define ID_TRAY 5000
#define TRAY_NOTIFYICON WM_USER + 2001
#define EVENT_EXIT_NAME L"binnavi_exit"
#define EXIT_PROGRAM 0xFFFFFFFF
#define INDEX_EXIT 1
#define INDEX_CONN 0
//#define DEBUG 0

// Global Variables:
HINSTANCE g_hInst;  // The current instance
HWND g_hwndCB;      // The command bar handle
HWND g_hwndComCtls;
HANDLE hEventExit;
HANDLE debugger;
HWND hDlg;
bool volatile g_dbgactive = false;
HWND hwBox;
static SHACTIVATEINFO s_sai;
bool g_WinHide = false;
bool g_Minimize = false;

// Forward declarations of functions included in this code module:
BOOL InitInstance(HINSTANCE, int);
BOOL WINAPI BasicDlgProc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp);

DWORD debug_loop_attach(unsigned int ulPID);
DWORD debug_loop_start(const wchar_t* path);

#ifdef DEBUG
void initLogger() {
  msglog = new zylib::zylog::Logger(LOG_ALL);
  // msglog = new zylib::zylog::Logger(LOG_VERBOSE);
  zylib::zylog::ListBoxTarget* t1 =
  new zylib::zylog::ListBoxTarget(hDlg, IDC_LOG);
  zylib::zylog::FileTarget* t2 =
  new zylib::zylog::FileTarget("debug_BINNAVI_ARM.txt");
  msglog->addTarget(t1);
  msglog->addTarget(t2);

}
#else
void initLogger() {
  //TODO: recompile logger lib in order to make this work again
  msglog = new zylib::zylog::Logger(LOG_ALWAYS);
  zylib::zylog::ListBoxTarget* t1 = new zylib::zylog::ListBoxTarget(hDlg,
                                                                    IDC_LOG);
  msglog->addTarget(t1);

}
#endif

void ClearComboBox(HWND hwnd, int item) {
  HWND hwComboBox = GetDlgItem(hwnd, item);
  ComboBox_ResetContent(hwComboBox);

  return;
}

void AddToComboBox(HWND hwnd, int IDDlgItem, TCHAR* text) {
  //get the control window
  HWND hwComboBox = GetDlgItem(hwnd, IDDlgItem);
  ComboBox_AddString(hwComboBox, text /*szBufW*/);
}

void ClearListBox(HWND hwnd, int item) {
  HWND hwListBox = GetDlgItem(hwnd, item);
  ListBox_ResetContent(hwListBox);

  return;
}

void FillProcessList(HWND hwnd, int item) {
  msglog->log(LOG_ALWAYS, "Creating the process snapshot ...");

#define TH32CS_SNAPNOHEAPS 0x40000000

  HANDLE snapshot = CreateToolhelp32Snapshot(
      TH32CS_SNAPPROCESS | TH32CS_SNAPNOHEAPS /*TH32CS_SNAPALL*/, 0);
  PROCESSENTRY32 procentry;

  procentry.dwSize = sizeof(PROCESSENTRY32);

  if (snapshot == INVALID_HANDLE_VALUE) {
    msglog->log(LOG_ALWAYS,
                "Could not create process snapshot (Error Code: %d)",
                GetLastError());
    msglog->log(LOG_ALWAYS, "Error Code: %d", GetLastError());
    return;
  }

  if (!Process32First(snapshot, &procentry)) {
    CloseToolhelp32Snapshot(snapshot);
    msglog->log(LOG_ALWAYS, "Could not determine first process");
    return;
  }

  do {
    TCHAR szBufW[MAX_PATH * 2];
    wsprintf(szBufW, L"%08X   [%s]", procentry.th32ProcessID,
             procentry.szExeFile);
    AddToComboBox(hwnd, item, szBufW);
  } while (Process32Next(snapshot, &procentry));

  CloseToolhelp32Snapshot(snapshot);
  return;

}

bool TrayMessage(HWND hwnd, DWORD dwMessage, UINT uID, HICON hIcon,
                 PTSTR pszTip) {
  bool res = FALSE;
  NOTIFYICONDATA tnd;

  tnd.cbSize = sizeof(NOTIFYICONDATA);
  tnd.hWnd = hwnd;
  tnd.uID = uID;
  tnd.uFlags = NIF_MESSAGE | NIF_ICON;
  tnd.uCallbackMessage = TRAY_NOTIFYICON;
  tnd.hIcon = hIcon;
  tnd.szTip[0] = '\0';

  res = (Shell_NotifyIcon(dwMessage, &tnd) != 0);
  return res;
}

void TrayIconAdd(HWND hwnd, UINT uID, HICON hIcon, PTSTR pszTip) {
  TrayMessage(hwnd, NIM_ADD, uID, hIcon, NULL);
}

void TrayIconDelete(HWND hwnd, UINT uID, HICON hIcon, PTSTR pszTip) {
  TrayMessage(hwnd, NIM_DELETE, uID, hIcon, NULL);
}

int GetIP(char* inStrIp, int maxChars) {
  char* strIp;
  WSADATA wsaData;
  int nErrorCode = WSAStartup(MAKEWORD(1, 1), &wsaData);
  if (nErrorCode != 0) {
    return 0;
  }

  char strHostName[81];
  if (gethostname(strHostName, 80) == 0) {
    hostent* pHost = gethostbyname(strHostName);
    if (pHost->h_addrtype == AF_INET) {
      in_addr** ppip = (in_addr**) pHost->h_addr_list;
      while (*ppip) {
        in_addr ip = **ppip;

        strIp = inet_ntoa(ip);
        ppip++;
        if (strlen(strIp) != 0) {
          break;
        }
      }
    }
  }
  WSACleanup();
  strncpy(inStrIp, strIp, maxChars);
  return strlen(strIp);
}

void HideGui() {
  if (!g_WinHide) {
    ShowWindow(hDlg, SW_HIDE);
    g_WinHide = TRUE;
  }
}

void EnableDebugControls(bool enable) {
  EnableWindow(GetDlgItem(hDlg, IDLOADFILE), enable);
  EnableWindow(GetDlgItem(hDlg, IDATTACH), enable);
  EnableWindow(GetDlgItem(hDlg, IDDETACH), !enable);
}

void StopDebugger() {
  g_dbgactive = false;

  if (debugger != NULL) {
    SetEvent(hEventExit);
    if (WaitForSingleObject(debugger, 2000) == WAIT_TIMEOUT)
      TerminateThread(debugger, 0);

    CloseHandle(debugger);
  }

  EnableDebugControls(true);
  msglog->log(LOG_ALWAYS, "User aborted.");
}

bool Close(HWND hwnd) {
  int answer = MessageBox(hwnd, L"Quit BinNavi Debug-Client?",
                          L"zynamics BinNavi", MB_YESNO);
  if (answer == IDYES) {
    g_dbgactive = false;

    StopDebugger();
    WSACleanup();
    TrayIconDelete(hDlg, ID_TRAY,
                   LoadIcon(g_hInst, MAKEINTRESOURCE(IDI_NAVI_GUI)), NULL);
    EndDialog(hwnd, TRUE);
  }
  return true;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,
    LPTSTR lpCmdLine, int nCmdShow) {
  //	MSG msg;
  HACCEL hAccelTable;
  int retcode;
  HWND hwnd;

  if (hwnd = FindWindow(NULL, L"BinNavi")) {
    SetFocus(hwnd);
    return (TRUE);
  }

  g_hInst = hInstance;

  InitCommonControls();
  hAccelTable = LoadAccelerators(hInstance, (LPCTSTR) IDI_NAVI_GUI);

  retcode = DialogBox(g_hInst, MAKEINTRESOURCE(IDD_BINNAVI_PROCESSES), NULL,
      (DLGPROC) BasicDlgProc);

  return false;
}

BOOL WINAPI BasicDlgProc(HWND hwnd, UINT msg, WPARAM wp, LPARAM lp) {
  LRESULT lResult = TRUE;
  int wmId, wmEvent;

  TCHAR szBufW[MAX_PATH * 2];
  LRESULT res;
  unsigned int pid;

  hDlg = hwnd;

  switch (msg) {
    case WM_COMMAND:
      wmId = LOWORD(wp);
      wmEvent = HIWORD(wp);
      // Parse the menu selections:
      switch (wmId) {
        case IDOK: {
          //Hide window
          HideGui();
          return (TRUE);
        }
          break;

        case IDMINIMIZE: {
          g_Minimize = !g_Minimize;
        }
          break;

        case IDOPENFILE: {
          OPENFILENAME* ofn =
              reinterpret_cast<OPENFILENAME*>(
                  GlobalAlloc(GPTR, sizeof(OPENFILENAME)));
          wchar_t* path = reinterpret_cast<wchar_t*>(GlobalAlloc(
              GPTR, (MAX_LOADSTRING + 1) * sizeof(wchar_t)));

          ofn->lStructSize = sizeof(OPENFILENAME);
          ofn->hwndOwner = hDlg;
          ofn->lpstrFilter = L"Executable\0*.exe\0";
          ofn->lpstrFile = path;
          ofn->nMaxFile = MAX_LOADSTRING;
          ofn->lpstrInitialDir = L"\\";
          ofn->lpstrTitle = L"Open file to debug...";
          ofn->Flags = OFN_FILEMUSTEXIST | OFN_PATHMUSTEXIST;

          if (GetOpenFileNameW(ofn))
            SetDlgItemTextW(hDlg, IDEDITFILE, ofn->lpstrFile);

          GlobalFree(ofn);
          GlobalFree(path);

        }
          break;

        case IDLOADFILE: {
          int len = SendDlgItemMessage(hDlg, IDEDITFILE, WM_GETTEXTLENGTH, NULL,
                                       NULL);
          if (len > 0) {
            wchar_t* path =
                reinterpret_cast<wchar_t*>(GlobalAlloc(GPTR,
                                                       (len + 1) *
                                                       sizeof(wchar_t)));
            GetDlgItemTextW(hDlg, IDEDITFILE, path, len + 1);

            if (g_Minimize == true) {
              HideGui();
            }

            if (debugger != NULL) {
              CloseHandle(debugger);
              debugger = 0;
            }

            debugger = CreateThread(NULL, 0,
                                    (LPTHREAD_START_ROUTINE) & debug_loop_start,
                                    reinterpret_cast<void*>(path), 0, NULL);

            if (debugger != NULL) {
              msglog->log(LOG_ALWAYS, "Debugger successfully created.");
            } else {
              msglog->log(LOG_ALWAYS, "Failed to create debugger.");
            }
          }
          return true;
        }
          break;

        case IDATTACH:
          if (g_dbgactive) {
            //MessageBox(hwnd, L"Debugger is active. Stop debugger first!",
            //L"Warning", MB_OK | MB_ICONWARNING);
            msglog->log(LOG_ALWAYS, "Stop active session first!");
            return true;
          }

          hwBox = GetDlgItem(hwnd, IDC_PROCESSES);

          res = ComboBox_GetCurSel(hwBox);

          if (res == LB_ERR) {
            //msglog->log( LOG_ALWAYS, "No process selected!" );
            AddToComboBox(hwnd, IDC_LOG, L"No process selected!");
            return true;
          }

          res = ComboBox_GetLBText(hwBox, res, szBufW);

          if (res == LB_ERR || res < 8) {
            msglog->log(LOG_ALWAYS, "No valid process selected!");
            return true;
          }
          //res = number of characters

          szBufW[8] = '\0';
          szBufW[9] = '\0';

          swscanf(szBufW, L"%x", &pid);

          if (debugger != NULL) {
            CloseHandle(debugger);
            debugger = 0;
          }

          debugger = CreateThread(NULL, 0,
                                  (LPTHREAD_START_ROUTINE) & debug_loop_attach,
                                  reinterpret_cast<void*>(pid), 0, NULL);
          if (debugger != NULL) {
            msglog->log(LOG_ALWAYS, "Debugger successfully created.");
            if (g_Minimize == true) {
              HideGui();
            }
          } else {
            msglog->log(LOG_ALWAYS, "Failed to create debugger.");
          }
          return true;
          break;

        case IDREFRESH:
          ClearComboBox(hwnd, IDC_PROCESSES);
          FillProcessList(hwnd, IDC_PROCESSES);
          return true;
          break;

        case IDDETACH:

          EnableWindow(GetDlgItem(hDlg, IDDETACH), false);
          StopDebugger();
          return true;
          break;

        case IDCLOSE:
          return Close(hwnd);
          break;
      }
      return false;
      break;

    case WM_INITDIALOG:

      SHINITDLGINFO shidi;
      // Create a Done button and size it.
      shidi.dwMask = SHIDIM_FLAGS;
      shidi.dwFlags = SHIDIF_DONEBUTTON | SHIDIF_SIPDOWN
          | SHIDIF_SIZEDLGFULLSCREEN;
      shidi.hDlg = hDlg;
      //initialzes the dialog based on the dwFlags parameter
      SHInitDialog(&shidi);

      //set tray-icon
      TrayIconAdd(hDlg, ID_TRAY,
                  LoadIcon(g_hInst, MAKEINTRESOURCE(IDI_NAVI_GUI)), NULL);

      initLogger();

      msglog->log(LOG_ALWAYS, "Welcome to Zynamics BinNavi2.");

      //get the device's ip and print it
      char strIp[15 + 1 + 4 + 1];
      if (GetIP(strIp, 16) == 0) {
        msglog->log(LOG_ALWAYS, "ERROR: got no ip");
      } else {
        strcat(strIp, ":2222");
        msglog->log(LOG_ALWAYS, "This client is listening on:");
        msglog->log(LOG_ALWAYS, strIp);
      }

      FillProcessList(hwnd, IDC_PROCESSES);
      SetForegroundWindow((HWND)((ULONG) hwnd | 0x01));

      //create event to signal debug-thread when to stop
      hEventExit = CreateEvent(NULL, true, false, EVENT_EXIT_NAME);

      return true;
      break;

    case WM_CLOSE:
      return Close(hwnd);
      break;

    case WM_DESTROY: {
      if (debugger != 0)
        CloseHandle(debugger);
      //MessageBox(0, L"bye",L"bye",0);
      //WaitForSingleObject( debugger, INFINITE );
    }
      return true;
      break;

    case TRAY_NOTIFYICON:
      switch (lp) {
        case WM_LBUTTONDOWN:
          if (wp == ID_TRAY) {
            //g_PalmTrayTaskListActive = FALSE;
            if (g_WinHide) {
              //Show the List of Processes
              //SendMessage(hDlg, WM_COMMAND, (WPARAM)IDC_REFRESHBUTTON, 0);
              ShowWindow(hDlg, SW_SHOW);
              SetForegroundWindow(hDlg);  // make us come to the front
              g_WinHide = FALSE;
            } else {
              g_WinHide = TRUE;
            }
          }
      }
      break;

  }
  return false;
}

DWORD debug_loop_attach(unsigned int ulPID) {
  unsigned int ulPort;
  HANDLE hProcessCheck;

  bool attached;

  g_dbgactive = true;
  DWORD code = 0;

  ulPort = 2222;

  EnableDebugControls(false);

  attached = false;
  while (g_dbgactive) {
    //check if process exists
    hProcessCheck = OpenProcess(0, false, ulPID);
    if (hProcessCheck == NULL) {
      msglog->log(LOG_ALWAYS, "ERROR: Process does not exist.");
      ExitThread(-1);
    }
    msglog->log(LOG_ALWAYS, "Waiting for incoming connection.");
    std::unique_ptr<DebugClient> debugClient;

    debugClient = std::unique_ptr<DebugClient>(
        new DebugClient(new CONNECTION_POLICY(ulPort),
        new SYSTEM_POLICY(ulPID)));

    unsigned int init = debugClient->initializeConnection();

    if (init) {
      ExitThread(0);
    }
    unsigned int connected = debugClient->waitForConnection();

    if (connected) {
      EnableDebugControls(true);
      ExitThread(-1);
    }

    if (connected == EXIT_PROGRAM) {
      EnableDebugControls(true);
      ExitThread(0);
    }

    msglog->log(LOG_ALWAYS, "Got connection.");

    //check if we're already attached to the target process
    if (attached == false) {
      //we got a first-time-connection => we need to attach to target process
      unsigned int _attached = debugClient->attachToProcess();
      if (_attached) {
        g_dbgactive = false;
        debugClient->closeConnection();
        msglog->log(LOG_ALWAYS, "ERROR: Attaching to process failed.");
        EnableDebugControls(true);
        ExitThread(-1);
      }

      msglog->log(LOG_ALWAYS, "Debugger is active.");

      //we're attached :)
      attached = true;
    } else {
      //not a first-time-connection => just change flags
    }

    unsigned int procp = debugClient->processPackets();
    if (procp) {
      debugClient->closeConnection();
      EnableDebugControls(true);
      msglog->log(LOG_ALWAYS, "ERROR: Failed while processing packets");
      ExitThread(-1);
    }
    debugClient->closeConnection();
    msglog->log(LOG_ALWAYS, "Recording stopped.");
  }
  g_dbgactive = false;

  EnableDebugControls(true);
  ExitThread(0);
  return 0;
}

DWORD debug_loop_start(const wchar_t* path) {
  unsigned int ulPort;
  bool attached;

  g_dbgactive = true;
  DWORD code = 0;

  ulPort = 2222;

  EnableDebugControls(false);

  HANDLE hFileCheck = CreateFile(path, 0, 0, 0, OPEN_EXISTING, 0, 0);
  if (hFileCheck == NULL) {
    msglog->log(LOG_ALWAYS, "ERROR: File does not exist.");
    ExitThread(-1);
  }
  CloseHandle(hFileCheck);

  attached = false;
  while (g_dbgactive) {
    msglog->log(LOG_ALWAYS, "Waiting for incoming connection.");
    std::unique_ptr<DebugClient> debugClient;

    const std::vector<const NATIVE_STRING> commands;
    debugClient = std::unique_ptr<DebugClient>(
        new DebugClient(new CONNECTION_POLICY(ulPort),
                        new SYSTEM_POLICY(path, commands)));

    unsigned int init = debugClient->initializeConnection();

    if (init) {
      std::cout << "INIT " << std::endl;
      ExitThread(-1);
    }
    unsigned int connected = debugClient->waitForConnection();

    if (connected == -1) {
      EnableDebugControls(true);
      ExitThread(-1);
    }

    if (connected == EXIT_PROGRAM) {
      EnableDebugControls(true);
      ExitThread(-1);
    }

    msglog->log(LOG_ALWAYS, "Got connection.");

    //check if we're already attached to the target process
    if (attached == false) {
      //we got a first-time-connection => we need to attach to target process
      unsigned int _attached = debugClient->attachToProcess();
      if (_attached) {
        g_dbgactive = false;
        debugClient->closeConnection();
        msglog->log(LOG_ALWAYS, "ERROR: Attaching to process failed.");
        EnableDebugControls(true);
        ExitThread(-1);
      }

      msglog->log(LOG_ALWAYS, "Debugger is active.");
      attached = true;
    }

    unsigned int procp = debugClient->processPackets();
    if (procp) {
      debugClient->closeConnection();
      EnableDebugControls(true);
      msglog->log(LOG_ALWAYS, "ERROR: Failed while processing packets");
      ExitThread(-1);
    }
    debugClient->closeConnection();
    msglog->log(LOG_ALWAYS, "Recording stopped.");
  }
  g_dbgactive = false;

  EnableDebugControls(true);
  ExitThread(0);
  return 0;
}
