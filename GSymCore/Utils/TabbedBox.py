##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.StyleSheet import *


class TabbedBoxStyleSheet (StyleSheet):
	class _Params (object):
		def __init__(self, headerStyle, bodyStyle):
			self.headerStyle = headerStyle
			self.bodyStyle = bodyStyle
	
	def __init__(self, prototype=None):
		if prototype is not None:
			super( TabbedBoxStyleSheet, self ).__init__( prototype )
		else:
			super( TabbedBoxStyleSheet, self ).__init__()
			
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )

		self.initAttr( 'headerFont', Font( "SansSerif", Font.BOLD, 16 ) )
		self.initAttr( 'headerForeground', Color.BLACK )
		self.initAttr( 'headerPadding', 2.0 )
		
		self.initAttr( 'bodyPadding', 2.0 )
		
		self.initAttr( 'boxColour', Color( 161, 178, 160 ) )

		self._params = None
		
		
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttr( 'primitiveStyle', primitiveStyle )
	
	
	def withHeaderFont(self, headerFont):
		return self.withAttr( 'headerFont', headerFont )
	
	def withHeaderForeground(self, headerForeground):
		return self.withAttr( 'headerForeground', headerForeground )
	
	def withHeaderPadding(self, headerPadding):
		return self.withAttr( 'headerPadding', headerPadding )
	
	
	def withBodyPadding(self, bodyPadding):
		return self.withAttr( 'bodyPadding', bodyPadding )
	
	
	def withBoxColour(self, boxColour):
		return self.withAttr( 'boxColour', boxColour )
	
	
	
	def _getParams(self):
		if self._params is None:
			hpad = self['headerPadding']
			bpad = self['bodyPadding']
			boxColour = self['boxColour']
			primitiveStyle = self['primitiveStyle']
			self._params = self._Params( primitiveStyle.withFont( self['headerFont'] ).withForeground( self['headerForeground'] ).withBorder( EmptyBorder( hpad, hpad, hpad, hpad, boxColour ) ), \
			                             primitiveStyle.withBorder( SolidBorder( bpad, bpad, boxColour, None ) ) )
		return self._params
		
	
	
	def tabbedBox(self, tabTitle, contents):
		params = self._getParams()
		headerStyle = params.headerStyle
		bodyStyle = params.bodyStyle
		
		header = headerStyle.border( headerStyle.staticText( tabTitle ) )
		body = bodyStyle.border( bodyStyle.vbox( [ contents ] ) )
		return bodyStyle.vbox( [ header, body.alignHExpand() ] )
	
	
TabbedBoxStyleSheet.instance = TabbedBoxStyleSheet()
