##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.DefaultPerspective import DefaultPerspective
from BritefuryJ.Projection import TransientSubject
from Britefury.Kernel.Document import Document

from LarchCore.MainApp.MainAppViewer.View import perspective
from LarchCore.MainApp.MainAppViewer.AboutPage import AboutPage


class MainAppSubject (TransientSubject):
	def __init__(self, appState, world):
		super( MainAppSubject, self ).__init__( world.worldSubject )
		self._appState = appState
		self._world = world
		self._about = AboutPageSubject( self )



	@property
	def aboutPageSubject(self):
		return self._about

	def getTrailLinkText(self):
		return 'Home'


	def getFocus(self):
		return self._appState
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Larch'
	

	def loadDocument(self, filename):
		document = Document.readFile( self._world, filename )
		if document is not None:
			self._appState.registerOpenDocument( document )
			return document
		return None
		
	
	
	def import_resolve(self, name, fullname, path):
		for appDocument in self._appState.getOpenDocuments():
			doc = appDocument.getDocument()
			subject = doc.newSubject( self, None, doc.getDocumentName() )
			try:
				resolve = subject.import_resolve
			except AttributeError:
				pass
			else:
				result = resolve( name, fullname, path )
				if result is not None:
					return result
		return None


	@property
	def ipython_context(self):
		return self.getFocus().ipython_context

	@property
	def kernel(self):
		return self.getFocus().ipython_kernel



class AboutPageSubject (TransientSubject):
	def __init__(self, appStateSubject):
		super( AboutPageSubject, self ).__init__( appStateSubject )
		self._aboutPage = AboutPage()
		self._appStateSubject = appStateSubject


	def getTrailLinkText(self):
		return 'About'


	def getFocus(self):
		return self._aboutPage

	def getPerspective(self):
		return DefaultPerspective.instance

	def getTitle(self):
		return 'About Larch'
