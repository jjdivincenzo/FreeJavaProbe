// JavaLauncher.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#pragma once


#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <wincon.h>
#include <winbase.h>
#include <psapi.h>
#include <sstream>
#include "jni.h"
#include "Logger.h"
#include "Properties.h"
#include "JVMManager.h"
#include "SplashScreen.h"

using namespace std;


char* getIniFile();
char* getErrorMsg();



string logFile = "JProbeWindows.log";
Logger *logger;


char* getIniFile() {

	char modName[BUFF_LEN];
	char path[BUFF_LEN];

	//Get the fully qualified path to the executable i.e. c:\program files\...\myprog.exe
	GetModuleFileNameEx(GetCurrentProcess(), NULL, modName, sizeof(modName));
	
	//get string length
	size_t len = strlen(modName) + 1;
	
	//chop off the "exe" and null terminator
	char* iFile = new char[len];
	memset(path,0,len);
	memcpy(path,modName,len-4);

	//append the "ini" to the name and save it...
	strcat(path,"ini");
	strcpy(iFile,path);

	
	return iFile;
		
}


int InvokeMain(int argc, LPWSTR * argv) {
	
	

	Properties *props = new Properties(getIniFile());
	logger = new Logger(logFile, props->isDebug());

	
	JVMManager *jvmm = new JVMManager(logger, props);

	
	CSplashScreen::ShowSplashScreen( NULL, "Loading...");

	if (!jvmm->load()) {
		CSplashScreen::HideSplashScreen();
		MessageBox(0,jvmm->getLastError().c_str(),"Unable to Load JVM DLL", MB_OK);
		return -1;
	}
	
	if (!jvmm->create()) {
		CSplashScreen::HideSplashScreen();
		MessageBox(0,jvmm->getLastError().c_str(),"Unable to Create JVM ", MB_OK);
		return -1;
	}
	
	if (!jvmm->invokeMain(argc, argv)) {
		CSplashScreen::HideSplashScreen();
		MessageBox(0,jvmm->getLastError().c_str(),"Failed Calling Main", MB_OK);
		return -1;
	}

	CSplashScreen::HideSplashScreen();
	jvmm->destroy();
	delete jvmm;
	delete props;
	
	return 0;

}



int WINAPI WinMain(      
    HINSTANCE hInstance,
    HINSTANCE hPrevInstance,
    LPSTR lpCmdLine,
    int nCmdShow)
{
	
   LPWSTR *szArglist;
   int nArgs;
   	
   szArglist = CommandLineToArgvW(GetCommandLineW(), &nArgs);
   

   if( NULL == szArglist )
   {
      wprintf(L"CommandLineToArgvW failed\n");
      return 0;
   }
   
   
	return InvokeMain(nArgs, szArglist);
	
}
char* getErrorMsg() {
	DWORD err = GetLastError();
    char* s;

	if(FormatMessage(FORMAT_MESSAGE_ALLOCATE_BUFFER |
		FORMAT_MESSAGE_FROM_SYSTEM,
		NULL,
		err,
		0,
		(LPTSTR)&s,
		0,
		NULL) == 0)
	{ 
		s = new char[BUFF_LEN];
		sprintf(s,"GetLastError() is %d", err); 
	
	}
	return s;
}
