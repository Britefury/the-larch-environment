##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.KMeta import KMetaMember, KClass, KObject

from Britefury.DocView.MoveFocus import MoveFocus
from Britefury.DocView.DocViewTokeniser import DocViewTokeniser
from Britefury.DocView.SelectPath import SelectPath




class DVStyleSheetAction (object):
	def handleToken(self, nodeView, token, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		selectedNode = None
		nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			nodeView.cursorRight()
		selectPath = self._o_tokenAction( token, parentDocNode, indexInParent, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return selectPath



	def handleEmpty(self, nodeView, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		selectedNode = None
		nodeView._f_commandHistoryFreeze()
		if bMoveCursor:
			nodeView.cursorRight()
		selectPath = self._o_emptyAction( parentDocNode, indexInParent, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return selectPath



	# Should return select path
	def _o_tokenAction(self, token, parentDocNode, indexInParent, parentStyleSheet):
		pass

	def _o_emptyAction(self, parentDocNode, indexInParent, parentStyleSheet):
		pass




class DVStyleSheetSetValueAction (DVStyleSheetAction):
	def __init__(self, textToNode):
		self._textToNode = textToNode


	def _o_tokenAction(self, token, parentDocNode, indexInParent, parentStyleSheet):
		tokenClassName, text = token
		parentDocNode[indexInParent] = self._textToNode( text )
		return SelectPath( parentDocNode, [ parentDocNode[indexInParent] ] )





class DVStyleSheetDeleteAction (DVStyleSheetAction):
	def _o_emptyAction(self, parentDocNode, indexInParent, parentStyleSheet):
		del parentDocNode[indexInParent]
		if len( parentDocNode ) > indexInParent:
			return SelectPath( parentDocNode, [ parentDocNode[indexInParent] ] )
		else:
			return SelectPath( parentDocNode, [] )




class DVStyleSheetTokenHandler (KMetaMember):
	def __init__(self, tokenClassName, action):
		super( DVStyleSheetTokenHandler, self ).__init__()
		self._tokenClassName = tokenClassName
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._tokenClassNameToHandler[self._tokenClassName] = self



	def handleToken(self, nodeView, token, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		return self._action.handleToken( nodeView, token, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor )





class DVStyleSheetEmptyHandler (KMetaMember):
	def __init__(self, action):
		super( DVStyleSheetEmptyHandler, self ).__init__()
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._emptyHandler = self



	def handleEmpty(self, nodeView, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		return self._action.handleEmpty( nodeView, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor )







class DVStyleSheetClass (KClass):
	pass




class DVStyleSheet (KObject):
	__metaclass__ = DVStyleSheetClass



	tokeniser = DocViewTokeniser()


	def __init__(self, src=None):
		self._tokenClassNameToHandler = {}
		self._emptyHandler = None
		super( DVStyleSheet, self ).__init__( src )




	def _f_handleToken(self, nodeView, token, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		tokenClassName, text = token
		try:
			handler = self._tokenClassNameToHandler[tokenClassName]
		except KeyError:
			handler = None
		if handler is not None:
			return handler.handleToken( nodeView, token, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor )


	def _f_handleEmpty(self, nodeView, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor):
		if self._emptyHandler is not None:
			return self._emptyHandler.handleEmpty( nodeView, parentDocNode, indexInParent, parentStyleSheet, bMoveCursor )
