##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import xml.sax
import xml.sax.saxutils
import xml.parsers.expat

import cStringIO


"""
XML based serialisation system

Primitive types have XML serialisation handers set up for them by IOXml.

Quick read and write
ioReadObjectFromString(s)  ->  object    # read an object from a string
ioReadObjectFromFile(f)      ->  object    # read an object from a file
ioWriteObjectAsString(obj)  ->  string    # write an object to a string
ioWriteObjectToFile(f, obj)  ->  None     # write an object to a file


To handle more complex documents:
input:
  create a document:
    document = InputXmlDocument()
  read the data from a string:
    document.parse(s)
  OR read the data from a file:
    document.parseFile( f )
  get the document content node:
    node = document.getContentNode()
  -- handle the XML node tree

output:
  create a document:
    document = OutputXmlDocument()
  get the document content node:
    node = document.getContentNode()
  -- manipulate the XML node tree
  to write to a file:
    document.writeFile( f )                  # write to a file f
  OR to write to a string:
    document.writeString() -> string    # write to a string




Classes can define the following:

__readxml__ and __writexml__
	read from and write to XML node content

__ioxml_read_all_types__ = True
	will skip type checking, reference linking is still done

__ioxml_can_delegate__ = True
	node >> obj will result in the following:
	 	if the XML node type matches the object type:
			 __readxml__ is called as normal to read the node
		else:
			type checking and reference linking is skipped and __readxml__ is called directly; this allows a class to handle all types
			
			
call ioObjectFactoryRegister(name, cls)   for each class that is to be XML serialisable
"""




def ioXmlStringToBoolHelper(string):
	"Helper function to convert a string into a bool"
	if string == 'True':
		return True
	else:
		return False




#//////////////////////////////////////////////////////////////////////////////
#			IOObjectFactory
#//////////////////////////////////////////////////////////////////////////////

g_IOObjectFactoryRegistryByName = {}
g_IOObjectFactoryRegistryByClass = {}
g_IOPrimitiveTypeNames = set()
g_IOPrimitiveTypeClasses = set()
g_IOPrimitiveReadersAndWritersByClass = {}

def ioObjectFactoryRegister(name, objectClass):
	"Register an object factory for use in the XML IO system"
	g_IOObjectFactoryRegistryByName[name] = objectClass
	g_IOObjectFactoryRegistryByClass[objectClass] = name

def ioObjectFactoryRegisterPrimitive(name, objectClass):
	"Register a primitive type for use in the XML IO system"
	g_IOObjectFactoryRegistryByName[name] = objectClass
	g_IOObjectFactoryRegistryByClass[objectClass] = name
	g_IOPrimitiveTypeNames.add( name )
	g_IOPrimitiveTypeClasses.add( objectClass )

def ioObjectFactoryRegisterPrimitiveWithReaderAndWriter(name, objectClass, reader, writer):
	"Register a primitive type for use in the XML IO system"
	g_IOObjectFactoryRegistryByName[name] = objectClass
	g_IOObjectFactoryRegistryByClass[objectClass] = name
	g_IOPrimitiveTypeNames.add( name )
	g_IOPrimitiveTypeClasses.add( objectClass )
	g_IOPrimitiveReadersAndWritersByClass[objectClass] = reader, writer

def ioObjectFactoryCreate(name):
	"Create an object by type name"
	factory = g_IOObjectFactoryRegistryByName.get( name )
	if factory is not None:
		return factory()
	else:
		return None

def ioObjectFactoryGetName(objectClass):
	"Get the registered name of a class"
	return g_IOObjectFactoryRegistryByClass[objectClass]

def ioObjectFactoryNamedClassIsSubclass(name, objectClass):
	"Determine if the class specified by @name is a subclass of @objectClass"
	namedClass = g_IOObjectFactoryRegistryByName.get( name )
	if namedClass is not None:
		return issubclass( namedClass, objectClass )
	else:
		return False







#//////////////////////////////////////////////////////////////////////////////
#				IOXmlNode
#//////////////////////////////////////////////////////////////////////////////

