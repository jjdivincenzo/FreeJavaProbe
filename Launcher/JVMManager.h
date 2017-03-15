///////////////////////////////////////////////////////////////////////////////////
//
//  Java VM Probe - Monitor your Java Program Without making code changes!
//
//    Copyright (C) 2007 - 2014  James Di Vincenzo
//
//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Lesser General Public
//    License as published by the Free Software Foundation; either
//    version 2.1 of the License, or (at your option) any later version.
//
//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Lesser General Public License for more details.
//
//    You should have received a copy of the GNU Lesser General Public
//    License along with this library; if not, write to the Free Software
//    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
///////////////////////////////////////////////////////////////////////////////////

#pragma once
#include "jni.h"
#include "Logger.h"
#include "Properties.h"
#include <vector>

#define BUFF_LEN 8192
#define CLASS_PATH_PREFIX "-Djava.class.path="
#define JAVA_HOME_PATH "\\jre\\bin\\server\\jvm.dll"
#define JAVA_HOME "JAVA_HOME"
typedef jint (JNICALL *CreateJavaVM)(JavaVM **pvm, void **penv, void *args);
/*
**
**
** This Class Manages the JVM.
*/
class  JVMManager 
{

private:
	
	JavaVM *vm;
	jclass cls;
	char* jvmDllName;
	Logger *logger;
	Properties *props;
	CreateJavaVM createJVM;
	JavaVMInitArgs vm_args;
	JNIEnv *env;
	jmethodID mid;
	HINSTANCE hDllInstance;
	JavaVMOption *options;
	string lastError;


	vector<string>* JVMManager::parseHeap(char* str);
	char* getErrorMsg();
	void Log(int level, char* emsg, ...);

protected:

public:

	JVMManager::JVMManager(Logger *logger, Properties *props);
	JVMManager::~JVMManager();
	DWORD JVMManager::load();
	DWORD JVMManager::create();
	DWORD JVMManager::invokeMain(int argc, LPWSTR * argv);
	DWORD JVMManager::destroy();
	string JVMManager::getLastError();



	char* getCreateJVMError(jint ec);
};
