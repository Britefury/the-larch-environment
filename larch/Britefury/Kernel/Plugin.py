##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
import sys

from Britefury.Config.PathsConfigPage import getPathsConfig
from Britefury import app_in_jar



_jarPluginNames = []


def _testJarEntryName(name):
	return name.endswith( '/larchplugin$py.class' )  or  name.endswith( '/larchplugin.py' )

def _handleJarEntry(name, reader_fn):
	lastSlash = name.rfind( '/' )
	pluginName = name[:lastSlash]
	pluginName = '.'.join( pluginName.split( '/' ) )
	if pluginName not in _jarPluginNames:
		_jarPluginNames.append( pluginName )


app_in_jar.registerJarEntryHandler(_testJarEntryName, _handleJarEntry)




_localPluginDirectories = [ 'LarchCore', 'LarchTools' ]


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


def _getUserPluginDirs():
	return getPathsConfig().pluginPaths

def _getUserPluginRootPaths():
	return getPathsConfig().pluginRootPaths

	

def _loadPlugin(plugins, pluginName):
	importName = pluginName + '.larchplugin'
	mod = __import__( importName )
	components = importName.split( '.' )
	for comp in components[1:]:
		mod = getattr( mod, comp )

	initPluginFn = getattr( mod, 'initPlugin' )

	plugins.append( Plugin( pluginName, initPluginFn ) )



def _loadPluginsFromJar(plugins):
	for pluginName in _jarPluginNames:
		_loadPlugin(plugins, pluginName)



def _loadPluginsInDir(plugins, pluginDir, isLocal):
	for dirpath, dirnames, filenames in os.walk( pluginDir ):
		for filename in filenames:
			if filename == 'larchplugin.py'  or  filename == 'larchplugin$py.class':
				pathComponents = _splitPath( dirpath )
				if isLocal:
					del pathComponents[0]
				pluginName = '.'.join( pathComponents )

				_loadPlugin(plugins, pluginName)

				break

				

class Plugin (object):
	def __init__(self, name, initFn):
		self.name = name
		self._initFn = initFn
		
		
	def initialise(self, world):
		self._initFn( self, world )
		
		
	@staticmethod
	def loadPlugins():
		sys.path.extend( _getUserPluginRootPaths() )
		
		
		plugins = []

		if app_in_jar.startedFromJar():
			_loadPluginsFromJar( plugins )
		else:
			for pluginDir in _localPluginDirectories:
				_loadPluginsInDir( plugins, os.path.join( 'larch', pluginDir ), True )
		
		for pluginDir in _getUserPluginDirs():
			_loadPluginsInDir( plugins, pluginDir, False )
		
		return plugins


		
