from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.VirtualMachine.vcls_string import *
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
	printStringBlock.name = 'printString'

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
	mainModule.name = 'main'

	mainBindPrintString.value = printStringClosure.parent
	mainBindPrintString.variable = mainPrintStringVar.declaration

	mainLoadPrintString.variable = mainPrintStringVar.references

	mainHelloWorld.value = 'Hello world'

	mainCallPrintString.targetObject = mainLoadPrintString.parent
	mainCallPrintString.messageName = 'call'
	mainCallPrintString.args.append( mainHelloWorld.parent )

	mainPrintStringVar.name = 'printString'



	machine = VMMachine()

	llct = mainModule.generateLLCT()
	block = llct.generateBlockInstructions()

	machine.run( block, bDebug=True )
