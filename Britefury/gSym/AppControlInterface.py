##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************



class AppControlInterface (object):
	# handleNewPageFn(unit)
	def promptNewPage(self, handleNewPageFn):
		pass
	
	# handleImportedPageFn(name, unit)
	def promptImportPage(self, handleImportedPageFn):
		pass
	
	
	# handleNewDocumentFn(unit)
	def promptNewDocument(self, handleNewDocumentFn):
		pass
	
	# handleOpenedDocumentFn(fullPath, document)
	def promptOpenDocument(self, handleOpenedDocumentFn):
		pass
	
	# handleSaveDocumentAsFn(filename)
	def promptSaveDocumentAs(self, handleSaveDocumentAsFn):
		pass
	
	
	def getWorld(self):
		return None
	
	