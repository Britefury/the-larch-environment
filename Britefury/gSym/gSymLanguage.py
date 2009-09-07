##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


class GSymLanguage (object):
	def __init__(self):
		super( GSymLanguage, self ).__init__()
		self._codeGeneratorFactories = {}
		self._viewLocationAsElementFn = None
		self._getDocNodeForLocationFn = None
		self._transformModifyFn = None
		
		
	def registerCodeGeneratorFactory(self, format, factory):
		self._codeGeneratorFactories[format] = factory



	def registerViewLocationAsElementFn(self, viewLocationAsElementFn):
		self._viewLocationAsElementFn = viewLocationAsElementFn

	def registerGetDocNodeForLocationFn(self, getDocNodeForLocationFn):
		self._getDocNodeForLocationFn = getDocNodeForLocationFn

		
		
	def getCodeGeneratorFactory(self, format):
		return self._codeGeneratorFactories[format]
	
	def getViewLocationAsElementFn(self):
		return self._viewLocationAsElementFn

	def getGetDocNodeForLocationFn(self):
		return self._getDocNodeForLocationFn




class GSymPageFactory (object):
	def __init__(self, menuLabelText, newPageFn):
		self.menuLabelText = menuLabelText
		self.newPageFn = newPageFn



class GSymPageImporter (object):
	def __init__(self, menuLabelText, fileType, filePattern, importFn):
		self.menuLabelText = menuLabelText
		self.fileType = fileType
		self.filePattern = filePattern
		self.importFn = importFn

		
		
class GSymDocumentFactory (object):
	def __init__(self, menuLabelText, newDocumentFn):
		self.menuLabelText = menuLabelText
		self.newDocumentFn = newDocumentFn



