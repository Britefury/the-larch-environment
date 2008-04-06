##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocModel.DMListInterface import DMListInterface

from Britefury.gSym.RelativeNode import RelativeNode, RelativeList



def isGLispString(xs):
	return isinstance( xs, str )  or  isinstance( xs, unicode )

def isGLispList(xs):
	return isinstance( xs, tuple )  or  isinstance( xs, list )  or  isinstance( xs, DMListInterface )  or  isinstance( xs, RelativeList )


def isGLispComment(xs):
	return isGLispList( xs )  and  len( xs ) >= 1  and  xs[0] == '$#'


def stripGLispComments(xs):
	return [ x   for x in xs   if not isGLispComment( x ) ]



def gLispSrcToString(x, level=3):
	if isinstance( x, RelativeNode ):
		x = x.node

	if x is None:
		return 'None'
	elif isinstance( x, str )  or  isinstance( x, unicode ):
		return x
	elif isGLispList( x ):
		if level == 0:
			return '(...)'
		else:
			return '(' + ' '.join( [ gLispSrcToString( v, level - 1 )  for v in x ] ) + ')'
	else:
		raise TypeError, '%s'  %  ( x.__class__, )
