##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.VirtualMachine.VMMachine import VMMachine

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
from Britefury.CodeView.CodeView import CodeView

from Britefury.Event.QueuedEvent import queueEvent

from Britefury.LowLevelCodeTree.LowLevelCodeTree import LowLevelCodeTree




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

	mainBindPrintString.value = printStringClosure.parent
	mainBindPrintString.variable = mainPrintStringVar.declaration

	mainLoadPrintString.variable = mainPrintStringVar.references

	mainHelloWorld.value = 'Hello world'

	mainCallPrintString.targetObject = mainLoadPrintString.parent
	mainCallPrintString.messageName = 'call'
	mainCallPrintString.args.append( mainHelloWorld.parent )

	mainPrintStringVar.name = 'printString'


	graph = SheetGraph()

	print 'Built graph'

	tree = CodeViewTree( graph )
	treeNode = tree.buildNode( mainModule )

	print 'Built code view tree'

	view = CodeView( tree )
	viewNode = view.buildView( treeNode, None )

	print 'Built code view'



	def refreshCodeView():
		view.refresh()


	def queueRefresh():
		queueEvent( refreshCodeView )

	view.refreshCell.changedSignal.connect( queueRefresh )



	def executeCode(widget):
		machine = VMMachine()

		llctTree = LowLevelCodeTree( graph )
		llctNode = mainModule.generateLLCT( llctTree )
		block = llctNode.generateBlockInstructions()

		machine.run( block, bDebug=False )



	def executeCodeWithDebug(widget):
		machine = VMMachine()

		llctTree = LowLevelCodeTree( graph )
		llctNode = mainModule.generateLLCT( llctTree )
		block = llctNode.generateBlockInstructions()

		machine.run( block, bDebug=True )



	def oneToOne(widget):
		doc.oneToOne()


	view.refresh()


	import cairo
	from Britefury.DocView.Toolkit.DTDocument import DTDocument
	import pygtk
	pygtk.require( '2.0' )
	import gtk

	def onDeleteEvent(widget, event, data=None):
		return False

	def onDestroy(widget, data=None):
		gtk.main_quit()



	doc = DTDocument()
	doc.show()

	doc.child = viewNode.widget

	view.setDocument( doc )


	oneToOneButton = gtk.Button( '1:1' )
	oneToOneButton.show()
	oneToOneButton.connect( 'clicked', oneToOne )


	executeButton = gtk.Button( 'Execute' )
	executeButton.show()
	executeButton.connect( 'clicked', executeCode )


	executeDebugButton = gtk.Button( 'Execute with debug' )
	executeDebugButton.show()
	executeDebugButton.connect( 'clicked', executeCodeWithDebug )


	buttonBox = gtk.HBox( spacing=20 )
	buttonBox.pack_end( executeDebugButton, False, False, 0 )
	buttonBox.pack_end( executeButton, False, False, 0 )
	buttonBox.pack_end( oneToOneButton, False, False, 0 )
	buttonBox.show()




	box = gtk.VBox()
	box.pack_start( doc )
	box.pack_start( gtk.HSeparator(), False, False, 10 )
	box.pack_start( buttonBox, False, False, 10 )
	box.show_all()


	window = gtk.Window( gtk.WINDOW_TOPLEVEL );
	window.connect( 'delete-event', onDeleteEvent )
	window.connect( 'destroy', onDestroy )
	window.set_border_width( 10 )
	window.set_size_request( 640, 480 )
	window.add( box )
	window.show()

	gtk.main()
