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

from Britefury.DocView.DocView import DocView
from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser

from Britefury.DocView.StyleSheet.DVStyleSheet import DVStyleSheetSetValueAction, DVStyleSheetTokenHandler

from Britefury.DocView.StyleSheet.StyleSheetDispatcher import StyleSheetDispatcher
from Britefury.DocView.StyleSheet.DVStringStyleSheet import DVStringStyleSheet
from Britefury.DocView.StyleSheet.DVListWrappedLineStyleSheet import DVListWrappedLineStyleSheet





_unquotedStringChars = ( string.digits + string.letters + string.punctuation ).replace( '(', '' ).replace( ')', '' ).replace( '\'', '' ).replace( '`', '' ).replace( '{', '' ).replace( '}', '' )


_string = DocViewTokenDefinition( 'string', pyparsing.Word( _unquotedStringChars )  |  pyparsing.quotedString )
_openParen = DocViewTokenDefinition( 'openParen', pyparsing.Literal( '(' ) )
_closeParen = DocViewTokenDefinition( 'closeParen', pyparsing.Literal( ')' ) )
_whitespace = DocViewTokenDefinition( 'whitespace', pyparsing.Word( string.whitespace ) )







class LispStringStyleSheet (DVStringStyleSheet):
	tokeniser = DocViewTokeniser( [ _string, _openParen, _closeParen, _whitespace ] )

	def _setValueNode(text):
		return text

	def _setValueEmpty(token, parentDocNode, indexInParent, parentStyleSheet):
		parentStyleSheet.deleteChildByIndex( parentDocNode, indexInParent )

	_setValueAction = DVStyleSheetSetValueAction( _setValueNode, '', _setValueEmpty )



	_setValueTokenHandler = DVStyleSheetTokenHandler( 'string', _setValueAction )






class DVListSExpressionStyleSheet (DVListWrappedLineStyleSheet):
	elementSpacing = 10.0


	def beginDelimiter(self):
		return DTLabel( '(', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )

	def endDelimiter(self):
		return DTLabel( ')', font='Sans bold 11', colour=Colour3f( 0.0, 0.6, 0.0 ) )




def makeLispDocView(root, commandHistory):
	disp = StyleSheetDispatcher( LispStringStyleSheet(), DVListSExpressionStyleSheet() )
	view = DocView( root, commandHistory, disp )
	return view



