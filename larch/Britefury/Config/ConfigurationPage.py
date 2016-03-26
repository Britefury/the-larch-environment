##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Pres.Primitive import Primitive
from BritefuryJ.Pres.RichText import SplitLinkHeaderBar, TitleBar, Page, Head, Body
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import TransientSubject

from Britefury.Util.Abstract import abstractmethod



_staticStyle = StyleSheet.style( Primitive.editable( False ) )


class ConfigurationPage (object):
	class _ConfigPageSubject (TransientSubject):
		def __init__(self, enclosingSubject, page):
			super( ConfigurationPage._ConfigPageSubject, self ).__init__( enclosingSubject )
			self._page = page
		
		def getFocus(self):
			return self._page
		
		def getTitle(self):
			return self._page.getSubjectTitle()
		
	
	def __init__(self):
		self.__config = None

		
	def __getstate__(self):
		return {}
	
	def __setstate__(self, state):
		pass

		
		
	def initPage(self, config):
		self.__config = config


	def subject(self, enclosingSubject=None):
		if enclosingSubject is None:
			enclosingSubject = self.__config.subject()
		return self._ConfigPageSubject( enclosingSubject, self )

		
	@abstractmethod
	def getSubjectTitle(self):
		pass
	
	@abstractmethod
	def getTitleText(self):
		pass
	
	@abstractmethod
	def getLinkText(self):
		pass
	
	
	@abstractmethod
	def __present_contents__(self, fragment, inheritedState):
		pass	


	def __present__(self, fragment, inheritedState):
		homeLink = Hyperlink( 'HOME PAGE', fragment.subject.rootSubject )
		configLink = Hyperlink( 'CONFIGURATION PAGE', fragment.subject.configSubject )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [ configLink ] )
		
		title = TitleBar( self.getTitleText() )

		head = _staticStyle.applyTo( Head( [ linkHeader, title ] ) )
		
		contents = self.__present_contents__( fragment, inheritedState )
		
		return self._configPageStyle.applyTo( Page( [ head, contents ] ) )
	
	
	_configPageStyle = StyleSheet.style( Primitive.editable( False ) )


	
	
	
