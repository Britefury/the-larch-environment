##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Colour3f, Point2, Vector2
from Britefury.DocPresent.Toolkit.DTBin import DTBin 



_docToCurrent = {}


class DTHighlight (DTBin):
	contextSignal = ClassSignal()



	def __init__(self, borderOffset=3.0, borderWidth=1.0, highlightBorderWidth=1.0, borderColour=None, highlightBorderColour=Colour3f( 0.6, 0.6, 0.6 ), backgroundColour=None, highlightBackgroundColour=Colour3f( 0.95, 1.0, 0.95 ) ):
		super( DTHighlight, self ).__init__()

		self._borderOffset = borderOffset
		self._borderWidth = borderWidth
		self._highlightBorderWidth = highlightBorderWidth
		self._borderColour = borderColour
		self._highlightBorderColour = highlightBorderColour
		self._highlightBackgroundColour = highlightBackgroundColour
		self._bHighlighted = False



	def setBorderOffset(self, offset):
		self._borderOffset = offset
		self._o_queueFullRedraw()

	def getBorderOffset(self):
		return self._borderOffset


	def setBorderWidth(self, width):
		self._borderWidth = width
		self._o_queueFullRedraw()

	def getBorderWidth(self):
		return self._borderWidth


	def setHighlightBorderWidth(self, width):
		self._highlightBorderWidth = width
		self._o_queueFullRedraw()

	def getHighlightBorderWidth(self):
		return self._highlightBorderWidth


	def setBorderColour(self, colour):
		self._borderColour = colour
		self._o_queueFullRedraw()

	def getBorderColour(self):
		return self._borderColour


	def setHighlightBorderColour(self, colour):
		self._highlightBorderColour = colour
		self._o_queueFullRedraw()

	def getHighlightBorderColour(self):
		return self.highlightBorderColour


	def setHighlightBackgroundColour(self, colour):
		self._highlightBackgroundColour = colour
		self._o_queueFullRedraw()

	def getHighlightBackgroundColour(self):
		return self._highlightBackgroundColour


	def _o_queueFullRedraw(self):
		offset = self._borderOffset
		self._o_queueRedraw( Point2( -offset, -offset ), self._allocation  +  Vector2( offset*2.0, offset*2.0 ) )

		
		
	def _o_onEnter(self, localPos):
		super( DTHighlight, self )._o_onEnter( localPos )
		self._bHighlighted = True
		stack = _docToCurrent.setdefault( self._document, [] )
		if len( stack ) > 0:
			stack[-1]._o_queueFullRedraw()
		stack.append( self )
		self._o_queueFullRedraw()

	def _o_onLeave(self, localPos):
		super( DTHighlight, self )._o_onLeave( localPos )
		self._bHighlighted = False
		try:
			stack = _docToCurrent[self._document]
		except KeyError:
			pass
		else:
			if len( stack ) > 0:
				stack.pop()
			if len( stack ) > 0:
				stack[-1]._o_queueFullRedraw()
		self._o_queueFullRedraw()


	def _o_drawBackground(self, context):
		bHighlighted = self._bHighlighted
		bHighlightBackground = False
		try:
			stack = _docToCurrent[self._document]
		except KeyError:
			pass
		else:
			if len( stack ) > 0:
				bHighlightBackground = bHighlighted  and  stack[-1]  ==  self
		
		if bHighlighted:
			b = self._highlightBorderWidth
			borderColour = self._highlightBorderColour
		else:
			b = self._borderWidth
			borderColour = self._borderColour
			
		if bHighlightBackground:
			backgroundColour = self._highlightBackgroundColour
		else:
			backgroundColour = self._backgroundColour

			
		offset = self._borderOffset


		if backgroundColour is not None:
			context.rectangle( -offset, -offset, self._allocation.x + offset * 2.0, self._allocation.y + offset * 2.0 )
			context.set_source_rgb( backgroundColour.r, backgroundColour.g, backgroundColour.b )
			context.fill()

		if borderColour is not None:
			context.rectangle( b * 0.5 - offset, b * 0.5 - offset, self._allocation.x + offset * 2.0 - b, self._allocation.y + offset * 2.0 - b )
			context.set_line_width( b )
			context.set_source_rgb( borderColour.r, borderColour.g, borderColour.b )
			context.stroke()


		
	borderOffset = property( getBorderOffset, setBorderOffset )
	borderWidth = property( getBorderWidth, setBorderWidth )
	highlightBorderWidth = property( getHighlightBorderWidth, setHighlightBorderWidth )
	borderColour = property( getBorderColour, setBorderColour )
	highlightBorderColour = property( getHighlightBorderColour, setHighlightBorderColour )
	highlightBackgroundColour = property( getHighlightBackgroundColour, setHighlightBackgroundColour )

