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

from Britefury.GLisp.GLispInterpreter import specialform, GLispFrame, GLispModule, ModuleRegistry, _moduleRegistry

from Britefury.gSym.MetaLanguage import metaLanguageFactory



#
#
#
# SETTINGS
#
#
#

_settingsDir = os.path.expanduser( '~/.gsym' )



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
	def internalMetaLanguage(self, env, xs):
		if len( xs ) < 4:
			raise ValueError, 'GSymEnvironment::internalMetaLanguage: requires at least 2 parameter (control interface target variable, and language target variable)'
		
		ctlVarName = xs[2]
		varName = xs[3]
		expressions = xs[4:]
		
		
		if not isinstance( ctlVarName, str ):
			raise ValueError, 'GSymEnvironment::internalMetaLanguage: first parameter (control interface target variable) must be a string'
		
		if ctlVarName[0] != '@':
			raise ValueError, 'GSymEnvironment::internalMetaLanguage: first parameter (control interface target variable) must be a @'
	
		
		if not isinstance( varName, str ):
			raise ValueError, 'GSymEnvironment::internalMetaLanguage: first parameter (language target variable) must be a string'
		
		if varName[0] != '@':
			raise ValueError, 'GSymEnvironment::internalMetaLanguage: first parameter (language target variable) must be a @'
		
		
		metaLanguageControlInterface = metaLanguageFactory.createLanguageControlInterface()
		metaLanguageInterface = metaLanguageControlInterface.getLanguageInterface()
		env[ctlVarName[1:]] = metaLanguageControlInterface
		env[varName[1:]] = metaLanguageInterface
		
		return env.evaluate( expressions )	
			
		
		
	

		
def createGSymGLispEnvironment():
	gsym = GSymEnvironment()
	
	return GLispModule( gsym=gsym )



_moduleRegistry.moduleFactory = createGSymGLispEnvironment


