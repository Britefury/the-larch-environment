##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************

from Britefury.FileIO.IOXml import ioXmlReadStringProp, ioXmlWriteStringProp, ioObjectFactoryRegister




class LanguageRegistry (object):
	class _Language (object):
		def __init__(self, filename=''):
			self._filename = filename
			self._languageFactory = None
			
			
		def __readxml__(self, node):
			self._filename = ioXmlReadStringProp( node.property( 'filename' ) )
			
		def __writexml__(self, node):
			ioXmlWriteStringProp( node.property( 'filename' ), self._filename )
			

		def getLanguageFactory(self, languageRegistry):
			if self._languageFactory is not None:
				return self._languageFactory
			else:
				self._languageFactory = languageRegistry._languageLoader( self._filename )
				return self._languageFactory
			
			
			
			
	def __init__(self, languageLoader):
		self._languages = {}
		self._languageLoader = languageLoader

		
	def __readxml__(self, node):
		tableXmlNode = node.getChild( 'table' )
		for entryXmlNode in tableXmlNode.childrenNamed( 'entry' ):
			name = ioXmlReadStringProp( entryXmlNode.property( 'name' ) )
			vendor = ioXmlReadStringProp( entryXmlNode.property( 'vendor' ) )
			version = ioXmlReadStringProp( entryXmlNode.property( 'version' ) )
			k = self._p_key( name, vendor, version )
			language = entryXmlNode.getChild( 'language' ).readObject()
			self._languages[k] = language
			
	
	def __writexml__(self, node):
		tableXmlNode = node.addChild( 'table' )
		for key, language in self._languages.items():
			entryXmlNode = tableXmlNode.addChild( 'entry' )
			ioXmlWriteStringProp( entryXmlNode.property( 'name' ), key[0] )
			ioXmlWriteStringProp( entryXmlNode.property( 'vendor' ), key[1] )
			ioXmlWriteStringProp( entryXmlNode.property( 'version' ), key[2] )
			entryXmlNode.addChild( 'language' )  << language
			
		
		
	def registerLanguage(self, name, vendor, version, filename):
		k = self._p_key( name, vendor, version )
		self._languages[k] = self._Entry( filename )
		
		
	def getLanguageFactory(self, name, vendor, version):
		k = self._p_key( name, vendor, version )
		entry = self._languages[k]
		return entry.getLanguageFactory()

		
	def _p_key(self, name, vendor, version):
		return name, vendor, version

	

ioObjectFactoryRegister( 'GSymLanguageRegistry', LanguageRegistry )
ioObjectFactoryRegister( 'GSymLanguageRegistryEntry', LanguageRegistry._Language )
	
