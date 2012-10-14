##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.Pres.Help import TipBox

from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig




_tipboxConfigFilename = 'tipboxes'



def initTipboxConfig():
	config = loadUserConfig( _tipboxConfigFilename )
	if isinstance( config, dict ):
		TipBox.initialiseTipHiddenStates( config )



def saveTipboxConfig():
	config = dict( TipBox.getTipHiddenStates() )
	saveUserConfig( _tipboxConfigFilename, config )




