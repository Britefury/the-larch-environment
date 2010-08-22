##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
import os

import ConfigParser


_userConfigDirName = '.gsym'

# Files in user's home directory
_userConfigDirPath = os.path.expanduser( os.path.join( '~', _userConfigDirName ) )
_userConfigFilePath = os.path.join( _userConfigDirPath, 'userconfig.cfg' )



def makeSettingsDir():
	if not os.path.exists( _userConfigDirPath ):
		os.mkdir( _userConfigDirPath )



		
		
class ConfigProperty (object):
	def __init__(self, section, option, valueType, defaultValue):
		self._section = section
		self._option = option
		self._valueType = valueType
		self._defaultValue = defaultValue


	def __get__(self, obj, objType):
		if obj._config.has_section( self._section )  and  obj._config.has_option( self._section, self._option ):
			try:
				return self._valueType( obj._config.get( self._section, self._option ) )
			except ValueError:
				return self._defaultValue
		else:
			return self._defaultValue

	def __set__(self, obj, value):
		if not obj._config.has_section( self._section ):
			obj._config.add_section( self._section )
		return obj._config.set( self._section, self._option, value )

	def __delete__(self, obj):
		obj._config.remove_option( self._section, self._option )




class GSymUserConfig (object):
	def __init__(self):
		self._config = ConfigParser.SafeConfigParser()


	def load(self):
		if os.path.exists( _userConfigFilePath ):
			try:
				f = open( _userConfigFilePath, 'r' )
			except IOError:
				print 'Could not open user config file for reading'
			else:
				self._config.readfp( f )
				f.close()


	def save(self):
		makeSettingsDir()
		try:
			f = open( _userConfigFilePath, 'w' )
		except IOError:
			print 'Could not open user config file for writing'
		else:
			self._config.write( f )
			f.close()


	pluginPaths = ConfigProperty( 'Paths', 'pluginPaths', str, '' )
	pluginRootPaths = ConfigProperty( 'Paths', 'pluginRootPaths', str, '' )




userConfig = GSymUserConfig()

	