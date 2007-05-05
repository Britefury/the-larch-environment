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

import cairo

from Britefury.Math.Math import Colour3f, Vector2

from Britefury.Util.SignalSlot import *

from Britefury.DocView.Toolkit.DTWidget import DTWidget
from Britefury.DocView.Toolkit.DTFont import DTFont



class DTEntry (DTWidget):
	returnSignal = ClassSignal()
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )


	def __init__(self, text='', font=None, borderWidth=2.0, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), borderColour=Colour3f( 0.6, 0.8, 0.6 )):
		super( DTEntry, self ).__init__()

		if font is None:
			font = DTFont()

		self.keyHandler = None
		self.allowableCharacters = None
		self.bEditable = True
		self._text = text
		self._font = font
		self._font.changedSignal.connect( self._p_onFontChanged )
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

		self._o_queueResize()



	def setText(self, text):
		if text != self._text:
			self._text = text
			self._textSize = None
			if self._bHasFocus:
				self._cursorLocation = len( self._text )
			self._o_queueResize()

	def getText(self):
		return self._text


	def setFont(self, font):
		if self._font is not None:
			self._font.changedSignal.disconnect( self._p_onFontChanged )
		self._font = font
		if self._font is not None:
			self._font.changedSignal.connect( self._p_onFontChanged )
		self._textSize = None
		self._o_queueResize()

	def getFont(self):
		return self._font


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



	def _p_computeTextSize(self):
		self._font.select( self._realiseContext )
		x = self._font.getTextSpace( self._realiseContext, self._text ).x + 2.0
		y = self._font.getFontSpace( self._realiseContext ) + 2.0
		self._textSize = Vector2( x + y * 3.0,  y )
		self._entrySize = self._textSize  +  Vector2( self._borderWidth * 2.0, self._borderWidth * 2.0 )
		self._o_queueResize()


	def _p_refreshTextSize(self):
		if self._textSize is None:
			self._p_computeTextSize()


	def _p_onTextModified(self):
		if self._textSize is None:
			self._p_computeTextSize()
		else:
			self._font.select( self._realiseContext )
			x = self._font.getTextSpace( self._realiseContext, self._text ).x + 2.0
			y = self._font.getFontSpace( self._realiseContext ) + 2.0
			bufferZoneWidth = y * 3.0
			if x > self._textSize.x  or  x < ( self._textSize.x - ( bufferZoneWidth * 2.0 ) ):
				self._textSize = Vector2( x + bufferZoneWidth,  y )
				self._entrySize = self._textSize  +  Vector2( self._borderWidth * 2.0, self._borderWidth * 2.0 )
				self._o_queueResize()
		self._o_queueFullRedraw()



	def _p_moveCursor(self, bDragSelection, location):
		if bDragSelection:
			# Extend the selection
			if self._selectionBounds is None:
				self._selectionBounds = self._cursorLocation, location
			else:
				first, second = self._selectionBounds
				if first != location:
					self._selectionBounds = first, location
				else:
					self._selectionBounds = None
		else:
			# Clear the selection
			self._selectionBounds = None
		self._cursorLocation = location
		self._o_queueFullRedraw()


	def _p_deleteSelection(self):
		start = min( self._selectionBounds )
		end = max( self._selectionBounds )
		textDeleted = self._text[start:end]
		self._text = self._text[:start] + self._text[end:]
		self._cursorLocation = start
		self.textDeletedSignal.emit( self, start, end, textDeleted )
		self._selectionBounds = None



	def _p_textPosToTextLocation(self, textX):
		lowerX = 0.0
		upperX = self._font.getTextSpace( self._realiseContext, self._text ).x
		lowerLoc = 0
		upperLoc = len( self._text )
		while upperLoc  >  ( lowerLoc + 1 ):
			midLoc = ( lowerLoc + upperLoc ) >> 1
			midX = self._font.getTextSpace( self._realiseContext, self._text[:midLoc] ).x
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
			self._selectionBounds = 0, len( self._text )
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


	def _o_onKeyPress(self, event):
		super( DTEntry, self )._o_onKeyPress( event )
		bHandled = False
		if self.keyHandler is not None:
			bHandled = self.keyHandler._f_handleKeyPress( self, event )
		if not bHandled:
			if event.keyVal == gtk.keysyms.Left:
				self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, max( self._cursorLocation - 1, 0 ) )
			elif event.keyVal == gtk.keysyms.Right:
				self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, min( self._cursorLocation + 1, len( self._text ) ) )
			elif event.keyVal == gtk.keysyms.Home:
				self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, 0 )
			elif event.keyVal == gtk.keysyms.End:
				self._p_moveCursor( ( event.state & gtk.gdk.SHIFT_MASK ) != 0, len( self._text ) )
			elif event.keyVal == gtk.keysyms.Return:
				self.returnSignal.emit()
				self.ungrabFocus()
			elif self.bEditable:
				if event.keyVal == gtk.keysyms.BackSpace:
					if self._selectionBounds is not None:
						self._p_deleteSelection()
					elif self._cursorLocation > 0:
						textDeleted = self._text[self._cursorLocation-1:self._cursorLocation]
						self._text = self._text[:self._cursorLocation-1] + self._text[self._cursorLocation:]
						self._cursorLocation -= 1
						self.textDeletedSignal.emit( self, self._cursorLocation, self._cursorLocation+1, textDeleted )
					self._p_onTextModified()
				elif event.keyVal == gtk.keysyms.Delete:
					if self._selectionBounds is not None:
						self._p_deleteSelection()
					elif self._cursorLocation < len( self._text ):
						textDeleted = self._text[self._cursorLocation:self._cursorLocation+1]
						self._text = self._text[:self._cursorLocation] + self._text[self._cursorLocation+1:]
						self.textDeletedSignal.emit( self, self._cursorLocation, self._cursorLocation+1, textDeleted )
					self._p_onTextModified()
				elif event.keyString != '':
					if self.allowableCharacters is None  or  event.keyString in self.allowableCharacters:
						if self._selectionBounds is not None:
							self._p_deleteSelection()
						position = self._cursorLocation
						bAppended = position == len( self._text )
						self._text = self._text[:self._cursorLocation] + event.keyString + self._text[self._cursorLocation:]
						self._cursorLocation += len( event.keyString )
						self.textInsertedSignal.emit( self, position, bAppended, event.keyString )
						self._p_onTextModified()

	def _o_onKeyRelease(self, event):
		super( DTEntry, self )._o_onKeyRelease( event )



	def _o_onGainFocus(self):
		super( DTEntry, self )._o_onGainFocus()
		self._cursorLocation = len( self._text )
		self._o_queueFullRedraw()

	def _o_onLoseFocus(self):
		super( DTEntry, self )._o_onLoseFocus()
		self._o_queueFullRedraw()



	def _o_draw(self, context):
		super( DTEntry, self )._o_draw( context )
		b = self._borderWidth

		# Background
		context.rectangle( self._entryPosition.x + b * 0.5, self._entryPosition.y + b * 0.5, self._entrySize.x - b, self._entrySize.y - b )

		# Fill
		context.set_source_rgb( self._backgroundColour.r, self._backgroundColour.g, self._backgroundColour.b )
		context.fill_preserve()

		# Border
		context.set_line_width( b )
		context.set_source_rgb( self._borderColour.r, self._borderColour.g, self._borderColour.b )
		context.stroke()

		# Text with selection
		self._font.select( context )
		if self._selectionBounds is not None  and  self._bHasFocus:
			start = min( self._selectionBounds )
			end = max( self._selectionBounds )
			space = self._font.getTextSpace( self._realiseContext, self._text[:start] )
			startX = self._textRoot.x + space.x
			selectionSpace = self._font.getTextSpace( self._realiseContext, self._text[start:end] )
			context.set_source_rgb( self._highlightedBackgroundColour.r, self._highlightedBackgroundColour.g, self._highlightedBackgroundColour.b )
			context.rectangle( startX, self._entryPosition.y + self._borderWidth, selectionSpace.x, self._textSize.y )
			context.fill()

			context.move_to( self._textPosition.x, self._textPosition.y )
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			self._font.showText( context, self._text[:start] )
			context.set_source_rgb( self._highlightedTextColour.r, self._highlightedTextColour.g, self._highlightedTextColour.b )
			self._font.showText( context, self._text[start:end] )
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			self._font.showText( context, self._text[end:] )
		else:
			# Text without selection
			context.set_source_rgb( self._textColour.r, self._textColour.g, self._textColour.b )
			context.move_to( self._textPosition.x, self._textPosition.y )
			self._font.showText( context, self._text )

		# Cursor
		if self._bHasFocus:
			space = self._font.getTextSpace( self._realiseContext, self._text[:self._cursorLocation] )
			cursorPositionX = self._textRoot.x + space.x
			context.set_line_width( 1.0 )
			context.set_source_rgb( 0.0, 0.0, 0.0 )
			context.move_to( cursorPositionX, self._entryPosition.y + self._borderWidth )
			context.rel_line_to( 0.0, self._textSize.y )
			context.stroke()


	def _o_getRequiredWidth(self):
		self._p_refreshTextSize()
		return self._entrySize.x

	def _o_getRequiredHeight(self):
		self._font.select( self._realiseContext )
		return self._entrySize.y


	def _o_onAllocateX(self, allocation):
		super( DTEntry, self )._o_onAllocateX( allocation )

	def _o_onAllocateY(self, allocation):
		super( DTEntry, self )._o_onAllocateY( allocation )
		self._entryPosition = ( self._allocation - self._entrySize )  *  0.5
		self._textRoot = self._entryPosition + Vector2( self._borderWidth + 1.0, self._borderWidth + 1.0 )
		self._font.select( self._realiseContext )
		bearing, size, advance = self._font.getTextExtents( self._realiseContext, self._text )
		ascent, descent, height, maxAdvance = self._font.getFontExtents( self._realiseContext )
		self._textPosition = self._textRoot  +  Vector2( -bearing.x, ascent )


	def _p_onFontChanged(self):
		self._textSize = None
		self._o_queueResize()



	text = property( getText, setText )
	font = property( getFont, setFont )
	borderWidth = property( getBorderWidth, setBorderWidth )
	backgroundColour = property( getBackgroundColour, setBackgroundColour )
	highlightedBackgroundColour = property( getHighlightedBackgroundColour, setHighlightedBackgroundColour )
	textColour = property( getTextColour, setTextColour )
	highlightedTextColour = property( getHighlightedTextColour, setHighlightedTextColour )
	borderColour = property( getBorderColour, setBorderColour )







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

	entry = DTEntry( 'Hello world' )
	doc.child = entry
	entry.grabFocus()


	window.add( doc )
	window.show()

	gtk.main()
