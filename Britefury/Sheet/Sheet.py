##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.FileIO import IOXml

from Britefury.Kernel import KMeta

from Britefury.Cell.Cell import *
from Britefury.Cell.LiteralCell import *
from Britefury.Cell.ProxyCell import ProxyCell

from Britefury.Sheet.SheetCommandTracker import SheetCommandTracker

from Britefury.Util import SignalSlot



#//////////////////////////////////////////////////////////////////////////////
#			Cell type registry
#//////////////////////////////////////////////////////////////////////////////

_cellTypeRegistry = {}


def registerCellType(valueClass, cellClass):
	"Register a cell type by value type"
	_cellTypeRegistry[valueClass] = cellClass

def getCellType(valueClass):
	"Get a cell type by name"
	return _cellTypeRegistry[valueClass]


registerCellType( bool, BoolCell )
registerCellType( int, IntCell )
registerCellType( float, FloatCell )
registerCellType( str, StringCell )






#//////////////////////////////////////////////////////////////////////////////
#				FieldInterface
#//////////////////////////////////////////////////////////////////////////////

class FieldInterface (object):
	"""Field interface class"""
	def _f_getCellFromInstance(self, instance):
		"""Get the cell corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		assert False, 'abstract'


	def _f_getValueFromInstance(self, instance):
		"""Get the value of the cell managed by @self in @instance"""
		assert False, 'abstract'

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the value of the cell managed by @self in @instance (returns a value that *MUST NOT* be modified)"""
		assert False, 'abstract'


	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the literal value of the cell managed by @self in @instance to @value"""
		assert False, 'abstract'


	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		assert False, 'abstract'




#//////////////////////////////////////////////////////////////////////////////
#				FieldBase
#//////////////////////////////////////////////////////////////////////////////

class FieldBase (KMeta.KMetaMember, FieldInterface):
	"""Field base class
	NOTE: subclasses: NEVER use self as a key for retrieving data incase the field is overridden"""
	def __init__(self, doc=''):
		KMeta.KMetaMember.__init__( self, doc )
		FieldInterface.__init__( self )
		self._cellAttrName = None


	def _f_metaMember_initMetaMember(self, cls, name):
		"""Initialise a meta-member; called by the metaclass constructor
		@cls - the class that this meta-member is part of
		@name - the attribute name of this meta-member"""
		super( FieldBase, self )._f_metaMember_initMetaMember( cls, name )
		self._cellAttrName = intern( self._p_computeCellAttrName( name ) )


	@staticmethod
	def _p_computeCellAttrName(name):
		"""Private - compute the full cell attribute name"""
		return '_cell_' + name





#//////////////////////////////////////////////////////////////////////////////
#				FieldBaseWithXml
#//////////////////////////////////////////////////////////////////////////////

class FieldBaseWithXml (FieldBase):
	"""Field base class
	Adds XML serialisation interface to @FieldBase"""
	def __init__(self, xmlName=None, doc=''):
		super( FieldBaseWithXml, self ).__init__( doc )
		self._xmlName = xmlName



	def _f_metaMember_initMetaMember(self, cls, name):
		"""Friends - initialise the field
		@name - the field name"""
		super( FieldBaseWithXml, self )._f_metaMember_initMetaMember( cls, name )
		if self._xmlName is None:
			self._xmlName = self._o_computeXmlName( name )


	def _f_readInstanceFieldXml(self, instance, instanceXmlNode):
		"""Read the cell managed by @self in @instance from an XML node"""
		pass

	def _f_writeInstanceFieldXml(self, instance, instanceXmlNode):
		"""Write the cell managed by @self in @instance to an XML node"""
		pass






#//////////////////////////////////////////////////////////////////////////////
#				Field
#//////////////////////////////////////////////////////////////////////////////

class Field (FieldBaseWithXml):
	def __init__(self, valueType, initialValue, xmlName=None, bWriteIfDefault=True, doc=''):
		"""Constructor
		@valueType - the value type
		@initialValue - the initialValue
		@xmlName - the xml node name [None]
		@doc - doc string ['']"""
		super( Field, self ).__init__( xmlName, doc )
		self._valueType = valueType
		self._initialValue = initialValue
		self._bWriteIfDefault = bWriteIfDefault
		self._cellType = getCellType( valueType )



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise
		@srcInstance - source instance to copy values from"""
		super( Field, self )._f_metaMember_initInstance( instance, srcInstance )
		if srcInstance is None:
			cell = self._cellType( self._initialValue )
		else:
			cell = copy( self._f_getCellFromInstance( srcInstance ) )
		setattr( instance, self._cellAttrName, cell )
		instance._f_attachCellToScope( self._name, cell )
		cell.owner = instance


	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the cell value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the cell declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		self._f_setLiteralValueInInstance( obj, value )

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete cell value'


	def _f_getCellFromInstance(self, instance):
		"""Get the cell corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		return getattr( instance, self._cellAttrName )



	def _f_getValueFromInstance(self, instance):
		"""Get the value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()


	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the value of the cell managed by @self in @instance"""
		getattr( instance, self._cellAttrName ).setLiteralValue( value )



	def _f_readInstanceFieldXml(self, instance, instanceXmlNode):
		"""Read the cell managed by @self in @instance from an XML node"""
		super( Field, self )._f_readInstanceFieldXml( instance, instanceXmlNode )
		instanceXmlNode.getChild( self._xmlName )  >>  self._f_getCellFromInstance( instance )

	def _f_writeInstanceFieldXml(self, instance, instanceXmlNode):
		"""Write the cell managed by @self in @instance to an XML node"""
		super( Field, self )._f_writeInstanceFieldXml( instance, instanceXmlNode )
		cell = self._f_getCellFromInstance( instance )
		if self._bWriteIfDefault  or  cell.getEvaluator()  !=  self._initialValue:
			instanceXmlNode.addChild( self._xmlName )  <<  cell




	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		cell = self._f_getCellFromInstance( instance )
		srcCell = self._f_getCellFromInstance( srcInstance )
		cell.copyFrom( srcCell )








#//////////////////////////////////////////////////////////////////////////////
#				Field
#//////////////////////////////////////////////////////////////////////////////

class CompositeField (FieldBaseWithXml):
	def __init__(self, valueType, xmlName=None, doc=''):
		"""Constructor
		@valueType - the value type
		@xmlName - the xml node name [None]
		@doc - doc string ['']"""
		super( CompositeField, self ).__init__( xmlName, doc )
		self._valueType = valueType
		self._cellType = getCellType( valueType )



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise
		@srcInstance - source instance to copy values from"""
		super( CompositeField, self )._f_metaMember_initInstance( instance, srcInstance )
		if srcInstance is None:
			cell = self._cellType()
		else:
			cell = copy( self._f_getCellFromInstance( srcInstance ) )
		setattr( instance, self._cellAttrName, cell )
		instance._f_attachCellToScope( self._name, cell )
		cell.owner = instance


	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the cell value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the cell declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		self._f_setLiteralValueInInstance( obj, value )

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete cell value'


	def _f_getCellFromInstance(self, instance):
		"""Get the cell corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		return getattr( instance, self._cellAttrName )



	def _f_getValueFromInstance(self, instance):
		"""Get the value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()


	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the value of the cell managed by @self in @instance"""
		getattr( instance, self._cellAttrName ).setLiteralValue( value )



	def _f_readInstanceFieldXml(self, instance, instanceXmlNode):
		"""Read the cell managed by @self in @instance from an XML node"""
		super( CompositeField, self )._f_readInstanceFieldXml( instance, instanceXmlNode )
		instanceXmlNode.getChild( self._xmlName )  >>  self._f_getCellFromInstance( instance )

	def _f_writeInstanceFieldXml(self, instance, instanceXmlNode):
		"""Write the cell managed by @self in @instance to an XML node"""
		super( CompositeField, self )._f_writeInstanceFieldXml( instance, instanceXmlNode )
		instanceXmlNode.addChild( self._xmlName )  <<  self._f_getCellFromInstance( instance )




	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		cell = self._f_getCellFromInstance( instance )
		srcCell = self._f_getCellFromInstance( srcInstance )
		cell.copyFrom( srcCell )








#//////////////////////////////////////////////////////////////////////////////
#				SheetRefField
#//////////////////////////////////////////////////////////////////////////////

class SheetRefField (FieldBaseWithXml):
	"""Sheet-reference field"""
	class _SheetMemberRef (FieldInterface):
		"""Sheet-reference field member"""
		def __init__(self, ownerField, targetField):
			super( SheetRefField._SheetMemberRef, self ).__init__()

			if not isinstance( targetField, FieldInterface ):
				raise TypeError, 'targetField must reference a FieldInterface'

			# _ownerField is the field that manages the reference to the external sheet
			self._ownerField = ownerField
			# _targetField is the field that manages the target cell _within_ the external sheet
			self._targetField = targetField
			self._targetCellRefCellAttrName = None
			self._proxyCellAttrName = None


		def _f_getDependency(self):
			return self._ownerField


		def _f_initMetaMember(self):
			"""Initialise the meta-member"""
			self._targetCellRefCellAttrName = intern( '_ref_' + self._ownerField._name + '.' + self._targetField._name )
			self._proxyCellAttrName = intern( '_proxy_' + self._ownerField._name + '.' + self._targetField._name )


		def _p_initInstance(self, instance):
			"""Initialise instance
			@instance - instance to initialise"""


			# Get the external sheet reference cell
			sheetRefCell = getattr( instance.cells, self._ownerField._name )


			# Create the reference cell; this cell returns a reference to the target cell
			# define a function that gets the target cell
			def refCellFunction():
				sheet = sheetRefCell.getValue()
				if sheet is None:
					return None
				else:
					return getattr( sheet.cells, self._targetField._name )

			targetCellRefCell = CellRefCell()
			targetCellRefCell.setFunction( refCellFunction )
			targetCellRefCell.owner = instance
			setattr( instance, self._targetCellRefCellAttrName, targetCellRefCell )


			# Create the proxy cell
			proxy = ProxyCell( targetCellRefCell )
			proxy.owner = instance
			setattr( instance, self._proxyCellAttrName, proxy )



		def _f_getCellFromInstance(self, instance):
			"""Get the cell corresponding to @self from @instance
			@instance - an instance of @Sheet"""
			if not hasattr( instance, self._proxyCellAttrName ):
				self._p_initInstance( instance )
			return getattr( instance, self._proxyCellAttrName )


		def _f_getValueFromInstance(self, instance):
			"""Get the value of the field managed by @self in @instance"""
			if not hasattr( instance, self._proxyCellAttrName ):
				self._p_initInstance( instance )
			return getattr( instance, self._proxyCellAttrName ).getValue()


		def _f_getImmutableValueFromInstance(self, instance):
			"""Get the immutable value of the cell managed by @self in @instance"""
			if not hasattr( instance, self._proxyCellAttrName ):
				self._p_initInstance( instance )
			return getattr( instance, self._cellAttrName ).getImmutableValue()


		def _f_setLiteralValueInInstance(self, instance, value):
			"""Set the value of the field managed by @self in @instance"""
			if not hasattr( instance, self._proxyCellAttrName ):
				self._p_initInstance( instance )
			getattr( instance, self._proxyCellAttrName ).setLiteralValue( value )



		def _f_copyFrom(self, instance, srcInstance):
			"""Copy data from @srcInstance, into @instance"""
			pass



	def __init__(self, targetSheetClass, xmlName=None, doc=''):
		"""Constructor
		@targetSheetClass - the target sheet class
		@xmlName - the xml node name [None]
		@doc - doc string ['']
		@traits - field traits [None]"""
		super( SheetRefField, self ).__init__( xmlName, doc )
		if not ( isinstance( targetSheetClass, SheetClass )  and  issubclass( targetSheetClass, Sheet ) ):
			raise TypeError, 'targetSheetClass must be an instance of SheetClass and a subclass of Sheet'
		self._targetSheetClass = targetSheetClass
		self._targetFieldNameToRefTable = {}
		self._bInitialised = False



	def _f_metaMember_initMetaMember(self, cls, name):
		super( SheetRefField, self )._f_metaMember_initMetaMember( cls, name )
		self._bInitialised = True
		for ref in self._targetFieldNameToRefTable.values():
			ref._f_initMetaMember()


	def _f_metaMember_overload(self, superMetaMember, superClass):
		"""Inform a meta-member that it is overloading an existing meta-member
		@superMetaMember - the meta-member being overloaded
		@superClass - the base class"""
		if not issubclass( self._targetSheetClass, superMetaMember._targetSheetClass ):
			raise ValueError, 'cannot overload sheet-reference field %s.%s; the target sheet class (%s) must be a subclass of the base field target sheet class (%s)' % ( self._cls.__name__, self._name, self._targetSheetClass.__name__, superMetaMember._targetSheetClass.__name__ )


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise an instance of the class of which this meta-member is part
		@instance - the instance to initialise
		@srcMember - instance of this meta-member, part of another object, from which to copy data"""
		super( SheetRefField, self )._f_metaMember_initInstance( instance, srcInstance )

		cell = SheetRefCell( self._targetSheetClass )
		if srcInstance is not None:
			cell.setLiteralValue( self._f_getValueFromInstance( srcInstance ) )
		setattr( instance, self._cellAttrName, cell )
		instance._f_attachCellToScope( self._name, cell )
		cell.owner = instance




	def __getattr__(self, name):
		"""Attribute accessor - Create a field wrapper for a specified field from the target sheet class"""
		try:
			targetField = getattr( self._targetSheetClass, name )
		except AttributeError:
			raise AttributeError, 'Sheet class \'%s\' has no attribute named \'%s\'' % ( self._targetSheetClass.__name__, name )
		else:
			try:
				memberRef = self._targetFieldNameToRefTable[name]
			except KeyError:
				if isinstance( targetField, FieldInterface ):
					memberRef = SheetRefField._SheetMemberRef( self, targetField )
					if self._bInitialised:
						memberRef._f_initMetaMember()
				else:
					raise TypeError, 'Attempting to access member of class \'%s\'; it is not an instance of \'FieldInterface\''  %  ( self._targetSheetClass.__name__, )
				self._targetFieldNameToRefTable[name] = memberRef
			return memberRef



	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the field value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the proxy declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		self._f_setLiteralValueInInstance( obj, value )

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete field value'





	def _f_getCellFromInstance(self, instance):
		"""Get the variable interface corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		return getattr( instance, self._cellAttrName )


	def _f_getValueFromInstance(self, instance):
		"""Get the value of the field managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()

	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the value of the field managed by @self in @instance"""
		getattr( instance, self._cellAttrName ).setLiteralValue( value )



	def _f_readInstanceFieldXml(self, instance, instanceXmlNode):
		"""Read the field managed by @self in @instance from an XML node"""
		super( SheetRefField, self )._f_readInstanceFieldXml( instance, instanceXmlNode )
		instanceXmlNode.getChild( self._xmlName )  >>  self._f_getCellFromInstance( instance )

	def _f_writeInstanceFieldXml(self, instance, instanceXmlNode):
		"""Write the field managed by @self in @instance to an XML node"""
		super( SheetRefField, self )._f_writeInstanceFieldXml( instance, instanceXmlNode )
		instanceXmlNode.addChild( self._xmlName )  <<  self._f_getCellFromInstance( instance )



	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		cell = self._f_getCellFromInstance( instance )
		srcCell = self._f_getCellFromInstance( srcInstance )
		cell.copyFrom( srcCell )









#//////////////////////////////////////////////////////////////////////////////
#				FieldProxy
#//////////////////////////////////////////////////////////////////////////////

class FieldProxy (FieldBase):
	"""Field proxy declaration"""
	def __init__(self, memberRef):
		"""Constructor
		@memberRef - Sheet ref field member"""
		super( FieldProxy, self ).__init__( '[PROXY] ' + memberRef._targetField.__doc__ )

		if not isinstance( memberRef, SheetRefField._SheetMemberRef ):
			raise TypeError, '@memberRef must be a SheetRefField member reference (a SheetRefField._SheetMemberRef)'

		self._o_addDependency( memberRef._ownerField )

		self._memberRef = memberRef


	def _f_metaMember_initClass(self):
		super( FieldProxy, self )._f_metaMember_initClass()

		if not issubclass( self._cls, self._memberRef._ownerField._cls ):
			raise ValueError, 'field proxy \'%s.%s\' cannot target a field from a class that is not a superclass of %s, it tried to target a field from %s' %	\
							( cls.__name__, name, cls.__name__, self._memberRef._ownerField._cls.__name__ )


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise"""
		proxy = self._memberRef._f_getCellFromInstance( instance )
		assert proxy.owner is instance, 'Proxy has incorrect owner'

		setattr( instance, self._cellAttrName, proxy )
		instance._f_attachCellToScope( self._name, proxy )





	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the field value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the proxy declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		self._f_setLiteralValueInInstance( obj, value )

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete field value'



	def _f_getCellFromInstance(self, instance):
		"""Get the cell proxy from a FieldProxy instance that corresponds to @self
		@instance - the instance from which to retrieve the field proxy"""
		return getattr( instance, self._cellAttrName )


	def _f_getValueFromInstance(self, instance):
		"""Get the value of the cell proxy managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()

	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the literal value of the cell proxy managed by @self in @instance"""
		getattr( instance, self._cellAttrName ).setLiteralValue( value )


	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		pass






