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

#include "toolhelp.h"

#include <iostream>

bool listProcessModulesToolhelp(DWORD dwPID, std::vector<MODULEENTRY32>& modules)
{
	HANDLE hModuleSnap = INVALID_HANDLE_VALUE;

	// Take a snapshot of all modules in the specified process.
	hModuleSnap = CreateToolhelp32Snapshot(TH32CS_SNAPMODULE, dwPID);

	if(hModuleSnap == INVALID_HANDLE_VALUE)
	{
		std::cout << "INVALID HANDLE" << std::endl;
		return false;
	}

	MODULEENTRY32 me32;

	// Set the size of the structure before using it.
	me32.dwSize = sizeof(MODULEENTRY32);

	// Retrieve information about the first module,
	// and exit if unsuccessful
	if(!Module32First(hModuleSnap, &me32))
	{
		CloseHandle(hModuleSnap);     // Must clean up the snapshot object!
		return false;
	}

	// Now walk the module list of the process,
	// and display information about each module
	do
	{
		modules.push_back(me32);
	} while(Module32Next(hModuleSnap, &me32));

	// Do not forget to clean up the snapshot object.
	CloseHandle(hModuleSnap);
	return true;
}

bool listProcessThreadsToolhelp(DWORD dwPID, std::vector<THREADENTRY32>& threads)
{
	HANDLE hThreadSnap = INVALID_HANDLE_VALUE;

	// Take a snapshot of all modules in the specified process.
	hThreadSnap = CreateToolhelp32Snapshot(TH32CS_SNAPTHREAD, dwPID);

	if(hThreadSnap == INVALID_HANDLE_VALUE)
	{
		std::cout << "INVALID HANDLE" << std::endl;
		return false;
	}

	THREADENTRY32 thread32;

	// Set the size of the structure before using it.
	thread32.dwSize = sizeof(MODULEENTRY32);

	// Retrieve information about the first thread,
	// and exit if unsuccessful
	if(!Thread32First(hThreadSnap, &thread32))
	{
		CloseHandle(hThreadSnap);     // Must clean up the snapshot object!
		return false;
	}

	// Now walk the thread list of the process,
	// and display information about each thread
	do
	{
		threads.push_back(thread32);
	} while(Thread32Next(hThreadSnap, &thread32));

	// Do not forget to clean up the snapshot object.
	CloseHandle(hThreadSnap);
	return true;
}

bool listProcessModules(HANDLE hProcess, std::vector<std::pair<HMODULE, std::string> >& modules)
{
    DWORD bytesNeeded;
    HMODULE moduleHandles[1024];

    if (EnumProcessModules(hProcess, moduleHandles, sizeof( moduleHandles ), &bytesNeeded ))
    {
		for ( unsigned int i = 0; i < bytesNeeded / sizeof( HMODULE ); ++i )
		{
			char moduleName[MAX_PATH];

			if (GetModuleFileNameEx( hProcess, moduleHandles[i], moduleName, sizeof( moduleName ) / sizeof( moduleName[0] ) ) )
			{
				modules.emplace_back(moduleHandles[i], moduleName);
            }
        }
  
        return true;
    }
    else
    {
	    return false;
    }
}

bool GetFileNameFromHandle(HANDLE hFile, std::string& filename)
{
	bool bSuccess = false;
	char pszFilename[MAX_PATH+1];
	HANDLE hFileMap;

	// Get the file size.
	DWORD dwFileSizeHi = 0;
	DWORD dwFileSizeLo = GetFileSize(hFile, &dwFileSizeHi);

	if( dwFileSizeLo == 0 && dwFileSizeHi == 0 )
	{
		return false;
	}

	// Create a file mapping object.
	hFileMap = CreateFileMapping(hFile, NULL, PAGE_READONLY, 0, 1, NULL);

	if (hFileMap)
	{
		// Create a file mapping to get the file name.
		void* pMem = MapViewOfFile(hFileMap, FILE_MAP_READ, 0, 0, 1);

		if (pMem)
		{
			if (GetMappedFileName(GetCurrentProcess(), pMem, pszFilename, MAX_PATH))
			{
				// Translate path with device name to drive letters.
				TCHAR szTemp[512];
				szTemp[0] = '\0';

				if (GetLogicalDriveStrings(512-1, szTemp))
				{
					TCHAR szName[MAX_PATH];
					TCHAR szDrive[3] = TEXT(" :");
					BOOL bFound = FALSE;
					TCHAR* p = szTemp;

					do
					{
						// Copy the drive letter to the template string
						*szDrive = *p;

						// Look up each device name
						if (QueryDosDevice(szDrive, szName, MAX_PATH))
						{
							UINT uNameLen = strlen(szName);

							if (uNameLen < MAX_PATH)
							{
								bFound = _strnicmp(pszFilename, szName, uNameLen) == 0;

								if (bFound)
								{
									// Reconstruct pszFilename using szTempFile
									// Replace device path with DOS path
									char szTempFile[MAX_PATH];
#ifdef WIN32
                  sprintf_s(szTempFile, sizeof(szTempFile), TEXT("%s%s"), szDrive, pszFilename+uNameLen);
                  strcpy_s(pszFilename, sizeof(pszFilename), szTempFile);
#else
									sprintf(szTempFile, TEXT("%s%s"), szDrive, pszFilename+uNameLen);
                  strcpy(pszFilename, szTempFile);
#endif
								}
							}
						}
					// Go to the next NULL character.
					while (*p++);
					} while (!bFound && *p); // end of string
				}
			}
			bSuccess = TRUE;
			UnmapViewOfFile(pMem);
		}

		CloseHandle(hFileMap);
	}

	filename = pszFilename;

	return bSuccess;
}
