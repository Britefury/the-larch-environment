##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy


from Britefury.Math.Math import Point2, Vector2, Xform2, BBox2

from Britefury.Kernel.Abstract import abstractmethod

from Britefury.DocPresent.Toolkit.DTKeyEvent import DTKeyEvent




class DTWidgetKeyHandlerInterface (object):
	# Return False if the widget should handle the event as normal
	# Return True if the widget should ignore it (let the key handler take care of it)
	@abstractmethod
	def _f_handleKeyPress(self, widget, keyPressEvent):
		pass





class DTWidget (object):
	"""DTWidget Drag and drop information


	To enable drag and drop:

		To allow a widget to be dragged:

			call:
				widget.addDndSourceOp( operation ) to add a drag and drop operation
				remove using widget.removeDndSourceOp( operation )

		To allow drops:

			call:
				widget.addDndDestOp( operation ) to add an operation
				remove using widget.removeDndDestOp( operation )


		Operations should be comparable using ==

		They are some sort of application defined identifier.

		DnD can only happen if the source and destination widgets have at lease ONE operation in common with one another.

		If a widget has no DnD operations defined, its parents/ancestors are checked until a widget that supports DnD is found.



	To work the DnD, set the foloowing callbacks:

	dndBeginCallback  :   f(dndSource, localpos, button, state)											->		Dnd begin data or None
		dndSource: source widget
		localPos: pointer position in source widget
		button: button used to initiate drag
		state: modifier key state at drag
	Called when a drag is initiated from a widget.

	dndMotionCallback  :   f(dndSource, dndDest, dndBeginData, localpos, button, state)					->		None
		dndSource: source widget
		dndDest: destination widget
		dndBeginData: the result of the dndBeginCallback invoked against the DnD source, or None if dndBeginCallback not set
		localPos: pointer position in destination widet
		button: button used to initiate drag
		state: modifier key state at drag
	Called when the pointer hovers over a potential target.

	dndCanDropFromCallback  :   f(dndSource, dndDest, dndBeginData, button, state)						->		True or False			All drops will be accepted if not defined
		dndSource: source widget
		dndDest: destination widget
		dndBeginData: the result of the dndBeginCallback invoked against the DnD source, or None if dndBeginCallback not set
		button: button used to initiate drag
		state: modifier key state at drop
	Called to determine if a target will accept a drop from the source.

	dndDragToCallback  :   f(dndSource, dndDest, localPos, button, state)									->		Dnd data or None		Can be undefined
		dndSource: source widget
		dndDest: destination widget
		localPos: pointer position in source widget
		button: button used to initiate drag
		state: modifier key state at drag
	Invoked against source to get drag data to send to the destination.

	dndDropFromCallback  :   f(dndSource, dndDest, dndData, localPos, button, state)						->		None				Can be undefined
		dndSource: source widget
		dndDest: destination widget
		dndData: result of the dndDragToCallback invoked against the DnD source, or None if dndDragToCallback not set
		localPos: pointer position in destination widet
		button: button used to initiate drag
		state: modifier key state at drop
	Invoked against the destination to send drag data.
	"""

	def __init__(self):
		super( DTWidget, self ).__init__()

		self._parent = None
		self._document = None
		self._bHasFocus = False
		self._bFocusGrabbed = False
		self._realiseContext = None
		self._pangoContext = None
		self._bResizeQueued = False
		self._sizeRequest = Vector2( -1, -1 )
		self._scale = 1.0
		self._rootScale = 1.0
		self._allocation = Vector2()
		self._requiredSize = Vector2()

		self._waitingImmediateEvents = []

		self._dndLocalPos = None
		self._dndButton = None
		self._dndState = None

		self.dndBeginCallback = None
		self.dndMotionCallback = None
		self.dndCanDropFromCallback = None
		self.dndDragToCallback = None
		self.dndDropFromCallback = None


		self._dndSourceOps = []
		self._dndDestOps = []



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


	def getBoundingBox(self):
		return BBox2( Point2(), Point2( self._allocation ) )


	def getTransformRelativeToDocument(self, x=Xform2()):
		return self.getTransformRelativeToAncestor( None, x )

	def getTransformRelativeToAncestor(self, ancestor, x=Xform2()):
		if ancestor is self:
			return x
		elif self._parent is not None:
			return self._parent._f_getChildTransformRelativeToAncestor( self, ancestor, x )
		else:
			if ancestor is not None:
				raise ValueError, '@ancestor is not and ancestor of the target widget'
			return x

	def getTransformRelativeTo(self, toWidget, x=Xform2()):
		myXform = self.getTransformRelativeToDocument()
		toWidgetXform = toWidget.getTransformRelativeToDocument()
		return myXform * toWidgetXform.inverse()


	def getPointRelativeToDocument(self, point):
		return self.getPointRelativeToAncestor( None, point )

	def getPointRelativeToAncestor(self, ancestor, point):
		if ancestor is self:
			return point
		elif self._parent is not None:
			return self._parent._f_getChildPointRelativeToAncestor( self, ancestor, point )
		else:
			if ancestor is not None:
				raise ValueError, '@ancestor is not and ancestor of the target widget'
			return point

	def getPointRelativeTo(self, toWidget, point):
		pointInDoc = self.getPointRelativeToDocument( point )
		toWidgetXform = toWidget.getTransformRelativeToDocument()
		return pointInDoc * toWidgetXform.inverse()





	def getDocument(self):
		return self._document



	def grabFocus(self):
		if not self._bFocusGrabbed:
			self._bFocusGrabbed = True
			if self._document is not None:
				self._document._f_widgetGrabFocus( self )

	def ungrabFocus(self):
		if self._bFocusGrabbed:
			self._bFocusGrabbed = False
			if self._document is not None:
				self._document._f_widgetUngrabFocus( self )


	def _f_clearFocusGrab(self):
		self._bFocusGrabbed = False



	def addDndSourceOp(self, op):
		if op in self._dndSourceOps:
			raise ValueError, 'dnd op already in source ops list'
		self._dndSourceOps.append( op )

	def removeDndSourceOp(self, op):
		if op not in self._dndSourceOps:
			raise ValueError, 'dnd op not in source ops list'
		self._dndSourceOps.remove( op )


	def addDndDestOp(self, op):
		if op in self._dndDestOps:
			raise ValueError, 'dnd op already in dest ops list'
		self._dndDestOps.append( op )

	def removeDndSourceOp(self, op):
		if op not in self._dndDestOps:
			raise ValueError, 'dnd op not in dest ops list'
		self._dndDestOps.remove( op )




	def queueImmediateEvent(self, f):
		if self._document is None:
			self._waitingImmediateEvents.append( f )
		else:
			self._document.queueImmediateEvent( f )



	def _o_onDndButtonDown(self, localPos, button, state):
		if len( self._dndSourceOps ) > 0:
			self._dndLocalPos = localPos
			self._dndButton = button
			self._dndState = state
			return self
		else:
			return None

	def _o_onDndButtonUp(self, localPos, button, state, dndSource, dndBeginData):
		self._dndLocalPos = None
		self._dndButton = None
		self._dndState = None
		for op in self._dndDestOps:
			if op in dndSource._dndSourceOps:
				bCanDrop = self._f_dndCanDropFrom( dndSource, dndBeginData, button, state )
				if bCanDrop:
					dndData = dndSource._f_dndDragTo( self )
					self._f_dndDropFrom( dndSource, dndData, localPos, button, state )
					return True
		return False


	def _o_onDndMotion(self, localPos, dndButton, state, dndSource, dndBeginData, dndCache):
		key = self, state
		try:
			bCanDrop = dndCache[key]
		except KeyError:
			bCanDrop = False
			for op in self._dndDestOps:
				if op in dndSource._dndSourceOps:
					bCanDrop = self._f_dndCanDropFrom( dndSource, dndBeginData, dndButton, state )
			dndCache[key] = bCanDrop

		if bCanDrop:
			if self.dndMotionCallback is not None:
				self.dndMotionCallback( dndSource, self, dndBeginData, localPos, dndButton, state )
			return self
		else:
			return None


	def _o_onDndBegin(self, localPos, button, state):
		if self.dndBeginCallback is not None:
			return self.dndBeginCallback( self, localPos, button, state )
		else:
			return None


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

	def _o_onRealise(self, context, pangoContext):
		pass

	def _o_onUnrealise(self):
		pass

	def _o_draw(self, context):
		pass


	def _o_onSetScale(self, scale, rootScale):
		pass

	def _o_getRequiredWidth(self):
		pass

	def _o_getRequiredHeight(self):
		pass

	def _o_onAllocateX(self, allocation):
		pass

	def _o_onAllocateY(self, allocation):
		pass




	def _o_clip(self, context):
		context.new_path()
		context.rectangle( 0, 0, self._allocation.x, self._allocation.y )
		context.clip()

	def _o_clipIfAllocationInsufficient(self, context):
		if self._requiredSize.x > self._allocation.x  or  self._requiredSize.y > self._allocation.y:
			self._o_clip( context )


	def _o_queueResize(self):
		if not self._bResizeQueued  and  self._realiseContext is not None:
			if self._parent is not None:
				self._parent._f_childResizeRequest( self )
			self._bResizeQueued = True


	def _o_queueRedraw(self, localPos, localSize):
		if self._realiseContext is not None  and  self._parent is not None:
			self._parent._f_childRedrawRequest( self, localPos, localSize )

	def _o_queueFullRedraw(self):
		self._o_queueRedraw( Point2(), self._allocation )




	def _f_evDndButtonDown(self, localPos, button, state):
		return self._o_onDndButtonDown( localPos, button, state )

	def _f_evDndButtonUp(self, localPos, button, state, dndSource, dndBeginData):
		return self._o_onDndButtonUp( localPos, button, state, dndSource, dndBeginData )

	def _f_evDndMotion(self, localPos, dndButton, state, dndSource, dndBeginData, dndCache):
		return self._o_onDndMotion( localPos, dndButton, state, dndSource, dndBeginData, dndCache )

	def _f_evDndLeave(self):
		return self._o_onDndLeave()



	def _f_evDndBegin(self):
		return self._o_onDndBegin( self._dndLocalPos, self._dndButton, self._dndState )


	def _f_dndCanDropFrom(self, dndSource, dndBeginData, button, state):
		if self.dndCanDropFromCallback is not None:
			return self.dndCanDropFromCallback( dndSource, self, dndBeginData, button, state )
		else:
			return True

	def _f_dndDragTo(self, dndDest):
		if self.dndDragToCallback is not None:
			return self.dndDragToCallback( self, dndDest, self._dndLocalPos, self._dndButton, self._dndState )
		else:
			return None

	def _f_dndDropFrom(self, dndSource, dndData, localPos, button, state):
		if self.dndDropFromCallback is not None:
			return self.dndDropFromCallback( dndSource, self, dndData, localPos, button, state )


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

	def _f_evRealise(self, context, pangoContext):
		self._realiseContext = context
		self._pangoContext = pangoContext
		self._o_onRealise( context, pangoContext )
		self._o_queueResize()
		if self._bFocusGrabbed  and  self._document is not None:
			self._document._f_widgetAcquireFocus( self )

	def _f_evUnrealise(self):
		if self._bFocusGrabbed  and  self._document is not None:
			self._document._f_widgetRelinquishFocus( self )
		self._o_onUnrealise()
		self._realiseContext = None

	def _f_draw(self, context, areaBox):
		self._o_draw( context )




	def _f_refreshScale(self, scale, rootScale):
		self._o_onSetScale( scale, rootScale )


	def _f_setScale(self, scale, rootScale):
		if scale != self._scale  or  rootScale != self._rootScale:
			self._scale = scale
			self._rootScale = rootScale
			self._o_onSetScale( scale, rootScale )
		self._f_refreshScale( scale, rootScale )

	def _f_getRequisitionWidth(self):
		requisition = self._o_getRequiredWidth()  *  self._scale
		if self._sizeRequest.x != -1:
			requisition = self._sizeRequest.x
		self._requiredSize.x = requisition
		return requisition

	def _f_getRequisitionHeight(self):
		requisition = self._o_getRequiredHeight()  *  self._scale
		if self._sizeRequest.y != -1:
			requisition = self._sizeRequest.y
		self._requiredSize.y = requisition
		return requisition

	def _f_allocateX(self, allocation):
		self._allocation.x = allocation
		self._o_onAllocateX( allocation )

	def _f_allocateY(self, allocation):
		self._bResizeQueued = False
		self._allocation.y = allocation
		self._o_onAllocateY( allocation )




	@abstractmethod
	def getFirstCursorEntity(self):
		pass
	
	@abstractmethod
	def getLastCursorEntity(self):
		pass


	def getPrevCursorEntity(self):
		first = self.getFirstCursorEntity()
		if first is not None:
			return first.prev
		else:
			if self._parent is not None:
				return self._parent._f_getPrevCursorEntityBeforeChild( self )
			else:
				return None

	def getNextCursorEntity(self):
		last = self.getLastCursorEntity()
		if last is not None:
			return last.next
		else:
			if self._parent is not None:
				return self._parent._f_getNextCursorEntityAfterChild( self )
			else:
				return None




	def _p_getParent(self):
		return self._parent

	def _f_setParent(self, parent, document):
		self._parent = parent
		self._f_setDocument( document )


	def _f_unparent(self):
		if self._parent is not None:
			self._parent._f_removeChild( self )
		self._docuement = None


	def _f_setDocument(self, document):
		self._document = document
		if self._document is not None:
			for event in self._waitingImmediateEvents:
				self._document.queueImmediateEvent( event )
			self._waitingImmediateEvents = []


	def _dbg_getWidgetsWithFocus(self):
		if self._bHasFocus:
			return [ self ]
		else:
			return []

	def _dbg_getWidgetsWithFocusGrab(self):
		if self._bFocusGrabbed:
			return [ self ]
		else:
			return []



	def __copy__(self):
		raise TypeError, 'widgets cannot be copied'


	def __deepcopy__(self):
		raise TypeError, 'widgets cannot be copied'


	parent = property( _p_getParent )
	bRealised = property( isRealised )
	document = property( getDocument )
	allocation = property( getAllocation )
	sizeRequest = property( getSizeRequest, setSizeRequest )

