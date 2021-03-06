##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod

from LarchCore.Languages.Python2 import CodeGenerator
from LarchCore.Languages.Python2 import Schema




class PythonTextExporterError (Exception):
	pass


class PythonTextExporterCannotExportEmbeddedObjectsError (PythonTextExporterError):
	pass


class PythonTextExporterCannotExportQuoteError (PythonTextExporterError):
	pass


class PythonTextExporterCannotExportUnquoteError (PythonTextExporterError):
	pass


class _TextCodeGenerator (CodeGenerator.Python2CodeGenerator):
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


