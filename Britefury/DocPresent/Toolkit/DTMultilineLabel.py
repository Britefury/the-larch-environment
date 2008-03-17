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



class DTMultilineLabel (DTSimpleStaticWidget):
	HALIGN_LEFT = 0
	HALIGN_CENTRE = 1
	HALIGN_RIGHT = 2

	VALIGN_TOP = 0
	VALIGN_CENTRE = 1
	VALIGN_RIGHT = 2


	def __init__(self, text='', markup=None, font=None, colour=Colour3f( 0.0, 0.0, 0.0), hAlign=HALIGN_CENTRE, vAlign=VALIGN_CENTRE):
		super( DTMultilineLabel, self ).__init__()

		if font is None:
			font = 'Sans 11'

		self._text = text
		self._markup = markup

		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		self._layout = None
		self._layoutContext = None

		self._bLayoutNeedsRefresh = True
		self._colour = colour
		self._hAlign = hAlign
		self._vAlign = vAlign
		self._textPosition = Vector2()
		self._textSize = Vector2()

		self._o_queueResize()



	def setText(self, text):
		self._text = text
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getText(self):
		return self._text


	def setMarkup(self, markup):
		self._markup = markup
		self._text = markup
		self._bLayoutNeedsRefresh = True
		self._o_queueResize()

	def getMarkup(self):
		return self._markup


	def setFont(self, font):
		self._fontString = font
		self._fontDescription = pango.FontDescription( font )
		self._font = None
		self._o_queueResize()

	def getFont(self):
		return self._fontString


	def setColour(self, colour):
		self._colour = colour
		self._o_queueFullRedraw()

	def getColour(self):
		return self._colour



	def setHAlign(self, hAlign):
		self._hAlign = hAlign
		self._o_refreshTextPosition()
		self._o_queueFullRedraw()

	def getHAlign(self):
		return self._hAlign



	def setVAlign(self, vAlign):
		self._vAlign = vAlign
		self._o_refreshTextPosition()
		self._o_queueFullRedraw()

	def getVAlign(self):
		return self._vAlign



	def _p_refreshLayout(self):
		if self._bLayoutNeedsRefresh  and  self._layout is not None:
			self._layout.set_font_description( self._fontDescription )
			self._layout.set_alignment( pango.ALIGN_LEFT )
			if self._markup is not None:
				self._layout.set_markup( self._markup )
			else:
				self._layout.set_text( self._text )
			self._bLayoutNeedsRefresh = False


	def _o_onRealise(self, context, pangoContext):
		super( DTMultilineLabel, self )._o_onRealise( context, pangoContext )
		if context is not self._layoutContext:
			self._layoutContext = context
			self._layout = context.create_layout()
			self._layout.set_font_description( self._fontDescription )
			self._bLayoutNeedsRefresh = True



	def _o_draw(self, context):
		super( DTMultilineLabel, self )._o_draw( context )
		self._o_clipIfAllocationInsufficient( context )
		self._p_refreshLayout()
		context.set_source_rgb( self._colour.r, self._colour.g, self._colour.b )
		context.move_to( self._textPosition.x, self._textPosition.y )
		context.update_layout( self._layout )
		context.show_layout( self._layout )


	def _o_onSetScale(self, scale, rootScale):
		context = self._realiseContext
		context.save()
		context.scale( rootScale, rootScale )
		context.update_layout( self._layout )
		context.restore()


	def _o_getRequiredWidth(self):
		self._layout.set_width( -1 )
		self._p_refreshLayout()
		return self._layout.get_pixel_size()[0]  +  2.0

	def _o_getRequiredHeightAndBaseline(self):
		return self._layout.get_pixel_size()[1]  +  2.0,  0.0


	def _o_onAllocateX(self, allocation):
		super( DTMultilineLabel, self )._o_onAllocateX( allocation )

		self._layout.set_width( int( allocation * pango.SCALE ) )


	def _o_onAllocateY(self, allocation):
		super( DTMultilineLabel, self )._o_onAllocateY( allocation )
		self._p_refreshLayout()

		self._textSize = Vector2( *self._layout.get_pixel_size() )

		self._o_refreshTextPosition()


	def _o_refreshTextPosition(self):
		if self._hAlign == self.HALIGN_LEFT:
			x = 0.0
		elif self._hAlign == self.HALIGN_CENTRE:
			x = ( self._allocation.x - self._textSize.x ) * 0.5
		elif self._hAlign == self.HALIGN_RIGHT:
			x = self._allocation.x - self._textSize.x

		if self._vAlign == self.VALIGN_TOP:
			y = 0.0
		elif self._vAlign == self.VALIGN_CENTRE:
			y = ( self._allocation.y - self._textSize.y ) * 0.5
		elif self._vAlign == self.VALIGN_BOTTOM:
			y = self._allocation.y - self._textSize.y

		self._textPosition = Vector2( x, y )  +  Vector2( 1.0, 1.0 )


	def _p_onFontChanged(self):
		self._o_queueResize()



	text = property( getText, setText )
	markup = property( getMarkup, setMarkup )
	font = property( getFont, setFont )
	colour = property( getColour, setColour )
	hAlign = property( getHAlign, setHAlign )
	vAlign = property( getVAlign, setVAlign )







multilineText = """from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection
from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
import cairo
from Britefury.Math.Math import Colour3f
import traceback"""



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

	vbox = DTBox( DTDirection.TOP_TO_BOTTOM, alignment=DTBox.ALIGN_EXPAND )
	vbox.spacing = 10.0
	vbox.backgroundColour = Colour3f( 0.8, 0.8, 0.8 )

	vbox.append( DTMultilineLabel( multilineText ) )


	doc.child = vbox


	window.add( doc )
	window.show()

	gtk.main()
