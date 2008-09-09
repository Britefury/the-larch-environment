##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk
import gobject


class PopupWindow (object):
	TIMEOUT = 0
	CLICK = 1
	APP_CONTROLLED = 2


	def __init__(self, spacing=0, shadowType=None):
		#Contain the contents in an event box. @table is app paintable.
		#@m_contentsWrapper will ensure that the contents has its own window so that
		#the painting of @table will not obliterate the contents
		self._contentsWrapper = gtk.EventBox()
		self._contentsWrapper.show()
		self._contents = None

		self._table = gtk.Table( 1, 1, True )
		self._table.attach( self._contentsWrapper, 0, 1, 0, 1, gtk.EXPAND | gtk.SHRINK | gtk.FILL, gtk.EXPAND | gtk.SHRINK | gtk.FILL, spacing, spacing )
		self._table.show()
		self._table.set_app_paintable( True )
		self._table.connect( 'expose-event', self._p_onTableExpose )

		self._eventBox = gtk.EventBox()
		self._eventBox.add( self._table )
		self._eventBox.show()

		self._window = gtk.Window( gtk.WINDOW_POPUP )
		self._window.add( self._eventBox )

		self._eventBox.connect( 'enter-notify-event', self._p_onEventBoxEnterNotify )
		self._eventBox.connect( 'leave-notify-event', self._p_onEventBoxLeaveNotify )
		self._eventBox.connect( 'button-press-event', self._p_onEventBoxButtonPress )

		self._timeoutID = None
		self._bVisible = False
		self._popupMode = self.TIMEOUT
		self.shadowType = shadowType



	def popupAtWidget(self, atWidget, relativeX, relativeY, popupMode):
		if not self._bVisible:
			# Get the position of the GDK window used for @belowWidget
			srcOriginX, srcOriginY = atWidget.window.get_origin()

			# Get the requested size of the popup window
			winWidth, winHeight = self._window.size_request()

			# Compute the desired position
			posX = srcOriginX  +  atWidget.allocation.x  +  relativeX
			posY = srcOriginY  +  atWidget.allocation.y  +  relativeY


			# Get the screen from @belowWidget
			screen = atWidget.get_screen()

			# Get the screen dimensions
			screenWidth = screen.get_width()
			screenHeight = screen.get_height()

			# Show the window
			self._p_showWindow( screen, popupMode )

			# The desired position of the popup window may place it partially offscreen
			if  posX + winWidth  >  screenWidth:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posX = max( screenWidth - winWidth, 0 )

			if  posY + winHeight  >  screenHeight:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posY = max( screenHeight - winHeight, 0 )

			# Position the window
			self._window.move( posX, posY )



	def popupAtPointer(self, popupMode):
		if not self._bVisible:
			# Get the default display, and the pointer position
			display = gtk.gdk.display_get_default()
			screen, x, y, mods = display.get_pointer()

			# Get the screen dimensions
			screenWidth = screen.get_width()
			screenHeight = screen.get_height()

			# Get the requested size of the popup window
			winWidth, winHeight = self._window.size_request()

			# Compute desired position
			posX = x - winWidth / 2
			posY = y - winHeight / 2

			# The desired position of the popup window may place it partially offscreen
			if  posX + winWidth  >  screenWidth:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posX = max( screenWidth - winWidth, 0 )

			if  posY + winHeight  >  screenHeight:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posY = max( screenHeight - winHeight, 0 )

			# Position the window
			self._window.move( posX, posY )

			# Show the popup window
			self._p_showWindow( screen, popupMode )




	def _p_showWindow(self, screen, popupMode):
		# Get the screen dimensions
		screenWidth = screen.get_width()
		screenHeight = screen.get_height()

		# Get the requested size of the popup window
		winWidth, winHeight = self._window.size_request()

		# Compute the size request; if the window size is larger than the screen, clamp ot, else set the override
		# to -1, as gtk.Widget.set_size_request() interprets -1 as 'no effect'
		if winWidth > screenWidth:
			requestedWidth = screenWidth
		else:
			requestedWidth = -1

		if winHeight > screenHeight:
			requestedHeight = screenHeight
		else:
			requestedHeight = -1

		# Set the window size request and show it
		self._window.set_size_request( requestedWidth, requestedHeight )
		self._window.show()

		# Handle popup mode
		self._popupMode = popupMode

		# Set up window hide method
		if popupMode == self.TIMEOUT:
			self._timeoutID = gobject.timeout_add( 3000, self._p_onTimeout )
		elif popupMode == self.CLICK:
			self._eventBox.grab_add()


		self._bVisible = True




	def hide(self):
		if self._bVisible:
			if self._popupMode == self.TIMEOUT:
				if self._timeoutID is not None:
					# Destroy the still active timeout
					gobject.source_remove( self._timeoutID )
					self._timeoutID = None
			elif self._popupMode == self.CLICK:
				self._eventBox.grab_remove()

			# Hide the window
			self._window.hide()

			self._bVisible = False




	def _p_onEventBoxEnterNotify(self, widget, event):
		if self._popupMode == self.TIMEOUT:
			if self._timeoutID is not None:
				gobject.source_remove( self._timeoutID )
				self._timeoutID = None
		return True

	def _p_onEventBoxLeaveNotify(self, widget, event):
		if self._popupMode == self.TIMEOUT:
			# The pointer may have entered a widget the grabs events (e.g. a button); this still counts as a 'leave'; check for this
			x, y = self._window.get_pointer()
			if x < 0  or  y < 0  or  x >= self._window.allocation.width  or  y >= self._window.allocation.height:
				# The pointer has definitely left the bounds of the window; remove the window in 0.75 seconds
				self._timeoutID = gobject.timeout_add( 750, self._p_onTimeout )
		return True

	def _p_onEventBoxButtonPress(self, widget, event):
		if self._popupMode == self.CLICK:
			x, y = self._window.get_pointer()
			if x < 0  or  y < 0  or  x >= self._window.allocation.width  or  y >= self._window.allocation.height:
				self.hide()
		return True


	def _p_onTimeout(self):
		if self._popupMode == self.TIMEOUT:
			self._window.hide()
			self._timeoutID = -1
			self._bVisible = False
		return False


	def _p_onTableExpose(self, widget, event):
		if self.shadowType is not None:
			self._table.style.paint_shadow( self._table.window, gtk.STATE_NORMAL, self.shadowType, event.area, self._table, 'gsculpt_popup_window', 0, 0, -1, -1 )



	def _p_isVisible(self):
		return self._bVisible

	def _p_setContents(self, contents):
		if contents is not None:
			self._contentsWrapper.add( contents )
			self._contents = contents
		else:
			if self._contents is not None:
				self._contentsWrapper.remove( self._contents )

	def _p_getWindow(self):
		return self._window

	contents = property( None, _p_setContents )
	bVisible = property( _p_isVisible, None )
	window = property( _p_getWindow, None )





if __name__ == '__main__':
	def onPopup(widget):
		popupWindow.popupBelow( popupButton, PopupWindow.CLICK )

	def onDelete(widget, event):
		gtk.main_quit()

	popupWindow = PopupWindow()
	contents = gtk.Label( 'Hi' )
	contents.show()
	popupWindow.contents = contents

	popupButton = gtk.Button( 'Popup' )
	popupButton.connect( 'clicked', onPopup )
	popupButton.show()

	window = gtk.Window( gtk.WINDOW_TOPLEVEL )
	window.set_border_width( 20 )
	window.add( popupButton )
	window.show()
	window.connect( 'delete-event', onDelete )

	gtk.main()



