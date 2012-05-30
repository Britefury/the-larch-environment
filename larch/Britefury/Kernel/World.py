##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

from Britefury.Kernel.Plugin import Plugin
from Britefury.Config import Configuration
from Britefury.Config.PathsConfigPage import getPathsConfig

from BritefuryJ.AttributeTable import SimpleAttributeTable
from BritefuryJ.LSpace.Browser import Location
from BritefuryJ.Projection import Subject, ProjectiveBrowserContext



_internalSchemas = {}


		
def _get_attr(x, attrName, default=None):
	try:
		return getattr( x, attrName )
	except AttributeError:
		return default



class _WorldBrowserContext (ProjectiveBrowserContext):
	def __init__(self, world):
		super( _WorldBrowserContext, self ).__init__( True )
		self._world = world


	def inspectFragment(self, fragment, sourceElement, triggeringEvent):
		return self._world._inspectFragment( fragment, sourceElement, triggeringEvent )

	
	
class World (object):
	def __init__(self):
		super( World, self ).__init__()
		self._plugins = Plugin.loadPlugins()
		self.newDocumentFactories = []
		self._rootSubject = None
		self._importedModuleRegistry = set()
		self.configuration = Configuration.Configuration()
		self._fragmentInspector = None
		self._browserContext = None

		for plugin in self._plugins:
			plugin.initialise( self )

		self._browserContext = _WorldBrowserContext( self )
		self._browserContext.registerNamedSubject( 'config', self.configuration.subject )





	def registerNewDocumentFactory(self, plugin, newDocumentFactory):
		self.newDocumentFactories.append( newDocumentFactory )
		
	
	def setRootSubject(self, appStateSubject):
		self._rootSubject = appStateSubject
		if self._browserContext is not None:
			self._browserContext.registerMainSubject( self._rootSubject )

	def getRootSubject(self):
		return self._rootSubject



	def getBrowserContext(self):
		return self._browserContext



	def setFragmentInspector(self, inspector):
		self._fragmentInspector = inspector



	def _inspectFragment(self, fragment, sourceElement, triggeringEvent):
		if self._fragmentInspector is not None:
			return self._fragmentInspector( fragment, sourceElement, triggeringEvent )
		else:
			return False

		
		
	def registerImportedModule(self, fullname):
		self._importedModuleRegistry.add( fullname )
		
	def unregisterImportedModules(self, moduleNames):
		self._importedModuleRegistry -= set( moduleNames )
	
	
	def unloadImportedModules(self, moduleFullnames):
		modules = set( moduleFullnames )
		modulesToRemove = self._importedModuleRegistry & modules
		for moduleFullname in modulesToRemove:
			del sys.modules[moduleFullname]
		self._importedModuleRegistry -= modulesToRemove
		return modulesToRemove
		
		
	
	
	


	def enableImportHooks(self):
		sys.meta_path.append( self )
		
		
		
	def find_module(self, fullname, path=None):
		try:
			app_find_module = self._rootSubject.import_resolve
		except AttributeError:
			return None
		names = fullname.split( '.' )
		finder = self._rootSubject
		for name in names:
			try:
				resolver = finder.import_resolve
			except AttributeError:
				return None
			finder = resolver( name, fullname, path )
			if finder is None:
				return None
		return finder

	
	
class DocumentFactory (object):
	def __init__(self, menuLabelText, newDocumentFn):
		self.menuLabelText = menuLabelText
		self.newDocumentFn = newDocumentFn






class WorldDefaultOuterSubject (Subject):
	def __init__(self, world):
		super( WorldDefaultOuterSubject, self ).__init__( None )
		self._world = world


	def getFocus(self):
		return None

	def getPerspective(self):
		return None

	def getTitle(self):
		return '<default root subject>'

	def getSubjectContext(self):
		return SimpleAttributeTable.instance.withAttrs( world=self._world, document=None, docLocation=None, location=Location( '' ) )
