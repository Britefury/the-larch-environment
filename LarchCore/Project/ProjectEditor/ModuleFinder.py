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


class _PackageLoader (object):
	"""
	A module loader for loading project packages
	Looks for an __init__ module, and attempts to load that.
	"""
	def __init__(self, projectSubject, packageLocation, package, document):
		self._projectSubject = projectSubject
		self._packageLocation = packageLocation
		self._package = package
		self._document = document
		

	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		# First, see if there is an '__init__; page
		initPage = self._package.contentsMap.get( '__init__' )
		
		if initPage is not None and isinstance( initPage, ProjectPage ):
			# We have found a page called '__init__' - get its subject
			modelLocation = self._packageLocation + '.__init__'
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


class ModuleFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, namesuffix, path, document):
		suffix = namesuffix
		model = self._projectSubject._model
		modelLocation = self._projectSubject._location
		
		while suffix != '':
			prefix, dot, suffix = suffix.partition( '.' )

			item = model.contentsMap.get( prefix )

			if item is not None:
				modelLocation = modelLocation + '.' + prefix
				model = item

				if isinstance( model, ProjectPage ):
					if suffix == '':
						# We have found a page: get its subject
						pageSubject = self._projectSubject._document.newModelSubject( model.data, self._projectSubject, modelLocation, fullname, prefix )
						# Now, check if it has a 'createModuleLoader' method - if it has, then we can use it. Otherwise, we can't
						try:
							createModuleLoader = pageSubject.createModuleLoader
						except AttributeError:
							return None
						else:
							# The subject has a 'createModuleLoader' attribute - invoke it to create the module loader, for the module import system to use
							return createModuleLoader( document )
					else:
						# Still path to consume; cannot go further
						return None
				elif isinstance( model, ProjectPackage ):
					if suffix == '':
						# The import statement is attempting to import this package
						return _PackageLoader( self._projectSubject, modelLocation, model, document )
					else:
						# Still path to consume; exit into the traversal while-loop
						continue
				else:
					raise TypeError, 'unrecognised model type'
			else:
				return None

		# Ran out of name to consume, ERROR
		raise ValueError, 'Name \'%s\' consumed, target not found' % namesuffix


	

class _RootModuleLoader (object):
	def __init__(self, document):
		self._document = document

	def load_module(self, fullname):
		try:
			return sys.modules[fullname]
		except KeyError:
			pass

		return self._document.newModule( fullname, self )
	

class RootFinder (object):
	"""
	RootFinder is a module finder that navigates the root Python package name of a project
	"""
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, path, document):
		pythonPackageName = self._projectSubject._model.pythonPackageName
		if pythonPackageName is not None:
			pythonPackageName = pythonPackageName.split( '.' )
			suffix = fullname
			index = 0
			while suffix != '':
				prefix, dot, suffix = suffix.partition( '.' )
				if prefix == pythonPackageName[index]:
					index += 1
					if index == len( pythonPackageName )  and  suffix != '':
						return self._projectSubject._moduleFinder.find_module( fullname, suffix, path, document )
				else:
					return None
			return _RootModuleLoader( document )
		else:
			return None

