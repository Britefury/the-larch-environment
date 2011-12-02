##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMNode

from LarchCore.Languages.Python25 import Schema
from LarchCore.Languages.Python25 import CodeGenerator
from LarchCore.Languages.Python25.Execution import Execution
from LarchCore.Languages.Python25.Python25Importer import importPy25File
from LarchCore.Languages.Python25.PythonEditor.View import perspective as python25EditorPerspective
from LarchCore.Languages.Python25.PythonEditor.Subject import Python25Subject
from LarchCore.Languages.Python25.PythonEditor.Parser import Python25Grammar


from LarchCore.Project.PageData import PageData, registerPageFactory, registerPageImporter



def _py25NewModule():
	return Schema.PythonModule( suite=[] )

def _py25NewSuite():
	return Schema.PythonSuite( suite=[] )

def _py25NewExpr():
	return Schema.PythonExpression( expr=Schema.UNPARSED( value=[ '' ] ) )

def _py25NewTarget():
	return Schema.PythonTarget( target=Schema.UNPARSED( value=[ '' ] ) )


def py25NewModuleAsRoot():
	module = _py25NewModule()
	module.realiseAsRoot()
	return module


def isEmptyTopLevel(x):
	if isinstance(x, DMNode):
		if x.isInstanceOf(Schema.PythonModule)  or  x.isInstanceOf(Schema.PythonSuite):
			return x['suite'] == []
		elif x.isInstanceOf(Schema.PythonExpression):
			return x['expr'] == Schema.UNPARSED( value=[ '' ] )
		elif x.isInstanceOf(Schema.PythonTarget):
			return x['target'] == Schema.UNPARSED( value=[ '' ] )
	return False






class EmbeddedPython25 (object):
	class _WithPerspective (object):
		def __init__(self, embeddedPy, perspective):
			self._embeddedPy = embeddedPy
			self._perspective = perspective

		def __present__(self, fragment, inheritedState):
			return self.perspective( self._embeddedPy.model )


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
	
	
	def __get_trackable_contents__(self):
		return [ self.model ]
	
	
	def __present__(self, fragment, inheritedState):
		return python25EditorPerspective( self.model )


	def withPerspective(self, perspective):
		return self._WithPerspective( self, perspective )


	@staticmethod
	def module():
		return EmbeddedPython25( _py25NewModule() )

	@staticmethod
	def suite():
		return EmbeddedPython25( _py25NewSuite() )

	@staticmethod
	def expression():
		return EmbeddedPython25( _py25NewExpr() )

	@staticmethod
	def expressionFromText(text):
		parseResult = _grammar.tupleOrExpressionOrYieldExpression().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25( Schema.PythonExpression( expr=parseResult.getValue() ) )
		else:
			return EmbeddedPython25( Schema.PythonExpression( expr=Schema.UNPARSED( value = [ text ] ) ) )

	@staticmethod
	def target():
		return EmbeddedPython25( _py25NewTarget() )
	
	@staticmethod
	def targetFromText(text):
		parseResult = _grammar.targetListOrTargetItem().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25( Schema.PythonTarget( target=parseResult.getValue() ) )
		else:
			return EmbeddedPython25( Schema.PythonTarget( target=Schema.UNPARSED( value = [ text ] ) ) )



class EmbeddedPython25Target (EmbeddedPython25):
	def __init__(self, model=None):
		super(EmbeddedPython25Target, self).__init__( _py25NewTarget()   if model is None   else model )


	@staticmethod
	def fromText(text):
		parseResult = _grammar.targetListOrTargetItem().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Target( Schema.PythonTarget( target=parseResult.getValue() ) )
		else:
			return EmbeddedPython25Target( Schema.PythonTarget( target=Schema.UNPARSED( value = [ text ] ) ) )




class EmbeddedPython25Expr (EmbeddedPython25):
	def __init__(self, model=None):
		super(EmbeddedPython25Expr, self).__init__( _py25NewExpr()   if model is None   else model )


	def compileForEvaluation(self, filename):
		return CodeGenerator.compileForEvaluation( self.model, filename )


	def evaluate(self, globals, locals):
		return eval( self.compileForEvaluation( '<expr>', globals, locals ) )


	@staticmethod
	def fromText(text):
		parseResult = _grammar.tupleOrExpressionOrYieldExpression().parseStringChars( text )
		if parseResult.isValid():
			return EmbeddedPython25Expr( Schema.PythonExpression( expr=parseResult.getValue() ) )
		else:
			return EmbeddedPython25Expr( Schema.PythonExpression( expr=Schema.UNPARSED( value = [ text ] ) ) )




class EmbeddedPython25Executable (EmbeddedPython25):
	__python_code_type__ = '<larch_executable_code>'

	def compileForExecution(self, filename):
		return CodeGenerator.compileForExecution( self.model, filename )

	def compileForExecutionAndEvaluation(self, filename):
		return CodeGenerator.compileForExecution( self.model, filename )

	def compileForModuleExecution(self, module, filename):
		return CodeGenerator.compileForExecution( module, self.model, filename )

	def compileForModuleExecutionAndEvaluation(self, module, filename):
		return CodeGenerator.compileForExecution( module, self.model, filename )


	def executeWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, False )

	def executeAndEvaluateWithinModule(self, module):
		return Execution.executeWithinModule( self.model, module, True )




class EmbeddedPython25Suite (EmbeddedPython25Executable):
	__python_code_type__ = '<larch_suite>'

	def __init__(self, model=None):
		super(EmbeddedPython25Suite, self).__init__( _py25NewSuite()   if model is None   else model )




class EmbeddedPython25Module (EmbeddedPython25Executable):
	__python_code_type__ = '<larch_module>'

	def __init__(self, model=None):
		super(EmbeddedPython25Module, self).__init__( _py25NewModule()   if model is None   else model )




_grammar = Python25Grammar()

class Python25PageData (PageData):
	def makeEmptyContents(self):
		return _py25NewModule()
	
	def __new_subject__(self, document, enclosingSubject, location, importName, title):
		return Python25Subject( document, self.contents, enclosingSubject, location, importName, title )


def _py25ImportPage(filename):
	content = importPy25File( filename )
	return Python25PageData( content )	
	

registerPageFactory( 'Python 2.5', Python25PageData, 'Python' )
registerPageImporter( 'Python 2.5', 'Python 2.5 source (*.py)', 'py', _py25ImportPage )


