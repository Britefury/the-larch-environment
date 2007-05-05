import os
import sys


def prefixPaths(prefix, paths):
	return [ os.path.join( prefix, x )   for x in paths ]


def cppPrefixPaths(prefix, paths):
	return prefixPaths( os.path.join( 'cpp', prefix ), paths )



cppMathFiles = cppPrefixPaths( 'Math', [ 'Polygon2.cpp', 'Segment2.cpp' ] )
pyMathFiles = cppPrefixPaths( 'Math', [ 'pyAxis.cpp', 'pyBBox2.cpp', 'pyColour3f.cpp', 'pyPoint2.cpp', 'pyPolygon2.cpp', 'pySegment2.cpp', 'pySide.cpp', 'pyVector2.cpp', 'pyXform2.cpp', 'pyMath.cpp' ] )

cppDocViewHelperFiles = cppPrefixPaths( 'DocViewHelper', [ 'DocViewBoxTable.cpp' ] )
pyDocViewHelperFiles = cppPrefixPaths( 'DocViewHelper', [ 'pyDocViewBoxTable.cpp', 'pyDocViewHelper.cpp' ] )

cppGreenletFiles = cppPrefixPaths( os.path.join( 'extlibs', 'greenlet' ), [ 'greenlet.c' ] )




if sys.platform == 'win32':
	localIncPaths = [ 'cpp' ]
	pyIncPaths = [ 'C:\\python24\\include' ]
	boostPyIncPaths = [ 'D:\\boost\\boost_1_33_1' ]
	standardIncPaths = [ 'D:\\vs8\\vc\\include', 'D:\\vs8\\Microsoft Platform SDK\\include' ]
	localLibPaths = [ '.' ]
	pyLibPaths = [ 'C:\\python24\\libs' ]
	boostPyLibPaths = [ 'D:\\boost\\boost_1_33_1\\libs\\python\\build\\bin-stage' ]
	standardLibPaths = [ 'D:\\vs8\\vc\\lib', 'D:\\vs8\\Microsoft Platform SDK\\lib' ]

	pyLibs = [ 'python24' ]
	boostPyLibs = [ 'boost_python' ]
	glLibs = [ 'OpenGL32', 'GLU32' ]
	ccFlags = [ '/nologo', '/EHsc', '/DLL', '/MD', '"/D_DllExport_=__declspec(dllexport)"', '/D_PLATFORM_WIN32_', '/D_FPU_X86_' ]
	linkFlags = [ '/NOLOGO' ]

	envPath = [ 'D:\\vs8\\VC\\bin', 'D:\\vs8\\Common7\\IDE' ]

	pyExtSuffix = '.pyd'
else:
	localIncPaths = [ 'cpp' ]
	pyIncPaths = [ '/usr/include/python2.4' ]
	boostPyIncPaths = []
	standardIncPaths = []
	localLibPaths = [ '.' ]
	pyLibPaths = []
	boostPyLibPaths = []
	standardLibPaths = []

	pyLibs = [ 'python2.4' ]
	boostPyLibs = [ 'boost_python' ]
	glLibs = [ 'GL', 'GLU' ]
	ccFlags = [ '-Wall', '-Werror', '-ffast-math', '-g', '-D_DllExport_=', '-D_PLATFORM_POSIX_', '-D_FPU_X86_' ]
	linkFlags = [ '-g' ]

	envPath = None

	pyExtSuffix = '.so'

incPaths = localIncPaths + pyIncPaths + boostPyIncPaths + standardIncPaths
libPaths = localLibPaths + pyLibPaths + boostPyLibPaths + standardLibPaths

extLibs = pyLibs + boostPyLibs + glLibs



env = Environment( CCFLAGS=' '.join( ccFlags ), CPPPATH=incPaths, LINKFLAGS=linkFlags )
if envPath is not None:
	env['ENV']['PATH'] = envPath




cppMathLib = env.SharedLibrary( 'Math', cppMathFiles, LIBPATH=libPaths, LIBS=extLibs+[] )
cppDocViewHelperLib = env.SharedLibrary( 'DocViewHelper', cppDocViewHelperFiles, LIBPATH=libPaths, LIBS=extLibs+[ 'Math' ] )

cppLibs = [ 'Math', 'DocViewHelper' ]


env.SharedLibrary( os.path.join( 'Britefury', 'Math', 'Math' ), pyMathFiles, LIBS=extLibs + cppLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix  )
env.SharedLibrary( os.path.join( 'Britefury', 'DocViewHelper', 'DocViewHelper' ), pyDocViewHelperFiles, LIBS=extLibs + cppLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix  )
env.SharedLibrary( os.path.join( 'Britefury', 'extlibs', 'greenlet', 'greenlet' ), cppGreenletFiles, LIBS=pyLibs, LIBPATH=libPaths, SHLIBPREFIX='', SHLIBSUFFIX=pyExtSuffix )







