##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys
import imp

from BritefuryJ.DocPresent.Browser import Location

from BritefuryJ.GSym import GSymSubject

from GSymCore.Languages.Python25 import Schema
from GSymCore.Languages.Python25.CodeGenerator import compileForExecution
from GSymCore.Languages.Python25.PythonEditor.View import perspective


def _getSuiteStmtByName(suite, name):
	for stmt in reversed( suite ):
		if stmt.isInstanceOf( Schema.DefStmt )  or  stmt.isInstanceOf( Schema.ClassStmt ):
			if stmt['name'] == name:
				return stmt
	return None



class _MemberSubject (GSymSubject):
	def __init__(self, model, pythonSubject, location, name):
		self._model = model
		self._pythonSubject = pythonSubject
		self._location = location
		self._name = name


	def getFocus(self):
		return self._model
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return self._pythonSubject.getTitle() + ' [' + self._name + ']'
	
	def getSubjectContext(self):
		return self._pythonSubject.getSubjectContext().withAttrs( location=self._location )
	
	def getCommandHistory(self):
		return self._pythonSubject._document.getCommandHistory()
	

class _DefSubject (_MemberSubject):
	pass
	
	
class _ClassSubject (_MemberSubject):
	def __getattr__(self, name):
		stmt = _getSuiteStmtByName( self._model['suite'], name )
		if stmt is not None:
			if stmt.isInstanceOf( Schema.DefStmt ):
				return _DefSubject( stmt, self._pythonSubject, self._location + '.' + name, name )
			elif stmt.isInstanceOf( Schema.ClassStmt ):
				return _ClassSubject( stmt, self._pythonSubject, self._location + '.' + name, name )
		raise AttributeError, 'Could not find class or function called ' + name


class Python25Subject (GSymSubject):
	def __init__(self, document, model, enclosingSubject, location):
		self._document = document
		self._model = model
		self._enclosingSubject = enclosingSubject
		self._location = location


	def getFocus(self):
		return self._model
	
	def getPerspective(self):
		return perspective
	
	def getTitle(self):
		return 'Py 2.5: ' + self._enclosingSubject.getTitle()
	
	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( location=self._location )
	
	def getCommandHistory(self):
		return self._document.getCommandHistory()
	
	
	def __getattr__(self, name):
		stmt = _getSuiteStmtByName( self._model['suite'], name )
		if stmt is not None:
			if stmt.isInstanceOf( Schema.DefStmt ):
				return _DefSubject( stmt, self, self._location + '.' + name, name )
			elif stmt.isInstanceOf( Schema.ClassStmt ):
				return _ClassSubject( stmt, self, self._location + '.' + name, name )
		raise AttributeError, 'Could not find class or function called ' + name

	
	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		code = compileForExecution( self._model, fullname )
		exec code in mod.__dict__
		return mod
	
	