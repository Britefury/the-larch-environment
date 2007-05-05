##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMFrame import VMFrame
from Britefury.VirtualMachine.VMBlock import VMBlock
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.VMMachine import VMMachine
from Britefury.VirtualMachine.VMMessage import VMMessage
from Britefury.VirtualMachine.VMBuiltinMessage import VMBuiltinMessage
from Britefury.VirtualMachine.VObject import VObject
from Britefury.VirtualMachine.VClass import VClass
from Britefury.VirtualMachine.vcls_object import vobjectmsg_alloc, vcls_object
import Britefury.VirtualMachine.vcls_bool
from Britefury.VirtualMachine.vcls_string import pyStrToVString
import Britefury.VirtualMachine.vcls_list
import Britefury.VirtualMachine.vcls_closure
import Britefury.VirtualMachine.vcls_frame
from Britefury.VirtualMachine.vcls_block import blockToVBlock
from Britefury.VirtualMachine.Registers import *











if __name__ == '__main__':
	block = VMBlock( 'main', VMMachine.baseBlock )
	printBlock = VMBlock( 'printString', block, argNames=[ 'text' ],  )

	printBlockInstructions = [
		SendMessageInstruction( printBlock.getLocalReg( 'text' ), 0, [] )
	]


	blockInstructions = [
		SendMessageInstruction( block.getLocalReg( 'Closure' ), 1, [ constRegister( 0 ) ] ),					# call Closure.new( C0, T0 )	[ printBlock ]
		MoveInstruction( tempRegister( 1 ), resultRegister() ),											# T1 <- closure
		SendMessageInstruction( tempRegister( 1 ), 2, [ constRegister( 3 ) ] )								# call T1.call( C3 )			[ 'hello world' ]
	]

	printBlock.initialise( printBlockInstructions, 1, constants=[ pyStrToVString( 'print' ) ] )
	block.initialise( blockInstructions, 2, [ blockToVBlock( printBlock ), pyStrToVString( 'new' ), pyStrToVString( 'call' ), pyStrToVString( 'hello world' ) ] )

	machine = VMMachine()

	machine.run( block, bDebug=True )

