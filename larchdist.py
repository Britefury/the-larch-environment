import os
import glob
import sys
import py_compile
import zipfile


if len( sys.argv ) != 2:
	print 'Usage:'
	print '\t%s <version>'  %  sys.argv[0]
	sys.exit( 0 )


versionString = sys.argv[1]
srcZipFilename = 'larch-src-' + versionString + '.zip'
binZipFilename = 'larch-bin-' + versionString + '.zip'
packageName = 'larch-' + versionString

ignoreList = [ '.hg' ]

tmpClassFile = 'tmp$py.class'


def larchDir(d):
	return os.path.join( 'larch', d )

dirsForSource = [
	( larchDir( 'Britefury' ), '*.py' ),
	( larchDir( 'LarchCore' ), '*.py', '*.png' ),
	( larchDir( 'LarchTools' ), '*.py', '*.png' ),
	( larchDir( 'images' ), '*.png' ),

	( 'java', '*.java' ),
	( 'extlibs', '*.jar' ),
	( 'fonts', '*.*' ),
]

dirsForBin = [
	( 'bin', '*.class' ),
	( larchDir( 'LarchCore' ), '*.png' ),
	( larchDir( 'LarchTools' ), '*.png' ),
	( larchDir( 'images' ), '*.png' ),
	( 'extlibs', '*.jar' ),
	( 'fonts', '*.*' ),
	]

dirsForBinCompile = [ larchDir( 'Britefury' ), larchDir( 'LarchCore' ), larchDir( 'LarchTools' ) ]



rootFiles = [
	'AUTHORS',
	'BUGS',
	'build.xml',
	'COPYING',
	'COPYING-Jericho.txt',
	'COPYING-svgSalamander.txt',
	'larchdist.py',
	'lconsole',
	'lconsole.bat',
	'lconsole.py',
	'lch',
	'lch.bat',
	'lch.py',
	'LICENSE',
	'REFERENCES',
]


print 'PACKAGE: ' + srcZipFilename

def copyFile(z, src, dst):
	z.write( src, dst )


def copyRootFiles(z, destDir):
	for name in rootFiles:
		destPath = os.path.join( destDir, name )
		z.write( name, destPath )


def copyDir(z, src, dst, patterns):
	for e in os.listdir( src ):
		s = os.path.join( src, e )
		if os.path.isdir( s ):
			if e not in ignoreList:
				copyDir( z, os.path.join( src, e ), os.path.join( dst, e ), patterns )
	
	for pattern in patterns:
		for s in glob.glob( os.path.join( src, pattern ) ):
			if os.path.isfile( s ):
				filename = os.path.basename( s )
				d = os.path.join( dst, filename )
				
				copyFile( z, s, d )

def compile_file(z, src, dst):
	py_compile.compile( src, tmpClassFile )
	z.write( tmpClassFile, dst )


def compile_dir(z, src, dst):
	for e in os.listdir( src ):
		s = os.path.join( src, e )
		if os.path.isdir( s ):
			if e not in ignoreList:
				compile_dir( z, os.path.join( src, e ), os.path.join( dst, e ) )

	for s in glob.glob( os.path.join( src, '*.py' ) ):
		if os.path.isfile( s ):
			filename = os.path.basename( s )
			name, ext = os.path.splitext(filename)
			dest_filename = name + '$py.class'
			d = os.path.join( dst, dest_filename )

			compile_file( z, s, d )



print 'Source package: {0}'.format( srcZipFilename )
srcZ = zipfile.ZipFile( srcZipFilename, 'w', zipfile.ZIP_DEFLATED )

print 'Adding files in root directory'
copyRootFiles( srcZ, packageName )

for d in dirsForSource:
	print 'Adding files in {0}'.format( d[0] )
	copyDir( srcZ, d[0], os.path.join( packageName, d[0] ), d[1:] )

srcZ.close()



print 'Binary package: {0}'.format( binZipFilename )
binZip = zipfile.ZipFile( binZipFilename, 'w', zipfile.ZIP_DEFLATED )

print 'Adding files in root directory'
copyRootFiles( binZip, packageName )


for d in dirsForBin:
	print 'Adding files in {0}'.format( d[0] )
	copyDir( binZip, d[0], os.path.join( packageName, d[0] ), d[1:] )

for d in dirsForBinCompile:
	print 'Compiling files in {0}'.format( d )
	compile_dir( binZip, d, os.path.join( packageName, d ) )


if os.path.exists( tmpClassFile ):
	os.remove( tmpClassFile )

binZip.close()
