##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

from copy import copy

from Britefury.DocModel.DMIO import readSX

from Britefury.gSym import gSymEnvironment
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






class GMetaModuleFactory (object):
	def __init__(self, importer, name, xs):
		super( GMetaModuleFactory, self ).__init__()
		self._importer = importer

		self.name = name
		self.xs = xs
		
		self.factoryFunction = _compileGMeta( name, self.xs )
		
		
	def instantiate(self, world, moduleGlobals):
		env = gSymEnvironment.GSymEnvironment( self._importer._world, self.name )

		g = copy( moduleGlobals )
		g['__gsym__env__'] = env
		g['__gsym__globals__'] = g
		
		return self.factoryFunction( g )

		
		
class GMetaModuleImporter (object):
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
			xs = self._p_readModuleXs( path, realpath )
			module = GMetaModuleFactory( self, path, xs )
			self._modules[realpath] = module
		return module
	
	
	def createModule(self, name, xs):
		module = GMetaModuleFactory( self, name, xs )
		self._modules[name] = module
		return module
	
	
	def _p_readModuleXs(self, path, realpath):
		docXs = readSX( open( realpath, 'r' ) )
		xs = self._moduleImportContent( self._world, docXs )
		return xs
	


