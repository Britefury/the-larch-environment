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
from LarchCore.Languages.Python25.Embedded import _py25NewModule
from LarchCore.Languages.Python25.Python25Importer import importPy25File
from LarchCore.Languages.Python25.PythonEditor.Subject import Python25Subject
from LarchCore.Languages.Python25.PythonEditor.View import perspective as python25EditorPerspective


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
			return x['expr'] == Schema.UNPARSED( value=[ '' ] )
		elif x.isInstanceOf(Schema.PythonTarget):
			return x['target'] == Schema.UNPARSED( value=[ '' ] )
	return False




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


