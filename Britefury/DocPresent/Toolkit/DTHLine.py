##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )

import cairo
import pango
import pangocairo

from Britefury.Math.Math import Colour3f, Vector2

from Britefury.DocPresent.Toolkit.DTSimpleStaticWidget import DTSimpleStaticWidget



class DTHLine (DTSimpleStaticWidget):
	def __init__(self, thickness=1.0, colour=Colour3f( 0.0, 0.0, 0.0)):
		super( DTHLine, self ).__init__()

		self._thickness = thickness
		self._colour = colour

		self._o_queueResize()



	def setThickness(self, thickness):
		self._thickness = thickness
		self._o_queueResize()

	def getThickness(self):
		return self._thickness


	def setColour(self, colour):
		self._colour = colour
		self._o_queueFullRedraw()

	def getColour(self):
		return self._colour



	def _o_draw(self, context):
		super( DTHLine, self )._o_draw( context )
		context.save()
		self._o_clipIfAllocationInsufficient( context )
		context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
		y = self._thickness * 0.5
		context.set_line_width( self._thickness )
		context.move_to( 0.0, y )
		context.line_to( self._allocation.x, y )
		context.stroke()
		context.restore()


	def _o_getRequiredWidth(self):
		return 0.0

	def _o_getRequiredHeight(self):
		return self._thickness



	thickness = property( getThickness, setThickness )
	colour = property( getColour, setColour )










if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocPresent.Toolkit.DTBox import DTBox
	from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	import cairo
	from Britefury.Math.Math import Colour3f
	import traceback

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()


	def makeButton(text, response):
		button = gtk.Button( text )
		button.connect( 'clicked', response )
		button.show()
		return button



	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	vbox = DTBox( direction=DTDirection.TOP_TO_BOTTOM, minorDirectionAlignment=DTBox.ALIGN_EXPAND )
	vbox.spacing = 10.0
	vbox.append( DTHLine() )
	vbox.append( DTHLine() )
	doc.child = vbox


	window.add( doc )
	window.show()

	gtk.main()