class IOXmlNode:
	"XML IO node"
	__slots__ = [ '_document', 'name', 'parent', 'children', 'nextSibling', 'prevSibling', '_properties' ]
	def __init__(self, document, name, parent, properties):
		"""Constructor:
		@name - node name
		@parent - parent node
		@properties - property map"""
		self.name = name
		self.parent = parent
		self.indexInParent = None
		self.children = []
		self.nextSibling = None
		self.prevSibling = None
		self._properties = properties
		self._document = document
		self.content = ''

		self._p_appendedToParent( parent )


	def isValid(self):
		"Determine if the node is valid"
		return self.name is not None  and  self._properties is not None


	def _p_appendedToParent(self, parent):
		if parent is not None:
			if len( parent.children ) > 0:
				self.prevSibling = parent.children[-1]
				self.prevSibling.nextSibling = self
			self.indexInParent = len( parent.children )
			parent.children.append( self )





#//////////////////////////////////////////////////////////////////////////////
#		InputXmlNodeProperty
#//////////////////////////////////////////////////////////////////////////////

class InputXmlNodeProperty:
	"Input XML node property"
	def __init__(self, value):
		"""Constructor:
		@value - property value"""
		self.value = value



def ioXmlReadPropHelper(prop, reader, default):
	"Helper function to read properties"
	if isinstance( prop.value, unicode ):
		try:
			return reader( prop.value )
		except ValueError:
			return default
	else:
		return default


def ioXmlReadBoolProp(prop, default=False):
	"Read a boolean property"
	return ioXmlReadPropHelper( prop, ioXmlStringToBoolHelper, default )

def ioXmlReadIntProp(prop, default=0):
	"Read an integer property"
	return ioXmlReadPropHelper( prop, int, default )

def ioXmlReadFloatProp(prop, default=0.0):
	"Read a float property"
	return ioXmlReadPropHelper( prop, float, default )

def ioXmlReadStringProp(prop, default=''):
	"Read a string property"
	return ioXmlReadPropHelper( prop, str, default )




#//////////////////////////////////////////////////////////////////////////////
#		InputXmlNode
#//////////////////////////////////////////////////////////////////////////////

class InputXmlNode (IOXmlNode):
	"Input XML node"
	def getChild(self, name):
		"find a child node by name; returns an invalid node if none found by the specified name"
		for x in self.children:
			if x.name == name:
				return x
		return InputXmlNode( None, None, None, {} )

	def property(self, name):
		"get a named property"
		try:
			return InputXmlNodeProperty( self._properties[name] )
		except KeyError:
			return InputXmlNodeProperty( None )

	def getTypeProperty(self):
		"get the property called 'type'"
		return self.property( 'type' ).value

	def getVersionProperty(self, defaultValue=None):
		"get the property called 'version'"
		try:
			return str( self._properties['version'] )
		except KeyError:
			return defaultValue

	def getVersionNumberProperty(self, defaultValue=0):
		try:
			return int( self._properties['version'] )
		except KeyError:
			return defaultValue
		except ValueError:
			return defaultValue


	def childrenNamed(self, name):
		for node in self.children:
			if node.name == name:
				yield node


	def readObject(self):
		if self.isValid():
			return self._document._p_readObject( self )

	def __rshift__(self, readable):
		assert readable is not None, 'cannot read into None'
		if self.isValid():
			self._document._p_readIntoObject( self, readable )




	def __iter__(self):
		return iter( self.children )

	def __len__(self):
		return len( self.children )


	def _p_readContentIntoObject(self, readable):
		try:
			f = readable.__readxml__
		except AttributeError:
			raise TypeError, 'object type %s does not support XML reading' % ( type( readable ).__name__ )
		f( self )



def ioXmlReadNodeHelper(node, typeName, propertyReader, default):
	"XML node reader helper function"
	if node.getTypeProperty() == typeName:
		return propertyReader( node.property( 'value' ), default )
	else:
		return default

def ioXmlReadBoolNode(node, default=False):
	"Read a boolean XML node"
	return ioXmlReadNodeHelper( node, 'bool', ioXmlReadBoolProp, default )

def ioXmlReadIntNode(node, default=0):
	"Read an integer XML node"
	return ioXmlReadNodeHelper( node, 'int', ioXmlReadIntProp, default )

def ioXmlReadFloatNode(node, default=0.0):
	"Read a float XML node"
	return ioXmlReadNodeHelper( node, 'float', ioXmlReadFloatProp, default )

