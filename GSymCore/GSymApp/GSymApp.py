##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from Britefury.gSym.gSymUnitClass import GSymUnitClass
from Britefury.gSym.gSymDocument import gSymUnit

from GSymCore.GSymApp.GSymAppViewer.View import viewGSymAppDocNodeAsElement, viewGSymAppDocNodeAsPage, resolveGSymAppLocation
from GSymCore.GSymApp import NodeClasses as Nodes


def newAppState():
	configuration = Nodes.AppConfiguration()
	appState = Nodes.AppState( openDocuments=[], configuration=configuration )
	return gSymUnit( Nodes.schema, appState )



unitClass = GSymUnitClass( Nodes.schema )
unitClass.registerViewDocNodeAsElementFn( viewGSymAppDocNodeAsElement )
unitClass.registerViewDocNodeAsPageFn( viewGSymAppDocNodeAsPage )
unitClass.registerResolveLocationFn( resolveGSymAppLocation )


