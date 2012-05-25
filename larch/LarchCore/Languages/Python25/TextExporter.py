##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMObject, DMList, DMEmbeddedObject, DMEmbeddedIsolatedObject

from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Languages.Python25 import CodeGenerator
from LarchCore.Languages.Python25 import Schema




class PythonTextExporterError (Exception):
	pass


class PythonTextExporterCannotExportEmbeddedObjectsError (PythonTextExporterError):
	pass


class PythonTextExporterCannotExportQuoteError (PythonTextExporterError):
	pass


class PythonTextExporterCannotExportUnquoteError (PythonTextExporterError):
	pass


class _TextCodeGenerator (CodeGenerator.Python25CodeGenerator):
	def __init__(self, filename):
		super( _TextCodeGenerator, self ).__init__( filename )


	# Quote and Unquote
	@DMObjectNodeDispatchMethod( Schema.Quote )
	def Quote(self, node, value):
		raise PythonTextExporterQuoteNotSupportedError, 'PythonTextExporter does not support quote expressions'

	@DMObjectNodeDispatchMethod( Schema.Unquote )
	def Unquote(self, node, value):
		raise PythonTextExporterUnquoteNotSupportedError, 'PythonTextExporter does not support unquote expressions'




	# Embedded object
	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectLiteral )
	def EmbeddedObjectLiteral(self, node, embeddedValue):
		raise PythonTextExporterEmbeddedObjectsNotSupportedError, 'PythonTextExporter does not support embedded object literals'


	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectExpr )
	def EmbeddedObjectExpr(self, node, embeddedValue):
		raise PythonTextExporterEmbeddedObjectsNotSupportedError, 'PythonTextExporter does not support embedded object expressions'


	@DMObjectNodeDispatchMethod( Schema.EmbeddedObjectStmt )
	def EmbeddedObjectStmt (self, node, embeddedValue):
		raise PythonTextExporterEmbeddedObjectsNotSupportedError, 'PythonTextExporter does not support embedded object statements'




class PythonTextExporter (object):
	def __init__(self, filename):
		self._codeGen = _TextCodeGenerator( filename )


	def __call__(self, node):
		return str( self._codeGen( node ) )


