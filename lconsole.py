##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys
import os

from javax.swing import UIManager

from Britefury.I18n import i18n
i18n.initialise()


from BritefuryJ.DocModel import DMIOReader, DMNode

from Britefury.Kernel.World import World
from Britefury.Kernel.Document import Document
from Britefury import app

from Britefury.Windows.WindowManager import WindowManager


from LarchCore.PythonConsole import Console

from LarchCore.Languages.Python25.Python25Importer import importPy25File



def main():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )
	app.appInit()

	world = World()
	world.enableImportHooks()
	console = Console.newConsole( 'Console' )
	world.setAppStateSubject( None, Console.newConsoleSubject( console, world.getAppStateSubject() ) )
	

	if len( sys.argv ) > 1:
		if len( sys.argv ) > 2:
			print 'Usage:'
			print '\t %s <python_script>'  %  ( sys.argv[0], )
			sys.exit( -1 )
			
		filename = sys.argv[1]
		if filename.lower().endswith( '.py' ):
			m = importPy25File( filename )
			console.executeModule( m, True )
		else:
			print 'Python script filename must end with .py'
			sys.exit( -1 )
		
	def _onClose(wm):
		app.appShutdown()
	
	wm = WindowManager( world )
	wm.setCloseLastWindowListener( _onClose )

	wm.showRootWindow()


if __name__ == '__main__':
	main()