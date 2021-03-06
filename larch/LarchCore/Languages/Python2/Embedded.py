##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from copy import deepcopy

import java.util.List

from BritefuryJ.DocModel import DMNode

from LarchCore.Languages.Python2 import Schema
from LarchCore.Languages.Python2 import CodeGenerator
from LarchCore.Languages.Python2.Execution import Execution
from LarchCore.Languages.Python2 import PythonEditor
from LarchCore.Languages.Python2.PythonEditor.Parser import Python2Grammar


_grammar = Python2Grammar()




def _py25NewModule():
	return Schema.PythonModule( suite=[] )

def _py25NewSuite():
	return Schema.PythonSuite( suite=[] )

def _py25NewExpr():
	return Schema.PythonExpression( expr=None )

def _py25NewTarget():
	return Schema.PythonTarget( target=Schema.UNPARSED( value=[ '' ] ) )




class EmbeddedPython2 (object):
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
		self.__change_history__ = None
		self.model = state['model']
		self.model.realiseAsRoot()


	def __copy__(self):
		return EmbeddedPython2( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython2( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython2( memo.copy( self.model ) )


	def __get_trackable_contents__(self):
		return [ self.model ]


	def __present__(self, fragment, inheritedState):
		return PythonEditor.View.perspective( self.model )


	def withPerspective(self, perspective):
		return self._WithPerspective( self, perspective )



class EmbeddedPython2Target (EmbeddedPython2):
	def __init__(self, model=None):
		if model is None:
			model = _py25NewTarget()
		elif isinstance( model, DMNode ):
			if model.isInstanceOf( Schema.PythonTarget ):
				pass
			else:
				model = Schema.PythonTarget( target=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython2Target from %s' % model

		super(EmbeddedPython2Target, self).__init__( model )


	def __eq__(self, other):
		if isinstance(other, EmbeddedPython2Target):
			return self.model == other.model
		else:
			return False

	def __ne__(self, other):
		if isinstance(other, EmbeddedPython2Target):
			return self.model != other.model
		else:
			return True


	def __copy__(self):
		return EmbeddedPython2Target( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython2Target( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython2Target( memo.copy( self.model ) )


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
			return EmbeddedPython2Target( Schema.PythonTarget( target=parseResult.getValue() ) )
		else:
			return EmbeddedPython2Target( Schema.PythonTarget( target=Schema.UNPARSED( value=[ text ] ) ) )




class EmbeddedPython2Expr (EmbeddedPython2):
	def __init__(self, model=None):
		if model is None:
			model = _py25NewExpr()
		elif isinstance( model, DMNode ):
			if model.isInstanceOf( Schema.PythonExpression ):
				pass
			else:
				model = Schema.PythonExpression( expr=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython2Expr from %s' % model

		super(EmbeddedPython2Expr, self).__init__( model )


	def __eq__(self, other):
		if isinstance(other, EmbeddedPython2Expr):
			return self.model == other.model
		else:
			return False

	def __ne__(self, other):
		if isinstance(other, EmbeddedPython2Expr):
			return self.model != other.model
		else:
			return True


	def __copy__(self):
		return EmbeddedPython2Expr( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython2Expr( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython2Expr( memo.copy( self.model ) )


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
			return EmbeddedPython2Expr( Schema.PythonExpression( expr=parseResult.getValue() ) )
		else:
			return EmbeddedPython2Expr( Schema.PythonExpression( expr=Schema.UNPARSED( value=[ text ] ) ) )




class EmbeddedPython2Executable (EmbeddedPython2):
	__python_code_type__ = '<larch_executable_code>'


	@staticmethod
	def _exprModelAsStmts(exprModel):
		return [ Schema.ExprStmt( expr=exprModel ) ]


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
		return Execution.getResultOfExecutionInScopeWithinModule( self.model, globals, locals, module, False )

	def getResultOfExecutionAndEvaluationInScopeWithinModule(self, module, globals, locals):
		return Execution.getResultOfExecutionInScopeWithinModule( self.model, globals, locals, module, True )


	def executeWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, False )

	def executeAndEvaluateWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, True )

	def executeInScopeWithinModule(self, module, globals, locals):
		return Execution.executeInScopeWithinModule( self.model, globals, locals, module, False )

	def executeAndEvaluateInScopeWithinModule(self, module, globals, locals):
		return Execution.executeInScopeWithinModule( self.model, globals, locals, module, True )




class EmbeddedPython2Suite (EmbeddedPython2Executable):
	__python_code_type__ = '<larch_suite>'

	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonSuite ):
			pass
		elif model is None:
			model = _py25NewSuite()
		elif isinstance( model, list )  or  isinstance( model, java.util.List ):
			model = Schema.PythonSuite( suite=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython2Suite from %s' % model

		super(EmbeddedPython2Suite, self).__init__( model )


	def __eq__(self, other):
		if isinstance(other, EmbeddedPython2Suite):
			return self.model == other.model
		else:
			return False

	def __ne__(self, other):
		if isinstance(other, EmbeddedPython2Suite):
			return self.model != other.model
		else:
			return True


	def __copy__(self):
		return EmbeddedPython2Suite( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython2Suite( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython2Suite( memo.copy( self.model ) )


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
			return EmbeddedPython2Suite( parseResult.getValue() )
		else:
			return EmbeddedPython2Suite( [] )

	@staticmethod
	def fromExprModel(exprModel):
		return EmbeddedPython2Suite( Schema.PythonSuite( suite=EmbeddedPython2Executable._exprModelAsStmts( deepcopy( exprModel ) ) ) )

	@staticmethod
	def fromEmbeddedExpr(embeddedExpr):
		return EmbeddedPython2Suite.fromExprModel( embeddedExpr.model['expr'] )




class EmbeddedPython2Module (EmbeddedPython2Executable):
	__python_code_type__ = '<larch_module>'

	def __init__(self, model=None):
		if isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonModule ):
			pass
		elif model is None:
			model = _py25NewModule()
		elif isinstance( model, list )  or  isinstance( model, java.util.List ):
			model = Schema.PythonModule( suite=model )
		else:
			raise TypeError, 'Cannot construct EmbeddedPython2Module from %s' % model

		super(EmbeddedPython2Module, self).__init__( model )


	def __eq__(self, other):
		if isinstance(other, EmbeddedPython2Module):
			return self.model == other.model
		else:
			return False

	def __ne__(self, other):
		if isinstance(other, EmbeddedPython2Module):
			return self.model != other.model
		else:
			return True


	def __copy__(self):
		return EmbeddedPython2Module( self.model )

	def __deepcopy__(self, memo):
		return EmbeddedPython2Module( deepcopy( self.model, memo ) )

	def __clipboard_copy__(self, memo):
		return EmbeddedPython2Module( memo.copy( self.model ) )


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
			return EmbeddedPython2Module( parseResult.getValue() )
		else:
			return EmbeddedPython2Module( [] )


	@staticmethod
	def fromExprModel(exprModel):
		return EmbeddedPython2Suite( Schema.PythonModule( suite=EmbeddedPython2Executable._exprModelAsStmts( deepcopy( exprModel ) ) ) )

	@staticmethod
	def fromEmbeddedExpr(embeddedExpr):
		return EmbeddedPython2Module.fromExprModel( embeddedExpr.model['expr'] )



def removeEmbeddedObjectContainingElement(element):
	return PythonEditor.PythonEditOperations.requestRemoveEmbeddedObjectContainingElement( element )
