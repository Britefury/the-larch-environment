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

from Britefury.Math.Math import Colour3f, Vector2, Point2

from Britefury.Util.SignalSlot import *

from Britefury.DocView.Toolkit.DTWidget import DTWidget
from Britefury.DocView.Toolkit.DTAutoCompleteDropDown import DTAutoCompleteDropDown



class DTEntry (DTWidget):
	returnSignal = ClassSignal()
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )


	def __init__(self, text='', font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 ), autoCompleteList=None, regexp=None):
		super( DTEntry, self ).__init__()

		if font is None:
			font = 'Sans 11'

		self.keyHandler = None
		self.bEditable = True
		self._text = text

		self._fontString = font
		self._fontDescription = pango.FontDescription( font )

		self._layout = None
		self._preSelectionLayout = None
		self._selectionLayout = None
		self._postSelectionLayout = None
		self._layoutContext = None

		self._bLayoutNeedsRefresh = True
		self._bSelectionLayoutsNeedRefresh = True

		self._borderWidth = borderWidth
		self._backgroundColour = backgroundColour
		self._highlightedBackgroundColour = highlightedBackgroundColour
		self._textColour = textColour
		self._highlightedTextColour = highlightedTextColour
		self._borderColour = borderColour

		self._textSize = None
		self._entrySize = Vector2()
		self._entryPosition = Vector2()
		self._textRoot = Vector2()
		self._textPosition = Vector2()
		self._cursorLocation = 0
		self._selectionBounds = None
		self._bButtonPressed = False

		self._autoCompleteList = autoCompleteList
		self._autoCompleteDropDown = DTAutoCompleteDropDown( [] )
		self._autoCompleteDropDown.autoCompleteSignal.connect( self._p_onAutoComplete )
		self._autoCompleteDropDown.autoCompleteDismissedSignal.connect( self._p_onAutoCompleteDismissed )
		self._bAutoCompleteDisabled = False

		if regexp is None:
			self._regexp = None
		else:
			self._regexp = re.compile( regexp )

		self._o_queueResize()



	def setText(self, text):
		if text != self._text:
			self._text = text
			if self._bHasFocus:
				self._cursorLocation = len( self._text )
			self._p_textChanged()
			self._o_queueResize()

	def getText(self):
		return self._text


	def setFont(self, font):
		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		self._textSize = None
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getFont(self):
		return self._fontString



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


	def setAutoCompleteList(self, autoCompleteList):
		self._autoCompleteList = autoCompleteList




	def _p_displayAutoComplete(self):
		if self._autoCompleteList is not None:
			filtered = [ text   for text in self._autoCompleteList   if text.startswith( self._text ) ]
			bEmpty = len( filtered ) == 0

			if bEmpty:
				self._autoCompleteDropDown.hide()
			else:
				self._autoCompleteDropDown.setAutoCompleteList( filtered )
				if not self._autoCompleteDropDown.isVisible():
					self._autoCompleteDropDown.showAt( self, Point2( self._entryPosition.x, self._entryPosition.y + self._entrySize.y ) )




	def _p_onTextModified(self):
		self._bLayoutNeedsRefresh = True
		self._bSelectionLayoutsNeedRefresh = True
		if self._textSize is None:
			self._p_computeTextSize()
		else:
			self._p_refreshLayout()
			x, y = self._layout.get_pixel_size()
			bufferZoneWidth = y * 3.0
			if x > self._textSize.x  or  x < ( self._textSize.x - ( bufferZoneWidth * 2.0 ) ):
				self._textSize = Vector2( x + bufferZoneWidth,  y )
				self._entrySize = self._textSize  +  Vector2( self._borderWidth * 2.0, self._borderWidth * 2.0 )
				self._o_queueResize()
		self._o_queueFullRedraw()
		if not self._bAutoCompleteDisabled:
			self._p_displayAutoComplete()



	def _p_moveCursor(self, bDragSelection, location):
		if bDragSelection:
			# Extend the selection
			if self._selectionBounds is None:
				bounds = self._cursorLocation, location
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

		self._cursorLocation = location
		self._o_queueFullRedraw()


	def _p_deleteSelection(self):
		start = min( self._selectionBounds )
		end = max( self._selectionBounds )
		textDeleted = self._text[start:end]
		self._text = self._text[:start] + self._text[end:]
		self._cursorLocation = start
		self.textDeletedSignal.emit( self, start, end, textDeleted )
		self._p_setSelectionBounds( None )



	def _p_textPosToTextLocation(self, textX):
		lowerX = 0.0
		upperX = self._layout.index_to_pos( len( self._text ) )[0]  /  pango.SCALE
		lowerLoc = 0
		upperLoc = len( self._text )
		while upperLoc  >  ( lowerLoc + 1 ):
			midLoc = ( lowerLoc + upperLoc ) >> 1
			midX = self._layout.index_to_pos( midLoc )[0]  /  pango.SCALE
			if textX < midX:
				upperLoc = midLoc
				upperX = midX
			else:
				lowerLoc = midLoc
				lowerX = midX
		if ( upperX - textX )  >  ( textX - lowerX ):
			return lowerLoc
		else:
			return upperLoc


	def _p_localPosToTextLocation(self, localPos):
		return self._p_textPosToTextLocation( localPos.x - self._textRoot.x )


	def _o_onButtonDown(self, localPos, button, state):
		super( DTEntry, self )._o_onButtonDown( localPos, button, state )
		if button == 1:
			self.grabFocus()
			self._bButtonPressed = True
			loc = self._p_localPosToTextLocation( localPos )
			self._p_moveCursor( False, loc )
		return True


	def _o_onButtonDown2(self, localPos, button, state):
		super( DTEntry, self )._o_onButtonDown2( localPos, button, state )
		if button == 1:
			self._p_setSelectionBounds( ( 0, len( self._text ) ) )
			self._cursorLocation = self._selectionBounds[1]
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
			loc = self._p_localPosToTextLocation( localPos )
			self._p_moveCursor( True, loc )


	def _p_onAutoComplete(self, autoComplete, text):
		deletedText = self._text
		self._text = text
		self._p_setSelectionBounds( None )
		self.textDeletedSignal.emit( self, 0, len( deletedText ), deletedText )
		self._cursorLocation = len( self._text )
		self.textInsertedSignal.emit( self, 0, True, text )
		self._p_onTextModified()


	def _p_onAutoCompleteDismissed(self, autoComplete):
		self._bAutoCompleteDisabled = True


	def _p_computeTextAfterDeletingSelection(self):
		if self._selectionBounds is not None:
			i = min( self._selectionBounds )
			j = max( self._selectionBounds )
			# Remove the selected text
			return self._text[:i] + self._text[j:]
		else:
			return self._text


	def _p_computeTextAfterDelete(self):
		text = self._text
		cursorLoc = self._cursorLocation
		if self._selectionBounds is not None:
			i = min( self._selectionBounds )
			j = max( self._selectionBounds )
			# Remove the selected text
			text = text[:i] + text[j:]
			cursorLoc = i
		if cursorLoc  <  len( text ) - 1:
			# Delete a character
			return text[:cursorLoc] + text[cursorLoc+1:]
		else:
			return text



	def _p_computeTextAfterBackspace(self):
		text = self._text
		cursorLoc = self._cursorLocation
		if self._selectionBounds is not None:
			i = min( self._selectionBounds )
			j = max( self._selectionBounds )
			# Remove the selected text
			text = text[:i] + text[j:]
			cursorLoc = i
		if cursorLoc > 0:
			# Delete a character
			return text[:cursorLoc-1] + text[cursorLoc:]
		else:
			return text



	def _p_computeTextAfterReplacingSelection(self, replacement):
		text = self._text
		cursorLoc = self._cursorLocation
		if self._selectionBounds is not None:
			i = min( self._selectionBounds )
			j = max( self._selectionBounds )
			# Remove the selected text
			text = text[:i] + text[j:]
			cursorLoc = i
		# Insert the string
		return text[:cursorLoc] + replacement + text[cursorLoc:]


	def _p_checkText(self, text):
		if self._regexp is not None:
			# Match to the regexp
			match = self._regexp.match( text )
			if match is not None:
				return match.span( 0 )  ==  ( 0, len( text ) )
			else:
				return None



	def _o_onKeyPress(self, event):
		super( DTEntry, self )._o_onKeyPress( event )

		# Flag to determine if the key event has been handled
		bHandled = False

		# If the auto-complete drop down is visible, pass the event to it, and see if it handles it
		if self._autoCompleteDropDown.isVisible():
			bHandled = self._autoCompleteDropDown.handleKeyPressEvent( event )

		# Not handle it ourselves
		if not bHandled:
			if event.keyVal == gtk.keysyms.Left:
				if self._cursorLocation > 0:
					self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, self._cursorLocation - 1 )
					bHandled = True
				else:
					# Handled if shift pressed
					bHandled = event.state & gtk.gdk.SHIFT_MASK  !=  0
			elif event.keyVal == gtk.keysyms.Right:
				if self._cursorLocation  <  len( self._text ):
					self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, self._cursorLocation + 1 )
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
			elif event.keyVal == gtk.keysyms.Return:
				self.returnSignal.emit()
				self.ungrabFocus()
				bHandled = True
			elif event.keyVal == gtk.keysyms.BackSpace  and  self.bEditable:
				bCanDelete = True
				if self._regexp is not None:
					text = self._p_computeTextAfterBackspace()
					if not self._p_checkText( text ):
						bCanDelete = False

				if bCanDelete:
					if self._selectionBounds is not None:
						self._p_deleteSelection()
					elif self._cursorLocation > 0:
						textDeleted = self._text[self._cursorLocation-1:self._cursorLocation]
						self._text = self._text[:self._cursorLocation-1] + self._text[self._cursorLocation:]
						self._cursorLocation -= 1
						self.textDeletedSignal.emit( self, self._cursorLocation, self._cursorLocation+1, textDeleted )
						# Event handled
						bHandled = True
					self._p_onTextModified()
			elif event.keyVal == gtk.keysyms.Delete  and  self.bEditable:
				bCanDelete = True
				if self._regexp is not None:
					text = self._p_computeTextAfterDelete()
					if not self._p_checkText( text ):
						bCanDelete = False

				if bCanDelete:
					text = self._text
					if self._selectionBounds is not None:
						self._p_deleteSelection()
					elif self._cursorLocation < len( self._text ):
						textDeleted = self._text[self._cursorLocation:self._cursorLocation+1]
						self._text = self._text[:self._cursorLocation] + self._text[self._cursorLocation+1:]
						self.textDeletedSignal.emit( self, self._cursorLocation, self._cursorLocation+1, textDeleted )
					self._p_onTextModified()
					# Event not handled if text was empty
					bHandled = len( text ) == 0
			elif event.keyString != ''  and  self.bEditable:
				bTextOk = True

				if self._regexp is not None:
					text = self._p_computeTextAfterReplacingSelection( event.keyString )
					bTextOk = self._p_checkText( text )

				if bTextOk:
					if self._selectionBounds is not None:
						self._p_deleteSelection()
					position = self._cursorLocation
					bAppended = position == len( self._text )
					self._text = self._text[:self._cursorLocation] + event.keyString + self._text[self._cursorLocation:]
					self._cursorLocation += len( event.keyString )
					self.textInsertedSignal.emit( self, position, bAppended, event.keyString )
					self._p_onTextModified()
					bHandled = True

		# Not handled; pass to the key handler, if there is one
		if not bHandled:
			if self.keyHandler is not None:
				bHandled = self.keyHandler._f_handleKeyPress( self, event )


	def _o_onKeyRelease(self, event):
		super( DTEntry, self )._o_onKeyRelease( event )



	def _o_onGainFocus(self):
		super( DTEntry, self )._o_onGainFocus()
		self._cursorLocation = len( self._text )
		self._bAutoCompleteDisabled = False
		self._o_queueFullRedraw()

	def _o_onLoseFocus(self):
		super( DTEntry, self )._o_onLoseFocus()
		self._autoCompleteDropDown.hide()
		self._o_queueFullRedraw()



	def _o_draw(self, context):
		super( DTEntry, self )._o_draw( context )
		b = self._borderWidth

		self._p_refreshLayout()


		# Background
		context.rectangle( self._entryPosition.x + b * 0.5, self._entryPosition.y + b * 0.5, self._entrySize.x - b, self._entrySize.y - b )

		# Fill
		context.set_source_rgb( self._backgroundColour.r, self._backgroundColour.g, self._backgroundColour.b )
		context.fill_preserve()

		# Border
		context.set_line_width( b )
		context.set_source_rgb( self._borderColour.r, self._borderColour.g, self._borderColour.b )
		context.stroke()

		if self._selectionBounds is not None  and  self._bHasFocus:
			self._p_refreshSelectionLayouts()
			# Text with selection
			start = min( self._selectionBounds )
			end = max( self._selectionBounds )

			preSelSize = self._preSelectionLayout.get_pixel_size()
			selSize = self._selectionLayout.get_pixel_size()

			startX = self._textRoot.x  +  preSelSize[0]
			selectionSpace = selSize[0]

			context.set_source_rgb( self._highlightedBackgroundColour.r, self._highlightedBackgroundColour.g, self._highlightedBackgroundColour.b )
			context.rectangle( startX, self._entryPosition.y + self._borderWidth, selectionSpace, self._textSize.y )
			context.fill()

			context.move_to( self._textPosition.x, self._textPosition.y )
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			context.show_layout( self._preSelectionLayout )


			context.move_to( self._textPosition.x + preSelSize[0], self._textPosition.y )
			context.set_source_rgb( self._highlightedTextColour.r, self._highlightedTextColour.g, self._highlightedTextColour.b )
			context.show_layout( self._selectionLayout )

			context.move_to( self._textPosition.x + preSelSize[0] + selSize[0], self._textPosition.y )
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			context.show_layout( self._postSelectionLayout )
		else:
			# Text without selection
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			context.move_to( self._textPosition.x, self._textPosition.y )
			context.show_layout( self._layout )

		# Cursor
		if self._bHasFocus:
			space = self._layout.index_to_pos( self._cursorLocation )[0]  /  pango.SCALE
			cursorPositionX = self._textRoot.x + space
			context.set_line_width( 1.0 )
			context.set_source_rgb( 0.0, 0.0, 0.0 )
			context.move_to( cursorPositionX, self._entryPosition.y + self._borderWidth )
			context.rel_line_to( 0.0, self._textSize.y )
			context.stroke()




	def _o_onRealise(self, context, pangoContext):
		super( DTEntry, self )._o_onRealise( context, pangoContext )
		if context is not self._layoutContext:
			self._layoutContext = context
			self._layout = self._realiseContext.create_layout()
			self._preSelectionLayout = self._realiseContext.create_layout()
			self._selectionLayout = self._realiseContext.create_layout()
			self._postSelectionLayout = self._realiseContext.create_layout()
			self._layout.set_font_description( self._fontDescription )
			self._preSelectionLayout.set_font_description( self._fontDescription )
			self._selectionLayout.set_font_description( self._fontDescription )
			self._postSelectionLayout.set_font_description( self._fontDescription )
			self._bLayoutNeedsRefresh = True
			self._bSelectionLayoutsNeedRefresh = True


	def _o_onSetScale(self, scale, rootScale):
		context = self._realiseContext
		context.save()
		context.scale( rootScale, rootScale )
		context.update_layout( self._layout )
		context.update_layout( self._preSelectionLayout )
		context.update_layout( self._selectionLayout )
		context.update_layout( self._postSelectionLayout )
		context.restore()


	def _o_getRequiredWidth(self):
		self._p_refreshTextSize()
		return self._entrySize.x

	def _o_getRequiredHeight(self):
		self._p_refreshTextSize()
		return self._entrySize.y


	def _o_onAllocateX(self, allocation):
		super( DTEntry, self )._o_onAllocateX( allocation )

	def _o_onAllocateY(self, allocation):
		super( DTEntry, self )._o_onAllocateY( allocation )
		self._entryPosition = ( self._allocation - self._entrySize )  *  0.5
		self._textRoot = self._entryPosition + Vector2( self._borderWidth + 1.0, self._borderWidth + 1.0 )
		self._textPosition = self._textRoot




	def _p_textChanged(self):
		self._textSize = None
		self._bLayoutNeedsRefresh = True
		self._bSelectionLayoutsNeedRefresh = True


	def _p_setSelectionBounds(self, bounds):
		self._selectionBounds = bounds
		self._bSelectionLayoutsNeedRefresh = True



	def _p_refreshLayout(self):
		if self._bLayoutNeedsRefresh  and  self._layout is not None:
			self._layout.set_text( self._text )
			self._bLayoutNeedsRefresh = False


	def _p_refreshSelectionLayouts(self):
		if self._bSelectionLayoutsNeedRefresh  and  self._layout is not None:
			if self._selectionBounds is None:
				self._preSelectionLayout.set_text( self._text )
				self._selectionLayout.set_text( '' )
				self._postSelectionLayout.set_text( '' )
			else:
				start = min( self._selectionBounds )
				end = max( self._selectionBounds )
				self._preSelectionLayout.set_text( self._text[:start] )
				self._selectionLayout.set_text( self._text[start:end] )
				self._postSelectionLayout.set_text( self._text[end:] )
			self._bSelectionLayoutsNeedRefresh = False



	def _p_computeTextSize(self):
		self._p_refreshLayout()
		layoutSize = self._layout.get_pixel_size()
		self._textSize = Vector2( layoutSize[0] + layoutSize[1] * 3.0  +  6.0,   layoutSize[1] + 2.0 )
		self._entrySize = self._textSize  +  Vector2( self._borderWidth * 2.0, self._borderWidth * 2.0 )
		self._o_queueResize()


	def _p_refreshTextSize(self):
		if self._textSize is None:
			self._p_computeTextSize()





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

	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	from Britefury.DocView.Toolkit.DTFont import DTFont
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
	doc.show()


	autoCompleteList = [ 'abc', 'Hello', 'Hello world', 'Hi', 'Hi world', 'Hello world 2' ]

	entry = DTEntry( 'Hello world', autoCompleteList=autoCompleteList )
	doc.child = entry
	entry.grabFocus()


	window.add( doc )
	window.show()

	gtk.main()
