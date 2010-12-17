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

from BritefuryJ.Editor.Sequential import SequentialEditor, SelectionEditTreeEvent, EditListener, ParsingEditListener, PartialParsingEditListener
from BritefuryJ.Editor.Sequential.StreamEditListener import HandleEditResult
from BritefuryJ.Editor.Language import UnparsedEditListener



from Britefury.Util.NodeUtil import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SequentialEditor import PythonSequentialEditor, PythonIndentationTreeEvent



class PythonParsingEditListener (ParsingEditListener):
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance

	
class PythonPartialParsingEditListener (PartialParsingEditListener):
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance



class PythonUnparsedEditListener (UnparsedEditListener):
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance


class PythonEditListener (EditListener):
	def getSequentialEditor(self):
		return PythonSequentialEditor.instance


#
#
# EDIT LISTENERS
#
#

class ParsedExpressionEditListener (PythonParsingEditListener):
	def getLogName(self):
		return 'Expression'
	
	
	def testValueEmpty(self, element, fragment, model, value):
		return value.isTextual()  and  value.textualValue().strip() == ''
	
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		pyReplaceNodeIfNotEqual( model, parsed )
		return HandleEditResult.HANDLED





class StatementEditListener (PythonParsingEditListener):
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		pyReplaceNodeIfNotEqual( model, parsed )
		return HandleEditResult.HANDLED



class CompoundStatementHeaderEditListener (PythonPartialParsingEditListener):
	def getLogName(self):
		return 'Compound statement header'


class StatementUnparsedEditListener (PythonUnparsedEditListener):
	def getLogName(self):
		return 'Statement'
	
	def testValue(self, element, sourceElement, fragment, event, model, value):
		i = value.indexOf( '\n' )
		return i != -1   and   i == len( value ) - 1
	
	def testValueEmpty(self, element, sourceElement, fragment, event, model, value):
		return value.isTextual()  and  value.textualValue().strip() == ''
	
	def handleUnparsed(self, element, sourceElement, fragment, event, model, value):
		unparsed = Schema.UNPARSED( value=value.getItemValues() )
		pyReplaceNode( model, unparsed )
		return HandleEditResult.HANDLED






class SuiteEditListener (PythonParsingEditListener):
	__slots__ = [ '_suite' ]

	
	def __init__(self, parser, suite):
		super( SuiteEditListener, self ).__init__( parser )
		self._suite = suite
		
		
	def getLogName(self):
		return 'Suite'
	
		
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		self._suite.become( parsed )
		return HandleEditResult.HANDLED







class PythonExpressionEditListener (PythonParsingEditListener):
	def getLogName(self):
		return 'Top level expression'
	
	
	def handleEmptyValue(self, element, fragment, event, model):
		model['expr'] = None
		return HandleEditResult.HANDLED
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		expr = model['expr']
		if parsed != expr:
			if expr is None:
				model['expr'] = parsed
			else:
				pyReplaceNode( expr, parsed )
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
				pyReplaceNodeIfNotEqual( expr, unparsed )
			return HandleEditResult.HANDLED
		else:
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
				pyForceNodeRefresh( model )
				event.revert()
				return True
			else:
				pyForceNodeRefresh( model )
				event.revert()
				return True
		elif isinstance( event, SelectionEditTreeEvent ):
			return True
		else:
			return False

PythonExpressionTopLevelEditListener.instance = PythonExpressionTopLevelEditListener()






