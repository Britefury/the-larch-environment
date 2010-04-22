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
		self._locationToDocument = {}
		self._documentIDCounter = 1
		self.newPageFactories = []
		self.newUnitFactories = []
		self.pageImporters = []
		self._appState = None
		self._appStatePerspective = None
		
		
		for plugin in self._plugins:
			plugin.initialise( self )
	

			
	def registerUnitClass(self, plugin, unitClass):
		schema = unitClass.getSchema()
		self.resolver._registerDMSchema( schema )
		self._unitClasses[schema.getLocation()] = unitClass
	
	def registerNewPageFactory(self, plugin, newPageFactory):
		self.newPageFactories.append( newPageFactory )
		
	def registerNewUnitFactory(self, plugin, newUnitFactory):
		self.newUnitFactories.append( newUnitFactory )
		
	def registerPageImporter(self, plugin, pageImporter):
		self.pageImporters.append( pageImporter )
		
	def registerAppStateAndPerspective(self, plugin, appState, appStatePerspective):
		assert self._appState is None
		self._appState = appState
		self._appStatePerspective = appStatePerspective
		
		
	def getAppState(self):
		return self._appState
	
	def getAppStatePerspective(self):
		return self._appStatePerspective
		
		
	def addNewDocument(self, document):
		location = 'Doc%03d'  %  self._documentIDCounter
		self._documentIDCounter += 1
		if location in self._locationToDocument:
			raise KeyError
		else:
			self._locationToDocument[location] = document
		return location
		
		
	

		
	def resolveRelativeLocation(self, enclosingSubject, relativeLocation):
		return self.resolveUnitRelativeLocation( self._unit, enclosingSubject, relativeLocation )
	
	
	def getDocument(self, location):
		try:
			return self._locationToDocument[location]
		except KeyError:
			return None
		
		
		
	
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
	


