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
from BritefuryJ.StyleSheet import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Interactor import KeyElementInteractor


from BritefuryJ.Logging import LogEntry

from BritefuryJ.Editor.Sequential import SequentialEditor, SelectionEditTreeEvent, EditListener
from BritefuryJ.Editor.Sequential.StreamEditListener import HandleEditResult
from BritefuryJ.Editor.SyntaxRecognizing import ParsingEditListener, PartialParsingEditListener, UnparsedEditListener, TopLevelEditListener



from Britefury.Util.NodeUtil import *


from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod



from GSymCore.Languages.Python25 import Schema

from GSymCore.Languages.Python25.PythonEditor.Parser import Python25Grammar
from GSymCore.Languages.Python25.PythonEditor.Precedence import *
from GSymCore.Languages.Python25.PythonEditor.PythonEditOperations import *
from GSymCore.Languages.Python25.PythonEditor.SREditor import PythonSyntaxRecognizingEditor, PythonIndentationTreeEvent




#
#
# EDIT LISTENERS
#
#

class PythonExpressionEditListener (ParsingEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance

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
			values = value.getItemValues()
			if values == []:
				values = [ '' ]
			model['expr'] = Schema.UNPARSED( value=values )
			return HandleEditResult.HANDLED
		else:
			return HandleEditResult.NOT_HANDLED







class PythonExpressionNewLineEvent (object):
	def __init__(self, model):
		self.model = model


class PythonExpressionTopLevelEditListener (TopLevelEditListener):
	def getSyntaxRecognizingEditor(self):
		return PythonSyntaxRecognizingEditor.instance
	
	def handleTopLevelEditEvent(self, element, sourceElement, event):
		if isinstance( event, TextEditEvent ):
			fragment = element.getFragmentContext()
			model = fragment.getModel()
			if isinstance( event, TextEditEventInsert )   and   '\n' in event.getTextInserted():
				element.postTreeEvent( PythonExpressionNewLineEvent( model ) )






class StatementIndentationInteractor (KeyElementInteractor):
	def __init__(self):
		pass
		
		
	def keyTyped(self, element, event):
		if event.getKeyChar() == '\t':
			fragment = element.getFragmentContext()
			node = fragment.getModel()
			
			editor = SequentialEditor.getEditorForElement( element )
			if event.getModifiers() & KeyEvent.SHIFT_MASK  !=  0:
				editor.dedent( element, fragment, node )
			else:
				editor.indent( element, fragment, node )
			
			return True
		else:
			return False
		
		
	def keyPressed(self, element, event):
		return False
	
	def keyReleased(self, element, event):
		return False


