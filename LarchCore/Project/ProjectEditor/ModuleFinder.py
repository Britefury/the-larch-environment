##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys

from LarchCore.Project.ProjectPackage import ProjectPackage
from LarchCore.Project.ProjectPage import ProjectPage


class PageFinder (object):
	def __init__(self, moduleLoader):
		self._moduleLoader = moduleLoader


	def find_module(self, name, fullname, path):
		return None


	def load_module(self, fullname):
		return self._moduleLoader.load_module( fullname )





class PackageFinder (object):
	def __init__(self, projectSubject, model, modelLocation):
		self._projectSubject = projectSubject
		if model is None:
			model = self._projectSubject._model
		if modelLocation is None:
			modelLocation = self._projectSubject._location
		self._model = model
		self._modelLocation = modelLocation
		self._document = self._projectSubject._document


	def find_module(self, name, fullname, path):
		item = self._model.contentsMap.get( name )

		if item is not None:
			modelLocation = self._modelLocation + '.' + name
			model = item

			if isinstance( model, ProjectPage ):
				# We have found a page: get its subject
				pageSubject = self._projectSubject._document.newModelSubject( model.data, self._projectSubject, modelLocation, fullname, name )
				# Now, check if it has a 'createModuleLoader' method - if it has, then we can use it. Otherwise, we can't
				try:
					createModuleLoader = pageSubject.createModuleLoader
				except AttributeError:
					return None
				else:
					# The subject has a 'createModuleLoader' attribute - invoke it to create the module loader, for the module import system to use
					return PageFinder( createModuleLoader( self._document ) )
			elif isinstance( model, ProjectPackage ):
				return PackageFinder( self._projectSubject, model, modelLocation )
			else:
				raise TypeError, 'unrecognised model type'
		else:
			return None



	# Package module loading
	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		# First, see if there is an '__init__; page
		initPage = self._model.contentsMap.get( '__init__' )

		if initPage is not None and isinstance( initPage, ProjectPage ):
			# We have found a page called '__init__' - get its subject
			modelLocation = self._modelLocation + '.__init__'
			pageSubject = self._projectSubject._document.newModelSubject( initPage.data, self._projectSubject, modelLocation, fullname + '.__init__', '__init__' )
			# Now, check if it has a 'createModuleLoader' method - if it has, then we can use it. Otherwise, use the default
			try:
				createModuleLoader = pageSubject.createModuleLoader
			except AttributeError:
				return self._default_load_module( fullname )
			else:
				loader = createModuleLoader( self._document )
				return loader.load_module( fullname )

		return self._default_load_module( fullname )


	def _default_load_module(self, fullname):
		return self._document.newModule( fullname, self )




class RootFinder (object):
	def __init__(self, projectSubject, pythonPackageName):
		self._document = projectSubject._document
		self._projectSubject = projectSubject
		if pythonPackageName is not None:
			self._name, _, self._nameSuffix = pythonPackageName.partition( '.' )
		else:
			self._name = self._nameSuffix = None


	def find_module(self, name, fullname, path):
		if name == self._name:
			if self._nameSuffix == '':
				return self._projectSubject._packageFinder
			else:
				return RootFinder( self._projectSubject, self._nameSuffix )
		else:
			return None

	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		return self._document.newModule( fullname, self )
