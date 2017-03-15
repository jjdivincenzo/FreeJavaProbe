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
#include <string>
#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <wincon.h>
#include <winbase.h>


#define CLASS_PATH "classPath" 
#define MAIN_CLASS "mainClass"
#define JVM_DLL "jvmDLL"
#define JVM_OPT "jvmoption"
#define NUMBER_OF_OPTIONS "numberOfOptions"
#define JAVA_HEAP "javaHeap"
#define DEBUG_OPTION "debug"
#define JVM_GROUP "JavaOptions"
#define PARM_LEN 8192



class  Properties 
{
private:
	

	char* mainClass;
	char* classPath;
	char* jvmDLL;
	char** jvmOpts;
	char* javaHeap;
	char* iniFile;
	char* errorMessage;
	char* serviceLog;
	BOOL valid;
	BOOL debug;
	int numberOfOPtions;
	

	
	char* getOption(const char*, const char*, const char* );

protected:
	
public:
	Properties::Properties(char*);
	Properties::~Properties();
	char* Properties::getMainClass(void);
	char* Properties::getJVMDLL(void);
	char* Properties::getJavaHeap(void);
	char** Properties::getJVMOptions(void);
	int Properties::getNumberOfOptions(void);
	char* Properties::getErrorMessage(void);
	char* Properties::getClassPath(void);
	BOOL Properties::isValid(void);
	BOOL Properties::isDebug(void);
};