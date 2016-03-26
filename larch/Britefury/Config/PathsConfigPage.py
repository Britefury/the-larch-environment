##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from javax.swing import JFileChooser
from java.awt import Color, BasicStroke

import os

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import Hyperlink, MenuItem

from BritefuryJ.Pres.Primitive import Primitive, Label, Column
from BritefuryJ.Pres.RichText import Body
from BritefuryJ.Pres.UI import Section, SectionHeading2
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.Graphics import FilledOutlinePainter

from Britefury.Config import Configuration
from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config.ConfigurationPage import ConfigurationPage




_pathsConfigFilename = 'paths'


_itemHoverHighlightStyle = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.8, 0.825, 0.9 ), Color( 0.125, 0.341, 0.574 ), BasicStroke( 1.0 ) ) ) )




class PathsConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( PathsConfigurationPage, self ).__init__()
		self._pluginPaths = []
		self._libraryPaths = []
		self._incr = IncrementalValueMonitor()
	
	
	def __getstate__(self):
		state = super( PathsConfigurationPage, self ).__getstate__()
		state['pluginPaths'] = self._pluginPaths
		state['libraryPaths'] = self._libraryPaths
		return state
	
	def __setstate__(self, state):
		super( PathsConfigurationPage, self ).__setstate__( state )
		self._pluginPaths = state['pluginPaths']
		try:
			self._libraryPaths = state['libraryPaths']
		except KeyError:
			self._libraryPaths = state['pluginRootPaths']
		self._incr = IncrementalValueMonitor()
	
		
		
	@property
	def pluginPaths(self):
		return self._pluginPaths
	
	@property
	def libraryPaths(self):
		return self._libraryPaths


	
	def getSubjectTitle(self):
		return '[CFG] Paths'
	
	def getTitleText(self):
		return 'Paths Configuration'
	
	def getLinkText(self):
		return 'Paths'
	
	
	def presentPathList(self, pathList):
		def pathItem(index):
			def _onDelete(menuItem):
				del pathList[index]
				self._incr.onChanged()

			def buildContextMenu(element, menu):
				menu.add( MenuItem.menuItemWithLabel( 'Delete', _onDelete ) )
				return True

			return _itemHoverHighlightStyle.applyTo( Label( pathList[index] ) ).withContextMenuInteractor( buildContextMenu )


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

		pathPres = Column( [ pathItem( i )   for i in xrange( len( pathList ) ) ] )
		return Column( [ controls, pathPres.padX( 5.0 ) ] )
	
	
	def pathsSection(self, title, pathList):
		pathsPres = self.presentPathList( pathList )
		return Section( SectionHeading2( title ), pathsPres )

	

	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()
		pluginPathsPres = self.pathsSection( 'Plugin paths', self._pluginPaths )
		pluginRootPathsPres = self.pathsSection( 'Library paths', self._libraryPaths )
		return Body( [ pluginPathsPres, pluginRootPathsPres ] )

	
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


