##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from BritefuryJ.DocModel import DMModule, DMModuleResolver



_internalModules = {}


class GSymDMModuleResolver (DMModuleResolver):
	def __init__(self, world):
		self._world = world
		self._locationToModule = {}
		
		
	def getModule(self, location):
		try:
			return _internalModules[location]
		except KeyError:
			try:
				return self._locationToModule[location]
			except KeyError:
				if self._world is not None:
					self._world._initialiseModule( location )
				
					try:
						return self._locationToModule[location]
					except KeyError:
						raise DMModuleResolver.CouldNotResolveModuleException( location )
				else:
					raise DMModuleResolver.CouldNotResolveModuleException( location )
			
	
	
	def registerDMModule(self, mod):
		self._locationToModule[mod.getLocation()] = mod
		

		
		
		
		
#
#
#
# GSYM World
#
#
#

class GSymWorld (object):
	def __init__(self):
		super( GSymWorld, self ).__init__()
		self.resolver = GSymDMModuleResolver( self )
		
		
	def registerDMModule(self, mod):
		self.resolver.registerDMModule( mod )

		
	def _initialiseModule(self, location ):
		mod = self._readModule( location )
		try:
			initFn = getattr( mod, 'initialiseModule' )
		except AttributeError:
			initFn = None
		
		if initFn is not None:
			return initFn( self )
		else:
			return None

	
	def getModuleLanguage(self, location):
		mod = self._readModule( location )
		return getattr( mod, 'language' )
	
	
	
	
	def _readModule(self, location):
		mod = __import__( location )
		components = location.split( '.' )
		for comp in components[1:]:
			mod = getattr( mod, comp )
		return mod
		
		
	
	@staticmethod
	def registerInternalDMModule(mod):
		_internalModules[mod.getLocation()] = mod
		
		
	@staticmethod
	def getInternalResolver():
		return _internalResolver

	
_internalResolver = GSymDMModuleResolver( None )
	
module = DMModule( 'GSymWorld', 'gsw', 'org.Britefury.gSym.Internal.GSymWorld' )

nodeClass_GSymPlugin = module.newClass( 'GSymPlugin', [ 'location' ] )


GSymWorld.registerInternalDMModule( module )
