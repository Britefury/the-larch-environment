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

from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation
from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocPresent.Toolkit.DTAutoCompleteDropDown import DTAutoCompleteDropDown



_modKeysMask = ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )





class DTEntry2 (DTWidget):
	returnSignal = ClassSignal()       				# ( entry )
	textInsertedSignal = ClassSignal()				# ( entry, position, bAppended, textInserted )
	textDeletedSignal = ClassSignal()				# ( entry, startIndex, endIndex, textDeleted )


	def __init__(self, text='', font=None, backgroundColour=Colour3f( 0.9, 0.95, 0.9 ), highlightedBackgroundColour=Colour3f( 0.0, 0.0, 0.5 ), textColour=Colour3f( 0.0, 0.0, 0.0 ), highlightedTextColour=Colour3f( 1.0, 1.0, 1.0 ), autoCompleteList=None):
		super( DTEntry2, self ).__init__()

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

		self._backgroundColour = backgroundColour
		self._highlightedBackgroundColour = highlightedBackgroundColour
		self._textColour = textColour
		self._highlightedTextColour = highlightedTextColour

		self._entrySize = Vector2()
		self._entryPosition = Vector2()
		self._textSize = None
		self._textPosition = Vector2()
		self._cursorIndex = 0
		self._selectionBounds = None
		self._bButtonPressed = False

		self._autoCompleteList = autoCompleteList
		self._autoCompleteDropDown = DTAutoCompleteDropDown( [], self )
		self._autoCompleteDropDown.autoCompleteSignal.connect( self._p_onAutoComplete )
		self._autoCompleteDropDown.autoCompleteDismissedSignal.connect( self._p_onAutoCompleteDismissed )
		self._bAutoCompleteDisabled = False
		
		self._cursorEntities = []
		self._endCursorEntity = DTCursorEntity( self, DTCursorEntity.EDGEFLAGS_LEADING, '<end>' )
		self._p_rebuildCursorEntityList()
		

		self._o_queueResize()



	def setText(self, text):
		if text != self._text:
			self._text = text
			self._p_rebuildCursorEntityList()
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


	def moveCursorToStart(self):
		if len( self._cursorEntities ) > 0:
			self._cursor.location = DTCursorLocation( self._cursorEntities[0], DTCursorLocation.EDGE_LEADING )
		else:
			self._cursor.location = DTCursorLocation( self._endCursorEntity, DTCursorLocation.EDGE_LEADING )
		#self._cursorIndex = 0
		self._o_queueFullRedraw()

	def moveCursorToEnd(self):
		self._cursor.location = DTCursorLocation( self._endCursorEntity, DTCursorLocation.EDGE_LEADING)
		#self._cursorIndex = len( self._text )
		self._o_queueFullRedraw()

	def setCursorIndex(self, index):
		index = min( max( index, 0 ), len( self._text ) )
		if len( self._cursorEntities ) > 0:
			if index < len( self._text ):
				self._cursor.location = DTCursorLocation( self._cursorEntities[index], DTCursorLocation.EDGE_LEADING )
			else:
				self._cursor.location = DTCursorLocation( self._cursorEntities[-1], DTCursorLocation.EDGE_TRAILING )
		else:
			self._cursor.location = DTCursorLocation( self._endCursorEntity, DTCursorLocation.EDGE_LEADING )
		#self._cursorIndex = index
		self._o_queueFullRedraw()

	def _p_internal_setCursorIndex(self, index):
		index = min( max( index, 0 ), len( self._text ) )
		if len( self._cursorEntities ) > 0:
			if index < len( self._text ):
				self._cursor.location = DTCursorLocation( self._cursorEntities[index], DTCursorLocation.EDGE_LEADING )
			else:
				self._cursor.location = DTCursorLocation( self._cursorEntities[-1], DTCursorLocation.EDGE_TRAILING )
		else:
			self._cursor.location = DTCursorLocation( self._endCursorEntity, DTCursorLocation.EDGE_LEADING )
		#self._cursorIndex = index
		self._o_queueFullRedraw()

	def getCursorIndex(self):
		return self._cursorIndex

	def isCursorAtStart(self):
		return self._cursorIndex == 0

	def isCursorAtEnd(self):
		return self._cursorIndex == len( self._text )

	def getCursorPosition(self):
		charRect = self._layout.index_to_pos( self._cursorIndex )
		layoutPoint = Point2( float( charRect[0] ) / pango.SCALE,  ( float( charRect[1] )  +  float( charRect[3] ) * 0.5 )  /  pango.SCALE )
		return layoutPoint + self._textPosition



	def getCharacterIndexAt(self, point):
		pointInLayout = point - self._textPosition
		index, trailing = self._layout.xy_to_index( int( pointInLayout.x * pango.SCALE ), int( pointInLayout.y * pango.SCALE ) )
		return index


	def getCharacterIndexAtX(self, x):
		y = self._textPosition.y + self._layout.get_pixel_size() * 0.5
		return self.getCharacterIndexAt( Point2( x, y ) )


	def getCursorIndexAt(self, point):
		pointInLayout = point - self._textPosition
		index, trailing = self._layout.xy_to_index( int( pointInLayout.x * pango.SCALE ), int( pointInLayout.y * pango.SCALE ) )
		return index + trailing


	def getCursorIndexAtX(self, x):
		y = self._textPosition.y + self._layout.get_pixel_size() * 0.5
		return self.getCursorIndexAt( Point2( x, y ) )




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




	def _p_computeTextSize(self):
		self._p_refreshLayout()
		layoutSize = self._layout.get_pixel_size()
		self._textSize = Vector2( layoutSize[0],   layoutSize[1] )
		self._entrySize = self._textSize  +  Vector2( 2.0, 2.0 )
		self._o_queueResize()


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
				self._entrySize = self._textSize  +  Vector2( 2.0, 2.0 )
				self._o_queueResize()
		self._o_queueFullRedraw()
		if not self._bAutoCompleteDisabled:
			self._p_displayAutoComplete()



	def _p_deleteSelection(self):
		start = min( self._selectionBounds )
		end = max( self._selectionBounds )
		if end > start:
			textDeleted = self._text[start:end]
			self._text = self._text[:start] + self._text[end:]
			# Update cursor entities
			# de-link cursor entities in range
			DTCursorEntity.remove( self._cursorEntities[start], self._cursorEntities[end-1] )
			# remove from list
			del self._cursorEntities[start:end]
				
			self._p_internal_setCursorIndex( start )
			self.textDeletedSignal.emit( self, start, end, textDeleted )
		self._p_setSelectionBounds( None )



	def _p_textPosToTextLocation(self, textX):
		layoutSize = self._layout.get_pixel_size()
		textY = layoutSize[1] * 0.5
		index, trailing = self._layout.xy_to_index( int( textX * pango.SCALE ), int( textY * pango.SCALE ) )
		return index + trailing



	def _p_localPosToTextLocation(self, localPos):
		return self._p_textPosToTextLocation( localPos.x - self._textPosition.x )


	def _o_onButtonDown(self, localPos, button, state):
		super( DTEntry2, self )._o_onButtonDown( localPos, button, state )
		if button == 1:
			self.grabFocus()
			self._bButtonPressed = True
			loc = self._p_localPosToTextLocation( localPos )
			self._p_moveCursor( False, loc )
		return True


	def _o_onButtonDown2(self, localPos, button, state):
		super( DTEntry2, self )._o_onButtonDown2( localPos, button, state )
		if button == 1:
			self._p_setSelectionBounds( ( 0, len( self._text ) ) )
			#self._cursorIndex = self._selectionBounds[1]
			self._o_queueFullRedraw()
		return True


	def _o_onButtonUp(self, localPos, button, state):
		super( DTEntry2, self )._o_onButtonUp( localPos, button, state )
		if button == 1:
			self._bButtonPressed = False
		return True


	def _o_onMotion(self, localPos):
		super( DTEntry2, self )._o_onMotion( localPos )
		if self._bButtonPressed:
			loc = self._p_localPosToTextLocation( localPos )
			self._p_moveCursor( True, loc )


	def _p_onAutoComplete(self, autoComplete, text):
		deletedText = self._text
		self._text = text
		self._p_setSelectionBounds( None )
		self._p_rebuildCursorEntityList()
		self.textDeletedSignal.emit( self, 0, len( deletedText ), deletedText )
		self.moveCursorToEnd()
		self.textInsertedSignal.emit( self, 0, True, text )
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
		super( DTEntry2, self )._o_onKeyPress( event )

		# Flag to determine if the key event has been handled
		bHandled = False

		modKeys = event.state & _modKeysMask
		if event.keyVal == gtk.keysyms.Return:
			self.returnSignal.emit( self )
			self.ungrabFocus()
			bHandled = True
		elif event.keyVal == gtk.keysyms.BackSpace  and  self.bEditable:
			if self._selectionBounds is not None:
				self._p_deleteSelection()
				self._p_onTextModified()
			elif self._cursorIndex > 0:
				cursorIndex = self._cursorIndex
				DTCursorEntity.remove( self._cursorEntities[cursorIndex-1], self._cursorEntities[cursorIndex-1] )
				del self._cursorEntities[cursorIndex-1]
				textDeleted = self._text[cursorIndex-1:cursorIndex]
				self._text = self._text[:cursorIndex-1] + self._text[cursorIndex:]
				self._p_internal_setCursorIndex( cursorIndex - 1 )
				self.textDeletedSignal.emit( self, cursorIndex, cursorIndex+1, textDeleted )
				self._p_onTextModified()
			else:
				# leave the entry
				self.returnSignal.emit( self )
				self.ungrabFocus()
			bHandled = True
		elif event.keyVal == gtk.keysyms.Delete  and  self.bEditable:
			text = self._text
			if self._selectionBounds is not None:
				self._p_deleteSelection()
				self._p_onTextModified()
			elif self._cursorIndex < len( self._text ):
				cursorIndex = self._cursorIndex
				DTCursorEntity.remove( self._cursorEntities[cursorIndex], self._cursorEntities[cursorIndex] )
				del self._cursorEntities[cursorIndex]
				textDeleted = self._text[cursorIndex:cursorIndex+1]
				self._text = self._text[:cursorIndex] + self._text[cursorIndex+1:]
				self._p_internal_setCursorIndex( cursorIndex )
				self.textDeletedSignal.emit( self, cursorIndex, cursorIndex+1, textDeleted )
				self._p_onTextModified()
			else:
				# leave the entry
				self.returnSignal.emit( self )
				self.ungrabFocus()
			bHandled = True
		elif event.keyString != ''  and  ( modKeys == 0  or  modKeys == gtk.gdk.SHIFT_MASK )  and  self.bEditable:
			if self._selectionBounds is not None:
				self._p_deleteSelection()
			position = self._cursorIndex
			bAppended = position == len( self._text )

			keyStringCursorEntities = [ DTCursorEntity( self )   for character in event.keyString ]
			DTCursorEntity.buildListLinks( keyStringCursorEntities )
			
			if len( self._cursorEntities ) == 0:
				prev = self.getPrevCursorEntity()
				next = self._endCursorEntity
			else:
				if self._cursorIndex > 0:
					prev = self._cursorEntities[self._cursorIndex-1]
				else:
					prev = self._cursorEntities[0].prev

				if self._cursorIndex < len( self._text ):
					next = self._cursorEntities[self._cursorIndex]
				else:
					next = self._cursorEntities[-1].next

			DTCursorEntity.splice( prev, next, keyStringCursorEntities[0], keyStringCursorEntities[-1] )
			self._cursorEntities[self._cursorIndex:self._cursorIndex] = keyStringCursorEntities
			
			self._text = self._text[:self._cursorIndex] + event.keyString + self._text[self._cursorIndex:]
			self._p_internal_setCursorIndex( self._cursorIndex  +  len( event.keyString ) )

			self.textInsertedSignal.emit( self, position, bAppended, event.keyString )
			self._p_onTextModified()
			bHandled = True

		# Not handled; pass to the key handler, if there is one
		if not bHandled:
			if self.keyHandler is not None:
				bHandled = self.keyHandler( self, event )


	def _o_onKeyRelease(self, event):
		super( DTEntry2, self )._o_onKeyRelease( event )



	def _o_onCursorEnter(self, cursor):
		super( DTEntry2, self )._o_onCursorEnter( cursor )
		self._o_refreshCursorIndex( cursor, self.CURSORMOVEMENT_MOVE )
		self._bAutoCompleteDisabled = False

	def _o_onCursorLeave(self):
		super( DTEntry2, self )._o_onCursorLeave()
		self._autoCompleteDropDown.hide()
		self._p_computeTextSize()
		self._o_queueFullRedraw()

	def _o_onCursorMotion(self, cursor, mode):
		super( DTEntry2, self )._o_onCursorMotion( cursor, mode )
		self._o_refreshCursorIndex( cursor, mode )
		
		
	def _o_refreshCursorIndex(self, cursor, mode):
		loc = cursor.location
		try:
			cursorIndex = self._cursorEntities.index( loc.cursorEntity )
		except ValueError:
			cursorIndex = len( self._text )
		else:
			if loc.edge == DTCursorLocation.EDGE_TRAILING:
				cursorIndex += 1
				
		self._p_moveCursor( mode == self.CURSORMOVEMENT_DRAG, cursorIndex )
			
		self._o_queueFullRedraw()

	
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


	def _o_draw(self, context):
		super( DTEntry2, self )._o_draw( context )

		self._o_clipIfAllocationInsufficient( context )

		self._p_refreshLayout()


		# Background
		context.rectangle( self._entryPosition.x, self._entryPosition.y, self._entrySize.x, self._entrySize.y )

		# Fill
		context.set_source_rgb( self._backgroundColour.r, self._backgroundColour.g, self._backgroundColour.b )
		context.fill()

		if self._selectionBounds is not None  and  self._cursor is not None:
			self._p_refreshSelectionLayouts()
			# Text with selection
			start = min( self._selectionBounds )
			end = max( self._selectionBounds )

			preSelSize = self._preSelectionLayout.get_pixel_size()
			selSize = self._selectionLayout.get_pixel_size()

			startX = self._textPosition.x  +  preSelSize[0]
			selectionSpace = selSize[0]

			context.set_source_rgb( self._highlightedBackgroundColour.r, self._highlightedBackgroundColour.g, self._highlightedBackgroundColour.b )
			context.rectangle( startX, self._entryPosition.y, selectionSpace, self._textSize.y )
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




	def _o_onRealise(self, context, pangoContext):
		super( DTEntry2, self )._o_onRealise( context, pangoContext )
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

	def _o_getRequiredHeightAndBaseline(self):
		self._p_refreshTextSize()
		return self._entrySize.y,  0.0


	def _o_onAllocateX(self, allocation):
		super( DTEntry2, self )._o_onAllocateX( allocation )

	def _o_onAllocateY(self, allocation):
		super( DTEntry2, self )._o_onAllocateY( allocation )
		self._entryPosition = ( self._allocation - self._entrySize )  *  0.5
		self._textPosition = self._entryPosition  +  Vector2( 1.0, 1.0 )




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



	def _p_refreshTextSize(self):
		if self._textSize is None:
			self._p_computeTextSize()



	#
	# CURSOR ENTITY METHODS
	#
	
	def _o_getFirstCursorEntity(self):
		if len( self._cursorEntities ) > 0:
			return self._cursorEntities[0]
		else:
			return self._endCursorEntity


	def _o_getLastCursorEntity(self):
		return self._endCursorEntity


	def _p_rebuildCursorEntityList(self):
		def _fixCursorIndexAndEdge(cie):
			cursor, index, edge = cie
			if index >= len( self._cursorEntities ):
				index = len( self._cursorEntities ) - 1
				edge = DTCursorLocation.EDGE_TRAILING
			return cursor, index, edge

		cursorsIndicesAndEdges = [ ( cursor, self._cursorEntities.index( cursor.location.cursorEntity ), cursor.location.edge )   for cursor in self._cursors.keys() ]
		
		self._cursorEntities = [ DTCursorEntity( self, identity=character )   for character in self._text ]
		DTCursorEntity.buildListLinks( self._cursorEntities )
		if len( self._cursorEntities ) > 0:
			self._cursorEntities[-1].next = self._endCursorEntity
		
		cursorsIndicesAndEdges = [ _fixCursorIndexAndEdge( cie )   for cie in cursorsIndicesAndEdges ]
		
		for cursor, index, edge in cursorsIndicesAndEdges:
			cursor._f_widgetNotifyOfLocationChange( DTCursorLocation( self._cursorEntities[index], edge ) )

	

		
		
		
	#
	# CURSOR POSITIONING METHODS
	#
	
	def getCursorSegment(self, cursorLocation):
		try:
			cursorIndex = self._cursorEntities.index( cursorLocation.cursorEntity )
		except ValueError:
			if cursorLocation.cursorEntity is self._endCursorEntity:
				cursorIndex = len( self._text )
			else:
				raise ValueError, 'cursor entity not in this widget'
		
		if cursorLocation.cursorEntity is not self._endCursorEntity  and  cursorLocation.edge == DTCursorLocation.EDGE_TRAILING:
			cursorIndex += 1
		
		space = self._layout.index_to_pos( cursorIndex )[0]  /  pango.SCALE
		cursorPositionX = self._textPosition.x + space

		pos = Point2( cursorPositionX, self._entryPosition.y )
		return Segment2( pos, pos + Vector2( 0.0, self._entrySize.y ) )

	
	def _o_getCursorLocationAtPosition(self, localPosition):
		pointInLayout = localPosition - self._textPosition
		index, trailing = self._layout.xy_to_index( int( pointInLayout.x * pango.SCALE ), int( pointInLayout.y * pango.SCALE ) )
		
		if trailing == 0:
			return DTCursorLocation( self._cursorEntities[index], DTCursorLocation.EDGE_LEADING )
		else:
			return DTCursorLocation( self._cursorEntities[index], DTCursorLocation.EDGE_TRAILING )

		
		
		
		
	#
	# FOCUS NAVIGATION METHODS
	#
	
	def _o_isFocusTarget(self):
		return True

	
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
		self.returnSignal.emit( self )
		self.ungrabFocus()

	
	
	#
	# DEBUG __repr__
	#
	
	def __repr__(self):
		return 'DTEntry2[%d](\'%s\')'  %  ( id(self), self._text )

	def __str__(self):
		return 'DTEntry2[%d](\'%s\')'  %  ( id(self), self._text )

	
	



	text = property( getText, setText )
	font = property( getFont, setFont )
	backgroundColour = property( getBackgroundColour, setBackgroundColour )
	highlightedBackgroundColour = property( getHighlightedBackgroundColour, setHighlightedBackgroundColour )
	textColour = property( getTextColour, setTextColour )
	highlightedTextColour = property( getHighlightedTextColour, setHighlightedTextColour )
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
	
	def _makeEntry(i):
		autoCompleteList = [ 'abc', 'Hello', 'Hello world', 'Hi', 'Hi world', 'Hello world 2' ]
		entry = DTEntry2( 'x%dy'  %  ( i, ), autoCompleteList=autoCompleteList )
		return entry
	
	box = DTBox( spacing=10.0 )
	box.extend( [ _makeEntry( i )   for i in xrange( 0, 5 ) ] )
	doc.child = box
	#entry.grabFocus()


	window.add( doc.getGtkWidget() )
	window.show()

	gtk.main()
