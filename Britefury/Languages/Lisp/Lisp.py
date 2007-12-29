##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string
import pyparsing

from Britefury.Math.Math import Colour3f

from Britefury.DocPresent.Toolkit.DTLabel import DTLabel

from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.DocView.DocView import DocView
from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser

from Britefury.DocView.StyleSheet.DVStyleSheet import *

from Britefury.DocView.StyleSheet.StyleSheetDispatcher import StyleSheetDispatcher
from Britefury.DocView.StyleSheet.DVStringStyleSheet import DVStringStyleSheet
from Britefury.DocView.StyleSheet.DVListWrappedLineStyleSheet import DVListWrappedLineStyleSheet



"""
(
(= unquotedStringChars 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!"#$%&*+,-./:;<=>?@[\]^_|~ )

(= stringTok (gsym defineToken string ((gsym parserWord @unquotedStringChars)  |  (gsym parserQuotedString))))
(= openParen (gsym defineToken openParen (gsym parserLiteral '(')))
(= closeParen (gsym defineToken closeParen (gsym parserLiteral ')')))
(= whitespace (gsym defineToken whitespace (gsym parserWord (gsym whitespace))))
)
"""

_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_string = DocViewTokenDefinition( 'string', pyparsing.Word( _unquotedStringChars )  |  pyparsing.quotedString )
_openParen = DocViewTokenDefinition( 'openParen', pyparsing.Literal( '(' ) )
_closeParen = DocViewTokenDefinition( 'closeParen', pyparsing.Literal( ')' ) )
_whitespace = DocViewTokenDefinition( 'whitespace', pyparsing.Word( string.whitespace ) )






@DVStyleSheetSetValueAction
def _setValueTokenAction(text):
	return text



def _getInsertPosition(receivingDocNodeKeyPath):
	docNodeKey = receivingDocNodeKeyPath[0]
	if isinstance( docNodeKey.docNode, DMListInterface ):
		return docNodeKey.docNode, len( docNodeKey.docNode )
	else:
		return docNodeKey.parentDocNode, docNodeKey.index



class LispAddListAction (DVStyleSheetAction):
	def _p_insertList(self, event, bAfter):
		if bAfter:
			parentDocNode, indexInParent = event.receivingDocNodeKeyPath[0].parentDocNode, event.receivingDocNodeKeyPath[0].index + 1
		else:
			parentDocNode, indexInParent = _getInsertPosition( event.receivingDocNodeKeyPath )
		parentDocNode.insert( indexInParent, [] )
		v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
		v.makeCurrent()
		return v

	def _p_replaceWithList(self, event):
		parentDocNode, indexInParent = event.docNodeKey.parentDocNode, event.docNodeKey.index
		parentDocNode[indexInParent] = []
		v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
		v.makeCurrent()
		return v

	def _o_keyAction(self, event, parentStyleSheet):
		return self._p_insertList( event, False )

	def _o_tokenAction (self, event, parentStyleSheet):
		if event.bFirst:
			return self._p_replaceWithList( event )
		else:
			return self._p_insertList( event, True )

_addListAction = LispAddListAction()




class LispAddStringAction (DVStyleSheetAction):
	def _o_keyAction(self, event, parentStyleSheet):
		parentDocNode, indexInParent = _getInsertPosition( event.receivingDocNodeKeyPath )
		parentDocNode.insert( indexInParent, event.keyPressEvent.keyString )
		v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
		v.startEditing()
		return v

	def _o_tokenAction (self, event, parentStyleSheet):
		parentDocNode, indexInParent = _getInsertPosition( event.receivingDocNodeKeyPath )
		parentDocNode.insert( indexInParent, event.token.text )
		v = event.nodeView.docView.refreshAndGetViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
		#v.startEditing()
		if event.bLast:
			v.startEditing()
		else:
			v.makeCurrent()
		return v

_addStringAction = LispAddStringAction()




class LispNextSiblingAction (DVStyleSheetAction):
	def _o_keyAction(self, event, parentStyleSheet):
		return self._p_action( event, parentStyleSheet )
			
	def _o_tokenAction (self, event, parentStyleSheet):
		return self._p_action( event, parentStyleSheet )
		
		
	def _p_action(self, event, parentStyleSheet):
		docNodeKey = event.receivingDocNodeKeyPath[0]
		parentDocNode, indexInParent = docNodeKey.parentDocNode, docNodeKey.index
		if indexInParent  <  len( parentDocNode ) - 1:
			indexInParent += 1
			v = event.nodeView.docView.getViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
			v.makeCurrent()
			return v
		else:
			v = event.nodeView.docView.getViewNodeForDocNodeKey( DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent ) )
			v.makeCurrent()
			return v

_nextSiblingAction = LispNextSiblingAction()




class LispStringStyleSheet (DVStringStyleSheet):
	tokeniser = DocViewTokeniser( [ _string, _openParen, _closeParen, _whitespace ] )


	_setValueTokenHandler = DVStyleSheetTokenHandler( 'string', _setValueTokenAction )
	_addListTokenHandler = DVStyleSheetTokenHandler( 'openParen', _addListAction )
	_nextSiblingTokenHandler = DVStyleSheetTokenHandler( 'whitespace', _nextSiblingAction )


	_deleteAction = DVStyleSheetDeleteAction()
	_emptyHandler = DVStyleSheetEmptyHandler( _deleteAction )

	_addListKeyHandler = DVStyleSheetCharHandler( '(', _addListAction )
	_addStringKeyHandler = DVStyleSheetCharHandler( _unquotedStringChars, _addStringAction )
	_nextSiblingKeyHandler = DVStyleSheetCharHandler( ' ', _nextSiblingAction )
	






class DVListSExpressionStyleSheet (DVListWrappedLineStyleSheet):
	elementSpacing = 10.0


	def beginDelimiter(self):
		return DTLabel( '(', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	def endDelimiter(self):
		return DTLabel( ')', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	_addHandler = DVStyleSheetCharHandler( '(', _addListAction )
	_addStringKeyHandler = DVStyleSheetCharHandler( _unquotedStringChars, _addStringAction )
	_addStringTokenHandler = DVStyleSheetTokenHandler( 'string', _addStringAction )




def makeLispDocView(root, commandHistory):
	disp = StyleSheetDispatcher( LispStringStyleSheet(), DVListSExpressionStyleSheet() )
	view = DocView( root, commandHistory, disp )
	return view



