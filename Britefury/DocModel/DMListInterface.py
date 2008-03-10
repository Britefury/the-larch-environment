##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file valued 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.Kernel.Abstract import abstractmethod
from Britefury.DocModel.DMNode import DMNode



def isDMListCompatible(xs):
	return isinstance( xs, list )  or  isinstance( xs, DMListInterface )



class DMListInterface (DMNode):
	@abstractmethod
	def append(self, x):
		pass

	@abstractmethod
	def extend(self, xs):
		pass

	@abstractmethod
	def insert(self, i, x):
		pass

	@abstractmethod
	def remove(self, x):
		pass

	@abstractmethod
	def __setitem__(self, i, x):
		pass

	@abstractmethod
	def __delitem__(self, i):
		pass


	@abstractmethod
	def __getitem__(self, i):
		pass

	@abstractmethod
	def __contains__(self, x):
		pass

	@abstractmethod
	def __iter__(self):
		pass

	@abstractmethod
	def __add__(self, xs):
		pass

	@abstractmethod
	def __len__(self):
		pass

	@abstractmethod
	def index(self, x):
		pass


	@abstractmethod
	def getDestList(self, layer):
		pass

	@abstractmethod
	def getSrcList(self, layer):
		pass



	@abstractmethod
	def __copy__(self):
		pass

	@abstractmethod
	def __deepcopy__(self, memo):
		pass


	def __cmp__(self, x):
		if isinstance( x, DMListInterface ):
			return cmp( self[:], x[:] )
		else:
			return cmp( self[:], x )
		
		
		
