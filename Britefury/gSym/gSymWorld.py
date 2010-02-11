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
		self._documentClasses = {}
		self._locationToDocument = {}
		self._documentIDCounter = 1
		self.newPageFactories = []
		self.newDocumentFactories = []
		self.pageImporters = []
		
		
		for plugin in self._plugins:
			plugin.initialise( self )
	

			
	def registerDocumentClass(self, plugin, documentClass):
		schema = documentClass.getSchema()
		self.resolver._registerDMSchema( schema )
		self._documentClasses[schema.getLocation()] = documentClass
	
	def registerNewPageFactory(self, plugin, newPageFactory):
		self.newPageFactories.append( newPageFactory )
		
	def registerNewDocumentFactory(self, plugin, newDocumentFactory):
		self.newDocumentFactories.append( newDocumentFactory )
		
	def registerPageImporter(self, plugin, pageImporter):
		self.pageImporters.append( pageImporter )
		
		
	def addNewDocument(self, document):
		location = 'Doc%03d'  %  self._documentIDCounter
		if location in self._locationToDocument:
			raise KeyError
		else:
			self._locationToDocument[location] = document
		return location
		
		
	

		
	def getDocument(self, location):
		try:
			return self._locationToDocument[location]
		except KeyError:
			return None
		
		
		
	
	def getDocumentClass(self, schemaLocation):
		try:
			return self._documentClasses[schemaLocation]
		except KeyError:
			print 'Could not get document class %s; registered classes: %s'  %  ( schemaLocation, self._documentClasses.keys() )
			return None
		
	

	@staticmethod
	def registerInternalDMSchema(schema):
		_internalSchemas[schema.getLocation()] = schema
		
		
	@staticmethod
	def getInternalResolver():
		return _internalResolver

	
_internalResolver = GSymDMSchemaResolver()
	


