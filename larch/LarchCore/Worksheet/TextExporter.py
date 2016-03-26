##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Languages.Python2.TextExporter import PythonTextExporter
from LarchCore.Worksheet import Schema



class WorksheetTextExporterError (Exception):
	pass

class WorksheetTextExporterCannotExportEmbeddedObjectError (Exception):
	pass




class WorksheetTextExporter (object):
	__dispatch_num_args__ = 0


	def __init__(self, filename):
		self._pythonExporter = PythonTextExporter( filename )


	# Callable - use document model node method dispatch mechanism
	def __call__(self, x):
		return methodDispatch( self, x )


	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def Worksheet(self, node):
		return self( node['body'] )


	@DMObjectNodeDispatchMethod( Schema.Body )
	def Body(self, node):
		return ''.join( [ self( x ) + '\n'   for x in node['contents'] ] )


	def _convertTextItem(self, x):
		if isinstance( x, str )  or  isinstance( x, unicode ):
			return x
		else:
			return self( x )


	def _convertText(self, text):
		return ''.join( [ self._convertTextItem( x )   for x in text ] )


	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def Paragraph(self, node):
		return '#' + self._convertText( node['text'] )


	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def TextSpan(self, node):
		return self._convertText( node['text'] )


	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def PythonCode(self, node):
		return self._pythonExporter( node['code'] )


	@DMObjectNodeDispatchMethod( Schema.InlineEmbeddedObject )
	def InlineEmbeddedObject(self, node):
		raise WorksheetTextExporterCannotExportEmbeddedObjectError


	@DMObjectNodeDispatchMethod( Schema.ParagraphEmbeddedObject )
	def ParagraphEmbeddedObject(self, node):
		raise WorksheetTextExporterCannotExportEmbeddedObjectError





