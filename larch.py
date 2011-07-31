##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from javax.swing import UIManager

from Britefury.I18n import i18n
i18n.initialise()


from BritefuryJ.DocModel import DMIOReader, DMNode

from Britefury.Kernel.World import World
from Britefury.Kernel.Document import Document
from Britefury import app

from Britefury.Windows.WindowManager import WindowManager



def main():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() )
	app.appInit()

	world = World()
	world.enableImportHooks()

	if len( sys.argv ) > 1:
		filenames = sys.argv[1:]
		appStateSubject = world.getAppStateSubject()
		for filename in filenames:
			try:
				document = appStateSubject.loadDocument( filename )
				if document is None:
					print 'Failed to load document from %s'  %  filename
			except:
				print 'Failed to load %s'  %  filename
		
	def _onClose(wm):
		app.appShutdown()

	wm = WindowManager( world )
	wm.setCloseLastWindowListener( _onClose )

	wm.showRootWindow()




if __name__ == '__main__':
	main()