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

from BritefuryJ.SequentialEditor import SequentialEditor, SelectionEditTreeEvent, EditListener, ParsingEditListener
from BritefuryJ.SequentialEditor.StreamEditListener import HandleEditResult



from Britefury.Util.NodeUtil import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SequentialEditor import PythonSequentialEditor, PythonIndentationTreeEvent



class PythonParsingEditListener (ParsingEditListener):
	_outerPrecedence = None
	
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance
	
	def postParseResult(self, value):
		return removeUnNeededParens( value, self._outerPrecedence )



class PythonEditListener (EditListener):
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance


#
#
# EDIT LISTENERS
#
#

class ParsedExpressionEditListener (PythonParsingEditListener):
	__slots__ = [ '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence):
		super( ParsedExpressionEditListener, self ).__init__( parser )
		self._outerPrecedence = outerPrecedence
	
		
	def testValueEmpty(self, element, fragment, model, value):
		return value.isTextual()  and  value.textualValue().strip() == ''
	
	
	def handleEmptyValue(self, element, fragment, event, model):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - deleted' ).hItem( 'parser', self.parser ) )
		return HandleEditResult.NOT_HANDLED
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ) )
		if parsed != model:
			pyReplaceExpression( fragment, model, parsed )
		return HandleEditResult.HANDLED
		
		
		


class PythonExpressionEditListener (PythonParsingEditListener):
	__slots__ = [ '_outerPrecedence' ]
	
	def __init__(self, parser, outerPrecedence):
		super( PythonExpressionEditListener, self ).__init__( parser )
		self._outerPrecedence = outerPrecedence
	
		
	def handleEmptyValue(self, element, fragment, event, model):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Expression - deleted' ).hItem( 'parser', self.parser ) )
		model['expr'] = None
		return HandleEditResult.HANDLED
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Top level expression - success' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ) )
		expr = model['expr']
		if parsed != expr:
			if expr is None:
				model['expr'] = parsed
			else:
				pyReplaceExpression( fragment, expr, parsed )
		return HandleEditResult.HANDLED
		
	def handleParseFailure(self, element, sourceElement, fragment, event, model, value):
		if '\n' not in value:
			unparsed = Schema.UNPARSED( value=value.getItemValues() )
			log = fragment.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25ExprEdit' ).hItem( 'description', 'Top level expression - unparsed' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', unparsed ) )
			expr = model['expr']
			if expr is None:
				model['expr'] = unparsed
			else:
				pyReplaceExpression( fragment, expr, unparsed )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NOT_HANDLED


	
		
		


class StatementEditListener (PythonParsingEditListener):
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		if not isCompoundStmtHeader( model )  and  not isCompoundStmtHeader( parsed ):
			log = fragment.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ) )
			pyReplaceStmt( fragment, model, parsed )
			return HandleEditResult.HANDLED
		else:
			event.getStreamValueVisitor().setElementFixedValue( element, parsed )
			return HandleEditResult.PASS_TO_PARENT




class StatementUnparsedEditListener (PythonParsingEditListener):
	def handleParseFailure(self, element, sourceElement, fragment, event, model, value):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement (unparsed) - could not parse - passing up' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ) )
		# Pass further up:

		# Replacing the node with itself ensures that the view of this node will be rebuilt,
		# due to the modification event being sent.
		# It is necessary to do this, as the text of the statement has been edited;
		# leaving the existing view intact will result in the parent node reparsing the
		# modified text.
		# This normally leads to blank lines doubling on each press of the return key
		#
		# TODO: IMPROVE THIS TECHNIQUE - THIS IS A HACK
		pyReplaceStmt( fragment, model, model, False )
		
		return HandleEditResult.NOT_HANDLED

	
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		# Statement has been replaced by unparsed content
		# Only edit the innermost node around the element that is the source of the event
		sourceFragment = sourceElement.getFragmentContext()
		if sourceFragment is None:
			print 'NULL SOURCE CONTEXT: ', sourceElement
		if sourceFragment is fragment:
			log = fragment.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, node replaced' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ) )
			pyReplaceNode( fragment, model, parsed )
			return HandleEditResult.HANDLED
		else:
			sourceFragmentElement = sourceFragment.getFragmentContentElement()
			sourceNode = sourceFragment.getModel()
			sourceValue = sourceFragmentElement.getStreamValue()
			
			if sourceValue.isTextual():
				if sourceValue.textualValue().strip() == '':
					# The content within @sourceFragmentElement has been deleted entirely, replace the whole statement
					log = fragment.getView().getPageLog()
					if log.isRecording():
						log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node deleted' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ).vItem( 'sourceNode', sourceNode ) )
					pyReplaceStmt( fragment, model, parsed )
					return HandleEditResult.HANDLED
			
			unparsed = Schema.UNPARSED( value=sourceValue.getItemValues() )
			log = fragment.getView().getPageLog()
			if log.isRecording():
				log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Statement - unparsed, sub-node replaced' ).vItem( 'editedStream', sourceValue ).hItem( 'parser', self.parser ).vItem( 'parsedResult', unparsed ) )
			pyReplaceNode( sourceFragment, sourceNode, unparsed )
			return HandleEditResult.HANDLED






