##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from java.awt import Color

from java.util.regex import Pattern

from copy import deepcopy

from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor
from BritefuryJ.Live import LiveValue

from BritefuryJ.Controls import *

from BritefuryJ.DocPresent.Interactor import *
from BritefuryJ.Graphics import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.ObjectPres import *

from BritefuryJ.Editor.Table.Generic import *

from BritefuryJ.StyleSheet import *

from LarchCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAtCaretAction, makeWrapSelectionInEmbeddedExpressionAction, chainActions
from LarchCore.Languages.Python25.Embedded import EmbeddedPython25Expr



class EmbeddedDisplay (object):
	def __init__(self):
		self._expr = EmbeddedPython25Expr()
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
		
		
	def __getstate__(self):
		return { 'expr' : self._expr }
	
	def __setstate__(self, state):
		self._expr = state['expr']
		self._code = None
		self._values = []
		self._incr = IncrementalValueMonitor()
		self.__change_history__ = None
	
	
	def __get_trackable_contents__(self):
		return [ self._expr ]
		
		
	def __py_compile_visit__(self, codeGen):
		self._code = codeGen.compileForEvaluation( self._expr.model )
	
	def __py_eval__(self, _globals, _locals, codeGen):
		value = eval( self._code, _globals, _locals )
		self._values.append( value )
		self._incr.onChanged()
		return value
	
	def __py_replacement__(self):
		return deepcopy( self._expr.model['expr'] )
		
	
	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				del self._values[:]
				self._incr.onChanged()
			
			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )
			
			return False
				
		
		
		self._incr.onAccess()
		#exprPres = pyPerspective.applyTo( self._expr )
		exprPres = self._expr
		
		valuesPres = ObjectBox( 'Values', Column( [ Paragraph( [ value ] )   for value in self._values ] ) )
		
		contents = Column( [ exprPres, valuesPres ] )
		return ObjectBox( 'Embedded display', contents ).withContextMenuInteractor( _embeddedDisplayMenu )



	
def _newEmbeddedDisplayAtCaret(caret):
	return EmbeddedDisplay()

def _newEmbeddedDisplayAtSelection(expr, selection):
	d = EmbeddedDisplay()
	d._expr.model['expr'] = deepcopy( expr )
	return d

_exprAtCaret = makeInsertEmbeddedExpressionAtCaretAction( _newEmbeddedDisplayAtCaret )
_exprAtSelection = makeWrapSelectionInEmbeddedExpressionAction( _newEmbeddedDisplayAtSelection )


_edCommand = Command( '&Embedded &Display', chainActions( _exprAtSelection, _exprAtCaret ) )

_edCommands = CommandSet( 'LarchTools.PythonTools.EmbeddedDisplay', [ _edCommand ] )

pythonCommands.registerCommandSet( _edCommands )
