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




class GLispTokeniser (object):
	def __init__(self):
		pass
	
	
	# Define a token
	def defineToken(self, name, parser):
		return DocViewTokenDefinition( name, parser )
	
	
	# Literal and word
	def literalSubtoken(self, matchString):
		return pyparsing.Literal( matchString)
	
	def wordSubtoken(self, initChars):
		return pyparsing.Word( initChars )
	
	
	# Combine
	def combineOr(self, x, y):
		return x | y
	
	
	# Constants
	def quotedString(self):
		return pyparsing.quotedString
	
	def whitespace(self):
		return string.whitespace
	




def gSymGLispEnvironment():
	tokeniser = GLispTokeniser()
	
	return DMInterpreterEnv( tokeniser=tokeniser )



glisp = gSymGLispEnvironment()