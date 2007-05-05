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


from Britefury.Math.Math import Vector2
from Britefury.Util.SignalSlot import Signal


# Win32 BUG WORK-AROUNDS
# The Win32 version of cairo raises a MemoryError if an empty string is passed to either
# Context.text_extents() or Context.show_text()

def textExtents(context, text):
	if text == '':
		return 0, 0, 0, 0, 0, 0
	else:
		return context.text_extents( text )


def showText(context, text):
	if text != '':
		context.show_text( text )





class DTFont (object):
	changedSignal = Signal()


	def __init__(self, name='Sans', slant=cairo.FONT_SLANT_NORMAL, weight=cairo.FONT_WEIGHT_NORMAL, size=14.0):
		self._name = name
		self._slant = slant
		self._weight = weight
		self._size = size



	def select(self, context):
		context.select_font_face( self._name, self._slant, self._weight )
		context.set_font_size( self._size )


	@staticmethod
	def getTextExtents(context, text):
		xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, text )
		return Vector2( xBearing, yBearing ), Vector2( width, height ), Vector2( xAdvance, yAdvance )

	@staticmethod
	def getTextSpace(context, text):
		xBearing, yBearing, width, height, xAdvance, yAdvance = textExtents( context, text )
		return Vector2( xAdvance, height )


	@staticmethod
	def showText(context, text):
		showText( context, text )



	def getName(self):
		return self._name

	def setName(self, name):
		self._name = name
		self.changedSignal.emit()


	def getSlant(self):
		return self._slant

	def setSlant(self, slant):
		self._slant = slant
		self.changedSignal.emit()


	def getWeight(self):
		return self._weight

	def setWeight(self, weight):
		self._weight = weight
		self.changedSignal.emit()


	def getSize(self):
		return self._size

	def setSize(self, size):
		self._size = size
		self.changedSignal.emit()



	def getFontExtents(self, context):
		ascent, descent, height, maxXAdvance, maxYAdvance = context.font_extents()
		return ascent, descent, height, Vector2( maxXAdvance, maxYAdvance )

	def getFontSpace(self, context):
		ascent, descent, height, maxXAdvance, maxYAdvance = context.font_extents()
		return height



	name = property( getName, setName )
	slant = property( getSlant, setSlant )
	weight = property( getWeight, setWeight )
	size = property( getSize, setSize )
