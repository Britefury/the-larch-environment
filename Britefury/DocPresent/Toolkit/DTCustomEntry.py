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
from Britefury.DocPresent.Toolkit.DTEntry import DTEntry




class DTCustomEntry (DTBin):
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )
	startEditingSignal = ClassSignal()				# ( entry, text )
	finishEditingSignal = ClassSignal()			# ( entry, text, bChanged, bUserEvent )


	class _CustomContainer (DTBin):
		def __init__(self, customEntry, backgroundColour=None):
			super( DTCustomEntry._CustomContainer, self ).__init__( backgroundColour )
			self._customEntry = customEntry
			
			
		def _o_onButtonDown(self, localPos, button, state):
			if button == 1:
				return True
			else:
				return super( DTCustomEntry._CustomContainer, self )._o_onButtonDown( localPos, button, state )

		def _o_onButtonDown2(self, localPos, button, state):
			if button == 1:
				return True
			else:
				return super( DTCustomEntry._CustomContainer, self )._o_onButtonDown2( localPos, button, state )

		def _o_onButtonDown3(self, localPos, button, state):
			if button == 1:
				return True
			else:
				return super( DTCustomEntry._CustomContainer, self )._o_onButtonDown3( localPos, button, state )

		def _o_onButtonUp(self, localPos, button, state):
			if button == 1:
				self._customEntry._p_onCustomClicked( localPos )
				return True
			else:
				return super( DTCustomEntry._CustomContainer, self )._o_onButtonUp( localPos, button, state )

			
		#
		# FOCUS NAVIGATION METHODS
		#
		
		def _o_isFocusTarget(self):
			return True
			

		def startEditing(self):
			self._customEntry.startEditing()
			
		def startEditingOnLeft(self):
			self._customEntry.startEditingOnLeft()
			
		def startEditingOnRight(self):
			self._customEntry.startEditingOnRight()
			
		def startEditingAtPosition(self, pos):
			self._customEntry.startEditingAtPosition( pos )
			
		def finishEditing(self):
			self._customEntry.finishEditing()

	
	
	class _Entry (DTEntry):
		def __init__(self, customEntry, text, font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 )):
			super( DTCustomEntry._Entry, self ).__init__( text, font, borderWidth, backgroundColour, highlightedBackgroundColour, textColour, highlightedTextColour, borderColour )
			self._customEntry = customEntry

		def _o_onLoseFocus(self):
			super( DTCustomEntry._Entry, self )._o_onLoseFocus()
			if not self._bFocusGrabbed:
				self._customEntry._p_onEntryLoseFocus()





	def __init__(self, entryText='', font=None):
		super( DTCustomEntry, self ).__init__()

		self._custom = self._CustomContainer( self )
		self._entry = self._Entry( self, entryText, font )
		self._entry.textInsertedSignal.connect( self._p_onEntryTextInserted )
		self._entry.textDeletedSignal.connect( self._p_onEntryTextDeleted )
		self._entry.returnSignal.connect( self._p_onEntryReturn )

		self.setChild( self._custom )
		
		self._textAtStart = None

		self._bIgnoreEntryLoseFocus = False
		
		
		
	def getCustomChild(self):
		return self._custom.getChild()
	
	def setCustomChild(self, child):
		self._custom.setChild( child )



	def getEntryText(self):
		return self._entry.text

	def setEntryText(self, text):
		self._entry.text = text
		
		
	def setFont(self, font):
		self._label.setFont( font )
		self._entry.setFont( font )

	def getFont(self):
		return self._label.getFont()


	def startEditing(self):
		if self.getChild() is not self._entry:
			self.setChild( self._entry )
			self._entry.startEditing()
			self._textAtStart = self._entry.text
			self.startEditingSignal.emit( self, self._entry.text )

	def startEditingOnLeft(self):
		self.startEditing()
		self._entry.moveCursorToStart()

	def startEditingOnRight(self):
		self.startEditing()
		self._entry.moveCursorToEnd()

	def startEditingAtPosition(self, pos):
		if self.getChild()  is  self._entry:
			index = self._entry.getCursorIndexAt( pos )
		else:
			index = len( self._entry.text )
		self.startEditing()
		self._entry.setCursorIndex( index )

	def finishEditing(self):
		self._p_finishEditing( False )

	def _p_finishEditing(self, bUserEvent):
		if self.getChild() is not self._custom:
			# Store @bUserEvent so that _p_onEditingFinish() can retrieve it
			self._bIgnoreEntryLoseFocus = True
			self._entry.ungrabFocus()
			self._bIgnoreEntryLoseFocus = False
			self.setChild( self._custom )
			self._o_emitFinishEditing( self._entry.text, self._entry.text != self._textAtStart, bUserEvent )

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




	def _p_onCustomClicked(self, localPos):
		self.startEditing()
		self._entry.moveCursorToEnd()


	def _p_onEntryTextInserted(self, entry, position, bAppended, textInserted):
		self._o_emitTextInserted( position, bAppended, textInserted )

	def _p_onEntryTextDeleted(self, entry, start, end, textDeleted):
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
		
		
		

	customChild = property( getCustomChild, setCustomChild )
	entryText = property( getEntryText, setEntryText )
	font = property( getFont, setFont )

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
	from Britefury.DocPresent.Toolkit.DTHLine import DTHLine
	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
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
	
	box = DTBox( direction=DTBox.TOP_TO_BOTTOM, spacing=10.0, alignment=DTBox.ALIGN_EXPAND )

	entry1 = DTCustomEntry( 'Hello world' )
	entry1.setCustomChild( DTHLine() )
	box.append( entry1 )
	entry1.textInsertedSignal.connect( onInserted )
	entry1.textDeletedSignal.connect( onDeleted )

	entry2 = DTCustomEntry( 'Hi there' )
	entry2.setCustomChild( DTLabel( 'xyz' ) )
	box.append( entry2 )
	entry2.textInsertedSignal.connect( onInserted )
	entry2.textDeletedSignal.connect( onDeleted )

	doc.child = box


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
