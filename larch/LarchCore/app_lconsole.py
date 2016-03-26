##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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


	def on_kernel_started(kernel):
		console = Console.Console(kernel)
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





