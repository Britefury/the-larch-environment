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




class DVStyleSheetAction (object):
	def invoke(self, docView, token, parentDocNode, indexInParent, parentStyleSheet):
		selectedNode = None
		docView._f_commandHistoryFreeze()
		select = self._o_action( token, parentDocNode, indexInParent, parentStyleSheet )
		docView._f_commandHistoryThaw()
		return select



	def invokeDirect(self, nodeView, token, parentDocNode, indexInParent, parentStyleSheet):
		selectedNode = None
		nodeView._f_commandHistoryFreeze()
		nodeView.cursorRight()
		select = self._o_action( token, parentDocNode, indexInParent, parentStyleSheet )
		nodeView._f_commandHistoryThaw()
		return select


	# Should return selected doc node
	def _o_action(self, token, parentDocNode, indexInParent, parentStyleSheet):
		pass




class DVStyleSheetSetValueAction (DVStyleSheetAction):
	def __init__(self, textToNode, emptyValue, emptyFunction):
		self._textToNode = textToNode
		self._emptyValue = emptyValue
		self._emptyFunction = emptyFunction




	def _o_action(self, token, parentDocNode, indexInParent, parentStyleSheet):
		tokenClassName, text = token
		if text == self._emptyValue:
			return self._emptyFunction( token, parentDocNode, indexInParent, parentStyleSheet )
		else:
			parentDocNode[indexInParent] = self._textToNode( text )
			return parentDocNode[indexInParent]




class DVStyleSheetTokenHandler (KMetaMember):
	def __init__(self, tokenClassName, action):
		super( DVStyleSheetTokenHandler, self ).__init__()
		self._tokenClassName = tokenClassName
		self._action = action



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		instance._tokenClassNameToHandler[self._tokenClassName] = self



	def invoke(self, docView, token, parentDocNode, indexInParent, parentStyleSheet):
		return self._action.invoke( docView, token, parentDocNode, indexInParent, parentStyleSheet )

	def invokeDirect(self, nodeView, token, parentDocNode, indexInParent, parentStyleSheet):
		return self._action.invokeDirect( nodeView, token, parentDocNode, indexInParent, parentStyleSheet )








class DVStyleSheetClass (KClass):
	pass




class DVStyleSheet (KObject):
	__metaclass__ = DVStyleSheetClass



	tokeniser = DocViewTokeniser()


	def __init__(self, src=None):
		self._tokenClassNameToHandler = {}
		super( DVStyleSheet, self ).__init__( src )





	def _f_handleTokenList(self, nodeView, tokens, parentDocNode, indexInParent, parentStyleSheet, bDirectEvent):
		if len( tokens ) == 1  and  bDirectEvent:
			token = tokens[0]
			tokenClassName, text = token
			try:
				handler = self._tokenClassNameToHandler[tokenClassName]
			except KeyError:
				handler = None
			if handler is not None:
				select = handler.invokeDirect( nodeView, token, parentDocNode, indexInParent, parentStyleSheet )
				if select is not None:
					# TODO: SELECT THIS NODE
					pass
		else:
			docView = nodeView._view
			for token in tokens:
				tokenClassName, text = token
				try:
					handler = self._tokenClassNameToHandler[tokenClassName]
				except KeyError:
					handler = None
				if handler is not None:
					handler.invoke( docView, token, parentDocNode, indexInParent, parentStyleSheet )


