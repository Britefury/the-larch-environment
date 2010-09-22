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

from GSymCore.Project import Schema
from GSymCore.Project.ProjectEditor.View import perspective





class PackageSubject (object):
	def __init__(self, projectSubject, model, location):
		self._projectSubject = projectSubject
		self._model = model
		self._location = location


	def __getattr__(self, name):
		for item in self._model['contents']:
			if name == item['name']:
				itemLocation = self._location + '.' + name
				if item.isInstanceOf( Schema.Package ):
					return PackageSubject( self._projectSubject, item, itemLocation )
				elif item.isInstanceOf( Schema.Page ):
					return self._projectSubject._document.newUnitSubject( item['unit'], self._projectSubject, itemLocation )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )




class _ModuleFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, namesuffix, path):
		suffix = namesuffix
		model = self._projectSubject._model
		modelLocation = self._projectSubject._location
		while suffix != '':
			prefix, dot, suffix = suffix.partition( '.' )
			bFoundItem = False
			for item in model['contents']:
				if prefix == item['name']:
					bFoundItem = True
					modelLocation = modelLocation + '.' + prefix
					model = item

					if model.isInstanceOf( Schema.Page ):
						if suffix == '':
							# We have found a page: load it
							pageSubject = self._projectSubject._document.newUnitSubject( model['unit'], self._projectSubject, modelLocation )
							try:
								l = pageSubject.load_module
							except AttributeError:
								return None
							else:
								return pageSubject
						else:
							# Still path to consume; cannot go further
							return None
					elif model.isInstanceOf( Schema.Package ):
						if suffix == '':
							return self
						else:
							# Still path to consume; loop over
							break
					else:
						raise TypeError, 'unreckognised model type'
			if not bFoundItem:
				return None

		# Ran out of name to comsume, load as a package
		return self

	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod


class _RootFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, path):
		pythonPackageName = self._projectSubject._model['pythonPackageName']
		if pythonPackageName is not None:
			pythonPackageName = pythonPackageName.split( '.' )
			suffix = fullname
			index = 0
			while suffix != '':
				prefix, dot, suffix = suffix.partition( '.' )
				if prefix == pythonPackageName[index]:
					index += 1
					if index == len( pythonPackageName )  and  suffix != '':
						return self._projectSubject._moduleFinder.find_module( fullname, suffix, path )
				else:
					return None
		return self

	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod



class ProjectSubject (GSymSubject):
	def __init__(self, document, model, enclosingSubject, location):
		self._document = document
		self._model = model
		self._enclosingSubject = enclosingSubject
		self._location = location
		self._moduleFinder = _ModuleFinder( self )
		self._rootFinder = _RootFinder( self )


	def getFocus(self):
		return self._model

	def getPerspective(self):
		return perspective

	def getTitle(self):
		return 'Project'

	def getSubjectContext(self):
		return self._enclosingSubject.getSubjectContext().withAttrs( document=self._document, documentLocation=Location( self._location ), location=Location( self._location ) )

	def getCommandHistory(self):
		return self._document.getCommandHistory()



	def __getattr__(self, name):
		for item in self._model['contents']:
			if name == item['name']:
				itemLocation = self._location + '.' + name
				if item.isInstanceOf( Schema.Package ):
					return PackageSubject( self, item, itemLocation )
				elif item.isInstanceOf( Schema.Page ):
					return self._document.newUnitSubject( item['unit'], self, itemLocation )
		raise AttributeError, "Did not find item for '%s'"  %  ( name, )



	def find_module(self, fullname, path=None):
		return self._rootFinder.find_module( fullname, path )

