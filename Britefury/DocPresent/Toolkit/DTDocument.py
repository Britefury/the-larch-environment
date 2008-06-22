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

from Britefury.Event.QueuedEvent import queueEvent, dequeueEvent

from Britefury.Math.Math import Vector2, Point2, BBox2, Segment2

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTCursor import DTCursorLocation, DTCursor
from Britefury.DocPresent.Toolkit.DTKeyEvent import DTKeyEvent
from Britefury.DocPresent.Toolkit.DTBin import DTBin
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer



_CURSORSYSTEM_WIDGET = 1
_CURSORSYSTEM_DOCUMENT = 2

_CURSORSYSTEM = _CURSORSYSTEM_WIDGET



"""
Keyboard focus handling:

Here is how the document handles keyboard focus
It maintains a reference to the grab-target and the focus-target.
The grab-target is the last widget to grab the focus.
The focus-target is the widget that has the focus currently, and receives all keyboard events.

This is done so that situations where a widget grabs the focus, is removed from the widget tree, and then returned to the widget tree, and handled correctly.

Example 1:
1. Widget @w grabs focus:
    The grab-target and the focus-target are both set to @w
    @w is notified that it has gained focus
2. @w is removed from the widget tree:
    inside of its unrealise handler, @w relinquishes focus
    the focus-target is cleared; @w no longer has focus
    @w is notified that is has lost focus
3. @w is placed back into the widget tree:
    inside of its unrealise handler, @w asks to reacquire the focus
    the focus-target is set to @w; @w has the focus
    @w is notified that it has gained focus
4. @w ungrabs focus:
    The grab-target and the focus-target are both cleared
    @w is notified that it has lost focus
    
Example 2:
1. Widget @w grabs focus:
    The grab-target and the focus-target are both set to @w
    @w is notified that it has gained focus
2. @w is removed from the widget tree:
    inside of its unrealise handler, @w relinquishes focus
    the focus-target is cleared; @w no longer has focus
    @w is notified that is has lost focus
3. *** Widget @x grabs the focus ***
    The grab-target and the focus-target are both set to @x
    @x is notified that it has gained focus
4. @w is placed back into the widget tree:
    inside of its unrealise handler, @w asks to reacquire the focus
    since the grab-target is @x, not @w, this is refused
    @w is told to clear its focus grab
"""



_undoAccel = gtk.accelerator_parse( '<control>z' )
_redoAccel = gtk.accelerator_parse( '<control><shift>Z' )


_CURSOR_HEIGHT = 22.0




class _GtkPresentationArea (gtk.DrawingArea):
	"""This helper class is declared, since the do_key_press_event() method of a gtk.DrawingArea must be overridden in order to receive keyboard events.
	We cannot derive our main class from gtk.DrawingArea, otherwise the DTWidget and gtk.DrawingArea 'parent' attributed will collide, resulting in problems.
	This help class overrides the do_key_press_event() method and passes its arguments along."""
	def __init__(self, presentationArea):
		gtk.DrawingArea.__init__( self )
		self._presentationArea = presentationArea
		
	def do_key_press_event(self, event):
		bHandled = self._presentationArea.do_key_press_event( event )
		if bHandled:
			return True
		else:
			return gtk.DrawingArea.do_key_press_event( self, event )
		
	def do_key_release_event(self, event):
		bHandled = self._presentationArea.do_key_release_event( event )
		if bHandled:
			return True
		else:
			#return super( _GtkPresentationArea, self ).do_key_release_event( event )
			return gtk.DrawingArea.do_key_release_event( self, event )
		
gobject.type_register( _GtkPresentationArea )