def ioXmlReadStringNode(node, default=''):
	"Read a float XML node"
	return ioXmlReadNodeHelper( node, 'str', ioXmlReadStringProp, default )






#//////////////////////////////////////////////////////////////////////////////
#		InputXmlDocument
#//////////////////////////////////////////////////////////////////////////////

class InputXmlDocument (object):
	def __init__(self):
		self._parser = xml.parsers.expat.ParserCreate()
		self._documentRootNode = None
		self._contentNode = None

		self._objectIDToXmlNode = {}
		self._objectIDToObject = {}

		self._parseCurrentNode = None
		self._contentList = []

		self._parser.StartElementHandler = self._p_initialStartElement
		self._parser.EndElementHandler = self._p_endElement
		self._parser.CharacterDataHandler = self._p_characterData



	def parse(self, xmlData):
		try:
			self._parser.Parse( xmlData, 1 )
		except xml.parsers.expat.ExpatError:
			print 'Error parsing XML data'
		self._p_finalise()

	def parseFile(self, f):
		self._parser.ParseFile( f )
		self._p_finalise()


	def getContentNode(self):
		return self._contentNode


	def _p_readObject(self, objectNode):
		obj = self._p_readObjectHelper( objectNode )
		while obj is not None   and  hasattr( obj, '__ioreplacement__' ):
			obj = obj.__ioreplacement__()
		return obj

	def _p_readObjectHelper(self, objectNode):
		if objectNode.getTypeProperty() == '__ioxml_object_reference__':
			try:
				objectID = int( objectNode.property( 'ioxml_id' ).value )

				if objectID == -1:
					return None
				else:
					try:
						obj = self._objectIDToObject[objectID]
						return obj
					except KeyError:
						try:
							# set @objectNode to be the object content XML node
							objectNode = self._objectIDToXmlNode[objectID]
						except KeyError:
							return None
						# Get the object type
						objectType = objectNode.getTypeProperty()
						# Create the object
						obj = ioObjectFactoryCreate( objectType )
						# Fill in the id->obj table
						self._objectIDToObject[objectID] = obj
						# Read the object
						objectNode._p_readContentIntoObject( obj )
						return obj
			except ValueError:
				return None

		# Get the type
		objectTypeName = objectNode.getTypeProperty()
		if objectTypeName in g_IOPrimitiveTypeNames:
			objectType = g_IOObjectFactoryRegistryByName[objectTypeName]
			reader, writer = g_IOPrimitiveReadersAndWritersByClass[objectType]
			return reader( objectNode )
		else:
			obj = ioObjectFactoryCreate( objectTypeName )
			objectNode._p_readContentIntoObject( obj )
			return obj


	def _p_readIntoObject(self, objectNode, obj):
		try:
			bDelegate = obj.__ioxml_can_delegate__
		except AttributeError:
			bDelegate = False

		if bDelegate:
			objectNode._p_readContentIntoObject( obj )
		else:
			if objectNode.getTypeProperty() == '__ioxml_object_reference__':
				try:
					objectID = int( objectNode.property( 'ioxml_id' ).value )

					if objectID != -1:
						try:
							# set @objectNode to be the object content node, and use the reading code
							objectNode = self._objectIDToXmlNode[objectID]
						except KeyError:
							return
						# Verify that the contents of the XML node can be read into @obj
						if self._p_verifyReadType( objectNode, obj ):
							assert not self._objectIDToObject.has_key( objectID ), 'object = node.readObject()  called before  node  >>  object'
							# Fill in the id->obj table
							self._objectIDToObject[objectID] = obj
							# Read the object
							objectNode._p_readContentIntoObject( obj )
				except ValueError:
					pass
			else:
				if self._p_verifyReadType( objectNode, obj ):
					objectNode._p_readContentIntoObject( obj )


	def _p_verifyReadType(self, objectNode, obj):
		# Verify that the contents of @objectNode can be read into @obj
		try:
			bReadAll = obj.__ioxml_read_all_types__
		except AttributeError:
			bReadAll = False

		if bReadAll:
			return True
		else:
			typeProp = objectNode.getTypeProperty()
			if ioObjectFactoryNamedClassIsSubclass( typeProp, type( obj ) ):
				return True
			else:
				try:
					readableTypes = obj.__readxml_readable_types__
				except AttributeError:
					return False

				for t in readableTypes:
					if ioObjectFactoryNamedClassIsSubclass( typeProp, t ):
						return True

				return False



	def _p_finalise(self):
		self._parseCurrentNode = None
		self._contentNode = self._documentRootNode.getChild( 'ioxml_content' )
		objectTableNode = self._documentRootNode.getChild( 'ioxml_object_table' )

		if self._contentNode.isValid()  and  objectTableNode.isValid():
			for entryNode in objectTableNode:
				if entryNode.name == 'object':
					try:
						objectID = int( entryNode.property( 'ioxml_id' ).value )
						self._objectIDToXmlNode[objectID] = entryNode
					except ValueError:
						pass


	def _p_initialStartElement(self, name, attrs):
		self._parseCurrentNode = InputXmlNode( self, name, self._parseCurrentNode, attrs )
		self._documentRootNode = self._parseCurrentNode
		self._parser.StartElementHandler = self._p_startElement

	def _p_startElement(self, name, attrs):
		self._parseCurrentNode = InputXmlNode( self, name, self._parseCurrentNode, attrs )
		self._contentList = []

	def _p_endElement(self, name):
		self._parseCurrentNode.content = ''.join( self._contentList )
		self._parseCurrentNode = self._parseCurrentNode.parent

	def _p_characterData(self, data):
		self._contentList.append( data )








