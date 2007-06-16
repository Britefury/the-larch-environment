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





class _CVBInputHandler (KMeta.KMetaMember):
	def __init__(self, handlerMethod):
		super( _CVBInputHandler, self ).__init__()
		self._handlerMethod = handlerMethod


	def _f_handleKeyPress(self, viewNode, receivingNodePath, widget, keyPressEvent):
		return self._handlerMethod( self, viewNode, receivingNodePath, widget, keyPressEvent )


class CVBAccelInputHandler (_CVBInputHandler):
	def __init__(self, handlerMethod, keys):
		super( CVBAccelInputHandler, self ).__init__( handlerMethod )
		if isinstance( keys, str ):
			self._keyTuples = [ gtk.accelerator_parse( keys ) ]
		else:
			self._keyTuples = [ gtk.accelerator_parse( key )   for key in keys ]


	def _f_metaMember_initClass(self):
		for key in self._keyTuples:
			self._cls._keyToInputHandler[key] = self




def CVBAccelInputHandlerMethod(keys):
	def cvinputhandlermethoddecorator(method):
		return CVBAccelInputHandler( method, keys )
	return cvinputhandlermethoddecorator




class CVBCharInputHandler (_CVBInputHandler):
	def __init__(self, handlerMethod, chars):
		super( CVBCharInputHandler, self ).__init__( handlerMethod )
		self._chars = chars


	def _f_metaMember_initClass(self):
		for char in self._chars:
			self._cls._charToInputHandler[char] = self




def CVBCharInputHandlerMethod(chars):
	def cvinputhandlermethoddecorator(method):
		return CVBCharInputHandler( method, chars )
	return cvinputhandlermethoddecorator






class CodeViewBehaviorClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		cls._keyToInputHandler = {}
		cls._charToInputHandler = {}

		for base in clsBases:
			if hasattr( base, '_keyToInputHandler' ):
				cls._keyToInputHandler.update( base._keyToInputHandler )
				cls._charToInputHandler.update( base._charToInputHandler )


		super( CodeViewBehaviorClass, cls ).__init__( clsName, clsBases, clsDict )





class CodeViewBehavior (Sheet):
	__metaclass__ = CodeViewBehaviorClass
	__debug_display_keypresses__ = False


	def handleKeyPress(self, fromNode, receivingNodePath, widget, keyPressEvent):
		state = keyPressEvent.state  &  ( gtk.gdk.SHIFT_MASK | gtk.gdk.CONTROL_MASK | gtk.gdk.MOD1_MASK )
		keyVal = keyPressEvent.keyVal
		key = keyVal, state
		char = keyPressEvent.keyString

		if self.__debug_display_keypresses__:
			print '%s: ' % ( self.__class__.__name__, ), keyVal, state, char

		# Look up the key handler
		inputHandler = None
		try:
			inputHandler = self._keyToInputHandler[key]
		except KeyError:
			try:
				inputHandler = self._charToInputHandler[char]
			except KeyError:
				pass


		if inputHandler is not None:
			return inputHandler._f_handleKeyPress( fromNode, receivingNodePath, widget, keyPressEvent )
		else:
			return False



