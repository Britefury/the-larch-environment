##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os.path



class GLispModuleNotFoundError (Exception):
	pass

_gsymLibsDir = 'GSymLibs'





def getGLispModulePath(path):
	realpath = os.path.realpath( path )
	
	if not os.path.exists( realpath ):
		realpath = os.path.realpath( os.path.join( _gsymLibsDir, path ) )
		
	if not os.path.exists( realpath ):
		raise GLispModuleNotFoundError, path
	
	return realpath
