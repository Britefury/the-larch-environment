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





_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_string = DocViewTokenDefinition( 'string', pyparsing.Word( _unquotedStringChars )  |  pyparsing.quotedString )
_openParen = DocViewTokenDefinition( 'openParen', pyparsing.Literal( '(' ) )
_closeParen = DocViewTokenDefinition( 'closeParen', pyparsing.Literal( ')' ) )
_whitespace = DocViewTokenDefinition( 'whitespace', pyparsing.Word( string.whitespace ) )






@DVStyleSheetSetValueAction
def _setValueTokenAction(text):
	return text



def _getInsertPosition(receivingDocNodePathKeys):
	docNodeKey = receivingDocNodePathKeys[0]
	if isinstance( docNodeKey.docNode, DMListInterface ):
		return docNodeKey.docNode, 0
	else:
		return docNodeKey.parentDocNode, docNodeKey.index



class LispAddListAction (DVStyleSheetAction):
	def _o_keyAction(self, receivingViewNodePath, receivingDocNodePathKeys, keyPressEvent, parentStyleSheet):
		#docNodeKey = receivingDocNodePathKeys[0]
		parentDocNode, indexInParent = _getInsertPosition( receivingDocNodePathKeys )
		#parentDocNode = docNodeKey.parentDocNode
		#indexInParent = docNodeKey.index
		parentDocNode.insert( indexInParent, [] )
		return DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent )
_addListAction = LispAddListAction()




class LispAddStringAction (DVStyleSheetAction):
	def _o_keyAction(self, receivingViewNodePath, receivingDocNodePathKeys, keyPressEvent, parentStyleSheet):
		#docNodeKey = receivingDocNodePathKeys[0]
		parentDocNode, indexInParent = _getInsertPosition( receivingDocNodePathKeys )
		#parentDocNode = docNodeKey.parentDocNode
		#indexInParent = docNodeKey.index
		parentDocNode.insert( indexInParent, keyPressEvent.keyString )
		return DocNodeKey( parentDocNode[indexInParent], parentDocNode, indexInParent )
_addStringAction = LispAddStringAction()




class LispStringStyleSheet (DVStringStyleSheet):
	tokeniser = DocViewTokeniser( [ _string, _openParen, _closeParen, _whitespace ] )


	_setValueTokenHandler = DVStyleSheetTokenHandler( 'string', _setValueTokenAction )


	_deleteAction = DVStyleSheetDeleteAction()
	_emptyHandler = DVStyleSheetEmptyHandler( _deleteAction )

	_addListHandler = DVStyleSheetCharHandler( '(', _addListAction )
	_addStringHandler = DVStyleSheetCharHandler( _unquotedStringChars, _addStringAction )






class DVListSExpressionStyleSheet (DVListWrappedLineStyleSheet):
	elementSpacing = 10.0


	def beginDelimiter(self):
		return DTLabel( '(', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	def endDelimiter(self):
		return DTLabel( ')', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	_addHandler = DVStyleSheetCharHandler( '(', _addListAction )
	_addStringHandler = DVStyleSheetCharHandler( _unquotedStringChars, _addStringAction )




def makeLispDocView(root, commandHistory):
	disp = StyleSheetDispatcher( LispStringStyleSheet(), DVListSExpressionStyleSheet() )
	view = DocView( root, commandHistory, disp )
	return view



