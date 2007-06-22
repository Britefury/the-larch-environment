##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGGetAttr import CGGetAttr

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTExpression import CVTExpression
from Britefury.CodeViewTree.CVTAttrName import *
from Britefury.CodeViewTree.CodeViewTree import *

from Britefury.CodeViewTreeOperations.CVTOWrapInAssignment import cvto_wrapInAssignment



class CVTGetAttr (CVTExpression):
	graphNode = SheetRefField( CGGetAttr )


	targetObjectNode = CVTSimpleSinkProductionSingleField( CGGetAttr.targetObject )
	attrNameNode = CVTSimpleNodeProductionSingleField( CVTRuleAttrName )



	def unwrapGetAttr(self):
		parentCGSink = self.graphNode.parent[0]
		targetObjectSource = self.graphNode.targetObject[0]

		self.graphNode.targetObject.remove( targetObjectSource )

		parentCGSink.replace( self.graphNode.parent, targetObjectSource )

		self.graphNode.destroySubtree()



	def wrapInAssignment(self):
		return cvto_wrapInAssignment( self )



class CVTRuleGetAttr (CVTRuleSimple):
	graphNodeClass = CGGetAttr
	cvtNodeClass = CVTGetAttr

CVTRuleGetAttr.register()

