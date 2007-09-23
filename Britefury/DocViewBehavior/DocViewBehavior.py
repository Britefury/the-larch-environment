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





class _DVBInputHandler (KMeta.KMetaMember):
	def __init__(self, handlerMethod):
		super( _DVBInputHandler, self ).__init__()
		self._handlerMethod = handlerMethod


	def _f_handleKeyPress(self, viewNode, receivingNodePath, widget, keyPressEvent):
		return self._handlerMethod( self, viewNode, receivingNodePath, widget, keyPressEvent )


class DVBAccelInputHandler (_DVBInputHandler):
	def __init__(self, handlerMethod, keys):
		super( DVBAccelInputHandler, self ).__init__( handlerMethod )
		if isinstance( keys, str ):
			self._keyTuples = [ gtk.accelerator_parse( keys ) ]
		else:
			self._keyTuples = [ gtk.accelerator_parse( key )   for key in keys ]


	def _f_metaMember_initClass(self):
		for key in self._keyTuples:
			self._cls._keyToInputHandler[key] = self




def DVBAccelInputHandlerMethod(keys):
	def cvinputhandlermethoddecorator(method):
		return DVBAccelInputHandler( method, keys )
	return cvinputhandlermethoddecorator




class DVBCharInputHandler (_DVBInputHandler):
	def __init__(self, handlerMethod, chars):
		super( DVBCharInputHandler, self ).__init__( handlerMethod )
		self._chars = chars


	def _f_metaMember_initClass(self):
		for char in self._chars:
			self._cls._charToInputHandler[char] = self




def DVBCharInputHandlerMethod(chars):
	def cvinputhandlermethoddecorator(method):
		return DVBCharInputHandler( method, chars )
	return cvinputhandlermethoddecorator






class DocViewBehaviorClass (SheetClass):
	def __init__(cls, clsName, clsBases, clsDict):
		cls._keyToInputHandler = {}
		cls._charToInputHandler = {}

		for base in clsBases:
			if hasattr( base, '_keyToInputHandler' ):
				cls._keyToInputHandler.update( base._keyToInputHandler )
				cls._charToInputHandler.update( base._charToInputHandler )


		super( DocViewBehaviorClass, cls ).__init__( clsName, clsBases, clsDict )





class DocViewBehavior (Sheet):
	__metaclass__ = DocViewBehaviorClass
	__debug_display_keypresses__ = False


	def handleKeyPress(self, fromNode, receivingNodePath, widget, keyPressEvent):
		state = keyPressEvent.state
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
			if state == 0  or  state == gtk.gdk.SHIFT_MASK:
				try:
					inputHandler = self._charToInputHandler[char]
				except KeyError:
					pass


		if inputHandler is not None:
			return inputHandler._f_handleKeyPress( fromNode, receivingNodePath, widget, keyPressEvent )
		else:
			return False



