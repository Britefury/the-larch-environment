##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import string
import pyparsing

from Britefury.DocModel.DMInterpreter import DMInterpreterEnv

from Britefury.DocView.DocViewTokeniser import DocViewTokenDefinition, DocViewTokeniser




class GSymString (object):
	def whitespace(self):
		return string.whitespace
	

	
class GSymParser (object):
	def quotedString(self):
		return pyparsing.quotedString
	
	def Word(self):
		return pyparsing.Word
	
	def Literal(self):
		return pyparsing.Literal



class GSymMetaLanguage (object):
	def __init__(self):
		self._string = GSymString()
		self._parser = GSymParser()

		
	def TokenDefinition(self):
		return DocViewTokenDefinition
	
	def string(self):
		return self._string

	def parser(self):
		return self._parser
	
	


def gSymGLispEnvironment():
	ml = GSymMetaLanguage()
	
	return DMInterpreterEnv( ml=ml )



glisp = gSymGLispEnvironment()