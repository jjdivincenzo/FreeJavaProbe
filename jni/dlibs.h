
#include "stdafx.h"
//Dynamic Libs
BOOL debug;

HINSTANCE kernelLib;
CNTLC cntlProcAddr;
GET_CUR_CPU_NO getCurCpuNo;
GET_PROC_HANDLE_COUNT getProcessHandleCount;
IW64PFP isWOW64;


HINSTANCE psapiLib;
GET_PROC_MEM_INFO getProcMemInfo;
EMPTY_WS emptyWS;

HINSTANCE ntdllLib;
GET_CUR_PROC_NO getCurrentProcNo;