##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.lang import String
from java.util import List

from BritefuryJ.DocModel import DMListInterface

from BritefuryJ.DocTree import DocTreeNode
from BritefuryJ.DocTree import DocTreeString
from BritefuryJ.DocTree import DocTreeList


def isStringNode(x):
	return isinstance( x, str )  or  isinstance( x, unicode )  or  isinstance( x, String )  or  isinstance( x, DocTreeString )

def isListNode(xs):
	return isinstance( xs, List )  or  isinstance( xs, list )  or isinstance( xs, DMListInterface )  or  isinstance( xs, DocTreeList )

def isNullNode(x):
	return isStringNode( x )  and  x.toString() == '<nil>'

def makeNullNode():
	return '<nil>'



def nodeToSXString(x, level=3):
	if isinstance( x, DocTreeNode ):
		x = x.getNode()

	if x is None:
		return 'None'
	elif isStringNode( x ):
		return x
	elif isListNode( x ):
		if level == 0:
			return '(...)'
		else:
			return '(' + ' '.join( [ nodeToSXString( v, level - 1 )  for v in x ] ) + ')'
	else:
		raise TypeError, '%s'  %  ( x.__class__, )


