##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import weakref

from Britefury.Util.SignalSlot import *

from Britefury.Math.Math import Colour3f, Point2, Vector2

from Britefury.DocPresent.Toolkit.DTDocument import DTDocument 
from Britefury.DocPresent.Toolkit.DTBin import DTBin 



class DTHighlight (DTBin):
	class _HighlightStack (object):
		def __init__(self):
			self._stack = []
			
		def _onEnter(self, highlight):
			prev = self._getCurrentHighlight()
			self._stack.append( highlight )
			current = self._getCurrentHighlight()
			if prev is not current:
				if prev is not None:
					prev._unhighlightBackground()
				if current is not None:
					current._highlightBackground()
			
		def _onLeave(self, highlight):
			prev = self._getCurrentHighlight()
			self._stack.remove( highlight )
			current = self._getCurrentHighlight()
			if prev is not current:
				if prev is not None:
					prev._unhighlightBackground()
				if current is not None:
					current._highlightBackground()
					
				
		def __onStateKeyChange(self):
			current = self._getCurrentHighlight()
			for h in self._stack:
				if h is not current:
					h._unhighlightBackground()
			if current is not None:
				current._highlightBackground()
		
		def _onStateKeyPress(self, event):
			self.__onStateKeyChange()
			
		def _onStateKeyRelease(self, event):
			self.__onStateKeyChange()
			
			
			
		def _getCurrentHighlight(self):
			for h in reversed( self._stack ):
				if h._testState():
					return h
			return None

		
			
	class _HighlightStackTable (object):
		def __init__(self):
			self._docToStack = weakref.WeakKeyDictionary()
			
		def _onEnter(self, highlight):
			self.__getStack( highlight )._onEnter( highlight )
			
		def _onLeave(self, highlight):
			self.__getStack( highlight )._onLeave( highlight )
			
			
		def __getStack(self, highlight):
			document = highlight._document
			try:
				return self._docToStack[document]
			except KeyError:
				stack = DTHighlight._HighlightStack()
				document.addStateKeyListener( stack )
				self._docToStack[document] = stack
				return stack
	
	
	contextSignal = ClassSignal()
	
	
	_highlightStackTable = _HighlightStackTable()



	def __init__(self, stateMask=0, stateTest=0, borderOffset=3.0, borderWidth=1.0, highlightBorderWidth=1.0, borderColour=None, highlightBorderColour=Colour3f( 0.6, 0.6, 0.6 ), backgroundColour=None, highlightBackgroundColour=Colour3f( 0.95, 1.0, 0.95 )):
		super( DTHighlight, self ).__init__()

		self._stateMask = DTDocument.stateValueCoerce( stateMask )
		self._stateTest = DTDocument.stateValueCoerce( stateTest )
		self._borderOffset = borderOffset
		self._borderWidth = borderWidth
		self._highlightBorderWidth = highlightBorderWidth
		self._borderColour = borderColour
		self._highlightBorderColour = highlightBorderColour
		self._highlightBackgroundColour = highlightBackgroundColour
		self._bHighlighted = False
		self._bHighlightBackground = False



	def setStateMask(self, stateMask):
		self._stateMask = DTDocument.stateValueCoerce( stateMask )
		
	def getStateMask(self):
		return self._stateMask

	
	def setStateTest(self, stateTest):
		self._stateTest = DTDocument.stateValueCoerce( stateTest )
		
	def getStateTest(self):
		return self._stateTest

	
	def setBorderOffset(self, offset):
		self._borderOffset = offset
		self._o_queueFullRedraw()

	def getBorderOffset(self):
		return self._borderOffset


	def setBorderWidth(self, width):
		self._borderWidth = width
		self._o_queueFullRedraw()

	def getBorderWidth(self):
		return self._borderWidth


	def setHighlightBorderWidth(self, width):
		self._highlightBorderWidth = width
		self._o_queueFullRedraw()

	def getHighlightBorderWidth(self):
		return self._highlightBorderWidth


	def setBorderColour(self, colour):
		self._borderColour = colour
		self._o_queueFullRedraw()

	def getBorderColour(self):
		return self._borderColour


	def setHighlightBorderColour(self, colour):
		self._highlightBorderColour = colour
		self._o_queueFullRedraw()

	def getHighlightBorderColour(self):
		return self.highlightBorderColour


	def setHighlightBackgroundColour(self, colour):
		self._highlightBackgroundColour = colour
		self._o_queueFullRedraw()

	def getHighlightBackgroundColour(self):
		return self._highlightBackgroundColour
	
	

	def _highlightBackground(self):
		if not self._bHighlightBackground:
			self._bHighlightBackground = True
			self._o_queueFullRedraw()
	
	def _unhighlightBackground(self):
		if self._bHighlightBackground:
			self._bHighlightBackground = False
			self._o_queueFullRedraw()
	

	def _testState(self):
		if self._document is not None:
			return self._document._pointerState & self._stateMask  ==  self._stateTest
		else:
			return False

	def _o_queueFullRedraw(self):
		offset = self._borderOffset
		self._o_queueRedraw( Point2( -offset, -offset ), self._allocation  +  Vector2( offset*2.0, offset*2.0 ) )
		
		
	def _o_onEnter(self, localPos):
		super( DTHighlight, self )._o_onEnter( localPos )
		self._highlightStackTable._onEnter( self )
		self._bHighlighted = True
		self._o_queueFullRedraw()

	def _o_onLeave(self, localPos):
		super( DTHighlight, self )._o_onLeave( localPos )
		self._highlightStackTable._onLeave( self )
		self._bHighlighted = False
		self._o_queueFullRedraw()
		


	def _o_drawBackground(self, context):
		bHighlighted = self._bHighlighted
		if bHighlighted:
			b = self._highlightBorderWidth
			borderColour = self._highlightBorderColour
		else:
			b = self._borderWidth
			borderColour = self._borderColour
			
		if self._bHighlightBackground:
			backgroundColour = self._highlightBackgroundColour
		else:
			backgroundColour = self._backgroundColour

			
		offset = self._borderOffset


		if backgroundColour is not None:
			context.rectangle( -offset, -offset, self._allocation.x + offset * 2.0, self._allocation.y + offset * 2.0 )
			context.set_source_rgb( backgroundColour.r, backgroundColour.g, backgroundColour.b )
			context.fill()

		if borderColour is not None:
			context.rectangle( b * 0.5 - offset, b * 0.5 - offset, self._allocation.x + offset * 2.0 - b, self._allocation.y + offset * 2.0 - b )
			context.set_line_width( b )
			context.set_source_rgb( borderColour.r, borderColour.g, borderColour.b )
			context.stroke()


		
	stateMask = property( getStateMask, setStateMask )
	stateTest = property( getStateTest, setStateTest )
	borderOffset = property( getBorderOffset, setBorderOffset )
	borderWidth = property( getBorderWidth, setBorderWidth )
	highlightBorderWidth = property( getHighlightBorderWidth, setHighlightBorderWidth )
	borderColour = property( getBorderColour, setBorderColour )
	highlightBorderColour = property( getHighlightBorderColour, setHighlightBorderColour )
	highlightBackgroundColour = property( getHighlightBackgroundColour, setHighlightBackgroundColour )