#//////////////////////////////////////////////////////////////////////////////
#    FieldSecondOrderProxy
#//////////////////////////////////////////////////////////////////////////////

class FieldSecondOrderProxy (FieldBase):
	"""Field proxy declaration"""
	def __init__(self, targetField):
		"""Constructor
		@memberRef - Sheet ref field member"""
		super( FieldSecondOrderProxy, self ).__init__( '[2ND ORDER PROXY] ' + targetField.__doc__ )

		self._o_addDependency( targetField )

		self._targetField = targetField


	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise"""
		super( FieldSecondOrderProxy, self )._f_metaMember_initInstance( instance, srcInstance )

		targetCellRefCell = self._targetField._f_getCellFromInstance( instance )

		# Create the proxy cell
		proxy = ProxyCell( targetCellRefCell )
		setattr( instance, self._cellAttrName, proxy )
		instance._f_attachCellToScope( self._name, proxy )
		proxy.owner = instance



	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the field value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the proxy declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		self._f_setLiteralValueInInstance( obj, value )

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete field value'



	def _f_getCellFromInstance(self, instance):
		"""Get the cell proxy from a FieldSecondOrderProxy instance that corresponds to @self
		@instance - the instance from which to retrieve the field proxy"""
		return getattr( instance, self._cellAttrName )


	def _f_getValueFromInstance(self, instance):
		"""Get the value of the cell proxy managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()

	def _f_setLiteralValueInInstance(self, instance, value):
		"""Set the literal value of the cell proxy managed by @self in @instance"""
		getattr( instance, self._cellAttrName ).setLiteralValue( value )


	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		pass