#//////////////////////////////////////////////////////////////////////////////
#		OutputXmlNodeProperty
#//////////////////////////////////////////////////////////////////////////////

class OutputXmlNodeProperty:
	"Output XML node property"
	def __init__(self, properties, name):
		"""Constructor:
		@properties - property map
		@name - property name"""
		self._properties = properties
		self._name = name

	def setValue(self, value):
		"Set the property value"
		if self._properties is not None:
			assert self._name is not  None, 'cannot set value of null property'
			assert isinstance( value, str ), 'value must be string'
			self._properties[self._name] = value


def ioXmlWriteBoolProp(prop, value):
	"Write a boolean property"
	prop.setValue( repr( value ) )

def ioXmlWriteIntProp(prop, value):
	"Write an integer property"
	prop.setValue( repr( value ) )

def ioXmlWriteFloatProp(prop, value):
	"Write a float property"
	prop.setValue( repr( value ) )

def ioXmlWriteStringProp(prop, value):
	"Write a string property"
	prop.setValue( value )




#//////////////////////////////////////////////////////////////////////////////
#			OutputXmlNode
#//////////////////////////////////////////////////////////////////////////////

class OutputXmlNode (IOXmlNode):
	"Output XML node"
	def addChild(self, name):
		"Create and add a named child node"
		return OutputXmlNode( self._document, name, self, {} )

	def property(self, name):
		"Create a named property"
		return OutputXmlNodeProperty( self._properties, name )

	def setTypeProperty(self, typeName):
		"Set the 'type' property"
		self.property( 'type' ).setValue( typeName )

	def setVersionProperty(self, versionName):
		"Set the 'version' property"
		assert isinstance( versionName, str ), 'version must be a string'
		self.property( 'version' ).setValue( versionName )

	def setVersionNumberProperty(self, versionNumber):
		"Set the 'version' property to a numeric value"
		assert isinstance( versionNumber, int )  or  isinstance( versionNumber, long ), 'version number must be an integer or a long integer'
		self.property( 'version' ).setValue( str( versionNumber ) )



	def writeObject(self, obj):
		if self.isValid():
			self._document._p_writeObject( self, obj )


	def __lshift__(self, obj):
		if self.isValid():
			self._document._p_writeObject( self, obj )



	def _p_writeContentFromObject(self, writeable):
		writeableType = type( writeable )
		try:
			reader, writer = g_IOPrimitiveReadersAndWritersByClass[writeableType]
			writer( self, writeable )
		except KeyError:
			try:
				f = writeable.__writexml__
			except AttributeError:
				raise TypeError, 'object type %s does not support XML writing' % ( type( writeable ).__name__ )
			self.setTypeProperty( ioObjectFactoryGetName( writeableType ) )
			f( self )


	def _p_writeAsXml(self, contentHandler):
		"Private - Write out to an XML file using the supplied content handler interface"
		contentHandler.startElement( self.name, self._properties )
		if self.content is not None:
			if self.content != '':
				contentHandler.characters( self.content )
		for child in self.children:
			child._p_writeAsXml( contentHandler )
		contentHandler.endElement( self.name )

	def _p_appendChild(self, node):
		"Private - append a supplied child node"
		node._p_appendedToParent( self )

	def _p_replaceChild(self, oldNode, newNode):
		"Private - replace a child new with a new one"
		index = oldNode.indexInParent
		assert index is not None

		newNode.parent = self
		newNode.indexInParent = index
		newNode.prevSibling = oldNode.prevSibling
		newNode.nextSibling = oldNode.nextSibling
		if newNode.prevSibling is not None:
			newNode.prevSibling.nextSibling = newNode
		if newNode.nextSibling is not None:
			newNode.nextSibling.prevSibling = newNode

		oldNode.parent = None
		oldNode.indexInParent = None
		oldNode.prevSibling = oldNode.nextSibling = None

		self.children[index] = newNode





