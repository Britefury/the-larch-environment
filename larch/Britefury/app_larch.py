##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from javax.swing import UIManager

from BritefuryJ.DocModel import DMIOReader, DMNode
from BritefuryJ.Browser import Location

from Britefury.Kernel.World import World, WorldDefaultOuterSubject
from Britefury.Kernel.Document import Document
from Britefury import app

from Britefury.Windows.WindowManager import WindowManager

from LarchCore.MainApp import MainApp



def start_larch():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )
	app.appInit()

	world = World()
	world.enableImportHooks()

	if len( sys.argv ) > 1:
		if sys.argv[1] == '-app':
			if len( sys.argv ) < 3:
				print 'Usage:'
				print '\t{0} -app <app_name>'
				sys.exit()
			# Custom app
			importName = sys.argv[2]
			appModule = __import__( importName )

			components = importName.split( '.' )
			for comp in components[1:]:
				appModule = getattr( appModule, comp )

			appState = appModule.newAppState()
			world.setRootSubject( appModule.newAppStateSubject( world, appState ) )
		else:
			# Load a document
			filename = sys.argv[1]
			document = Document.readFile( world, filename )
			outerSubject = WorldDefaultOuterSubject( world )
			subject = document.newSubject( outerSubject, Location( 'main' ), None, filename )
			world.setRootSubject( subject )
	else:
		appState = MainApp.newAppState()
		world.setRootSubject( MainApp.newAppStateSubject( world, appState ) )



	def _onClose(wm):
		app.appShutdown()

	wm = WindowManager( world )
	wm.setCloseLastWindowListener( _onClose )

	wm.showRootWindow()
