#include "SystemInfo.h"
#include "stdio.h"
#include <tlhelp32.h>
#include <sstream>

#define MAX_NAME 256

BOOL elevatedPriv = FALSE;

BOOL getIOCounters(PIO_COUNTERS counters) {

	HANDLE proc = GetCurrentProcess();
	BOOL resp = GetProcessIoCounters(proc, counters);
	CloseHandle(proc);
	return resp;
}

BOOL getCPUTimes(DWORD* tUser, DWORD* tKernel) {

	DWORD pid = GetCurrentProcessId();

	return getCPUTimes(pid, tUser, tKernel);
}

BOOL getCPUTimes(DWORD pid, DWORD* tUser, DWORD* tKernel) {

	FILETIME ftCreate, ftExit, ftKernel, ftUser;
	
	HANDLE hProcess =  OpenProcess (PROCESS_QUERY_INFORMATION, FALSE, pid);
	BOOL resp = TRUE;	

	if ( GetProcessTimes ( hProcess, &ftCreate, &ftExit, &ftKernel, &ftUser))
    {
        
        LONGLONG tUser64 = *(LONGLONG *)&ftUser;
        LONGLONG tKernel64 = *(LONGLONG *)&ftKernel;
    
        // The LONGLONGs contain the time in 100 nanosecond intervals (now
        // there's a useful unit of measurement...).  Divide each of them by
        // 10000 to convert into milliseconds, and store the results in a
        // DWORD.  This means that the max time before overflowing is around
        // 4 Million seconds (about 49 days)
        *tUser = (DWORD)(tUser64 / 10000);
  	    *tKernel = (DWORD)(tKernel64 / 10000);
		      
		
    }
	else
	{
		resp = FALSE;
	}
	CloseHandle(hProcess);
	return resp;

}
BOOL getDebug(void) {

	char buff[100];

	DWORD numc = GetEnvironmentVariable("JPROBE_DLL_DEBUG",(LPSTR) &buff, sizeof(buff));
	BOOL debug = FALSE;

	if (numc == 0) return FALSE;
	if (strcmp(buff,"true") == 0 || strcmp(buff,"TRUE") == 0 ) {
		debug = TRUE;
		
	}
	return debug;
}
void debugMsg(char* out) {

	if (debug) {
		char time[30];
		//DWORD tid = GetCurrentThreadId();
		getTime(time);
		printf("<JavaOSInfo.dll>(%s)T[%d] - %s\n", time, GetCurrentThreadId(), out);
	}
}
void dumpProcAddress(char* name, void* ptr) {

	char buff[300];
	sprintf_s(buff, "    Procedure %s is at 0x%p", name, ptr);
	debugMsg(buff);
}

void dumpLibAddress(char* name, void* ptr) {

	char buff[300];
	sprintf_s(buff, "Library %s loaded at 0x%p", name, ptr);
	debugMsg(buff);
}


