##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import sys
import app_in_jar


def appStartupFromFileSystem(larchJavaClassPath):
	sys.path.append( larchJavaClassPath )


def appStartupFromJar(larchJarURL):
	sys.packageManager.addJarToPackages( larchJarURL )
	app_in_jar.setLarchJarURL(larchJarURL)

