##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
import sys
import app_in_jar


def appStartupFromFileSystem(larchJavaClassPath):
	sys.path.append( larchJavaClassPath )


def appStartupFromJar(larchJarURL):
	sys.packageManager.addJarToPackages( larchJarURL )
	app_in_jar.setLarchJarURL(larchJarURL)

