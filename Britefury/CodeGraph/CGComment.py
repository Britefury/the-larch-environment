##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.vcls_string import pyStrToVString
from Britefury.CodeGraph.CGStatement import CGStatement
from Britefury.Sheet.Sheet import *
from Britefury.SemanticGraph.SemanticGraph import *

from Britefury.PyCodeGen.PyCodeGen import *



class CGComment (CGStatement):
	text = Field( str, '' )


	def generatePyCodeBlock(self):
		codeBlock = PyCodeBlock()
		for line in self.text.split( '\n' ):
			codeBlock.append( '# ' + line )
		return codeBlock

