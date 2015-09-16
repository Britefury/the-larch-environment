##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from BritefuryJ.GraphViz import GraphViz

from Britefury.Config import PathsConfigPage, GraphVizConfigPage, FontConfigPage, TipBoxConfig

from Britefury.app_fonts import loadFonts
from LarchCore.Kernel import interpreter_config_page


_shutdownListeners = []

def appInit():
	loadFonts()
	PathsConfigPage.initPathsConfig()
	GraphVizConfigPage.initGraphVizConfig()
	FontConfigPage.initFontConfig()
	TipBoxConfig.initTipboxConfig()
	interpreter_config_page.init_interpreter_config()



def appShutdown():
	PathsConfigPage.savePathsConfig()
	GraphVizConfigPage.saveGraphVizConfig()
	GraphViz.shutdown()
	FontConfigPage.saveFontConfig()
	TipBoxConfig.saveTipboxConfig()
	interpreter_config_page.save_interpreter_config()

	for l in _shutdownListeners:
		l()



def registerShutdownListener(shutdownFn):
	_shutdownListeners.append(shutdownFn)