#include "stdafx.h"
#include "CPUAffinity.h"
#include "SystemInfo.h"

JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getCPUAffinity
(JNIEnv *, jclass) {

	DWORD lpProcessAffinityMask = 0;
	DWORD lpSystemAffinityMask = 0;

	HANDLE proc = GetCurrentProcess();

	BOOL res = GetProcessAffinityMask(proc, &lpProcessAffinityMask, &lpSystemAffinityMask	);	
	return lpProcessAffinityMask;
}

JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_setCPUAffinity
(JNIEnv *, jclass, jlong mask) {

	DWORD newMask = (DWORD) mask;
	HANDLE proc = GetCurrentProcess();
		
	if	(SetProcessAffinityMask(proc, newMask ) == 0) {
		return GetLastError();
	}
	
	return 0;
}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getNumberOfCPUs
(JNIEnv *, jclass) {

	SYSTEM_INFO si;
   	GetSystemInfo(&si);
	return si.dwNumberOfProcessors;
}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_setCPUAffinityToSystem
(JNIEnv *, jclass) {

	DWORD lpProcessAffinityMask = 0;
	DWORD lpSystemAffinityMask = 0;

	HANDLE proc = GetCurrentProcess();

	BOOL res = GetProcessAffinityMask(proc, &lpProcessAffinityMask, &lpSystemAffinityMask	);	

	
	if	(SetProcessAffinityMask(proc, lpSystemAffinityMask ) == 0) {
		return GetLastError();
	}
	return 0;
}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getIOCount
	(JNIEnv *, jclass) {

	IO_COUNTERS counters;
	BOOL res = getIOCounters(&counters);

	if (res == 0) return -1;

	ULONGLONG totalIO = counters.ReadOperationCount + 
						counters.WriteOperationCount +
						counters.OtherOperationCount;
	return totalIO;

}

/*
 * Class:     com_sybase_cpuaffinity_CPUAffinity
 * Method:    getIOBytes
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getIOBytes
	(JNIEnv *, jclass) {
	
	IO_COUNTERS counters;

	BOOL res = getIOCounters(&counters);

	if (res == 0) return -1;

	ULONGLONG totalIO = counters.ReadTransferCount + 
						counters.WriteTransferCount +
						counters.OtherTransferCount;
	return totalIO;

}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessCPUTime
(JNIEnv *, jclass){ 

	DWORD tKernel, tUser;
    BOOL res = getCPUTimes(&tUser, &tKernel);

	if (res == FALSE) return -1;
	return tKernel + tUser;
}

/*
 * Class:     com_sybase_cpuaffinity_CPUAffinity
 * Method:    getProcessKernelTime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessKernelTime
(JNIEnv *, jclass){

	
	DWORD tKernel, tUser;
    BOOL res = getCPUTimes(&tUser, &tKernel);

	if (res == FALSE) return -1;
	return tKernel;
}

/*
 * Class:     com_sybase_cpuaffinity_CPUAffinity
 * Method:    getProcessUserTime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessUserTime
(JNIEnv *, jclass){

	DWORD tKernel, tUser;
    BOOL res = getCPUTimes(&tUser, &tKernel);

	if (res == FALSE) return -1;
	return tUser;
}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessHandleCount
(JNIEnv *, jclass) {

	DWORD hCount;
	HANDLE proc = GetCurrentProcess();
	BOOL res = GetProcessHandleCount(proc, &hCount);

	if (res == 0) return -1;

	return hCount;

}
JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessWSMin
(JNIEnv *, jclass){

	DWORD min, max;
	HANDLE proc = GetCurrentProcess();
	BOOL res = GetProcessWorkingSetSize(proc, &min, &max);

	if (res == 0) return -1;
	return min;
}


JNIEXPORT jlong JNICALL Java_com_sybase_systeminfo_OSSystemInfo_getProcessWSMax
(JNIEnv *, jclass){
	
	DWORD min, max;
	HANDLE proc = GetCurrentProcess();
	BOOL res = GetProcessWorkingSetSize(proc, &min, &max);

	if (res == 0) return -1;
	return max;
}