#//////////////////////////////////////////////////////////////////////////////
#				FunctionField
#//////////////////////////////////////////////////////////////////////////////

class FunctionField (FieldBase):
	"""Function field"""
	def __init__(self, function, doc=''):
		"""Constructor
		@function - the function used to compute the value
		@doc - doc string ['']"""
		super( FunctionField, self ).__init__( doc )
		self._function = function



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise
		@srcInstance - source instance to copy values from"""
		super( FunctionField, self )._f_metaMember_initInstance( instance, srcInstance )
		cell = Cell()
		def _f():
			return self._function( instance )
		cell.setFunction( _f )
		setattr( instance, self._cellAttrName, cell )
		instance._f_attachCellToScope( self._name, cell )
		cell.owner = instance


	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the field value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the field declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		raise TypeError, 'cannot set function field value'

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete field value'


	def _f_getCellFromInstance(self, instance):
		"""Get the cell corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		return getattr( instance, self._cellAttrName )



	def _f_getValueFromInstance(self, instance):
		"""Get the value of the field managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()


	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		pass






#//////////////////////////////////////////////////////////////////////////////
#				FunctionRefField
#//////////////////////////////////////////////////////////////////////////////

class FunctionRefField (FieldBase):
	"""Function reference field"""
	def __init__(self, function, doc=''):
		"""Constructor
		@function - the function used to compute the value
		@doc - doc FunctionRefField ['']"""
		super( FunctionRefField, self ).__init__( doc )
		self._function = function



	def _f_metaMember_initInstance(self, instance, srcInstance=None):
		"""Initialise instance
		@instance - instance to initialise
		@srcInstance - source instance to copy values from"""
		super( FunctionRefField, self )._f_metaMember_initInstance( instance, srcInstance )
		cell = RefCell()
		def _f():
			return self._function( instance )
		cell.setFunction( _f )
		setattr( instance, self._cellAttrName, cell )
		instance._f_attachCellToScope( self._name, cell )
		cell.owner = instance


	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		if obj is not None:
			# Return the field value
			return self._f_getValueFromInstance( obj )
		else:
			# Return the field declaration
			return self

	def __set__(self, obj, value):
		"""Descriptor - set"""
		raise TypeError, 'cannot set function field value'

	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise TypeError, 'cannot delete field value'


	def _f_getCellFromInstance(self, instance):
		"""Get the cell corresponding to @self from @instance
		@instance - an instance of @Sheet"""
		return getattr( instance, self._cellAttrName )



	def _f_getValueFromInstance(self, instance):
		"""Get the value of the field managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getValue()

	def _f_getImmutableValueFromInstance(self, instance):
		"""Get the immutable value of the cell managed by @self in @instance"""
		return getattr( instance, self._cellAttrName ).getImmutableValue()


	def _f_copyFrom(self, instance, srcInstance):
		"""Copy data from @srcInstance, into @instance"""
		pass