def ioXmlWriteBoolNode(node, value):
	"Write a boolean value to an XML node"
	node.setTypeProperty( 'bool' )
	ioXmlWriteBoolProp( node.property( 'value' ), value )

def ioXmlWriteIntNode(node, value):
	"Write an integer value to an XML node"
	node.setTypeProperty( 'int' )
	ioXmlWriteIntProp( node.property( 'value' ), value )

def ioXmlWriteFloatNode(node, value):
	"Write a float value to an XML node"
	node.setTypeProperty( 'float' )
	ioXmlWriteFloatProp( node.property( 'value' ), value )

def ioXmlWriteStringNode(node, value):
	"Write a string value to an XML node"
	node.setTypeProperty( 'str' )
	ioXmlWriteStringProp( node.property( 'value' ), value )







#//////////////////////////////////////////////////////////////////////////////
#		OutputXmlDocument
#//////////////////////////////////////////////////////////////////////////////

class OutputXmlDocument (object):
	def __init__(self):
		self._documentRootNode = OutputXmlNode( self, 'ioxml_document', None, {} )
		self._contentNode = self._documentRootNode.addChild( 'ioxml_content' )
		self._objectTableNode = self._documentRootNode.addChild( 'ioxml_object_table' )

		# Entry: ( content_node, [list_of_reference_nodes], obj )
		# the @obj reference is kept to ensure that the object does not get killed by the garbage collector.
		# If this happens, then id( obj ) can be 'reused'
		self._objectToEntryTable = {}
		self._bFinalised = False


	def getContentNode(self):
		assert not self._bFinalised, 'attempting to write object to document that has been finalised'
		return self._contentNode

	def writeFile(self, f):
		self._p_finalise()
		generator = xml.sax.saxutils.XMLGenerator( f )
		generator.startDocument()
		self._documentRootNode._p_writeAsXml( generator )
		generator.endDocument()


	def writeString(self):
		f = cStringIO.StringIO()
		self.writeFile( f )
		return f.getvalue()


	def _p_writeObject(self, node, obj):
		assert not self._bFinalised, 'attempting to write object to document that has been finalised'

		"""Private - write an object into the document"""
		if obj is None:
			# None; use and object ID of -1
			node.setTypeProperty( '__ioxml_object_reference__' )
			node.property( 'ioxml_id' ).setValue( str( -1 ) )
		else:
			if type( obj ) in g_IOPrimitiveTypeClasses:
				# The object is a primitive; write it
				node._p_writeContentFromObject( obj )
			else:
				# Get an entry for the object if it already exists
				try:
					entry = self._objectToEntryTable[ id( obj ) ]
				except KeyError:
					# No entry for @obj exitst; create one

					# Create the content node
					contentNode = OutputXmlNode( self, 'ioxml_temporary_name', None, {} )

					# Create the entry for the object->entry table
					entry = ( contentNode, [], obj )
					self._objectToEntryTable[ id( obj ) ] = entry

					# Write the object content
					contentNode._p_writeContentFromObject( obj )

				# Append the XML node to the reference list
				entry[1].append( node )


	def _p_finalise(self):
		"""Private - finalise the document before writing the XML
		Uses the contents of the object->entry table to link up the XML document;
		Objects referenced only once have their content placed at the point of reference.
		Objects referenced more than once have their content placed in the object table node, and reference nodes are placed
		at the reference points."""
		if not self._bFinalised:
			objectIDCounter = 1

			relinkList = []

			for contentNode, refNodeList, objectRef  in  self._objectToEntryTable.values():
				if len( refNodeList ) == 1:
					# One reference
					# Get the reference node
					refNode = refNodeList[0]
					if refNode.parent is None:
						# refNode is itself a content node with no parent. As it has no parent, we cannot re-link at the moment, so handle this as a last step at the end
						relinkList.append( ( refNode, contentNode ) )
					else:
						# Replace the reference node with the content node
						# copy the name over
						contentNode.name = refNode.name
						refNode.parent._p_replaceChild( refNode, contentNode )
				else:
					# Create an ID for this object
					objectIDStr = str( objectIDCounter )
					objectIDCounter += 1

					# Set the name of the content node to 'object' as it is going to be made a child of the object table node
					contentNode.name = 'object'
					# Set the ID
					contentNode.property( 'ioxml_id' ).setValue( objectIDStr )
					# Make it a child of the object table node
					self._objectTableNode._p_appendChild( contentNode )

					# Set up the references
					for refNode in refNodeList:
						# Set the type
						refNode.setTypeProperty( '__ioxml_object_reference__' )
						# and the ID
						refNode.property( 'ioxml_id' ).setValue( objectIDStr )

			# Perform any necessary relinking
			while len( relinkList ) > 0:
				for i, ( refNode, contentNode )  in  enumerate( relinkList ):
					if refNode.parent is not None:
						# Replace the reference node with the content node
						# copy the name over
						contentNode.name = refNode.name
						refNode.parent._p_replaceChild( refNode, contentNode )
						del relinkList[i]
						break

			self._bFinalised = True






