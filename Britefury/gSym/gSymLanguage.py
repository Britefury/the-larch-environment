##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.gSym.View.gSymView import GSymViewFactory


class GSymLanguage (object):
	def __init__(self):
		super( GSymLanguage, self ).__init__()
		self._compilerFactories = {}
		self._viewFactory = None
		
		
	def registerCompilerFactory(self, format, factory):
		self._compilerFactories[format] = factory



	def registerViewFactory(self, factory):
		self._viewFactory = factory

		
		
	def getCompilerFactory(self, format):
		return self._compilerFactories[format]
	
	def getViewFactory(self):
		return self._viewFactory


