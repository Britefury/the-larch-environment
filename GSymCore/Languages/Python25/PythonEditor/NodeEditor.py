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
from java.awt import Color

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.Transformation import DefaultIdentityTransformationFunction


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent import *


from BritefuryJ.Logging import LogEntry


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations


from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SelectionEditor import SelectionLinearRepresentationEvent





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
		# if @event is a @SelectionLinearRepresentationEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it.
		# Otherwise, clear the structural represnetation of all elements on the path from the source element to @element
		if not ( isinstance( event, SelectionLinearRepresentationEvent )  and  event.getSourceElement() is element ):
			event.getSourceElement().clearStructuralRepresentationsOnPathUpTo( element )
			element.clearStructuralRepresentation()
		value = element.getLinearRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		if '\n' not in value:
			parsed = parseStream( self._parser, value, self._outerPrecedence )
			if parsed is not None:
				log = ctx.getView().getPageLog()
				if log.isRecording():
					log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
				if parsed != node:
					pyReplaceExpression( ctx, node, parsed )
			else:
				if value.isTextual():
					if value.textualValue().strip() == '':
						# Expression content has been deleted entirely
						log = ctx.getView().getPageLog()
						if log.isRecording():
							log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - deleted' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
						return False
				unparsed = Schema.UNPARSED( value=value.getItemValues() )
				log = ctx.getView().getPageLog()
				if log.isRecording():
					log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - unparsed' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', unparsed ) )
				pyReplaceExpression( ctx, node, unparsed )
			return True
		else:
			return False
		
	
	_listenerTable = None
		
	@staticmethod
	def newListener(parser, outerPrecedence):
		if ParsedExpressionLinearRepresentationListener._listenerTable is None:
			ParsedExpressionLinearRepresentationListener._listenerTable = _ListenerTable( ParsedExpressionLinearRepresentationListener )
		return ParsedExpressionLinearRepresentationListener._listenerTable.get( parser, outerPrecedence )
		
		


class StructuralExpressionLinearRepresentationListener (ElementLinearRepresentationListener):
	def linearRepresentationModified(self, element, event):
		element.clearStructuralRepresentation()
		return False
		
	
	_listener = None
		
	@staticmethod
	def newListener():
		if StructuralExpressionLinearRepresentationListener._listener is None:
			StructuralExpressionLinearRepresentationListener._listener = StructuralExpressionLinearRepresentationListener()
		return StructuralExpressionLinearRepresentationListener._listener
		


class StatementLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser

		
	def linearRepresentationModified(self, element, event):
		# if @event is a @SelectionLinearRepresentationEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it.
		# Otherwise, clear the structural represnetation of all elements on the path from the source element to @element
		if not ( isinstance( event, SelectionLinearRepresentationEvent )  and  event.getSourceElement() is element ):
			event.getSourceElement().clearStructuralRepresentationsOnPathUpTo( element )
			element.clearStructuralRepresentation()
		ctx = element.getFragmentContext()
		node = ctx.getDocNode()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, ctx, node, value, parsed, event )
		else:
			# Pass further up:

			# Replacing the node with itself ensures that the view of this node will be rebuilt,
			# due to the modification event being sent.
			# It is necessary to do this, as the text of the statement has been edited;
			# leaving the existing view intact will result in the parent node reparsing the
			# modified text.
			# This normally leads to blank lines doubling on each press of the return key
			pyReplaceStmt( ctx, node, node, False )
			
			return element.sendLinearRepresentationModifiedEventToParent( event )

		
	def handleParsed(self, element, ctx, node, value, parsed, event):
		if not isCompoundStmtOrCompoundHeader( node )  and  not isCompoundStmtOrCompoundHeader( parsed ):
			if isUnparsed( parsed ):
				# Statement has been replaced by unparsed content
				# Only edit the innermost node around the element that is the source of the event
				sourceElement = event.getSourceElement()
				sourceCtx = sourceElement.getFragmentContext()
				if sourceCtx is None:
					print 'NULL SOURCE CONTEXT: ', sourceElement
				if sourceCtx is ctx:
					log = ctx.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'tatement - unparsed, node replaced' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
					pyReplaceNode( ctx, node, parsed )
					return True
				else:
					sourceCtxElement = sourceCtx.getFragmentContentElement()
					sourceNode = sourceCtx.getDocNode()
					sourceValue = sourceCtxElement.getLinearRepresentation()
					
					if sourceValue.isTextual():
						if sourceValue.textualValue().strip() == '':
							# The content within @sourceCtxElement has been deleted entirely, replace the whole statement
							log = ctx.getView().getPageLog()
							if log.isRecording():
								log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node deleted' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ).vItem( 'sourceNode', sourceNode ) )
							pyReplaceStmt( ctx, node, parsed )
							return True
					
					unparsed = Schema.UNPARSED( value=sourceValue.getItemValues() )
					log = ctx.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node replaced' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self._parser ).vItem( 'parsedResult', unparsed ) )
					pyReplaceNode( sourceCtx, sourceNode, unparsed )
					return True
			else:
				log = ctx.getView().getPageLog()
				if log.isRecording():
					log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
				pyReplaceStmt( ctx, node, parsed )
				return True
		else:
			element.setStructuralValueObject( parsed )
			return element.sendLinearRepresentationModifiedEventToParent( event )

			
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
		# if @event is a @SelectionLinearRepresentationEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it
		if not isinstance( event, SelectionLinearRepresentationEvent )  or  event.getSourceElement() is not element:
			element.clearStructuralRepresentation()
		ctx = element.getFragmentContext()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, value, parsed, event )
		else:
			return element.sendLinearRepresentationModifiedEventToParent( event )

		
	def handleParsed(self, element, value, parsed, event):
		element.setStructuralValueObject( parsed )
		return element.sendLinearRepresentationModifiedEventToParent( event )

			
	_listenerTable = None
		
	@staticmethod
	def newListener(parser):
		if CompoundHeaderLinearRepresentationListener._listenerTable is None:
			CompoundHeaderLinearRepresentationListener._listenerTable = _ListenerTable( CompoundHeaderLinearRepresentationListener )
		return CompoundHeaderLinearRepresentationListener._listenerTable.get( parser )
			

	

			
class SuiteLinearRepresentationListener (ElementLinearRepresentationListener):
	__slots__ = [ '_parser', '_suite' ]

	
	def __init__(self, parser, suite):
		self._parser = parser
		self._suite = suite

		
	def linearRepresentationModified(self, element, event):
		# if @event is a @SelectionLinearRepresentationEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it
		if not isinstance( event, SelectionLinearRepresentationEvent )  or  event.getSourceElement() is not element:
			element.clearStructuralRepresentation()
		ctx = element.getFragmentContext()
		# Get the content
		value = element.getLinearRepresentation()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( value, parsed )
		else:
			return element.sendLinearRepresentationModifiedEventToParent( event )


	def handleParsed(self, value, parsed):
		performSuiteEdits( self._suite, parsed )
		return True
			
			
			
			
	

class StatementIndentationInteractor (ElementInteractor):
	def __init__(self):
		pass
		
		
	def onKeyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getFragmentContext()
			node = context.getDocNode()
			
			editHandler = element.getRegion().getEditHandler()
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
	
	
	
	
