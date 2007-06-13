##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeGraph.CGModule import CGModule
from Britefury.CodeGraph.CGBlock import CGBlock

from Britefury.MainApp.MainApp import MainApp





if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk


	from Britefury.I18n import i18n

	i18n.initialise()

	bBuildGraphView = '--with-graph-view'  in  sys.argv



	graph = SheetGraph()

	# main module
	mainModule = CGModule()
	graph.nodes.append( mainModule )

	mainBlock = CGBlock()
	graph.nodes.append( mainBlock )

	# connect module -> block
	mainModule.block.append( mainBlock.parent )

	app = MainApp( graph, mainModule, bBuildGraphView )

	gtk.main()