DWORD handleError( char* msg )
{
  
	DWORD eNum;
	char sysMsg[256];
	char* p;

	eNum = GetLastError( );

	if (!debug) return eNum;

	FormatMessage(
         FORMAT_MESSAGE_FROM_SYSTEM | FORMAT_MESSAGE_IGNORE_INSERTS,
         NULL, 
		 eNum,
         MAKELANGID(LANG_NEUTRAL, SUBLANG_DEFAULT), // Default lang.
         sysMsg, 
		 256, 
		 NULL );

	// Trim the end of the line and terminate it with a null
	p = sysMsg;

	while( ( *p > 31 ) || ( *p == 9 ) )	++p;

	do { *p-- = 0; } while( ( p >= sysMsg ) &&
                          ( ( *p == '.' ) || ( *p < 33 ) ) );

	char buff[300];
	sprintf_s(buff, "System Call [%s], failed with error code %d, (%s)",
    msg, eNum, sysMsg );
	debugMsg(buff);

    return eNum;
}
void getTime(char* buf)
{
	time_t time(time_t *time);
	time_t ltime;
    struct tm *today;
	
	
	memset(buf,0,20);
    
	time( &ltime );
    today = localtime( &ltime );
	strftime(buf, 20, "%H:%M:%S",today);
		
}
void dumpThreadTime() {

	if (!debug) return;
	DWORD pid = GetCurrentProcessId();
			
	HANDLE hThreadSnap = INVALID_HANDLE_VALUE;
    THREADENTRY32 te32;
	
    hThreadSnap = CreateToolhelp32Snapshot( TH32CS_SNAPTHREAD, 0 );

	if( hThreadSnap == INVALID_HANDLE_VALUE )
    {
		handleError("CreateToolhelp32Snapshot");
        return;
    }

	te32.dwSize = sizeof( THREADENTRY32 );

   // Retrieve information about the first thread,
   // and exit if unsuccessful
	if( !Thread32First( hThreadSnap, &te32 ) ) {
         handleError("Thread32First");
		 CloseHandle( hThreadSnap );    // Must clean up the
         return;
    }

	do {

		HANDLE hThread;
		DWORD tid = te32.th32ThreadID;   

		
		hThread = OpenThread(READ_CONTROL | THREAD_QUERY_INFORMATION, FALSE, tid); 

		if (hThread == NULL) {
			continue;
		}
		
		DWORD tpid = te32.th32OwnerProcessID;
		
		//only want threads associated with our process...
		if (tpid != pid) continue;

		FILETIME ct, xt, kt, ut;
		
		BOOL resp = GetThreadTimes(hThread, &ct, &xt, &kt, &ut);
		if (!resp) {
			handleError("GetThreadTimes");
			continue;
		}
		
		    
		DWORD tUser = ut.dwLowDateTime / 10000;
		DWORD tKernel = kt.dwLowDateTime / 10000;
		

		printf("For Thread(%d)\n", tid);
		printf("\tThe Thread Kernel time is: %03dms\n", tKernel);
		printf("\tThe Thread User time is: %03dms\n", tUser);


	} while( Thread32Next( hThreadSnap, &te32 ) );
	CloseHandle( hThreadSnap );
	
}
string getProcessInfoData(PROCESSENTRY32 pe32, BOOL allUsers) {

	stringstream ss;
	IO_COUNTERS io;
	DWORD tUser;
	DWORD tKernel;
	PROCESS_MEMORY_COUNTERS_EX pmc;
	DWORD pid = pe32.th32ProcessID;
	DWORD hCount;
	DWORD err;
	BOOL wow64;
	
	if (allUsers) {
		EnableDebugPriv();
	} else {
		DisableDebugPriv();
	}

	//HANDLE hProcess =  OpenProcess (PROCESS_QUERY_INFORMATION, FALSE, pid);
	HANDLE hProcess =  OpenProcess (PROCESS_ALL_ACCESS | READ_CONTROL | PROCESS_TERMINATE, FALSE  , pid);
	/*
	*	Not authorized if null
	*/
	if (hProcess == NULL) {
		err = GetLastError();
		return ss.str();
	}

	/*
	*	Get Process Basic Info
	*/

	ss << "processid=" << 
		pid << ";parentpid=" << 
		pe32.th32ParentProcessID << 
		";processname=" << pe32.szExeFile << 
		";threadcount=" << pe32.cntThreads << ";";

	
	/*
	* If runing in wow 64, it must be a 32 bit process
	*/
	if (isWOW64 != NULL) {

		if ((isWOW64) (hProcess, &wow64)) {
			if (wow64) {
				ss << "imagetype=32bit;";
			} else {
				ss << "imagetype=64bit;";
			}
		}
	} else {
		ss << "imagetype=32bit;";
	}


	if (getProcessHandleCount(hProcess, &hCount)) {
		ss << "handlecount=" << hCount << ";";
	}
	string user = getUser(hProcess);
	if (user.length() > 0) {
		ss << "user=" << user << ";";
	}

	if (getCPUTimes(pid, &tUser, &tKernel)) {
		ss << "totalcpu=" << (tUser + tKernel) << ";usertime="
			<< tUser << ";kerneltime=" << tKernel << ";";
	}

	if (GetProcessIoCounters(hProcess, &io)) {
		ss << "ioreads=" <<  io.ReadOperationCount 
			<< ";iowrites=" << io.WriteOperationCount
			<< ";ioother=" << io.OtherOperationCount
			<< ";ioreadbytes=" << io.ReadTransferCount
			<< ";iowritebytes=" << io.WriteTransferCount
			<< ";iootherbytes=" << io.OtherTransferCount << ";";
	}

	if (getProcMemInfo != NULL) { 
		if ( (getProcMemInfo)( hProcess,(PPROCESS_MEMORY_COUNTERS) &pmc, sizeof(pmc))) {

			ss << "pagefaults=" << pmc.PageFaultCount
				<< ";workingsetsize=" << pmc.WorkingSetSize
				<< ";peakworkingsetsize=" << pmc.PeakWorkingSetSize
				<< ";pagefileusage=" << pmc.PagefileUsage
				<< ";peakpagefileusage=" << pmc.PeakPagefileUsage
				<< ";privateusage=" << pmc.PrivateUsage << ";";
		}
	}

	CloseHandle(hProcess);

	return ss.str();

}
void EnableDebugPriv()
{
    HANDLE hToken;
    LUID luid;
    TOKEN_PRIVILEGES tkp;
	DWORD err;

	if (elevatedPriv) {
		return;
	}
	elevatedPriv = TRUE;
    if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hToken)) {
		err = GetLastError();
	}

    if (!LookupPrivilegeValue(NULL, SE_DEBUG_NAME, &luid))
    {
       err = GetLastError();
        CloseHandle(hToken); 
        return;
    }

    tkp.PrivilegeCount = 1;
    tkp.Privileges[0].Luid = luid;
    tkp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;

    if (!::AdjustTokenPrivileges(hToken, false, &tkp, sizeof(tkp), NULL, NULL))
    {
       err = GetLastError();
        CloseHandle(hToken); 
        return;
    }

    CloseHandle(hToken);
   
}
void DisableDebugPriv()
{
    HANDLE hToken;
    LUID luid;
    TOKEN_PRIVILEGES tkp;
	DWORD err;

	if (!elevatedPriv) {
		return;
	}
	elevatedPriv = FALSE;
    if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hToken)) {
		err = GetLastError();
	}

    if (!LookupPrivilegeValue(NULL, SE_DEBUG_NAME, &luid))
    {
       err = GetLastError();
        CloseHandle(hToken); 
        return;
    }

    tkp.PrivilegeCount = 1;
    tkp.Privileges[0].Luid = luid;
    tkp.Privileges[0].Attributes = SE_PRIVILEGE_REMOVED;

    if (!::AdjustTokenPrivileges(hToken, false, &tkp, sizeof(tkp), NULL, NULL))
    {
       err = GetLastError();
        CloseHandle(hToken); 
        return;
    }

    CloseHandle(hToken);
   
}
string getUser(HANDLE hProcess) {

	DWORD dwSize = MAX_NAME;
	HANDLE hToken = NULL;
	PTOKEN_USER ptu = NULL;
	DWORD dwLength = 0;
	SID_NAME_USE SidType;
    char lpName[MAX_NAME];
    char lpDomain[MAX_NAME];
	stringstream ss;

	if (!OpenProcessToken( hProcess, TOKEN_QUERY, &hToken )) {
		return "";
	}

	if (!GetTokenInformation(hToken, TokenUser,   
         (LPVOID) ptu,  0, &dwLength )) {
      if (GetLastError() != ERROR_INSUFFICIENT_BUFFER) {
		  return "";
		}
	}

	ptu = (PTOKEN_USER)HeapAlloc(GetProcessHeap(),
         HEAP_ZERO_MEMORY, dwLength);

	if (GetTokenInformation(hToken,
			 TokenUser,   
			 (LPVOID) ptu,   
			 dwLength,  
			 &dwLength)) {
		if(LookupAccountSid( NULL , ptu->User.Sid, lpName, &dwSize, lpDomain, &dwSize, &SidType )) {
			ss << lpDomain << "\\" << lpName;
		}
	} else {
		DWORD err = GetLastError();
	}

	CloseHandle(hToken);
    HeapFree(GetProcessHeap(), 0, (LPVOID)ptu);
	return ss.str();
}