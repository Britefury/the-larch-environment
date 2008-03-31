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

from Britefury.Cell.Cell import RefCell

from Britefury.Sheet.Sheet import Sheet, SheetClass, FunctionRefField, FunctionField

from Britefury.DocViewBehavior.DVBMovementBehavior import DVBMovementBehavior
from Britefury.DocViewBehavior.DVBDeleteNodeBehavior import DVBDeleteNodeBehavior

from Britefury.DocView.DocView import DocView
from Britefury.DocView.MoveFocus import MoveFocus
from Britefury.DocView.DocViewEvent import DocViewEventKey, DocViewEventEmpty, DocViewEventToken

from Britefury.DocPresent.Toolkit.DTWidget import *
from Britefury.DocPresent.Toolkit.DTBorder import *
from Britefury.DocPresent.Toolkit.DTLabel import *




class DVNodeClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		cls.behaviors = []

		for base in clsBases:
			if hasattr( base, 'behaviors' ):
				cls.behaviors = base.behaviors  +  [ b   for b in cls.behaviors   if b not in base.behaviors ]
		try:
			myBehaviors = clsDict['behaviors']
		except KeyError:
			myBehaviors = []
		cls.behaviors = myBehaviors  +  [ b   for b in cls.behaviors   if b not in myBehaviors ]


		super( DVNodeClass, cls ).__init__( clsName, clsBases, clsDict )




