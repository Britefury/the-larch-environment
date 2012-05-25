##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Controls import *

from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.LSpace.Browser import Location
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import Subject


_staticStyle = StyleSheet.style( Primitive.editable( False ) )


class Configuration (object):
	class _ConfigurationSubject (Subject):
		def __init__(self, config):
			super( Configuration._ConfigurationSubject, self ).__init__( None )
			self._config = config
	
	
		def getFocus(self):
			return self._config
	
		def getTitle(self):
			return 'Configuration'
		
		
		def __resolve__(self, name):
			return self._config._pagesByName[name].getSubject()


	
	def __init__(self):
		self._pagesByName = {}
		self.subject = self._ConfigurationSubject( self )
		
		for page in _systemConfigPages:
			self.registerConfigurationPage( page )
	
	
	def __present__(self, fragment, inheritedState):
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		systemLink = Hyperlink( 'SYSTEM PAGE', Location( 'system' ) )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [ systemLink ] ).alignHExpand()
		
		title = TitleBar( 'Configuration' )
		
		head = Head( [ linkHeader, title ] )
		
		pageItemCmp = lambda itemA, itemB: cmp( itemA[1].getLinkText(), itemB[1].getLinkText() )
		items = list( self._pagesByName.items() )
		items.sort( pageItemCmp )
		
		links = [ Hyperlink( page.getLinkText(), Location( 'config.%s'  %  ( name, ) ) )   for name, page in items ]
		body = Body( [ Column( links ) ] )
		
		return _staticStyle.applyTo( Page( [ head, body ] ) )
	
	
	def getConfigurationLocation(self):
		return self._configLocation
	
	
	def registerConfigurationPage(self, page):
		index = len( self._pagesByName )
		name = 'p%d'  %  index
		self._pagesByName[name] = page
		



_systemConfigPages = []

def registerSystemConfigurationPage(page):
	_systemConfigPages.append( page )

def unregisterSystemConfigurationPage(page):
	_systemConfigPages.remove( page )

