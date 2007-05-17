##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Colour3f, Vector2

from Britefury.UI.PopupWindow import PopupWindow

from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTPopupDocument import DTPopupDocument
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class DTAutoCompleteDropDown (DTPopupDocument):
	autoCompleteSignal = ClassSignal()


	class _Label (DTLabel):
		def __init__(self, autoComplete, text, font=None, textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0), highlightColour=Colour3f( 0.0, 0.0, 0.5 )):
			super( DTAutoCompleteDropDown._Label, self ).__init__( text, font, textColour )
			self._autoComplete = autoComplete
			self._textColour = textColour
			self._highlightedTextColour = highlightedTextColour
			self._highlightColour = highlightColour

			self._bHighlighted = False



		def _o_onButtonDown(self, localPos, button, state):
			super( DTAutoCompleteDropDown._Label, self )._o_onButtonDown( localPos, button, state )
			return button == 1

		def _o_onButtonUp(self, localPos, button, state):
			super( DTAutoCompleteDropDown._Label, self )._o_onButtonUp( localPos, button, state )
			if button == 1:
				self._autoComplete._p_onLabelClicked( self )
				return True
			else:
				return False


		def _o_draw(self, context):
			# Background
			if self._bHighlighted:
				context.rectangle( 0.0, 0.0, self._allocation.x, self._allocation.y )
				context.set_source_rgb( self._highlightColour.r, self._highlightColour.g, self._highlightColour.b )
				context.fill()

			super( DTAutoCompleteDropDown._Label, self )._o_draw( context )


		def enableHighlight(self):
			self._bHighlighted = True
			self.colour = self._highlightedTextColour

		def disableHighlight(self):
			self._bHighlighted = False
			self.colour = self._textColour





	def __init__(self, autoCompleteList):
		super( DTAutoCompleteDropDown, self ).__init__( False )

		self._autoCompleteList = autoCompleteList



	def showAt(self, widget, posInWidget):
		self._p_refreshContents()

		# Refresh
		self.popupAtWidget( widget, posInWidget, Vector2( 200.0, 250.0 ), DTPopupDocument.APP_CONTROLLED )



	def setAutoCompleteList(self, autoCompleteList):
		self._autoCompleteList = autoCompleteList

		if self.isVisible():
			self._p_refreshContents()



	def _o_draw(self, context):
		# Background
		context.rectangle( 0.5, 0.5, self._allocation.x - 1.0, self._allocation.y - 1.0 )

		# Border
		context.set_line_width( 1.0 )
		context.set_source_rgb( 0.0, 0.0, 0.0 )
		context.stroke()



	def _p_onLabelClicked(self, label):
		pass


	def _p_refreshContents(self):
		box = DTBox( direction=DTDirection.TOP_TO_BOTTOM )

		labels = [ self._Label( self, text )   for text in self._autoCompleteList ]
		box[:] = labels

		self.child = box
