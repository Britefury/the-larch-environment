##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys

from BritefuryJ.Projection import Subject

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Pres.Primitive import Column

from Britefury.Kernel import AppLocationPath

from LarchCore.Languages.Python2.CodeGenerator import compileForModuleExecution
from LarchCore.Languages.Python2.PythonEditor.View import perspective as python2Perspective


class _Python2ModuleLoader (object):
	def __init__(self, model, document):
		self._model = model
		self._document = document
		
	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass
		mod = self._document.newModule( fullname, self )
		code = compileForModuleExecution( mod, self._model, fullname )
		exec code in mod.__dict__
		return mod



class _Python2Page (object):
	def __init__(self, model):
		self._model = model

	def __present__(self, fragment, inherited_state):
		linkHeader = AppLocationPath.appLinkheaderBar( fragment.subject, [] )
		pyView = python2Perspective.applyTo( self._model )
		return Column( [ linkHeader.padY( 0.0, 5.0 ).alignVRefY(), pyView.alignVRefYExpand() ] )


class Python2Subject (Subject):
	def __init__(self, document, model, enclosingSubject, importName, title):
		super( Python2Subject, self ).__init__( enclosingSubject )
		self._document = document
		self._model = model
		self._importName = importName
		self._title = title
		self._page = _Python2Page( model )


	def getFocus(self):
		return self._page
	
	def getPerspective(self):
		return DefaultPerspective.instance
	
	def getTitle(self):
		return self._title + ' [Py25]'
	
	@property
	def appLocationPath(self):
		return self.enclosingSubject.appLocationPath.withPathEntry(  'Python 2.x', self )

	def getChangeHistory(self):
		return self._document.getChangeHistory()

	
	def createModuleLoader(self, document):
		return _Python2ModuleLoader( self._model, document )
	
	