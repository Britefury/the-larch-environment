##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os

import ConfigParser


_userConfigDirName = '.larch'

# Files in user's home directory
_userConfigDirPath = os.path.expanduser( os.path.join( '~', _userConfigDirName ) )
_userConfigFilePath = os.path.join( _userConfigDirPath, 'userconfig.cfg' )




def makeSettingsDir():
	if not os.path.exists( _userConfigDirPath ):
		os.mkdir( _userConfigDirPath )

		

def userConfigFilePath(filename):
	return os.path.join( _userConfigDirPath, filename )

	