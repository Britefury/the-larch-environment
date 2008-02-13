##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from copy import copy


class GLispFrame (object):
	def __init__(self, **env):
		self._env = copy( env )

	def __getitem__(self, key):
		return self._env[key]

	def __setitem__(self, key, value):
		self._env[key] = value
