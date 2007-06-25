##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGIfBlock import CGIfBlock

from Britefury.CodeViewTree.CVTNode import *
from Britefury.CodeViewTree.CVTIfBlock import CVTIfBlock
from Britefury.CodeViewTree.CodeViewTree import *



class CVTElseIfBlock (CVTIfBlock):
	pass



class CVTRuleElseIfBlock (CVTRuleSimple):
	graphNodeClass = CGIfBlock
	cvtNodeClass = CVTElseIfBlock


