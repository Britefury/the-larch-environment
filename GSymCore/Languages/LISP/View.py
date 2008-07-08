##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.GLisp.GLispUtil import isGLispList


from Britefury.gSym.View.gSymView import activeBorder, border, indent, highlight, hline, label, markupLabel, entry, markupEntry, customEntry, hbox, ahbox, vbox, flow, flowSep, \
     script, scriptLSuper, scriptLSub, scriptRSuper, scriptRSub, listView, interact, focus, viewEval, mapViewEval, GSymView
from Britefury.gSym.View.ListView import FlowListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout

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



MODE_HORIZONTAL = 0
MODE_VERTICALINLINE = 1
MODE_VERTICAL = 2


def viewLispNode(node, state):
	if isGLispList( node ):
		# List
		xViews = mapViewEval( node )
		
		# Check the contents:
		mode = MODE_HORIZONTAL
		if len( node ) > 0:
			if isGLispList( node[0] ):
				mode = MODE_VERTICAL
			else:
				for x in node[1:]:
					if isGLispList( x ):
						mode = MODE_VERTICALINLINE
						break
		
			
		if mode == MODE_HORIZONTAL:
			layout = HorizontalListViewLayout( 10.0, 0.0 )
		elif mode == MODE_VERTICALINLINE:
			layout = VerticalInlineListViewLayout( 10.0, 0.0, 10.0 )
		elif mode == MODE_VERTICAL:
			layout = VerticalListViewLayout( 10.0, 0.0, 10.0 )
		else:
			raise ValueError
		v = listView( layout, label( '(', punctuationStyle ), label( ')', punctuationStyle ), None, xViews )
		unparsed = UnparsedText( '(' + UnparsedText( ' ' ).join( [ xv.text   for xv in xViews ] )  +  ')' )
		return nodeEditor( node, v, unparsed, state )
	else:
		res, pos = Parser.unquotedString.parseString( node )
		if res is not None:
			unparsed = UnparsedText( node )
		else:
			unparsed = UnparsedText( repr( node ) )
		# String
		return nodeEditor( node, label( node ), unparsed, state )
	
	
	
def LISPView():
	return viewLispNode