##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


class GSymUnitClass (object):
	def __init__(self, schema, unitSubjectFactory):
		super( GSymUnitClass, self ).__init__()
		self._schema = schema
		self.name = schema.getName()
		self._unitSubjectFactory = unitSubjectFactory


	def getSchema(self):
		return self._schema

	def getUnitSubjectFactory(self):
		return self._unitSubjectFactory





class GSymPageUnitFactory (object):
	def __init__(self, menuLabelText, newPageFn):
		self.menuLabelText = menuLabelText
		self.newPageFn = newPageFn



class GSymPageUnitImporter (object):
	def __init__(self, menuLabelText, fileType, filePattern, importFn):
		self.menuLabelText = menuLabelText
		self.fileType = fileType
		self.filePattern = filePattern
		self.importFn = importFn

		
		
class GSymDocumentFactory (object):
	def __init__(self, menuLabelText, newDocumentFn):
		self.menuLabelText = menuLabelText
		self.newDocumentFn = newDocumentFn



