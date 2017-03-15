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
#include "Logger.h"


/***********************************************************
MEMBER FUNCTION: Logger()	   default constructor
VISIBILITY: protected
DESCRIPTION: This is the 0 arg ctor. It opens the logfile and
			 writes a series of log records.
************************************************************/
Logger::Logger(string &fileName, BOOL debug)
{

	mutex_handle = CreateMutex(NULL, FALSE, NULL);
	logName = fileName;
	logLevel = INFO;
	logFile.open(logName, ios::app | ios::out);
	closed = FALSE;

	if (!logFile.is_open()) {
		cout << "Open of log file " << logName << ", Failed" << endl;
	}
	string buildInfo;
	buildInfo = "Source File Version ";
	buildInfo += VERSION_INFO;
	buildInfo += " - ";
	buildInfo += __FILE__;
	buildInfo += " Compiled On ";
	buildInfo += __DATE__;
	buildInfo += " ";
	buildInfo += __TIME__;

	info((string)"****************************************************************************");
	info((string)"**              Java  VM Probe Windows Launcher Log                       **");
	info((string)"****************************************************************************");
	info(buildInfo);
	string msg = "Logger Starting, Log Level is ";
	msg += getLevel(logLevel);
	
	log(INFO,msg);
	if (debug) {
		
		BOOL resp = AllocConsole();
		if (!resp) {
			error((string)"Unable to Allocate a Console"); 
			
		}
		hStdout = GetStdHandle(STD_OUTPUT_HANDLE); 

		if (hStdout != INVALID_HANDLE_VALUE) 
		{
			COORD cord;
			cord.X = SCREEN_WIDTH;
			cord.Y = SCREEN_DEPTH;
			BOOL rval = SetConsoleScreenBufferSize(hStdout, cord);
						
  		}
		
	}
	hStdout = GetStdHandle(STD_OUTPUT_HANDLE);
		
}
/***********************************************************
MEMBER FUNCTION: ~Logger()	   destructor
VISIBILITY: public
DESCRIPTION: No Implementation.
			
************************************************************/
Logger::~Logger(void)
{
	close();	
	CloseHandle(mutex_handle);
}

void Logger::close() {

	if (!closed) {
		info((string)"Logger is Stopping");
		info((string)"****************************************************************************");
		logFile.close();
		closed = TRUE;
	}
}
/***********************************************************
MEMBER FUNCTION: log()
VISIBILITY: public
INPUT: logging level, string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log if the
			 level is not less that the current level.
************************************************************/
void Logger::log(int level, string &msg) 
{
	char levelName[] = {'D', 'I', 'W', 'E', 'S', 'F'};
	if (level >= logLevel) {
		WaitForSingleObject(mutex_handle, INFINITE);
		DWORD tid = GetCurrentThreadId();
		stringstream ss;

		ss << "[" << getTime() << "][" << levelName[level] << "][tid:"  << tid << "] - " << msg << endl;
		string lmsg = ss.str();

		if (!closed) {
			logFile << lmsg.c_str();
			logFile.flush();
		}
		
		if (hStdout != INVALID_HANDLE_VALUE) {
			DWORD outl;
			const char* cmsg = lmsg.c_str();
			WriteConsole(hStdout, cmsg, (DWORD) strlen(cmsg), &outl, 0);
		}
		ReleaseMutex(mutex_handle);
	}
}

void Logger::log_f(int level, char* emsg, ...) {

	size_t len = strlen(emsg);
	char msg[MAX_BUFF];
	va_list ap;
	va_start (ap, emsg);
	vsprintf_s(msg, emsg, ap);
	string out = msg;
	log(level, out);
}
/***********************************************************
MEMBER FUNCTION: console()
VISIBILITY: public
INPUT: logging level, string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log and stdout.
************************************************************/
void Logger::console(string& os) 
{
	
	cout <<	 getTime() << " - " << os << endl;
	logFile << getTime() << ":[C] - " << os << endl;
	
}
/***********************************************************
MEMBER FUNCTION: debug()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 debug level.
************************************************************/
void Logger::debug(string& msg)
{
	
	log(DBUG,msg);
}
/***********************************************************
MEMBER FUNCTION: info()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 info level.
************************************************************/
void Logger::info(string& msg)
{
	log(INFO,msg);
}

/***********************************************************
MEMBER FUNCTION: warn()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 warn level.
************************************************************/
void Logger::warn(string& msg)
{
	log(WARN,msg);
}
/***********************************************************
MEMBER FUNCTION: error()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 error level.
************************************************************/
void Logger::error(string& msg)
{
	log(ERR,msg);
}
/***********************************************************
MEMBER FUNCTION: severe()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 severe level.
************************************************************/
void Logger::severe(string& msg)
{
	log(SEVERE,msg);
}
/***********************************************************
MEMBER FUNCTION: fatal()
VISIBILITY: public
INPUT: string - message to log
OUTPUT: none.
DESCRIPTION: This function writes a message to the log at the
			 fatal level.
************************************************************/
void Logger::fatal(string& msg)
{
	log(FATAL,msg);
}
/***********************************************************
MEMBER FUNCTION: changeLevel()
VISIBILITY: public
INPUT: int - new log level
OUTPUT: none.
DESCRIPTION: This function sets the log level to a new value.
************************************************************/
void Logger::changeLevel(int newLevel) 
{
	char buff[80];
	ostrstream ostr(buff,80);
	ostr << "Changing Log Level from " << getLevel(logLevel) << " to " << getLevel(newLevel) << ends;
	string msg(buff);
	log(INFO,msg);
	logLevel = newLevel;
}
/***********************************************************
MEMBER FUNCTION: changeLevel()
VISIBILITY: protected
INPUT: int - log level
OUTPUT: string - name of log level
DESCRIPTION: This function retreives the name of the log level
************************************************************/
string Logger::getLevel(int lvl)
{
	if (lvl > 5) return "Invalid";
	string levels[] = {"Debug", "Info", "Warning", "Error", "Severe", "Fatal"};
	return levels[lvl];
}
/***********************************************************
MEMBER FUNCTION: getTime()
VISIBILITY: protected
INPUT: none
OUTPUT: string - current date/time stamp
DESCRIPTION: This function retreives the current date/time
************************************************************/
string Logger::getTime()
{
	SYSTEMTIME stime;
    FILETIME ltime;
    FILETIME ftTimeStamp;
    char TimeStamp[MAX_BUFF];
    GetSystemTimeAsFileTime(&ftTimeStamp);

    FileTimeToLocalFileTime (&ftTimeStamp,&ltime);
    FileTimeToSystemTime(&ltime,&stime);

    sprintf_s(TimeStamp, MAX_BUFF, "%02d-%02d-%02d %02d:%02d:%02d:%03d", stime.wMonth, stime.wDay, stime.wYear, 
		stime.wHour, stime.wMinute, stime.wSecond, stime.wMilliseconds);
	string ret = TimeStamp;
    return ret;
}
