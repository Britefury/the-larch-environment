##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from GSymCore.Languages.Python25.Python25Importer import importPy25File


def py25New():
	return [ '$withLanguageModule', 'GSymCore.Languages.Python25.Python25', [ 'python25Module', [ 'commentStmt', 'New Python 2.5 document' ] ] ]

def py25ImportFile(filename):
	content = importPy25File( filename )
	return [ '$withLanguageModule', 'GSymCore.Languages.Python25.Python25', content ]


def initPlugin(pluginInterface):
	pluginInterface.registerNewDocumentFactory( 'Python 2.5', py25New )
	pluginInterface.registerImporter( 'Python 2.5', 'Python 2.5 source (*.py)', '*.py', py25ImportFile )
	