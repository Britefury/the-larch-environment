import os
import sys

import re


# Determine the platform
PLATFORM_WIN32 = 0
PLATFORM_LINUX = 1

platform = PLATFORM_WIN32

if sys.platform == 'win32':
	platform = PLATFORM_WIN32
else:
	platform = PLATFORM_LINUX





# Some helper functions
def prefixPaths(prefix, paths):
	return [ os.path.join( prefix, x )   for x in paths ]


def cppPrefixPaths(prefix, paths):
	return prefixPaths( os.path.join( 'cpp', prefix ), paths )


def shLibsForShLib(libs):
	if platform == PLATFORM_WIN32:
		return libs
	else:
		return []




cppMathFiles = cppPrefixPaths( 'Math', [ 'Bezier2Util.cpp', 'ConvexHull2.cpp', 'Polygon2.cpp', 'Segment2.cpp' ] )
pyMathFiles = cppPrefixPaths( 'Math', [ 'pyAxis.cpp', 'pyBBox2.cpp', 'pyBezier2Util.cpp', 'pyColour3f.cpp', 'pyConvexHull2.cpp', 'pyPoint2.cpp', 'pyPolygon2.cpp', 'pySegment2.cpp', 'pySide.cpp', 'pyVector2.cpp', 'pyXform2.cpp', 'pyMath.cpp' ] )

cppDocViewHelperFiles = cppPrefixPaths( 'DocViewHelper', [ 'DocViewBoxTable.cpp' ] )
pyDocViewHelperFiles = cppPrefixPaths( 'DocViewHelper', [ 'pyDocViewBoxTable.cpp', 'pyDocViewHelper.cpp' ] )

cppGraphViewHelperFiles = cppPrefixPaths( 'GraphViewHelper', [ 'GraphViewWidgetBoxTable.cpp', 'GraphViewLinkCurveTable.cpp' ] )
pyGraphViewHelperFiles = cppPrefixPaths( 'GraphViewHelper', [ 'pyGraphViewWidgetBoxTable.cpp', 'pyGraphViewLinkCurveTable.cpp', 'pyGraphViewHelper.cpp' ] )

cppGreenletFiles = cppPrefixPaths( os.path.join( 'extlibs', 'greenlet' ), [ 'greenlet.c' ] )




if platform == PLATFORM_WIN32:
	_pythonPath = sys.prefix
	_pythonIncPath = os.path.join( _pythonPath, 'include' )
	_pythonLibPath = os.path.join( _pythonPath, 'libs' )

	assert os.path.exists( _pythonIncPath ), 'Could not get path for python include files, tried %s'  %  ( _pythonIncPath, )
	assert os.path.exists( _pythonLibPath ), 'Could not get path for python library files, tried %s'  %  ( _pythonLibPath, )

	_vc8RootPath = os.environ['VCINSTALLDIR']
	_vc8IncPaths = os.environ['INCLUDE'].split( ';' )
	_vc8LibPaths = os.environ['LIB'].split( ';' )


	# Attempt to get the boost path
	try:
		_platformSDKRootPath = os.environ['MSSDK']
	except KeyError:
		_vs8RootPath = os.environ['VSINSTALLDIR']
		_platformSDKRootPath = os.path.join( _vs8RootPath, 'Microsoft Platform SDK' )

	assert os.path.exists( _platformSDKRootPath ), 'Could not get path for Microsoft Platform SDK, tried %s, you can set the environment variable \'MSSDK\' to the SDK install path'  %  ( _platformSDKRootPath, )

	_platformSDKIncPath = os.path.join( _platformSDKRootPath, 'include' )
	_platformSDKLibPath = os.path.join( _platformSDKRootPath, 'lib' )

	assert os.path.exists( _platformSDKIncPath ), 'Could not get path for Microsoft Platform SDK include files, tried %s'  %  ( _platformSDKIncPath, )
	assert os.path.exists( _platformSDKLibPath ), 'Could not get path for Microsoft Platform SDK include files, tried %s'  %  ( _platformSDKLibPath, )


	# Attempt to get the boost path
	try:
		_boostRootPath = os.environ['BOOST']
	except KeyError:
		_drive = os.path.splitdrive( sys.prefix )[0]
		_boostRootPath = os.path.join( _drive, '\\boost' )

	assert os.path.exists( _boostRootPath ), 'Could not get path for Boost, tried %s, you can set the environment variable \'BOOST\' to the boost install path'  %  ( _boostRootPath, )

	_boostIncPath = os.path.join( _boostRootPath, 'boost_1_33_1' )
	_boostLibPath = os.path.join( _boostRootPath, 'boost_1_33_1\\libs\\python\\build\\bin-stage' )

	assert os.path.exists( _boostIncPath ), 'Could not get path for Boost include files, tried %s'  %  ( _boostIncPath, )
	assert os.path.exists( _boostLibPath ), 'Could not get path for Boost include files, tried %s'  %  ( _boostLibPath, )


	localIncPaths = [ 'cpp' ]
	pyIncPaths = [ _pythonIncPath ]
	boostPyIncPaths = [ _boostIncPath ]
	standardIncPaths = _vc8IncPaths  +  [ _platformSDKIncPath ]
	localLibPaths = [ '.' ]
	pyLibPaths = [ _pythonLibPath ]
	boostPyLibPaths = [ _boostLibPath ]
	standardLibPaths = _vc8LibPaths  +  [ _platformSDKLibPath ]

	pyLibs = [ 'python%s%s'  %  ( sys.version_info[0], sys.version_info[1] ) ]
	boostPyLibs = [ 'boost_python' ]
	glLibs = [ 'OpenGL32', 'GLU32' ]
	ccFlags = [ '/nologo', '/EHsc', '/DLL', '/MD', '"/D_DllExport_=__declspec(dllexport)"', '/D_PLATFORM_WIN32_', '/D_FPU_X86_' ]
	linkFlags = [ '/NOLOGO' ]

	envPath = os.environ['PATH'].split( ';' )

	pyExtSuffix = '.pyd'
