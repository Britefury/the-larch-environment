##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.util import List

from BritefuryJ.DocModel import DMList, DMObject, DMObjectInterface

from BritefuryJ.DocTree import DocTreeNode, DocTreeList, DocTreeObject


from Britefury.Util.NodeUtil import *


from Britefury.gSym.View import EditOperations



from GSymCore.Languages.Python25 import NodeClasses as Nodes



def _isStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.Stmt )

def _isCompoundStmt(node):
	return isinstance( node, DMObjectInterface )  and  node.isInstanceOf( Nodes.CompoundStmt )




def pyReplaceExpression(ctx, data, replacement):
	return EditOperations.replaceNodeContents( ctx, data, replacement )


def pyReplaceStatement(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		## HACK
		## TODO
		## use Java equals() method for now due to a bug in Jython; should be fixed once the patch for issue 1338 (http://bugs.jython.org) is integrated
		if data.equals( replacement ):
			# Same data; ignore
			return data
		else:
			if _isCompoundStmt( data ):
				originalSuite = data['suite']
				if _isCompoundStmt( replacement ):
					replacement['suite'].extend( originalSuite )
					return EditOperations.replaceNodeContents( ctx, data, replacement )
				else:
					return EditOperations.replaceWithRange( ctx, data, [ replacement, Nodes.IndentedBlock( suite=originalSuite ) ] )
			else:
				if _isCompoundStmt( replacement ):
					parent = data.getParentTreeNode()
					if parent is None:
						raise TypeError, 'PythonEditOperations:pyReplace(): no parent '
					index = parent.indexOfById( data.getNode() )
					if index == -1:
						raise ValueError, 'could not replace'
					if len( parent )  > ( index + 1 ):
						if parent[index+1].isInstanceOf( Nodes.IndentedBlock ):
							# Join the indented block
							indentedBlock = parent[index+1]
							originalSuite = indentedBlock['suite']
							replacement['suite'].extend( originalSuite )
							del parent[index+1]
							return EditOperations.replaceNodeContents( ctx, data, replacement )
					return EditOperations.replaceNodeContents( ctx, data, replacement )
				else:
					return EditOperations.replaceNodeContents( ctx, data, replacement )
	else:
		raise TypeError, 'PythonEditOperations:pyReplace(): @data must be a DocTreeNode'
	
	
def pyReplaceStatementWithRange(ctx, data, replacement):
	if isinstance( data, DocTreeNode ):
		if len( replacement ) == 1:
			return [ pyReplaceStatement( ctx, data, replacement[0] ) ]
		else:
			xs = EditOperations.insertRangeBefore( ctx, data, replacement[:-1] )
			xs += [ pyReplaceStatement( ctx, data, replacement[-1] ) ]
			return xs
	else:
		raise TypeError, 'PythonEditOperations:pyReplace(): @data must be a DocTreeNode'
	
