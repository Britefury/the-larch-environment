##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys
import os

def cmpExt(filename, ext):
	name, fileExt = os.path.splitext( filename )
	return fileExt.lower() == ext.lower()

def isExtLib( filename ):
	pathItems = []
	while filename != '':
		filename, tail = os.path.split( filename )
		pathItems = [ tail ] + pathItems
	return 'extlibs'  in  [ item.lower()   for item in pathItems ]

def isLicenseLine(line):
	return line.startswith( '//##*' )  or  line.startswith( '##-*' )


javaLicense = open( 'javalicenseheader' ).readlines()
pyLicense = open( 'pylicenseheader' ).readlines()






for filename in sys.argv[1:]:
	bIsJava = cmpExt( filename, '.java' )
	bIsPy = cmpExt( filename, '.py' )

	if bIsJava  or  bIsPy:
		if not isExtLib( filename ):
			print 'Updating %s...' % filename
			f = open( filename, 'r' )

			strippedLines = [ line   for line in f   if not isLicenseLine( line ) ]
			if bIsJava:
				newLines = javaLicense + strippedLines
			elif bIsPy:
				newLines = pyLicense + strippedLines
			else:
				newLines = None

			f.close()

			if newLines is not None:
				f = open( filename, 'w' )
				f.writelines( newLines )


