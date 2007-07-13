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

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewBehavior.CVBMovementBehavior import CVBMovementBehavior
from Britefury.CodeViewBehavior.CVBDeleteNodeBehavior import CVBDeleteNodeBehavior

from Britefury.CodeViewTree.CVTNode import CVTNode, CVTNodeInvalid

from Britefury.CodeView.CodeView import CodeView
from Britefury.CodeView.MoveFocus import MoveFocus

from Britefury.DocView.Toolkit.DTWidget import *
from Britefury.DocView.Toolkit.DTBorder import *
from Britefury.DocView.Toolkit.DTLabel import *




class CVChildNodeSlotFunctionField (FunctionRefField):
	def __init__(self, doc=''):
		super( CVChildNodeSlotFunctionField, self ).__init__( doc )
		self._behaviors = []


	def _f_metaMember_initClass(self):
		super( CVChildNodeSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node is self._f_getImmutableValueFromInstance( instance )


	def setBehaviors(self, behaviors):
		self._behaviors = behaviors






class CVChildNodeListSlotFunctionField (FunctionField):
	def __init__(self, doc=''):
		super( CVChildNodeListSlotFunctionField, self ).__init__( doc )
		self._behaviors = []


	def _f_metaMember_initClass(self):
		super( CVChildNodeListSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node in self._f_getImmutableValueFromInstance( instance )


	def setBehaviors(self, behaviors):
		self._behaviors = behaviors







class CVNodeClass (SheetClass):
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


		super( CVNodeClass, cls ).__init__( clsName, clsBases, clsDict )


		try:
			treeNodeClass = clsDict['treeNodeClass']
		except KeyError:
			pass
		else:
			CodeView._nodeClassTable[treeNodeClass] = cls



class CVNode (Sheet, DTWidgetKeyHandlerInterface):
	__metaclass__ = CVNodeClass


	class _CouldNotFindNextChildError (Exception):
		pass


	treeNode = SheetRefField( CVTNode )


	behaviors = [ CVBMovementBehavior(), CVBDeleteNodeBehavior() ]



	def __init__(self, treeNode, view):
		super( CVNode, self ).__init__()
		self.treeNode = treeNode
		self._view = view
		self._parent = None
		self._bDeleting = False



	def refresh(self):
		self.refreshCell





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
		if self._parent is not None:
			return self._parent._f_cursorLeftFromChild( self, bItemStep )
		else:
			return True

	def cursorRight(self, bItemStep=False):
		if self._parent is not None:
			return self._parent._f_cursorRightFromChild( self, bItemStep )
		else:
			return True



	def cursorToLeftSibling(self):
		if self._parent is not None:
			return self._parent._f_cursorLeftFromChildToSibling( self )
		else:
			return True

	def cursorToRightSibling(self):
		if self._parent is not None:
			return self._parent._f_cursorRightFromChildToSibling( self )
		else:
			return True



	def cursorToParent(self):
		if self._parent is not None:
			self._parent.makeCurrent()

		return True



	def cursorToLeftChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[0].makeCurrent()
		return True

	def cursorToRightChild(self):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[-1].makeCurrent()
		return True



	def cursorUp(self):
		if self._parent is not None:
			return self._parent._f_cursorUpFromChild( self )
		else:
			return True

	def cursorDown(self):
		if self._parent is not None:
			return self._parent._f_cursorDownFromChild( self )
		else:
			return True



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


	def _f_cursorLeftFromChild(self, child, bItemStep):
		navList = self.horizontalNavigationList()
		leftChild = self._p_prevNavListItem( navList, child )
		if leftChild is not None:
			leftChild._f_cursorEnterFromRight( self, bItemStep )
			return True
		elif self._parent is not None:
			return self._parent._f_cursorLeftFromChild( self, bItemStep )
		else:
			return True

	def _f_cursorRightFromChild(self, child, bItemStep):
		navList = self.horizontalNavigationList()
		rightChild = self._p_nextNavListItem( navList, child )
		if rightChild is not None:
			rightChild._f_cursorEnterFromLeft( self, bItemStep )
			return True
		elif self._parent is not None:
			return self._parent._f_cursorRightFromChild( self, bItemStep )
		else:
			return True


	def _f_cursorLeftFromChildToSibling(self, child):
		navList = self.horizontalNavigationList()
		leftChild = self._p_prevNavListItem( navList, child )
		if leftChild is not None:
			leftChild.makeCurrent()
			return True
		else:
			return True

	def _f_cursorRightFromChildToSibling(self, child):
		navList = self.horizontalNavigationList()
		rightChild = self._p_nextNavListItem( navList, child )
		if rightChild is not None:
			rightChild.makeCurrent()
			return True
		else:
			return True


	def _f_cursorUpFromChild(self, child):
		navList = self.verticalNavigationList()
		childAbove = self._p_prevNavListItem( navList, child )
		if childAbove is not None:
			childAbove._f_cursorEnterFromBelow( self )
			return True
		elif self._parent is not None:
			return self._parent._f_cursorUpFromChild( self )
		else:
			return True

	def _f_cursorDownFromChild(self, child):
		navList = self.verticalNavigationList()
		childBelow = self._p_nextNavListItem( navList, child )
		if childBelow is not None:
			childBelow._f_cursorEnterFromAbove( self )
			return True
		elif self._parent is not None:
			return self._parent._f_cursorDownFromChild( self )
		else:
			return True





	def _f_cursorEnterFromLeft(self, parent, bItemStep):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[0]._f_cursorEnterFromLeft( self, bItemStep )
		else:
			if bItemStep:
				self.makeCurrent()
			else:
				self.startEditingOnLeft()

	def _f_cursorEnterFromRight(self, parent, bItemStep):
		navList = self.horizontalNavigationList()
		if navList != []:
			navList[-1]._f_cursorEnterFromRight( self, bItemStep )
		else:
			if bItemStep:
				self.makeCurrent()
			else:
				self.startEditingOnRight()



	def _f_cursorEnterFromAbove(self, parent):
		navList = self.verticalNavigationList()
		if navList != []:
			navList[0]._f_cursorEnterFromAbove( self )
		else:
			self.makeCurrent()

	def _f_cursorEnterFromBelow(self, parent):
		navList = self.verticalNavigationList()
		if navList != []:
			navList[-1]._f_cursorEnterFromBelow( self )
		else:
			self.makeCurrent()



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



	def getInsertPosition(self, receivingNodePath):
		return 0


	def _o_buildViewForChild(self, cvtNode, viewNodeClass=None):
		return self._view.buildView( cvtNode, self, viewNodeClass )

	def _o_getViewNode(self, cvtNode, viewNodeClass=None):
		return self._view.getViewNodeForTreeNode( cvtNode, viewNodeClass )






class CVNodeInvalid (CVNode):
	treeNodeClass = CVTNodeInvalid


	treeNode = SheetRefField( CVTNodeInvalid )


	@FunctionField
	def refreshCell(self):
		pass




	def __init__(self, treeNode, view):
		super( CVNodeInvalid, self ).__init__( treeNode, view )
		self.widget = DTBorder()
		self.widget.allMargins = 2.0
		self.widget.child = DTLabel( '<invalid>', font='Sans bold italic 11', colour=Colour3f( 1.0, 1.0, 1.0 ) )
		self.widget.backgroundColour = Colour3f( 1.0, 0.0, 0.0 )



	def startEditing(self):
		self.makeCurrent()



