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



#class LanguageNotFoundError (Exception):
	#pass


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


#def _languageLoader(filename):
	#"""Loads the language in the specified file, and returns a language factory"""
	#doc = readSX( file( filename, 'r' ) )
	## TODO




_settingsDir = os.path.expanduser( '~/.gsym' )
#_languageRegistryPath = os.path.join( _settingsDir, 'languageregistry.xml' )



#_languageRegistry = LanguageRegistry( _languageLoader )
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
	#global _languageRegistry
	_initSettingsDir()
	
	#if os.path.exists( _languageRegistryPath ):
		#ioReadIntoObjectFromFile( _languageRegistry, file( _languageRegistryPath, 'r' ) )
	#else:
		#print 'Could not read language registry'


def shutdownGSymEnvironment():
	#if os.path.exists( _settingsDir ):
		#ioWriteObjectToFile( file( _languageRegistryPath, 'w' ), _languageRegistry )
	#else:
		#print 'Could not write language registry'

		


		
		
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
		
		path = name.replace( '.', '/' )
		
		module = _moduleRegistry.getModule( path )
		
		for i in imports:
			env[i] = module[i]
			
		return module
		
		
		
		
		
		
	#def importLanguage(self, name, vendor, version):
		#languageFactory = _languageRegistry.getLanguageFactory( name, vendor, version )
		#if languageFactory is not None:
			#return languageFactory.createLanguageInstance()
		#else:
			#raise LanguageNotFoundError
		

		
def createGSymGLispEnvironment():
	gsym = GSymEnvironment()
	
	return DMInterpreterEnv( gsym=gsym )



