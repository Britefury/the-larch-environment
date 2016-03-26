##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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
