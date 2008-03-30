##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

from Britefury.gSym.gMeta.gMetaModuleImporter import GMetaModuleImporter



#
#
#
# GSYM World
#
#
#

class GSymWorld (object):
	def __init__(self, moduleImportContent):
		super( GSymWorld, self ).__init__()
		
		self._worldTable = {}
		self._moduleImporter = GMetaModuleImporter( self, moduleImportContent )
		self._metaLanguageViewFactory = None
		
		
	def __getitem__(self, key):
		return self._worldTable[key]
	
	def __setitem__(self, key, value):
		self._worldTable[key] = value

		
	def _f_setMetaLanguageViewFactory(self, viewFactory):
		self._metaLanguageViewFactory = viewFactory
		
	def _f_getMetaLanguageViewFactory(self):
		return self._metaLanguageViewFactory
	
	
	def getModuleImporter(self):
		return self._moduleImporter
	
	
	@staticmethod
	def filenameToModuleName(filename):
		return os.path.splitext( os.path.split( filename )[1] )[0]
		

	
