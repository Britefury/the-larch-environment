##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

from Britefury.gSym.gMeta.gMeta import compileGMeta




class GMetaModuleNotFoundError (Exception):
	pass

_coreDir = 'GSymCore'





def _getGMetaModulePath(path):
	realpath = os.path.realpath( path )
	
	if not os.path.exists( realpath ):
		realpath = os.path.realpath( os.path.join( _coreDir, path ) )
		
	if not os.path.exists( realpath ):
		raise GMetaModuleNotFoundError, path
	
	return realpath




class GMetaModule (object):
	def __init__(self, path, realpath):
		super( _GMetaModule, self ).__init__()
		self.path = path
		self.realpath = realpath
		
		self.xs = readSX( open( realpath, 'r' ) )
		
		self.factoryFunction = compileGMeta( path, self.xs )






class GMetaModuleRegistry (object):
	def __init__(self):
		super( GMetaModuleRegistry, self ).__init__()
		self._modules = {}
		
		
	def getModule(self, path):
		realpath = _getGMetaModulePath( path )
		try:
			module = self._modules[realpath]
		except KeyError:
			module = _GMetaModule( path, realpath )
			self._modules[realpath] = module
		return module
	
	def getModuleFactoryFunction(self, path):
		module = self.getModule( path )
		return module.factoryFunction
	


