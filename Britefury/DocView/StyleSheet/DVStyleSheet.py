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

from Britefury.Kernel.KMeta import KMetaMember, KClass, KObject

from Britefury.DocView.MoveFocus import MoveFocus
from Britefury.DocView.DocViewTokeniser import DocViewTokeniser
from Britefury.DocView.DocViewNodeTable import DocNodeKey




class DVStyleSheetAction (object):
	def handleToken(self, nodeView, token, docNodeKey, parentStyleSheet, bMoveCursor):
		nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			nodeView.cursorRight()
		selectNodeKey = self._o_tokenAction( nodeView, token, docNodeKey, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return selectNodeKey



	def handleEmpty(self, nodeView, docNodeKey, parentStyleSheet, bMoveCursor):
		nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			nodeView.cursorRight()
		selectNodeKey = self._o_emptyAction( nodeView, docNodeKey, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return selectNodeKey



	def handleKeyPress(self, receivingViewNodePath, receivingDocNodePathKeys, widget, keyPressEvent, parentStyleSheet):
		nodeView = receivingViewNodePath[0]
		nodeView._f_commandHistoryFreeze()
		selectNodeKey = self._o_keyAction( receivingViewNodePath, receivingDocNodePathKeys, keyPressEvent, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return selectNodeKey



	# Should return select path
	def _o_tokenAction(self, nodeView, token, docNodeKey, parentStyleSheet):
		pass

	def _o_emptyAction(self, nodeView, docNodeKey, parentStyleSheet):
		pass

	def _o_keyAction(self, receivingViewNodePath, receivingDocNodePathKeys, keyPressEvent, parentStyleSheet):
		pass





class DVStyleSheetSetValueAction (DVStyleSheetAction):
	def __init__(self, textToNode):
		self._textToNode = textToNode


	def _o_tokenAction(self, nodeView, token, docNodeKey, parentStyleSheet):
		tokenClassName, text = token
		parentDocNode = docNodeKey.parentDocNode
		indexInParent = docNodeKey.index
		node = self._textToNode( text )
		parentDocNode[indexInParent] = node
		return DocNodeKey( node, parentDocNode, indexInParent )





class DVStyleSheetDeleteAction (DVStyleSheetAction):
	def _o_emptyAction(self, nodeView, docNodeKey, parentStyleSheet):
		parentDocNode = docNodeKey.parentDocNode
		indexInParent = docNodeKey.index
		del parentDocNode[indexInParent]
		if len( parentDocNode ) > indexInParent:
			return DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent )
		else:
			return nodeView.parentNodeView.key




class DVStyleSheetTokenHandler (KMetaMember):
	def __init__(self, tokenClassName, action):
		super( DVStyleSheetTokenHandler, self ).__init__()
		self._tokenClassName = tokenClassName
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._tokenClassNameToHandler[self._tokenClassName] = self



	def handleToken(self, nodeView, token, docNodeKey, parentStyleSheet, bMoveCursor):
		return self._action.handleToken( nodeView, token, docNodeKey, parentStyleSheet, bMoveCursor )





class DVStyleSheetEmptyHandler (KMetaMember):
	def __init__(self, action):
		super( DVStyleSheetEmptyHandler, self ).__init__()
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._emptyHandler = self



	def handleEmpty(self, nodeView, docNodeKey, parentStyleSheet, bMoveCursor):
		return self._action.handleEmpty( nodeView, docNodeKey, parentStyleSheet, bMoveCursor )





class DVStyleSheetKeyHandler (KMetaMember):
	def __init__(self, action):
		super( DVStyleSheetKeyHandler, self ).__init__()
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._emptyHandler = self



	def handleKeyPress(self, receivingViewNodePath, receivingDocNodePathKeys, widget, keyPressEvent, parentStyleSheet):
		return self._action.handleKeyPress( receivingViewNodePath, receivingDocNodePathKeys, widget, keyPressEvent, parentStyleSheet )






class DVStyleSheetAccelHandler (DVStyleSheetKeyHandler):
	def __init__(self, accels, action):
		super( DVStyleSheetAccelHandler, self ).__init__( action )
		if isinstance( accels, str ):
			self._keyTuples = [ gtk.accelerator_parse( accels ) ]
		else:
			self._keyTuples = [ gtk.accelerator_parse( docNodeKey )   for docNodeKey in accels ]


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		for docNodeKey in self._keyTuples:
			instance._accelToHandler[docNodeKey] = self





class DVStyleSheetCharHandler (DVStyleSheetKeyHandler):
	def __init__(self, chars, action):
		super( DVStyleSheetCharHandler, self ).__init__( action )
		self._chars = chars


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		for char in self._chars:
			instance._charToHandler[char] = self






class DVStyleSheetClass (KClass):
	pass




class DVStyleSheet (KObject):
	__metaclass__ = DVStyleSheetClass



	tokeniser = DocViewTokeniser()


	def __init__(self, src=None):
		self._tokenClassNameToHandler = {}
		self._emptyHandler = None
		self._accelToHandler = {}
		self._charToHandler = {}
		super( DVStyleSheet, self ).__init__( src )




	def _f_handleToken(self, nodeView, token, docNodeKey, parentStyleSheet, bMoveCursor):
		tokenClassName, text = token
		try:
			handler = self._tokenClassNameToHandler[tokenClassName]
		except KeyError:
			handler = None
		if handler is not None:
			return handler.handleToken( nodeView, token, docNodeKey, parentStyleSheet, bMoveCursor )


	def _f_handleEmpty(self, nodeView, docNodeKey, parentStyleSheet, bMoveCursor):
		if self._emptyHandler is not None:
			return self._emptyHandler.handleEmpty( nodeView, docNodeKey, parentStyleSheet, bMoveCursor )



	def _f_handleKeyPress(self, receivingViewNodePath, recreceivingDocNodePathKeys, widget, keyPressEvent, parentStyleSheet):
		state = keyPressEvent.state
		keyVal = keyPressEvent.keyVal
		key = keyVal, state
		char = keyPressEvent.keyString

		# Look up the key handler
		keyHandler = None
		try:
			keyHandler = self._accelToHandler[key]
		except KeyError:
			if state == 0  or  state == gtk.gdk.SHIFT_MASK:
				try:
					keyHandler = self._charToHandler[char]
				except KeyError:
					pass


		if keyHandler is not None:
			return keyHandler.handleKeyPress( receivingViewNodePath, recreceivingDocNodePathKeys, widget, keyPressEvent, parentStyleSheet )
		else:
			return False




























