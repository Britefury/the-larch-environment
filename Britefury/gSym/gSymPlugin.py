##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os




_pluginDirectories = [ 'GSymCore' ]


def _splitPath(path):
	if path.strip() == '':
		return []
	else:
		h, t = os.path.split( path )
		p = [ t ]
		while h is not None  and  h != '':
			h, t = os.path.split( h )
			p.insert( 0, t )
		return p

def _pathToDottedName(path):
	return '.'.join( _splitPath( path ) )

	
					
class GSymPlugin (object):
	def __init__(self, name, initFn):
		self.name = name
		self._initFn = initFn
		
		
	def initialise(self, world):
		self._initFn( self, world )
		
		
	@staticmethod
	def loadPlugins():
		plugins = []
		for pluginDir in _pluginDirectories:
			for dirpath, dirnames, filenames in os.walk( pluginDir ):
				for filename in filenames:
					if filename == 'gsymplugin.py':
						fn, ext = os.path.splitext( filename )

						pluginName = _pathToDottedName( dirpath )
						
						pathComponents = _splitPath( dirpath )
						pathComponents.append( fn )
						importName = '.'.join( pathComponents )
						
						mod = __import__( importName )
						components = importName.split( '.' )
						for comp in components[1:]:
							mod = getattr( mod, comp )
							
						initPluginFn = getattr( mod, 'initPlugin' )

						plugins.append( GSymPlugin( pluginName, initPluginFn ) )
		
		return plugins



