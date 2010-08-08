##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from BritefuryJ.DocModel import DMSchema, DMSchemaResolver

from Britefury.gSym.gSymPlugin import GSymPlugin



_internalSchemas = {}


class GSymDMSchemaResolver (DMSchemaResolver):
	def __init__(self):
		self._locationToSchema = {}
		
		
	def getSchema(self, location):
		try:
			return _internalSchemas[location]
		except KeyError:
			try:
				return self._locationToSchema[location]
			except KeyError:
				raise DMSchemaResolver.CouldNotResolveSchemaException( location )
			
	
	
	def _registerDMSchema(self, mod):
		self._locationToSchema[mod.getLocation()] = mod
		

		
		
		
		
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
		self.resolver = GSymDMSchemaResolver()
		self._plugins = GSymPlugin.loadPlugins()
		self._unitClasses = {}
		self.newPageFactories = []
		self.newDocumentFactories = []
		self.pageImporters = []
		self._appStateSubject = None
		
		
		for plugin in self._plugins:
			plugin.initialise( self )
	

			
	def registerUnitClass(self, plugin, unitClass):
		schema = unitClass.getSchema()
		self.resolver._registerDMSchema( schema )
		self._unitClasses[schema.getLocation()] = unitClass
	
	def registerNewPageFactory(self, plugin, newPageFactory):
		self.newPageFactories.append( newPageFactory )
		
	def registerNewDocumentFactory(self, plugin, newDocumentFactory):
		self.newDocumentFactories.append( newDocumentFactory )
		
	def registerPageImporter(self, plugin, pageImporter):
		self.pageImporters.append( pageImporter )
		
	def registerAppStateSubject(self, plugin, appStateSubject):
		assert self._appStateSubject is None
		self._appStateSubject = appStateSubject
		
		
	def getAppStateSubject(self):
		return self._appStateSubject
		
		
		
	
	def getUnitClass(self, schemaLocation):
		try:
			return self._unitClasses[schemaLocation]
		except KeyError:
			print 'Could not get unit class %s; registered classes: %s'  %  ( schemaLocation, self._unitClasses.keys() )
			return None
		
	

	@staticmethod
	def registerInternalDMSchema(schema):
		_internalSchemas[schema.getLocation()] = schema
		
		
	@staticmethod
	def getInternalResolver():
		return _internalResolver

	
_internalResolver = GSymDMSchemaResolver()
	


