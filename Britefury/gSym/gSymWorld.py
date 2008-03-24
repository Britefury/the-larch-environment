##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os


#
#
#
# GSYM World
#
#
#

class GSymWorld (object):
	def __init__(self):
		super( GSymWorld, self ).__init__()
		
		self._worldTable = {}
		self._metaLanguageViewDef = None
		
		
	def __getitem__(self, key):
		return self._worldTable[key]
	
	def __setitem__(self, key, value):
		self._worldTable[key] = value

		
	def _f_setMetaLanguageViewDefinition(self, viewDef):
		self._metaLanguageViewDef = viewDef
		
	def _f_getMetaLanguageViewDefinition(self):
		return self._metaLanguageViewDef
	
	
	@staticmethod
	def filenameToModuleName(filename):
		return os.path.splitext( os.path.split( filename )[1] )[0]
		

	
