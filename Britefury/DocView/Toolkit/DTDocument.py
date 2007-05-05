##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref

import pygtk
pygtk.require( '2.0' )
import gtk
import gobject
import cairo

from Britefury.Math.Math import Vector2, Point2, BBox2
from Britefury.DocView.Toolkit.DTKeyEvent import DTKeyEvent
from Britefury.DocView.Toolkit.DTBin import DTBin




class DTDocument (gtk.DrawingArea, DTBin):
	def __init__(self):
		gtk.DrawingArea.__init__( self )
		DTBin.__init__( self )

		self._documentSize = Vector2()
		self._bAllocationRequired = False
		self._keyboardFocusChild = None
		self._keyboardFocusGrabChild = None

		# Connect signals
		self.connect_after( 'configure-event', self._p_configureEvent )
		self.connect( 'expose-event', self._p_exposeEvent )
		self.connect( 'button-press-event', self._p_buttonPressEvent )
		self.connect( 'button-release-event', self._p_buttonReleaseEvent )
		self.connect( 'motion-notify-event', self._p_motionNotifyEvent )
		self.connect( 'enter-notify-event', self._p_enterNotifyEvent )
		self.connect( 'leave-notify-event', self._p_leaveNotifyEvent )
		self.connect( 'scroll-event', self._p_scrollEvent )
		self.connect( 'key-press-event', self._p_keyPressEvent )
		self.connect( 'key-release-event', self._p_keyReleaseEvent )
		self.connect_after( 'realize', self._p_realiseEvent )
		self.connect( 'unrealize', self._p_unrealiseEvent )

		# Tell the widget to send these events
		self.add_events( gtk.gdk.EXPOSURE_MASK |
					gtk.gdk.BUTTON_PRESS_MASK |
					gtk.gdk.BUTTON_RELEASE_MASK |
					gtk.gdk.POINTER_MOTION_MASK |
					gtk.gdk.POINTER_MOTION_HINT_MASK |
					gtk.gdk.ENTER_NOTIFY_MASK |
					gtk.gdk.LEAVE_NOTIFY_MASK |
					gtk.gdk.SCROLL_MASK |
					gtk.gdk.KEY_PRESS_MASK |
					gtk.gdk.KEY_RELEASE_MASK )

		self.set_flags( gtk.CAN_FOCUS )



	def _o_queueRedraw(self, localPos, localSize):
		self._p_invalidateRect( localPos, localSize )

	def _p_invalidateRect(self, pos, size):
		self.queue_draw_area( int( pos.x ), int( pos.y ), int( size.x + 0.5 ), int( size.y + 0.5 ) )


	def _o_queueResize(self):
		self._bAllocationRequired = True
		self.queue_draw()




	def _f_childGrabFocus(self, child):
		if child is not self._keyboardFocusChild:
			if self._keyboardFocusGrabChild is not None  and  child is not None:
				self._keyboardFocusGrabChild._f_clearFocusGrab()
			if self._keyboardFocusChild is not None  and  child is not None:
				self._keyboardFocusChild._o_onLoseFocus()
			self._keyboardFocusChild = child
			if self._keyboardFocusChild is not None:
				self._keyboardFocusChild._o_onGainFocus()
				self._keyboardFocusGrabChild = child



	def _f_childUngrabFocus(self, child):
		if child is self._keyboardFocusChild:
			if self._keyboardFocusChild is not None:
				self._keyboardFocusChild._o_onLoseFocus()
			self._keyboardFocusChild = None





	def _p_configureEvent(self, widget, event):
		docSize = Vector2( event.width, event.height )
		if docSize != self._documentSize:
			self._documentSize = docSize
			self._bAllocationRequired = True


	def _p_exposeEvent(self, widget, event):
		if self._bAllocationRequired:
			reqWidth = self._f_getRequisitionWidth()
			self._f_allocateX( self._documentSize.x )
			reqHeight = self._f_getRequisitionHeight()
			self._f_allocateY( self._documentSize.y )
			self._bAllocationRequired = False
		context = widget.window.cairo_create()
		context.rectangle( event.area.x, event.area.y, event.area.width, event.area.height )
		context.clip_preserve()
		context.set_source_rgb( 1.0, 1.0, 1.0 )
		context.fill()
		context.new_path()
		self._f_draw( context, BBox2( Point2( event.area.x, event.area.y ), Point2( event.area.x + event.area.width, event.area.y + event.area.height ) ) )
		return False


	def _p_buttonPressEvent(self, widget, event):
		self.grab_focus()
		x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )
		if event.type == gtk.gdk.BUTTON_PRESS:
			self._f_evButtonDown( localPos, event.button, state )
		elif event.type == gtk.gdk._2BUTTON_PRESS:
			self._f_evButtonDown2( localPos, event.button, state )
		elif event.type == gtk.gdk._3BUTTON_PRESS:
			self._f_evButtonDown3( localPos, event.button, state )

	def _p_buttonReleaseEvent(self, widget, event):
		x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )
		self._f_evButtonUp( localPos, event.button, state )


	def _p_motionNotifyEvent(self, widget, event):
		if event.is_hint:
			x, y, state = event.window.get_pointer()
		else:
			x, y, state = event.x, event.y, event.state
		localPos = Point2( x, y )
		self._f_evMotion( localPos )


	def _p_enterNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		localPos = Point2( x, y )
		self._f_evEnter( localPos )


	def _p_leaveNotifyEvent(self, widget, event):
		x, y, state = event.window.get_pointer()
		localPos = Point2( x, y )
		self._f_evLeave( localPos )


	def _p_scrollEvent(self, widget, event):
		if event.direction == gtk.gdk.SCROLL_UP:
			scroll = Vector2( 0.0, -1.0 )
		elif event.direction == gtk.gdk.SCROLL_DOWN:
			scroll = Vector2( 0.0, 1.0 )
		elif event.direction == gtk.gdk.SCROLL_LEFT:
			scroll = Vector2( -1.0, 0.0 )
		elif event.direction == gtk.gdk.SCROLL_RIGHT:
			scroll = Vector2( 1.0, 0.0 )
		self._f_evScroll( scroll )



	def _p_keyPressEvent(self, widget, event):
		if self._keyboardFocusChild is not None:
			self._keyboardFocusChild._o_onKeyPress( DTKeyEvent( event ) )

	def _p_keyReleaseEvent(self, widget, event):
		if self._keyboardFocusChild is not None:
			self._keyboardFocusChild._o_onKeyRelease( DTKeyEvent( event ) )



	def _p_realiseEvent(self, widget):
		context = widget.window.cairo_create()
		self._f_evRealise( context )

	def _p_unrealiseEvent(self, widget):
		self._f_evUnrealise()










if __name__ == '__main__':
	from Britefury.DocView.Toolkit.DTLabel import DTLabel
	from Britefury.DocView.Toolkit.DTFont import DTFont
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
		label.font.weight = cairo.FONT_WEIGHT_BOLD
		label.font.size = 20.0

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


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	label = MyLabel( 'Hello world' )
	doc.child = label


	textButton = makeButton( 'Change text', onChangeText )
	fontButton = makeButton( 'Change font', onChangeFont )
	colourButton = makeButton( 'Change colour', onChangeColour )


	buttonBox = gtk.HBox( True )
	buttonBox.pack_start( textButton, False, False, 20 )
	buttonBox.pack_start( fontButton, False, False, 20 )
	buttonBox.pack_start( colourButton, False, False, 20 )
	buttonBox.show_all()

	box = gtk.VBox()
	box.pack_start( doc )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
