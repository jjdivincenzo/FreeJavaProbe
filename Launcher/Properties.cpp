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
/*
Properties Class
*/
#include "stdafx.h"
#include "Properties.h"
#include <fstream>
#include <iostream>
#include <iomanip>
using namespace std;

/*************************************************************************************
** Constructor.
*************************************************************************************/
Properties::Properties(char* fileName) {

	valid = TRUE;
	iniFile = fileName;

	ifstream propFile;
	propFile.open(iniFile, ios::in);

	if (!propFile.is_open()) {
		errorMessage = "File Does Not Exist";
		valid = FALSE;
	}
	propFile.close();

	jvmDLL = getOption(JVM_GROUP,JVM_DLL,"jvm.dll");

	mainClass = getOption(JVM_GROUP,MAIN_CLASS,"edu/regis/jprobe/ui/JProbeUI");
	classPath = getOption(JVM_GROUP,CLASS_PATH,"test.jar");
	javaHeap = getOption(JVM_GROUP,JAVA_HEAP,"-Xmx256m -Xms64m -XX:MaxPermSize=256m");
	char* db = getOption(JVM_GROUP, DEBUG_OPTION,"false");

	if (_stricmp("true", db) == 0) {
		debug = TRUE;
	} else {
		debug = FALSE;
	}
	
	char* num = getOption(JVM_GROUP,NUMBER_OF_OPTIONS,"1");
	numberOfOPtions = atoi(num);

	
	jvmOpts = new char*[numberOfOPtions];
	

	for (int i = 0; i < numberOfOPtions; i++) {
		char tmp[256];
				
		sprintf_s(tmp,"%s%d", JVM_OPT,i + 1 );
		char* val = getOption(JVM_GROUP,tmp,0);

		if (val == 0) {
			valid = FALSE;
			errorMessage = "Invalid Properties for JVMOptions";
			break;
		}
		jvmOpts[i] = val;
		

	}
}
/*************************************************************************************
** Destructor.
*************************************************************************************/
Properties::~Properties() {

	
}



/*************************************************************************************
** Method to obtain a property from an ini.
*************************************************************************************/
char* Properties::getOption(const char* group, const char* opt, const char* def) {
	
	char szBuffer[PARM_LEN];
	GetPrivateProfileString(group, opt, def, szBuffer,sizeof(szBuffer), iniFile);
	size_t len = strlen(szBuffer) + 1;
	char* ret = new char[len];
	strcpy_s(ret, len, szBuffer);

	return ret;
	
}
/*************************************************************************************
** Getter: mainClass
*************************************************************************************/

char* Properties::getMainClass(void) {

	return mainClass;
}
/*************************************************************************************
** Getter: jvmDLL
*************************************************************************************/
char* Properties::getJVMDLL(void) {
	return jvmDLL;
}
/*************************************************************************************
** Getter: classPAth
*************************************************************************************/
char* Properties::getClassPath(void) {
	return classPath;
}
/*************************************************************************************
** Getter: Java Heap
*************************************************************************************/
char* Properties::getJavaHeap(void) {
	return javaHeap;
}

/*************************************************************************************
** Getter: JVM Options
*************************************************************************************/
char** Properties::getJVMOptions(void) {
	return jvmOpts;
}
/*************************************************************************************
** Getter: Number of JVM options
*************************************************************************************/
int Properties::getNumberOfOptions(void) {

	return numberOfOPtions;
}

/*************************************************************************************
** Getter: Error Message
*************************************************************************************/
char* Properties::getErrorMessage(void) {
	return errorMessage;
}

/*************************************************************************************
** Getter: true if the properties are valid
*************************************************************************************/
BOOL Properties::isValid(void) {
	return valid;
}
/*************************************************************************************
** Getter: debug mode
*************************************************************************************/
BOOL Properties::isDebug(void) {
	return debug;
}
