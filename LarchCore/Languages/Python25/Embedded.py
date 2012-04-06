##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

import java.util.List

from BritefuryJ.DocModel import DMNode

from LarchCore.Languages.Python25 import Schema
from LarchCore.Languages.Python25 import CodeGenerator
from LarchCore.Languages.Python25.Execution import Execution
from LarchCore.Languages.Python25 import PythonEditor
from LarchCore.Languages.Python25.PythonEditor.Parser import Python25Grammar


_grammar = Python25Grammar()




def _py25NewModule():
	return Schema.PythonModule( suite=[] )

def _py25NewSuite():
	return Schema.PythonSuite( suite=[] )

def _py25NewExpr():
	return Schema.PythonExpression( expr=Schema.UNPARSED( value=[ '' ] ) )

def _py25NewTarget():
	return Schema.PythonTarget( target=Schema.UNPARSED( value=[ '' ] ) )




class EmbeddedPython25 (object):
	class _WithPerspective (object):
		def __init__(self, embeddedPy, perspective):
			self._embeddedPy = embeddedPy
			self._perspective = perspective

		def __present__(self, fragment, inheritedState):
			return self._perspective( self._embeddedPy.model )


	def __init__(self, model):
		self.model = model
		self.__change_history__ = None
		self.model.realiseAsRoot()


	def __getstate__(self):
		return { 'model' : self.model }

	def __setstate__(self, state):
		self.model = state['model']
		self.model.realiseAsRoot()


	def __copy__(self):
		return EmbeddedPython25( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython25( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython25( deepcopy( self.model, memo ) )


	def __get_trackable_contents__(self):
		return [ self.model ]


	def __present__(self, fragment, inheritedState):
		return PythonEditor.View.perspective( self.model )


	def withPerspective(self, perspective):
		return self._WithPerspective( self, perspective )



class EmbeddedPython25Target (EmbeddedPython25):
	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonTarget ):
			pass
		elif model is None:
			model = _py25NewTarget()
		elif isinstance( model, DMNode ):
			model = Schema.PythonTarget( target=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython25Target from %s' % model

		super(EmbeddedPython25Target, self).__init__( model )


	def __copy__(self):
		return EmbeddedPython25Target( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython25Target( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython25Target( deepcopy( self.model, memo ) )


	@property
	def target(self):
		return self.model['target']


	@classmethod
	def __import_from_plain_text__(cls, importData):
		return cls.fromText( importData )


	@staticmethod
	def fromText(text):
		parseResult = _grammar.targetListOrTargetItem().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Target( Schema.PythonTarget( target=parseResult.getValue() ) )
		else:
			return EmbeddedPython25Target( Schema.PythonTarget( target=Schema.UNPARSED( value = [ text ] ) ) )




class EmbeddedPython25Expr (EmbeddedPython25):
	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonExpression ):
			pass
		elif model is None:
			model = _py25NewExpr()
		elif isinstance( model, DMNode ):
			model = Schema.PythonExpression( expr=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython25Expr from %s' % model

		super(EmbeddedPython25Expr, self).__init__( model )


	def __copy__(self):
		return EmbeddedPython25Expr( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython25Expr( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython25Expr( deepcopy( self.model, memo ) )


	@property
	def expression(self):
		return self.model['expr']


	def compileForEvaluation(self, filename):
		return CodeGenerator.compileForEvaluation( self.model, filename )


	def evaluate(self, globals, locals):
		return eval( self.compileForEvaluation( '<expr>' ), globals, locals )


	@classmethod
	def __import_from_plain_text__(cls, importData):
		return cls.fromText( importData )


	@staticmethod
	def fromText(text):
		parseResult = _grammar.tupleOrExpressionOrYieldExpression().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Expr( Schema.PythonExpression( expr=parseResult.getValue() ) )
		else:
			return EmbeddedPython25Expr( Schema.PythonExpression( expr=Schema.UNPARSED( value = [ text ] ) ) )




class EmbeddedPython25Executable (EmbeddedPython25):
	__python_code_type__ = '<larch_executable_code>'

	def compileForModuleExecution(self, module, filename=None):
		if filename is None:
			filename = module.__file__
		if filename is None:
			filename = self.__python_code_type__
		return CodeGenerator.compileForModuleExecution( module, self.model, filename )

	def compileForModuleExecutionAndEvaluation(self, module, filename=None):
		if filename is None:
			filename = module.__file__
		if filename is None:
			filename = self.__python_code_type__
		return CodeGenerator.compileForModuleExecutionAndEvaluation( module, self.model, filename )


	def getResultOfExecutionWithinModule(self, module):
		return Execution.getResultOfExecutionWithinModule( self.model, module, False )

	def getResultOfExecutionAndEvaluationWithinModule(self, module):
		return Execution.getResultOfExecutionWithinModule( self.model, module, True )


	def getResultOfExecutionInScopeWithinModule(self, module, globals, locals):
		return Execution.getResultOfExecutionWithinModule( self.model, globals, locals, module, False )

	def getResultOfExecutionAndEvaluationInScopeWithinModule(self, module, globals, locals):
		return Execution.getResultOfExecutionWithinModule( self.model, globals, locals, module, True )


	def executeWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, False )

	def executeAndEvaluateWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, True )

	def executeInScopeWithinModule(self, module, globals, locals):
		return Execution.executeInScopeWithinModule( self.model, globals, locals, module, False )

	def executeAndEvaluateInScopeWithinModule(self, module, globals, locals):
		return Execution.executeInScopeWithinModule( self.model, globals, locals, module, True )




class EmbeddedPython25Suite (EmbeddedPython25Executable):
	__python_code_type__ = '<larch_suite>'

	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonSuite ):
			pass
		elif model is None:
			model = _py25NewSuite()
		elif isinstance( model, list )  or  isinstance( model, java.util.List ):
			model = Schema.PythonSuite( suite=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython25Suite from %s' % model

		super(EmbeddedPython25Suite, self).__init__( model )


	def __copy__(self):
		return EmbeddedPython25Suite( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython25Suite( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython25Suite( deepcopy( self.model, memo ) )


	@property
	def statements(self):
		return self.model['suite']


	@classmethod
	def __import_from_plain_text__(cls, importData):
		return cls.fromText( importData )


	@staticmethod
	def fromText(text):
		if not text.endswith( '\n' ):
			text = text + '\n'
		parseResult = _grammar.suite().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Suite( [] )
		else:
			return EmbeddedPython25Suite( parseResult.getValue() )




class EmbeddedPython25Module (EmbeddedPython25Executable):
	__python_code_type__ = '<larch_module>'

	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonModule ):
			pass
		elif model is None:
			model = _py25NewModule()
		elif isinstance( model, list )  or  isinstance( model, java.util.List ):
			model = Schema.PythoModule( suite=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython25Module from %s' % model

		super(EmbeddedPython25Module, self).__init__( model )


	def __copy__(self):
		return EmbeddedPython25Module( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython25Module( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython25Module( deepcopy( self.model, memo ) )


	@property
	def statements(self):
		return self.model['suite']


	@classmethod
	def __import_from_plain_text__(cls, importData):
		return cls.fromText( importData )


	@staticmethod
	def fromText(text):
		if not text.endswith( '\n' ):
			text = text + '\n'
		parseResult = _grammar.suite().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Module( [] )
		else:
			return EmbeddedPython25Module( parseResult.getValue() )



def removeEmbeddedObjectContainingElement(element):
	return PythonEditor.PythonEditOperations.requestRemoveEmbeddedObjectContainingElement( element )
