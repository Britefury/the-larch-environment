##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGDef import CGDef

from Britefury.CodeViewTree.CVTNode import CVTNode
from Britefury.CodeViewTree.CodeViewTree import *
from Britefury.CodeViewTree.CVTParametersDoc import *



class CVTDefDoc (CVTNode):
	graphNode = SheetRefField( CGDef )

	functionName = FieldProxy( graphNode.functionName )
	paramsDocNode = CVTSimpleSinkProductionSingleField( CGDef.parameters, rule=CVTRuleParametersDoc )
	functionDoc = FieldProxy( graphNode.functionDoc )





class CVTRuleDefDoc (CVTRuleSimple):
	graphNodeClass = CGDef
	cvtNodeClass = CVTDefDoc

