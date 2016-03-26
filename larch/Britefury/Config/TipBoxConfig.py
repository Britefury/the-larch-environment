##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
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




