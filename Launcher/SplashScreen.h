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

#include <stdio.h>
#include <tchar.h>
#include <windows.h>
#include <wincon.h>
#include <winbase.h>
#pragma once


class CSplashScreen  
{
public:
	CSplashScreen( HWND parentWnd );
	virtual ~CSplashScreen();

	static void ShowSplashScreen( HWND pParentWnd = NULL, LPCTSTR statusMessage = NULL, int millisecondsToDisplay=0 );
	static void HideSplashScreen();

protected:
	BOOL Create( HWND pParentWnd=NULL);
	static LRESULT CALLBACK WndProc(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam);
	void GetVersionStrings();
	void ReportError( LPCTSTR format, ...  );
	BOOL RegisterClass( LPCTSTR szWindowClassName );
	void ClearMessageQueue();

	HFONT CreatePointFont( int pointSize, LPCTSTR fontName, HDC dc);
	SIZE FindFontPointSize( HDC paintDC, LPCTSTR fontName, char **stringsToCheck, int numberOfStringsToCheck, SIZE maximumSize );
	void OnPaint( HWND hWnd );
	void DisplayProductName( HDC paintDC, int windowWidth, int windowHeight );
	void DisplayBody( HDC paintDC, int windowWidth, int windowHeight );
	void DisplayStatusLine( HDC paintDC, int windowWidth, int windowHeight );

private:
	HWND m_hDlg;
	HWND m_hParentWnd;
	HWND m_hWnd;
	HINSTANCE m_instance;
	static CSplashScreen* m_pSplashWnd;
	static ATOM m_szWindowClass ;
	static BOOL m_useStderr;

	HBITMAP m_bitmap;

	static LPTSTR m_productNameString;
	static LPTSTR m_companyNameString;
	static LPTSTR m_versionNumberString;
	static LPTSTR m_versionString;
	static LPTSTR m_copyrightString;
	static LPTSTR m_commentsString;
	static LPTSTR m_statusMessage;

	static int m_millisecondsToDisplay;				// 0 ==> until mouse click or keystroke
	
	// create rectangle that product name has to fit in
	static const int m_productNameVerticalOffset;		// empty space between top 3-D border and product name
	static const int m_productNameVerticalHeight;		// empty space between bottom 3-D border and bottom of product name
	static const int m_productNameLeftMargin;		// distance from left side to place name, company, copyright and version
	static const int m_productNameRightMargin;		// distance from right side to place name, company, copyright and version
	static const LPCTSTR m_productNameFontName;		// name of font for application name
	static SIZE m_productNamePointSize;				// point size used for the application name	
	static COLORREF m_productNameTextColor;			// color used for text

	static const BOOL m_displayCompanyName;			// true if displaying companyName
	static const BOOL m_displayVersion;				// true if displaying version
	static const BOOL m_displayCopyright;			// true if displaying copyright
	static const BOOL m_displayComments;			// true if displaying comments

	// create rectangle that strings in body have to fit in
	static const int m_bodyVerticalOffset;				// empty space between top 3-D border and top of body
	static const int m_bodyVerticalHeight;			// empty space between bottom 3-D border and bottom of body
	static const int m_bodyLeftMargin;				// distance from left side to place company name, copyright, version and comment
	static const int m_bodyRightMargin;				// distance from right side to place company name, copyright, version and comment
	static const LPCTSTR m_bodyFontName;			// name of font for company name, copyright, version and comment	
	static SIZE m_bodyPointSize;					// point size used for company name, copyright, version and comment	
	static COLORREF m_bodyTextColor;				// color used for company name, copyright, version and comment

	static const int m_statusVerticalOffset;				// empty space between top 3-D border and top of status line
	static const int m_statusVerticalHeight;			// empty space between bottom 3-D border and bottom of status line
	static const int m_statusLeftMargin;			// distance from left side to status line
	static const int m_statusRightMargin;			// distance from right side to place status line
	static const LPCTSTR m_statusMessageFontName;	// name of font for status message while starting	
	static SIZE m_statusMessagePointSize;			// point size used for status message while starting	
	static COLORREF m_statusMessageTextColor;		// color used for status message while starting

};


