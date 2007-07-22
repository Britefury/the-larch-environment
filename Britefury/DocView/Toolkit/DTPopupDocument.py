##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import math

import pygtk
pygtk.require( '2.0' )
import gtk


from Britefury.UI.SimplePopupWindow import SimplePopupWindow

from Britefury.DocView.Toolkit.DTDocument import DTDocument


class DTPopupDocument (DTDocument):
	TIMEOUT = SimplePopupWindow.TIMEOUT
	APP_CONTROLLED = SimplePopupWindow.APP_CONTROLLED


	def __init__(self, bCanGrabFocus=True):
		super( DTPopupDocument, self ).__init__( bCanGrabFocus )

		self.show()

		self._popupWindow = SimplePopupWindow()
		self._popupWindow.contents = self



	def popupAtWidget(self, widget, posInWidget, size, popupMode):
		widgetDocument = widget.getRootDocument()
		pos = widget.getPositionRelativeToDocument( posInWidget )
		self._popupWindow.popupAtWidget( widgetDocument, int( pos.x ), int( pos.y ), int( math.ceil( size.x ) ), int( math.ceil( size.y ) ), popupMode )

		self._p_popup()


	def popupAtPointer(self, popupMode):
		self._popupWindow.popupAtPointer( popupMode )

		self._p_popup()


	def hide(self):
		self.grab_remove()

		self._popupWindow.hide()





	def isVisible(self):
		return self._popupWindow.bVisible


	def _p_popup(self):
		self.grab_add()
		self.grab_focus()

		gtk.gdk.keyboard_grab( self.window, owner_events=True )




	def _o_handlePopupDocumentKey(self, keyEvent):
		return False


	def _o_handleDocumentKey(self, keyEvent):
		if keyEvent.keyVal == gtk.keysyms.Escape:
			self.hide()
			self._o_onEscapeClose()
			return True
		else:
			return self._o_handlePopupDocumentKey( keyEvent )



	def _o_onEscapeClose(self):
		pass
