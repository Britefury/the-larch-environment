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
		
		self._moduleImporter = GMetaModuleImporter( self, moduleImportContent )
		self._metaLanguageViewFactory = None
		
		
	def _f_setMetaLanguageViewFactory(self, viewFactory):
		self._metaLanguageViewFactory = viewFactory
		
	def _f_getMetaLanguageViewFactory(self):
		return self._metaLanguageViewFactory
	
	
	def getModuleImporter(self):
		return self._moduleImporter
	
	def getModule(self, path):
		return self._moduleImporter.getModule( path )
	
	def createModule(self, name, xs):
		return self._moduleImporter.createModule( name, xs )
	
	
	@staticmethod
	def filenameToModuleName(filename):
		return os.path.splitext( os.path.split( filename )[1] )[0]
		

	
