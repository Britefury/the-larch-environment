##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import Point2, Vector2

from Britefury.DocPresent.Toolkit.DTCursorEntity import DTCursorEntity
from Britefury.DocPresent.Toolkit.DTContainer import DTContainer



class DTScript (DTContainer):
	SUBSCRIPT = 0
	SUPERSCRIPT = 1

	def __init__(self, mode, spacing=1.0, backgroundColour=None):
		super( DTScript, self ).__init__( backgroundColour )

		self._mode = mode
		self._leftChild = None
		self._leftChildRequisition = Vector2()
		self._rightChild = None
		self._rightChildRequisition = Vector2()
		self._overlap = 0.0
		self._spacing=1.0

		self._childScale = 1.0



	def getLeftChild(self):
		return self._leftChild

	def setLeftChild(self, child):
		if child is not self._leftChild:
			if child is not None:
				child._f_unparent()
			if self._leftChild is not None:
				entry = self._childToEntry[self._leftChild]
				self._childEntries.remove( entry )
				self._o_unregisterChildEntry( entry )
				DTCursorEntity.remove( self._leftChild.getFirstCursorEntity(), self._leftChild.getLastCursorEntity() )
			self._leftChild = child
			if self._leftChild is not None:
				entry = self.ChildEntry( self._leftChild )
				self._childEntries.append( entry )
				self._o_registerChildEntry( entry )
				DTCursorEntity.splice( self._f_getPrevCursorEntityBeforeChild( self._leftChild ), self._f_getNextCursorEntityAfterChild( self._leftChild ), self._leftChild.getFirstCursorEntity(), self._leftChild.getLastCursorEntity() )

			self._o_queueResize()


	def getRightChild(self):
		return self._rightChild

	def setRightChild(self, child):
		if child is not self._rightChild:
			if child is not None:
				child._f_unparent()
			if self._rightChild is not None:
				entry = self._childToEntry[self._rightChild]
				self._childEntries.remove( entry )
				self._o_unregisterChildEntry( entry )
				DTCursorEntity.remove( self._rightChild.getFirstCursorEntity(), self._rightChild.getLastCursorEntity() )
			self._rightChild = child
			if self._rightChild is not None:
				entry = self.ChildEntry( self._rightChild )
				self._childEntries.append( entry )
				self._o_registerChildEntry( entry )
				DTCursorEntity.splice( self._f_getPrevCursorEntityBeforeChild( self._rightChild ), self._f_getNextCursorEntityAfterChild( self._rightChild ), self._rightChild.getFirstCursorEntity(), self._rightChild.getLastCursorEntity() )

			self._o_queueResize()


	def getChildScale(self):
		return self._childScale

	def setChildScale(self, childScale):
		self._childScale = childScale
		self._o_queueResize()


	def _f_removeChild(self, child):
		if child is self._leftChild:
			self.setLeftChild( None )
		elif child is self._rightChild:
			self.setRightChild( None )
		else:
			raise ValueError, 'cannot remove child'




	def _o_getRequiredWidth(self):
		if self._leftChild is not None:
			self._leftChildRequisition.x = self._leftChild._f_getRequisitionWidth()
		else:
			self._leftChildRequisition.x = 0.0

		if self._rightChild is not None:
			self._rightChildRequisition.x = self._rightChild._f_getRequisitionWidth()
		else:
			self._rightChildRequisition.x = 0.0

		return self._leftChildRequisition.x + self._rightChildRequisition.x + self._spacing


	def _o_getRequiredHeight(self):
		if self._leftChild is not None:
			self._leftChildRequisition.y = self._leftChild._f_getRequisitionHeight()
		else:
			self._leftChildRequisition.y = 0.0

		if self._rightChild is not None:
			self._rightChildRequisition.y = self._rightChild._f_getRequisitionHeight()
		else:
			self._rightChildRequisition.y = 0.0

		self._overlap = min( self._leftChildRequisition.y, self._rightChildRequisition.y ) / 3.0

		return self._leftChildRequisition.y + max( self._overlap, self._rightChildRequisition.y - self._overlap )


	def _o_onAllocateX(self, allocation):
		padding = max( ( allocation - self._requiredSize.x ) * 0.5, 0.0 )
		x = padding
		if self._leftChild is not None:
			self._o_allocateChildX( self._leftChild, x, self._leftChildRequisition.x )
			x += self._leftChildRequisition.x

		if self._rightChild is not None:
			x += self._spacing
			self._o_allocateChildX( self._rightChild, x, self._rightChildRequisition.x )


	def _o_onAllocateY(self, allocation):
		padding = max( ( allocation - self._requiredSize.y ) * 0.5, 0.0 )

		if self._mode == self.SUPERSCRIPT:
			rightY = padding
			leftY = padding + self._rightChildRequisition.y - self._overlap
		elif self._mode == self.SUBSCRIPT:
			leftY = padding
			rightY = padding + self._leftChildRequisition.y - self._overlap

		if self._leftChild is not None:
			self._o_allocateChildY( self._leftChild, leftY, self._leftChildRequisition.y )

		if self._rightChild is not None:
			self._o_allocateChildY( self._rightChild, rightY, self._rightChildRequisition.y )



	def _o_onChildResizeRequest(self, child):
		self._o_queueResize()


	def _f_refreshScale(self, scale, rootScale):
		if self._leftChild is not None:
			self._leftChild._f_setScale( self._childScale, rootScale * self._childScale )
		if self._rightChild is not None:
			self._rightChild._f_setScale( self._childScale, rootScale * self._childScale )




	#
	# CURSOR NAVIGATION METHODS
	#

	def _o_getFirstCursorEntity(self):
		if self._leftChild is not None:
			return self._leftChild.getFirstCursorEntity()
		elif self._rightChild is not None:
			return self._rightChild.getFirstCursorEntity()
		else:
			return None


	def _o_getLastCursorEntity(self):
		if self._rightChild is not None:
			return self._rightChild.getLastCursorEntity()
		elif self._leftChild is not None:
			return self._leftChild.getLastCursorEntity()
		else:
			return None
		
		
	
	def _o_getPrevCursorEntityBeforeChild(self, child):
		if child is self._rightChild  and  self._leftChild is not None:
			return self._leftChild.getLastCursorEntity()
		else:
			return None
	
		
	def _o_getNextCursorEntityAfterChild(self, child):
		if child is self._leftChild  and  self._rightChild is not None:
			return self._rightChild.getFirstCursorEntity()
		else:
			return None
	
	



	leftChild = property( getLeftChild, setLeftChild )
	rightChild = property( getRightChild, setRightChild )
	childScale = property( getChildScale, setChildScale )






if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
	from Britefury.DocPresent.Toolkit.DTDocument import DTDocument
	import cairo
	from Britefury.Math.Math import Colour3f
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

	label1 = DTLabel( 'x' )
	label2 = DTLabel( '2' )
	label3 = DTLabel( '\'Hi\'' )

	script1 = DTScript( DTScript.SUPERSCRIPT )
	script1.leftChild = label1
	script1.rightChild = label2
	script2 = DTScript( DTScript.SUBSCRIPT )
	script2.leftChild = script1
	script2.rightChild = label3

	doc.child = script2


	window.add( doc )
	window.show_all()

	gtk.main()

