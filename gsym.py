##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys
import os

from javax.swing import UIManager

from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from Britefury.I18n import i18n
i18n.initialise()


from BritefuryJ.DocModel import DMIOReader, DMNode

from Britefury.gSym.gSymEnvironment import initGSymEnvironment, shutdownGSymEnvironment
from Britefury.gSym.gSymWorld import GSymWorld

from Britefury.MainApp.MainApp import MainApp


def main():
	initGSymEnvironment()
	
	world = GSymWorld()
	

	if len( sys.argv ) == 2:
		filename = sys.argv[1]
		try:
			documentRoot = DMIOReader.readFromString( file( filename, 'r' ).read(), world.resolver )
			documentRoot = DMNode.coerce( documentRoot )
			bEvaluate = True
		except IOError:
			pass
		
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

	app = MainApp( world, None )

	app.run()
	
	shutdownGSymEnvironment()


if __name__ == '__main__':
	main()