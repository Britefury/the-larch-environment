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
from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Projection import Subject


_staticStyle = StyleSheet.instance.withAttr( Primitive.editable, False )


class Configuration (object):
	class _ConfigurationSubject (Subject):
		def __init__(self, config):
			super( Configuration._ConfigurationSubject, self ).__init__( None )
			self._config = config
	
	
		def getFocus(self):
			return self._config
	
		def getTitle(self):
			return 'Configuration'
		
		
		def __getitem__(self, index):
			return self._config._pages[index].getSubject()


	
	def __init__(self):
		self._pages = []
		self.subject = self._ConfigurationSubject( self )
	
	
	def __present__(self, fragment, inheritedState):
		homeLink = Hyperlink( 'HOME PAGE', Location( '' ) )
		systemLink = Hyperlink( 'SYSTEM PAGE', Location( 'system' ) )
		linkHeader = SplitLinkHeaderBar( [ homeLink ], [ systemLink ] )
		
		title = TitleBar( 'Configuration' )
		
		head = Head( [ linkHeader, title ] )
		
		links = [ Hyperlink( page.getLinkText(), Location( 'config[%d]'  %  ( i, ) ) )   for i, page in enumerate( self._pages ) ]
		body = Body( [ Column( links ) ] )
		
		return _staticStyle.applyTo( Page( [ head, body ] ) )
	
	
	def getConfigurationLocation(self):
		return self._configLocation
	
	
	def registerConfigurationPage(self, page):
		self._pages.append( page )
		

		
		