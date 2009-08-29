##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os

from BritefuryJ.DocModel import DMModule, DMModuleResolver

from Britefury.gSym.gSymPlugin import GSymPlugin



_internalModules = {}


class GSymDMModuleResolver (DMModuleResolver):
	def __init__(self):
		self._locationToModule = {}
		
		
	def getModule(self, location):
		try:
			return _internalModules[location]
		except KeyError:
			try:
				return self._locationToModule[location]
			except KeyError:
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
	def __init__(self, pluginOverrides={}):
		super( GSymWorld, self ).__init__()
		self.resolver = GSymDMModuleResolver()
		self._plugins = GSymPlugin.loadPlugins( pluginOverrides )
		self._languages = {}
		self._locationToDocument = {}
		self.newPageFactories = []
		self.newDocumentFactories = []
		self.pageImporters = []
		
		
		for plugin in self._plugins:
			plugin.initialise( self )
	

			
	def registerDMModule(self, plugin, mod):
		self.resolver.registerDMModule( mod )
	
	def registerLanguage(self, plugin, language):
		self._languages[plugin.name] = language
	
	def registerNewPageFactory(self, plugin, newPageFactory):
		self.newPageFactories.append( newPageFactory )
		
	def registerNewDocumentFactory(self, plugin, newDocumentFactory):
		self.newDocumentFactories.append( newDocumentFactory )
		
	def registerPageImporter(self, plugin, pageImporter):
		self.pageImporters.append( pageImporter )

		
	
	def addDocument(self, location, document):
		if location in self._locationToDocument:
			raise KeyError
		else:
			self._locationToDocument[location] = document
		
		
	def getDocument(self, location):
		try:
			return self._locationToDocument[location]
		except KeyError:
			return None
		
		
		
	
	def getLanguage(self, location):
		try:
			return self._languages[location]
		except KeyError:
			print 'Could not get language %s/%s'  %  ( location, self._languages.keys() )
			return None
		
	

	@staticmethod
	def registerInternalDMModule(mod):
		_internalModules[mod.getLocation()] = mod
		
		
	@staticmethod
	def getInternalResolver():
		return _internalResolver

	
_internalResolver = GSymDMModuleResolver()
	


