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
#include "stdafx.h" 
#include <iostream>
#include <tchar.h>
#include <string>
#include <time.h>
#include <fstream>
#include <strstream>
#include <iomanip>
#include <sstream>
#include <Windows.h>
//#include <initializer_list>
using namespace std;

//Logger Levels
#define DBUG 0
#define INFO 1
#define WARN 2
#define ERR 3
#define SEVERE 4
#define FATAL 5
#define MAX_LOG_LEVEL FATAL
#define VERSION_INFO "3.1.2.0"
#define DATE_TIME_FMT "%m-%d-%Y %H:%M:%S"
#define MAX_BUFF 8192
#define SCREEN_WIDTH 512
#define SCREEN_DEPTH 2048



// CLoggerApp
// See Logger.cpp for the implementation of this class
//

class  Logger 
{

private:
	

	string logName;
	string getTime();
	ofstream logFile;
	int logLevel;
	HANDLE mutex_handle;
	HANDLE hStdout;
	BOOL closed;

protected:
	
public:
	Logger::Logger(string&, BOOL);
	string Logger::getLevel(int);
	virtual Logger::~Logger();
	void Logger::log(int, string&);
	void Logger::console(string&);
	void Logger::debug(string&);
	void Logger::info(string&);
	void Logger::infos(string&);
	void Logger::warn(string&);
	void Logger::error(string&);
	void Logger::severe(string&);
	void Logger::fatal(string&);
	void Logger::changeLevel(int);
	void Logger::log_f(int level, char* emsg, ...);
	void Logger::close();
public:
	



};
