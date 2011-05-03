##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Command import *

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.ObjectPres import *

from GSymCore.Languages.Python25.PythonCommands import pythonCommands, makeInsertEmbeddedExpressionAction
from GSymCore.Languages.Python25.Python25 import py25NewExpr
from GSymCore.Languages.Python25.PythonEditor.View import perspective as pyPerspective



class EmbeddedDisplay (object):
	def __init__(self):
		self._expr = py25NewExpr()
		self._values = []
		self._incr = IncrementalValueMonitor()
		
		
	def __getstate__(self):
		return { 'expr' : self._expr }
	
	def __setstate__(self, state):
		self._expr = state['expr']
		self._values = []
		self._incr = IncrementalValueMonitor()
		
		
	def __py_value__(self, _globals, _locals, codeGen):
		code = codeGen.compileForEvaluation( self._expr )
		value = eval( code, _globals, _locals )
		self._values.append( value )
		self._incr.onChanged()
		
	
	def __present__(self, fragment, inheritedState):
		def _embeddedDisplayMenu(element, menu):
			def _onClear(item):
				del self._values[:]
				self._incr.onChanged()
			
			menu.add( MenuItem.menuItemWithLabel( 'Clear collected values', _onClear ) )
			
			return False
				
		
		
		self._incr.onAccess()
		exprPres = pyPerspective.applyTo( self._expr )
		
		valuesPres = ObjectBox( 'Values', Column( [ value   for value in self._values ] ) )
		
		contents = Column( [ exprPres, valuesPres ] )
		return ObjectBox( 'Embedded display', contents ).withContextMenuInteractor( _embeddedDisplayMenu )




def _newEmbeddedDisplay():
	return EmbeddedDisplay()


_edCommand = Command( '&Embedded &Display', makeInsertEmbeddedExpressionAction( _newEmbeddedDisplay ) )

_edCommands = CommandSet( 'GSymTools.PythonTools.EmbeddedDisplay', [ _edCommand ] )

pythonCommands.registerCommandSet( _edCommands )
