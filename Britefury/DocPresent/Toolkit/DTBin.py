##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer



class DTBin (DTContainer):
	def __init__(self, backgroundColour=None):
		super( DTBin, self ).__init__( backgroundColour )

		self._child = None
		self._childRequisition = Vector2()
		self._childBaseline = None
		self._childScale = 1.0



	def getChild(self):
		return self._child

	def setChild(self, child):
		if child is not self._child:
			if child is not None:
				child._f_unparent()
			if self._child is not None:
				entry = self._childToEntry[self._child]
				self._childEntries.remove( entry )
				self._o_unregisterChildEntry( entry )
			self._child = child
			if self._child is not None:
				entry = self.ChildEntry( self._child )
				self._childEntries.append( entry )
				self._o_registerChildEntry( entry )

			self._o_queueResize()


	def getChildScale(self):
		return self._childScale

	def setChildScale(self, childScale):
		self._childScale = childScale
		self._o_queueResize()


	def _f_removeChild(self, child):
		assert child is self._child, 'cannot remove child'
		self.setChild( None )




	def _o_getRequiredWidth(self):
		if self._child is not None:
			self._childRequisition.x = self._child._f_getRequisitionWidth()
		else:
			self._childRequisition.x = 0.0
		return self._childRequisition.x

	def _o_getRequiredHeightAndBaseline(self):
		if self._child is not None:
			self._childRequisition.y, self._childBaseline = self._child._f_getRequisitionHeightAndBaseline()
		else:
			self._childRequisition.y = 0.0
			self._childBaseline = None
		return self._childRequisition.y, self._childBaseline


	def _o_onAllocateX(self, allocation):
		if self._child is not None:
			self._o_allocateChildX( self._child, 0.0, allocation )

	def _o_onAllocateY(self, allocation):
		if self._child is not None:
			self._o_allocateChildY( self._child, 0.0, allocation )


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()


	def _f_refreshScale(self, scale, rootScale):
		if self._child is not None:
			self._child._f_setScale( self._childScale, rootScale * self._childScale )



	#
	# FOCUS NAVIGATION METHODS
	#
	
	def horizontalNavigationList(self):
		if self._child is not None:
			return [ self._child ]
		else:
			return []

	def verticalNavigationList(self):
		return []


	

	child = property( getChild, setChild )
	childScale = property( getChildScale, setChildScale )

