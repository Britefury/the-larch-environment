##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Util.SignalSlot import *

import traceback

from Britefury.Math.Math import Colour3f

from Britefury.DocView.Toolkit.DTBin import DTBin
from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTEntry import DTEntry




class DTEntryLabel (DTBin):
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )
	startEditingSignal = ClassSignal()
	finishEditingSignal = ClassSignal()


	class _Label (DTLabel):
		def __init__(self, entryLabel, text, markup=None, font=None, colour=Colour3f( 0.0, 0.0, 0.0 )):
			super( DTEntryLabel._Label, self ).__init__( text, markup, font, colour )
			self._entryLabel = entryLabel


		def _o_onButtonDown(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonDown( localPos, button, state )
			return button == 1

		def _o_onButtonUp(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonUp( localPos, button, state )
			if button == 1:
				self._entryLabel._p_onLabelClicked()
				return True
			else:
				return False


	class _Entry (DTEntry):
		def __init__(self, entryLabel, text, font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 ), regexp=None):
			super( DTEntryLabel._Entry, self ).__init__( text, font, borderWidth, backgroundColour, highlightedBackgroundColour, textColour, highlightedTextColour, borderColour, regexp=regexp )
			self._entryLabel = entryLabel

		def _o_onLoseFocus(self):
			super( DTEntryLabel._Entry, self )._o_onLoseFocus()
			if not self._bFocusGrabbed:
				self._entryLabel._p_onEntryLoseFocus()






	def __init__(self, text='', font=None, textColour=Colour3f( 0.0, 0.0, 0.0 ), regexp=None):
		super( DTEntryLabel, self ).__init__()

		self._text = text

		self._label = self._Label( self, text, None, font, textColour )
		self._entry = self._Entry( self, text, font, textColour=textColour, regexp=regexp )
		self._entry.textInsertedSignal.connect( self._p_onEntryTextInserted )
		self._entry.textDeletedSignal.connect( self._p_onEntryTextDeleted )
		self._entry.returnSignal.connect( self._p_onEntryReturn )

		self.setChild( self._label )

		self._bIgnoreLoseFocus = False



	def getText(self):
		return self._text

	def setText(self, text):
		self._text = text
		self._label.text = text
		self._entry.text = text



	def setFont(self, font):
		self._label.setFont( font )
		self._entry.setFont( font )

	def getFont(self):
		return self._label.getFont()


	def setTextColour(self, colour):
		self._label.setColour( colour )
		self._entry.setTextColour( colour )

	def getTextColour(self):
		return self._label.getColour()



	def startEditing(self):
		if self.getChild() is not self._entry:
			self.setChild( self._entry )
			self._entry.grabFocus()
			self.startEditingSignal.emit( self, self.text )

	def finishEditing(self):
		self._p_finishEditing( False )

	def _p_finishEditing(self, bUserEvent):
		if self.getChild() is not self._label:
			# Ignore incoming 'lose focus' events
			self._bIgnoreLoseFocus = True
			self._entry.ungrabFocus()
			self._bIgnoreLoseFocus = False
			self.setChild( self._label )
			self.finishEditingSignal.emit( self, self.text, bUserEvent )


	def startEditingOnLeft(self):
		self.startEditing()
		self._entry.moveCursorToStart()

	def startEditingOnRight(self):
		self.startEditing()
		self._entry.moveCursorToEnd()



	def _p_onLabelClicked(self):
		self.startEditing()


	def _p_onEntryTextInserted(self, entry, position, bAppended, textInserted):
		self.textInsertedSignal.emit( self, position, bAppended, textInserted )
		self._text = self._entry.text
		self._label.text = self._text

	def _p_onEntryTextDeleted(self, entry, start, end, textDeleted):
		self.textDeletedSignal.emit( self, start, end, textDeleted )
		self._text = self._entry.text
		self._label.text = self._text

	def _p_onEntryReturn(self):
		self._p_finishEditing( True )

	def _p_onEntryLoseFocus(self):
		if not self._bIgnoreLoseFocus:
			self.finishEditing()


	def _p_setKeyHandler(self, handler):
		self._entry.keyHandler = handler

	def _p_setAllowableCharacters(self, chars):
		self._entry.allowableCharacters = chars

	def _p_setBEditable(self, bEditable):
		self._entry.bEditable = bEditable


	text = property( getText, setText )
	font = property( getFont, setFont )
	textColour = property( getTextColour, setTextColour )

	keyHandler = property( None, _p_setKeyHandler )
	allowableCharacters = property( None, _p_setAllowableCharacters )
	bEditable = property( None, _p_setBEditable )








if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import cairo

	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onInserted(entry, position, bAppended, text):
		print 'INSERT: ', position, bAppended, text

	def onDeleted(entry, start, end, text):
		print 'DELETE: ', start, end, text


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	entry = DTEntryLabel( 'Hello world' )
	doc.child = entry
	entry.textInsertedSignal.connect( onInserted )
	entry.textDeletedSignal.connect( onDeleted )


	window.add( doc )
	window.show()

	gtk.main()
