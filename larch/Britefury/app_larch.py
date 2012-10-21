##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys
import jarray

from java.lang import String, Thread
from java.util.jar import JarInputStream
from javax.swing import UIManager

from BritefuryJ.DocModel import DMIOReader, DMNode
from BritefuryJ.Browser import Location

from Britefury.Kernel.World import World, WorldDefaultOuterSubject
from Britefury.Kernel.Document import Document
from Britefury import app, app_startup, app_in_jar

from Britefury.Windows.WindowManager import WindowManager

from LarchCore.MainApp import MainApp



world = None


def start_larch():
	global world

	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )
	app.appInit()



	jarCustomLarchApp = []
	jarCustomPythonApp = [ False ]


	# Register a JAR entry handler so that we can pick up custom apps
	def _handle_larch_app(name, reader):
		inputStream = reader()
		jarCustomLarchApp.append( inputStream )

	def _handle_python_app(name, reader):
		jarCustomPythonApp[0] = True

	app_in_jar.registerJarEntryHandler( lambda name: name == 'mainapp/app.larch', _handle_larch_app )
	app_in_jar.registerJarEntryHandler( lambda name: name == 'mainapp/app.py'  or  name == 'mainapp.app$py.class', _handle_python_app )



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
			outerSubject = WorldDefaultOuterSubject( world )
			subject = document.newSubject( outerSubject, Location( 'main' ), None, filename )
			world.setRootSubject( subject )
	else:
		foundAppInJar = False

		if len( jarCustomLarchApp ) > 0:
			inputStream = jarCustomLarchApp[0]
			document = Document.readFromInputStream( world, inputStream, 'app' )
			outerSubject = WorldDefaultOuterSubject( world )
			subject = document.newSubject( outerSubject, Location( 'main' ), None, 'app' )
			world.setRootSubject( subject )
		elif jarCustomPythonApp[0]:
			from mainapp import app as appModule
			appState = appModule.newAppState()
			world.setRootSubject( appModule.newAppStateSubject( world, appState ) )
		else:
			appState = MainApp.newAppState()
			world.setRootSubject( MainApp.newAppStateSubject( world, appState ) )



	def _onClose(wm):
		app.appShutdown()

	wm = WindowManager( world )
	wm.setCloseLastWindowListener( _onClose )

	wm.showRootWindow()
