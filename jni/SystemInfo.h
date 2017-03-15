#include "stdafx.h"
#include <time.h>
#include <string>
#include <tlhelp32.h>

using namespace std;
//typedef BOOL (WINAPI *CNTLC)(DWORD, DWORD);


BOOL getIOCounters(PIO_COUNTERS);

BOOL getCPUTimes(DWORD* tUser, DWORD* tKernel);
BOOL getCPUTimes(DWORD pid, DWORD* tUser, DWORD* tKernel);

BOOL getDebug(void);
void debugMsg(char*);
DWORD handleError(char*);
void dumpProcAddress(char*, void*);
void dumpLibAddress(char*, void*);
void getTime(char*);
void dumpThreadTime();
string getProcessInfoData(PROCESSENTRY32, BOOL);
void EnableDebugPriv();
void DisableDebugPriv();
string getUser(HANDLE); 