class DTDocument (DTBin):
	undoSignal = ClassSignal()
	redoSignal = ClassSignal()
	
	
	CONTROL_MASK = gtk.gdk.CONTROL_MASK
	SHIFT_MASK = gtk.gdk.SHIFT_MASK
	ALT_MASK = gtk.gdk.MOD1_MASK
	BUTTON1_MASK = gtk.gdk.BUTTON1_MASK
	BUTTON2_MASK = gtk.gdk.BUTTON2_MASK
	BUTTON3_MASK = gtk.gdk.BUTTON3_MASK
	BUTTON4_MASK = gtk.gdk.BUTTON4_MASK
	BUTTON5_MASK = gtk.gdk.BUTTON5_MASK
	

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


		self._docWindowTopLeftCornerInDocSpace = Point2()
		self._docScaleInWindowCoords = 1.0
		self._docDragStartPosWindowSpace = Point2()
		self._docDragButton = None

		self._documentSize = Vector2()
		self._bAllocationRequired = False
		
		# Pointer status
		self._pointerPosition = Point2()
		self._pointerState = 0
		self._stateKeyListeners = weakref.WeakKeyDictionary()
		
		# Keyboard focus; the grab-target is the widget that last grabbed the focus, the focus-target is the widget that has the focus
		self._keyboardFocusTarget = None
		self._keyboardFocusGrabTarget = None

		# Event queues
		self._immediateEvents = []
		
		# Cursor entities for the start and end of the document
		self._firstCursorEntity = DTCursorEntity( self, DTCursorEntity.EDGEFLAGS_TRAILING, identity='<DOCFIRST>' )
		self._lastCursorEntity = DTCursorEntity( self, DTCursorEntity.EDGEFLAGS_LEADING, identity='<DOCLAST>' )
		self._firstCursorEntity.next = self._lastCursorEntity
		
		# Cursor management
		self._mainCursor = DTCursor( self, DTCursorLocation( self._firstCursorEntity, DTCursorLocation.EDGE_TRAILING ) )
		self._prevCursor = None
		self._nextCursor = None
		self._cursorMode = self.CURSORMOVEMENT_MOVE
		self._cursorWidget = None
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
	
	
	def _p_windowSpaceToDocSpace(self, w):
		if isinstance( w, Point2 ):
			w = w.toVector2()
		return Point2( ( w  *  ( 1.0 / self._docScaleInWindowCoords ) )  +  self._docWindowTopLeftCornerInDocSpace.toVector2() )

	def _p_docSpaceToWindowSpace(self, d):
		if isinstance( d, Point2 ):
			d = d.toVector2()
		return Point2( ( d - self._docWindowTopLeftCornerInDocSpace.toVector2() )  *  self._docScaleInWindowCoords )

	
	def _p_windowSpaceSizeToDocSpace(self, w):
		return w  *  ( 1.0 / self._docScaleInWindowCoords )

	def _p_docSpaceSizeToWindowSpace(self, d):
		return d  *  self._docScaleInWindowCoords
	
	
	def getPointInWindowCoords(self, pointInDocCoords):
		return self._p_docSpaceToWindowSpace( pointInDocCoords )
	
	
	def oneToOne(self):
		# We want to scale about the centre of the document, not the top left corner
		self._docScaleInWindowCoords = 1.0
		self._p_queueFullRedraw()

	def reset(self):
		self._docWindowTopLeftCornerInDocSpace = Point2()
		self._docScaleInWindowCoords = 1.0
		self._p_queueFullRedraw()



	def queueImmediateEvent(self, f):
		if len( self._immediateEvents ) == 0:
			queueEvent( self._p_onQueuedEvent )
		if f not in self._immediateEvents:
			self._immediateEvents.append( f )
			
	def dequeueImmediateEvent(self, f):
		if f in self._immediateEvents:
			self._immediateEvents.remove( f )
			if len( self._immediateEvents ) == 0:
				dequeueEvent( self._p_onQueuedEvent )
	
				
				
	def addStateKeyListener(self, listener):
		self._stateKeyListeners[listener] = None
		
	def removeStateKeyListener(self, listener):
		del self._stateKeyListeners[listener]
			
			
			
	def _p_onQueuedEvent(self):
		self._p_emitImmediateEvents()


	def _p_emitImmediateEvents(self):
		# User events
		dequeueEvent( self._p_onQueuedEvent )
		while len( self._immediateEvents ) > 0:
			events = copy( self._immediateEvents )
			self._immediateEvents = []
			for event in events:
				event()
		
	




	def _p_queueFullRedraw(self):
		self._drawingArea.queue_draw()

	def _o_queueRedraw(self, localPos, localSize):
		self._p_invalidateRect( self._p_docSpaceToWindowSpace( localPos ), self._p_docSpaceSizeToWindowSpace( localSize ) )

	def _p_invalidateRect(self, posInWindowSpace, sizeInWindowSpace):
		self._drawingArea.queue_draw_area( int( posInWindowSpace.x ), int( posInWindowSpace.y ), int( math.ceil( sizeInWindowSpace.x ) ), int( math.ceil( sizeInWindowSpace.y ) ) )


	def _o_queueResize(self):
		self._bAllocationRequired = True
		self._drawingArea.queue_draw()




	def _p_performAllocation(self):
		if self._bAllocationRequired:
			reqWidth = self._f_getRequisitionWidth()
			self._f_allocateX( self._documentSize.x / self._docScaleInWindowCoords )
			reqHeight, reqBaseline = self._f_getRequisitionHeightAndBaseline()
			self._f_allocateY( reqHeight )
			self._bAllocationRequired = False
					


	def _o_onAllocateX(self, allocation):
		if self._child is not None:
			self._o_allocateChildX( self._child, 0.0, min( self._childRequisition.x, allocation ) )

	def _o_onAllocateY(self, allocation):
		if self._child is not None:
			self._o_allocateChildY( self._child, 0.0, allocation )



	def _f_widgetGrabFocus(self, child):
		assert child is not None

		# If there is already a widget that has grabbed the keyboard focus, then clear its grab, and replace it with @child
		if child is not self._keyboardFocusGrabTarget:
			if self._keyboardFocusGrabTarget is not None:
				self._keyboardFocusGrabTarget._f_clearFocusGrab()
			self._keyboardFocusGrabTarget = child

		# If @child is different from the widget that has focus at the moment, switch
		if child is not self._keyboardFocusTarget:
			if self._keyboardFocusTarget is not None:
				keyboardFocusChild = self._keyboardFocusTarget
				self._keyboardFocusTarget = None
				keyboardFocusChild._o_onLoseFocus()
			self._keyboardFocusTarget = child
			self._keyboardFocusTarget._o_onGainFocus()



	def _f_widgetUngrabFocus(self, child):
		assert child is not None

		if child is self._keyboardFocusGrabTarget:
			self._keyboardFocusGrabTarget = None

		if child is self._keyboardFocusTarget:
			keyboardFocusChild = self._keyboardFocusTarget
			self._keyboardFocusTarget = None
			keyboardFocusChild._o_onLoseFocus()





	def _f_widgetAcquireFocus(self, child):
		assert child is not None

		# If there is already a widget that has grabbed the keyboard focus, then clear the focus grab on @child
		if child is not self._keyboardFocusGrabTarget  and  self._keyboardFocusGrabTarget is not None:
			child._f_clearFocusGrab()
		else:
			# If @child is different from the widget that has focus at the moment, switch
			if child is not self._keyboardFocusTarget:
				if self._keyboardFocusTarget is not None:
					keyboardFocusChild = self._keyboardFocusTarget
					self._keyboardFocusTarget = None
					keyboardFocusChild._o_onLoseFocus()
				self._keyboardFocusTarget = child
				self._keyboardFocusTarget._o_onGainFocus()



	def _f_widgetRelinquishFocus(self, child):
		assert child is not None

		if child is self._keyboardFocusTarget:
			keyboardFocusChild = self._keyboardFocusTarget
			self._keyboardFocusTarget = None
			keyboardFocusChild._o_onLoseFocus()





	def removeFocusGrab(self):
		if self._keyboardFocusTarget is not None:
			self._keyboardFocusTarget.ungrabFocus()




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
		topLeftDocSpace = self._p_windowSpaceToDocSpace( Point2( event.area.x, event.area.y ) )
		bottomRightDocSpace = self._p_windowSpaceToDocSpace( Point2( event.area.x + event.area.width, event.area.y + event.area.height ) )

		context.save()
		context.scale( self._docScaleInWindowCoords, self._docScaleInWindowCoords )
		context.translate( -self._docWindowTopLeftCornerInDocSpace.x, -self._docWindowTopLeftCornerInDocSpace.y )

		self._f_draw( context, BBox2( topLeftDocSpace, bottomRightDocSpace ) )
		
		if _CURSORSYSTEM == _CURSORSYSTEM_DOCUMENT:
			self._p_drawCursor( context )
		
		context.restore()
		
		self._p_emitImmediateEvents()
		return False


	def _p_buttonPressEvent(self, widget, event):
		self._drawingArea.grab_focus()
		x, y, state = event.x, event.y, event.state
		self._pointerPosition = Point2( x, y )
		self._pointerState = state
		localPos = self._p_windowSpaceToDocSpace( self._pointerPosition )
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
				self._docDragStartPosWindowSpace = self._pointerPosition
		self._p_emitImmediateEvents()


	def _p_buttonReleaseEvent(self, widget, event):
		x, y, state = event.x, event.y, event.state
		self._pointerPosition = Point2( x, y )
		self._pointerState = state
		localPos = self._p_windowSpaceToDocSpace( self._pointerPosition )
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
		self._pointerPosition = Point2( x, y )
		self._pointerState = state
		localPos = self._p_windowSpaceToDocSpace( self._pointerPosition )

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
			delta = self._pointerPosition - self._docDragStartPosWindowSpace
			self._docDragStartPosWindowSpace = self._pointerPosition
			bModified = False
			if self._docDragButton == 1  or  self._docDragButton == 2:
				self._docWindowTopLeftCornerInDocSpace -= self._p_windowSpaceSizeToDocSpace( delta )
				bModified = True
			elif self._docDragButton == 3:
				scaleDeltaPixels = delta.x + delta.y
				scaleDelta = 2.0  **  ( scaleDeltaPixels / 200.0 )

				# We want to scale about the centre of the document, not the top left corner
				centreInWindowSpace = self._documentSize * 0.5
				centreInDocSpace = self._p_windowSpaceToDocSpace( centreInWindowSpace )
				self._docScaleInWindowCoords *= scaleDelta
				newCentreInDocSpace = self._p_windowSpaceToDocSpace( centreInWindowSpace )
				self._docWindowTopLeftCornerInDocSpace -= ( newCentreInDocSpace - centreInDocSpace )
		
				bModified = True
			if bModified:
				self._p_queueFullRedraw()
		self._p_emitImmediateEvents()


	def _p_enterNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		self._pointerPosition = Point2( x, y )
		self._pointerState = state
		localPos = self._p_windowSpaceToDocSpace( self._pointerPosition )
		if self._docDragButton is None:
			self._f_evEnter( localPos )
		self._p_emitImmediateEvents()


	def _p_leaveNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		self._pointerPosition = Point2( x, y )
		self._pointerState = state
		localPos = self._p_windowSpaceToDocSpace( self._pointerPosition )
		if self._docDragButton is None:
			self._f_evLeave( localPos )
		self._p_emitImmediateEvents()


	def _p_scrollEvent(self, widget, event):
		self._pointerState = event.state
		if event.state  &  ( gtk.gdk.MOD1_MASK | gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK )  ==  gtk.gdk.MOD1_MASK:
			if event.direction == gtk.gdk.SCROLL_UP:
				delta = 1.0
			elif event.direction == gtk.gdk.SCROLL_DOWN:
				delta = -1.0

			scaleDelta = 2.0  **  ( delta / 1.5 )

			# We want to scale about the pointer position, not the top left corner
			centreInWindowSpace = Vector2( event.x, event.y )
			centreInDocSpace = self._p_windowSpaceToDocSpace( centreInWindowSpace )
			self._docScaleInWindowCoords *= scaleDelta
			newCentreInDocSpace = self._p_windowSpaceToDocSpace( centreInWindowSpace )
			self._docWindowTopLeftCornerInDocSpace -= ( newCentreInDocSpace - centreInDocSpace )

			self._p_queueFullRedraw()
		else:
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
					delta = -1.0
				elif event.direction == gtk.gdk.SCROLL_DOWN:
					delta = 1.0
					
				self._docWindowTopLeftCornerInDocSpace += self._p_windowSpaceSizeToDocSpace( Vector2( 0.0, delta  *  150.0 ) )

				self._p_queueFullRedraw()




	def do_key_press_event(self, event):
		keyEvent = DTKeyEvent( event )
		key = keyEvent.keyVal, keyEvent.state
		self._pointerState = event.state
		if key == _undoAccel:
			self.undoSignal.emit( self )
			self._p_emitImmediateEvents()
			return True
		elif key == _redoAccel:
			self.redoSignal.emit( self )
			self._p_emitImmediateEvents()
			return True
		elif self._o_handleDocumentKeyPress( keyEvent ):
			self._p_emitImmediateEvents()
			return True
		else:
			if event.keyval in [ gtk.keysyms.Alt_L, gtk.keysyms.Alt_R, gtk.keysyms.Control_L, gtk.keysyms.Control_R, gtk.keysyms.Shift_L, gtk.keysyms.Shift_R ]:
				for listener in self._stateKeyListeners.keys():
					listener._onStateKeyPress( keyEvent )
			else:
				if _CURSORSYSTEM == _CURSORSYSTEM_WIDGET:
					if self._keyboardFocusTarget is not None:
						self._keyboardFocusTarget._o_onKeyPress( keyEvent )
						self._p_emitImmediateEvents()
						return True
					else:
						self._p_emitImmediateEvents()
						return False
				elif _CURSORSYSTEM == _CURSORSYSTEM_DOCUMENT:
					self._p_updateCursors()
					loc = self._mainCursor.location
					widget = loc.cursorEntity.widget
					if widget is not None:
						widget._o_onKeyPress( keyEvent )
						self._p_emitImmediateEvents()
						return True
					else:
						self._p_emitImmediateEvents()
						return False
				else:
					raise ValueError
				



	def do_key_release_event(self, event):
		keyEvent = DTKeyEvent( event )
		key = keyEvent.keyVal, keyEvent.state
		self._pointerState = event.state
		if key == _undoAccel  or  key == _redoAccel:
			self._p_emitImmediateEvents()
			return True
		elif self._o_handleDocumentKeyRelease( keyEvent ):
			self._p_emitImmediateEvents()
			return True
		else:
			if event.keyval in [ gtk.keysyms.Alt_L, gtk.keysyms.Alt_R, gtk.keysyms.Control_L, gtk.keysyms.Control_R, gtk.keysyms.Shift_L, gtk.keysyms.Shift_R ]:
				for listener in self._stateKeyListeners.keys():
					listener._onStateKeyRelease( keyEvent )
			else:
				if _CURSORSYSTEM == _CURSORSYSTEM_WIDGET:
					if self._keyboardFocusTarget is not None:
						self._keyboardFocusTarget._o_onKeyRelease( keyEvent )
						self._p_emitImmediateEvents()
						return True
					else:
						self._p_emitImmediateEvents()
						return False
				elif _CURSORSYSTEM == _CURSORSYSTEM_DOCUMENT:
					self._p_updateCursors()
					loc = self._mainCursor.location
					widget = loc.cursorEntity.widget
					if widget is not None:
						widget._o_onKeyRelease( keyEvent )
						self._p_emitImmediateEvents()
						return True
					else:
						self._p_emitImmediateEvents()
						return False
				else:
					raise ValueError



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
		if cursor is self._mainCursor:
			self._p_sendCursorWidgetEvents()


			
	def _f_cursorUnrealiseNotify(self, cursor):
		cursorEntity = cursor.location.cursorEntity
		if cursorEntity is not self._firstCursorEntity  and  cursorEntity is not self._lastCursorEntity:
			widget = cursorEntity.widget
			parent = widget.parent
			assert parent is not None
	
			if cursor is self._mainCursor:
				self._prevCursor = parent._f_getPrevCursorEntityBeforeChild( widget )
				self._nextCursor = parent._f_getNextCursorEntityAfterChild( widget )
				self._p_mainCursorChanged()
			elif cursor is self._prevCursor:
				self._prevCursor = parent._f_getPrevCursorEntityBeforeChild( widget )
			elif cursor is self._nextCursor:
				self._nextCursor = parent._f_getNextCursorEntityAfterChild( widget )
			
			
		
			

	
	
	#
	# CURSOR UPDATING
	#
	
	def _p_updateCursors(self):
		if self._bCursorRequiresUpdate:
			widget = self._mainCursor.location.cursorEntity.widget
			bMainCursorModified = False
			if widget.isRealised():
				# main cursor is valid; no need for prev and next cursors
				self._prevCursor = None
				self._nextCursor = None
			else:
				self._mainCursor = self._prevCursor
				self._prevCursor = None
				self._nextCursor = None
				bMainCursorModified = True
			self._bCursorRequiresUpdate = False
			self._p_cursorSegmentChanged()
			if bMainCursorModified:
				self._p_sendCursorWidgetEvents()
			
	def _p_mainCursorChanged(self):
		self._bCursorRequiresUpdate = True
		
		
	def _p_sendCursorWidgetEvents(self):
		widget = self._mainCursor.location.cursorEntity.widget
		if widget is not self._cursorWidget:
			if self._cursorWidget is not None:
				self._cursorWidget._f_evCursorLeave()
			self._cursorWidget = widget
			if self._cursorWidget is not None:
				self._cursorWidget._f_evCursorEnter( self._mainCursor )
		else:
			widget._f_evCursorMotion( self._mainCursor, self._cursorMode )
		
			
		
		
	def _p_updateCursorSegment(self):
		if self._bCursorSegmentRequiresUpdate:
			self._p_updateCursors()
			widget = self._mainCursor.location.cursorEntity.widget
			xform = widget.getTransformRelativeToDocument()
			self._cursorSegment = widget.getCursorSegment( self._mainCursor.location )  *  xform
			self._oldCursorSegment = None
			self._bCursorSegmentRequiresUpdate = False
			
	def _p_cursorSegmentChanged(self):
		self._oldCursorSegment = self._cursorSegment
		#if self._oldCursorSegment is not None:
		#	self._o_queueRedraw( self._oldCursorSegment.a - Vector2( -1.0, -1.0 ), self._oldCursorSegment.b - self._oldCursorSegment.a  +  Vector2( 2.0, 2.0 ) )
		# HACK
		# HACK
		# HACK
		self._drawingArea.queue_draw()
		# HACK
		# HACK
		# HACK
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
	
	def _p_cursorLeft(self, mode):
		self._p_updateCursors()
		loc = self._mainCursor.location
		oldMode = self._cursorMode
		self._cursorMode = mode
		if loc.edge == DTCursorLocation.EDGE_TRAILING:
			self._mainCursor.location = DTCursorLocation( loc.cursorEntity, DTCursorLocation.EDGE_LEADING )
		else:
			if loc.cursorEntity is not self._firstCursorEntity:
				self._mainCursor.location = DTCursorLocation( loc.cursorEntity.prev, DTCursorLocation.EDGE_LEADING )
		self._cursorMode = oldMode
		
	def _p_cursorRight(self, mode):
		self._p_updateCursors()
		loc = self._mainCursor.location
		oldMode = self._cursorMode
		self._cursorMode = mode
		if loc.edge == DTCursorLocation.EDGE_LEADING:
			self._mainCursor.location = DTCursorLocation( loc.cursorEntity, DTCursorLocation.EDGE_TRAILING )
		else:
			if loc.cursorEntity is not self._lastCursorEntity:
				self._mainCursor.location = DTCursorLocation( loc.cursorEntity.next, DTCursorLocation.EDGE_TRAILING )
		self._cursorMode = oldMode
	
	def _p_cursorUp(self, mode):
		pass
	
	def _p_cursorDown(self, mode):
		pass
	
	
	
	def _f_setCursorLocation(self, loc):
		self._mainCursor.location = loc
		



	#
	# FOCUS NAVIGATION METHODS
	#
	
	def _o_handleDocumentKeyPress(self, event):
		if event.keyVal in [ gtk.keysyms.Left, gtk.keysyms.Right, gtk.keysyms.Up, gtk.keysyms.Down, gtk.keysyms.Home, gtk.keysyms.End ]:
			if _CURSORSYSTEM == _CURSORSYSTEM_WIDGET:
				if self._keyboardFocusTarget is not None:
					if self._keyboardFocusTarget._f_handleMotionKeyPress( event ):
						return True
					else:
						if event.keyVal == gtk.keysyms.Left:
							self._keyboardFocusTarget.cursorLeft()
						elif event.keyVal == gtk.keysyms.Right:
							self._keyboardFocusTarget.cursorRight()
						elif event.keyVal == gtk.keysyms.Up:
							self._keyboardFocusTarget.cursorUp()
						elif event.keyVal == gtk.keysyms.Down:
							self._keyboardFocusTarget.cursorDown()
						return True
				return True
			elif _CURSORSYSTEM == _CURSORSYSTEM_DOCUMENT:
				mode = self.CURSORMOVEMENT_DRAG   if event.state == gtk.gdk.SHIFT_MASK   else self.CURSORMOVEMENT_MOVE
				if event.keyVal == gtk.keysyms.Left:
					self._p_cursorLeft( mode )
				elif event.keyVal == gtk.keysyms.Right:
					self._p_cursorRight( mode )
				elif event.keyVal == gtk.keysyms.Up:
					self._p_cursorUp( mode )
				elif event.keyVal == gtk.keysyms.Down:
					self._p_cursorDown( mode )
				return True
		else:
			return False


	def _o_handleDocumentKeyRelease(self, event):
		if event.keyVal in [ gtk.keysyms.Left, gtk.keysyms.Right, gtk.keysyms.Up, gtk.keysyms.Down, gtk.keysyms.Home, gtk.keysyms.End ]:
			return True
		else:
			return False
		
		
		
		
	# State utility functions
	@staticmethod
	def stateValueCoerce(state):
		if isinstance( state, str )  or  isinstance( state, unicode ):
			value = 0
			state = state.lower()
			if 'shift' in state:
				value |= DTDocument.SHIFT_MASK
			if 'control' in state  or  'ctrl' in state:
				value |= DTDocument.CONTROL_MASK
			if 'alt' in state  or  'mod1' in state:
				value |= DTDocument.ALT_MASK
			if 'button1' in state  or  'b1' in state:
				value |= DTDocument.BUTTON1_MASK
			if 'button2' in state  or  'b2' in state:
				value |= DTDocument.BUTTON2_MASK
			if 'button3' in state  or  'b3' in state:
				value |= DTDocument.BUTTON3_MASK
			if 'button4' in state  or  'b4' in state:
				value |= DTDocument.BUTTON4_MASK
			if 'button5' in state  or  'b5' in state:
				value |= DTDocument.BUTTON5_MASK
			return value
		else:
			return state
		




if __name__ == '__main__':
	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTBox import DTBox
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

	dndBox = DTBox( direction=DTBox.TOP_TO_BOTTOM, spacing=10.0 )
	dndBox.append( dndSourceBox )
	dndBox.append( dndDestBox )




	docBox = DTBox( direction=DTBox.TOP_TO_BOTTOM )
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
