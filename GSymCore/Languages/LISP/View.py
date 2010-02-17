##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.Util.NodeUtil import isListNode, isObjectNode, isStringNode

from Britefury.gSym.gSymResolveContext import GSymResolveContext
from Britefury.gSym.gSymResolveResult import GSymResolveResult
from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter
from Britefury.gSym.View.GSymView import GSymViewPage


from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *

from BritefuryJ.GSym.View import GSymViewContext


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

	def textRepresentationModified(self, element, event):
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
		
		# Get the list view style sheet
		if mode == MODE_HORIZONTAL:
			listViewStyleSheet = paragraph_listViewStyle
		elif mode == MODE_VERTICAL:
			listViewStyleSheet = vertical_listViewStyle
		else:
			raise ValueError
		
		# Create a list view
		v = listViewStyleSheet.createListElement( xViews ) 
		
		return nodeEditor( ctx, node, v, state )
	elif isObjectNode( node ):
		cls = node.getDMObjectClass()
		
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
			className = defaultStyle.span( [ classNameStyle.text( cls.getName() ), stringStyle.text( ' ' ), punctuationStyle.text( ':' ) ] )
		elif mode == MODE_VERTICALINLINE:
			className = defaultStyle.paragraph( [ classNameStyle.text( cls.getName() ), stringStyle.text( ' ' ), punctuationStyle.text( ':' ) ] )
		else:
			raise ValueError, 'invalid mode'
		
		itemViews = [ className ]
		# Create views of each item
		for i in xrange( 0, cls.getNumFields() ):
			value = node.get( i )
			fieldName = cls.getField( i ).getName()
			if value is not None:
				if mode == MODE_HORIZONTAL:
					line = defaultStyle.span( [ fieldNameStyle.text( fieldName ), punctuationStyle.text( '=' ), lispViewEval( value, ctx, state ) ] )
				elif mode == MODE_VERTICALINLINE:
					line = defaultStyle.paragraph( [ fieldNameStyle.text( fieldName ), punctuationStyle.text( '=' ), lispViewEval( value, ctx, state ) ] )
				else:
					raise ValueError, 'invalid mode'
				itemViews.append( line )
				
		# Create the layout
		if mode == MODE_HORIZONTAL:
			listViewStyleSheet = paragraph_objectViewStyle
		elif mode == MODE_VERTICALINLINE:
			listViewStyleSheet = verticalInline_objectViewStyle
		else:
			raise ValueError
		
		# Create a list view
		v = listViewStyleSheet.createListElement( itemViews )
		return nodeEditor( ctx, node, v, state )
	else:
		raise TypeError, 'node is %s'  %  node
	
	
	


def viewLISPDocNodeAsElement(document, docNode, resolveContext, location, commandHistory, app):
	viewContext = GSymViewContext( docNode, viewLispNode, defaultStyle, commandHistory )
	return viewContext.getFrame()



def viewLISPDocNodeAsPage(document, docNode, resolveContext, location, commandHistory, app):
	return GSymViewPage( 'Model: ' + resolveContext.getTitle(), viewLISPDocNodeAsElement( document, docNode, resolveContext, location, commandHistory, app ), commandHistory )


def resolveLISPLocation(currentUnitClass, document, docRootNode, resolveContext, location, app):
	return GSymResolveResult( document, docRootNode, currentUnitClass, resolveContext, location )
