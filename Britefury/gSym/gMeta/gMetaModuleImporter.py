##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

from Britefury.DocModel.DMIO import readSX

from Britefury.gSym.gMeta.gMeta import _compileGMeta




class GMetaModuleNotFoundError (Exception):
	pass

_coreDir = 'GSymCore'





def _getGMetaModulePath(path):
	if not path.lower().endswith( '.gsym' ):
		path = path + '.gsym'
	path = os.path.join( *path.split( '/') )
	realpath = os.path.realpath( path )
	
	if not os.path.exists( realpath ):
		realpath = os.path.realpath( os.path.join( _coreDir, path ) )
		
	if not os.path.exists( realpath ):
		raise GMetaModuleNotFoundError, path
	
	return realpath









class GMetaModuleImporter (object):
	class _Module (object):
		def __init__(self, importer, path, realpath):
			super( GMetaModuleImporter._Module, self ).__init__()
			self.path = path
			self.realpath = realpath
			
			docXs = readSX( open( realpath, 'r' ) )
			self.xs = importer._moduleImportContent( importer._world, docXs )
			
			self.factoryFunction = _compileGMeta( path, self.xs )
			
		
		
	def __init__(self, world, moduleImportContent):
		super( GMetaModuleImporter, self ).__init__()
		self._world = world
		self._modules = {}
		self._moduleImportContent = moduleImportContent
		
		
	def getModule(self, path):
		realpath = _getGMetaModulePath( path )
		try:
			module = self._modules[realpath]
		except KeyError:
			module = self._Module( self, path, realpath )
			self._modules[realpath] = module
		return module
	


