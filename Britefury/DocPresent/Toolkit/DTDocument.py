##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref

import math

import pygtk
pygtk.require( '2.0' )
import gtk
import gobject
import cairo

from copy import copy

from Britefury.Util.SignalSlot import *

from Britefury.Event.QueuedEvent import queueEvent

from Britefury.Math.Math import Vector2, Point2, BBox2, Segment2

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation, DTCursor
from Britefury.DocPresent.Toolkit.DTKeyEvent import DTKeyEvent
from Britefury.DocPresent.Toolkit.DTBin import DTBin



_undoAccel = gtk.accelerator_parse( '<control>z' )
_redoAccel = gtk.accelerator_parse( '<control><shift>Z' )


_CURSOR_HEIGHT = 11.0




class _GtkPresentationArea (gtk.DrawingArea):
	"""This helper class is declared, since the do_key_press_event() method of a gtk.DrawingArea must be overridden in order to receive keyboard events.
	We cannot derive our main class from gtk.DrawingArea, otherwise the DTWidget and gtk.DrawingArea 'parent' attributed will collide, resulting in problems.
	This help class overrides the do_key_press_event() method and passes its arguments along."""
	def __init__(self, presentationArea):
		gtk.DrawingArea.__init__( self )
		self._presentationArea = presentationArea
		
	def do_key_press_event(self, event):
		self._presentationArea.do_key_press_event( event )
		
gobject.type_register( _GtkPresentationArea )



