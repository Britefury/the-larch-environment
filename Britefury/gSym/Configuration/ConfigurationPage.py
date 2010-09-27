##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.Controls import *

from BritefuryJ.DocPresent.Combinators.Primitive import *
from BritefuryJ.DocPresent.Combinators.RichText import *
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import StyleSheet

from BritefuryJ.GSym import GSymSubject

from Britefury.Kernel.Abstract import abstractmethod



_staticStyle = StyleSheet.instance.withAttr( Primitive.editable, False )


class ConfigurationPage (object):
	class _ConfigPageSubject (GSymSubject):
		def __init__(self, page):
			self._page = page
		
		def getFocus(self):
			return self._page
		
		def getTitle(self):
			return self._page.getSubjectTitle()
		
	
	def __init__(self):
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
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		configLink = Hyperlink( 'CONFIGURATION PAGE', Location( 'config' ) )
		systemLink = Hyperlink( 'SYSTEM PAGE', Location( 'system' ) )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [ configLink, systemLink ] )
		
		title = TitleBar( self.getTitleText() )

		head = _staticStyle.applyTo( Head( [ linkHeader, title ] ) )
		
		contents = self.__present_contents__( fragment, inheritedState )
		
		return Page( [ head, contents ] )
	
	
	


	
	
	
