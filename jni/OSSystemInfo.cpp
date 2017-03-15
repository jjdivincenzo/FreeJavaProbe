#include "stdafx.h"
#include "OSSystemInfo.h"
#include "SystemInfo.h"
#include "dlibs.h"
#include "psapi.h"
#include <tlhelp32.h>
#include "CpuUsage.h"
#include <sstream>
#include "Shellapi.h"


DWORD lpProcessAffinityMask = 0;
DWORD lpSystemAffinityMask = 0;


JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getCPUAffinity
(JNIEnv *, jclass) {

	debugMsg("Function getCPUAffinity()");
	
	HANDLE proc = GetCurrentProcess();

#ifdef WIN64
	BOOL res = GetProcessAffinityMask(proc, (PDWORD_PTR) &lpProcessAffinityMask, (PDWORD_PTR) &lpSystemAffinityMask	);
#else 
	BOOL res = GetProcessAffinityMask(proc, &lpProcessAffinityMask, &lpSystemAffinityMask	);
#endif

	CloseHandle(proc);
	
	return lpProcessAffinityMask;

}

JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_setCPUAffinity
(JNIEnv *, jclass, jlong mask) {

	debugMsg("Function setCPUAffinity()");
	DWORD newMask = (DWORD) mask;
	HANDLE proc = GetCurrentProcess();
		
	if	(SetProcessAffinityMask(proc, newMask ) == 0) {
		CloseHandle(proc);
		return GetLastError();
	}
	
	CloseHandle(proc);
	return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getNumberOfCPUs
(JNIEnv *, jclass) {
	
	debugMsg("Function getNumberOfCPUs()");
	SYSTEM_INFO si;
   	GetSystemInfo(&si);
	return si.dwNumberOfProcessors;
}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_setCPUAffinityToSystem
(JNIEnv *, jclass) {

	debugMsg("Function setCPUAffinityToSystem()");
	HANDLE proc = GetCurrentProcess();

#ifdef WIN64
	BOOL res = GetProcessAffinityMask(proc, (PDWORD_PTR) &lpProcessAffinityMask, (PDWORD_PTR) &lpSystemAffinityMask	);
#else 
	BOOL res = GetProcessAffinityMask(proc, &lpProcessAffinityMask, &lpSystemAffinityMask	);
#endif	

	if	(SetProcessAffinityMask(proc, lpSystemAffinityMask ) == 0) {
		CloseHandle(proc);
		return handleError("GetProcessAffinityMask");
	}

	CloseHandle(proc);
	return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}

JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getProcessHandleCount
(JNIEnv *, jclass) {

	debugMsg("Function getProcessHandleCount()");
	DWORD hCount;
	HANDLE proc = GetCurrentProcess();
	if (getProcessHandleCount == NULL) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_LIBRARY_LOOKUP_FAILED;

	BOOL res = getProcessHandleCount(proc, &hCount);

	if (res == 0) return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;

	CloseHandle(proc);
	return hCount;

}

JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getIOCounters
	(JNIEnv * ENV, jclass thisclass, jlongArray counters) {

		debugMsg("Function getIOCounters()");
		jboolean isCopy;
		jlong* longElements = ENV->GetLongArrayElements(counters, &isCopy);
		jint numElements = ENV->GetArrayLength(counters);

		IO_COUNTERS io;
		
		if (!getIOCounters(&io)) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
		if (numElements < 6) 
			return edu_regis_jprobe_jni_OSSystemInfo_INVALID_PARMS;

		longElements[0] = io.ReadOperationCount;
		longElements[1] = io.WriteOperationCount;
		longElements[2] = io.OtherOperationCount;
		longElements[3] = io.ReadTransferCount;
		longElements[4] = io.WriteTransferCount;
		longElements[5] = io.OtherTransferCount;
		
		if (isCopy == JNI_TRUE) {
			ENV->ReleaseLongArrayElements(counters, longElements, 0);
		}
	return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getProcessMemoryInfo
	(JNIEnv * ENV, jclass thisclass, jlongArray stats) {

		debugMsg("Function getProcessMemoryInfo()");
		jboolean isCopy;
		jlong* longElements = ENV->GetLongArrayElements(stats, &isCopy);
		jint numElements = ENV->GetArrayLength(stats);

		if (numElements < 6) return edu_regis_jprobe_jni_OSSystemInfo_INVALID_PARMS;

		HANDLE proc = GetCurrentProcess();
		PROCESS_MEMORY_COUNTERS_EX pmc;

		if (getProcMemInfo == NULL)  
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_LIBRARY_LOOKUP_FAILED;
		  
		if ( !(getProcMemInfo)( proc,(PPROCESS_MEMORY_COUNTERS) &pmc, sizeof(pmc))) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;

		longElements[0] = pmc.PageFaultCount;
		longElements[1] = pmc.WorkingSetSize;
		longElements[2] = pmc.PeakWorkingSetSize;
		longElements[3] = pmc.PagefileUsage;
		longElements[4] = pmc.PeakPagefileUsage;
		longElements[5] = pmc.PrivateUsage;
		
		if (isCopy == JNI_TRUE) {
			ENV->ReleaseLongArrayElements(stats, longElements, 0);
		}
		
		CloseHandle(proc);
		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getProcessTimes
	(JNIEnv *ENV, jclass thisclass, jlongArray times) {

		debugMsg("Function getProcessTimes()");
		jboolean isCopy;
		jlong* longElements = ENV->GetLongArrayElements(times, &isCopy);
		jint numElements = ENV->GetArrayLength(times);

		if (numElements < 2) return edu_regis_jprobe_jni_OSSystemInfo_INVALID_PARMS;

		if (!getCPUTimes((DWORD*)&longElements[0], (DWORD*) &longElements[1])) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;

		if (isCopy == JNI_TRUE) {
			ENV->ReleaseLongArrayElements(times, longElements, 0);
		}
		
		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_emptyWorkingSet
	(JNIEnv *, jclass) {

		debugMsg("Function emptyWorkingSet()");
		HANDLE proc = GetCurrentProcess();

		if (emptyWS == NULL)
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_LIBRARY_LOOKUP_FAILED; 

		
		BOOL res = emptyWS(proc);
  
		if (res == 0) return handleError("EmptyWorkingSet");
		CloseHandle(proc);
		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;

}

JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_sendCTLBreak
	(JNIEnv *, jclass) {

		debugMsg("Function sendCTLBreak()");
		if (cntlProcAddr == NULL) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_LIBRARY_LOOKUP_FAILED;

		BOOL res = (cntlProcAddr) (CTRL_BREAK_EVENT, 0);

		if (res == 0) return handleError("GenerateConsoleCtrlEvent");

		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}


JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_sendCTLC
	(JNIEnv *, jclass) {

		debugMsg("Function sendCTLC()");
		if (cntlProcAddr == NULL) 
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_LIBRARY_LOOKUP_FAILED;

		BOOL res = (cntlProcAddr) (CTRL_C_EVENT, 0);

		if (res == 0) return handleError("GenerateConsoleCtrlEvent");

		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;

		
}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getCurrentCPUId
(JNIEnv *ENV, jclass clazz) {
		
		debugMsg("Function getCurrentCPUId()");
		DWORD cpuid = 0;

		if (getCurCpuNo != NULL) {
			cpuid = (getCurCpuNo)();
		} else if (getCurrentProcNo != NULL) {
			cpuid = (DWORD) (getCurrentProcNo)();
		} else {
			return -1;
		}

		return cpuid;
}
/*
 * Class:     edu_regis_jprobe_jni_OSSystemInfo
 * Method:    getNativeLibList
 * Signature: (Ledu/regis/jprobe/jni/OSNativeLibs;)I
 */
JNIEXPORT jint JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getNativeLibList
(JNIEnv *env, jclass thisClazz, jobject obj, jlong pid) {

	debugMsg("Function getNativeLibList()");
	//get the OSNativeLibs Class
	jclass jclazz = env->GetObjectClass(obj);
	//Find the addLib method
	jmethodID addMethod = env->GetMethodID(jclazz, "addLib", "(Ljava/lang/String;Ljava/lang/String;JJ)V");

	if (addMethod == NULL) {
		debugMsg("Error Locating addLib Method");
		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
	}
	
	HANDLE hModuleSnap = INVALID_HANDLE_VALUE;
    MODULEENTRY32 me32;
	
    hModuleSnap = CreateToolhelp32Snapshot( TH32CS_SNAPMODULE, (DWORD) pid );
    if( hModuleSnap == INVALID_HANDLE_VALUE )
    {
		debugMsg("Create Process Snapshot Failed");
		handleError("CreateToolhelp32Snapshot");
        return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
    }

   // Set the size of the structure before using it.
    me32.dwSize = sizeof( MODULEENTRY32 );

   // Retrieve information about the first module,
   // and exit if unsuccessful
	if( !Module32First( hModuleSnap, &me32 ) ) {
         debugMsg("Retreive Process Snapshot Failed");
		 handleError("Module32First");
		 CloseHandle( hModuleSnap );    // Must clean up the
         return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
    }

    // Now walk the module list of the process,
    // and display information about each module
    do
    {
		                   
		env->CallObjectMethod(obj, addMethod, 
				env->NewStringUTF(me32.szModule), 
				env->NewStringUTF(me32.szExePath),
				(jlong) me32.modBaseSize,
				(jlong) me32.modBaseAddr);
      

    } while( Module32Next( hModuleSnap, &me32 ) );

    CloseHandle( hModuleSnap );

    return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT void JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_setNativeDebug
	(JNIEnv *env, jclass thisClass, jboolean debugValue) {
		dumpThreadTime();
		if (debugValue) {
			debug = TRUE;
			debugMsg("DLL Library Debug Logging is beging set to ON");
		} else {
			debugMsg("DLL Library Debug Logging is beging set to OFF");
			debug = FALSE;
		}
}

JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getProcessInfo
  (JNIEnv *env, jclass thisClass, jobject list, jboolean allUsers) {

		HANDLE hProcessSnap;
		PROCESSENTRY32 pe32;

		jclass jclazz = env->GetObjectClass(list);

		jmethodID mid = env->GetMethodID(jclazz, "add", "(Ljava/lang/String;)V");

		if (mid == NULL) {
			return 999;
		}

		hProcessSnap = CreateToolhelp32Snapshot( TH32CS_SNAPPROCESS, 0 );
		if( hProcessSnap == INVALID_HANDLE_VALUE )  {
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
		}

		pe32.dwSize = sizeof( PROCESSENTRY32 );

		if( !Process32First( hProcessSnap, &pe32 ) ) {
			return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_FAILED;
		}

		do {

			string str = getProcessInfoData(pe32, allUsers);

			if (!str.empty()) {

				jstring jstr = env->NewStringUTF(str.c_str());
				//env->CallVoidMethod(jstr, mid);
				env->CallVoidMethod(list, mid, jstr);
			}

		} while( Process32Next( hProcessSnap, &pe32 ) );

		CloseHandle( hProcessSnap );
		return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getCurrentProcessID
	(JNIEnv *env, jclass thisClass){ 

		debugMsg("Function getCurrentProcessID()");
		return GetCurrentProcessId();
}


JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_getCurrentThreadID
	(JNIEnv *env, jclass thisClass) {

		debugMsg("Function getCurrentThreadID()");
		return GetCurrentThreadId();
}
JNIEXPORT jboolean JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_isProcessActive
  (JNIEnv *env, jclass thisClass, jlong pid) {

	  HANDLE hProcess =  OpenProcess (PROCESS_QUERY_INFORMATION, FALSE, (DWORD) pid);

	
	if (hProcess == NULL) {
		DWORD err = GetLastError();
		return false;
	}

	CloseHandle(hProcess);

	return true;
}
JNIEXPORT jboolean JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_isAdmin
  (JNIEnv *env, jclass thisClass) {

	BOOL fRet = FALSE;
    HANDLE hToken = NULL;
    if( OpenProcessToken( GetCurrentProcess( ),TOKEN_QUERY,&hToken ) ) {
        TOKEN_ELEVATION Elevation;
        DWORD cbSize = sizeof( TOKEN_ELEVATION );
        if( GetTokenInformation( hToken, TokenElevation, &Elevation, sizeof( Elevation ), &cbSize ) ) {
            fRet = Elevation.TokenIsElevated;
        }
    }
    if( hToken ) {
        CloseHandle( hToken );
    }


	if (fRet) {
		return TRUE;
	}

	return FALSE;

}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_killProcess
  (JNIEnv *env, jclass thisClass, jlong pid) { 

	EnableDebugPriv();

	DWORD err = edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
	HANDLE hProcess =  OpenProcess (PROCESS_TERMINATE, FALSE  , (DWORD) pid);
	/*
	*	Not authorized if null
	*/
	if (hProcess == NULL) {
		err = GetLastError();
		return err;
	}

	if (!TerminateProcess(hProcess, 666)) {
		err = GetLastError();
	}
	CloseHandle(hProcess); 
	return err;
}
JNIEXPORT jlong JNICALL Java_edu_regis_jprobe_jni_OSSystemInfo_selfElevate
  (JNIEnv *env, jclass thisClass) {

	  char szPath[MAX_PATH];
	  if (GetModuleFileName(NULL, szPath, ARRAYSIZE(szPath))) { 
                   
            SHELLEXECUTEINFO sei = { sizeof(sei) }; 
            sei.lpVerb = "runas"; 
            sei.lpFile = szPath; 
            sei.hwnd = NULL; 
            sei.nShow = SW_NORMAL; 
 
 
            if (!ShellExecuteEx(&sei)) 
            { 
                DWORD dwError = GetLastError(); 
                if (dwError == ERROR_CANCELLED) 
                { 
                    return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_SELF_ELEVATE_CANCELLED;
                } 
                else 
                { 
                    return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_SELF_ELEVATE_FAILED;
                } 
            } 
            else 
            return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_CALL_OK;
        }
	  return edu_regis_jprobe_jni_OSSystemInfo_SYSTEM_SELF_ELEVATE_FAILED;
}
