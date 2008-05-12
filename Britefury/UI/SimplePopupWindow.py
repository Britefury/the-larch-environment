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
import gobject


class SimplePopupWindow (object):
	TIMEOUT = 0
	APP_CONTROLLED = 1


	def __init__(self):
		#Contain the contents in an event box. @table is app paintable.
		#@m_contentsWrapper will ensure that the contents has its own window so that
		#the painting of @table will not obliterate the contents
		self._window = gtk.Window( gtk.WINDOW_POPUP )

		self._timeoutID = None
		self._bVisible = False
		self._popupMode = self.TIMEOUT



	def popupAtWidget(self, document, relativeX, relativeY, width, height, popupMode):
		if not self._bVisible:
			docGtkWidget = document.getGtkWidget()
			# Get the position of the GDK window used for @belowWidget
			srcOriginX, srcOriginY = docGtkWidget.window.get_origin()

			# Get the requested size of the popup window
			winWidth, winHeight = self._window.size_request()

			# Compute the desired position
			posX = srcOriginX  +  relativeX
			posY = srcOriginY  +  relativeY


			# Get the screen from @belowWidget
			screen = docGtkWidget.get_screen()

			# Get the screen dimensions
			screenWidth = screen.get_width()
			screenHeight = screen.get_height()

			# Show the window
			self._p_showWindow( screen, width, height, popupMode )

			# The desired position of the popup window may place it partially offscreen
			if  posX + winWidth  >  screenWidth:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posX = max( screenWidth - winWidth, 0 )

			if  posY + winHeight  >  screenHeight:
				# posX would place the popup window partially offscreen; compute new co-ordinates
				posY = max( screenHeight - winHeight, 0 )

			# Position the window
			self._window.move( int( posX ), int( posY ) )



	def popupAtPointer(self, popupMode, width, height):
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
			self._p_showWindow( screen, width, height, popupMode )




	def _p_showWindow(self, screen, width, height, popupMode):
		# Get the screen dimensions
		screenWidth = screen.get_width()
		screenHeight = screen.get_height()

		# Set the window size request and show it
		self._window.set_size_request( width, height )
		self._window.show()

		# Handle popup mode
		self._popupMode = popupMode

		# Set up window hide method
		if popupMode == self.TIMEOUT:
			self._timeoutID = gobject.timeout_add( 3000, self._p_onTimeout )


		self._bVisible = True




	def hide(self):
		if self._bVisible:
			if self._popupMode == self.TIMEOUT:
				if self._timeoutID is not None:
					# Destroy the still active timeout
					gobject.source_remove( self._timeoutID )
					self._timeoutID = None

			# Hide the window
			self._window.hide()

			self._bVisible = False




	def _p_onTimeout(self):
		if self._popupMode == self.TIMEOUT:
			self._window.hide()
			self._timeoutID = -1
			self._bVisible = False
		return False


	def _p_isVisible(self):
		return self._bVisible

	def _p_setContents(self, contents):
		if contents is not None:
			self._window.add( contents )
			self._contents = contents
		else:
			if self._contents is not None:
				self._window.remove( self._contents )

	def _p_getWindow(self):
		return self._window

	contents = property( None, _p_setContents )
	bVisible = property( _p_isVisible, None )
	window = property( _p_getWindow, None )





if __name__ == '__main__':
	def onPopup(widget):
		popupWindow.popupBelow( popupButton, SimplePopupWindow.TIMEOUT )

	def onDelete(widget, event):
		gtk.main_quit()

	popupWindow = SimplePopupWindow()
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



