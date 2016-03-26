##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import sys

from Britefury.Kernel.Plugin import Plugin
from Britefury.Kernel.Document import Document
from Britefury.Config import Configuration

from BritefuryJ.AttributeTable import SimpleAttributeTable
from BritefuryJ.Projection import TransientSubject



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
		return '<World>'




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







