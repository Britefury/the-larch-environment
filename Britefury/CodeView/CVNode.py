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


	behaviors = [ CVBMovementBehavior() ]



	def __init__(self, treeNode, view):
		super( CVNode, self ).__init__()
		self.treeNode = treeNode
		self._view = view
		self._parent = None



	def refresh(self):
		self.refreshCell



	def horizontalNavigationList(self):
		return None

	def verticalNavigationList(self):
		return None






	def getChildToLeft(self, child):
		navList = self.horizontalNavigationList()
		if navList is None:
			return None
		else:
			try:
				index = navList.index( child )
			except ValueError:
				return None
			if index > 0:
				return navList[index-1]
			else:
				return None


	def getChildToRight(self, child):
		navList = self.horizontalNavigationList()
		if navList is None:
			return None
		else:
			try:
				index = navList.index( child )
			except ValueError:
				return None
			if index < len(navList) - 1:
				return navList[index+1]
			else:
				return None


	def getChildAbove(self, child):
		navList = self.verticalNavigationList()
		if navList is None:
			return None
		else:
			try:
				index = navList.index( child )
			except ValueError:
				return None
			if index > 0:
				return navList[index-1]
			else:
				return None


	def getChildBelow(self, child):
		navList = self.verticalNavigationList()
		if navList is None:
			return None
		else:
			try:
				index = navList.index( child )
			except ValueError:
				return None
			if index < len(navList) - 1:
				return navList[index+1]
			else:
				return None


	def _move(self, toChild):
		if toChild is not None:
			if isinstance( toChild, CVNode ):
				toChild.makeCurrent()
				return True
			elif isinstance( toChild, DTWidget ):
				toChild.grabFocus()
				return True
			else:
				raise TypeError, 'cannot handle child %s' % ( toChild, )
				return False
		else:
			return False


	def moveLeft(self, fromChild):
		return self._move( self.getChildToLeft( fromChild ) )

	def moveRight(self, fromChild):
		return self._move( self.getChildToRight( fromChild ) )

	def moveUp(self, fromChild):
		return self._move( self.getChildAbove( fromChild ) )

	def moveDown(self, fromChild):
		return self._move( self.getChildBelow( fromChild ) )




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

