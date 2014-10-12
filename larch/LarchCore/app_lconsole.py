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
from Britefury import app

from Britefury.Windows.WindowManager import WindowManager
from LarchCore.Kernel.python import inproc_kernel

from LarchCore.PythonConsole import Console

from LarchCore.Languages.Python2.Python2Importer import importPy2File



def start_lconsole():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )


	app.appInit()
	world = World()
	world.enableImportHooks()


	def on_kernel_started(kernel):
		console = Console.Console( kernel, 'Console' )
		world.setRootSubject( Console.ConsoleSubject( console, world.worldSubject ) )

		if len( sys.argv ) > 1:
			if len( sys.argv ) > 2:
				print 'Usage:'
				print '\t %s <python_script>'  %  ( sys.argv[0], )
				sys.exit( -1 )

			filename = sys.argv[1]
			if filename.lower().endswith( '.py' ):
				m = importPy2File( filename )
				console.executeModule( m, True )
			else:
				print 'Python script filename must end with .py'
				sys.exit( -1 )

		def _onClose(wm):
			app.appShutdown()

		wm = WindowManager( world )
		wm.onCloseLastWindow = _onClose

		wm.showRootWindow()

	kernel_ctx = inproc_kernel.InProcessContext()
	kernel_ctx.start_kernel(on_kernel_started)





