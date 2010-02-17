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




class TitleBarStyleSheet (StyleSheet):
	class _Params (object):
		def __init__(self, titleStyle, headerStyle, titleBorderWidth):
			self.titleStyle = titleStyle
			self.headerStyle = headerStyle
			self.titleBorderWidth = titleBorderWidth
	
	def __init__(self, prototype=None):
		if prototype is not None:
			super( TitleBarStyleSheet, self ).__init__( prototype )
		else:
			super( TitleBarStyleSheet, self ).__init__()
			
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )

		self.initAttr( 'titleFont', Font( 'Serif', Font.BOLD, 32 ) )
		self.initAttr( 'titleForeground', Color.BLACK )
		self.initAttr( 'titleBackground', Color( 240, 240, 240 ) )
		self.initAttr( 'titlePadding', 5.0 )
		self.initAttr( 'titleBorderWidth', 5.0 )
		
		self.initAttr( 'headerFont', Font( 'Sans serif', Font.PLAIN, 14 ) )
		self.initAttr( 'headerForeground', Color( 0.0, 0.5, 0.0 ) )
		
		self._params = None
		
		
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttr( 'primitiveStyle', primitiveStyle )
	
	
	def withTitleFont(self, titleFont):
		return self.withAttr( 'titleFont', titleFont )
	
	def withTitleForeground(self, titleForeground):
		return self.withAttr( 'titleForeground', titleForeground )
	
	def withTitleBackground(self, titleBackground):
		return self.withAttr( 'titleBackground', titleBackground )
	
	def withTitlePadding(self, titlePadding):
		return self.withAttr( 'titlePadding', titlePadding )
	
	def withTitleBorderWidth(self, titleBorderWidth):
		return self.withAttr( 'titleBorderWidth', titleBorderWidth )
	
	
	def withHeaderFont(self, headerFont):
		return self.withAttr( 'headerFont', headerFont )
	
	def withHeaderForeground(self, headerForeground):
		return self.withAttr( 'headerForeground', headerForeground )
	
	

	def _getParams(self):
		if self._params is None:
			primitiveStyle = self['primitiveStyle']
			tpad = self['titlePadding']
			self._params = self._Params( primitiveStyle.withFont( self['titleFont'] ).withForeground( self['titleForeground'] ).withBorder( EmptyBorder( tpad, tpad, tpad, tpad, self['titleBackground'] ) ), \
			                             primitiveStyle.withFont( self['headerFont'] ).withForeground( self['headerForeground' ] ),
			                             self['titleBorderWidth'] )
		return self._params
		
	
	def titleBar(self, text):
		params = self._getParams()
		titleStyle = params.titleStyle
		titleBorderWidth = params.titleBorderWidth
		
		title = titleStyle.staticText( text )
		titleBackground = titleStyle.border( title.alignHCentre() )
		return titleBackground.alignHExpand().pad( titleBorderWidth, titleBorderWidth ).alignHExpand()
	
	
	def titleBarWithHeader(self, headerText, text):
		params = self._getParams()
		titleStyle = params.titleStyle
		titleBorderWidth = params.titleBorderWidth
		headerStyle = params.headerStyle

		header = headerStyle.staticText( headerText )
		title = titleStyle.staticText( text )
		titleVBox = titleStyle.vbox( [ header.alignHCentre(), title.alignHCentre() ] )
		titleBackground = titleStyle.border( titleVBox.alignHCentre() )
		return titleBackground.alignHExpand().pad( titleBorderWidth, titleBorderWidth ).alignHExpand()


	
TitleBarStyleSheet.instance = TitleBarStyleSheet()

