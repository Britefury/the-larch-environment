##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os



class ModuleRegistry (object):
	def __init__(self, moduleLoader):
		self._modules = {}
		self._moduleLoader = moduleLoader
			

	def getModule(self, path):
		realpath = os.path.realpath( path )
		try:
			return self._modules[realpath]
		except KeyError:
			m = self._moduleLoader( realpath )
			self._modules[realpath] = m
			return m
		