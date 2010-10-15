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


from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor


from BritefuryJ.Logging import LogEntry


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations
from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod


from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SelectionEditor import PythonSelectionEditTreeEvent, PythonIndentationTreeEvent




#
#
# EDIT LISTENERS
#
#

class ParsedExpressionTreeEventListener (TreeEventListenerObjectDispatch):
	__slots__ = [ '_parser', '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence, node=None):
		#super( ParsedExpressionTreeEventListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence


	@ObjectDispatchMethod( TextEditEvent, PythonSelectionEditTreeEvent )
	def editEvent(self, element, sourceElement, event):
		# if @event is a @PythonSelectionEditTreeEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it.
		# Otherwise, clear the structural represnetation of all elements on the path from the source element to @element
		if not ( isinstance( event, PythonSelectionEditTreeEvent )  and  event.getSourceElement() is element ):
			sourceElement.clearFixedValuesOnPathUpTo( element )
			element.clearFixedValue()
		value = element.getStreamValue()
		ctx = element.getFragmentContext()
		node = ctx.getModel()
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
		
	
		
		


class PythonExpressionTreeEventListener (TreeEventListenerObjectDispatch):
	__slots__ = [ '_parser' ]
	
	def __init__(self, parser, outerPrecedence):
		super( PythonExpressionTreeEventListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence


	@ObjectDispatchMethod( TextEditEvent, PythonSelectionEditTreeEvent )
	def editEvent(self, element, sourceElement, event):
		# if @event is a @PythonSelectionEditTreeEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it.
		# Otherwise, clear the structural represnetation of all elements on the path from the source element to @element
		if not ( isinstance( event, PythonSelectionEditTreeEvent )  and  event.getSourceElement() is element ):
			sourceElement.clearFixedValuesOnPathUpTo( element )
			element.clearFixedValue()
		value = element.getStreamValue()
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		nodeExpr = node['expr']
		if '\n' not in value:
			if value.isEmpty():
				node['expr'] = None
				return True
			else:
				parsed = parseStream( self._parser, value, self._outerPrecedence )
				if parsed is not None:
					log = ctx.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
					if parsed != nodeExpr:
						if nodeExpr is None:
							node['expr'] = parsed
						else:
							pyReplaceExpression( ctx, nodeExpr, parsed )
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
					if nodeExpr is None:
						node['expr'] = unparsed
					else:
						pyReplaceExpression( ctx, nodeExpr, unparsed )
				return True
		else:
			return False
		
	
		
		


class StructuralExpressionTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( TextEditEvent )
	def onTextEditEvent(self, element, sourceElement, event):
		element.clearFixedValue()
		return False
		
	
StructuralExpressionTreeEventListener.instance = StructuralExpressionTreeEventListener()




class DeleteSpecialFormExpressionTreeEvent (object):
	def __init__(self, expr):
		self._expr = expr

		
class SpecialFormExpressionTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( DeleteSpecialFormExpressionTreeEvent )
	def onDeleteExtExpr(self, element, sourceElement, event):
		pyReplaceExpression( element.getFragmentContext(), event._expr, Schema.Load( name='None' ) )
		return True
		
	
SpecialFormExpressionTreeEventListener.instance = SpecialFormExpressionTreeEventListener()




class StatementTreeEventListener (TreeEventListenerObjectDispatch):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser

		
	@ObjectDispatchMethod( TextEditEvent, PythonSelectionEditTreeEvent )
	def onEditEvent(self, element, sourceElement, event):
		# if @event is a @PythonSelectionEditTreeEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it.
		# Otherwise, clear the structural represnetation of all elements on the path from the source element to @element
		if not ( isinstance( event, PythonSelectionEditTreeEvent )  and  event.getSourceElement() is element ):
			sourceElement.clearFixedValuesOnPathUpTo( element )
			element.clearFixedValue()
		ctx = element.getFragmentContext()
		node = ctx.getModel()
		# Get the content
		value = element.getStreamValue()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, sourceElement, ctx, node, value, parsed, event )
		else:
			log = ctx.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - could not parse - passing up' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ) )
			# Pass further up:

			# Replacing the node with itself ensures that the view of this node will be rebuilt,
			# due to the modification event being sent.
			# It is necessary to do this, as the text of the statement has been edited;
			# leaving the existing view intact will result in the parent node reparsing the
			# modified text.
			# This normally leads to blank lines doubling on each press of the return key
			pyReplaceStmt( ctx, node, node, False )
			
			return element.postTreeEventToParent( event )

		
	def handleParsed(self, element, sourceElement, ctx, node, value, parsed, event):
		if not isCompoundStmtOrCompoundHeader( node )  and  not isCompoundStmtOrCompoundHeader( parsed ):
			if isUnparsed( parsed ):
				# Statement has been replaced by unparsed content
				# Only edit the innermost node around the element that is the source of the event
				sourceCtx = sourceElement.getFragmentContext()
				if sourceCtx is None:
					print 'NULL SOURCE CONTEXT: ', sourceElement
				if sourceCtx is ctx:
					log = ctx.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, node replaced' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
					pyReplaceNode( ctx, node, parsed )
					return True
				else:
					sourceCtxElement = sourceCtx.getFragmentContentElement()
					sourceNode = sourceCtx.getModel()
					sourceValue = sourceCtxElement.getStreamValue()
					
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
			element.setFixedValue( parsed )
			return element.postTreeEventToParent( event )
			
			
			

class CompoundHeaderTreeEventListener (TreeEventListenerObjectDispatch):
	__slots__ = [ '_parser' ]

	
	def __init__(self, parser):
		self._parser = parser


	@ObjectDispatchMethod( PythonSelectionEditTreeEvent, TextEditEvent )
	def onEditEvent(self, element, sourceElement, event):
		# if @event is a @PythonSelectionEditTreeEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it
		if not isinstance( event, PythonSelectionEditTreeEvent )  or  event.getSourceElement() is not element:
			element.clearFixedValue()
		ctx = element.getFragmentContext()
		# Get the content
		value = element.getStreamValue()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, value, parsed, event )
		else:
			return element.postTreeEventToParent( event )

		
	def handleParsed(self, element, value, parsed, event):
		element.setFixedValue( parsed )
		return element.postTreeEventToParent( event )
			

	

			
