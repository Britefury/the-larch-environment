##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Util.NodeUtil import isListNode

from Britefury.gSym.View.GSymView import GSymView

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent.ElementTree import *

from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout


from GSymCore.Languages.LISP.Parser2 import LispGrammar
from GSymCore.Languages.LISP.Styles import *



_parser = LispGrammar()


def _parseText(text):
	res = _parser.expression().parseString( text )
	if res.isValid():
		pos = res.getEnd()
		if pos == len( text ):
			return res.getValue()
		else:
			print '<INCOMPLETE>'
			print 'FULL TEXT:', text
			print 'PARSED:', text[:pos]
			return None
	else:
		print 'FULL TEXT:', text
		print '<FAIL>'
		return None


class ParsedNodeContentListener (ElementContentListener):
	def __init__(self, ctx, node):
		#super( ParsedNodeContentListener, self ).__init__()
		self._ctx = ctx
		self._node = node

	def contentModified(self, element):
		value = element.getContent()
		parsed = _parseText( value )
		if parsed is not None:
			replace( self._ctx, self._node, parsed )
		else:
			replace( self._ctx, self._node, value )
		return True
		
		
	
	
def nodeEditor(ctx, node, contents, state):
	return ctx.contentListener( contents, ParsedNodeContentListener( ctx, node ) )


def stringNodeEditor(ctx, node, metadata, state):
	res = _parser.unquotedString().parseString( node.toString() )
	if res.isValid():
		nodeText = node.toString()
	else:
		nodeText = repr( node.toString() )
	return ctx.contentListener( ctx.text( string_textStyle, nodeText ), ParsedNodeContentListener( ctx, node ) )



MODE_HORIZONTAL = 0
MODE_VERTICALINLINE = 1
MODE_VERTICAL = 2


def viewStringNode(node, ctx, state):
	# String
	return stringNodeEditor( ctx, node, None, state )


def lispViewEval(node, ctx, state):
	if isListNode( node ):
		return ctx.viewEval( node )
	else:
		return viewStringNode( node, ctx, state )


def viewLispNode(node, ctx, state):
	if isListNode( node ):
		# List
		xViews = [ lispViewEval( x, ctx, state )   for x in node ]
		
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
			layout = horizontal_listViewLayout
		elif mode == MODE_VERTICALINLINE:
			layout = verticalInline_listViewLayout
		elif mode == MODE_VERTICAL:
			layout = vertical_listViewLayout
		else:
			raise ValueError
		v = ctx.listView( layout, lambda: ctx.text( punctuation_textStyle, '(' ), lambda: ctx.text( punctuation_textStyle, ')' ), None, xViews )
		
		return nodeEditor( ctx, node, v, state )
	else:
		raise TypeError, 'node is %s'  %  node
	
	
	
def LISPView():
	return viewLispNode