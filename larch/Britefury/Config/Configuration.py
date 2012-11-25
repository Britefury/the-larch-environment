##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Pres.Primitive import Primitive, Column
from BritefuryJ.Pres.RichText import SplitLinkHeaderBar, TitleBar, Page, Head, Body
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import Subject


_staticStyle = StyleSheet.style( Primitive.editable( False ) )


class Configuration (object):
	class _ConfigurationSubject (Subject):
		def __init__(self, enclosingSubject, config):
			super( Configuration._ConfigurationSubject, self ).__init__( enclosingSubject )
			self._config = config
	
		@property
		def configSubject(self):
			return self


		def getFocus(self):
			return self._config
	
		def getTitle(self):
			return 'Configuration'


	
	def __init__(self, world):
		self._pages = []
		self.__world = world

		for page in _systemConfigPages:
			self.registerConfigurationPage( page )


	def subject(self, enclosingSubject):
		return self._ConfigurationSubject( enclosingSubject, self )


	def __present__(self, fragment, inheritedState):
		homeLink = Hyperlink( 'HOME PAGE', self.__world.rootSubject )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [] ).alignHExpand()
		
		title = TitleBar( 'Configuration' )
		
		head = Head( [ linkHeader, title ] )
		
		pageItemCmp = lambda itemA, itemB: cmp( itemA.getLinkText(), itemB.getLinkText() )
		pages = self._pages[:]
		pages.sort( pageItemCmp )
		
		links = [ Hyperlink( page.getLinkText(), page.subject( fragment.subject ) )   for page in pages ]
		body = Body( [ Column( links ) ] )
		
		return _staticStyle.applyTo( Page( [ head, body ] ) )
	
	
	def registerConfigurationPage(self, page):
		self._pages.append( page )




_systemConfigPages = []

def registerSystemConfigurationPage(page):
	_systemConfigPages.append( page )

def unregisterSystemConfigurationPage(page):
	_systemConfigPages.remove( page )

