##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from BritefuryJ.GraphViz import GraphViz

from Britefury.Config import PathsConfigPage, GraphVizConfigPage, FontConfigPage, TipBoxConfig

from Britefury.app_fonts import loadFonts


_shutdownListeners = []

def appInit():
	loadFonts()
	PathsConfigPage.initPathsConfig()
	GraphVizConfigPage.initGraphVizConfig()
	FontConfigPage.initFontConfig()
	TipBoxConfig.initTipboxConfig()


def appShutdown():
	PathsConfigPage.savePathsConfig()
	GraphVizConfigPage.saveGraphVizConfig()
	GraphViz.shutdown()
	FontConfigPage.saveFontConfig()
	TipBoxConfig.saveTipboxConfig()

	for l in _shutdownListeners:
		l()



def registerShutdownListener(shutdownFn):
	_shutdownListeners.append(shutdownFn)