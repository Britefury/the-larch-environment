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
	def handleKeyPress(self, event, parentStyleSheet):
		event.nodeView._f_commandHistoryFreeze()
		selectNodeKey = self._o_keyAction( event, parentStyleSheet )
		event.nodeView._f_commandHistoryThaw()
		return selectNodeKey



	def handleEmpty(self, event, parentStyleSheet, bMoveCursor):
		event.nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			event.nodeView.cursorRight()
		selectedNode = self._o_emptyAction( event, parentStyleSheet )
		event.nodeView._f_commandHistoryThaw()
		return selectedNode


	def handleToken(self, event, parentStyleSheet, bMoveCursor):
		event.nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			event.nodeView.cursorRight()
		selectedNode = self._o_tokenAction( event, parentStyleSheet )
		event.nodeView._f_commandHistoryThaw()
		return selectedNode





	def _o_keyAction(self, event, parentStyleSheet):
		pass

	# Should return currently selected node view
	def _o_emptyAction(self, event, parentStyleSheet):
		pass

	# Should return currently selected node view
	def _o_tokenAction(self, event, parentStyleSheet):
		pass







class DVStyleSheetSetValueAction (DVStyleSheetAction):
	def __init__(self, textToNode):
		self._textToNode = textToNode


	def _o_tokenAction(self, event, parentStyleSheet):
		parentDocNode = event.docNodeKey.parentDocNode
		indexInParent = event.docNodeKey.index
		node = self._textToNode( event.token.text )
		parentDocNode[indexInParent] = node
		v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( node, parentDocNode, indexInParent ) )
		v.makeCurrent()
		return v





class DVStyleSheetDeleteAction (DVStyleSheetAction):
	def _o_emptyAction(self, event, parentStyleSheet):
		parentDocNode = event.docNodeKey.parentDocNode
		indexInParent = event.docNodeKey.index
		del parentDocNode[indexInParent]
		if len( parentDocNode ) > indexInParent:
			v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
			v.makeCurrent()
			return v
		else:
			event.nodeView.parentNodeView.makeCurrent()
			return event.nodeView.parentNodeView






class DVStyleSheetKeyHandler (KMetaMember):
	def __init__(self, action):
		super( DVStyleSheetKeyHandler, self ).__init__()
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._emptyHandler = self



	def handleKeyPress(self, event, parentStyleSheet):
		return self._action.handleKeyPress( event, parentStyleSheet )



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





class DVStyleSheetEmptyHandler (KMetaMember):
	def __init__(self, action):
		super( DVStyleSheetEmptyHandler, self ).__init__()
		self._action = action


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._emptyHandler = self


	def handleEmpty(self, event, parentStyleSheet, bMoveCursor):
		return self._action.handleEmpty( event, parentStyleSheet, bMoveCursor )





class DVStyleSheetTokenHandler (KMetaMember):
	def __init__(self, tokenClassName, action):
		super( DVStyleSheetTokenHandler, self ).__init__()
		self._tokenClassName = tokenClassName
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._tokenClassNameToHandler[self._tokenClassName] = self



	def handleToken(self, event, parentStyleSheet, bMoveCursor):
		return self._action.handleToken( event, parentStyleSheet, bMoveCursor )








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




	def _f_handleKeyPress(self, event, parentStyleSheet):
		state = event.keyPressEvent.state
		keyVal = event.keyPressEvent.keyVal
		key = keyVal, state
		char = event.keyPressEvent.keyString

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
			return keyHandler.handleKeyPress( event, parentStyleSheet )
		else:
			return False



	def _f_handleEmpty(self, event, parentStyleSheet, bMoveCursor):
		if self._emptyHandler is not None:
			return self._emptyHandler.handleEmpty( event, parentStyleSheet, bMoveCursor )
		else:
			return None



	def _f_handleToken(self, event, parentStyleSheet, bMoveCursor):
		tokenClassName = event.token.tokenClassName
		try:
			handler = self._tokenClassNameToHandler[tokenClassName]
		except KeyError:
			handler = None
		if handler is not None:
			return handler.handleToken( event, parentStyleSheet, bMoveCursor )
		else:
			return None





