// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once


#define WIN32_LEAN_AND_MEAN		// Exclude rarely-used stuff from Windows headers
#define _WIN32_WINNT 0x0501
// Windows Header Files:
#include <windows.h>
#include <windowsx.h> 
#include "psapi.h"


// TODO: reference additional headers your program requires here
extern BOOL debug;

//kernel32.dll library and functions
extern HINSTANCE kernelLib;

typedef BOOL (WINAPI *CNTLC)(DWORD, DWORD); //GenerateConsoleCtrlEvent() function
extern CNTLC cntlProcAddr;
typedef DWORD (WINAPI *GET_CUR_CPU_NO)(); //GetCurrentProcessorNumber() function
extern GET_CUR_CPU_NO getCurCpuNo;
typedef DWORD (WINAPI *GET_PROC_HANDLE_COUNT)(HANDLE, PDWORD); //GetProcessHandleCount() function
extern GET_PROC_HANDLE_COUNT getProcessHandleCount;
typedef BOOL (WINAPI *IW64PFP)(HANDLE, BOOL *);
extern IW64PFP isWOW64;


//psapi.dll library and functions
extern HINSTANCE psapiLib;
typedef BOOL (WINAPI *GET_PROC_MEM_INFO) (HANDLE, PPROCESS_MEMORY_COUNTERS, DWORD);
extern GET_PROC_MEM_INFO getProcMemInfo;
typedef BOOL (WINAPI *EMPTY_WS) (HANDLE);
extern EMPTY_WS emptyWS;

//ntdll.dll library and functions
extern HINSTANCE ntdllLib;
typedef ULONG (WINAPI *GET_CUR_PROC_NO) ();
extern GET_CUR_PROC_NO getCurrentProcNo;

