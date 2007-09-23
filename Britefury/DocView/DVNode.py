##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

import traceback

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel import KMeta

from Britefury.Sheet.Sheet import Sheet, SheetClass, FunctionRefField, FunctionField

from Britefury.DocViewBehavior.DVBMovementBehavior import DVBMovementBehavior
from Britefury.DocViewBehavior.DVBDeleteNodeBehavior import DVBDeleteNodeBehavior

from Britefury.DocView.DocView import DocView
from Britefury.DocView.MoveFocus import MoveFocus

from Britefury.DocPresent.Toolkit.DTWidget import *
from Britefury.DocPresent.Toolkit.DTBorder import *
from Britefury.DocPresent.Toolkit.DTLabel import *




class DVChildNodeSlotFunctionField (FunctionRefField):
	def __init__(self, doc=''):
		super( DVChildNodeSlotFunctionField, self ).__init__( doc )
		self._behaviors = []


	def _f_metaMember_initClass(self):
		super( DVChildNodeSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node is self._f_getImmutableValueFromInstance( instance )


	def setBehaviors(self, behaviors):
		self._behaviors = behaviors






class DVChildNodeListSlotFunctionField (FunctionField):
	def __init__(self, doc=''):
		super( DVChildNodeListSlotFunctionField, self ).__init__( doc )
		self._behaviors = []


	def _f_metaMember_initClass(self):
		super( DVChildNodeListSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node in self._f_getImmutableValueFromInstance( instance )


	def setBehaviors(self, behaviors):
		self._behaviors = behaviors







class DVNodeClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		cls.behaviors = []
		cls._nodeSlots = []

		for base in clsBases:
			if hasattr( base, 'behaviors' ):
				cls.behaviors = base.behaviors  +  [ b   for b in cls.behaviors   if b not in base.behaviors ]
			if hasattr( base, '_nodeSlots' ):
				cls._nodeSlots = base._nodeSlots  +  [ s   for s in cls._nodeSlots   if s not in base._nodeSlots ]
		try:
			myBehaviors = clsDict['behaviors']
		except KeyError:
			myBehaviors = []
		cls.behaviors = myBehaviors  +  [ b   for b in cls.behaviors   if b not in myBehaviors ]


		super( DVNodeClass, cls ).__init__( clsName, clsBases, clsDict )


		try:
			docNodeClass = clsDict['docNodeClass']
		except KeyError:
			pass
		else:
			if isinstance( docNodeClass, tuple )  or  isinstance( docNodeClass, list ):
				for c in docNodeClass:
					DocView._nodeClassTable[c] = cls
			else:
				DocView._nodeClassTable[docNodeClass] = cls



class DVNode (Sheet, DTWidgetKeyHandlerInterface):
	__metaclass__ = DVNodeClass


	class _CouldNotFindNextChildError (Exception):
		pass


	behaviors = [ DVBMovementBehavior(), DVBDeleteNodeBehavior() ]



	def __init__(self, docNode, view, parentDocNode, index):
		super( DVNode, self ).__init__()
		self.docNode = docNode
		self._view = view
		self._index = None
		self._bDeleting = False
		self._parent = None
		self._parentDocNode = parentDocNode
		self._index = index


	def _f_setParentAndIndex(self, parent, parentDocNode, index):
		self._parent = parent
		self._parentDocNode = parentDocNode
		self._index = index



	def refresh(self):
		self.refreshCell


	def refreshFromParent(self):
		self._parent.refresh()



	def isDescendantOf(self, node):
		n = self
		while n is not None:
			if n is node:
				return True
			n = n._parent
		return False



	def _o_moveFocus(self, moveFocus):
		if moveFocus == MoveFocus.LEFT:
			self.cursorLeft( True )
		else:
			self.cursorRight( True )


	def deleteChild(self, child, moveFocus):
		pass


	def _o_deleteNode(self, moveFocus):
		if self._parent is not None:
			self._parent.deleteChild( self, moveFocus )


	def deleteNode(self, moveFocus):
		if not self._bDeleting:
			self._bDeleting = True
			self._o_deleteNode( moveFocus )
			self._bDeleting = False



	def horizontalNavigationList(self):
		return []

	def verticalNavigationList(self):
		return []




	def cursorLeft(self, bItemStep=False):
		left = self.getLeafToLeft()
		if left is not None:
			if bItemStep:
				left.makeCurrent()
			else:
				left.startEditingOnRight()


	def cursorRight(self, bItemStep=False):
		right = self.getLeafToRight()
		if right is not None:
			if bItemStep:
				right.makeCurrent()
			else:
				right.startEditingOnLeft()



	def cursorToLeftChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[0].makeCurrent()

	def cursorToRightChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[-1].makeCurrent()


	def cursorToParent(self):
		if self._parent is not None:
			self._parent.makeCurrent()




	def cursorUp(self):
		above = self.getNodeAbove()
		if above is not None:
			cursorPosInAbove = self.widget.getPointRelativeTo( above.widget, self.getCursorPosition() )
			#above.makeCurrent()
			above.startEditingAtPosition( cursorPosInAbove )

	def cursorDown(self):
		below = self.getNodeBelow()
		if below is not None:
			cursorPosInBelow = self.widget.getPointRelativeTo( below.widget, self.getCursorPosition() )
			#below.makeCurrent()
			below.startEditingAtPosition( cursorPosInBelow )







	def _f_commandHistoryFreeze(self):
		self._view._f_commandHistoryFreeze()


	def _f_commandHistoryThaw(self):
		self._view._f_commandHistoryThaw()



	def _p_prevNavListItem(self, navList, item):
		if navList != []:
			try:
				index = navList.index( item )
			except ValueError:
				pass
			else:
				if index > 0:
					return navList[index-1]
		return None

	def _p_nextNavListItem(self, navList, item):
		if navList != []:
			try:
				index = navList.index( item )
			except ValueError:
				pass
			else:
				if index < len( navList ) - 1:
					return navList[index+1]
		return None






	def getLeafToLeft(self):
		if self._parent is not None:
			return self._parent._p_getLeafToLeftFromChild( self )
		else:
			return None

	def getLeafToRight(self):
		if self._parent is not None:
			return self._parent._p_getLeafToRightFromChild( self )
		else:
			return None


	def _p_getLeafToLeftFromChild(self, child):
		navList = self.horizontalNavigationList()
		leftChild = self._p_prevNavListItem( navList, child )
		if leftChild is not None:
			return leftChild.getRightLeaf()
		elif self._parent is not None:
			return self._parent._p_getLeafToLeftFromChild( self )
		else:
			return None

	def _p_getLeafToRightFromChild(self, child):
		navList = self.horizontalNavigationList()
		rightChild = self._p_nextNavListItem( navList, child )
		if rightChild is not None:
			return rightChild.getLeftLeaf()
		elif self._parent is not None:
			return self._parent._p_getLeafToRightFromChild( self )
		else:
			return None


	def getLeftLeaf(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			return navList[0].getLeftLeaf()
		else:
			return self

	def getRightLeaf(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			return navList[-1].getRightLeaf()
		else:
			return self




	def getNodeAbove(self):
		return self.getNodeAboveOrBelow( False )

	def getNodeBelow(self):
		return self.getNodeAboveOrBelow( True )

	def getNodeAboveOrBelow(self, bBelow):
		if self._parent is not None:
			return self._parent._p_getLeafAboveOrBelowFromChild( self, bBelow, self.widget, self.getCursorPosition() )
		else:
			return None

	def _p_getLeafAboveOrBelowFromChild(self, child, bBelow, widget, cursorPosInWidget):
		navList = self.verticalNavigationList()
		targetChild = None
		if bBelow:
			targetChild = self._p_nextNavListItem( navList, child )
		else:
			targetChild = self._p_prevNavListItem( navList, child )
		if targetChild is not None:
			cursorPosInDocSpace = widget.getPointRelativeToDocument( cursorPosInWidget )
			return targetChild._p_getTopOrBottomLeaf( not bBelow, cursorPosInDocSpace )
		elif self._parent is not None:
			return self._parent._p_getLeafAboveOrBelowFromChild( self, bBelow, widget, cursorPosInWidget )
		else:
			return None

	def _p_getTopOrBottomLeaf(self, bBottom, cursorPosInDocSpace):
		navList = self.verticalNavigationList()
		if navList != []:
			if bBottom:
				n = navList[-1]
			else:
				n = navList[0]
			return n._p_getTopOrBottomLeaf( bBottom, cursorPosInDocSpace )
		else:
			navList = self.horizontalNavigationList()
			if navList != []:
				closestDistance = None
				closestNode = None
				for item in navList:
					bounds = item.widget.getBoundingBox()
					lower = item.widget.getPointRelativeToDocument( bounds.getLower() ).x
					upper = item.widget.getPointRelativeToDocument( bounds.getUpper() ).x
					if cursorPosInDocSpace.x >= lower  and  cursorPosInDocSpace.x <= upper:
						return item._p_getTopOrBottomLeaf( bBottom, cursorPosInDocSpace )
					else:
						distance = None
						if cursorPosInDocSpace.x < lower:
							distance = lower - cursorPosInDocSpace.x
						elif cursorPosInDocSpace.x > upper:
							distance = cursorPosInDocSpace.x - upper
						if distance is not None:
							if closestDistance is None  or  distance < closestDistance:
								closestDistance = distance
								closestNode = item

				if closestNode is not None:
					return closestNode._p_getTopOrBottomLeaf( bBottom, cursorPosInDocSpace )
			return self








	def _o_handleKeyPress(self, receivingNodePath, widget, keyPressEvent):
		state = keyPressEvent.state
		keyVal = keyPressEvent.keyVal
		key = keyVal, state
		char = keyPressEvent.keyString

		inputHandler = None

		receivingNodePath = ( self, )  +  receivingNodePath

		# Try to get the node slot input handler
		if len( receivingNodePath ) > 1:
			nodeSlot = None
			for slot in self._nodeSlots:
				if slot._f_containsNode( self, receivingNodePath[1] ):
					for behavior in slot._behaviors:
						if behavior.handleKeyPress( self, receivingNodePath, widget, keyPressEvent ):
							return True


		for behavior in self.behaviors:
			if behavior.handleKeyPress( self, receivingNodePath, widget, keyPressEvent ):
				return True

		# Pass to the parent node
		if self._parent is not None:
			return self._parent._o_handleKeyPress( ( self, ) + receivingNodePath, widget, keyPressEvent )
		else:
			return False


	def _f_handleKeyPress(self, widget, keyPressEvent):
		return self._o_handleKeyPress( (), widget, keyPressEvent)


	def makeCurrent(self):
		self.widget.grabFocus()



	def startEditingOnLeft(self):
		self.makeCurrent()


	def startEditingOnRight(self):
		self.makeCurrent()


	def startEditingAtPosition(self, pos):
		self.makeCurrent()



	def getCursorPosition(self):
		return Point2( self.widget.getAllocation() * 0.5 )



	def getInsertPosition(self, receivingNodePath):
		return 0


	def _o_buildViewForChild(self, docNode, viewNodeClass=None):
		return self._view.buildView( docNode, self, viewNodeClass )

	def _o_getViewNode(self, docNode, viewNodeClass=None):
		return self._view.getViewNodeForDocNode( docNode, viewNodeClass )
