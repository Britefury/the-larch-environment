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

from BritefuryJ.GSym.SequentialEditor import SequentialParsingTreeEventListener


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations
from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SelectionEditor import PythonSelectionEditTreeEvent, PythonIndentationTreeEvent




class PythonParsingTreeEventListener (SequentialParsingTreeEventListener):
	_outerPrecedence = None
	
	def getSelectionEditTreeEventClass(self):
		return PythonSelectionEditTreeEvent
	
	def postParseResult(self, value):
		return removeUnNeededParens( value, self._outerPrecedence )
#
#
# EDIT LISTENERS
#
#

class ParsedExpressionTreeEventListener (PythonParsingTreeEventListener):
	__slots__ = [ '_parser', '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence):
		super( ParsedExpressionTreeEventListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence
	
		
	def testValueEmpty(self, element, fragment, model, value):
		return value.isTextual()  and  value.textualValue().strip() == ''
	
	
	def getParser(self):
		return self._parser
	
	
	def handleEmptyValue(self, element, fragment, event, model):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - deleted' ).hItem( 'parser', self._parser ) )
		return False
	
	def handleParseSuccess(self, element, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
		if parsed != model:
			pyReplaceExpression( fragment, model, parsed )
		return True
		
		
		


class PythonExpressionTreeEventListener (PythonParsingTreeEventListener):
	__slots__ = [ '_parser', '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence):
		super( PythonExpressionTreeEventListener, self ).__init__()
		self._parser = parser
		self._outerPrecedence = outerPrecedence
	
		
	def getParser(self):
		return self._parser
	
	
	def handleEmptyValue(self, element, fragment, event, model):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Expression - deleted' ).hItem( 'parser', self._parser ) )
		model['expr'] = None
		return True
	
	def handleParseSuccess(self, element, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Top level expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
		expr = model['expr']
		if parsed != expr:
			if expr is None:
				model['expr'] = parsed
			else:
				pyReplaceExpression( fragment, expr, parsed )
		return True
		
	def handleParseFailure(self, element, fragment, event, model, value):
		unparsed = Schema.UNPARSED( value=value.getItemValues() )
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Top level expression - unparsed' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', unparsed ) )
		expr = model['expr']
		if expr is None:
			model['expr'] = unparsed
		else:
			pyReplaceExpression( fragment, expr, unparsed )
		return True


	
		
		


class StructuralExpressionTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( TextEditEvent )
	def onTextEditEvent(self, element, sourceElement, event):
		element.clearFixedValue()
		return False
		
	
StructuralExpressionTreeEventListener.instance = StructuralExpressionTreeEventListener()




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
		fragment = element.getFragmentContext()
		node = fragment.getModel()
		# Get the content
		value = element.getStreamValue()
		parsed = parseStream( self._parser, value )
		if parsed is not None:
			return self.handleParsed( element, sourceElement, fragment, node, value, parsed, event )
		else:
			log = fragment.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - could not parse - passing up' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ) )
			# Pass further up:

			# Replacing the node with itself ensures that the view of this node will be rebuilt,
			# due to the modification event being sent.
			# It is necessary to do this, as the text of the statement has been edited;
			# leaving the existing view intact will result in the parent node reparsing the
			# modified text.
			# This normally leads to blank lines doubling on each press of the return key
			pyReplaceStmt( fragment, node, node, False )
			
			return element.postTreeEventToParent( event )

		
	def handleParsed(self, element, sourceElement, fragment, node, value, parsed, event):
		if not isCompoundStmtOrCompoundHeader( node )  and  not isCompoundStmtOrCompoundHeader( parsed ):
			if isUnparsed( parsed ):
				# Statement has been replaced by unparsed content
				# Only edit the innermost node around the element that is the source of the event
				sourceFragment = sourceElement.getFragmentContext()
				if sourceFragment is None:
					print 'NULL SOURCE CONTEXT: ', sourceElement
				if sourceFragment is fragment:
					log = fragment.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, node replaced' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
					pyReplaceNode( fragment, node, parsed )
					return True
				else:
					sourceFragmentElement = sourceFragment.getFragmentContentElement()
					sourceNode = sourceFragment.getModel()
					sourceValue = sourceFragmentElement.getStreamValue()
					
					if sourceValue.isTextual():
						if sourceValue.textualValue().strip() == '':
							# The content within @sourceFragmentElement has been deleted entirely, replace the whole statement
							log = fragment.getView().getPageLog()
							if log.isRecording():
								log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node deleted' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ).vItem( 'sourceNode', sourceNode ) )
							pyReplaceStmt( fragment, node, parsed )
							return True
					
					unparsed = Schema.UNPARSED( value=sourceValue.getItemValues() )
					log = fragment.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node replaced' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self._parser ).vItem( 'parsedResult', unparsed ) )
					pyReplaceNode( sourceFragment, sourceNode, unparsed )
					return True
			else:
				log = fragment.getView().getPageLog()
				if log.isRecording():
					log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
				pyReplaceStmt( fragment, node, parsed )
				return True
		else:
			if isCompoundStmt( node )  or isCompoundStmt( parsed ):
				print 'StatementTreeEventListener attempted to handle a compound node'
			element.setFixedValue( parsed )
			return element.postTreeEventToParent( event )
			
			
			

class CompoundHeaderTreeEventListener (PythonParsingTreeEventListener):
	__slots__ = [ '_parser' ]
	
	def __init__(self, parser):
		super( CompoundHeaderTreeEventListener, self ).__init__()
		self._parser = parser
	
		
	def getParser(self):
		return self._parser
	
	
	def handleParseSuccess(self, element, fragment, event, model, value, parsed):
		element.setFixedValue( parsed )
		# Only partially handled - pass it up
		return False





class SuiteTreeEventListener (PythonParsingTreeEventListener):
	__slots__ = [ '_parser', '_suite' ]

	
	def __init__(self, parser, suite):
		super( SuiteTreeEventListener, self ).__init__()
		self._parser = parser
		self._suite = suite
	
		
	def getParser(self):
		return self._parser
	
	def clearFixedValuesOnPath(self):
		return False
	
	
	def isEditEvent(self, event):
		return isinstance( event, PythonIndentationTreeEvent )
	
	
	def handleParseSuccess(self, element, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse SUCCESS' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ).vItem( 'parsedResult', parsed ) )
		# Alter the value of the existing suite so that it becomes the same as the parsed result, but minimise the number of changes required to do so
		modifySuiteMinimisingChanges( self._suite, parsed )
		return True
	
	def handleParseFailure(self, element, fragment, event, model, value):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse FAIL - passing to parent' ).vItem( 'editedStream', value ).hItem( 'parser', self._parser ) )
		return False







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
	
	
	
	
class PythonModuleTopLevelTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( PythonSelectionEditTreeEvent, PythonIndentationTreeEvent, TextEditEvent )
	def onEditEvent(self, element, sourceElement, event):
		return True

PythonModuleTopLevelTreeEventListener.instance = PythonModuleTopLevelTreeEventListener()




class PythonSuiteTopLevelTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( PythonSelectionEditTreeEvent, PythonIndentationTreeEvent, TextEditEvent )
	def onEditEvent(self, element, sourceElement, event):
		return True

PythonSuiteTopLevelTreeEventListener.instance = PythonSuiteTopLevelTreeEventListener()




class PythonExpressionNewLineEvent (object):
	def __init__(self, model):
		self.model = model


class PythonExpressionTopLevelTreeEventListener (TreeEventListenerObjectDispatch):
	@ObjectDispatchMethod( PythonSelectionEditTreeEvent )
	def onSelectionEditEvent(self, element, sourceElement, event):
		return True

	@ObjectDispatchMethod( TextEditEvent )
	def onTextEditEvent(self, element, sourceElement, event):
		value = element.getStreamValue()
		if '\n' in value:
			element.postTreeEvent( PythonExpressionNewLineEvent( element.getFragmentContext().getModel() ) )
			return True
		else:
			return True

PythonExpressionTopLevelTreeEventListener.instance = PythonExpressionTopLevelTreeEventListener()






