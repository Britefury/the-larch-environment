##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os

from Britefury.DocModel.DMIO import readSX
from Britefury.DocModel.DMListInterface import DMListInterface



def initPlugins(pluginInterface):
	try:
		f = open( os.path.join( 'GSymCore', 'pluginregistry' ),  'r' )
	except:
		print 'Could not open plugin registry'
		return

	content = readSX( f )


	if isinstance( content, list ):
		if content[0] == '$gSymPluginRegistry':
			for moduleName in content[1:]:
				mod = __import__( moduleName )
				components = moduleName.split( '.' )
				for comp in components[1:]:
					mod = getattr( mod, comp )
				initPlugin = getattr( mod, 'initPlugin' )
				initPlugin( pluginInterface )
