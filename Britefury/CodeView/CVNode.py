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

from Britefury.Util.SignalSlot import ClassSignal

from Britefury.Kernel import KMeta

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewBehavior.CVBMovementBehavior import CVBMovementBehavior
from Britefury.CodeViewBehavior.CVBDeleteNodeBehavior import CVBDeleteNodeBehavior

from Britefury.CodeViewTree.CVTNode import CVTNode

from Britefury.CodeView.CodeView import CodeView

from Britefury.DocView.Toolkit.DTWidget import *




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


	treeNode = SheetRefField( CVTNode )


	behaviors = [ CVBMovementBehavior(), CVBDeleteNodeBehavior() ]



	def __init__(self, treeNode, view):
		super( CVNode, self ).__init__()
		self.treeNode = treeNode
		self._view = view
		self._parent = None



	def refresh(self):
		self.refreshCell





	def deleteChild(self, child):
		return False


	def deleteNode(self, bMoveFocusLeft, widget):
		if self._parent is not None:
			if self._parent.deleteChild( self ):
				if bMoveFocusLeft:
					self.cursorLeft( widget == self.widget )
				else:
					self.cursorRight( widget == self.widget )




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
			return leftChild._f_cursorEnterFromRight( self, bItemStep )
		elif self._parent is not None:
			return self._parent._f_cursorLeftFromChild( self, bItemStep )
		else:
			return True

	def _f_cursorRightFromChild(self, child, bItemStep):
		navList = self.horizontalNavigationList()
		rightChild = self._p_nextNavListItem( navList, child )
		if rightChild is not None:
			return rightChild._f_cursorEnterFromLeft( self, bItemStep )
		elif self._parent is not None:
			return self._parent._f_cursorRightFromChild( self, bItemStep )
		else:
			return True


	def _f_cursorUpFromChild(self, child):
		navList = self.verticalNavigationList()
		childAbove = self._p_prevNavListItem( navList, child )
		if childAbove is not None:
			return childAbove._f_cursorEnterFromBelow( self )
		elif self._parent is not None:
			return self._parent._f_cursorUpFromChild( self )
		else:
			return True

	def _f_cursorDownFromChild(self, child):
		navList = self.verticalNavigationList()
		childBelow = self._p_nextNavListItem( navList, child )
		if childBelow is not None:
			return childBelow._f_cursorEnterFromAbove( self )
		elif self._parent is not None:
			return self._parent._f_cursorDownFromChild( self )
		else:
			return True





	def _f_cursorEnterFromLeft(self, parent, bItemStep):
		navList = self.horizontalNavigationList()
		if navList != []:
			return navList[0]._f_cursorEnterFromLeft( self, bItemStep )
		else:
			if bItemStep:
				self.makeCurrent()
			else:
				self.startEditingOnLeft()
			return True

	def _f_cursorEnterFromRight(self, parent, bItemStep):
		navList = self.horizontalNavigationList()
		if navList != []:
			return navList[-1]._f_cursorEnterFromRight( self, bItemStep )
		else:
			if bItemStep:
				self.makeCurrent()
			else:
				self.startEditingOnRight()
			return True



	def _f_cursorEnterFromAbove(self, parent):
		navList = self.verticalNavigationList()
		if navList != []:
			return navList[0]._f_cursorEnterFromAbove( self )
		else:
			self.makeCurrent()
			return True

	def _f_cursorEnterFromBelow(self, parent):
		navList = self.verticalNavigationList()
		if navList != []:
			return navList[-1]._f_cursorEnterFromBelow( self )
		else:
			self.makeCurrent()
			return True



	def _o_handleKeyPress(self, receivingNodePath, widget, keyPressEvent):
		state = keyPressEvent.state  &  ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )
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