#//////////////////////////////////////////////////////////////////////////////
#			IOXmlEnumDecl
#//////////////////////////////////////////////////////////////////////////////

class IOXmlEnumDecl (object):
	def __init__(self, enumClass):
		self._enumClass = enumClass
		self._valueToXmlName = {}
		self._xmlNameToValue = {}


	def value(self, value, xmlName):
		if self._valueToXmlName.has_key( value ):
			raise ValueError, 'value %s already assigned to name %s' % ( value, xmlName )
		if self._xmlNameToValue.has_key( xmlName ):
			raise ValueError, 'name %s already assigned to value %s' % ( xmlName, value )
		self._valueToXmlName[value] = xmlName
		self._xmlNameToValue[xmlName] = value





#//////////////////////////////////////////////////////////////////////////////
#			IOXmlEnum
#//////////////////////////////////////////////////////////////////////////////

class IOXmlEnum (object):
	def __init__(self, enumClass, className, defaultValue):
		self._enumClass = enumClass
		self._className = className
		self._valueToXmlName = {}
		self._xmlNameToValue = {}


		def propReader(prop, default=defaultValue):
			"""Read XML node property"""
			strValue = ioXmlReadStringProp( prop, None )
			if strValue is not None:
				try:
					return self._xmlNameToValue[strValue]
				except KeyError:
					return default
			else:
				return default

		def nodeReader(node, default=defaultValue):
			"""Read XML node"""
			return ioXmlReadNodeHelper( node, self._className, propReader, default )


		def propWriter(prop, value):
			"""Write XML property"""
			try:
				strValue = self._valueToXmlName[value]
				ioXmlWriteStringProp( prop, strValue )
			except KeyError:
				pass

		def nodeWriter(node, value):
			"""Write XML node"""
			node.setTypeProperty( self._className )
			propWriter( node.property( 'value' ), value )


		self.propReader = propReader
		self.nodeReader = nodeReader
		self.propWriter = propWriter
		self.nodeWriter = nodeWriter

		ioObjectFactoryRegisterPrimitiveWithReaderAndWriter( className, enumClass, self.nodeReader, self.nodeWriter )



	def value(self, value, xmlName):
		if self._valueToXmlName.has_key( value ):
			raise ValueError, 'value %s already assigned to name %s' % ( value, xmlName )
		if self._xmlNameToValue.has_key( xmlName ):
			raise ValueError, 'name %s already assigned to value %s' % ( xmlName, value )
		self._valueToXmlName[value] = xmlName
		self._xmlNameToValue[xmlName] = value








