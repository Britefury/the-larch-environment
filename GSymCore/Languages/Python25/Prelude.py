##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************


_prelude = ''


def registerPrelude(p):
	global _prelude
	
	_prelude += '\n' + p + '\n'
	
	
	
def getPrelude():
	return _prelude


def getCompiledPrelude(filename):
	return compile( _prelude, filename, 'exec' )


def executePrelude(module):
	code = getCompiledPrelude( module.__name__ )
	exec code in module.__dict__


