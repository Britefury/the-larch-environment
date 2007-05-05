##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )

import cairo

from Britefury.Math.Math import Colour3f, Vector2

from Britefury.DocView.Toolkit.DTWidget import DTWidget
from Britefury.DocView.Toolkit.DTFont import DTFont



class DTLabel (DTWidget):
	def __init__(self, text='', font=None, colour=Colour3f( 0.0, 0.0, 0.0)):
		super( DTLabel, self ).__init__()

		if font is None:
			font = DTFont()

		self._text = text
		self._font = font
		self._font.changedSignal.connect( self._p_onFontChanged )
		self._colour = colour
		self._textPosition = Vector2()

		self._o_queueResize()



	def setText(self, text):
		self._text = text
		self._o_queueResize()

	def getText(self):
		return self._text


	def setFont(self, font):
		if self._font is not None:
			self._font.changedSignal.disconnect( self._p_onFontChanged )
		self._font = font
		if self._font is not None:
			self._font.changedSignal.connect( self._p_onFontChanged )
		self._o_queueResize()

	def getFont(self):
		return self._font


	def setColour(self, colour):
		self._colour = colour
		self._o_queueFullRedraw()

	def getColour(self):
		return self._colour



	def _o_draw(self, context):
		super( DTLabel, self )._o_draw( context )
		self._font.select( context )
		context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
		context.move_to( self._textPosition.x, self._textPosition.y )
		self._font.showText( context, self._text )

	def _o_getRequiredWidth(self):
		self._font.select( self._realiseContext )
		return self._font.getTextSpace( self._realiseContext, self._text ).x + 2

	def _o_getRequiredHeight(self):
		self._font.select( self._realiseContext )
		return self._font.getFontSpace( self._realiseContext ) + 2


	def _o_onAllocateX(self, allocation):
		super( DTLabel, self )._o_onAllocateX( allocation )

	def _o_onAllocateY(self, allocation):
		super( DTLabel, self )._o_onAllocateY( allocation )
		self._font.select( self._realiseContext )
		bearing, size, advance = self._font.getTextExtents( self._realiseContext, self._text )
		ascent, descent, height, maxAdvance = self._font.getFontExtents( self._realiseContext )
		size.y = height
		self._textPosition = ( self._allocation - size ) * 0.5  +  Vector2( -bearing.x + 1.0, ascent + 1.0 )


	def _p_onFontChanged(self):
		self._o_queueResize()



	text = property( getText, setText )
	font = property( getFont, setFont )
	colour = property( getColour, setColour )