#//////////////////////////////////////////////////////////////////////////////
#			_FieldCellAccessor
#//////////////////////////////////////////////////////////////////////////////

class _FieldCellAccessor (object):
	__slots__ = [ '_sheetClass', '_sheetInstance' ]

	"""_FieldCellAccessor - helper class"""
	def __init__(self, sheetClass, sheetInstance):
		"""Constructor
		@sheetClass - a subclass of Sheet
		@sheetInstance - the instance"""
		super( _FieldCellAccessor, self ).__init__()
		self._sheetClass = sheetClass
		self._sheetInstance = sheetInstance


	def __getattr__(self, attrName):
		try:
			field = self._sheetClass._Sheet_fields[attrName]
		except KeyError:
			raise AttributeError, 'no cell named %s in class %d'  %  ( attrName, self._sheetClass )

		if self._sheetInstance is None:
			return field
		else:
			return field._f_getCellFromInstance( self._sheetInstance )


	def __setattr__(self, attrName, value):
		if attrName == '_sheetClass'  or  attrName == '_sheetInstance':
			object.__setattr__( self, attrName, value )
		else:
			raise AttributeError, 'cells cannot be set'


	def __delattr__(self, attrName):
		raise AttributeError, 'cells cannot be deleted'





#//////////////////////////////////////////////////////////////////////////////
#   _FieldCellAccessorDescriptor
#//////////////////////////////////////////////////////////////////////////////

