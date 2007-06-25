##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Colour3f
from Britefury.DocView.Toolkit.DTBorder import DTBorder



class DTActiveBorder (DTBorder):
	def __init__(self, leftMargin=3.0, rightMargin=3.0, topMargin=3.0, bottomMargin=3.0, borderWidth=1.0, highlightedBorderWidth=2.0, borderColour=None, prelitBorderColour=Colour3f( 0.5, 0.5, 0.5 ), highlightedBorderColour=Colour3f( 0.2, 0.2, 0.2 ), backgroundColour=None, highlightedBackgroundColour=Colour3f( 0.93, 0.93, 0.93 ) ):
		super( DTActiveBorder, self ).__init__( leftMargin, rightMargin, topMargin, bottomMargin, backgroundColour )

		self.keyHandler = None
		self._borderWidth = borderWidth
		self._highlightedBorderWidth = highlightedBorderWidth
		self._borderColour = borderColour
		self._prelitBorderColour = prelitBorderColour
		self._highlightedBorderColour = highlightedBorderColour
		self._highlightedBackgroundColour = highlightedBackgroundColour
		self._bPrelit = False



	def setBorderWidth(self, width):
		self._borderWidth = width
		self._o_queueFullRedraw()

	def getBorderWidth(self):
		return self._borderWidth


	def setHighlightedBorderWidth(self, width):
		self._highlightedBorderWidth = width
		self._o_queueFullRedraw()

	def getHighlightedBorderWidth(self):
		return self._highlightedBorderWidth


	def setBorderColour(self, colour):
		self._borderColour = colour
		self._o_queueFullRedraw()

	def getBorderColour(self):
		return self._borderColour


	def setPrelitBorderColour(self, colour):
		self._prelitBorderColour = colour

	def getPrelitBorderColour(self):
		return self._borderColour


	def setHighlightedBorderColour(self, colour):
		self.highlightedBorderColour = colour
		self._o_queueFullRedraw()

	def getHighlightedBorderColour(self):
		return self.highlightedBorderColour


	def setHighlightedBackgroundColour(self, colour):
		self._highlightedBackgroundColour = colour
		self._o_queueFullRedraw()

	def getHighlightedBackgroundColour(self):
		return self._highlightedBackgroundColour


	def _o_onButtonDown(self, localPos, button, state):
		super( DTActiveBorder, self )._o_onButtonDown( localPos, button, state )
		if button == 1  or  button == 2:
			self.grabFocus()
			return True
		else:
			return False

	def _o_onEnter(self, localPos):
		super( DTActiveBorder, self )._o_onEnter( localPos )
		self._bPrelit = True
		self._o_queueFullRedraw()

	def _o_onLeave(self, localPos):
		super( DTActiveBorder, self )._o_onLeave( localPos )
		self._bPrelit = False
		self._o_queueFullRedraw()


	def _o_onKeyPress(self, event):
		super( DTActiveBorder, self )._o_onKeyPress( event )
		if self.keyHandler is not None:
			self.keyHandler._f_handleKeyPress( self, event )


	def _o_onGainFocus(self):
		super( DTActiveBorder, self )._o_onGainFocus()
		self._o_queueFullRedraw()

	def _o_onLoseFocus(self):
		super( DTActiveBorder, self )._o_onLoseFocus()
		self._o_queueFullRedraw()


	def _o_draw(self, context):
		if self._bHasFocus:
			b = self._highlightedBorderWidth
		else:
			b = self._borderWidth

		# Get the colours
		if self._bHasFocus:
			backgroundColour = self._highlightedBackgroundColour
			borderColour = self._highlightedBorderColour
		else:
			backgroundColour = self._backgroundColour

			if self._bPrelit:
				borderColour = self._prelitBorderColour
			else:
				borderColour = self._borderColour



		if backgroundColour is not None   or   borderColour is not None:
			# Background
			context.rectangle( b * 0.5, b * 0.5, self._allocation.x - b, self._allocation.y - b )


			if backgroundColour is not None:
				context.set_source_rgb( backgroundColour.r, backgroundColour.g, backgroundColour.b )

				if borderColour is not None:
					context.fill_preserve()
				else:
					context.fill()

			if borderColour is not None:
				context.set_line_width( b )
				context.set_source_rgb( borderColour.r, borderColour.g, borderColour.b )
				context.stroke()


		super( DTActiveBorder, self )._o_draw( context )



	borderWidth = property( getBorderWidth, setBorderWidth )
	highlightedBorderWidth = property( getHighlightedBorderWidth, setHighlightedBorderWidth )
	borderColour = property( getBorderColour, setBorderColour )
	prelitBorderColour = property( getPrelitBorderColour, setPrelitBorderColour )
	highlightedBorderColour = property( getHighlightedBorderColour, setHighlightedBorderColour )
	highlightedBackgroundColour = property( getHighlightedBackgroundColour, setHighlightedBackgroundColour )

