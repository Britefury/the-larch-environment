##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.Python25Importer import importPy25File
from GSymCore.Languages.Python25.PythonEditor.View import perspective as python25EditorPerspective
from GSymCore.Languages.Python25.PythonEditor.Subject import Python25Subject

from GSymCore.Project.PageData import PageData, registerPageFactory, registerPageImporter



def py25NewModule():
	return Schema.PythonModule( suite=[] )

def py25NewSuite():
	return Schema.PythonSuite( suite=[] )

def py25NewExpr():
	return Schema.PythonExpression( expr=Schema.UNPARSED( value=[ '' ] ) )

def py25NewTarget():
	return Schema.PythonTarget( target=Schema.UNPARSED( value=[ '' ] ) )



class EmbeddedPython25 (object):
	def __init__(self, model):
		self._model = model
	
	
	def __getstate__(self):
		return { 'model' : self._model }
	
	def __setstate__(self, state):
		self._model = state['model']
	
	
	def __present__(self, fragment, inheritedState):
		return python25EditorPerspective( self._model )


	@staticmethod
	def module():
		return EmbeddedPython25( py25NewModule() )

	@staticmethod
	def suite():
		return EmbeddedPython25( py25NewSuite() )

	@staticmethod
	def expression():
		return EmbeddedPython25( py25NewExpr() )

	@staticmethod
	def target():
		return EmbeddedPython25( py25NewTarget() )



class Python25PageData (PageData):
	def makeEmptyContents(self):
		return py25NewModule()
	
	def __new_subject__(self, document, enclosingSubject, location, title):
		return Python25Subject( document, self.contents, enclosingSubject, location, title )
	
def _py25ImportPage(filename):
	content = importPy25File( filename )
	return Python25PageData( content )	
	

registerPageFactory( 'Python 2.5', Python25PageData, 'Python' )
registerPageImporter( 'Python 2.5', 'Python 2.5 source (*.py)', 'py', _py25ImportPage )


