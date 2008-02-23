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

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTWidget import DTSimpleStaticWidget



class DTCustomSymbol (DTSimpleStaticWidget):
	HALIGN_LEFT = 0
	HALIGN_CENTRE = 1
	HALIGN_RIGHT = 2

	VALIGN_TOP = 0
	VALIGN_CENTRE = 1
	VALIGN_RIGHT = 2


	def __init__(self, size=11.0, colour=Colour3f( 0.0, 0.0, 0.0), hAlign=HALIGN_CENTRE, vAlign=VALIGN_CENTRE):
		super( DTCustomSymbol, self ).__init__()

		self._size = size
		self._colour = colour
		self._hAlign = hAlign
		self._vAlign = vAlign
		self._symbolPosition = Vector2()
		self._symbolSize = None
		
		self._o_queueResize()



	def setSize(self, size):
		self._size= size
		self._symbolSize = None
		self._o_queueResize()

	def getSize(self):
		return self._size


	def setColour(self, colour):
		self._colour = colour
		self._o_queueFullRedraw()

	def getColour(self):
		return self._colour



	def setHAlign(self, hAlign):
		self._hAlign = hAlign
		self._o_refreshSymbolPosition()
		self._o_queueFullRedraw()

	def getHAlign(self):
		return self._hAlign



	def setVAlign(self, vAlign):
		self._vAlign = vAlign
		self._o_refreshSymbolPosition()
		self._o_queueFullRedraw()

	def getVAlign(self):
		return self._vAlign



	def _o_getSymbolSizeRequest(self):
		raise TypeError, 'abstract'

	def _o_drawSymbol(self, context):
		raise TypeError, 'abstract'




	def _o_draw(self, context):
		super( DTCustomSymbol, self )._o_draw( context )
		context.save()
		self._o_clipIfAllocationInsufficient( context )
		context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
		context.translate( self._symbolPosition.x, self._symbolPosition.y )
		self._o_drawSymbol( context )
		context.restore()


	def _o_getRequiredWidth(self):
		self._o_refreshSymbolSize()
		return self._symbolSize.x + 2.0

	def _o_getRequiredHeight(self):
		self._o_refreshSymbolSize()
		return self._symbolSize.y + 2.0


	def _o_onAllocateY(self, allocation):
		super( DTCustomSymbol, self )._o_onAllocateY( allocation )

		self._o_refreshSymbolPosition()



	def _o_refreshSymbolSize(self):
		if self._symbolSize is None:
			self._symbolSize = self._o_getSymbolSizeRequest()



	def _o_refreshSymbolPosition(self):
		self._o_refreshSymbolSize()

		if self._hAlign == self.HALIGN_LEFT:
			x = 0.0
		elif self._hAlign == self.HALIGN_CENTRE:
			x = ( self._allocation.x - self._symbolSize.x ) * 0.5
		elif self._hAlign == self.HALIGN_RIGHT:
			x = self._allocation.x - self._symbolSize.x

		if self._vAlign == self.VALIGN_TOP:
			y = 0.0
		elif self._vAlign == self.VALIGN_CENTRE:
			y = ( self._allocation.y - self._symbolSize.y ) * 0.5
		elif self._vAlign == self.VALIGN_BOTTOM:
			y = self._allocation.y - self._symbolSize.y

		self._symbolPosition = Vector2( x, y )  +  Vector2( 1.0, 1.0 )



	size = property( getSize, setSize )
	colour = property( getColour, setColour )
	hAlign = property( getHAlign, setHAlign )
	vAlign = property( getVAlign, setVAlign )










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



	class MySymbol (DTCustomSymbol):
		def _o_getSymbolSizeRequest(self):
			return Vector2( self._size, self._size )

		def _o_drawSymbol(self, context):
			context.set_line_width( 1.0 / 11.0 )
			context.scale( self._size, self._size )
			context.move_to( 0.5, 0.0 )
			context.line_to( 0.0, 0.5 )
			context.line_to( 0.5, 1.0 )

			context.move_to( 1.0, 0.0 )
			context.line_to( 0.5, 0.5 )
			context.line_to( 1.0, 1.0 )
			context.stroke()



	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 300, 100 )

	doc = DTDocument()
	doc.show()

	hbox = DTBox()
	hbox.spacing = 10.0
	hbox.append( MySymbol() )
	doc.child = hbox


	window.add( doc )
	window.show()

	gtk.main()
