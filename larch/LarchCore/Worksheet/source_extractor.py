##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2014.
##-*************************
from Britefury.Dispatch.MethodDispatch import DMObjectNodeDispatchMethod, methodDispatch

from LarchCore.Languages.Python2.TextExporter import PythonTextExporter
from LarchCore.Worksheet import Schema



class WorksheetSourceExtractor (object):
	__dispatch_num_args__ = 1


	def __init__(self):
		pass

	# Callable - use document model node method dispatch mechanism
	def __call__(self, x, source):
		return methodDispatch( self, x, source )


	@DMObjectNodeDispatchMethod( Schema.Worksheet )
	def Worksheet(self, source, node):
		return self(node['body'], source)


	@DMObjectNodeDispatchMethod( Schema.Body )
	def Body(self, source, node):
		for x in node['contents']:
			self(x, source)


	def _convertText(self, source, text):
		for x in text:
			if isinstance( x, str )  or  isinstance( x, unicode ):
				pass
			else:
				self(x, source)


	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def Paragraph(self, source, node):
		pass


	@DMObjectNodeDispatchMethod( Schema.TextSpan )
	def TextSpan(self, source, node):
		return self._convertText(source, node['text'])


	@DMObjectNodeDispatchMethod( Schema.PythonCode )
	def PythonCode(self, source, node):
		source.append(node['code'])


	@DMObjectNodeDispatchMethod( Schema.InlineEmbeddedObject )
	def InlineEmbeddedObject(self, source, node):
		pass


	@DMObjectNodeDispatchMethod( Schema.ParagraphEmbeddedObject )
	def ParagraphEmbeddedObject(self, source, node):
		pass





