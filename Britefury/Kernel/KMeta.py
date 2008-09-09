##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


class KMetaMember (object):
	"""Meta-member class"""
	def __init__(self, doc=''):
		super( KMetaMember, self ).__init__()
		self._cls = None
		self._name = None
		self._dependencies = []
		self.__doc__ = doc



	def _f_getDependency(self):
		"""Allows dependencies to be redirected;
		if another meta-member adds @self as a dependency, overloading _f_getDependency and returning
		a different meta-member will result in that member being a dependency of the caller rather than @self
		"""
		return self


	def _o_addDependency(self, metaMember):
		metaMember = metaMember._f_getDependency()
		if metaMember not in self._dependencies:
			self._dependencies.append( metaMember )


	def _f_metaMember_isInitialised(self):
		return self._cls is not None  or  self._name is not None

	def _f_metaMember_initMetaMember(self, cls, name):
		"""Initialise a meta-member; called by the metaclass constructor
		@cls - the class that this meta-member is part of
		@name - the attribute name of this meta-member"""
		self._cls = cls
		self._name = name

	def _f_metaMember_overload(self, superMetaMember, superClass):
		"""Inform a meta-member that it is overloading an existing meta-member
		@superMetaMember - the meta-member being overloaded
		@superClass - the base class"""
		raise TypeError, 'cannot overload meta-member named \'%s\' (a %s)' % ( self._name, superMetaMember )

	def _f_metaMember_initClass(self):
		"""Initialise a the class of which this meta-member is part"""
		pass

	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise an instance of the class of which this meta-member is part
		@instance - the instance to initialise
		@srcInstance - an instance from which to copy values"""
		pass


	def _f_metaMember_getClass(self):
		return self._cls

	def _f_metaMember_getName(self):
		return self._name


	def _p_getDependencies(self):
		"""Private - get the dependencies of @self (includes all sub-dependencies)"""
		depsList = []
		for dep in self._dependencies:
			depsList.extend( dep._p_getDependencies() )
		return depsList + [ self ]


	@staticmethod
	def _f_computeDependencySortedList(metaMembers):
		"""Friends - compute a depedency-sorted list of meta-members to process;
		the order will be such that each meta-members appears after its dependencies"""
		result = []
		resultSet = set()
		metaMemberSet = set( metaMembers )
		for member in metaMembers:
			deps = member._p_getDependencies()
			for dep in deps:
				if dep in metaMemberSet  and  dep not in resultSet:
					result.append( dep )
					resultSet.add( dep )
		return result



	@staticmethod
	def _o_computeXmlName(name):
		"""Protected - compute the xml node name"""
		def upperToUnderscorePrefix(c):
			if c.isupper():
				return '_' + c.lower()
			elif c == '_':
				return '__'
			else :
				return c
		return ''.join( [ upperToUnderscorePrefix( c )   for c in name ] )




class KClass (type):
	"""Base meta-class"""
	def __init__(cls, clsName, clsBases, clsDict):
		super( KClass, cls ).__init__( clsName, clsBases, clsDict )
		# Gather a list of meta-members from the base classes
		cls._metaMembers = cls._o_gatherDictFromBases( clsBases, '_metaMembers' )
		cls._dependencySortedMetaMembers = cls._o_gatherListFromBases( clsBases, '_dependencySortedMetaMembers' )


		# Get the list of meta-members, and sort it into dependency-order
		metaMembers = [ value   for value in clsDict.values()   if isinstance( value, KMetaMember ) ]
		dependencySortedMetaMembers = KMetaMember._f_computeDependencySortedList( metaMembers )



		# Initialise meta members
		for name, value in clsDict.items():
			if isinstance( value, KMetaMember ):
				# Initialise the meta-member
				value._f_metaMember_initMetaMember( cls, name )




		# Replace overloaded meta-members in @cls._dependencySortedMetaMembers (meta-members from base classes) with the new meta-members from @cls
		def _getOverload(existingMember):
			name = existingMember._name
			try:
				newMember = clsDict[name]
			except KeyError:
				pass
			else:
				if isinstance( newMember, KMetaMember ):
					return newMember
			return existingMember

		cls._dependencySortedMetaMembers = [ _getOverload( member )   for member in cls._dependencySortedMetaMembers ]


		cls._dependencySortedMetaMembers.extend( [ member   for member in dependencySortedMetaMembers   if member not in cls._dependencySortedMetaMembers ] )



		# Register meta members
		for name, value in clsDict.items():
			if isinstance( value, KMetaMember ):
				# Register meta-member under its name
				cls._metaMembers[name] = value




		# Process any overloading, and initialise the class
		for member in dependencySortedMetaMembers:
			name = member._name

			# See if this meta-member is overloading one from a base class
			for base in clsBases:
				try:
					# Get the existing meta-member
					existingMember = getattr( base, name )
				except AttributeError:
					# No existing meta-member; no overload
					pass
				else:
					# Meta-member exists - attempt overload
					member._f_metaMember_overload( existingMember, base )

			member._f_metaMember_initClass()



	@staticmethod
	def _o_gatherListFromBases(bases, attrName):
		"""Gather a list from base classes
		@bases - sequence of base classes
		@attrName - the name of the attribute"""
		resultList = []
		for base in bases:
			if isinstance( base, KClass ):
				try:
					baseList = getattr( base, attrName )
				except AttributeError:
					baseList = []
				for item in baseList:
					if item not in resultList:
						resultList.append( item )
					else:
						raise TypeError, 'Meta-member named %s exists in multiple base classes' % ( item._name, )
		return resultList

	@staticmethod
	def _o_gatherDictFromBases(bases, attrName):
		"""Gather a dictionary from base classes
		@bases - sequence of base classes
		@attrName - the name of the attribute"""
		resultDict = {}
		for base in bases:
			if isinstance( base, KClass ):
				try:
					baseMetaMembers = getattr( base, attrName )
				except AttributeError:
					pass
				else:
					for key in baseMetaMembers.keys():
						if key in resultDict:
							if baseMetaMembers[key] is not resultDict[key]:
								raise TypeError, 'Different meta-members named %s exist in base classes' % ( key._name, )
					resultDict.update( baseMetaMembers )
		return resultDict




class KObject (object):
	"""KObject, instance of a KClass"""
	__metaclass__ = KClass

	def __init__(self, src=None):
		super( KObject, self ).__init__()
		for member in self._dependencySortedMetaMembers:
			member._f_metaMember_initInstance( self, src )




class KExternalReference (KMetaMember):
	def __init__(self, targetClass):
		super( KExternalReference, self ).__init__()
		self._targetClass = targetClass
		self._refInstanceAttrName = None

	def _f_metaMember_initMetaMember(self, cls, name):
		super( KExternalReference, self )._f_metaMember_initMetaMember( cls, name )
		self._refInstanceAttrName = self._p_computeRefName( name )

	def _f_metaMember_overload(self, superMetaMember, superClass):
		if not issubclass( self._targetClass, superMetaMember._targetClass ):
			raise ValueError, 'cannot overload meta-member %s; the target class must be a subclass of the base meta-member target class' % ( metaMember, )

	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		setattr( instance, self._refInstanceAttrName, None )

	def __get__(self, instance, owner):
		if instance is None:
			return self
		else:
			return getattr( instance, self._refInstanceAttrName )

	def __set__(self, instance, value):
		if not isinstance( value, self._targetClass ):
			raise ValueError, 'value is not an instance of %s'  %  ( self._targetClass.__name__, )
		setattr( instance, self._refInstanceAttrName, value )

	def __getattr__(self, attrName):
		return getattr( self._targetClass, attrName )

	@staticmethod
	def _p_computeRefName(name):
		return intern( '_ref_' + name )




if __name__ == '__main__':
	class AMember (KMetaMember):
		def _f_metaMember_initInstance(self, instance):
			print 'AMember._f_metaMember_initInstance: %s.%s' % ( self._cls.__name__, self._name )

		def __repr__(self):
			return 'AMember %s.%s' % ( self._cls.__name__, self._name )


	class BMember (KMetaMember):
		def _f_metaMember_initInstance(self, instance):
			print 'BMember._f_metaMember_initInstance: %s.%s' % ( self._cls.__name__, self._name )

		def __repr__(self):
			return 'BMember %s.%s' % ( self._cls.__name__, self._name )


	class Model (KObject):
		a = AMember()
		b = BMember()

	class ModelB (Model):
		c = BMember()



	class RefMember (KMetaMember):
		def __init__(self, target):
			self._target = target

		def _f_metaMember_initInstance(self, instance):
			print 'RefMember._f_metaMember_initInstance: %s'  %  ( self._target, )


	class Editor (KObject):
		model = KExternalReference( Model )
		a = RefMember( model.a )
		b = RefMember( model.b )

	class EditorB (Editor):
		model = KExternalReference( ModelB )
		c = RefMember( model.c )



	print 'p'
	p = Model()
	print 'q'
	q = ModelB()

	print 'Editor.a._target', Editor.a._target
	print 'Editor.b._target', Editor.b._target

	print 'EditorB.a._target', EditorB.a._target
	print 'EditorB.b._target', EditorB.b._target
	print 'EditorB.c._target', EditorB.c._target

	pe = Editor()
	qe = EditorB()

	pe.model = p
	qe.model = q