class DVNode (Sheet, DTWidgetKeyHandlerInterface):
	__metaclass__ = DVNodeClass


	class _CouldNotFindNextChildError (Exception):
		pass


	#behaviors = [ DVBMovementBehavior(), DVBDeleteNodeBehavior() ]
	behaviors = []




	@FunctionRefField
	def _node_styleSheet(self):
		return self._view._f_getStyleSheet( self._docNodeKey )


	def _o_refreshNode(self):
		styleSheet = self._node_styleSheet
		if styleSheet is not self._styleSheet:
			self._styleSheet = styleSheet
			self._o_styleSheetChanged()


	def _o_reset(self):
		pass


	def _o_styleSheetChanged(self):
		pass



	def __init__(self, docNode, view, docNodeKey):
		super( DVNode, self ).__init__()
		self.docNode = docNode
		self._view = view
		self._bDeleting = False
		self._parent = None
		self._docNodeKey = docNodeKey
		self._styleSheet = None
		self.refreshCell = RefCell()
		self.refreshCell.function = self._o_refreshNode
		


	def _f_setParentAndKey(self, parent, docNodeKey):
		if parent is not self._parent  or  docNodeKey != self._docNodeKey:
			self._parent = parent
			oldKey = self._docNodeKey
			self._docNodeKey = docNodeKey
			self._view._f_nodeChangeKey( self, oldKey, docNodeKey )
			# Force refreshCell to require recomputation due to potential style sheet change
			self.refreshCell.function = self._o_refreshNode
		self._o_reset()
		
		
	def _o_resetRefreshCell(self):
		self.refreshCell.function = self._o_refreshNode



	def getDocView(self):
		return self._view

	def getDocNodeKey(self):
		return self._docNodeKey




	def refresh(self):
		self.refreshCell.immutableValue


	def refreshFromParent(self):
		self._parent.refresh()



	def getParentNodeView(self):
		return self._parent

	def isDescendantOf(self, node):
		n = self
		while n is not None:
			if n is node:
				return True
			n = n._parent
		return False




	def getChildViewNodeForChildDocNode(self, childDocNode):
		if childDocNode is not None:
			raise KeyError
		else:
			return None


	def isForDocNode(self, docNode):
		return docNode is self.docNode


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
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			if bItemStep:
				left.makeCurrent()
			else:
				left.startEditingOnRight()


	def cursorRight(self, bItemStep=False):
		right = self.getLeafToRight()
		if right is not None:
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			if bItemStep:
				right.makeCurrent()
			else:
				right.startEditingOnLeft()



	def cursorToLeftChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			navList[0].makeCurrent()

	def cursorToRightChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			navList[-1].makeCurrent()


	def cursorToParent(self):
		if self._parent is not None:
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			self._parent.makeCurrent()




	def cursorUp(self):
		above = self.getNodeAbove()
		if above is not None:
			cursorPosInAbove = self.widget.getPointRelativeTo( above.widget, self.getCursorPosition() )
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
			above.startEditingAtPosition( cursorPosInAbove )

	def cursorDown(self):
		below = self.getNodeBelow()
		if below is not None:
			cursorPosInBelow = self.widget.getPointRelativeTo( below.widget, self.getCursorPosition() )
			# Must finish editing first, or we get problems with events invoking one another through the presentation system
			self.finishEditing()
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








	def _o_handleKeyPress(self, receivingViewNodePath, widget, keyPressEvent):
		state = keyPressEvent.state
		keyVal = keyPressEvent.keyVal
		key = keyVal, state
		char = keyPressEvent.keyString

		inputHandler = None
		
		receivingViewNodePath = [ self ]  +  receivingViewNodePath

		for behavior in self.behaviors:
			if behavior.handleKeyPress( self, receivingViewNodePath, widget, keyPressEvent ):
				return True

		parentStyleSheet = None
		if self._parent is not None:
			parentStyleSheet = self._parent._styleSheet

		result = self._styleSheet._f_handleKeyPress( DocViewEventKey( receivingViewNodePath, keyPressEvent ), parentStyleSheet )
		if result is not False:
			return result

		# Pass to the parent node
		if self._parent is not None:
			return self._parent._o_handleKeyPress( receivingViewNodePath, widget, keyPressEvent )
		else:
			return False


	# Called by DocPresent widgets
	def _f_handleKeyPress(self, widget, keyPressEvent):
		return self._o_handleKeyPress( [], widget, keyPressEvent)




	def _f_handleEmpty(self, receivingViewNodePath, bDirectEvent):
		parentStyleSheet = None
		if self._parent is not None:
			parentStyleSheet = self._parent._styleSheet

		receivingViewNodePath = [ self ]  +  receivingViewNodePath

		result = self._styleSheet._f_handleEmpty( DocViewEventEmpty( receivingViewNodePath ), parentStyleSheet, bDirectEvent )
		if result is not None:
			return result

		# Pass to the parent node
		if self._parent is not None:
			return self._parent._f_handleEmpty( receivingViewNodePath, bDirectEvent )
		else:
			return None


	def _f_handleToken(self, receivingViewNodePath, token, tokenIndex, numTokens, bDirectEvent):
		parentStyleSheet = None
		if self._parent is not None:
			parentStyleSheet = self._parent._styleSheet

		receivingViewNodePath = [ self ]  +  receivingViewNodePath

		result = self._styleSheet._f_handleToken( DocViewEventToken( receivingViewNodePath, token, tokenIndex, numTokens ), parentStyleSheet, bDirectEvent )
		if result is not None:
			return result

		# Pass to the parent node
		if self._parent is not None:
			return self._parent._f_handleToken( receivingViewNodePath, token, tokenIndex, numTokens, bDirectEvent )
		else:
			return None




	def makeCurrent(self):
		self.widget.grabFocus()



	def startEditingOnLeft(self):
		self.makeCurrent()


	def startEditingOnRight(self):
		self.makeCurrent()


	def startEditingAtPosition(self, pos):
		self.makeCurrent()


	def finishEditing(self):
		self.widget.ungrabFocus()




	def getCursorPosition(self):
		return Point2( self.widget.getAllocation() * 0.5 )



	def getInsertPosition(self, receivingNodePath):
		return 0



	parentNodeView = property( getParentNodeView )
	docView = property( getDocView )
	docNodeKey = property( getDocNodeKey )
