##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.CodeGraph.CGNode import CGNode
from Britefury.CodeGraph.CGLocalVarDeclaration import CGLocalVarDeclaration
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *
from Britefury.VirtualMachine.vcls_string import pyStrToVString

from Britefury.LowLevelCodeTree.LLCTBlock import LLCTBlock
from Britefury.LowLevelCodeTree.LLCTLoadLocalExp import LLCTLoadLocalExp
from Britefury.LowLevelCodeTree.LLCTAssignmentExp import LLCTAssignmentExp
from Britefury.LowLevelCodeTree.LLCTBindExp import LLCTBindExp
from Britefury.LowLevelCodeTree.LLCTClosureExp import LLCTClosureExp
from Britefury.LowLevelCodeTree.LLCTSendMessageExp import LLCTSendMessageExp
from Britefury.LowLevelCodeTree.LLCTReturnExp import LLCTReturnExp
from Britefury.LowLevelCodeTree.LLCTLoadConstantExp import LLCTLoadConstantExp

from Britefury.VirtualMachine.VMTag import VMTag



def loadVarName(varName):
	return '_' + varName

def getVarName(varName):
	return varName

def setVarName(varName):
	return varName + '='

localModuleName = '__module__'


class CGModule (CGNode):
	name = Field( str, '' )
	statements = SheetGraphSinkMultipleField( 'Statements', 'Statement list' )



	def generateStatementLLCT(self, node, moduleTag, tree):
		if isinstance( node, CGLocalVarDeclaration ):
			varName = node.variable[0].node.name
			varTag = node.variable[0].node.generateLLCT( tree )
			loadVarTag = VMTag( 'ModuleLoadVariable', loadVarName( varName ) )
			getVarNameLLCT = LLCTLoadConstantExp( pyStrToVString( getVarName( varName ) ) )
			setVarNameLLCT = LLCTLoadConstantExp( pyStrToVString( setVarName( varName ) ) )
			getBlock = LLCTBlock( '%s:%s.get' % ( self.name, varName ), [ LLCTReturnExp( LLCTLoadLocalExp( varTag ) ) ] )
			setBlock = LLCTBlock( '%s:%s.set' % ( self.name, varName ), [ LLCTAssignmentExp( varTag, LLCTLoadLocalExp( loadVarTag ) ) ], [ loadVarTag ] )
			addGet = LLCTSendMessageExp( LLCTLoadLocalExp( moduleTag ), 'setInstanceMessage', [ getVarNameLLCT, LLCTClosureExp( getBlock ) ] )
			addSet = LLCTSendMessageExp( LLCTLoadLocalExp( moduleTag ), 'setInstanceMessage', [ setVarNameLLCT, LLCTClosureExp( setBlock ) ] )
			return [ node.generateLLCT( tree ) ]  +  [ addGet, addSet ]
		else:
			return [ node.generateLLCT( tree ) ]

	def generateLLCT(self, tree):
		moduleTag = VMTag( 'Module', localModuleName )
		llctStatements = [ LLCTBindExp( moduleTag, LLCTSendMessageExp( LLCTLoadLocalExp( tree.tag_Module ), 'new', [] ) ) ]
		for statementSource in self.statements:
			llctStatements += self.generateStatementLLCT( statementSource.node, moduleTag, tree )
		return LLCTBlock( self.name, llctStatements )
