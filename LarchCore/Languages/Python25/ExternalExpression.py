##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************


_externalExpressionFactories = []


def registerExternalExpressionFactory(labelText, factory):
	_externalExpressionFactories.append( ( labelText, factory ) )
	
def getExternalExpressionFactories():
	return _externalExpressionFactories

	
	
	
_externalExpressionPresenterAndTitles = {}

def registerExternalExpressionPresenterAndTitle(schema, presenter, title):
	_externalExpressionPresenterAndTitles[schema] = ( presenter, title )

def getExternalExpressionPresenterAndTitle(schema):
	return _externalExpressionPresenterAndTitles[schema]
	
	
	

_externalExpressionCodeGeneratorFactories = {}

def registerExternalExpressionCodeGeneratorFactory(schema, codeGenFac):
	_externalExpressionCodeGeneratorFactories[schema]  = codeGenFac

def getExternalExpressionCodeGeneratorFactory(schema):
	return _externalExpressionCodeGeneratorFactories[schema]