class SuiteTreeEventListener (TreeEventListenerObjectDispatch):
	__slots__ = [ '_parser', '_suite' ]

	
	def __init__(self, parser, suite):
		self._parser = parser
		self._suite = suite

	
	@ObjectDispatchMethod( PythonSelectionEditTreeEvent, PythonIndentationTreeEvent, TextEditEvent )
	def onEditEvent(self, element, sourceElement, event):
		# if @event is a @PythonSelectionEditTreeEvent, and its source element is @element, then @element has had its
		# structural representation set to a value, in an inner invokation of a linearRepresentationModified method, so don't clear it
		if not isinstance( event, PythonSelectionEditTreeEvent )  or  event.getSourceElement() is not element:
			element.clearFixedValue()
		ctx = element.getFragmentContext()
		# Get the content
		value = element.getStreamValue()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			log = ctx.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse SUCCESS' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
			# Alter the value of the existing suite so that it becomes the same as the parsed result, but minimise the number of changes required to do so
			modifySuiteMinimisingChanges( self._suite, parsed )
			return True
		else:
			log = ctx.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse FAIL - passing to parent' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ) )
			return element.postTreeEventToParent( event )

			
			
			
			
	

class StatementIndentationInteractor (KeyElementInteractor):
	def __init__(self):
		pass
		
		
	def keyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getFragmentContext()
			node = context.getModel()
			
			editHandler = element.getRegion().getEditHandler()
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				editHandler.dedent( element, context, node )
			else:
				editHandler.indent( element, context, node )
			
			return True
		else:
			return False
		
		
	def keyPressed(self, element, event):
		return False
	
	def keyReleased(self, element, event):
		return False
	
	
	
	
