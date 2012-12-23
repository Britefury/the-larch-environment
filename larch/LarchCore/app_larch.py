##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from javax.swing import UIManager

from Britefury.Kernel.World import World
from Britefury.Kernel.Document import Document
from Britefury import app, app_startup, app_in_jar

from Britefury.Windows.WindowManager import WindowManager


world = None


def start_larch():
	global world

	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )
	app.appInit()



	jarCustomLarchApp = []
	jarCustomPythonApp = [ False ]


	# Register a JAR entry handler so that we can pick up custom apps
	def _handle_larch_app(name, reader):
		buf = reader()
		jarCustomLarchApp.append( buf )

	def _handle_python_app(name, reader):
		jarCustomPythonApp[0] = True

	app_in_jar.registerJarEntryHandler( lambda name: name == 'app.larch', _handle_larch_app )
	app_in_jar.registerJarEntryHandler( lambda name: name == 'app.py'  or  name == 'app$py.class', _handle_python_app )



	# If Larch was started from a JAR, scan it
	if app_in_jar.startedFromJar():
		app_in_jar.scanLarchJar()


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
			subject = document.newSubject( world.worldSubject, None, filename )
			world.setRootSubject( subject )
	else:
		if len( jarCustomLarchApp ) > 0:
			buf = jarCustomLarchApp[0]
			document = Document.readFromBytes( world, buf, 'app' )
			subject = document.newSubject( world.worldSubject, None, 'app' )
			world.setRootSubject( subject )
		elif jarCustomPythonApp[0]:
			import app as appModule
			appState = appModule.newAppState()
			world.setRootSubject( appModule.newAppStateSubject( world, appState ) )
		else:
			from LarchCore.MainApp import MainApp
			appState = MainApp.newAppState()
			world.setRootSubject( MainApp.newAppStateSubject( world, appState ) )



	def _onClose(wm):
		app.appShutdown()

	wm = WindowManager( world )
	wm.onCloseLastWindow = _onClose

	wm.showRootWindow()
