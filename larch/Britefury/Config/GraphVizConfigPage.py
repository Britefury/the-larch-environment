##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter
from java.net import URI

from java.awt import Color

import os
import sys

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Graphics import SolidBorder

from BritefuryJ.Pres.Primitive import Primitive, Label, Spacer, Border, Row, Column, Table
from BritefuryJ.Pres.RichText import Body, NormalText, EmphSpan
from BritefuryJ.Pres.UI import Section, SectionHeading2, SectionHeading3, NotesText
from BritefuryJ.StyleSheet import StyleSheet

from BritefuryJ.GraphViz import GraphViz, Configuration as GraphVizConfiguration

from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config import Configuration
from Britefury.Config.ConfigurationPage import ConfigurationPage




_graphVizConfigFilename = 'graphviz'


if sys.registry['os.name'].startswith( 'Windows' ):
	_exeExtension = '.exe'
else:
	_exeExtension = ''




class GraphVizConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( GraphVizConfigurationPage, self ).__init__()
		self._graphVizDir = None
		self._config = None
		self._incr = IncrementalValueMonitor()
	
	
	def initPage(self, config):
		super( GraphVizConfigurationPage, self ).initPage( config )
		GraphVizConfiguration.setConfigurationPageSubject( self.subject() )




	def __getstate__(self):
		state = super( GraphVizConfigurationPage, self ).__getstate__()
		state['graphVizDir'] = self._graphVizDir
		return state
	
	def __setstate__(self, state):
		super( GraphVizConfigurationPage, self ).__setstate__( state )
		self._graphVizDir = state['graphVizDir']
		self._config = None
		self._incr = IncrementalValueMonitor()

		self._refreshConfig()
	
		
	def _checkedToolPath(self, name):
		path = os.path.join( self._graphVizDir, name + _exeExtension )
		if os.path.exists( path ):
			return path
		else:
			return None
		
		
		
	def _setGraphVizDir(self, dir):
		self._graphVizDir = dir
		self._refreshConfig()
		self._incr.onChanged()


	def __isConfigured(self):
		if self._graphVizDir is not None  and  os.path.isdir( self._graphVizDir ):
			dotPath = self._checkedToolPath( 'dot' )
			neatoPath = self._checkedToolPath( 'neato' )
			twopiPath = self._checkedToolPath( 'twopi' )
			circoPath = self._checkedToolPath( 'circo' )
			fdpPath = self._checkedToolPath( 'fdp' )
			sfdpPath = self._checkedToolPath( 'sfdp' )
			osagePath = self._checkedToolPath( 'osage' )
			return dotPath is not None  and  neatoPath is not None  and  twopiPath is not None  and  circoPath is not None  and  fdpPath is not None  and  sfdpPath is not None  and  osagePath is not None
		return False

		
		
	def _refreshConfig(self):
		if self._graphVizDir is not None  and  os.path.isdir( self._graphVizDir ):
			dotPath = self._checkedToolPath( 'dot' )
			neatoPath = self._checkedToolPath( 'neato' )
			twopiPath = self._checkedToolPath( 'twopi' )
			circoPath = self._checkedToolPath( 'circo' )
			fdpPath = self._checkedToolPath( 'fdp' )
			sfdpPath = self._checkedToolPath( 'sfdp' )
			osagePath = self._checkedToolPath( 'osage' )
			self._config = GraphVizConfiguration( dotPath, neatoPath, twopiPath, circoPath, fdpPath, sfdpPath, osagePath )
			GraphVizConfiguration.setInstance( self._config )
		else:
			self._config = None
			GraphVizConfiguration.setInstance( None )
		self._incr.onChanged()
		
			
	
	def getSubjectTitle(self):
		return '[CFG] GraphViz'
	
	def getTitleText(self):
		return 'GraphViz Configuration'
	
	def getLinkText(self):
		return 'GraphViz'
	
	
	def _presentDir(self):
		def _onSet(hyperlink, event):
			component = hyperlink.getElement().getRootElement().getComponent()
			openDialog = JFileChooser()
			openDialog.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY )
			response = openDialog.showDialog( component, 'Choose path' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None  and  os.path.isdir( filename ):
						self._graphVizDir = filename
						self._refreshConfig()
						self._incr.onChanged()
		
		dirLabel = Label( self._graphVizDir )   if self._graphVizDir is not None   else   self._notSetStyle.applyTo( Label( '<Not set>' ) )
						
		setLink = Hyperlink( 'CHANGE', _onSet )
		return self._dirBorderStyle.applyTo( Border( Row( [ dirLabel, Spacer( 25.0, 0.0 ), setLink ] ) ) )
	
	
	def _toolLabel(self, name):
		return self._configTableToolNameStyle.applyTo( Label( name ) )
	
	def _presentConfig(self):
		if self._config is not None:
			rows = []
			rows.append( [ SectionHeading3( 'Tool' ), SectionHeading3( 'Path' ) ] )
			rows.append( [ self._toolLabel( 'dot' ), Label( self._config.getDotPath() ) ] )
			rows.append( [ self._toolLabel( 'neato' ), Label( self._config.getNeatoPath() ) ] )
			rows.append( [ self._toolLabel( 'twopi' ), Label( self._config.getTwopiPath() ) ] )
			rows.append( [ self._toolLabel( 'circo' ), Label( self._config.getCircoPath() ) ] )
			rows.append( [ self._toolLabel( 'fdp' ), Label( self._config.getFdpPath() ) ] )
			rows.append( [ self._toolLabel( 'sfdp' ), Label( self._config.getSfdpPath() ) ] )
			rows.append( [ self._toolLabel( 'osage' ), Label( self._config.getOsagePath() ) ] )
			return self._configTableStyle.applyTo( Table( rows ) ).pad( 15.0, 5.0 )
	
	
	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()

		dirPres = self._presentDir()
		note = NotesText( [ 'Note: please choose the location of the GraphViz ', EmphSpan( 'bin' ), ' directory.' ] )
		columnContents = [ note, dirPres ]
		if self._config is not None:
			columnContents.append( self._presentConfig() )
		pathContents = Column( columnContents )
		pathSection = Section( SectionHeading2( 'GraphViz path' ), pathContents )


		downloadText = ''
		if self.__isConfigured():
			downloadText = 'GraphViz appears to be installed on this machine and configured. If it does not work correctly, you may need to install it. You can download it from the '
		else:
			downloadText = 'If GraphViz is not installed on this machine, please install it. You can download it from the '

		downloadLink = Hyperlink( 'GraphViz homepage', URI( 'http://www.graphviz.org/' ) )
		download = NormalText( [ downloadText, downloadLink, '.' ] )
		downloadSec = Section( SectionHeading2( 'GraphViz download/installation' ), download )


		return Body( [ pathSection, downloadSec ] )


	_dirBorderStyle = StyleSheet.style( Primitive.border( SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 1.0, 0.85, 0.0 ), Color( 1.0, 1.0, 0.85 ) ) ) )
	_notSetStyle = StyleSheet.style( Primitive.fontItalic( True ) )
	_configTableStyle = StyleSheet.style( Primitive.tableColumnSpacing( 10.0 ) )
	_configTableToolNameStyle = StyleSheet.style( Primitive.foreground( Color( 0.25, 0.0, 0.5 ) ) )
	

	
def _loadGraphVizConfig():
	return loadUserConfig( _graphVizConfigFilename )


def saveGraphVizConfig():
	saveUserConfig( _graphVizConfigFilename, _graphvizConfig )

			


_graphvizConfig = None


def initGraphVizConfig():
	global _graphvizConfig
	
	if _graphvizConfig is None:
		_graphvizConfig = _loadGraphVizConfig()
		
		if _graphvizConfig is None:
			_graphvizConfig = GraphVizConfigurationPage()

		Configuration.registerSystemConfigurationPage( _graphvizConfig )
