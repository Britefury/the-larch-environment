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

from Britefury.DocView.DVNode import DVNode

from Britefury.DocPresent.Toolkit.DTActiveBorder import DTActiveBorder



class DVBorderNode (DVNode):
	def _o_styleSheetChanged(self):
		super( DVBorderNode, self )._o_styleSheetChanged()
		self.widget.leftMargin = self._styleSheet.leftMargin
		self.widget.rightMargin = self._styleSheet.rightMargin
		self.widget.topMargin = self._styleSheet.topMargin
		self.widget.bottomMargin = self._styleSheet.bottomMargin

		self.widget.borderWidth = self._styleSheet.borderWidth
		self.widget.highlightedBorderWidth = self._styleSheet.highlightedBorderWidth

		self.widget.borderColour = self._styleSheet.borderColour
		self.widget.prelitBorderColour = self._styleSheet.prelitBorderColour
		self.widget.highlightedBorderColour = self._styleSheet.highlightedBorderColour

		self.widget.backgroundColour = self._styleSheet.backgroundColour
		self.widget.highlightedBackgroundColour = self._styleSheet.highlightedBackgroundColour




	def __init__(self, docNode, view, docNodeKey):
		super( DVBorderNode, self ).__init__( docNode, view, docNodeKey )
		self.widget = DTActiveBorder()
		self.widget.keyHandler = self
		self.widget.topMargin = self.widget.bottomMargin = 1.0
		self.widget.contextSignal.connect( self._p_onContext )
		#self.widget.addDndSourceOp( self._cvNodeDndOp )
		#self.widget.dndBeginCallback = self._dndBeginCallback
		#self.widget.dndDragToCallback = self._dndDragToCallback
		self._pieMenu = self._o_createPieMenu()


	def _p_onContext(self, widget):
		if self._pieMenu is not None:
			# Get the default display, and the pointer position
			display = gtk.gdk.display_get_default()
			screen, x, y, mods = display.get_pointer()
			self._pieMenu.popup( x, y, True )


	def _o_createPieMenu(self):
		return None



	def _dndBeginCallback(self, dndSource, localpos, button, state):
		return self


	def _dndDragToCallback(self, dndSource, dndDest, localPos, button, state):
		return self