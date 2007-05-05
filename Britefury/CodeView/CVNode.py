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

from Britefury.CodeViewTree.CVTNode import CVTNode

from Britefury.CodeView.CodeView import CodeView

from Britefury.DocView.Toolkit.DTWidget import *




class CVChildNodeSlotFunctionField (FunctionRefField):
	def __init__(self, doc=''):
		super( CVChildNodeSlotFunctionField, self ).__init__( doc )
		self._keyToInputHandler = {}


	def _f_metaMember_initClass(self):
		super( CVChildNodeSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node is self._f_getImmutableValueFromInstance( instance )




class CVChildNodeListSlotFunctionField (FunctionField):
	def __init__(self, doc=''):
		super( CVChildNodeListSlotFunctionField, self ).__init__( doc )
		self._keyToInputHandler = {}


	def _f_metaMember_initClass(self):
		super( CVChildNodeListSlotFunctionField, self )._f_metaMember_initClass()
		self._cls._nodeSlots.append( self )


	def _f_containsNode(self, instance, node):
		return node in self._f_getImmutableValueFromInstance( instance )







class _CVlInputHandler (KMeta.KMetaMember):
	def __init__(self, handlerMethod, keyTuples, childNodeSlot=None):
		super( _CVlInputHandler, self ).__init__()
		self._handlerMethod = handlerMethod
		self._childNodeSlot = childNodeSlot
		self._keyTuples = keyTuples
		if childNodeSlot is not None:
			self._o_addDependency( childNodeSlot )


	def _f_metaMember_initClass(self):
		if self._childNodeSlot is not None:
			for key in self._keyTuples:
				self._childNodeSlot._keyToInputHandler[key] = self
		else:
			for key in self._keyTuples:
				self._cls._keyToInputHandler[key] = self


	def _f_handleKeyPress(self, instance, receivingNodePath, entry, keyPressEvent):
		return self._handlerMethod( instance, receivingNodePath, entry, keyPressEvent )


class CVAccelInputHandler (_CVlInputHandler):
	def __init__(self, handlerMethod, keys, childNodeSlot=None):
		if isinstance( keys, str ):
			keyTuples = [ gtk.accelerator_parse( keys ) ]
		else:
			keyTuples = [ gtk.accelerator_parse( key )   for key in keys ]
		super( CVAccelInputHandler, self ).__init__( handlerMethod, keyTuples, childNodeSlot )


def CVAccelInputHandlerMethod(keys, childNodeSlot=None):
	def cvinputhandlermethoddecorator(method):
		return CVAccelInputHandler( method, keys, childNodeSlot )
	return cvinputhandlermethoddecorator




class CVCharInputHandler (_CVlInputHandler):
	def __init__(self, handlerMethod, chars, childNodeSlot=None):
		keyTuples = [ CVCharInputHandler._p_charToKeyTuple( char )   for char in chars ]
		super( CVCharInputHandler, self ).__init__( handlerMethod, keyTuples, childNodeSlot )


	@staticmethod
	def _p_charToKeyTuple(char):
		bShift = char.isupper()
		if bShift:
			return ord( char.upper() ), gtk.gdk.SHIFT_MASK
		else:
			return ord( char.upper() ), 0


def CVCharInputHandlerMethod(chars, childNodeSlot=None):
	def cvinputhandlermethoddecorator(method):
		return CVCharInputHandler( method, chars, childNodeSlot )
	return cvinputhandlermethoddecorator






class CVNodeClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		cls._keyToInputHandler = {}
		cls._nodeSlots = set()

		for base in clsBases:
			if hasattr( base, '_keyToInputHandler' ):
				cls._keyToInputHandler.update( base._keyToInputHandler )
				cls._nodeSlots = cls._nodeSlots.union( set( base._nodeSlots ) )
		cls._nodeSlots = list( cls._nodeSlots )


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



	def __init__(self, treeNode, view):
		super( CVNode, self ).__init__()
		self.treeNode = treeNode
		self._view = view
		self._parent = None



	def refresh(self):
		self.refreshCell




	def _o_handleKeyPress(self, receivingNodePath, entry, keyPressEvent):
		state = keyPressEvent.state  &  ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )
		keyVal = keyPressEvent.keyVal
		key = keyVal, state

		# Try to get the node slot input handler
		if len( receivingNodePath ) > 0:
			nodeSlot = None
			for slot in self._nodeSlots:
				if slot._f_containsNode( self, receivingNodePath[0] ):
					nodeSlot = slot

			if nodeSlot is not None:
				try:
					inputHandler = nodeSlot._keyToInputHandler[key]
				except KeyError:
					pass
				else:
					return inputHandler._f_handleKeyPress( self, ( self, ) + receivingNodePath, entry, keyPressEvent )


		try:
			inputHandler = self._keyToInputHandler[key]
		except KeyError:
			# Pass to the parent node
			if self._parent is not None:
				return self._parent._o_handleKeyPress( ( self, ) + receivingNodePath, entry, keyPressEvent )
			else:
				return False
		else:
			return inputHandler._f_handleKeyPress( self, ( self, ) + receivingNodePath, entry, keyPressEvent )


	def _f_handleKeyPress(self, entry, keyPressEvent):
		return self._o_handleKeyPress( (), entry, keyPressEvent)