class DTDocument (DTBin):
	undoSignal = ClassSignal()
	redoSignal = ClassSignal()

	def __init__(self, bCanGrabFocus=True):
		DTBin.__init__( self )
		
		
		self._drawingArea = _GtkPresentationArea( self )


		# Set the document to self
		self._document = self

		self._dndSource = None
		self._dndCache = {}
		self._dndButton = None
		self._dndInProgress = False
		self._dndBeginData = None


		self._docOffset = Vector2()
		self._docScale = 1.0
		self._docDragStartPos = Point2()
		self._docDragButton = None

		self._documentSize = Vector2()
		self._bAllocationRequired = False
		self._keyboardFocusChild = None
		self._keyboardFocusGrabChild = None

		# Immediate event queue
		self._immediateEvents = []
		
		# Cursor entities for the start and end of the document
		self._firstCursorEntity = DTCursorEntity( self )
		self._lastCursorEntity = DTCursorEntity( self )
		self._firstCursorEntity.next = self._lastCursorEntity
		
		# Cursor management
		self._mainCursor = DTCursor( self, DTCursorLocation( self._firstCursorEntity, DTCursorLocation.EDGE_TRAILING ) )
		self._prevCursor = None
		self._nextCursor = None
		self._oldCursorSegment = None
		self._cursorSegment = None
		self._bCursorRequiresUpdate = False
		self._bCursorSegmentRequiresUpdate = True

		# Connect signals
		self._drawingArea.connect_after( 'configure-event', self._p_configureEvent )
		self._drawingArea.connect( 'expose-event', self._p_exposeEvent )
		self._drawingArea.connect( 'button-press-event', self._p_buttonPressEvent )
		self._drawingArea.connect( 'button-release-event', self._p_buttonReleaseEvent )
		self._drawingArea.connect( 'motion-notify-event', self._p_motionNotifyEvent )
		self._drawingArea.connect( 'enter-notify-event', self._p_enterNotifyEvent )
		self._drawingArea.connect( 'leave-notify-event', self._p_leaveNotifyEvent )
		self._drawingArea.connect( 'scroll-event', self._p_scrollEvent )
		self._drawingArea.connect_after( 'realize', self._p_realiseEvent )
		self._drawingArea.connect( 'unrealize', self._p_unrealiseEvent )

		# Tell the widget to send these events
		self._drawingArea.add_events( gtk.gdk.EXPOSURE_MASK |
					      gtk.gdk.BUTTON_PRESS_MASK |
					      gtk.gdk.BUTTON_RELEASE_MASK |
					      gtk.gdk.POINTER_MOTION_MASK |
					      gtk.gdk.POINTER_MOTION_HINT_MASK |
					      gtk.gdk.ENTER_NOTIFY_MASK |
					      gtk.gdk.LEAVE_NOTIFY_MASK |
					      gtk.gdk.SCROLL_MASK |
					      gtk.gdk.KEY_PRESS_MASK |
					      gtk.gdk.KEY_RELEASE_MASK )

		if bCanGrabFocus:
			self._drawingArea.set_flags( gtk.CAN_FOCUS )

			
	def getGtkWidget(self):
		return self._drawingArea


	def oneToOne(self):
		# We want to scale about the centre of the document, not the top left corner
		centre = self._documentSize * 0.5
		centreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )
		self._docScale = 1.0
		newCentreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )

		self._docOffset += ( newCentreInDocSpace - centreInDocSpace ) * self._docScale

		self.childScale = self._docScale
		self._o_queueResize()

	def reset(self):
		self._docOffset = Vector2()
		self._docScale = 1.0
		self.childScale = self._docScale
		self._o_queueResize()



	def queueImmediateEvent(self, f):
		self._immediateEvents.append( f )


	def _p_emitImmediateEvents(self):
		events = copy( self._immediateEvents )
		for event in events:
			event()
		self._immediateEvents = []





	def _o_queueRedraw(self, localPos, localSize):
		self._p_invalidateRect( localPos, localSize )

	def _p_invalidateRect(self, pos, size):
		self._drawingArea.queue_draw_area( int( pos.x ), int( pos.y ), int( math.ceil( size.x ) ), int( math.ceil( size.y ) ) )


	def _o_queueResize(self):
		self._bAllocationRequired = True
		self._drawingArea.queue_draw()




	def _p_performAllocation(self):
		if self._bAllocationRequired:
			self._f_setScale( self._docScale, self._docScale )
			reqWidth = self._f_getRequisitionWidth()
			self._f_allocateX( self._documentSize.x * self._docScale )
			reqHeight = self._f_getRequisitionHeight()
			yAlloc = max( self._documentSize.y * self._docScale, reqHeight )
			self._f_allocateY( yAlloc )
			self._bAllocationRequired = False


	def _o_onAllocateX(self, allocation):
		self._o_allocateChildX( self._child, self._docOffset.x, allocation )

	def _o_onAllocateY(self, allocation):
		self._o_allocateChildY( self._child, self._docOffset.y, allocation )



	#def _f_widgetGrabFocus(self, child):
		#if child is not self._keyboardFocusChild:
			#if self._keyboardFocusGrabChild is not None  and  child is not None  and  child is not self._keyboardFocusGrabChild:
				#self._keyboardFocusGrabChild._f_clearFocusGrab()
			#if self._keyboardFocusChild is not None  and  child is not None:
				#self._keyboardFocusChild._o_onLoseFocus()
			#self._keyboardFocusChild = child
			#if self._keyboardFocusChild is not None:
				#self._keyboardFocusChild._o_onGainFocus()
				#self._keyboardFocusGrabChild = child



	#def _f_widgetUngrabFocus(self, child):
		#if child is self._keyboardFocusChild:
			#if self._keyboardFocusChild is not None:
				#self._keyboardFocusChild._o_onLoseFocus()
			#self._keyboardFocusChild = None




	def _f_widgetGrabFocus(self, child):
		assert child is not None

		# If there is already a widget that has grabbed the keyboard focus, then clear its grab, and replace it with @child
		if child is not self._keyboardFocusGrabChild:
			if self._keyboardFocusGrabChild is not None:
				self._keyboardFocusGrabChild._f_clearFocusGrab()
			self._keyboardFocusGrabChild = child

		# If @child is different from the widget that has focus at the moment, switch
		if child is not self._keyboardFocusChild:
			if self._keyboardFocusChild is not None:
				keyboardFocusChild = self._keyboardFocusChild
				self._keyboardFocusChild = None
				keyboardFocusChild._o_onLoseFocus()
			self._keyboardFocusChild = child
			self._keyboardFocusChild._o_onGainFocus()



	def _f_widgetUngrabFocus(self, child):
		assert child is not None

		if child is self._keyboardFocusGrabChild:
			self._keyboardFocusGrabChild = None

		if child is self._keyboardFocusChild:
			keyboardFocusChild = self._keyboardFocusChild
			self._keyboardFocusChild = None
			keyboardFocusChild._o_onLoseFocus()





	def _f_widgetAcquireFocus(self, child):
		assert child is not None

		# If there is already a widget that has grabbed the keyboard focus, then clear the focus grab on @child
		if child is not self._keyboardFocusGrabChild  and  self_f_clearFocusGrab._keyboardFocusGrabChild is not None:
			child._f_clearFocusGrab()
		else:
			# If @child is different from the widget that has focus at the moment, switch
			if child is not self._keyboardFocusChild:
				if self._keyboardFocusChild is not None:
					keyboardFocusChild = self._keyboardFocusChild
					self._keyboardFocusChild = None
					keyboardFocusChild._o_onLoseFocus()
				self._keyboardFocusChild = child
				self._keyboardFocusChild._o_onGainFocus()



	def _f_widgetRelinquishFocus(self, child):
		assert child is not None

		if child is self._keyboardFocusChild:
			keyboardFocusChild = self._keyboardFocusChild
			self._keyboardFocusChild = None
			keyboardFocusChild._o_onLoseFocus()





	def removeFocusGrab(self):
		if self._keyboardFocusChild is not None:
			self._keyboardFocusChild.ungrabFocus()




	def _f_setParent(self, parent, document):
		pass


	def _f_unparent(self):
		pass





	def _p_configureEvent(self, widget, event):
		docSize = Vector2( event.width, event.height )
		if docSize != self._documentSize:
			self._documentSize = docSize
			self._bAllocationRequired = True
		self._p_emitImmediateEvents()


	def _p_exposeEvent(self, widget, event):
		self._p_performAllocation()
		context = widget.window.cairo_create()
		context.rectangle( event.area.x, event.area.y, event.area.width, event.area.height )
		context.clip_preserve()
		context.set_source_rgb( 1.0, 1.0, 1.0 )
		context.fill()
		context.new_path()
		self._f_draw( context, BBox2( Point2( event.area.x, event.area.y ), Point2( event.area.x + event.area.width, event.area.y + event.area.height ) ) )
		self._p_drawCursor()
		self._p_emitImmediateEvents()
		return False


	def _p_buttonPressEvent(self, widget, event):
		self._drawingArea.grab_focus()
		x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )
		if event.state & gtk.gdk.MOD1_MASK  ==  0:
			if event.type == gtk.gdk.BUTTON_PRESS  and  self._dndSource is None:
				self._dndSource = self._f_evDndButtonDown( localPos, event.button, state )
				self._dndCache = {}
				self._dndButton = event.button
				self._dndInProgress = False
				self._dndBeginData = None

			if event.type == gtk.gdk.BUTTON_PRESS:
				self._f_evButtonDown( localPos, event.button, state )
			elif event.type == gtk.gdk._2BUTTON_PRESS:
				self._f_evButtonDown2( localPos, event.button, state )
			elif event.type == gtk.gdk._3BUTTON_PRESS:
				self._f_evButtonDown3( localPos, event.button, state )
		else:
			if self._docDragButton is None:
				self._docDragButton = event.button
				self._docDragStartPos = localPos
		self._p_emitImmediateEvents()


	def _p_buttonReleaseEvent(self, widget, event):
		x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )
		if self._dndSource is not None  and  self._dndInProgress  and  self._dndButton == event.button:
			# Ensure that @self._dndSource is still part of this document
			if self._dndSource.document is self:
				self._f_evDndButtonUp( localPos, event.button, state, self._dndSource, self._dndBeginData )
			self._dndSource = None
			self._dndCache = {}
			self._dndButton = None
			self._dndInProgress = False
			self._dndBeginData = None
			self._drawingArea.window.set_cursor( None )

		if self._docDragButton is None:
			self._f_evButtonUp( localPos, event.button, state )
		else:
			self._docDragButton = None
		self._p_emitImmediateEvents()



	def _p_motionNotifyEvent(self, widget, event):
		if event.is_hint:
			x, y, state = event.window.get_pointer()
		else:
			x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )

		if self._docDragButton is None:
			if self._dndSource is not None:
				if not self._dndInProgress:
					self._dndBeginData = self._dndSource._f_evDndBegin()
					self._dndInProgress = True
					self._drawingArea.window.set_cursor( gtk.gdk.Cursor( gtk.gdk.HAND2 ) )
				self._f_evDndMotion( localPos, self._dndButton, state, self._dndSource, self._dndBeginData, self._dndCache )
			else:
				self._f_evMotion( localPos )
		else:
			delta = localPos - self._docDragStartPos
			bModified = False
			if self._docDragButton == 1  or  self._docDragButton == 2:
				self._docOffset += delta
				bModified = True
			elif self._docDragButton == 3:
				scaleDeltaPixels = delta.x + delta.y
				scaleDelta = 2.0  **  ( scaleDeltaPixels / 200.0 )

				# We want to scale about the centre of the document, not the top left corner
				centre = self._documentSize * 0.5
				centreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )
				self._docScale *= scaleDelta
				newCentreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )

				self._docOffset += ( newCentreInDocSpace - centreInDocSpace ) * self._docScale


				self.childScale = self._docScale
				bModified = True
			self._docDragStartPos = localPos
			if bModified:
				self._o_queueResize()
		self._p_emitImmediateEvents()


	def _p_enterNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		localPos = Point2( x, y )
		if self._docDragButton is None:
			self._f_evEnter( localPos )
		self._p_emitImmediateEvents()


	def _p_leaveNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		localPos = Point2( x, y )
		if self._docDragButton is None:
			self._f_evLeave( localPos )
		self._p_emitImmediateEvents()


	def _p_scrollEvent(self, widget, event):
		if event.state  &  ( gtk.gdk.MOD1_MASK | gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK )  !=  0:
			if event.direction == gtk.gdk.SCROLL_UP:
				scroll = Vector2( 0.0, -1.0 )
			elif event.direction == gtk.gdk.SCROLL_DOWN:
				scroll = Vector2( 0.0, 1.0 )
			elif event.direction == gtk.gdk.SCROLL_LEFT:
				scroll = Vector2( -1.0, 0.0 )
			elif event.direction == gtk.gdk.SCROLL_RIGHT:
				scroll = Vector2( 1.0, 0.0 )
			self._f_evScroll( scroll )
			self._p_emitImmediateEvents()
		else:
			if event.direction == gtk.gdk.SCROLL_UP:
				delta = 1.0
			elif event.direction == gtk.gdk.SCROLL_DOWN:
				delta = -1.0

			scaleDelta = 2.0  **  ( delta / 1.5 )

			# We want to scale about the pointer position, not the top left corner
			centre = Vector2( event.x, event.y )
			centreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )
			self._docScale *= scaleDelta
			newCentreInDocSpace = ( centre - self._docOffset )  *  ( 1.0 / self._docScale )

			self._docOffset += ( newCentreInDocSpace - centreInDocSpace ) * self._docScale


			self.childScale = self._docScale
			bModified = True
			self._o_queueResize()




	def _o_handleDocumentKey(self, keyEvent):
		return False



	def do_key_press_event(self, event):
		keyEvent = DTKeyEvent( event )
		key = keyEvent.keyVal, keyEvent.state
		if key == _undoAccel:
			self.undoSignal.emit( self )
			self._p_emitImmediateEvents()
			return True
		elif key == _redoAccel:
			self.redoSignal.emit( self )
			self._p_emitImmediateEvents()
			return True
		elif self._o_handleDocumentKey( keyEvent ):
			self._p_emitImmediateEvents()
			return True
		else:
			if self._keyboardFocusChild is not None:
				self._keyboardFocusChild._o_onKeyPress( keyEvent )
				self._p_emitImmediateEvents()
				return True
			else:
				self._p_emitImmediateEvents()
				return False


	def do_key_release_event(self, event):
		keyEvent = DTKeyEvent( event )
		key = keyEvent.keyVal, keyEvent.state
		if key == _undoAccel  or  key == _redoAccel:
			self._p_emitImmediateEvents()
			return True
		else:
			if self._keyboardFocusChild is not None:
				self._keyboardFocusChild._o_onKeyRelease( keyEvent )
				self._p_emitImmediateEvents()
				return True
			else:
				self._p_emitImmediateEvents()
				return False



	def _p_realiseEvent(self, widget):
		context = widget.window.cairo_create()
		pangoContext = widget.get_pango_context()
		self._f_evRealise( context, pangoContext )
		self._p_emitImmediateEvents()

	def _p_unrealiseEvent(self, widget):
		self._f_evUnrealise()
		self._p_emitImmediateEvents()
		
		
	
		
	
	#
	# CURSOR ENTITY METHODS
	#
	
	def _o_getFirstCursorEntity(self):
		return self._firstCursorEntity

	def _o_getLastCursorEntity(self):
		return self._lastCursorEntity

	def _f_getPrevCursorEntityBeforeChild(self, child):
		assert child is self._child
		return self._firstCursorEntity
		
	def _f_getNextCursorEntityAfterChild(self, child):
		assert child is self._child
		return self._lastCursorEntity



	#
	# CURSOR NOTIFICATION METHODS
	#

	def _f_cursorLocationNotify(self, cursor, bCurrent):
		if cursor is self._mainCursor  or  cursor is self._prevCursor  or  cursor is self._nextCursor:
			self._p_cursorSegmentChanged()
			
	def _f_cursorUnrealiseNotify(self, cursor):
		if cursor is not self._firstCursorEntity  and  cursor is not self._lastCursorEntity:
			widget = cursor.location.cursorEntity.widget
			parent = widget.parent
			assert parent is not None
	
			if cursor is self._mainCursor:
				self._prevCursor = parent._f_getPrevCursorEntityBeforeChild( widget )
				self._nextCursor = parent._f_getNextCursorEntityBeforeChild( widget )
				self._p_mainCursorChanged()
			elif cursor is self._prevCursor:
				self._prevCursor = parent._f_getPrevCursorEntityBeforeChild( widget )
			elif cursor is self._nextCursor:
				self._nextCursor = parent._f_getPrevCursorEntityBeforeChild( widget )
			
			
		
			

	
	
	#
	# CURSOR UPDATING
	#
	
	def _p_updateCursors(self):
		if self._bCursorRequiresUpdate:
			widget = self._mainCursor.location.cursorEntity.widget
			if widget.isRealised():
				# main cursor is valid; no need for prev and next cursors
				self._prevCursor = None
				self._nextCursor = None
			else:
				self._mainCursor = self._prevCursor
				self._prevCursor = None
				self._nextCursor = None
			self._bCursorRequiresUpdate = False
			self._p_cursorSegmentChanged()
			
	def _p_mainCursorChanged(self):
		self._bCursorRequiresUpdate = True
			
		
		
	def _p_updateCursorSegment(self):
		if self._bCursorSegmentRequiresUpdate:
			self._p_updateCursors()
			widget = self._mainCursor.location.cursorEntity.widget
			self._cursorSegment = widget.getCursorSegment( self._mainCursor.location )
			self._oldCursorSegment = None
			self._bCursorSegmentRequiresUpdate = False
			
	def _p_cursorSegmentChanged(self):
		self._oldCursorSegment = self._cursorSegment
		if self._oldCursorSegment is not None:
			self._o_queueRedraw( self._oldCursorSegment.a - Vector2( -1.0, -1.0 ), self._oldCursorSegment.b - self._oldCursorSegment.a  +  Vector2( 2.0, 2.0 ) )
		self._bCursorSegmentRequiresUpdate = True
		
	
	def _p_drawCursor(self, context):
		self._p_updateCursorSegment()
		if self._cursorSegment is not None:
			context.set_line_width( 1.0 )
			context.set_source_rgb( 0.0, 0.0, 0.0 )
			context.move_to( self._cursorSegment.a.x, self._cursorSegment.a.y )
			context.line_to( self._cursorSegment.b.x, self._cursorSegment.b.y )
			context.stroke()

	
	
	
	#
	# CURSOR POSITIONING METHODS
	#
	
	def getCursorSegment(self, cursorLocation):
		assert cursorLocation.cursorEntity is self._firstCursorEntity  or  cursorLocation.cursorEntity is self._lastCursorEntity
		entry = self._childEntries[0]
		if cursorLocation.cursorEntity is self._firstCursorEntity:
			return Segment2( Point2( entry._xPos, entry._yPos ), Point2( entry._xPos, entry._yPos + _CURSOR_HEIGHT ) )
		else:
			x = entry._xPos + entry._width
			y = entry._yPos + entry._height
			return Segment2( Point2( x, y - _CURSOR_HEIGHT ), Point2( x, y ) )
	
	
	def _o_getCursorLocationAtPosition(self, localPosition):
		entry = self._childEntries[0]
		midX = entry._xPos  +  _entry._width * 0.5
		if localPosition.x < midX:
			return DTCursorLocation( self._firstCursorEntity, DTCursorLocation.EDGE_TRAILING )
		else:
			return DTCursorLocation( self._lastCursorEntity, DTCursorLocation.EDGE_LEADING )

		
		
		
	#
	# CURSOR NAVIGATION METHODS
	#
	
	def _p_cursorLeft(self):
		pass
		
	def _p_cursorRight(self):
		pass
	
	def _p_cursorUp(self):
		pass
	
	def _p_cursorDown(self):
		pass









