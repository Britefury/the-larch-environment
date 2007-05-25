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


from Britefury.Util.SignalSlot import *

from Britefury.DocView.Toolkit.DTEntryLabel import DTEntryLabel
from Britefury.DocView.CellEdit.DVCBasicWidgetSingleCellEdit import DVCBasicWidgetSingleCellEdit



class DVCStringCellEditEntryLabel (DVCBasicWidgetSingleCellEdit):
	"""String cell editor"""
	finishSignal = ClassSignal()


	__valueclass__ = str


	def __init__(self, regexp=None):
		super( DVCStringCellEditEntryLabel, self ).__init__()
		self.entry = DTEntryLabel( regexp=regexp )
		self.entry.finishEditingSignal.connect( self._p_onEntry )


	def startEditing(self):
		self.entry.startEditing()


	def finishEditing(self):
		self.entry.finishEditing()


	def startEditingOnLeft(self):
		self.entry.startEditingOnLeft()

	def startEditingOnRight(self):
		self.entry.startEditingOnRight()


	def _o_setWidgetValue(self, value):
		self._bIgnoreCheck = True
		self.entry.text = value
		self._bIgnoreCheck = False


	def _p_onEntry(self, entry, text):
		assert self._cell is not None
		if self._cell.bLiteral  and  text != self._cell.literalValue:
			self._o_blockCell( text )
			self._cell.literalValue = text
			self._o_unblockCell()
		self.finishSignal.emit( self, text )



	def _p_setKeyHandler(self, handler):
		self.entry.keyHandler = handler


	keyHandler = property( None, _p_setKeyHandler )









if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk
	import cairo

	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	from Britefury.Cell.Cell import StringCell
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

	cell = StringCell( '' )
	cell.literalValue = 'Hello world'

	entry = DVCStringCellEditEntryLabel()
	entry.attachCell( cell )

	doc.child = entry.entry


	window.add( doc )
	window.show()

	gtk.main()


