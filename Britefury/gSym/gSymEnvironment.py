##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os.path

from Britefury.FileIO.IOXml import ioReadIntoObjectFromFile, ioWriteObjectToFile

from Britefury.DocModel.DMIO import readSX

from Britefury.GLisp.GLispInterpreter import GLispFrame, specialform
from Britefury.GLisp.GLispModule import GLispModule
from Britefury.GLisp.ModuleRegistry import ModuleRegistry



#
#
#
# SETTINGS
#
#
#

def _moduleLoader(filename):
	"""Loads the specified module, and return it"""
	module = GLispModule()
	doc = readSX( file( filename, 'r' ) )
	module.execute( doc )
	return module	


_settingsDir = os.path.expanduser( '~/.gsym' )


_moduleRegistry = ModuleRegistry( _moduleLoader )



def _initSettingsDir():
	if not os.path.exists( _settingsDir ):
		os.mkdir( _settingsDir )







#
#
#
# ENVIRONMENT INITIALISATION AND SHUTDOWN
#
#
#



def initGSymEnvironment():
	_initSettingsDir()


def shutdownGSymEnvironment():
	pass

		


		
		
#
#
#
# GSYM GLISP ENVIRONMENT
#
#
#
		

class GSymEnvironment (object):
	@specialform
	def importModule(self, env, xs):
		if len( xs ) < 3:
			raise TypeError, 'insufficient arguments for GSymEnvironment#importModule'

		name = xs[2]
		imports = xs[3:]
		
		path = name.split( '.' )
		path = os.path.join( path ) + '.gsym'
		
		module = _moduleRegistry.getModule( path )
		
		for i in imports:
			env[i] = module[i]
			
		return module
		
		
		
	

		
def createGSymGLispEnvironment():
	gsym = GSymEnvironment()
	
	return GLispModule( gsym=gsym )



