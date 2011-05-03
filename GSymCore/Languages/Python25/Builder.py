##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.DocModel import DMNode

from GSymCore.Languages.Python25 import Schema as Py



def embeddedExpression(x):
	return Py.EmbeddedObjectExpr( embeddedValue=DMNode.embedIsolated( x ) )


def embeddedStatement(x):
	return Py.EmbeddedObjectStmt( embeddedValue=DMNode.embedIsolated( x ) )