if __name__ == '__main__':
	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTBox import DTBox
	from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	def onChangeText(widget, data=None):
		label.text = 'Something else'

	def onChangeFont(widget, data=None):
		label.font = 'Sans bold 20'

	def onChangeColour(widget, data=None):
		label.colour = Colour3f( 1.0, 0.0, 0.0 )


	def makeButton(text, response):
		button = gtk.Button( text )
		button.connect( 'clicked', response )
		button.show()
		return button


	class MyLabel (DTLabel):
		def _o_onEnter(self, localPos):
			super( MyLabel, self )._o_onEnter( localPos )
			self._savedColour = self.colour
			self.colour = Colour3f( 0.0, 0.0, 1.0 )

		def _o_onLeave(self, localPos):
			super( MyLabel, self )._o_onLeave( localPos )
			self.colour = self._savedColour


	# test label
	label = MyLabel( 'Hello world' )





	# Dnd test

	class DndOp (object):
		pass


	op = DndOp()


	dndTitleLabel = DTLabel( '--- Drag and drop test ---' )

	dndSourceLabels = [ DTLabel( 'Source %d'  %  ( i, ) )    for i in xrange( 0, 3 ) ]
	dndDestLabels = [ DTLabel( 'Dest %d'  %  ( i, ) )    for i in xrange( 0, 3 ) ]


	def dndBeginCallback(dndSource, localPos, button, state):
		print 'dndBeginCallback: ', dndSource, localPos, button, state
		return 123

	def dndMotionCallback(dndSource, dndDest, dndBeginData, localPos, button, state):
		print 'dndMotionCallback: ', dndSource, dndDest, dndBeginData, localPos, button, state

	def dndCanDropFromCallback(dndSource, dndDest, dndBeginData, button, state):
		print 'dndCanDropFromCallback: ', dndSource, dndDest, dndBeginData, button, state
		#return True
		return dndSourceLabels.index( dndSource )  ==  dndDestLabels.index( dndDest )

	def dndDragToCallback(dndSource, dndDest, localPos, button, state):
		print 'dndDragToCallback: ', dndSource, dndDest, localPos, button, state
		return dndSource.text

	def dndDropFromCallback(dndSource, dndDest, dndData, localPos, button, state):
		print 'dndDropFromCallback: ', dndSource, dndDest, dndData, localPos, button, state


	for srcLabel in dndSourceLabels:
		srcLabel.addDndSourceOp( op )
		srcLabel.dndBeginCallback = dndBeginCallback
		srcLabel.dndDragToCallback = dndDragToCallback

	for dstLabel in dndDestLabels:
		dstLabel.addDndDestOp( op )
		dstLabel.dndMotionCallback = dndMotionCallback
		dstLabel.dndCanDropFromCallback = dndCanDropFromCallback
		dstLabel.dndDropFromCallback = dndDropFromCallback


	dndSourceBox = DTBox( spacing=20.0 )
	dndSourceBox[:] = dndSourceLabels

	dndDestBox = DTBox( spacing=20.0 )
	dndDestBox[:] = dndDestLabels

	dndBox = DTBox( direction=DTDirection.TOP_TO_BOTTOM, spacing=10.0 )
	dndBox.append( dndSourceBox )
	dndBox.append( dndDestBox )




	docBox = DTBox( direction=DTDirection.TOP_TO_BOTTOM )
	docBox.append( label )
	docBox.append( dndBox, bExpand=True, bFill=True, padding=20.0 )





	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.getGtkWidget().show()

	doc.child = docBox


	textButton = makeButton( 'Change text', onChangeText )
	fontButton = makeButton( 'Change font', onChangeFont )
	colourButton = makeButton( 'Change colour', onChangeColour )


	buttonBox = gtk.HBox( True )
	buttonBox.pack_start( textButton, False, False, 20 )
	buttonBox.pack_start( fontButton, False, False, 20 )
	buttonBox.pack_start( colourButton, False, False, 20 )
	buttonBox.show_all()

	box = gtk.VBox()
	box.pack_start( doc.getGtkWidget() )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
