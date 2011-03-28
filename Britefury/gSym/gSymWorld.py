##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

from Britefury.gSym.gSymPlugin import GSymPlugin
from Britefury.gSym.Configuration import Configuration



_internalSchemas = {}


		
#
#
#
# GSYM World
#
#
#


def _get_attr(x, attrName, default=None):
	try:
		return getattr( x, attrName )
	except AttributeError:
		return default

	
	
class GSymWorld (object):
	def __init__(self):
		super( GSymWorld, self ).__init__()
		self._plugins = GSymPlugin.loadPlugins()
		self.newDocumentFactories = []
		self._appStateSubject = None
		self._importedModuleRegistry = set()
		self.configuration = Configuration.Configuration()
		
		
		for plugin in self._plugins:
			plugin.initialise( self )
			
				
		

	def registerNewDocumentFactory(self, plugin, newDocumentFactory):
		self.newDocumentFactories.append( newDocumentFactory )
		
	
	def setAppStateSubject(self, plugin, appStateSubject):
		self._appStateSubject = appStateSubject
		
	def getAppStateSubject(self):
		return self._appStateSubject
	
		
		
	def registerImportedModule(self, fullname):
		self._importedModuleRegistry.add( fullname )
		
	def unloadImportedModules(self, moduleFullnames):
		modules = set( moduleFullnames )
		modulesToRemove = self._importedModuleRegistry & modules
		for moduleFullname in modulesToRemove:
			del sys.modules[moduleFullname]
		self._importedModuleRegistry -= modulesToRemove
		return modulesToRemove
		
		
	
	
	
	def registerBrowserContext(self, browserContext):
		browserContext.registerNamedSubject( 'config', self.configuration.subject )
	
	
	
	def enableImportHooks(self):
		sys.meta_path.append( self )
		
		
		
	def find_module(self, fullname, path=None):
		try:
			app_find_module = self._appStateSubject.find_module
		except AttributeError:
			return None
		return app_find_module( fullname, path, self )

	
	
class GSymDocumentFactory (object):
	def __init__(self, menuLabelText, newDocumentFn):
		self.menuLabelText = menuLabelText
		self.newDocumentFn = newDocumentFn