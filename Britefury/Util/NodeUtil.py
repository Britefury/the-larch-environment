##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMListInterface, DMObjectInterface, DMNode



def isStringNode(x):
	return isinstance( x, str )  or  isinstance( x, unicode )

def isListNode(xs):
	return isinstance( xs, List )  or  isinstance( xs, list )  or isinstance( xs, DMListInterface )

def isObjectNode(xs):
	return isinstance( xs, DMObjectInterface )



def nodeToSXString(x, level=3):
	if x is None:
		return 'None'
	elif isStringNode( x ):
		return x
	elif isListNode( x ):
		if level == 0:
			return '[...]'
		else:
			return '[' + ' '.join( [ nodeToSXString( v, level - 1 )  for v in x ] ) + ']'
	elif isObjectNode( x ):
		if level == 0:
			return '(...)'
		else:
			return '( ' + x.getDMClass().getName() + ' : ' + ' '.join( [ x.getDMClass().getField( i ).getName() + '=' + nodeToSXString( x.get( i ), level - 1 )  for i in xrange( 0, x.getDMClass().getNumFields() ) ] ) + ')'
	else:
		raise TypeError, '%s'  %  ( x.__class__, )


