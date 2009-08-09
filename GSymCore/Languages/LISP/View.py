##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Util.NodeUtil import isListNode, isObjectNode, isStringNode

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Browser import Page
from BritefuryJ.DocPresent.StyleSheets import *

from BritefuryJ.GSym.View import GSymViewInstance
from BritefuryJ.GSym.View.ListView import ParagraphListViewLayout, HorizontalListViewLayout, VerticalInlineListViewLayout, VerticalListViewLayout


from GSymCore.Languages.LISP.Parser2 import LispGrammar
from GSymCore.Languages.LISP.Styles import *



_parser = LispGrammar()


def _parseText(text):
	res = _parser.expression().parseStringChars( text )
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


class ParsedNodeTextRepresentationListener (ElementLinearRepresentationListener):
	def __init__(self, ctx, node):
		#super( ParsedNodeTextRepresentationListener, self ).__init__()
		self._ctx = ctx
		self._node = node

	def textRepresentationModified(self, element):
		value = element.getTextRepresentation()
		parsed = _parseText( value )
		if parsed is not None:
			replace( self._ctx, self._node, parsed )
		else:
			replace( self._ctx, self._node, value )
		return True
		
		
	
	
def nodeEditor(ctx, node, contents, state):
	return ctx.linearRepresentationListener( contents, ParsedNodeTextRepresentationListener( ctx, node ) )


def stringNodeEditor(ctx, node, metadata, state):
	res = _parser.unquotedString().parseStringChars( node )
	if res.isValid():
		nodeText = node
	else:
		nodeText = repr( node )
	return ctx.linearRepresentationListener( ctx.text( string_textStyle, nodeText ), ParsedNodeTextRepresentationListener( ctx, node ) )



MODE_HORIZONTAL = 0
MODE_VERTICALINLINE = 1
MODE_VERTICAL = 2


def viewStringNode(node, ctx, state):
	# String
	return stringNodeEditor( ctx, node, None, state )


def lispViewEval(node, ctx, state):
	if isStringNode( node ):
		return viewStringNode( node, ctx, state )
	else:
		return ctx.viewEval( node )


def viewLispNode(node, ctx, state):
	if isListNode( node ):
		# List
		xViews = [ lispViewEval( x, ctx, state )   for x in node ]
		
		# Check the contents, to determine the layout
		mode = MODE_HORIZONTAL
		if len( node ) > 0:
			for x in node:
				if not isStringNode( x ):
					mode = MODE_VERTICAL
					break
		
		# Create the layout
		if mode == MODE_HORIZONTAL:
			layout = paragraph_listViewLayout
		elif mode == MODE_VERTICAL:
			layout = vertical_listViewLayout
		else:
			raise ValueError
		
		# Create a list view
		v = ctx.listView( layout, lambda: ctx.text( punctuation_textStyle, '[' ), lambda: ctx.text( punctuation_textStyle, ']' ), None, xViews )
		
		return nodeEditor( ctx, node, v, state )
	elif isObjectNode( node ):
		cls = node.getDMClass()
		
		# Determine how this node is to be displayed
		mode = MODE_HORIZONTAL
		for i in xrange( 0, cls.getNumFields() ):
			value = node.get( i )
			if value is not None:
				# If we encounter a non-string value, then this object cannot be displayed in a single line
				if not isStringNode( value ):
					mode = MODE_VERTICALINLINE
					break
		
		# Header
		if mode == MODE_HORIZONTAL:
			className = ctx.span( [ ctx.text( className_textStyle, cls.getName() ), ctx.text( string_textStyle, ' ' ), ctx.text( punctuation_textStyle, ':' ) ] )
		elif mode == MODE_VERTICALINLINE:
			className = ctx.paragraph( lisp_paragraphStyle, [ ctx.text( className_textStyle, cls.getName() ), ctx.text( string_textStyle, ' ' ), ctx.text( punctuation_textStyle, ':' ) ] )
		else:
			raise ValueError, 'invalid mode'
		
		itemViews = [ className ]
		# Create views of each item
		for i in xrange( 0, cls.getNumFields() ):
			value = node.get( i )
			fieldName = cls.getField( i ).getName()
			if value is not None:
				if mode == MODE_HORIZONTAL:
					line = ctx.span( [ ctx.text( fieldName_textStyle, fieldName ), ctx.text( punctuation_textStyle, '=' ), lispViewEval( value, ctx, state ) ] )
				elif mode == MODE_VERTICALINLINE:
					line = ctx.paragraph( lisp_paragraphStyle, [ ctx.text( fieldName_textStyle, fieldName ), ctx.text( punctuation_textStyle, '=' ), lispViewEval( value, ctx, state ) ] )
				else:
					raise ValueError, 'invalid mode'
				itemViews.append( line )
				
		# Create the layout
		if mode == MODE_HORIZONTAL:
			layout = paragraph_listViewLayout
		elif mode == MODE_VERTICALINLINE:
			layout = verticalInline_listViewLayout
		else:
			raise ValueError
		
		# Create a list view
		v = ctx.listView( layout, lambda: ctx.text( punctuation_textStyle, '(' ), lambda: ctx.text( punctuation_textStyle, ')' ), None, itemViews )
		return nodeEditor( ctx, node, v, state )
	else:
		raise TypeError, 'node is %s'  %  node
	
	
	
class _LISPViewPage (Page):
	def __init__(self, docRootNode, location, commandHistory, app):
		self._docRootNode = docRootNode
		self._location = location
		self._app = app
		viewContext = GSymViewInstance( docRootNode, viewLispNode, self._viewRootFn, commandHistory, self )
		self._frame = viewContext.getFrame()
		
		
	def getContentsElement(self):
		return self._frame
		
		
	def _viewRootFn(self, node, ctx, state):
		return viewLispNode( node, ctx, state )

	


def viewLocationAsPage(docRootNode, location, commandHistory, app):
	return _LISPViewPage( docRootNode, location, commandHistory, app )
