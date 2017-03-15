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
#include "stdafx.h"
#include "JVMManager.h"

JVMManager::JVMManager(Logger *logger, Properties *props) {

	this->logger = logger;
	this->props = props;

	

	Log(INFO, "Initializing JVM Manager");

	if (props->isDebug()) {
		logger->changeLevel(DBUG);
		Log(INFO, "Running in Debug Mode");
	}

}

JVMManager::~JVMManager()  {
}

DWORD JVMManager::load() {

	DWORD ret = TRUE;
	Log(INFO, "Loading JVM DLL %s from Properties", props->getJVMDLL());
	hDllInstance = LoadLibrary(props->getJVMDLL());

	if( hDllInstance != 0) {
		return TRUE;
	}

	Log(ERR,"Load of JVM.dll from config failed, looking in JAVA_HOME");

	stringstream ss;

	char* javaHome = new char[1024];
	DWORD rc = GetEnvironmentVariable(JAVA_HOME, javaHome, 1024);

	if (rc > 0) {
		ss << javaHome << JAVA_HOME_PATH;
		
		hDllInstance = LoadLibrary(ss.str().c_str());
		if( hDllInstance == 0) {
			Log(ERR, "Failed to load jvm dll from JAVA_HOME at %s, Error is %s", ss.str().c_str(), getErrorMsg());
			ret = FALSE;
		} else {
			Log(INFO, "JVM dll Loaded from %s based on JAVA_HOME of %s", ss.str().c_str(), javaHome);
			ret = TRUE;
		}
	} else {
		Log(ERR, "Environment Variable ", JAVA_HOME, " is not Defined");
		ret = FALSE;
	}

	delete javaHome;

	return ret;
}

DWORD JVMManager::create() {

	int idx = 0;
	createJVM = (CreateJavaVM)GetProcAddress(hDllInstance, "JNI_CreateJavaVM");
	Log(INFO, "JNI_CreateJavaVM Function Address is 0x%p", createJVM);

	int len = props->getNumberOfOptions();
	char* addPath = props->getClassPath();
	vector<string> *heapOpts = parseHeap(props->getJavaHeap());
	int numOpts = len + (int) heapOpts->size() + 1;
	options = new JavaVMOption[numOpts];

	size_t size = strlen(CLASS_PATH_PREFIX) + strlen(addPath) + 1;
	char* classpath = new char[size];
	strcpy_s(classpath, size, CLASS_PATH_PREFIX);
	strcat_s(classpath, size, addPath); 
	options[idx] = JavaVMOption();
	options[idx].optionString = classpath;
	
	idx++;
	char** opts = props->getJVMOptions();
	Log(INFO, "Creating JVM With the Following Properties:");
	Log(INFO, "\t%s = %s", "Main Class", props->getMainClass());

	for (int i = 0; i < heapOpts->size(); i++) {
		options[idx] = JavaVMOption();
		string str = heapOpts->at(i);
		char * cstr = new char [str.length()+1];
		strcpy_s (cstr, str.length()+1, str.c_str());
		options[idx].optionString = cstr;
		Log(INFO,"\t%s", cstr);
		idx++;
	}
	
	for (int i = 0; i < len; i++) {
		options[idx] = JavaVMOption();
		options[idx].optionString = opts[i];
		Log(INFO,"\t%s", opts[i]);
		idx++;
	}
	
	
	vm_args.version = JNI_VERSION_1_6; //JNI Version 1.4 and above
	vm_args.options = options;
	vm_args.nOptions = (jint) numOpts;
	vm_args.ignoreUnrecognized = JNI_TRUE;

	Log(INFO, "Creating the JVM");
	jint res = createJVM(&vm, (void **)&env, &vm_args);
	if (res < 0)  {
		Log(ERR, "Error creating JVM, Error Code(%d), %s" ,res, getCreateJVMError(res));
		return FALSE;
	}

	Log(INFO, "JVM Successfully Created");
	return TRUE;
	
}

