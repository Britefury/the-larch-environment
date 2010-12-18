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

from BritefuryJ.Editor.Sequential import SequentialEditor, SelectionEditTreeEvent, EditListener
from BritefuryJ.Editor.Sequential.StreamEditListener import HandleEditResult
from BritefuryJ.Editor.SyntaxRecognizing import ParsingEditListener, PartialParsingEditListener, UnparsedEditListener, TopLevelEditListener



from Britefury.Util.NodeUtil import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod



from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SREditor import PythonSyntaxRecognizingEditor, PythonIndentationTreeEvent



class PythonParsingEditListener (ParsingEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance

	
class PythonPartialParsingEditListener (PartialParsingEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance



class PythonUnparsedEditListener (UnparsedEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance


class PythonTopLevelEditListener (TopLevelEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance
	


#
#
# EDIT LISTENERS
#
#

class ParsedExpressionEditListener (PythonParsingEditListener):
	def getLogName(self):
		return 'Expression'
	
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		if parsed != model:
			pyReplaceNode( model, parsed )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NO_CHANGE





class StatementEditListener (PythonParsingEditListener):
	def getLogName(self):
		return 'Statement'
	
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		if parsed != model:
			pyReplaceNode( model, parsed )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NO_CHANGE



class CompoundStatementHeaderEditListener (PythonPartialParsingEditListener):
	def getLogName(self):
		return 'Compound statement header'


class StatementUnparsedEditListener (PythonUnparsedEditListener):
	def getLogName(self):
		return 'Statement'
	
	def isValueValid(self, element, sourceElement, fragment, event, model, value):
		i = value.indexOf( '\n' )
		return i != -1   and   i == len( value ) - 1
	
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
		modifySuiteMinimisingChanges( self._suite, parsed )
		return HandleEditResult.HANDLED







class PythonExpressionEditListener (PythonParsingEditListener):
	def getLogName(self):
		return 'Top level expression'
	
	
	def handleEmptyValue(self, element, fragment, event, model):
		model['expr'] = Schema.UNPARSED( value=[ '' ] )
		return HandleEditResult.HANDLED
	
	def handleParseSuccess(self, element, sourceElement, fragment, event, model, value, parsed):
		expr = model['expr']
		if parsed != expr:
			model['expr'] = parsed
		return HandleEditResult.HANDLED
		
	def handleParseFailure(self, element, sourceElement, fragment, event, model, value):
		if '\n' not in value:
			model['expr'] = Schema.UNPARSED( value=value.getItemValues() )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NOT_HANDLED







class PythonModuleTopLevelEditListener (PythonTopLevelEditListener):
	pass

PythonModuleTopLevelEditListener.instance = PythonModuleTopLevelEditListener()




class PythonSuiteTopLevelEditListener (PythonTopLevelEditListener):
	pass

PythonSuiteTopLevelEditListener.instance = PythonSuiteTopLevelEditListener()




class PythonExpressionNewLineEvent (object):
	def __init__(self, model):
		self.model = model


class PythonExpressionTopLevelEditListener (PythonTopLevelEditListener):
	def handleEditEvent(self, element, sourceElement, event):
		if isinstance( event, TextEditEvent ):
			value = element.getStreamValue()
			fragment = element.getFragmentContext()
			model = fragment.getModel()
			if '\n' in value:
				element.postTreeEvent( PythonExpressionNewLineEvent( model ) )
				
		return super( PythonExpressionTopLevelEditListener, self ).handleEditEvent( element, sourceElement, event )

PythonExpressionTopLevelEditListener.instance = PythonExpressionTopLevelEditListener()






class StatementIndentationInteractor (KeyElementInteractor):
	def __init__(self):
		pass
		
		
	def keyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			context = element.getFragmentContext()
			node = context.getModel()
			
			editor = SequentialEditor.getEditorForElement( element )
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
	
	
	
	