class _FieldCellAccessorDescriptor (object):
	"""Cells can be accessed from here"""
	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		# Return a _FieldCellAccessor
		return _FieldCellAccessor( objtype, obj )


	def __set__(self, obj, value):
		"""Descriptor - set"""
		raise AttributeError, 'cannot set cells'


	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise AttributeError, 'cannot delete cells'






#//////////////////////////////////////////////////////////////////////////////
#		_FieldImmutableValueAccessor
#//////////////////////////////////////////////////////////////////////////////

class _FieldImmutableValueAccessor (object):
	__slots__ = [ '_sheetClass', '_sheetInstance' ]

	"""_FieldCellAccessor - helper class"""
	def __init__(self, sheetClass, sheetInstance):
		"""Constructor
		@sheetClass - a subclass of Sheet
		@sheetInstance - the instance"""
		super( _FieldImmutableValueAccessor, self ).__init__()
		self._sheetClass = sheetClass
		self._sheetInstance = sheetInstance


	def __getattr__(self, attrName):
		try:
			field = self._sheetClass._Sheet_fields[attrName]
		except KeyError:
			raise AttributeError, 'no cell named %s in class %d'  %  ( attrName, self._sheetClass )

		if self._sheetInstance is None:
			return field
		else:
			return field._f_getCellFromInstance( self._sheetInstance ).getImmutableValue()


	def __setattr__(self, attrName, value):
		if attrName == '_sheetClass'  or  attrName == '_sheetInstance':
			object.__setattr__( self, attrName, value )
		else:
			raise AttributeError, 'cell immutable values cannot be set'


	def __delattr__(self, attrName):
		raise AttributeError, 'cell  cannot be deleted'





