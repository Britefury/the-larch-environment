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

from Britefury.DocPresent.Toolkit.DTBin import DTBin
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTEntry import DTEntry




class DTEntryLabel (DTBin):
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )
	startEditingSignal = ClassSignal()				# ( entry, text )
	finishEditingSignal = ClassSignal()			# ( entry, text, bChanged, bUserEvent )


	class _Label (DTLabel):
		def __init__(self, entryLabel, text, bUseMarkup=None, font=None, colour=Colour3f( 0.0, 0.0, 0.0 )):
			super( DTEntryLabel._Label, self ).__init__( text, bUseMarkup, font, colour )
			self._entryLabel = entryLabel


		def _o_onButtonDown(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonDown( localPos, button, state )
			return button == 1

		def _o_onButtonUp(self, localPos, button, state):
			super( DTEntryLabel._Label, self )._o_onButtonUp( localPos, button, state )
			if button == 1:
				self._entryLabel._p_onLabelClicked( localPos )
				return True
			else:
				return False

			
		#
		# FOCUS NAVIGATION METHODS
		#
		
		def _o_isFocusTarget(self):
			return True
			

		def startEditing(self):
			self._entryLabel.startEditing()
			
		def startEditingOnLeft(self):
			self._entryLabel.startEditingOnLeft()
			
		def startEditingOnRight(self):
			self._entryLabel.startEditingOnRight()
			
		def startEditingAtPosition(self, pos):
			self._entryLabel.startEditingAtPosition( pos )
			
		def finishEditing(self):
			self._entryLabel.finishEditing()

	
	
	class _Entry (DTEntry):
		def __init__(self, entryLabel, text, font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 )):
			super( DTEntryLabel._Entry, self ).__init__( text, font, borderWidth, backgroundColour, highlightedBackgroundColour, textColour, highlightedTextColour, borderColour )
			self._entryLabel = entryLabel

		def _o_onLoseFocus(self):
			super( DTEntryLabel._Entry, self )._o_onLoseFocus()
			if not self._bFocusGrabbed:
				self._entryLabel._p_onEntryLoseFocus()





	def __init__(self, labelText='', entryText=None, bLabelUseMarkup=False, font=None, textColour=Colour3f( 0.0, 0.0, 0.0 )):
		super( DTEntryLabel, self ).__init__()

		self._labelText = labelText
		self._entryText = entryText
		self._textAtStart = None
		
		self._bLabelUseMarkup = bLabelUseMarkup

		self._label = self._Label( self, labelText, bLabelUseMarkup, font, textColour )
		if entryText is None:
			entryText = labelText
		self._entry = self._Entry( self, entryText, font )
		self._entry.textInsertedSignal.connect( self._p_onEntryTextInserted )
		self._entry.textDeletedSignal.connect( self._p_onEntryTextDeleted )
		self._entry.returnSignal.connect( self._p_onEntryReturn )

		self.setChild( self._label )

		self._bIgnoreEntryLoseFocus = False



	def getLabelText(self):
		return self._labelText

	def setLabelText(self, text):
		self._labelText = text
		self._label.text = text
		if self._entryText is None:
			self._entry.text = text


	def getEntryText(self):
		return self._entryText

	def setEntryText(self, text):
		self._entryText = text
		if text is None:
			self._entry.text = self._labelText
		else:
			self._entry.text = text
		
		
	def getText(self):
		if self._entryText is None:
			return self._labelText
		else:
			return self._entryText


	def getLabelUseMarkup(self):
		return self._label.getUseMarkup()
	
	def setLabelUseMarkup(self, bUseMarkup):
		self._label.setUseMarkup( bUseMarkup )



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
			self._entry.startEditing()
			if self._entryText is None:
				self._textAtStart = self._labelText
			else:
				self._textAtStart = self._entryText
			self.startEditingSignal.emit( self, self._textAtStart )

	def startEditingOnLeft(self):
		self.startEditing()
		self._entry.moveCursorToStart()

	def startEditingOnRight(self):
		self.startEditing()
		self._entry.moveCursorToEnd()

	def startEditingAtPosition(self, pos):
		index = self.getChild().getCursorIndexAt( pos )
		self.startEditing()
		self._entry.setCursorIndex( index )

	def finishEditing(self):
		self._p_finishEditing( False )

	def _p_finishEditing(self, bUserEvent):
		if self.getChild() is not self._label:
			# Store @bUserEvent so that _p_onEditingFinish() can retrieve it
			self._bIgnoreEntryLoseFocus = True
			self._entry.ungrabFocus()
			self._bIgnoreEntryLoseFocus = False
			self.setChild( self._label )
			if self._entryText is None:
				t = self._labelText
			else:
				t = self._entryText
			self._o_emitFinishEditing( t, t != self._textAtStart, bUserEvent )

	def _o_emitFinishEditing(self, text, bChanged, bUserEvent):
		self.finishEditingSignal.emit( self, text, bChanged, bUserEvent )





	def getCursorIndex(self):
		if self.getChild() is self._entry:
			return self._entry.getCursorIndex()
		else:
			return None

	def getCursorPosition(self):
		if self.getChild() is self._entry:
			return self._entry.getCursorPosition()
		else:
			return None

	def isCursorAtStart(self):
		if self.getChild() is self._entry:
			return self._entry.isCursorAtStart()
		else:
			return False

	def isCursorAtEnd(self):
		if self.getChild() is self._entry:
			return self._entry.isCursorAtEnd()
		else:
			return False




	def _p_onLabelClicked(self, localPos):
		self.startEditing()
		index = self._label.getCursorIndexAt( localPos )
		if self._entryText is not None:
			proportion = float( index )  /  float( len( self._labelText ) )
			index = int( float( len( self._entryText ) )  *  proportion  +  0.5 )
		self._entry.setCursorIndex( index )


	def _p_onEntryTextInserted(self, entry, position, bAppended, textInserted):
		text = self._entry.text
		if self._entryText is None:
			self._labelText = text
			self._label.text = text
		else:
			self._entryText = text
		self._o_emitTextInserted( position, bAppended, textInserted )

	def _p_onEntryTextDeleted(self, entry, start, end, textDeleted):
		text = self._entry.text
		if self._entryText is None:
			self._labelText = text
			self._label.text = text
		else:
			self._entryText = text
		self._o_emitTextDeleted( start, end, textDeleted )

	def _o_emitTextInserted(self, position, bAppended, textInserted):
		self.textInsertedSignal.emit( self, position, bAppended, textInserted )

	def _o_emitTextDeleted(self, start, end, textDeleted):
		self.textInsertedSignal.emit( self, start, end, textDeleted )

	def _p_onEntryReturn(self, entry):
		self._p_finishEditing( True )

	def _p_onEntryLoseFocus(self):
		if not self._bIgnoreEntryLoseFocus:
			self._p_finishEditing( False )


	def _p_setKeyHandler(self, handler):
		self._entry.keyHandler = handler

	def _p_setAllowableCharacters(self, chars):
		self._entry.allowableCharacters = chars

	def _p_setBEditable(self, bEditable):
		self._entry.bEditable = bEditable

		


	#
	# CURSOR ENTITY METHODS
	#

	def _o_getFirstCursorEntity(self):
		return self._entry.getFirstCursorEntity()
	
	def _o_getLastCursorEntity(self):
		return self._entry.getLastCursorEntity()

	
	def _o_linkChildEntryCursorEntity(self, childEntry):
		# Prevent the DTBin superclass from linking in anything other than the entry
		pass
		#prevCursorEntity = self._f_getPrevCursorEntityBeforeChild( childEntry.child )
		#nextCursorEntity = self._f_getNextCursorEntityAfterChild( childEntry.child )
		#DTCursorEntity.splice( prevCursorEntity, nextCursorEntity, childEntry.child.getFirstCursorEntity(), childEntry.child.getLastCursorEntity() )

	def _o_unlinkChildEntryCursorEntity(self, childEntry):
		# Prevent the DTBin superclass from unlinking in anything other than the entry
		pass
		#DTCursorEntity.remove( childEntry.child.getFirstCursorEntity(), childEntry.child.getLastCursorEntity() )
		
		
		

	labelText = property( getLabelText, setLabelText )
	entryText = property( getEntryText, setEntryText )
	text = property( getText )
	bLabelUseMarkup = property( getLabelUseMarkup, setLabelUseMarkup )
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

	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	from Britefury.DocPresent.Toolkit.DTBox import DTBox
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
	doc.getGtkWidget().show()
	
	box = DTBox( direction=DTBox.TOP_TO_BOTTOM, spacing=10.0 )

	entry1 = DTEntryLabel( 'Hello world' )
	box.append( entry1 )
	entry1.textInsertedSignal.connect( onInserted )
	entry1.textDeletedSignal.connect( onDeleted )

	entry2 = DTEntryLabel( 'Hello world', 'Hi there' )
	box.append( entry2 )
	entry2.textInsertedSignal.connect( onInserted )
	entry2.textDeletedSignal.connect( onDeleted )

	doc.child = box


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
