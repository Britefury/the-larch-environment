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

from Britefury.I18n import i18n
i18n.initialise()


from BritefuryJ.DocModel import DMIOReader, DMNode

from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymDocument import GSymDocument

from Britefury.MainApp.MainApp import MainApp


from GSymCore.GSymApp import GSymApp



def main():
	UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

	world = GSymWorld()

	if len( sys.argv ) == 2:
		filename = sys.argv[1]
		assert False, 'Load file from command line not implemented yet'
		
	app = MainApp( world )

	app.show()


if __name__ == '__main__':
	main()