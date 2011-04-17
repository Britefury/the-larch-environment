##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMListInterface, DMObjectInterface, DMNode, DMEmbeddedObject, DMEmbeddedIsolatedObject



def isStringNode(x):
	return isinstance( x, str )  or  isinstance( x, unicode )

def isListNode(x):
	return isinstance(x, List )  or  isinstance( x, list )  or isinstance( x, DMListInterface )

def isObjectNode(x):
	return isinstance( x, DMObjectInterface )

def isEmbeddedObjectNode(x):
	return isinstance( x, DMEmbeddedObject ) or isinstance( x, DMEmbeddedIsolatedObject )



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
			return '( ' + x.getDMObjectClass().getName() + ' : ' + ' '.join( [ x.getDMObjectClass().getField( i ).getName() + '=' + nodeToSXString( x.get( i ), level - 1 )  for i in xrange( 0, x.getDMObjectClass().getNumFields() ) ] ) + ')'
	elif isEmbeddedObjectNode( x ):
		return '<<Embedded>>'
	else:
		raise TypeError, '%s'  %  ( x.__class__, )


