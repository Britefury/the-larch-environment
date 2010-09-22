##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import sys
import imp

from GSymCore.Project import Schema


class _PackageLoader (object):
	"""
	A module loader for loading project packages
	Looks for an __init__ module, and attempts to load that.
	"""
	def __init__(self, projectSubject, packageLocation, package, world):
		self._projectSubject = projectSubject
		self._packageLocation = packageLocation
		self._package = package
		self._world = world
		

	def load_module(self, fullname):
		# First, see if there is an '__init__; page
		for model in self._package['contents']:
			if model['name'] == '__init__':
				if model.isInstanceOf( Schema.Page ):
					# We have found a page called '__init__' - get its subject
					modelLocation = self._packageLocation + '.__init__'
					pageSubject = self._projectSubject._document.newUnitSubject( model['unit'], self._projectSubject, modelLocation )
					# Now, check if it has a 'createModuleLoader' method - if it has, then we can use it. Otherwise, use the default
					try:
						createModuleLoader = pageSubject.createModuleLoader
					except AttributeError:
						return self._default_load_module( fullname )
					else:
						loader = createModuleLoader( self._world )
						return loader.load_module( fullname )
		
		return self._default_load_module( fullname )

					
	def _default_load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		self._world.registerImportedModule( fullname )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod


class ModuleFinder (object):
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, namesuffix, path, world):
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
							# We have found a page: get its subject
							pageSubject = self._projectSubject._document.newUnitSubject( model['unit'], self._projectSubject, modelLocation )
							# Now, check if it has a 'createModuleLoader' method - if it has, then we can use it. Otherwise, we can't
							try:
								createModuleLoader = pageSubject.createModuleLoader
							except AttributeError:
								return None
							else:
								# The subject has a 'createModuleLoader' attribute - invoke it to create the module loader, for the module import system to use
								return createModuleLoader( world )
						else:
							# Still path to consume; cannot go further
							return None
					elif model.isInstanceOf( Schema.Package ):
						if suffix == '':
							# The import statement is attempting to import this package
							return _PackageLoader( self._projectSubject, modelLocation, model, world )
						else:
							# Still path to consume; exit the name search for-loop, into the traversal while-loop
							break
					else:
						raise TypeError, 'unreckognised model type'
			if not bFoundItem:
				return None

		# Ran out of name to comsume, ERROR
		raise ValueError, 'Name consumed, target not found'


	

class _RootModuleLoader (object):
	def __init__(self, world):
		self._world = world

	def load_module(self, fullname):
		mod = sys.modules.setdefault( fullname, imp.new_module( fullname ) )
		self._world.registerImportedModule( fullname )
		mod.__file__ = fullname
		mod.__loader__ = self
		mod.__path__ = fullname.split( '.' )
		return mod
	

class RootFinder (object):
	"""
	_RootFinder is a module finder that navigates the root Python package name of a project
	"""
	def __init__(self, projectSubject):
		self._projectSubject = projectSubject

	def find_module(self, fullname, path, world):
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
						return self._projectSubject._moduleFinder.find_module( fullname, suffix, path, world )
				else:
					return None
		return _RootModuleLoader( world )



def _getImportedModulesToUnloadFromPackage(world, package, fullname):
	modules = []
	for item in package['contents']:
		name = item['name']
		itemFullname = fullname + '.' + name
		if item.isInstanceOf( Schema.Package ):
			modules.append( itemFullname )
			modules.extend( _getImportedModulesToUnloadFromPackage( world, item, itemFullname ) )
		elif item.isInstanceOf( Schema.Page ):
			modules.append( itemFullname )
		else:
			raise TypeError, 'unknown project item type'
	return modules

def unloadImportedModules(world, project):
	fullname = project['pythonPackageName']
	modules = [ fullname ]
	modules.extend( _getImportedModulesToUnloadFromPackage( world, project, project['pythonPackageName'] ) )
	print 'GSymCore.Project.ProjectEditor.ModuleFinder.unloadImportedModules: removing:'
	for module in modules:
		print '\t' + module
	return world.unloadImportedModules( modules )

