##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import re

import pygtk
pygtk.require( '2.0' )
import gtk

import cairo
import pango
import pangocairo

from Britefury.Math.Math import Colour3f, Vector2, Point2, Segment2

from Britefury.Util.SignalSlot import *

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTAutoCompleteDropDown import DTAutoCompleteDropDown

from Britefury.DocPresent.Util.DUTextLayout import DUTextLayout



_modKeysMask = ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )




class _EntryTextSizeAllocator (object):
	def __init__(self, layout):
		self._layout = layout
		self._allocatedSize = None
		
		self._layout.evRequestResize = self._p_onRequestResize
		
		self.evRequestResize = None
		self.evRequestRedraw = None
		
		
	def getSize(self):
		if self._allocatedSize is None:
			self._allocatedSize = self._layout.getSize()
		return self._allocatedSize
	
	
	def reset(self):
		self._allocatedSize = None
		if self.evRequestResize is not None:
			self.evRequestResize()

		
		
	def _p_onRequestResize(self):
		bResizeRequired = False
		if self._allocatedSize is None:
			self._allocatedSize = self._layout.getSize()
			bResizeRequired = True
		else:
			size = self._layout.getSize()
			if size.y == self._allocatedSize.y:
				bufferZone = size.y * 3.0
				
				
				if size.x  <  self._allocatedSize.x - bufferZone * 2.0:
					self._allocatedSize.x = size.x
					bResizeRequired = True
				elif size.x  >  self._allocatedSize.x:
					self._allocatedSize.x = size.x + bufferZone
					bResizeRequired = True
			else:
				self._allocatedSize = size
				bResizeRequired = True
			
		if bResizeRequired:
			if self.evRequestResize is not None:
				self.evRequestResize()
		else:
			if self.evRequestRedraw is not None:
				self.evRequestRedraw()
		
			

				
				
class DTEntryListener (object):
	def onEntryCommit(self, entry):
		pass
	
	def onEntryFinish(self, entry):
		pass
		  
	def onBackstaceStart(self, entry):
		pass
	
	def onDeleteEnd(self, entry):
		pass
	
	def onTextInserted(self, entry, position, bAppended, textInserted):
		pass
	
	def onTextDelete(self, entry, startIndex, endIndex, textDeleted):
		pass
	
	def onTextModified(self, entry, text):
		pass
	
				
			
			

