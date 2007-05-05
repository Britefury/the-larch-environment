##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGBlock import CGBlock
from Britefury.CodeGraph.CGClosure import CGClosure
from Britefury.CodeGraph.CGLocalAssignment import CGLocalAssignment
from Britefury.CodeGraph.CGLocalRef import CGLocalRef
from Britefury.CodeGraph.CGLocalVarDeclaration import CGLocalVarDeclaration
from Britefury.CodeGraph.CGModule import CGModule
from Britefury.CodeGraph.CGSendMessage import CGSendMessage
from Britefury.CodeGraph.CGStringLiteral import CGStringLiteral
from Britefury.CodeGraph.CGReturn import CGReturn
from Britefury.CodeGraph.CGVar import CGVar

from Britefury.CodeViewTree.CodeViewTree import CodeViewTree




if __name__ == '__main__':
	# function printString
	printStringClosure = CGClosure()
	printStringBlock = CGBlock()
	printStringSendPrintMessage = CGSendMessage()
	printStringLoadText = CGLocalRef()
	printStringTextParam = CGVar()

	printStringClosure.block = printStringBlock.parent

	printStringBlock.params.append( printStringTextParam.declaration )
	printStringBlock.statements.append( printStringSendPrintMessage.parent )

	printStringSendPrintMessage.targetObject = printStringLoadText.parent
	printStringSendPrintMessage.messageName = 'print'

	printStringLoadText.variable = printStringTextParam.references

	printStringTextParam.name = 'text'



	# main module
	mainModule = CGModule()
	mainBindPrintString = CGLocalVarDeclaration()
	mainLoadPrintString = CGLocalRef()
	mainHelloWorld = CGStringLiteral()
	mainCallPrintString = CGSendMessage()
	mainPrintStringVar = CGVar()


	mainModule.statements.append( mainBindPrintString.parent )
	mainModule.statements.append( mainCallPrintString.parent )

	mainBindPrintString.initialValue = printStringClosure.parent
	mainBindPrintString.variable = mainPrintStringVar.declaration

	mainLoadPrintString.variable = mainPrintStringVar.references

	mainHelloWorld.value = 'Hello world'

	mainCallPrintString.targetObject = mainLoadPrintString.parent
	mainCallPrintString.messageName = 'call'
	mainCallPrintString.args.append( mainHelloWorld.parent )

	mainPrintStringVar.name = 'printString'


	graph = SheetGraph()



	tree = CodeViewTree( graph )
	treeNode = tree.buildNode( mainModule )

