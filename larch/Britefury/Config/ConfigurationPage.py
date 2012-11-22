##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Pres.Primitive import Primitive
from BritefuryJ.Pres.RichText import SplitLinkHeaderBar, TitleBar, Page, Head, Body
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import Subject

from Britefury.Util.Abstract import abstractmethod



_staticStyle = StyleSheet.style( Primitive.editable( False ) )


class ConfigurationPage (object):
	class _ConfigPageSubject (Subject):
		def __init__(self, page):
			super( ConfigurationPage._ConfigPageSubject, self ).__init__( None )
			self._page = page
		
		def getFocus(self):
			return self._page
		
		def getTitle(self):
			return self._page.getSubjectTitle()
		
	
	def __init__(self):
		self._subject = self._ConfigPageSubject( self )
		
		
	def __getstate__(self):
		return {}
	
	def __setstate__(self, state):
		self._subject = self._ConfigPageSubject( self )
		
		
		
	def getSubject(self):
		return self._subject
		
		
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
		configLink = Hyperlink( 'CONFIGURATION PAGE', fragment.subect.configSubject )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [ configLink ] )
		
		title = TitleBar( self.getTitleText() )

		head = _staticStyle.applyTo( Head( [ linkHeader, title ] ) )
		
		contents = self.__present_contents__( fragment, inheritedState )
		
		return self._configPageStyle.applyTo( Page( [ head, contents ] ) )
	
	
	_configPageStyle = StyleSheet.style( Primitive.editable( False ) )


	
	
	
