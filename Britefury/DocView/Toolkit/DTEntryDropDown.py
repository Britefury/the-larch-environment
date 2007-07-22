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

from Britefury.Math.Math import Colour3f, Vector2

from Britefury.UI.PopupWindow import PopupWindow

from Britefury.DocView.Toolkit.DTLabel import DTLabel
from Britefury.DocView.Toolkit.DTEntry import DTEntry
from Britefury.DocView.Toolkit.DTBorder import DTBorder
from Britefury.DocView.Toolkit.DTBox import DTBox
from Britefury.DocView.Toolkit.DTPopupDocument import DTPopupDocument
from Britefury.DocView.Toolkit.DTDirection import DTDirection



class DTEntryDropDown (DTPopupDocument):
	activateSignal = ClassSignal()
	cancelSignal = ClassSignal()


	def __init__(self, entryText='', bShowLabel=False, labelText='', labelMarkup=None, labelFont=None):
		super( DTEntryDropDown, self ).__init__( False )

		self.label = DTLabel( text=labelText, markup=labelMarkup, font=labelFont, hAlign=DTLabel.HALIGN_LEFT )
		self.entry = DTEntry( text=entryText )
		self.entry.returnSignal.chainConnect( self.activateSignal )


		self._box = DTBox( direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_LEFT )

		if bShowLabel:
			self._box.append( self.label )
		self._box.append( self.entry )

		self._border = DTBorder( 2.0, 2.0, 2.0, 2.0 )
		self._border.child = self._box

		self.child = self._border


	def showAt(self, widget, posInWidget):
#		self._p_refreshContents()

		# Refresh
		self.popupAtWidget( widget, posInWidget, Vector2( 300.0, 50.0 ), DTPopupDocument.APP_CONTROLLED )

		self.entry.grabFocus()



	def _o_draw(self, context):
		# Background
		context.rectangle( 0.5, 0.5, self._allocation.x - 1.0, self._allocation.y - 1.0 )

		# Border
		context.set_line_width( 1.0 )
		context.set_source_rgb( 0.0, 0.0, 0.0 )
		context.stroke()



	def _o_onEscapeClose(self):
		self.cancelSignal.emit()




if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocView.Toolkit.DTLabel import DTLabel
	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()

	class MyLabel (DTLabel):
		def _o_onButtonDown(self, localPos, button, state):
			super( MyLabel, self )._o_onButtonDown( localPos, button, state )
			if button == 1:
				entryDropDown.showAt( self, Vector2( 0.0, self._allocation.y ) )
				return True
			else:
				return False


	entryDropDown = DTEntryDropDown( '', True, 'Text:' )



	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	label1 = MyLabel( 'Click me' )

	doc.child = label1


	box = gtk.VBox()
	box.pack_start( doc )
	box.show_all()

	window.add( box )
	window.show()

	gtk.main()
