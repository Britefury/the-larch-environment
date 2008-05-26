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
	
	

def _indent(s):
	return [ '  ' + x   for x in s ]


def _gLispSrcToStringPretty(x):
	if isinstance( x, RelativeNode ):
		x = x.node

	if x is None:
		return [ 'None' ]
	elif isinstance( x, str )  or  isinstance( x, unicode ):
		return [ x ]
	elif isGLispList( x ):
		if len( x ) > 0:
			bListContents = False
			for v in x[1:]:
				if isGLispList( v ):
					bListContents = True
			if isGLispList( x[0] ):
				# Starts with a list
				# All on separate lines
				result = [ '(' ]
				for v in x:
					result.extend( _indent( _gLispSrcToStringPretty( v ) ) )
				result.append( ')' )
				return result
			else:
				if bListContents:
					# 1st entry is a string, lists in remainder
					# 1st on main line
					# rest on own new lines
					first = _gLispSrcToStringPretty( x[0] )
					# @first only has 1 entry
					assert len( first ) == 1
					result = [ '(' + first[0] ]
					for v in x[1:]:
						result.extend( _indent( _gLispSrcToStringPretty( v ) ) )
					result.append( ')' )
					return result
				else:
					# All on 1 line
					return [ '(' + ' '.join( [ gLispSrcToStringPretty( v )  for v in x ] ) + ')' ]
		else:
			return [ '()' ]
	else:
		raise TypeError, '%s'  %  ( x.__class__, )


def gLispSrcToStringPretty(x):
	return '\n'.join( _gLispSrcToStringPretty( x ) )
