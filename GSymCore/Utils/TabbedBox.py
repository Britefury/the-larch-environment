##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.StyleSheet import *

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod


class TabbedBoxStyleSheet (StyleSheet):
	class _Params (object):
		def __init__(self, headerStyle, bodyStyle):
			self.headerStyle = headerStyle
			self.bodyStyle = bodyStyle
	
	@DerivedAttributeMethod
	def _params(self):
		hpad = self['headerPadding']
		bpad = self['bodyPadding']
		boxColour = self['boxColour']
		primitiveStyle = self['primitiveStyle']
		return self._Params( primitiveStyle.withAttrValues( self['headerTextAttrs'] ).withForeground( self['headerForeground'] ).withBorder( FilledBorder( hpad, hpad, hpad, hpad, boxColour ) ), \
	                                     primitiveStyle.withBorder( SolidBorder( bpad, bpad, boxColour, None ) ) )

	
	def __init__(self):
		super( TabbedBoxStyleSheet, self ).__init__()
			
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )

		self.initAttr( 'headerTextAttrs', AttributeValues( fontFace='SansSerif', fontBold=True, fontSize=16 ) )
		self.initAttr( 'headerForeground', Color.BLACK )
		self.initAttr( 'headerPadding', 2.0 )
		
		self.initAttr( 'bodyPadding', 2.0 )
		
		self.initAttr( 'boxColour', Color( 161, 178, 160 ) )
	
	
	def newInstance(self):
		return TabbedBoxStyleSheet()
		
		
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttr( 'primitiveStyle', primitiveStyle )
	
	
	def withHeaderTextAttrs(self, headerTextAttrs):
		return self.withAttr( 'headerTextAttrs', headerTextAttrs )
	
	def withHeaderForeground(self, headerForeground):
		return self.withAttr( 'headerForeground', headerForeground )
	
	def withHeaderPadding(self, headerPadding):
		return self.withAttr( 'headerPadding', headerPadding )
	
	
	def withBodyPadding(self, bodyPadding):
		return self.withAttr( 'bodyPadding', bodyPadding )
	
	
	def withBoxColour(self, boxColour):
		return self.withAttr( 'boxColour', boxColour )
	
	
	
	def tabbedBox(self, tabTitle, contents):
		params = self._params()
		headerStyle = params.headerStyle
		bodyStyle = params.bodyStyle
		
		header = headerStyle.border( headerStyle.staticText( tabTitle ) )
		body = bodyStyle.border( contents )
		return bodyStyle.vbox( [ header, body.alignHExpand() ] )
	
	
TabbedBoxStyleSheet.instance = TabbedBoxStyleSheet()
