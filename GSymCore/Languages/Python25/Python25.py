##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymPageUnitFactory, GSymPageUnitImporter
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.Python25Importer import importPy25File
from GSymCore.Languages.Python25.PythonEditor.View import perspective as python25EditorPerspective
from GSymCore.Languages.Python25.PythonEditor.Subject import Python25Subject



def py25NewModule():
	return Schema.PythonModule( suite=[] )

def py25NewExpr():
	return Schema.PythonExpression( expr=None )

def _py25NewUnit():
	return gSymUnit( Schema.schema, py25NewModule() )

def _py25ImportFile(filename):
	content = importPy25File( filename )
	return gSymUnit( Schema.schema, content )



unitClass = GSymUnitClass( Schema.schema, Python25Subject )


newPageFactory = GSymPageUnitFactory( 'Python 2.5', _py25NewUnit )


pageImporter = GSymPageUnitImporter( 'Python 2.5', 'Python 2.5 source (*.py)', 'py', _py25ImportFile )

