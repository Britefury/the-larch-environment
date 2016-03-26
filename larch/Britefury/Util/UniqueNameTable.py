##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************

class UniqueNameTable (object):
	def __init__(self):
		self.__nameToCount = {}


	def clear(self):
		self.__nameToCount.clear()


	def uniqueName(self, name):
		if name in self.__nameToCount:
			self.__nameToCount[name] += 1
			index = self.__nameToCount[name]
			return self.uniqueName( name + '_' + str( index ) )
		else:
			self.__nameToCount[name] = 1
			return name
