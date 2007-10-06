##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Math.Math import BBox2, Point2, Vector2, Xform2

from Britefury.DocPresent.Toolkit.DTWidget import DTWidget
from Britefury.DocViewHelper.DocViewHelper import DocViewBoxTable



class DTContainer (DTWidget):
	class ChildEntry (object):
		def __init__(self, child, xform=None):
			if xform is None:
				xform = Xform2()

			self.child = child
			self.xform = xform
			self.invXform = xform.inverse()
			self.box = BBox2()

			self._xPos = 0.0
			self._width = 0.0
			self._yPos = 0.0
			self._height = 0.0


		def isPointInContainerSpaceWithinBounds(self, p):
			return self.box.contains( p )

		def containerToChildSpace(self, p):
			return p * self.invXform

		def containerToChildSpace(self, p):
			return p * self.invXform





	def __init__(self, backgroundColour=None):
		super( DTContainer, self ).__init__()

		self._childEntries = []
		self._childToEntry = {}
		self._entryToChildId = {}
		self._childIdToEntry = {}
		self._widgetBoxTable = DocViewBoxTable()
		self._pressGrabChildEntry = None
		self._pressGrabButton = None
		self._pointerChildEntry = None
		self._pointerDndChildEntry = None
		self._backgroundColour = backgroundColour



	def setBackgroundColour(self, colour):
		self._backgroundColour = colour
		self._o_queueFullRedraw()

	def getBackgroundColour(self):
		return self._backgroundColour




	def hasChild(self, child):
		for entry in self._childEntries:
			if child is entry.child:
				return True
		return False


	def _f_getChildTransformRelativeToAncestor(self, child, ancestor, x):
		entry = self._childToEntry[child]
		localX = x * entry.xform
		return self.getTransformRelativeToAncestor( ancestor, localX )

	def _f_getChildPointRelativeToAncestor(self, child, ancestor, point):
		entry = self._childToEntry[child]
		localPoint = point * entry.xform
		return self.getPointRelativeToAncestor( ancestor, localPoint )



	def _o_registerChildEntry(self, childEntry):
		child = childEntry.child

		self._childToEntry[child] = childEntry

		child._f_unparent()

		child._f_setParent( self, self._document )

		if self._realiseContext is not None:
			child._f_evRealise( self._realiseContext, self._pangoContext )

		return childEntry


	def _o_unregisterChildEntry(self, childEntry):
		child = childEntry.child

		if self._realiseContext is not None:
			child._f_evUnrealise()

		child._f_setParent( None, None )

		del self._childToEntry[child]

		try:
			childId = self._entryToChildId[childEntry]
		except KeyError:
			pass
		else:
			self._widgetBoxTable.removeWidgetBox( childId )
			del self._entryToChildId[childEntry]
			del self._childIdToEntry[childId]



	def _f_removeChild(self, child):
		assert False, 'abstract'



	def _o_onLeaveIntoChild(self, localPos, child):
		pass

	def _o_onEnterFromChild(self, localPos, child):
		pass

	def _o_onChildResizeRequest(self, child):
		pass




	def _f_refreshScale(self, scale, rootScale):
		for entry in self._childEntries:
			entry.child._f_setScale( 1.0, rootScale )


	def _o_allocateChildX(self, child, localPositionX, localWidth):
		assert isinstance( localPositionX, float )  and  isinstance( localWidth, float )
		childWidth = localWidth / child._scale
		child._f_allocateX( childWidth )
		entry = self._childToEntry[child]

		entry._xPos = localPositionX
		entry._width = localWidth



	def _o_allocateChildY(self, child, localPositionY, localHeight):
		assert isinstance( localPositionY, float )  and  isinstance( localHeight, float )
		childHeight = localHeight / child._scale
		child._f_allocateY( childHeight )
		entry = self._childToEntry[child]

		entry._yPos = localPositionY
		entry._height = localHeight

		xform = Xform2( child._scale, Vector2( entry._xPos, entry._yPos ) )
		entry.xform = xform
		entry.invXform = xform.inverse()
		entry.box = BBox2( Point2( entry._xPos, entry._yPos ), Point2( entry._xPos + entry._width, entry._yPos + entry._height ) )

		try:
			childId = self._entryToChildId[entry]
		except KeyError:
			childId = self._widgetBoxTable.addWidgetBox( entry.box )
			self._entryToChildId[entry] = childId
			self._childIdToEntry[childId] = entry
		else:
			self._widgetBoxTable.setWidgetBox( childId, entry.box )



	def _o_drawBackground(self, context):
		if self._backgroundColour is not None:
			# Background
			context.rectangle( 0.0, 0.0, self._allocation.x, self._allocation.y )
			context.set_source_rgb( self._backgroundColour.r, self._backgroundColour.g, self._backgroundColour.b )
			context.fill()



	def _f_childResizeRequest(self, child):
		self._o_onChildResizeRequest( child )

	def _f_childRedrawRequest(self, child, childPos, childSize):
		entry = self._childToEntry[child]
		localPos = childPos * entry.xform
		localSize = childSize * entry.xform
		self._o_queueRedraw( localPos, localSize )


	def _f_childGrabFocus(self, child):
		if self._parent is not None:
			self._parent._f_childGrabFocus( child )

	def _f_childUngrabFocus(self, child):
		if self._parent is not None:
			self._parent._f_childUngrabFocus( child )





	def _f_evDndButtonDown(self, localPos, button, state):
		widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
		if widgetId != -1:
			childEntry = self._childIdToEntry[widgetId]
			dndSource = childEntry.child._f_evDndButtonDown( childEntry.containerToChildSpace( localPos ), button, state )
			if dndSource is not None:
				return dndSource
			else:
				return self._o_onDndButtonDown( localPos, button, state )
		else:
			return None


	def _f_evDndButtonUp(self, localPos, button, state, dndSource, dndBeginData):
		widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
		if widgetId != -1:
			childEntry = self._childIdToEntry[widgetId]
			bDropped = childEntry.child._f_evDndButtonUp( childEntry.containerToChildSpace( localPos ), button, state, dndSource, dndBeginData )
			if bDropped:
				return True
			else:
				return self._o_onDndButtonUp( localPos, button, state, dndSource, dndBeginData )
		else:
			return False


	def _f_evDndMotion(self, localPos, dndButton, state, dndSource, dndBeginData, dndCache):
		widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
		if widgetId != -1:
			childEntry = self._childIdToEntry[widgetId]
			dndDest = childEntry.child._f_evDndMotion( childEntry.containerToChildSpace( localPos ), dndButton, state, dndSource, dndBeginData, dndCache )
			if dndDest is not None:
				return dndDest
			else:
				return self._o_onDndMotion( localPos, dndButton, state, dndSource, dndBeginData, dndCache )
		else:
			return None


	def _f_evButtonDown(self, localPos, button, state):
		if self._pressGrabChildEntry is None:
			widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
			if widgetId != -1:
				childEntry = self._childIdToEntry[widgetId]
				bProcessed = childEntry.child._f_evButtonDown( childEntry.containerToChildSpace( localPos ), button, state )
				if bProcessed:
					self._pressGrabChildEntry = childEntry
					self._pressGrabButton = button
					return True
			if self._pressGrabChildEntry is None:
				return self._o_onButtonDown( localPos, button, state )
		else:
			return self._pressGrabChildEntry.child._f_evButtonDown( self._pressGrabChildEntry.containerToChildSpace( localPos ), button, state )


	def _f_evButtonDown2(self, localPos, button, state):
		if self._pressGrabChildEntry is not None:
			return self._pressGrabChildEntry.child._f_evButtonDown2( self._pressGrabChildEntry.containerToChildSpace( localPos ), button, state )
		else:
			return self._o_onButtonDown2( localPos, button, state )


	def _f_evButtonDown3(self, localPos, button, state):
		if self._pressGrabChildEntry is not None:
			return self._pressGrabChildEntry.child._f_evButtonDown3( self._pressGrabChildEntry.containerToChildSpace( localPos ), button, state )
		else:
			return self._o_onButtonDown3( localPos, button, state )


	def _f_evButtonUp(self, localPos, button, state):
		if self._pressGrabChildEntry is not None:
			if button == self._pressGrabButton:
				self._pressGrabButton = None
				childSpacePos = self._pressGrabChildEntry.containerToChildSpace( localPos )
				if not self._pressGrabChildEntry.isPointInContainerSpaceWithinBounds( localPos ):
					self._pressGrabChildEntry.child._f_evLeave( childSpacePos )
				bResult = self._pressGrabChildEntry.child._f_evButtonUp( childSpacePos, button, state )
				savedPressGrabChildEntry = self._pressGrabChildEntry
				self._pressGrabChildEntry = None
				if localPos.x >= 0.0  and  localPos.x <= self.allocation.x  and  localPos.y >= 0.0  and  localPos.y <= self.allocation.y:
					widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
					if widgetId != -1:
						childEntry = self._childIdToEntry[widgetId]
						if childEntry is not savedPressGrabChildEntry:
							childEntry.child._f_evEnter( childEntry.containerToChildSpace( localPos ) )
						self._pointerChildEntry = childEntry
					else:
						self._pointerChildEntry = None
						self._o_onEnter( localPos )
				return bResult
			else:
				return self._pressGrabChildEntry.child._f_evButtonUp( self._pressGrabChildEntry.containerToChildSpace( localPos ), button, state )
		else:
			return self._o_onButtonUp( localPos, button, state )


	def _f_evMotion(self, localPos):
		if self._pressGrabChildEntry is not None:
			self._pressGrabChildEntry.child._f_evMotion( self._pressGrabChildEntry.containerToChildSpace( localPos ) )
		else:
			oldPointerChildEntry = self._pointerChildEntry
			if self._pointerChildEntry is not None:
				if not self._pointerChildEntry.isPointInContainerSpaceWithinBounds( localPos ):
					self._pointerChildEntry.child._f_evLeave( self._pointerChildEntry.containerToChildSpace( localPos ) )
					self._pointerChildEntry = None
				else:
					self._pointerChildEntry.child._f_evMotion( self._pointerChildEntry.containerToChildSpace( localPos ) )

			if self._pointerChildEntry is None:
				widgetId = self._widgetBoxTable.getWidgetAtPoint( localPos )
				if widgetId != -1:
					childEntry = self._childIdToEntry[widgetId]
					childEntry.child._f_evEnter( childEntry.containerToChildSpace( localPos ) )
					self._pointerChildEntry = childEntry

			if oldPointerChildEntry is None  and  self._pointerChildEntry is not None:
				self._o_onLeaveIntoChild( localPos, self._pointerChildEntry.child )
			elif oldPointerChildEntry is not None  and  self._pointerChildEntry is None:
				self._o_onEnterFromChild( localPos, oldPointerChildEntry.child )
		self._o_onMotion( localPos )


	def _f_evEnter(self, localPos):
		self._o_onEnter( localPos )
		for childEntry in reversed( self._childEntries ):
			if childEntry.isPointInContainerSpaceWithinBounds( localPos ):
				childEntry.child._f_evEnter( childEntry.containerToChildSpace( localPos ) )
				self._pointerChildEntry = childEntry
				self._o_onLeaveIntoChild( localPos, childEntry.child )
				break


	def _f_evLeave(self, localPos):
		if self._pressGrabChildEntry is None:
			if self._pointerChildEntry is not None:
				self._pointerChildEntry.child._f_evLeave( self._pointerChildEntry.containerToChildSpace( localPos ) )
				self._o_onEnterFromChild( localPos, self._pointerChildEntry.child )
				self._pointerChildEntry = None
		self._o_onLeave( localPos )


	def _f_evScroll(self, scroll):
		if self._pressGrabChildEntry is not None:
			self._pressGrabChildEntry.child._f_evScroll( scroll )
		elif self._pointerChildEntry is not None:
			self._pointerChildEntry.child._f_evScroll( scroll )
		self._o_onScroll( scroll )


	def _f_evRealise(self, context, pangoContext):
		super( DTContainer, self )._f_evRealise( context, pangoContext )
		for entry in self._childEntries:
			entry.child._f_evRealise( context, pangoContext )


	def _f_evUnrealise(self):
		super( DTContainer, self )._f_evUnrealise()
		for entry in self._childEntries:
			entry.child._f_evUnrealise()


	def _f_draw(self, context, areaBox):
		super( DTContainer, self )._f_draw( context, areaBox )
		self._o_drawBackground( context )
		widgets = self._widgetBoxTable.getIntersectingWidgetList( areaBox )
		for childId in widgets:
			entry = self._childIdToEntry[childId]
			context.save()
			context.translate( entry.xform.translation.x, entry.xform.translation.y )
			context.scale( entry.xform.scale, entry.xform.scale )
			entry.child._f_draw( context, areaBox * entry.invXform )
			context.restore()



	def _f_setDocument(self, document):
		super( DTContainer, self )._f_setDocument( document )

		for childEntry in self._childEntries:
			childEntry.child._f_setDocument( document )




	backgroundColour = property( getBackgroundColour, setBackgroundColour )

