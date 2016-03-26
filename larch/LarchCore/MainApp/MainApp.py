##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from LarchCore.MainApp.MainAppViewer.View import perspective as mainAppViewerPerspective
from LarchCore.MainApp.MainAppViewer.Subject import MainAppSubject
from LarchCore.MainApp import Application


def newAppState():
	return Application.AppState()

def newAppStateSubject(world, appState):
	return MainAppSubject( appState, world )



