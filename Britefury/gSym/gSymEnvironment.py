##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os.path
import sys

from Britefury.GLisp.GLispUtil import gLispSrcToString



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
# GSYM ENVIRONMENT
#
#
#
		

class GSymEnvironment (object):
	def __init__(self, world, moduleName):
		super( GSymEnvironment, self ).__init__()
		self.world = world
		self._moduleName = moduleName
		self._moduleToModuleInstance = {}


	def raiseError(self, exceptionClass, src, reason):
		raise exceptionClass, reason  +  '   ::   '  +  gLispSrcToString( src, 3 )
	
	
	def _f_instantiateImportedModule(self, path, moduleGlobals):
		module = self.world.getModule( path )
		try:
			instance = self._moduleToModuleInstance[module]
		except KeyError:
			instance = module.instantiate( self.world, moduleGlobals )
			self._moduleToModuleInstance[module] = instance
		return instance
	
	
	

		

