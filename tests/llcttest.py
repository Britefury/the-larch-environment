##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.VirtualMachine.vcls_string import *
from Britefury.LowLevelCodeTree.LLCTAssignmentExp import *
from Britefury.LowLevelCodeTree.LLCTBlock import *
from Britefury.LowLevelCodeTree.LLCTClosureExp import *
from Britefury.LowLevelCodeTree.LLCTLoadConstantExp import *
from Britefury.LowLevelCodeTree.LLCTLoadLocalExp import *
from Britefury.LowLevelCodeTree.LLCTSendMessageExp import *





if __name__ == '__main__':
	printBlock = LLCTBlock( 'printString', [ LLCTSendMessageExp( LLCTLoadLocalExp( 'text' ), 'print', [] ) ], [ 'text' ] )

	mainBlock = LLCTBlock( 'main', [ LLCTSendMessageExp( LLCTClosureExp( printBlock ), 'call', [ LLCTLoadConstantExp( pyStrToVString( 'Hello world' ) ) ] ) ] )

	machine = VMMachine()

	block = mainBlock.generateBlockInstructions()

	machine.run( block, bDebug=True )