class DTEntry (DTWidget):
	def __init__(self, text='', font=None, borderWidth=1.0, backgroundColour=Colour3f( 1.0, 1.0, 1.0 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.7, 0.85, 0.7 ), autoCompleteList=None):
		super( DTEntry, self ).__init__()

		if font is None:
			font = 'Sans 11'

		self.keyHandler = None
		self.bEditable = True
		
		
		self.listener = None
		

		self._text = text
		
		self._borderWidth = borderWidth
		self._backgroundColour = backgroundColour
		self._highlightedBackgroundColour = highlightedBackgroundColour
		self._textColour = textColour
		self._highlightedTextColour = highlightedTextColour
		self._borderColour = borderColour

		self._layout = DUTextLayout( text, False, font )
		self._sizeAllocator = _EntryTextSizeAllocator( self._layout )
		self._layout.evRequestRedraw = self._o_queueFullRedraw
		self._sizeAllocator.evRequestResize = self._o_queueResize
		self._sizeAllocator.evRequestRedraw = self._o_queueFullRedraw

		self._textPosition = Vector2()
		self._entryPosition = Vector2()
		self._cursorIndex = 0
		self._selectionBounds = None
		self._bButtonPressed = False

		self._autoCompleteList = autoCompleteList
		self._autoCompleteDropDown = DTAutoCompleteDropDown( [], self )
		self._autoCompleteDropDown.autoCompleteSignal.connect( self._p_onAutoComplete )
		self._autoCompleteDropDown.autoCompleteDismissedSignal.connect( self._p_onAutoCompleteDismissed )
		self._bAutoCompleteDisabled = False
		
		self._o_queueResize()



	def setText(self, text):
		if text != self._text:
			self._text = text
			if self._bHasFocus:
				self._cursorIndex = min( self._cursorIndex, len( self._text ) )
			self._sizeAllocator.reset()
			self._layout.setText( text )

	def getText(self):
		return self._text


	def setFont(self, font):
		self._sizeAllocator.reset()
		self._layout.setFont( font )

	def getFont(self):
		return self._layout.getFont()



	def setBorderWidth(self, width):
		self._borderWidth = width
		self._o_queueResize()

	def getBorderWidth(self):
		return self._borderWidth


	def setBackgroundColour(self, colour):
		self._backgroundColour = colour
		self._o_queueFullRedraw()

	def getBackgroundColour(self):
		return self._backgroundColour


	def setHighlightedBackgroundColour(self, colour):
		self._highlightedBackgroundColour = colour
		self._o_queueFullRedraw()

	def getHighlightedBackgroundColour(self):
		return self._highlightedBackgroundColour


	def setTextColour(self, colour):
		self._textColour = colour
		self._o_queueFullRedraw()

	def getTextColour(self):
		return self._textColour


	def setHighlightedTextColour(self, colour):
		self._highlightedTextColour = colour
		self._o_queueFullRedraw()

	def getHighlightedTextColour(self):
		return self._highlightedTextColour


	def setBorderColour(self, colour):
		self._borderColour = colour
		self._o_queueFullRedraw()

	def getBorderColour(self):
		return self._borderColour



	def moveCursorToStart(self):
		self._cursorIndex = 0
		self._o_queueFullRedraw()

	def moveCursorToEnd(self):
		self._cursorIndex = len( self._text )
		self._o_queueFullRedraw()

	def setCursorIndex(self, index):
		index = min( max( index, 0 ), len( self._text ) )
		self._cursorIndex = index
		self._o_queueFullRedraw()

	def getCursorIndex(self):
		return self._cursorIndex

	def isCursorAtStart(self):
		return self._cursorIndex == 0

	def isCursorAtEnd(self):
		return self._cursorIndex == len( self._text )

	def getCursorPosition(self):
		pos, size = self._layout.getCharacterRectangle( self._cursorIndex )
		return Point2( pos.x, pos.y  +  size.y * 0.5 )
	
	
	def _p_getTextPosition(self):
		return self._textPosition



	def getCharacterIndexAt(self, point):
		return self._layout.getCharacterIndexAt( point - self._p_getTextPosition() )


	def getCharacterIndexAtX(self, x):
		y = self._p_getTextPosition().y + self._textSize.y * 0.5
		return self.getCharacterIndexAt( Point2( x, y ) )


	def getCursorIndexAt(self, point):
		return self._layout.getCursorIndexAt( point - self._p_getTextPosition() )


	def getCursorIndexAtX(self, x):
		y = self._p_getTextPosition().y + self._textSize.y * 0.5
		return self.getCursorIndexAt( Point2( x, y ) )




	def setAutoCompleteList(self, autoCompleteList):
		self._autoCompleteList = autoCompleteList


		
	def _p_setText(self, text):
		self._text = text
		self._layout.setText( text )


	def _p_displayAutoComplete(self):
		if self._autoCompleteList is not None:
			filtered = [ text   for text in self._autoCompleteList   if text.startswith( self._text ) ]
			bEmpty = len( filtered ) == 0
			
			entrySize = self._p_getEntrySize()

			if bEmpty:
				self._autoCompleteDropDown.hide()
			else:
				self._autoCompleteDropDown.setAutoCompleteList( filtered )
				if not self._autoCompleteDropDown.isVisible():
					self._autoCompleteDropDown.showAt( self, Point2( self._entryPosition.x, self._entryPosition.y + entrySize.y ) )




	def _p_getTextSize(self):
		return self._sizeAllocator.getSize()
	
	
	def _p_getEntrySize(self):
		return self._p_getTextSize()  +  Vector2( self._borderWidth * 2.0, self._borderWidth * 2.0 )
		

		
	def _p_onTextModified(self):
		if not self._bAutoCompleteDisabled:
			self._p_displayAutoComplete()



	def _p_moveCursor(self, bDragSelection, location):
		if bDragSelection:
			# Extend the selection
			if self._selectionBounds is None:
				bounds = self._cursorIndex, location
			else:
				first, second = self._selectionBounds
				if first != location:
					bounds = first, location
				else:
					bounds = None
		else:
			# Clear the selection
			bounds = None

		self._p_setSelectionBounds( bounds )

		self._cursorIndex = location
		self._o_queueFullRedraw()


	def _p_deleteSelection(self):
		start = min( self._selectionBounds )
		end = max( self._selectionBounds )
		if end > start:
			textDeleted = self._text[start:end]
			self._p_setText( self._text[:start] + self._text[end:] )
			self._cursorIndex = start
			if self.listener is not None:
				self.listener.onEntryTextDeleted( self, start, end, textDeleted )
		self._p_setSelectionBounds( None )



	def _o_onButtonDown(self, localPos, button, state):
		super( DTEntry, self )._o_onButtonDown( localPos, button, state )
		if button == 1:
			self.grabFocus()
			self._bButtonPressed = True
			index = self._layout.getCursorIndexAt( localPos - self._textPosition )
			self._p_moveCursor( False, index )
		return True


	def _o_onButtonDown2(self, localPos, button, state):
		super( DTEntry, self )._o_onButtonDown2( localPos, button, state )
		if button == 1:
			self._p_setSelectionBounds( ( 0, len( self._text ) ) )
			self._cursorIndex = self._selectionBounds[1]
			self._o_queueFullRedraw()
		return True


	def _o_onButtonUp(self, localPos, button, state):
		super( DTEntry, self )._o_onButtonUp( localPos, button, state )
		if button == 1:
			self._bButtonPressed = False
		return True


	def _o_onMotion(self, localPos):
		super( DTEntry, self )._o_onMotion( localPos )
		if self._bButtonPressed:
			index = self._layout.getCursorIndexAt( localPos - self._textPosition )
			self._p_moveCursor( True, index )


	def _p_onAutoComplete(self, autoComplete, text):
		deletedText = self._text
		self._p_setText( text )
		self._p_setSelectionBounds( None )
		if self.listener is not None:
			self.listener.onEntryTextDeleted( self, 0, len( deletedText ), deletedText )
		self._cursorIndex = len( self._text )
		if self.listener is not None:
			self.listener.onEntryTextInserted( self, 0, True, text )
			self.listener.onEntryTextModified( self, self.getText() )
		self._p_onTextModified()


	def _p_onAutoCompleteDismissed(self, autoComplete):
		self._bAutoCompleteDisabled = True



	def _f_handleMotionKeyPress(self, event):
		bHandled = False
		modKeys = event.state & _modKeysMask
		if event.keyVal == gtk.keysyms.Left:
			if modKeys == gtk.gdk.SHIFT_MASK  or  modKeys == 0:
				if self._cursorIndex > 0:
					self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, self._cursorIndex - 1 )
					bHandled = True
				else:
					# Handled if shift pressed
					bHandled = event.state & gtk.gdk.SHIFT_MASK  !=  0
		elif event.keyVal == gtk.keysyms.Right:
			if modKeys == gtk.gdk.SHIFT_MASK  or  modKeys == 0:
				if self._cursorIndex  <  len( self._text ):
					self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, self._cursorIndex + 1 )
					bHandled = True
				else:
					# Handled if shift pressed
					bHandled = event.state & gtk.gdk.SHIFT_MASK  !=  0
		elif event.keyVal == gtk.keysyms.Home:
			self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, 0 )
			bHandled = True
		elif event.keyVal == gtk.keysyms.End:
			self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, len( self._text ) )
			bHandled = True
		return bHandled

	
	
	def _o_onKeyPress(self, event):
		super( DTEntry, self )._o_onKeyPress( event )

		# Flag to determine if the key event has been handled
		bHandled = False

		modKeys = event.state & _modKeysMask
		if event.keyVal == gtk.keysyms.Return:
			if self.listener is not None:
				self.listener.onEntryCommit( self )
			self.ungrabFocus()
			bHandled = True
		elif event.keyVal == gtk.keysyms.BackSpace  and  self.bEditable:
			if self._selectionBounds is not None:
				self._p_deleteSelection()
				self._p_onTextModified()
			elif self._cursorIndex > 0:
				textDeleted = self._text[self._cursorIndex-1:self._cursorIndex]
				self._p_setText( self._text[:self._cursorIndex-1] + self._text[self._cursorIndex:] )
				self._cursorIndex -= 1
				if self.listener is not None:
					self.listener.onEntryTextDeleted( self, self._cursorIndex, self._cursorIndex+1, textDeleted )
					self.listener.onEntryTextModified( self, self.getText() )
				self._p_onTextModified()
			else:
				# Send backspace start signal
				if self.listener is not None:
					self.listener.onEntryBackspaceStart( self )
			bHandled = True
		elif event.keyVal == gtk.keysyms.Delete  and  self.bEditable:
			text = self._text
			if self._selectionBounds is not None:
				self._p_deleteSelection()
				self._p_onTextModified()
			elif self._cursorIndex < len( self._text ):
				textDeleted = self._text[self._cursorIndex:self._cursorIndex+1]
				self._p_setText( self._text[:self._cursorIndex] + self._text[self._cursorIndex+1:] )
				if self.listener is not None:
					self.listener.onEntryTextDeleted( self, self._cursorIndex, self._cursorIndex+1, textDeleted )
					self.listener.onEntryTextModified( self, self.getText() )
				self._p_onTextModified()
			else:
				# Send delete end signal
				if self.listener is not None:
					self.listener.onEntryDeleteEnd( self )
			bHandled = True
		elif event.keyString != ''  and  ( modKeys == 0  or  modKeys == gtk.gdk.SHIFT_MASK )  and  self.bEditable:
			if self._selectionBounds is not None:
				self._p_deleteSelection()
			position = self._cursorIndex
			bAppended = position == len( self._text )

			self._p_setText( self._text[:self._cursorIndex] + event.keyString + self._text[self._cursorIndex:] )
			self._cursorIndex += len( event.keyString )

			if self.listener is not None:
				self.listener.onEntryTextInserted( self, position, bAppended, event.keyString )
				self.listener.onEntryTextModified( self, self.getText() )
			self._p_onTextModified()
			bHandled = True

		# Not handled; pass to the key handler, if there is one
		if not bHandled:
			if self.keyHandler is not None:
				bHandled = self.keyHandler( self, event )


	def _o_onKeyRelease(self, event):
		super( DTEntry, self )._o_onKeyRelease( event )



	def _o_onGainFocus(self):
		super( DTEntry, self )._o_onGainFocus()
		self._cursorIndex = min( max( self._cursorIndex, 0 ), len( self._text ) )
		self._bAutoCompleteDisabled = False
		self._sizeAllocator.reset()

	def _o_onLoseFocus(self):
		super( DTEntry, self )._o_onLoseFocus()
		self._autoCompleteDropDown.hide()
		self._sizeAllocator.reset()


	def _o_draw(self, context):
		super( DTEntry, self )._o_draw( context )
		b = self._borderWidth

		self._o_clipIfAllocationInsufficient( context )

		textSize = self._p_getTextSize()
		entrySize = self._p_getEntrySize()


		# Background
		context.rectangle( self._entryPosition.x + b * 0.5, self._entryPosition.y + b * 0.5, entrySize.x - b, entrySize.y - b )

		# Fill
		context.set_source_rgb( self._backgroundColour.r, self._backgroundColour.g, self._backgroundColour.b )
		context.fill_preserve()

		# Border
		context.set_line_width( b )
		context.set_source_rgb( self._borderColour.r, self._borderColour.g, self._borderColour.b )
		context.stroke()

		# Text without selection
		context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
		context.move_to( self._textPosition.x, self._textPosition.y )
		self._layout.draw( context )
		
		if self._selectionBounds is not None  and  self._bHasFocus:
			# Text with selection
			start = min( self._selectionBounds )
			end = max( self._selectionBounds )
			
			startPos, startSize = self._layout.getCharacterRectangle( start )
			endPos, endSize = self._layout.getCharacterRectangle( end )
			
			context.set_source_rgb( self._highlightedBackgroundColour.r, self._highlightedBackgroundColour.g, self._highlightedBackgroundColour.b )
			context.rectangle( self._textPosition.x + startPos.x, self._textPosition.y, endPos.x - startPos.x, textSize.y )
			context.fill_preserve()
			
			context.save()
			context.clip()

			context.set_source_rgb( self._highlightedTextColour.r, self._highlightedTextColour.g, self._highlightedTextColour.b )
			context.move_to( self._textPosition.x, self._textPosition.y )
			self._layout.draw( context )
			
			context.restore()

		# Cursor
		if self._bHasFocus:
			charPos, charSize = self._layout.getCharacterRectangle( self._cursorIndex )
			cursorPositionX = self._textPosition.x + charPos.x
			context.set_line_width( 1.0 )
			context.set_source_rgb( 0.0, 0.0, 0.0 )
			context.move_to( cursorPositionX, self._entryPosition.y + self._borderWidth )
			context.rel_line_to( 0.0, textSize.y )
			context.stroke()




	def _o_onRealise(self, context, pangoContext):
		super( DTEntry, self )._o_onRealise( context, pangoContext )
		self._layout.initialise( context )


	def _o_onSetScale(self, scale, rootScale):
		context = self._realiseContext
		context.save()
		context.scale( rootScale, rootScale )
		self._layout.update( context )
		context.restore()


	def _o_getRequiredWidth(self):
		entrySize = self._p_getEntrySize()
		return entrySize.x

	def _o_getRequiredHeightAndBaseline(self):
		entrySize = self._p_getEntrySize()
		return entrySize.y,  0.0


	def _o_onAllocateX(self, allocation):
		super( DTEntry, self )._o_onAllocateX( allocation )

	def _o_onAllocateY(self, allocation):
		super( DTEntry, self )._o_onAllocateY( allocation )
		entrySize = self._p_getEntrySize()
		self._entryPosition = ( self._allocation - entrySize )  *  0.5
		self._textPosition = self._entryPosition + Vector2( self._borderWidth + 1.0, self._borderWidth + 1.0 )




	def _p_setSelectionBounds(self, bounds):
		self._selectionBounds = bounds



	#
	# FOCUS NAVIGATION METHODS
	#
	
	focusPolicy = DTWidget.FOCUSPOLICY_CHILDRENFIRST

	
	def startEditing(self):
		self.grabFocus()
		
	def startEditingOnLeft(self):
		self.startEditing()
		self.moveCursorToStart()
		
	def startEditingOnRight(self):
		self.startEditing()
		self.moveCursorToEnd()
		
	def startEditingAtPosition(self, pos):
		index = self.getCursorIndexAt( pos )
		self.startEditing()
		self.setCursorIndex( index )
		
	def finishEditing(self):
		if self.listener is not None:
			self.listener.onEntryFinish( self )
		self.ungrabFocus()

	
	
	#
	# DEBUG __repr__
	#
	
	def __repr__(self):
		return super( DTEntry, self ).__repr__()  +  '(\'%s\')'  %  ( self._text, )


	
	



	text = property( getText, setText )
	font = property( getFont, setFont )
	borderWidth = property( getBorderWidth, setBorderWidth )
	backgroundColour = property( getBackgroundColour, setBackgroundColour )
	highlightedBackgroundColour = property( getHighlightedBackgroundColour, setHighlightedBackgroundColour )
	textColour = property( getTextColour, setTextColour )
	highlightedTextColour = property( getHighlightedTextColour, setHighlightedTextColour )
	borderColour = property( getBorderColour, setBorderColour )
	autoCompleteList = property( None, setAutoCompleteList )






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


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.getGtkWidget().show()


	autoCompleteList = [ 'abc', 'Hello', 'Hello world', 'Hi', 'Hi world', 'Hello world 2' ]
	vbox = DTBox( DTBox.TOP_TO_BOTTOM )
	for y in xrange( 0, 5 ):
		hbox = DTBox()
		for x in xrange( 0, 5 ):
			entry = DTEntry( 'Hello world', autoCompleteList=autoCompleteList )
			hbox.append( entry )
		vbox.append( hbox )
	doc.child = vbox
	entry.grabFocus()


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
