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

from Britefury.CodeView.CVNode import *

from Britefury.DocView.Toolkit.DTActiveBorder import DTActiveBorder



class CVBorderNode (CVNode):
	def __init__(self, treeNode, view):
		super( CVBorderNode, self ).__init__( treeNode, view )
		self.widget = DTActiveBorder()
		self.widget.keyHandler = self
		self.widget.topMargin = self.widget.bottomMargin = 1.0
		self.widget.contextSignal.connect( self._p_onContext )
		self._pieMenu = self._o_createPieMenu()


	def _p_onContext(self, widget):
		if self._pieMenu is not None:
			# Get the default display, and the pointer position
			display = gtk.gdk.display_get_default()
			screen, x, y, mods = display.get_pointer()
			self._pieMenu.popup( x, y, True )


	def _o_createPieMenu(self):
		return None