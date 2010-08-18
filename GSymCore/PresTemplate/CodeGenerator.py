##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymCodeGenerator import GSymCodeGeneratorObjectNodeDispatch
from Britefury.Dispatch.DMObjectNodeMethodDispatch import DMObjectNodeDispatchMethod

from GSymCore.PresTemplate import Schema
from GSymCore.PresTemplate.PresTemplate import presTemplateRuntimeModuleName


class PresTemplateCodeGenerator (GSymCodeGeneratorObjectNodeDispatch):
	__dispatch_num_args__ = 0
	
	
	def __init__(self, pythonCodeGen):
		super( PresTemplateCodeGenerator, self ).__init__()
		self._pythonCodeGen = pythonCodeGen
		
		
	def _runtimePresComName(self, name):
		return presTemplateRuntimeModuleName + '.' + name
	
	
	@DMObjectNodeDispatchMethod( Schema.Template )
	def Template(self, node, body):
		return self( body )
	
	
	@DMObjectNodeDispatchMethod( Schema.Body )
	def Body(self, node, contents):
		return self._runtimePresComName( 'Body' ) + '( [ ' + ', '.join( [ self( x )   for x in contents ] ) + ' ] )'
	
	
	def _paragraph(self, combinatorName, text):
		return self._runtimePresComName( combinatorName ) + '( ' + repr( text ) + ' )'
		
	@DMObjectNodeDispatchMethod( Schema.Paragraph )
	def Paragraph(self, node, text, style):
		if style == 'normal':
			return self._paragraph( 'NormalText', text )
		elif style == 'h1':
			return self._paragraph( 'Heading1', text )
		elif style == 'h2':
			return self._paragraph( 'Heading2', text )
		elif style == 'h3':
			return self._paragraph( 'Heading3', text )
		elif style == 'h4':
			return self._paragraph( 'Heading4', text )
		elif style == 'h5':
			return self._paragraph( 'Heading5', text )
		elif style == 'h6':
			return self._paragraph( 'Heading6', text )
		elif style == 'title':
			return self._paragraph( 'TitleBar', text )
		else:
			raise ValueError, 'bad style'
	
	
	@DMObjectNodeDispatchMethod( Schema.PythonExpr )
	def PythonExpr(self, node, code):
		return self._runtimePresComName( 'Paragraph' ) + '( [ ' + self._pythonCodeGen( code ) + ' ] )'
	
	
