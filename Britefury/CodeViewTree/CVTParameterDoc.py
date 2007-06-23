##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGParameterVar import CGParameterVar

from Britefury.CodeViewTree.CVTNode import CVTNode
from Britefury.CodeViewTree.CodeViewTree import *



class CVTParameterDoc (CVTNode):
	graphNode = SheetRefField( CGParameterVar )

	varName = FieldProxy( graphNode.name )
	paramDoc = FieldProxy( graphNode.paramDoc )


	def __init__(self, graphNode, tree):
		super( CVTParameterDoc, self ).__init__( graphNode, tree )



class CVTRuleParameterDoc (CVTRuleSimple):
	graphNodeClass = CGParameterVar
	cvtNodeClass = CVTParameterDoc

