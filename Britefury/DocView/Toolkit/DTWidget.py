##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy


from Britefury.Math.Math import Point2, Vector2

from Britefury.Kernel.Abstract import *

from Britefury.DocView.Toolkit.DTKeyEvent import DTKeyEvent




class DTWidgetKeyHandlerInterface (object):
	# Return False if the widget should handle the event as normal
	# Return True if the widget should ignore it (let the key handler take care of it)
	@abstractmethod
	def _f_handleKeyPress(self, entry, keyPressEvent):
		pass





class DTWidget (object):
	def __init__(self):
		super( DTWidget, self ).__init__()

		self._parent = None
		self._bHasFocus = False
		self._bFocusGrabbed = False
		self._realiseContext = None
		self._bResizeQueued = False
		self._sizeRequest = Vector2( -1, -1 )
		self._allocation = Vector2()



	def isRealised(self):
		return self._realiseContext is not None


	def getSizeRequest(self):
		return self._sizeRequest

	def setSizeRequest(self, size):
		if size != self._sizeRequest:
			self._sizeRequest = copy( size )
			self._o_queueResize()


	def getAllocation(self):
		return self._allocation



	def grabFocus(self):
		if not self._bFocusGrabbed:
			self._bFocusGrabbed = True
			if self._parent is not None:
				self._parent._f_childGrabFocus( self )

	def ungrabFocus(self):
		if self._bFocusGrabbed:
			self._bFocusGrabbed = False
			if self._parent is not None:
				self._parent._f_childUngrabFocus( self )


	def _f_clearFocusGrab(self):
		self._bFocusGrabbed = False




	def _o_onButtonDown(self, localPos, button, state):
		return False

	def _o_onButtonDown2(self, localPos, button, state):
		return False

	def _o_onButtonDown3(self, localPos, button, state):
		return False

	def _o_onButtonUp(self, localPos, button, state):
		return False

	def _o_onMotion(self, localPos):
		pass

	def _o_onEnter(self, localPos):
		pass

	def _o_onLeave(self, localPos):
		pass

	def _o_onScroll(self, scroll):
		pass

	def _o_onKeyPress(self, keyEvent):
		pass

	def _o_onKeyRelease(self, keyEvent):
		pass

	def _o_onGainFocus(self):
		self._bHasFocus = True

	def _o_onLoseFocus(self):
		self._bHasFocus = False

	def _o_onRealise(self, context):
		pass

	def _o_onUnrealise(self):
		pass

	def _o_draw(self, context):
		pass


	def _o_getRequiredWidth(self):
		pass

	def _o_getRequiredHeight(self):
		pass

	def _o_onAllocateX(self, allocation):
		pass

	def _o_onAllocateY(self, allocation):
		pass






	def _o_queueResize(self):
		if not self._bResizeQueued  and  self._realiseContext is not None:
			if self._parent is not None:
				self._parent._f_childResizeRequest( self )
			self._bResizeQueued = True


	def _o_queueRedraw(self, localPos, localSize):
		if self._realiseContext is not None:
			self._parent._f_childRedrawRequest( self, localPos, localSize )

	def _o_queueFullRedraw(self):
		self._o_queueRedraw( Point2(), self._allocation )



	def _f_evButtonDown(self, localPos, button, state):
		return self._o_onButtonDown( localPos, button, state )

	def _f_evButtonDown2(self, localPos, button, state):
		return self._o_onButtonDown2( localPos, button, state )

	def _f_evButtonDown3(self, localPos, button, state):
		return self._o_onButtonDown3( localPos, button, state )

	def _f_evButtonUp(self, localPos, button, state):
		return self._o_onButtonUp( localPos, button, state )

	def _f_evMotion(self, localPos):
		self._o_onMotion( localPos )

	def _f_evEnter(self, localPos):
		self._o_onEnter( localPos )

	def _f_evLeave(self, localPos):
		self._o_onLeave( localPos )

	def _f_evScroll(self, scroll):
		self._o_onScroll( scroll )

	def _f_evRealise(self, context):
		self._realiseContext = context
		self._o_onRealise( context )
		self._o_queueResize()
		if self._bFocusGrabbed  and  self._parent is not None:
			self._parent._f_childGrabFocus( self )

	def _f_evUnrealise(self):
		if self._bFocusGrabbed  and  self._parent is not None:
			self._parent._f_childUngrabFocus( self )
		self._o_onUnrealise()
		self._realiseContext = None

	def _f_draw(self, context, areaBox):
		self._o_draw( context )



	def _f_getRequisitionWidth(self):
		requisition = self._o_getRequiredWidth()
		if self._sizeRequest.x != -1:
			requisition = self._sizeRequest.x
		return requisition

	def _f_getRequisitionHeight(self):
		requisition = self._o_getRequiredHeight()
		if self._sizeRequest.y != -1:
			requisition = self._sizeRequest.y
		return requisition

	def _f_allocateX(self, allocation):
		self._allocation.x = allocation
		self._o_onAllocateX( allocation )

	def _f_allocateY(self, allocation):
		self._bResizeQueued = False
		self._allocation.y = allocation
		self._o_onAllocateY( allocation )




	def _p_getParent(self):
		return self._parent

	def _f_setParent(self, parent):
		self._parent = parent


	def _f_unparent(self):
		if self._parent is not None:
			self._parent._f_removeChild( self )



	def __copy__(self):
		return self



	parent = property( _p_getParent )
	allocation = property( getAllocation )
	sizeRequest = property( getSizeRequest, setSizeRequest )

