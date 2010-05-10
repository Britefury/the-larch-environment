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




class TitleBarStyleSheet (StyleSheet):
	class _Params (object):
		def __init__(self, titleStyle, headerStyle, titleBorderWidth):
			self.titleStyle = titleStyle
			self.headerStyle = headerStyle
			self.titleBorderWidth = titleBorderWidth
	
	@DerivedAttributeMethod
	def _params(self):
		primitiveStyle = self['primitiveStyle']
		tpad = self['titlePadding']
		return self._Params( primitiveStyle.withAttrValues( self['titleTextAttrs'] ).withForeground( self['titleForeground'] ).withBorder( FilledBorder( tpad, tpad, tpad, tpad, self['titleBackground'] ) ), \
	                                     primitiveStyle.withAttrValues( self['headerTextAttrs'] ).withForeground( self['headerForeground' ] ),
	                                     self['titleBorderWidth'] )
		
	
	def __init__(self):
		super( TitleBarStyleSheet, self ).__init__()
			
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )

		self.initAttr( 'titleTextAttrs', AttributeValues( fontFace='Serif', fontBold=True, fontSize=32 ) )
		self.initAttr( 'titleForeground', Color.BLACK )
		self.initAttr( 'titleBackground', Color( 240, 240, 240 ) )
		self.initAttr( 'titlePadding', 5.0 )
		self.initAttr( 'titleBorderWidth', 5.0 )
		
		self.initAttr( 'headerTextAttrs', AttributeValues( fontFace='Sans serif', fontSize=14 ) )
		self.initAttr( 'headerForeground', Color( 0.0, 0.5, 0.0 ) )
	
	
	def newInstance(self):
		return TitleBarStyleSheet()
		
		
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttr( 'primitiveStyle', primitiveStyle )
	
	
	def withTitleTextAttrs(self, titleTextAttrs):
		return self.withAttr( 'titleTextAttrs', titleTextAttrs )
	
	def withTitleForeground(self, titleForeground):
		return self.withAttr( 'titleForeground', titleForeground )
	
	def withTitleBackground(self, titleBackground):
		return self.withAttr( 'titleBackground', titleBackground )
	
	def withTitlePadding(self, titlePadding):
		return self.withAttr( 'titlePadding', titlePadding )
	
	def withTitleBorderWidth(self, titleBorderWidth):
		return self.withAttr( 'titleBorderWidth', titleBorderWidth )
	
	
	def withHeaderTextAttrs(self, headerTextAttrs):
		return self.withAttr( 'headerTextAttrs', headerTextAttrs )
	
	def withHeaderForeground(self, headerForeground):
		return self.withAttr( 'headerForeground', headerForeground )
	
	

	def titleBar(self, text):
		params = self._params()
		titleStyle = params.titleStyle
		titleBorderWidth = params.titleBorderWidth
		
		title = titleStyle.staticText( text )
		titleBackground = titleStyle.border( title.alignHCentre() )
		return titleBackground.alignHExpand().pad( titleBorderWidth, titleBorderWidth ).alignHExpand()
	
	
	def titleBarWithHeader(self, headerText, text):
		params = self._params()
		titleStyle = params.titleStyle
		titleBorderWidth = params.titleBorderWidth
		headerStyle = params.headerStyle

		header = headerStyle.staticText( headerText )
		title = titleStyle.staticText( text )
		titleVBox = titleStyle.vbox( [ header.alignHCentre(), title.alignHCentre() ] )
		titleBackground = titleStyle.border( titleVBox.alignHCentre() )
		return titleBackground.alignHExpand().pad( titleBorderWidth, titleBorderWidth ).alignHExpand()


	
TitleBarStyleSheet.instance = TitleBarStyleSheet()

