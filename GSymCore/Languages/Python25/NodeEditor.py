##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from weakref import WeakValueDictionary

from java.lang import Object, System
from java.io import IOException
from java.util import List
from java.awt.event import KeyEvent

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from BritefuryJ.DocPresent.StyleSheets import *
from BritefuryJ.DocPresent import *


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations


from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Parser import Python25Grammar
from GSymCore.Languages.Python25.Precedence import *
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25.PythonEditOperations import *







	
	
#
#
# EDIT LISTENERS
#
#

class _ListenerTable (object):
	def __init__(self, createFn):
		self._table = WeakValueDictionary()
		self._createFn = createFn
	
		
	def get(self, *args):
		key = args
		try:
			return self._table[key]
		except KeyError:
			listener = self._createFn( *args )
			self._table[key] = listener
			return listener
		
	
	
class ParsedExpressionLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser', '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence, node=None):
		#super( ParsedExpressionLinearRepresentationListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence

	def linearRepresentationModified(self, element, event):
		value = element.getLinearRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		if '\n' not in value:
			#parsed = parseText( self._parser, value, self._outerPrecedence )
			parsed = parseStream( self._parser, value, self._outerPrecedence )
			if parsed is not None:
				if parsed != node:
					pyReplaceExpression( ctx, node, parsed )
			else:
				pyReplaceExpression( ctx, node, Nodes.UNPARSED( value=value ) )
			return True
		else:
			return False
		
	
	_listenerTable = None
		
	@staticmethod
	def newListener(parser, outerPrecedence):
		if ParsedExpressionLinearRepresentationListener._listenerTable is None:
			ParsedExpressionLinearRepresentationListener._listenerTable = _ListenerTable( ParsedExpressionLinearRepresentationListener )
		return ParsedExpressionLinearRepresentationListener._listenerTable.get( parser, outerPrecedence )
		
		


class StatementLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser

		
	def linearRepresentationModified(self, element, event):
		element.clearStructuralRepresentation()
		ctx = element.getContext()
		node = ctx.getTreeNode()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, ctx, node, value, parsed, event )
		else:
			pyReplaceStmt( ctx, node, node, False )
			return element.passLinearRepresentationModifiedEventUpwards( event )

		
	def handleParsed(self, element, ctx, node, value, parsed, event):
		if not isCompoundStmtOrCompoundHeader( node )  and  not isCompoundStmtOrCompoundHeader( parsed ):
			pyReplaceStmt( ctx, node, parsed )
			return True
		else:
			element.setStructuralValueObject( parsed )
			return element.passLinearRepresentationModifiedEventUpwards( event )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if StatementLinearRepresentationListener._listenerTable is None:
			StatementLinearRepresentationListener._listenerTable = _ListenerTable( StatementLinearRepresentationListener )
		return StatementLinearRepresentationListener._listenerTable.get( parser )
			
			
			
			
class CompoundHeaderLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser

		
	def linearRepresentationModified(self, element, event):
		element.clearStructuralRepresentation()
		ctx = element.getContext()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, value, parsed, event )
		else:
			return element.passLinearRepresentationModifiedEventUpwards( event )

		
	def handleParsed(self, element, value, parsed, event):
		element.setStructuralValueObject( parsed )
		return element.passLinearRepresentationModifiedEventUpwards( event )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if CompoundHeaderLinearRepresentationListener._listenerTable is None:
			CompoundHeaderLinearRepresentationListener._listenerTable = _ListenerTable( CompoundHeaderLinearRepresentationListener )
		return CompoundHeaderLinearRepresentationListener._listenerTable.get( parser )
			
			
			
from BritefuryJ.Parser.ItemStream import ItemStream
from BritefuryJ.DocModel import DMIOWriter, DMNode
from java.util import ArrayList
def _writeValue(x):
	def _handleItem(i):
		if isinstance( i, ItemStream.TextItem ):
			return i.getTextValue()
		elif isinstance( i, ItemStream.StructuralItem ):
			return i.getStructuralValue()
		else:
			return None
	value = DMNode.coerce( [ _handleItem( i )   for i in x.getItems() ] )
	s = DMIOWriter.writeAsString( value )
	f = open( 'parsevalue.in', 'w' )
	f.write( s )
	f.close()
	
	
	
class SuiteLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser', '_suite' ]

	
	def __init__(self, parser, suite):
		self._parser = parser
		self._suite = suite

		
	def linearRepresentationModified(self, element, event):
		element.clearStructuralRepresentation()
		ctx = element.getContext()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( value, parsed )
		else:
			return element.passLinearRepresentationModifiedEventUpwards( event )


	def handleParsed(self, value, parsed):
		performSuiteEdits( self._suite, parsed )
		return True
			
			
			
			
	

class StatementKeyboardListener (ElementKeyboardListener):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getContext()
			node = context.getTreeNode()
			
			editHandler = context.getViewContext().getEditHandler()
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				editHandler.dedent( element, context, node )
			else:
				editHandler.indent( element, context, node )
			
			return True
		else:
			return False
		
		
	def onKeyPress(self, element, event):
		return False
	
	def onKeyRelease(self, element, event):
		return False
	
	
	
	
