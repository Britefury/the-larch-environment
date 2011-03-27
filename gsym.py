##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys
import os

from javax.swing import UIManager

from Britefury.I18n import i18n
i18n.initialise()


from BritefuryJ.DocModel import DMIOReader, DMNode

from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymDocument import GSymDocument
from Britefury.gSymConfig import UserConfig

from Britefury.MainApp.MainApp import MainApp


from BritefuryJ.DocModel import DMPickleHelper
DMPickleHelper.initialise()



def main():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
	UserConfig.userConfig.load()

	world = GSymWorld()
	world.enableImportHooks()

	if len( sys.argv ) > 1:
		filenames = sys.argv[1:]
		appStateSubject = world.getAppStateSubject()
		for filename in filenames:
			try:
				if not appStateSubject.loadDocument( filename ):
					print 'Failed to load document from %s'  %  filename
			except:
				print 'Failed to load %s'  %  filename
		
	def _onClose(app):
		UserConfig.userConfig.save()
	
	app = MainApp( world )
	app.setCloseListener( _onClose )

	app.show()
	
	


if __name__ == '__main__':
	main()