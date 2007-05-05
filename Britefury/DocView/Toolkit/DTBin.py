##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2
from Britefury.DocView.Toolkit.DTContainer import DTContainer



class DTBin (DTContainer):
	def __init__(self):
		super( DTBin, self ).__init__()

		self._child = None
		self._childRequisition = Vector2()



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


	def _f_removeChild(self, child):
		assert child is self._child, 'cannot remove child'
		self.setChild( None )




	def _o_getRequiredWidth(self):
		self._childRequisition.x = self._child._f_getRequisitionWidth()
		return self._childRequisition.x

	def _o_getRequiredHeight(self):
		self._childRequisition.y = self._child._f_getRequisitionHeight()
		return self._childRequisition.y


	def _o_onAllocateX(self, allocation):
		self._o_allocateChildX( self._child, 0.0, allocation, 1.0 )

	def _o_onAllocateY(self, allocation):
		self._o_allocateChildY( self._child, 0.0, allocation, 1.0 )


	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()




	child = property( getChild, setChild )