#//////////////////////////////////////////////////////////////////////////////
#   _FieldImmutableValueAccessorDescriptor
#//////////////////////////////////////////////////////////////////////////////

class _FieldImmutableValueAccessorDescriptor (object):
	"""Cells can be accessed from here"""
	def __get__(self, obj, objtype = None):
		"""Descriptor - get"""
		# Return a _FieldCellAccessor
		return _FieldImmutableValueAccessor( objtype, obj )


	def __set__(self, obj, value):
		"""Descriptor - set"""
		raise AttributeError, 'cannot set cells'


	def __delete__(self, obj):
		"""Descriptor - delete"""
		raise AttributeError, 'cannot delete cells'






#//////////////////////////////////////////////////////////////////////////////
#			SheetClass
#//////////////////////////////////////////////////////////////////////////////

class SheetClass (KMeta.KClass):
	"""Cell container metaclass"""
	def __init__(cls, clsName, clsBases, clsDict):
		"""Constructor
		@clsName - class name
		@bases - base classes
		@clsDict - class dict"""
		super( SheetClass, cls ).__init__( clsName, clsBases, clsDict )

		cls._Sheet_fields = cls._o_gatherDictFromBases( clsBases, '_Sheet_fields' )


		# Process cell columns
		# Gather a list of cell columns, and a list of composite columns
		for name, value in clsDict.items():
			if isinstance( value, FieldBase ):
				# Register the column
				cls._Sheet_fields[name] = value



		# Register the class with the XML IO system
		xmlName = clsDict.get( '__iotypename__', clsName )
		IOXml.ioObjectFactoryRegister( xmlName, cls )



	def getFieldByName(cls, name):
		"""Get a column by name
		@name - the name of the column to retrieve"""
		return cls._Sheet_fields[name]








#//////////////////////////////////////////////////////////////////////////////
#   			Sheet
#//////////////////////////////////////////////////////////////////////////////

class Sheet (KMeta.KObject, CellOwner):
	__metaclass__ = SheetClass
	trackerClass = SheetCommandTracker

	cellModifiedSignal = SignalSlot.ClassSignal()

	"""Cell container base class"""
	def __init__(self, src = None):
		"""Constructor"""
		self._commandTracker_ = None
		super( Sheet, self ).__init__( src )

		self._ownerCompositeCell = None



	def _f_onCellEvaluator(self, cell, oldEval, newEval):
		if self._ownerCompositeCell is not None:
			self._ownerCompositeCell._f_onSheetModify( self )

		if self._commandTracker_ is not None:
			self._commandTracker_._f_onSheetCellEvaluator( self, cell, oldEval, newEval )

		self._f_onChildCellModified()
		self._f_onCellModified()
		self.cellModifiedSignal.emit( self )



	def _f_onDelegateCellEvaluator(self, cell, oldEval, newEval):
		if self._ownerCompositeCell is not None:
			self._ownerCompositeCell._f_onSheetModify( self )

		self._f_onCellModified()
		self.cellModifiedSignal.emit( self )




	def _f_onChildCellModified(self):
		pass


	def _f_onCellModified(self):
		pass




	def getCellByName(self, name):
		"""Get a cell by name
		@cellName - the name of the cell to get"""
		field = getattr( type( self ), name )
		assert isinstance( field, FieldInterface )
		return field._f_getCellFromInstance( self )


	def _o_getVersion(self):
		try:
			currentVersion = self.__version__
		except AttributeError:
			return None
		else:
			return currentVersion




	def __readxml__(self, xmlNode):
		"""XML IO read helper"""
		for column in self._Sheet_fields.values():
			if isinstance( column, FieldBaseWithXml ):
				column._f_readInstanceFieldXml( self, xmlNode )
		# Set the version if the version property exists, OR the object already has a version
		version = xmlNode.getVersionProperty()
		self.__version__ = version


	def __writexml__(self, xmlNode):
		"""XML IO write helper"""
		for column in self._Sheet_fields.values():
			if isinstance( column, FieldBaseWithXml ):
				column._f_writeInstanceFieldXml( self, xmlNode )
		try:
			currentVersion = self.__version__
		except AttributeError:
			pass
		else:
			if currentVersion is not None:
				xmlNode.setVersionProperty( currentVersion )



