##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import java.util.List

from BritefuryJ.DocModel import DMNode

from LarchCore.Languages.Python2 import Schema
from LarchCore.Languages.Python2.Embedded import _py25NewModule
from LarchCore.Languages.Python2.Python2Importer import importPy2File
from LarchCore.Languages.Python2.PythonEditor.Subject import Python2Subject
from LarchCore.Languages.Python2.PythonEditor.View import perspective as python2EditorPerspective
from LarchCore.Languages.Python2.TextExporter import PythonTextExporter
from LarchCore.Languages.Python2.CodeGenerator import Python2CodeGenerator


from LarchCore.Project.PageData import PageData, registerPageFactory, registerPageImporter



def py25NewModuleAsRoot():
	module = _py25NewModule()
	module.realiseAsRoot()
	return module


def isEmptyTopLevel(x):
	if isinstance(x, DMNode):
		if x.isInstanceOf(Schema.PythonModule)  or  x.isInstanceOf(Schema.PythonSuite):
			return x['suite'] == []
		elif x.isInstanceOf(Schema.PythonExpression):
			expr = x['expr']
			return expr is None  or  expr == Schema.UNPARSED( value=[ '' ] )
		elif x.isInstanceOf(Schema.PythonTarget):
			target = x['target']
			return target == Schema.UNPARSED( value=[ '' ] )
	return False




class Python2PageData (PageData):
	def makeEmptyContents(self):
		return _py25NewModule()

	def get_source_code(self, filename):
		code_gen = Python2CodeGenerator(filename)
		return code_gen.compileForExecution( self.contents )

	def exportAsString(self, filename):
		exporter = PythonTextExporter( filename )
		return exporter( self.contents )

	def __new_subject__(self, document, enclosingSubject, path, importName, title):
		return Python2Subject( document, self.contents, enclosingSubject, path, importName, title )


def _py25ImportPage(filename):
	content = importPy2File( filename )
	return Python2PageData( content )
	

registerPageFactory( 'Python 2', Python2PageData, 'Python' )
registerPageImporter( 'Python 2', 'Python 2 source (*.py)', 'py', _py25ImportPage )


