##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Uitl.NodeUtil import isListNode


from Britefury.gSym.View.gSymView import activeBorder, border, indent, highlight, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, flow, flowSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

from Britefury.gSym.View.Interactor import keyEventMethod, accelEventMethod, textEventMethod, backspaceStartMethod, deleteEndMethod, Interactor

from Britefury.gSym.View.EditOperations import replace, append, prepend, insertBefore, insertAfter

from Britefury.gSym.View.UnparsedText import UnparsedText

import traceback


from GSymCore.Languages.LISP import Parser
from GSymCore.Languages.LISP.Styles import *




def _parseText(text):
	res, pos = Parser.parser.parseString( text )
	if res is not None:
		if pos == len( text ):
			return res.result
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None


class ParsedNodeInteractor (Interactor):
	@textEventMethod()
	def tokData(self, bUserEvent, bChanged, value, node):
		if bChanged:
			parsed = _parseText( value )
			if parsed is not None:
				replace( node, parsed )
			else:
				replace( node, value )
	
	eventMethods = [ tokData ]

	
	
	
def nodeEditor(node, contents, text, state):
	return interact( focus( customEntry( highlight( contents ), text.getText() ) ),  ParsedNodeInteractor( node ) ),   text


def stringNodeEditor(node, text, state):
	return interact( focus( customEntry( highlight( label( text.getText() ) ), text.getText() ) ),  ParsedNodeInteractor( node ) )



MODE_HORIZONTAL = 0
MODE_VERTICALINLINE = 1
MODE_VERTICAL = 2


def viewStringNode(node, state):
	res, pos = Parser.unquotedString.parseString( node )
	if res is not None:
		unparsed = UnparsedText( node )
	else:
		unparsed = UnparsedText( repr( node ) )
	# String
	return stringNodeEditor( node, unparsed, state )


def lispViewEval(node, state):
	if isListNode( node ):
		return viewEval( node )
	else:
		return viewStringNode( node, state )


def viewLispNode(node, state):
	if isListNode( node ):
		# List
		xViews = [ lispViewEval( x, state )   for x in node ]
		
		# Check the contents:
		mode = MODE_HORIZONTAL
		if len( node ) > 0:
			if isListNode( node[0] ):
				mode = MODE_VERTICAL
			else:
				for x in node[1:]:
					if isListNode( x ):
						mode = MODE_VERTICALINLINE
						break
		
			
		if mode == MODE_HORIZONTAL:
			layout = HorizontalListViewLayout( 10.0, 0.0 )
		elif mode == MODE_VERTICALINLINE:
			layout = VerticalInlineListViewLayout( 0.0, 0.0, 10.0 )
		elif mode == MODE_VERTICAL:
			layout = VerticalListViewLayout( 0.0, 0.0, 10.0 )
		else:
			raise ValueError
		v = listView( layout, label( '(', punctuationStyle ), label( ')', punctuationStyle ), None, xViews )
		
		def _text(x, n):
			if isListNode( n ):
				return x.text
			else:
				return n
		unparsed = UnparsedText( '(' + UnparsedText( ' ' ).join( [ _text( xv, n )   for xv, n in zip( xViews, node ) ] )  +  ')' )
		return nodeEditor( node, v, unparsed, state )
	else:
		raise TypeError
	
	
	
def LISPView():
	return viewLispNode