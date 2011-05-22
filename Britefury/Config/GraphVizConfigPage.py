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

from BritefuryJ.GraphViz import GraphViz, Configuration as GraphVizConfiguration

from Britefury.Util.Abstract import abstractmethod

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
		
		
	def _refreshConfig(self):
		if self._graphVizDir is not None  and  os.path.isdir( self._graphVizDir ):
			dotPath = self._checkedToolPath( 'dot' )
			neatoPath = self._checkedToolPath( 'neato' )
			smyrnaPath = self._checkedToolPath( 'smyrna' )
			leftyPath = self._checkedToolPath( 'lefty' )
			dottyPath = self._checkedToolPath( 'dotty' )
			self._config = GraphVizConfiguration( dotPath, neatoPath, smyrnaPath, leftyPath, dottyPath )
			GraphVizConfiguration.setInstance( self._config )
		else:
			self._config = None
			GraphViz.setConfiguration( None )
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
						
		setLink = Hyperlink( 'SET', _onSet )
		controls = setLink.pad( 10.0, 5.0 )
		return Row( [ dirLabel, Spacer( 25.0, 0.0 ), setLink ] )
	
	
	def _presentConfig(self):
		if self._config is not None:
			rows = []
			rows.append( [ Label( 'Dot' ), Label( self._config.getDotPath() ) ] )
			rows.append( [ Label( 'Neato' ), Label( self._config.getNeatoPath() ) ] )
			rows.append( [ Label( 'SMYRNA' ), Label( self._config.getSmyrnaPath() ) ] )
			rows.append( [ Label( 'Lefty' ), Label( self._config.getLeftyPath() ) ] )
			rows.append( [ Label( 'Dotty' ), Label( self._config.getDottyPath() ) ] )
			return self._configTableStyle.applyTo( Table( rows ) ).pad( 5.0, 5.0 )
	
	
	def section(self, title, contents):
		titlePres = Heading3( title )
		return self._sectionStyle.applyTo( Column( [ titlePres, contents ] ) )
		
	

	def __present_contents__(self, fragment, inheritedState):
		self._incr.onAccess()
		dirPres = self._presentDir()
		columnContents = [ dirPres ]
		if self._config is not None:
			columnContents.append( self._presentConfig() )
		pathContents = Column( columnContents )
		pathSection = self.section( 'GraphViz Path', pathContents )
		return Body( [ pathSection ] )
	
	
	_sectionStyle = StyleSheet.instance.withAttr( Primitive.columnSpacing, 1.0 )
	_notSetStyle = StyleSheet.instance.withAttr( Primitive.fontItalic, True )
	_configTableStyle = StyleSheet.instance.withAttr( Primitive.tableColumnSpacing, 10.0 )
	

	
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
