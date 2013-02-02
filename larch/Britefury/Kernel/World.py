##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys

from Britefury.Kernel.Plugin import Plugin
from Britefury.Kernel.Document import Document
from Britefury.Config import Configuration

from BritefuryJ.AttributeTable import SimpleAttributeTable
from BritefuryJ.Projection import TransientSubject



class _WorldImportHooks (object):
	def __init__(self, world):
		self.__world = world


	def find_module(self, fullname, path=None):
		try:
			app_find_module = self.__world._rootSubject.import_resolve
		except AttributeError:
			return None
		names = fullname.split( '.' )
		finder = self.__world._rootSubject
		for name in names:
			try:
				resolver = finder.import_resolve
			except AttributeError:
				return None
			finder = resolver( name, fullname, path )
			if finder is None:
				return None
		return finder




class _DocumentFactory (object):
	def __init__(self, menuLabelText, newDocumentContentFn):
		self.menuLabelText = menuLabelText
		self.__newDocumentContentFn = newDocumentContentFn
		self.__firstPageSubjectFn = None


	def makeDocument(self, world):
		content = self.__newDocumentContentFn()
		return Document( world, content )

	def firstPageSubject(self, documentContentSubject):
		if self.__firstPageSubjectFn is not None:
			return self.__firstPageSubjectFn( documentContentSubject )
		else:
			return documentContentSubject


	def firstPageSubjectFn(self, fn):
		"""
		fn: (documentContentSubject) -> firstPageSubject
		"""
		self.__firstPageSubjectFn = fn
		return self




class _WorldSubject (TransientSubject):
	def __init__(self, world):
		super( _WorldSubject, self ).__init__( None )
		self.__world = world


	@property
	def world(self):
		return self.__world

	@property
	def rootSubject(self):
		return self.__world.rootSubject


	def getFocus(self):
		return None

	def getPerspective(self):
		return None

	def getTitle(self):
		return '<world subject>'




class World (object):
	"""
	The World

	Maintains:

	- loaded plugins
	- factories for creating new documents (registering and retrieving)
	- application root subject
	- list of modules imported via import hooks
	- configuration
	- fragment inspector

	The 'newDocumentFactories' attribute contains a list of document factories.
	The 'configuration' attribute contains the application configuration.
	"""
	def __init__(self):
		super( World, self ).__init__()

		self.__worldOuterSubject = _WorldSubject( self )

		self.__plugins = Plugin.loadPlugins()
		self.newDocumentFactories = []
		self._rootSubject = None
		self.__importedModuleRegistry = set()
		self.configuration = Configuration.Configuration( self )
		self.__fragmentInspector = None

		for plugin in self.__plugins:
			plugin.initialise( self )

		self.__import_hooks = _WorldImportHooks( self )




	@property
	def worldSubject(self):
		return self.__worldOuterSubject


	@property
	def rootSubject(self):
		"""Property to retrieve the root subject"""
		return self._rootSubject


	def setRootSubject(self, appStateSubject):
		"""Set the root subject"""
		self._rootSubject = appStateSubject



	def setFragmentInspector(self, inspector):
		"""Set the fragment inspector"""
		self.__fragmentInspector = inspector



	def inspectFragment(self, fragment, sourceElement, triggeringEvent):
		if self.__fragmentInspector is not None:
			return self.__fragmentInspector( fragment, sourceElement, triggeringEvent )
		else:
			return False



	def documentContentFactory(self, description):
		"""Register a document content factory

		Use as a decorator:

		@world.documentContentFactory('New something')
		def newSomethingContent():
			return SomethingContent()
		"""
		def decorate(contentFactoryFn):
			factory = _DocumentFactory( description, contentFactoryFn )
			self.newDocumentFactories.append( factory )
			return factory
		return decorate



	def registerImportedModule(self, fullname):
		"""Register a module imported via the import hooks"""
		self.__importedModuleRegistry.add( fullname )
		
	def unregisterImportedModules(self, moduleNames):
		"""Unregister a set of modules imported via the import hooks"""
		self.__importedModuleRegistry -= set( moduleNames )
	
	
	def unloadImportedModules(self, moduleFullnames):
		"""Unload a list of modules

		Only unloads those imported via the import hooks
		"""
		modules = set( moduleFullnames )
		modulesToRemove = self.__importedModuleRegistry & modules
		for moduleFullname in modulesToRemove:
			del sys.modules[moduleFullname]
		self.__importedModuleRegistry -= modulesToRemove
		return modulesToRemove
		
		
	
	def enableImportHooks(self):
		"""Enable the import hooks"""
		if self.__import_hooks not in sys.meta_path:
			sys.meta_path.append( self.__import_hooks )
		
		
		





