##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os

from BritefuryJ.Isolation import IsolationPickle


_userConfigDirName = '.larch'

# Files in user's home directory
_userConfigDirPath = os.path.expanduser( os.path.join( '~', _userConfigDirName ) )
_userConfigFilePath = os.path.join( _userConfigDirPath, 'userconfig.cfg' )




def makeSettingsDir():
	if not os.path.exists( _userConfigDirPath ):
		os.mkdir( _userConfigDirPath )

		

def userConfigFilePath(filename):
	return os.path.join( _userConfigDirPath, filename )



def loadUserConfig(filename):
	config = None
	path = userConfigFilePath( filename )
	
	if os.path.exists( path ):
		try:
			f = open( path, 'r' )
		except IOError:
			print 'Could not open config file \'%s\' for reading' % path
			return

		try:
			config = IsolationPickle.load( f )
		except EOFError:
			print 'Could not read config file \'%s\' - EOF' % path
		finally:
			f.close()
	
	return config


def saveUserConfig(filename, config):
	if config is not None:
		makeSettingsDir()
		path = userConfigFilePath( filename )
		try:
			f = open( path, 'w+' )
		except IOError:
			print 'Could not open user config file \'%s\' for writing' % path
			return
	
		IsolationPickle.dump( config, f )
		f.close()