elif platform == PLATFORM_LINUX:
	_pythonVersion = '%d.%d'  %  ( sys.version_info[0], sys.version_info[1] )

	localIncPaths = [ 'cpp' ]
	pyIncPaths = [ os.path.join( sys.prefix, 'include', 'python%s'  %  ( _pythonVersion, ) ) ]
	boostPyIncPaths = []
	standardIncPaths = []
	localLibPaths = [ '.' ]
	pyLibPaths = []
	boostPyLibPaths = []
	standardLibPaths = []

	pyLibs = [ 'python%s'  %  ( _pythonVersion, ) ]
	boostPyLibs = [ 'boost_python' ]
	glLibs = [ 'GL', 'GLU' ]
	ccFlags = [ '-Wall', '-Werror', '-ffast-math', '-g', '-D_DllExport_=', '-D_PLATFORM_POSIX_', '-D_FPU_X86_' ]
	linkFlags = [ '-g' ]

	envPath = None

	pyExtSuffix = '.so'

incPaths = localIncPaths + pyIncPaths + boostPyIncPaths + standardIncPaths
libPaths = localLibPaths + pyLibPaths + boostPyLibPaths + standardLibPaths

extLibs = pyLibs + boostPyLibs + glLibs



env = Environment( ENV=os.environ )

# Remove optimisation flags
s = re.compile( '(-O2|-O3|-Os|-O)' )
for key in [ 'CXXFLAGS', 'CCFLAGS' ]:
	if env['ENV'].has_key( key ):
		os.environ[key] = s.sub('', env['ENV'][key])

# Get CC, CXX, and CCACHE_DIR from the OS environment
for key in [ 'CC', 'CXX', 'CCACHE_DIR' ]:
  if os.environ.has_key(key):
    env.Replace( **{key: os.environ[key]})

# Append the values of CCFLAGS, CXXFLAGS, CPPPATH, LINKFLAGS, LIBPATH in the OS environment to @env
for key in [ 'CCFLAGS', 'CXXFLAGS', 'CPPPATH', 'LINKFLAGS', 'LIBPATH',]:
  if os.environ.has_key(key):
    env.Append( **{key: os.environ[key].split(' ')} )

# Append @incPaths to CPPPATH, @ccFlags to CCFLAGS
env.Append(CPPPATH = incPaths)
env.Append(CCFLAGS = ccFlags)

# Append @libPaths to LIBPATH
if env.has_key( 'LIBPATH' ):
	libPaths = libPaths + env['LIBPATH']

# Set the path
if envPath is not None:
	env['ENV']['PATH'] = envPath



cppMathLib = env.SharedLibrary( 'Math', cppMathFiles, LIBPATH=libPaths, LIBS=extLibs+[] )
cppDocViewHelperLib = env.SharedLibrary( 'DocViewHelper', cppDocViewHelperFiles, LIBPATH=libPaths, LIBS=extLibs+[ 'Math' ] )
cppGraphViewHelperLib = env.SharedLibrary( 'GraphViewHelper', cppGraphViewHelperFiles, LIBPATH=libPaths, LIBS=extLibs + shLibsForShLib( [ 'Math' ] ) )

cppLibs = [ 'Math', 'DocViewHelper', 'GraphViewHelper' ]


env.SharedLibrary( os.path.join( 'Britefury', 'Math', 'Math' ), pyMathFiles, LIBS=extLibs + cppLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix  )
env.SharedLibrary( os.path.join( 'Britefury', 'DocViewHelper', 'DocViewHelper' ), pyDocViewHelperFiles, LIBS=extLibs + cppLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix  )
env.SharedLibrary( os.path.join( 'Britefury', 'GraphView', 'GraphViewHelper' ), pyGraphViewHelperFiles, LIBS=extLibs + cppLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix  )
env.SharedLibrary( os.path.join( 'Britefury', 'extlibs', 'greenlet', 'greenlet' ), cppGreenletFiles, LIBS=pyLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix )







