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

#ifndef __NEWRES_H__
#define __NEWRES_H__

#pragma once

#if !defined(UNDER_CE)
#define UNDER_CE _WIN32_WCE
#endif

#if defined(_WIN32_WCE)
#if !defined(WCEOLE_ENABLE_DIALOGEX)
#define DIALOGEX DIALOG DISCARDABLE
#endif
#include <commctrl.h>
#define SHMENUBAR RCDATA
#if defined(WIN32_PLATFORM_PSPC) && (_WIN32_WCE >= 300)
#include <aygshell.h>
#define AFXCE_IDR_SCRATCH_SHMENU 28700
#else
#define I_IMAGENONE (-2)
#define NOMENU 0xFFFF
#define IDS_SHNEW 1

#define IDM_SHAREDNEW 10
#define IDM_SHAREDNEWDEFAULT 11
#endif  // _WIN32_WCE_PSPC
#define AFXCE_IDD_SAVEMODIFIEDDLG 28701
#endif  // _WIN32_WCE
#ifdef RC_INVOKED
#ifndef _INC_WINDOWS
#define _INC_WINDOWS
#include "winuser.h"  // extract from windows header
#include "winver.h"
#endif
#endif

#ifdef IDC_STATIC
#undef IDC_STATIC
#endif
#define IDC_STATIC (-1)

#endif  //__NEWRES_H__
