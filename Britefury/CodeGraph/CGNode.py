##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *
from Britefury.PyCodeGen.PyCodeGen import *


class CGNode (SemanticGraphNode):
	def generateTexParameters(self):
		return PyCodeBlock()

	def generateTexLeafParamsString(self):
		return ''

	def generateTexBody(self):
		return PyCodeBlock()
		#return ''


	texBegin = '\\gSymBegin%s'
	texEnd = '\\gSymEnd%s'
	texCommand = '\\gSym%s {%s}'
	bTexLeaf = False

	def generateTex(self):
		if self.bTexLeaf:
			texBlock = PyCodeBlock()
			texBlock.append( self.texCommand  %  ( self.__class__.__name__[2:], self.generateTexLeafParamsString() ) )
			return texBlock
		else:
			texBlock = PyCodeBlock()
			texBlock.append( self.texBegin  %  ( self.__class__.__name__[2:] ) )
			paramsBlock = self.generateTexParameters()
			paramsBlock.indent()
			texBlock += paramsBlock
			bodyBlock = self.generateTexBody()
			bodyBlock.indent()
			texBlock += bodyBlock
			texBlock.append( self.texEnd  %  ( self.__class__.__name__[2:], ) )
			return texBlock
