##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass, GSymPageFactory, GSymPageImporter
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.Languages.Python25.CodeGenerator import Python25CodeGenerator
from GSymCore.Languages.Python25 import NodeClasses as Nodes
from GSymCore.Languages.Python25.Python25Importer import importPy25File
from GSymCore.Languages.Python25.PythonEditor.View import Python25EditorPerspective



def _py25New():
	return gSymUnit( Nodes.schema, Nodes.PythonModule( suite=[ Nodes.BlankLine() ] ) )

def _py25ImportFile(filename):
	content = importPy25File( filename )
	return gSymUnit( Nodes.schema, content )



python25EditorPerspective = Python25EditorPerspective()
unitClass = GSymUnitClass( Nodes.schema, python25EditorPerspective )
unitClass.registerCodeGeneratorFactory( 'ascii', Python25CodeGenerator )


newPageFactory = GSymPageFactory( 'Python 2.5', _py25New )


pageImporter = GSymPageImporter( 'Python 2.5', 'Python 2.5 source (*.py)', 'py', _py25ImportFile )

