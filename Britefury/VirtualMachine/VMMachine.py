##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.VirtualMachine.VMFrame import VMFrame
from Britefury.VirtualMachine.VMBlock import VMBlock
from Britefury.VirtualMachine.VMMessage import VMMessage
from Britefury.VirtualMachine.Instructions import *
from Britefury.VirtualMachine.vcls_object import vcls_object, vcls_class, vobjectmsg_alloc
from Britefury.VirtualMachine.vcls_bool import vcls_bool, vfalse, vtrue
from Britefury.VirtualMachine.vcls_string import vcls_string, pyStrToVString
from Britefury.VirtualMachine.vcls_list import vcls_list
from Britefury.VirtualMachine.vcls_closure import vcls_closure
from Britefury.VirtualMachine.vcls_frame import vcls_frame
from Britefury.VirtualMachine.vcls_block import vcls_block
from Britefury.VirtualMachine.vcls_module import vcls_module
from Britefury.VirtualMachine.vcls_none import vnone
from Britefury.VirtualMachine.Registers import *


class VMMachine (object):
	baseBlock = VMBlock( 'builtins', None )

	reg_Object = baseBlock.allocLocalReg( 'Object' )
	reg_Class = baseBlock.allocLocalReg( 'Class' )
	reg_Bool = baseBlock.allocLocalReg( 'Bool' )
	reg_String = baseBlock.allocLocalReg( 'String' )
	reg_List = baseBlock.allocLocalReg( 'List' )
	reg_Closure = baseBlock.allocLocalReg( 'Closure' )
	reg_Frame = baseBlock.allocLocalReg( 'Frame' )
	reg_Block = baseBlock.allocLocalReg( 'Block' )
	reg_Module = baseBlock.allocLocalReg( 'Module' )
	reg_none = baseBlock.allocLocalReg( 'none' )
	reg_false = baseBlock.allocLocalReg( 'false' )
	reg_true = baseBlock.allocLocalReg( 'true' )

	baseBlock.initialise( [] )

	baseFrame = VMFrame( baseBlock )

	baseFrame.storeReg( reg_Object, vcls_object )
	baseFrame.storeReg( reg_Class, vcls_class )
	baseFrame.storeReg( reg_Bool, vcls_bool )
	baseFrame.storeReg( reg_String, vcls_string )
	baseFrame.storeReg( reg_List, vcls_list )
	baseFrame.storeReg( reg_Closure, vcls_closure )
	baseFrame.storeReg( reg_Frame, vcls_frame )
	baseFrame.storeReg( reg_Block, vcls_block )
	baseFrame.storeReg( reg_Module, vcls_module )
	baseFrame.storeReg( reg_none, vnone )
	baseFrame.storeReg( reg_false, vfalse )
	baseFrame.storeReg( reg_true, vtrue )




	def run(self, block, bDebug=False):
		self.frame = VMFrame( block, self.baseFrame )

		while self.frame is not None  and  self.frame.ip < len( self.frame.block.instructions ):
			instruction = self.frame.block.instructions[self.frame.ip]
			self.frame.ip += 1
			if bDebug:
				print '[%s]' % ( self.frame.block.name, )
				print '\t\t' + '>' * self.frame.depth()  + ' %s'  %  ( instruction.disassemble( self ), )
			instruction.execute( self )


	def pushFrame(self, frame):
		frame.parent = self.frame
		self.frame = frame

	def popFrame(self):
		p = self.frame.parent
		self.frame.parent = None
		self.frame = p







newBlock = VMBlock( 'newMessage', VMMachine.baseBlock, argNames=[], expandArgName='initArgs' )
newInstructions = [
	SendMessageInstruction( selfRegister(), 0, [] ),							# call self.alloc()			[]
	MoveInstruction( tempRegister( 0 ), resultRegister() ),					# T0 <- result
	SendMessageInstruction( tempRegister( 0 ), 1, [], localRegister( 0, 0 ) ),		# call T0.init( *initArgs )
	ReturnInstruction( tempRegister( 0 ) )								# return T0
]
newBlock.initialise( newInstructions, 1, [ pyStrToVString( 'alloc' ), pyStrToVString( 'init' ) ] )
newClosure = vobjectmsg_alloc( vcls_closure, None, [] )
newClosure._block = newBlock
newClosure._outerScope = None
vcls_object.setMessage( 'new', VMMessage( newClosure ) )



