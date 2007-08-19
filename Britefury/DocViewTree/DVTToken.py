##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.DocGraph.DGToken import DGToken

from Britefury.DocViewTree.DVTNode import DVTNode
from Britefury.DocViewTree.DocViewTree import *



class DVTToken (DVTNode):
	graphNode = SheetRefField( DGToken )

	value = FieldProxy( graphNode.value )



class DVTRuleToken (DVTRuleSimple):
	graphNodeClass = DGToken
	dvtNodeClass = DVTToken

DVTRuleToken.register()