class CompoundHeaderEditListener (PythonParsingEditListener):
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		event.getStreamValueVisitor().setElementFixedValue( element, parsed )
		# Only partially handled - pass it up
		return HandleEditResult.NOT_HANDLED





class SuiteEditListener (PythonParsingEditListener):
	__slots__ = [ '_suite' ]

	
	def __init__(self, parser, suite):
		super( SuiteEditListener, self ).__init__( parser )
		self._suite = suite
	
		
	def clearFixedValuesOnPath(self):
		return False
	
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse SUCCESS' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ).vItem( 'parsedResult', parsed ) )
		# Alter the value of the existing suite so that it becomes the same as the parsed result, but minimise the number of changes required to do so
		modifySuiteMinimisingChanges( self._suite, parsed )
		return HandleEditResult.HANDLED
	
	def handleParseFailure(self, element, sourceElement, fragment, event, model, value):
		log = fragment.getView().getPageLog()
		if log.isRecording():
			log.log( LogEntry( 'Py25Edit' ).hItem( 'description', 'Suite - parse FAIL - passing to parent' ).vItem( 'editedStream', value ).hItem( 'parser', self.parser ) )
		return HandleEditResult.NOT_HANDLED







class StatementIndentationInteractor (KeyElementInteractor):
	def __init__(self):
		pass
		
		
	def keyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getFragmentContext()
			node = context.getModel()
			
			editor = element.getRegion().getClipboardHandler().getSequentialEditor()
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				editor.dedent( element, context, node )
			else:
				editor.indent( element, context, node )
			
			return True
		else:
			return False
		
		
	def keyPressed(self, element, event):
		return False
	
	def keyReleased(self, element, event):
		return False
	
	
	
	
class PythonModuleTopLevelEditListener (PythonEditListener):
	def handleEditEvent(self, element, sourceElement, event):
		if isinstance( event, TextEditEvent ):
			event.revert()
		return True

PythonModuleTopLevelEditListener.instance = PythonModuleTopLevelEditListener()




class PythonSuiteTopLevelEditListener (PythonEditListener):
	def handleEditEvent(self, element, sourceElement, event):
		if isinstance( event, TextEditEvent ):
			event.revert()
		return True

PythonSuiteTopLevelEditListener.instance = PythonSuiteTopLevelEditListener()




class PythonExpressionNewLineEvent (object):
	def __init__(self, model):
		self.model = model


class PythonExpressionTopLevelEditListener (PythonEditListener):
	def handleEditEvent(self, element, sourceElement, event):
		if isinstance( event, TextEditEvent ):
			value = element.getStreamValue()
			fragment = element.getFragmentContext()
			model = fragment.getModel()
			if '\n' in value:
				element.postTreeEvent( PythonExpressionNewLineEvent( model ) )
				pyReplaceExpression( fragment, model, model )
				event.revert()
				return True
			else:
				pyReplaceExpression( fragment, model, model )
				event.revert()
				return True
		elif isinstance( event, SelectionEditTreeEvent ):
			return True
		else:
			return False

PythonExpressionTopLevelEditListener.instance = PythonExpressionTopLevelEditListener()






