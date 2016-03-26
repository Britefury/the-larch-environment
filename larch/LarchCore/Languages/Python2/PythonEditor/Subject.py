##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import sys

from BritefuryJ.Projection import Subject

from BritefuryJ.DefaultPerspective import DefaultPerspective

from BritefuryJ.Pres.Primitive import Column

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
		return python2Perspective.applyTo( self._model ).alignVRefYExpand()


class Python2Subject (Subject):
	def __init__(self, document, model, enclosingSubject, path, importName, title):
		super( Python2Subject, self ).__init__( enclosingSubject, path )
		self._document = document
		self._model = model
		self._importName = importName
		self._title = title
		self._page = _Python2Page( model )


	def getTrailLinkText(self):
		return 'Python 2.x'


	def getFocus(self):
		return self._page
	
	def getPerspective(self):
		return DefaultPerspective.instance
	
	def getTitle(self):
		return self._title
	
	def getChangeHistory(self):
		return self._document.getChangeHistory()

	
	def createModuleLoader(self, document):
		return _Python2ModuleLoader( self._model, document )
	
	