#	def __copy__(self):
#		"""Copy helper"""
#		return self.__class__( src=self )

	def __copy__(self):
		"""Copy helper"""
		return self


	def copyFrom(self, sheet):
		for field in self._Sheet_fields.values():
			if isinstance( field, FieldBase ):
				field._f_copyFrom( self, sheet )



	def _f_getCompositeCells(self):
		"""Get a list of the composite cells in this sheet (used by the command tracker)"""
		return [ field._f_getCellFromInstance( self )   for field in self._Sheet_fields.values()   if isinstance( field, CompositeField ) ]


	def _f_attachCellToScope(self, name, cell):
		self.cellScope[name] = cell


	cells = _FieldCellAccessorDescriptor()
	immutable = _FieldImmutableValueAccessorDescriptor()








#//////////////////////////////////////////////////////////////////////////////
#				SheetRefCell
#//////////////////////////////////////////////////////////////////////////////

class SheetRefCell (RefCell):
	__slots__ = [ '_sheetClass' ]

	valueClass = Sheet
	bAllowNone = True


	def __init__(self, sheetClass):
		self._sheetClass = sheetClass

		super( SheetRefCell, self ).__init__( None )



	def getValue(self):
		return self.getImmutableValue()



	def _p_checkValueType(self, value):
		if self.valueClass is not None:
			return isinstance( value, self._sheetClass )  or  value is None
		else:
			return False












class CompositeCellClass (type):
	def __init__(cls, clsName, clsBases, clsDict):
		super( CompositeCellClass, cls ).__init__( clsName, clsBases, clsDict )

		# Ensure that @valueType is present
		if not clsDict.has_key( 'valueClass' ):
			raise AttributeError, 'valueClass not specified'
		else:
			valueClass = clsDict['valueClass']
			if valueClass is not None  and  issubclass( valueClass, CellEvaluator ):
				raise TypeError, 'valueClass of literal cell cannot be a subclass of CellEvaluator; it is \'%s\''  %  ( clsDict['valueClass'], )

		ioObjectFactoryRegister( clsName, cls )



class CompositeCell (CellInterface):
	__slots__ = [ '_value', 'owner' ]

	__metaclass__ = CompositeCellClass

	__ioxml_can_delegate__ = True


	valueClass = Sheet


	def __init__(self):
		super( CompositeCell, self ).__init__()

		self._value = self.valueClass()
		self._value._ownerCompositeCell = self

		self.owner = None



	def getEvaluator(self):
		return self._value

	def setEvaluator(self, evaluator):
		if not isinstance( evaluator, self.valueClass ):
			raise TypeError, 'evaluator (literal value) should be an instance of the value class; value is \'%s\', value class is \'%s\''  %  ( evaluator, self.valueClass )
		self._p_setValue( evaluator )


	def getLiteralValue(self):
		return self._value

	def setLiteralValue(self, literal):
		if not isinstance( literal, self.valueClass ):
			raise TypeError, 'evaluator (literal value) should be an instance of the value class; value is \'%s\', value class is \'%s\''  %  ( evaluator, self.valueClass )
		self._p_setValue( evaluator )

	def isLiteral(self):
		return True


	def getValue(self):
		self._bRefreshRequired = False

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = 0

		return self._value

	def getImmutableValue(self):
		self._bRefreshRequired = False

		# Add @self to the global dependency list if it exists; this ensures that any cell that
		# is recomputing its value will know that the @value of self is required
		if CellInterface._cellAccessList is not None:
			CellInterface._cellAccessList[self] = 0

		return self._value



	def isValid(self):
		return True



	def _p_setValue(self, value):
		self._value.copyFrom( value )
		self.evaluatorSignal.emit( self._value, self._value )
		if self.owner is not None:
			self.owner._f_onDelegateCellEvaluator( self, oldEval, self._value )
		self._o_changed()




	def _f_onSheetModify(self, sheet):
		assert sheet is self._value
		self.evaluatorSignal.emit( self._value, self._value )
		if self.owner is not None:
			self.owner._f_onDelegateCellEvaluator( self, self._value, self._value )
		self._o_changed()



	def __readxml__(self, xmlNode):
		if xmlNode.isValid():
			xmlNode  >>  self._value



	def __writexml__(self, xmlNode):
		if xmlNode.isValid():
			xmlNode  <<  self._value



	def __copy__(self):
		cell = self.__class__()
		cell._p_setValue( self._value )
		return cell


	def copyFrom(self, cell):
		self._p_setValue( cell._value )









