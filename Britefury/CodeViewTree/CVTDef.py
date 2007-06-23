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

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTStatement import CVTStatement
from Britefury.CodeViewTree.CVTDefDoc import CVTRuleDefDoc
from Britefury.CodeViewTree.CodeViewTree import *



class CVTDef (CVTStatement):
	graphNode = SheetRefField( CGDef )


	docNode = CVTSimpleNodeProductionSingleField( CVTRuleDefDoc )
	declVarNode = CVTSimpleSinkProductionSingleField( CGDef.declVar )
	paramsNode = CVTSimpleSinkProductionSingleField( CGDef.parameters )
	statementsNode = CVTSimpleSinkProductionSingleField( CGDef.block )




class CVTRuleDef (CVTRuleSimple):
	graphNodeClass = CGDef
	cvtNodeClass = CVTDef

CVTRuleDef.register()

