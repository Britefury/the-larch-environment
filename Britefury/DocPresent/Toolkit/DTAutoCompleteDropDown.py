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

from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTBorder import DTBorder
from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTPopupDocument import DTPopupDocument



class DTAutoCompleteDropDown (DTPopupDocument):
	autoCompleteSignal = ClassSignal()
	autoCompleteDismissedSignal = ClassSignal()


	class _Label (DTLabel):
		def __init__(self, autoComplete, text, bUseMarkup=False, font=None, textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0), highlightColour=Colour3f( 0.0, 0.0, 0.5 )):
			super( DTAutoCompleteDropDown._Label, self ).__init__( text, bUseMarkup, font, textColour, hAlign=DTLabel.HALIGN_LEFT )
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





	class _ACBorder (DTBorder):
		def __init__(self, acDropDown, leftMargin=0.0, rightMargin=0.0, topMargin=0.0, bottomMargin=0.0, backgroundColour=None):
			super( DTAutoCompleteDropDown._ACBorder, self ).__init__( leftMargin, rightMargin, topMargin, bottomMargin, backgroundColour )
			self._acDropDown = acDropDown
			self.grabFocus()

		def _o_onKeyPress(self, keyEvent):
			if keyEvent.keyVal == gtk.keysyms.Down:
				self._acDropDown.nextEntry()
				return True
			elif keyEvent.keyVal == gtk.keysyms.Up:
				self._acDropDown.prevEntry()
				return True
			elif keyEvent.keyVal == gtk.keysyms.Tab:
				text = self._acDropDown.getHighlightedText()
				if text is not None:
					self._acDropDown.autoCompleteSignal.emit( self._acDropDown, text )
				return True
			else:
				if self._acDropDown._owner is not None:
					self._acDropDown._owner._o_onKeyPress( keyEvent )





	def __init__(self, autoCompleteList, owner=None):
		super( DTAutoCompleteDropDown, self ).__init__( False )

		self._autoCompleteList = autoCompleteList
		self._owner = owner
		self._labels = []
		self._highlightIndex = 0
		self._bUserSelection = False
		self._border = self._ACBorder( self, 2.0, 2.0, 2.0, 2.0 )
		self.child = self._border


	def reset(self):
		label = self._p_getLabel( self._highlightIndex )
		if label is not None:
			label.disableHighlighting()

		self._highlightIndex = 0
		self._bUserSelection = False



	def nextEntry(self):
		x = self._highlightIndex + 1
		if x >= len( self._autoCompleteList ):
			x = 0
		self._p_setHighlightIndex( x )
		self._bUserSelection = True


	def prevEntry(self):
		x = self._highlightIndex - 1
		if x < 0:
			x = len( self._autoCompleteList ) - 1
		self._p_setHighlightIndex( x )
		self._bUserSelection = True



	def _p_setHighlightIndex(self, x):
		label = self._p_getLabel( self._highlightIndex )
		if label is not None:
			label.disableHighlight()

		self._highlightIndex = x

		label = self._p_getLabel( self._highlightIndex )
		if label is not None:
			label.enableHighlight()



	def getHighlightedText(self):
		try:
			return self._autoCompleteList[self._highlightIndex]
		except IndexError:
			return None



	def showAt(self, widget, posInWidget):
		self._p_refreshContents()

		# Refresh
		self.popupAtWidget( widget, posInWidget, Vector2( 200.0, 250.0 ), DTPopupDocument.APP_CONTROLLED )



	def setAutoCompleteList(self, autoCompleteList):
		currentText = None
		if self._bUserSelection:
			try:
				currentText = self._autoCompleteList[self._highlightIndex]
			except IndexError:
				pass

		self._autoCompleteList = autoCompleteList

		self._highlightIndex = 0
		if self._bUserSelection:
			try:
				self._highlightIndex = self._autoCompleteList.index( currentText )
			except ValueError:
				pass

		if self.isVisible():
			self._p_refreshContents()



	def _o_onEscapeClose(self):
		self.autoCompleteDismissedSignal.emit( self )




	def _o_draw(self, context):
		# Background
		context.rectangle( 0.5, 0.5, self._allocation.x - 1.0, self._allocation.y - 1.0 )

		# Border
		context.set_line_width( 1.0 )
		context.set_source_rgb( 0.0, 0.0, 0.0 )
		context.stroke()



	def _p_onLabelClicked(self, label):
		try:
			x = self._labels.index( label )
		except ValueError:
			pass
		else:
			self._p_setHighlightIndex( x )
			text = self.getHighlightedText()
			if text is not None:
				self.autoCompleteSignal.emit( self, text )


	def _p_refreshContents(self):
		box = DTBox( direction=DTBox.TOP_TO_BOTTOM )


		self._labels = [ self._Label( self, text )   for text in self._autoCompleteList ]
		box[:] = self._labels

		self._border.child = box

		label = self._p_getLabel( self._highlightIndex )
		if label is not None:
			label.enableHighlight()


	def _p_getLabel(self, index):
		try:
			return self._labels[index]
		except IndexError:
			return None