if __name__ == '__main__':
	import unittest
	from Britefury.CommandHistory.CommandHistory import CommandHistory


	class SheetA (Sheet):
		a = Field( int, 0 )
		b = Field( float, 3.14 )
		c = Field( bool, False )

	class SheetA2 (SheetA):
		d = Field( int, 10 )


	class SheetB (Sheet):
		s = SheetRefField( SheetA )
		sa = FieldProxy( s.a )

	class SheetB2 (SheetB):
		s = SheetRefField( SheetA2 )
		sd = FieldProxy( s.d )


	class SheetC (Sheet):
		s1 = SheetRefField( SheetA )
		s2 = SheetRefField( SheetA )
		s3 = SheetRefField( SheetB )
		s1a = FieldProxy( s1.a )
		s2a = FieldProxy( s2.a )
		s3a = FieldProxy( s3.sa )




	class CellTest (unittest.TestCase):
		def testConstructionDescruction(self):
			s = SheetA()
			del s


		def testValues(self):
			s = SheetA()
			self.assert_( s.a == 0 )
			self.assert_( s.b == 3.14 )
			self.assert_( s.c == False )


		def testCells(self):
			s = SheetA()
			self.assert_( s.cells.a.getValue() == 0 )
			self.assert_( s.cells.b.getValue() == 3.14 )
			self.assert_( s.cells.c.getValue() == False )


		def testSet(self):
			s = SheetA()
			s.a = 2
			s.b = 1.414
			s.c = True
			self.assert_( s.a == 2 )
			self.assert_( s.b == 1.414 )
			self.assert_( s.c == True )


		def testRef(self):
			a1 = SheetA()
			a2 = SheetA()

			def f():
				return b.sa * 4

			cellX = Cell()
			cellX.setFunction( f )

			a2.a = 3

			b = SheetB()
			b.s = a1

			self.assert_( b.sa == 0 )
			self.assert_( cellX.getValue() == 0 )

			b.s = a2

			self.assert_( b.sa == 3 )
			self.assert_( cellX.getValue() == 12 )


		def testInheritedRef(self):
			a1 = SheetA2()
			a2 = SheetA2()

			def f():
				return b.sa * 4

			cellX = Cell()
			cellX.setFunction( f )

			a2.a = 3
			a2.d = 23

			b = SheetB2()
			b.s = a1

			self.assert_( b.sd == 10 )
			self.assert_( b.sa == 0 )
			self.assert_( cellX.getValue() == 0 )

			b.s = a2

			self.assert_( b.sd == 23 )
			self.assert_( b.sa == 3 )
			self.assert_( cellX.getValue() == 12 )


		def testXml(self):
			a1 = SheetA()
			a2 = SheetA()
			a2.a = 3
			a2.b = 1.414
			a2.c = True

			b = SheetB()
			b.s = a2

			c = SheetC()
			c.s1 = a1
			c.s2 = a2
			c.s3 = b

			docOut = OutputXmlDocument()
			docOut.getContentNode().writeObject( c )
			xml = docOut.writeString()

			docIn = InputXmlDocument()
			docIn.parse( xml )

			cIn = docIn.getContentNode().readObject()


			self.assert_( c.s1.a == 0 )
			self.assert_( c.s1.b == 3.14 )
			self.assert_( c.s1.c == False )
			self.assert_( c.s2.a == 3 )
			self.assert_( c.s2.b == 1.414 )
			self.assert_( c.s2.c == True )
			self.assert_( c.s3.s.b == 1.414 )

			self.assert_( cIn.s1.a == 0 )
			self.assert_( cIn.s1.b == 3.14 )
			self.assert_( cIn.s1.c == False )
			self.assert_( cIn.s2.a == 3 )
			self.assert_( cIn.s2.b == 1.414 )
			self.assert_( cIn.s2.c == True )
			self.assert_( cIn.s3.s.b == 1.414 )


		def testUndo(self):
			ch = CommandHistory()

			# Create the sheets
			a1 = SheetA()
			a2 = SheetA()
			b = SheetB()

			# Setup a2
			a2.a = 5
			a2.b = 6.0

			# Track them
			ch.track( a1 )
			ch.track( a2 )
			ch.track( b )

			# Change values
			# command 0
			a1.a = 3
			a1.b = 1.414
			a1.c = True
			# command 1
			b.s = a1

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == True )
			self.assert_( b.s is a1 )
			self.assert_( b.sa == 3 )

			# Undo command 1
			ch.undo()

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == True )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )

			# Undo command 0
			ch.undo()

			# Check
			self.assert_( a1.a == 0 )
			self.assert_( a1.b == 3.14 )
			self.assert_( a1.c == False )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )

			# command 0
			a1.a = 3
			ch.finishCommand()
			# command 1
			a1.b = 1.414
			ch.finishCommand()
			# command 2
			a1.c = True
			ch.finishCommand()
			# command 3
			b.s = a1
			ch.finishCommand()
			# command 4
			b.s = a2

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == True )
			self.assert_( b.s is a2 )
			self.assert_( b.sa == 5 )

			# Undo command 4
			ch.undo()

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == True )
			self.assert_( b.s is a1 )
			self.assert_( b.sa == 3 )

			# Undo command 3
			ch.undo()

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == True )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )

			# Undo command 2
			ch.undo()

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 1.414 )
			self.assert_( a1.c == False )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )

			# Undo command 1
			ch.undo()

			# Check
			self.assert_( a1.a == 3 )
			self.assert_( a1.b == 3.14 )
			self.assert_( a1.c == False )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )

			# Undo command 0
			ch.undo()

			# Check
			self.assert_( a1.a == 0 )
			self.assert_( a1.b == 3.14 )
			self.assert_( a1.c == False )
			self.assert_( b.s is None )
			self.assert_( b.sa == None )







	unittest.main()


