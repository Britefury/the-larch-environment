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
		self._viewFactory = None
		self._transformModifyFn = None
		
		
	def registerCodeGeneratorFactory(self, format, factory):
		self._codeGeneratorFactories[format] = factory



	def registerViewFactory(self, factory):
		self._viewFactory = factory

	
	def registerTransformModifyFn(self, transformModifyFn):
		self._transformModifyFn = transformModifyFn
		
		
	def getCodeGeneratorFactory(self, format):
		return self._codeGeneratorFactories[format]
	
	def getViewFactory(self):
		return self._viewFactory
	
	def getTransformModifyFn(self):
		return self._transformModifyFn


