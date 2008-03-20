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

from Britefury.GLisp.GLispUtil import gLispSrcToString
from Britefury.GLisp.GLispInterpreter import specialform, GLispFrame, GLispModule, ModuleRegistry, _moduleRegistry

from Britefury.gSym.MetaMetaLanguage import metaMetaLanguageFactory



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
	def __init__(self):
		super( GSymEnvironment, self ).__init__()
		self._metaLanguageViewDefinition = None
	
	@specialform
	def withInternalMetaLanguage(self, env, xs):
		if len( xs ) < 3:
			env.raiseError( ValueError, xs, 'GSymEnvironment::internalMetaLanguage: requires at least 1 parameters (language instance target variable)' )
		
		varName = xs[2]
		expressions = xs[3:]
		
		
		if not isinstance( varName, str ):
			env.raiseError( ValueError, xs, 'GSymEnvironment::internalMetaLanguage: second parameter (language target variable) must be a string' )
		
		if varName[0] != '@':
			env.raiseError( ValueError, xs, 'GSymEnvironment::internalMetaLanguage: second parameter (language target variable) must start with a @' )
		
		
		metaMetaLanguageInstance = metaMetaLanguageFactory.createLanguageInstance()
		env[varName[1:]] = metaMetaLanguageInstance
		
		return env.evaluate( expressions )	
			
		
	def raiseError(self, exceptionClass, src, reason):
		raise exceptionClass, reason  +  '   ::   '  +  gLispSrcToString( src, 3 )
	
	
	def _f_getMetaLanguageViewDefinition(self):
		return self._metaLanguageViewDefinition

	def _f_setMetaLanguageViewDefinition(self, m):
		self._metaLanguageViewDefinition = m
		
	

		
def createGSymGLispEnvironment():
	gsym = GSymEnvironment()
	
	return GLispModule( gsym=gsym )



_moduleRegistry.moduleFactory = createGSymGLispEnvironment


