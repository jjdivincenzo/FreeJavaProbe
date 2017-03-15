// JavaOSInfo.cpp : Defines the initialization routines for the DLL.
//

#include "stdafx.h"
#include "stdio.h"
#include "systeminfo.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
	debug = getDebug();
	// Perform actions based on the reason for calling.
    switch( ul_reason_for_call ) 
    { 
        case DLL_PROCESS_ATTACH:
			debugMsg("DLL Library Debug Logging is ON");
			/*
			Not all Windows OS's support the same Kernel, PSAPI and NTDLL calls. We will dynamically load these
			Functions so that the DLL will load. Otherwise, when the JavaOSInfo dll is loaded, if all of the
			external references can not be satisfied, the DLL load will fail.
			*/
			debugMsg("Loading KERNEL32.DLL");
			kernelLib = LoadLibrary(TEXT("KERNEL32"));
			dumpLibAddress("KERNEL32.DLL", kernelLib);
			if(kernelLib != NULL) { 
				cntlProcAddr = (CNTLC) GetProcAddress(kernelLib, "GenerateConsoleCtrlEvent");
				getCurCpuNo = (GET_CUR_CPU_NO) GetProcAddress(kernelLib, "GetCurrentProcessorNumber");
				getProcessHandleCount = (GET_PROC_HANDLE_COUNT) GetProcAddress(kernelLib, "GetProcessHandleCount");
				isWOW64 = (IW64PFP) GetProcAddress(kernelLib, "IsWow64Process");
				dumpProcAddress("GenerateConsoleCtrlEvent",cntlProcAddr);
				dumpProcAddress("GetCurrentProcessorNumber",getCurCpuNo);
				dumpProcAddress("GetProcessHandleCount",getProcessHandleCount);
			}
			
			debugMsg("Loading PSAPI.DLL");
			psapiLib = LoadLibrary(TEXT("PSAPI"));
			dumpLibAddress("PSAPI.DLL", psapiLib);
			if (psapiLib != NULL) {
				getProcMemInfo = (GET_PROC_MEM_INFO) GetProcAddress(psapiLib, "GetProcessMemoryInfo");
				emptyWS = (EMPTY_WS) GetProcAddress(psapiLib, "EmptyWorkingSet");
				dumpProcAddress("GetProcessMemoryInfo",getProcMemInfo);
				dumpProcAddress("EmptyWorkingSet",emptyWS);
			}
			
			debugMsg("Loading NTDLL.DLL");
			ntdllLib = LoadLibrary(TEXT("NTDLL"));
			dumpLibAddress("NTDLL.DLL", ntdllLib);
			if (ntdllLib != NULL) {											   
				getCurrentProcNo = (GET_CUR_PROC_NO) GetProcAddress(ntdllLib, "NtGetCurrentProcessorNumber");
				
			}
			dumpProcAddress("NtGetCurrentProcessorNumber",getCurrentProcNo);
            break;

        case DLL_THREAD_ATTACH:
         // Do thread-specific initialization.
            break;

        case DLL_THREAD_DETACH:
         // Do thread-specific cleanup.
            break;

        case DLL_PROCESS_DETACH:
			
			debugMsg("Unloading KERNEL32.DLL");
			if (kernelLib != NULL) FreeLibrary(kernelLib);
			debugMsg("Unloading PSAPI.DLL");
			if (psapiLib != NULL) FreeLibrary(psapiLib);
			debugMsg("Unloading NTDLL.DLL");
			if (ntdllLib != NULL) FreeLibrary(ntdllLib);
			
            break;
    }
    return TRUE;  // Successful DLL_PROCESS_ATTACH.
}
