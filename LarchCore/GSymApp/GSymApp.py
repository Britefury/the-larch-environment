##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.DocPresent.Browser import Location

from LarchCore.GSymApp.GSymAppViewer.View import perspective as gSymAppViewerPerspective
from LarchCore.GSymApp.GSymAppViewer.Subject import GSymAppSubject
from LarchCore.GSymApp import Application


def newAppState():
	return Application.AppState()

def newAppStateSubject(world):
	return GSymAppSubject( newAppState(), world, Location( 'main' ) )


