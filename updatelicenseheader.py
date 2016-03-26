##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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




def processFile(filename):
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


def visit(arg, dirname, names):
	for filename in names:
		processFile( os.path.join( dirname, filename ) )
		

				
for filename in sys.argv[1:]:
	if os.path.isdir( filename ):
		print 'Processing directory %s.....'  %  filename
		os.path.walk( filename, visit, None )
	else:
		processFile( filename )