DWORD JVMManager::invokeMain(int argc, LPWSTR * argv) {

	char* mc = props->getMainClass();
	cls = env->FindClass(mc);

	if (cls == 0) {
		Log(ERR, "Unable to Find Main Class %s", mc);
		return FALSE;
	}

	 mid = env->GetStaticMethodID(cls, "main", "([Ljava/lang/String;)V");

	if (mid == 0) {
		Log(ERR, "Main Class %s Does Not Have [public static void main(String[] args] Method", mc);
		return FALSE;
	}

	//Create an array of Strings to present the main(String[] args) in the Java program
	// and populate it with the args passed to our main starting at the second element (arg(1))...
	
	jstring jstr = env->NewStringUTF("");
	jobjectArray str_array = 
		env->NewObjectArray(argc - 1, env->FindClass("java/lang/String"),jstr);

	for (int i = 1; i < argc; i++) {
		char parm[BUFF_LEN];
		sprintf(parm,"%ws",argv[i]);
		jstr = env->NewStringUTF(parm);
		env->SetObjectArrayElement(str_array, i -1, jstr);
	}
	//LocalFree(argv);

	//invoke the main method with the string array
	Log(INFO, "Calling Main Class %s", mc);
	env->CallStaticVoidMethod(cls, mid, str_array);

	//Check for any Exceptions...
	if(env->ExceptionCheck()) {
		env->ExceptionDescribe();
		Log(ERR, "An Exception Occured in \"main\" ",
			"Exception Error", MB_OK);
		return FALSE;
	}

	Log(INFO, "Class Main Method Ended, Detaching Main Thread");
	if (vm->DetachCurrentThread() != 0) {
		Log(ERR,"An Error Occurred Detaching main thread");
		return FALSE;
	};

	return TRUE;

}

DWORD JVMManager::destroy() {

	Log(INFO, "Waiting for all non-daemon threads to terminate, the JVM will then be Destroyed.");
	Log(INFO, "Launcher is Exiting");
	logger->close();
	
	jint rc = vm->DestroyJavaVM();
	
	return TRUE;

}
/***************************************************************************
** Method to parse the single java heap option into mutliple options
****************************************************************************/
vector<string>* JVMManager::parseHeap(char* str) {

	vector<string> *list = new vector<string>();
	vector<string>::iterator iter;

	char seps[]   = " ";
	char *next_token1 = NULL;
	char *token1 = NULL;
	
	token1 = strtok_s( str, seps, &next_token1);
	 while ((token1 != NULL))
    {
        // Get next token:
        if (token1 != NULL)
        {

			string *s = new string(token1);
			
			list->push_back(*s);
			 token1 = strtok_s( NULL, seps, &next_token1);
        }
        
    }

	return list;
}
/***************************************************************************
** Method to get the error description of why the JVM could not be started
****************************************************************************/
char* JVMManager::getCreateJVMError(jint ec) {

	char *msg;

	switch(ec) {
	
	case JNI_ERR: 
		msg = "Unknown Error Creating JVM";			 /* unknown error */
		break;
	case  JNI_EDETACHED:							 /* thread detached from the VM */
		msg = "Thread detached from the VM";
		break;
	case  JNI_EVERSION:								 /* JNI version error */
		msg = "JNI version error";
		break;
	case  JNI_ENOMEM:								 /* not enough memory */
		msg = "Not enough memory to create the JVM";
		break;
	case  JNI_EEXIST:								 /* VM already created */
		msg = "VM already created";
		break;
	case  JNI_EINVAL:
		msg = "One or More JVM Options were Invalid"; /* Invalid Option */
		break;
	default:
		msg = "Unspecified Error Returned";
	}

	return msg;

}
/***************************************************************************************
** Returns the Windows last error
***************************************************************************************/
char* JVMManager::getErrorMsg() {
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
		sprintf_s(s, BUFF_LEN, "GetLastError() is %d", err); 
	
	}
	return s;
}
/***************************************************************************************
** Logs a message
***************************************************************************************/
void JVMManager::Log(int level, char* emsg, ...) {

	
	size_t len = strlen(emsg);
	char msg[BUFF_LEN];
	va_list ap;
	va_start (ap, emsg);
	vsprintf_s(msg, emsg, ap);
	string out = msg;
	logger->log(level, out);

	if (level == ERR) {
		lastError = out;
	}


	
}
string JVMManager::getLastError() {
	return lastError;
}
