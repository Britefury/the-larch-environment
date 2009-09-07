##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymLanguage import GSymLanguage
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.GSymApp.View import viewGSymAppLocationAsElement, getDocNodeForGSymAppLocation
from GSymCore.GSymApp import NodeClasses as Nodes


def newAppState():
	configuration = Nodes.AppConfiguration()
	appState = Nodes.AppState( openDocuments=[], configuration=configuration )
	return gSymUnit( 'GSymCore.GSymApp', appState )


def initialiseModule(world):
	world.registerDMModule( Nodes.module )



language = GSymLanguage()
language.registerViewLocationAsElementFn( viewGSymAppLocationAsElement )
language.registerGetDocNodeForLocationFn( getDocNodeForGSymAppLocation )


