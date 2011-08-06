##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

import os
import sys

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.StyleSheet import *

from Britefury.Config import Configuration
from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config.ConfigurationPage import ConfigurationPage




_pathsConfigFilename = 'paths'





class PathsConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( PathsConfigurationPage, self ).__init__()
		self._pluginPaths = []
		self._pluginRootPaths = []
		self._incr = IncrementalValueMonitor()
	
	
	def __getstate__(self):
		state = super( PathsConfigurationPage, self ).__getstate__()
		state['pluginPaths'] = self._pluginPaths
		state['pluginRootPaths'] = self._pluginRootPaths
		return state
	
	def __setstate__(self, state):
		super( PathsConfigurationPage, self ).__setstate__( state )
		self._pluginPaths = state['pluginPaths']
		self._pluginRootPaths = state['pluginRootPaths']
		self._incr = IncrementalValueMonitor()
	
		
		
	def getPluginPaths(self):
		return self._pluginPaths
	
	def getPluginRootPaths(self):
		return self._pluginRootPaths


	
	def getSubjectTitle(self):
		return '[CFG] Paths'
	
	def getTitleText(self):
		return 'Paths Configuration'
	
	def getLinkText(self):
		return 'Paths'
	
	
	def presentPathList(self, pathList):
		def _onNew(hyperlink, event):
			component = hyperlink.getElement().getRootElement().getComponent()
			openDialog = JFileChooser()
			openDialog.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY )
			response = openDialog.showDialog( component, 'Choose path' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None  and  os.path.isdir( filename ):
						pathList.append( filename )
						self._incr.onChanged()
		
		newLink = Hyperlink( 'NEW', _onNew )
		controls = newLink.pad( 10.0, 5.0 )
		pathPres = Column( [ Label( path )   for path in pathList ] )
		return Column( [ controls, pathPres.padX( 5.0 ) ] )
	
	
	def pathsSection(self, title, pathList):
		titlePres = Heading3( title )
		pathsPres = self.presentPathList( pathList )
		return self._sectionStyle.applyTo( Column( [ titlePres, pathsPres ] ) )
		
	

	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()
		pluginPathsPres = self.pathsSection( 'Plugin Paths', self._pluginPaths )
		pluginRootPathsPres = self.pathsSection( 'Plugin Root Paths', self._pluginRootPaths )
		return Body( [ pluginPathsPres, pluginRootPathsPres ] )
	
	
	pluginPaths = property( getPluginPaths )
	pluginRootPaths = property( getPluginRootPaths )
	
	_sectionStyle = StyleSheet.style( Primitive.columnSpacing( 1.0 ) )
	

	
def _loadPathsConfig():
	return loadUserConfig( _pathsConfigFilename )


def savePathsConfig():
	saveUserConfig( _pathsConfigFilename, _pathsConfig )

			


_pathsConfig = None


def initPathsConfig():
	global _pathsConfig
	
	if _pathsConfig is None:
		_pathsConfig = _loadPathsConfig()
		
		if _pathsConfig is None:
			_pathsConfig = PathsConfigurationPage()
	
		Configuration.registerSystemConfigurationPage( _pathsConfig )


def getPathsConfig():
	global _pathsConfig
	
	if _pathsConfig is None:
		_pathsConfig = _loadPathsConfig()
		
		if _pathsConfig is None:
			_pathsConfig = PathsConfigurationPage()
		
	return _pathsConfig


