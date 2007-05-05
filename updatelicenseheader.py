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

def isLicenseLine(line):
	return line.startswith( '//##*' )  or  line.startswith( '##-*' )

cppLicense = open( 'cpplicenseheader' ).readlines()
pyLicense = open( 'pylicenseheader' ).readlines()

for filename in sys.argv[1:]:
	bIsCpp = cmpExt( filename, '.cpp' )  or  cmpExt( filename, '.h' )
	bIsPy = cmpExt( filename, '.py' )

	if bIsCpp  or  bIsPy:
		print 'Updating %s...' % filename
		f = open( filename, 'r' )

		strippedLines = [ line   for line in f   if not isLicenseLine( line ) ]
		if bIsCpp:
			newLines = cppLicense + strippedLines
		elif bIsPy:
			newLines = pyLicense + strippedLines
		else:
			newLines = None

		f.close()

		if newLines is not None:
			f = open( filename, 'w' )
			f.writelines( newLines )