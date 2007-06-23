##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGParameters import CGParameters

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CodeViewTree import *
from Britefury.CodeViewTree.CVTParameterDoc import *



class CVTParametersDoc (CVTNode):
	graphNode = SheetRefField( CGParameters )


	paramDocNodes = CVTSimpleSinkProductionMultipleField( CGParameters.params, rule=CVTRuleParameterDoc )
	expandParamDocNode = CVTSimpleSinkProductionOptionalField( CGParameters.expandParam, rule=CVTRuleParameterDoc )


	def __init__(self, graphNode, tree):
		super( CVTParametersDoc, self ).__init__( graphNode, tree )






class CVTRuleParametersDoc (CVTRuleSimple):
	graphNodeClass = CGParameters
	cvtNodeClass = CVTParametersDoc