ioObjectFactoryRegisterPrimitiveWithReaderAndWriter( 'bool', bool, ioXmlReadBoolNode, ioXmlWriteBoolNode )
ioObjectFactoryRegisterPrimitiveWithReaderAndWriter( 'int', int, ioXmlReadIntNode, ioXmlWriteIntNode )
ioObjectFactoryRegisterPrimitiveWithReaderAndWriter( 'float', float, ioXmlReadFloatNode, ioXmlWriteFloatNode )
ioObjectFactoryRegisterPrimitiveWithReaderAndWriter( 'str', str, ioXmlReadStringNode, ioXmlWriteStringNode )



def isXmlReadable(objectOrClass):
	if hasattr( objectOrClass, '__readxml__' ):
		return True
	else:
		t = objectOrClass
		if not isinstance( t, type ):
			t = type( t )
		try:
			r, w = g_IOPrimitiveReadersAndWritersByClass[t]
		except KeyError:
			return False
		else:
			return r is not None


def isXmlWriteable(objectOrClass):
	if hasattr( objectOrClass, '__writexml__' ):
		return True
	else:
		t = objectOrClass
		if not isinstance( t, type ):
			t = type( t )
		try:
			r, w = g_IOPrimitiveReadersAndWritersByClass[t]
		except KeyError:
			return False
		else:
			return w is not None




def ioReadObjectFromString(s):
	inDoc = InputXmlDocument()
	inDoc.parse( s )
	return inDoc.getContentNode().readObject()

def ioReadObjectFromFile(f):
	inDoc = InputXmlDocument()
	inDoc.parseFile( s )
	return inDoc.getContentNode().readObject()



def ioWriteObjectAsString(obj):
	outDoc = OutputXmlDocument()
	outDoc.getContentNode()  <<  obj
	return outDoc.writeString()

def ioWriteObjectToFile(f, obj):
	outDoc = OutputXmlDocument()
	outDoc.getContentNode()  <<  obj
	outDoc.writeFile( f )



if __name__ == '__main__':
	import sys

	import cStringIO


	class MyObject (object):
		def __init__(self, name='', x = 0, y = 0, z = None, w = None):
			self.name = name
			self.x = x
			self.y = y
			self.z = z
			self.w = w


		def __readxml__(self, node):
			self.name = ioXmlReadStringNode( node.getChild( 'name' ) )
			self.x = ioXmlReadIntNode( node.getChild( 'x' ) )
			self.y = ioXmlReadIntNode( node.getChild( 'y' ) )
			self.z = node.getChild( 'z' ).readObject()
			self.w = node.getChild( 'w' ).readObject()

		def __writexml__(self, node):
			ioXmlWriteStringNode( node.addChild( 'name' ), self.name )
			ioXmlWriteIntNode( node.addChild( 'x' ), self.x )
			ioXmlWriteIntNode( node.addChild( 'y' ), self.y )
			node.addChild( 'z' ).writeObject( self.z )
			node.addChild( 'w' ).writeObject( self.w )

		def __str__(self):
			zID = 0
			wID = 0

			if self.z is not None:
				zID = id(self.z)

			if self.w is not None:
				wID = id(self.w)

			return 'START %s\nid: %d\t\tx=%d\ty=%d\tz=(%d)\tw=(%d)\n%s\n%s\nEND %s\n' % ( self.name, id( self ), self.x, self.y, zID, wID, self.z, self.w, self.name )


	ioObjectFactoryRegister( 'MyObject', MyObject )


	p = MyObject( 'p', 1, 2, None, None )
	q = MyObject( 'q', 3, 4, p, None )
	r = MyObject( 'r', 5, 6, p, None )
	obj = MyObject( 'obj', 7, 8, q, r )

	outDoc = OutputXmlDocument()
	outDoc.getContentNode()  <<  obj
	outStr = outDoc.writeString()

	print outStr


	inDoc = InputXmlDocument()
	inDoc.parse( outStr )

	obj2 = MyObject( 3, 4, None, None )
	inDoc.getContentNode()  >>  obj2

	print obj
	print '******'
	print obj2


















