##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import sys
import os

from datetime import datetime

from java.lang import Runnable
from javax.swing import JFrame, AbstractAction, JMenuItem, JMenu, JMenuBar, KeyStroke, JOptionPane, JFileChooser, JOptionPane, JTextField, JLabel, BoxLayout, JPanel, BorderFactory
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Font, Color
from java.awt.event import WindowListener, ActionListener, KeyEvent


from BritefuryJ.CommandHistory import CommandHistory, CommandHistoryListener

from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMNode

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *

from BritefuryJ.DocView import DocView


from Britefury.Kernel.Abstract import abstractmethod


from Britefury.Event.QueuedEvent import queueEvent


from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymEnvironment import GSymEnvironment
from Britefury.gSym.gSymDocument import GSymDocument, GSymUnit, viewUnit, viewUnitLisp, transformUnit

from Britefury.Plugin import InitPlugins


from Britefury.MainApp.LocationBar import LocationBar, LocationBarListener





class GSymScriptEnvironment (object):
	def __init__(self, app):
		self._app = app


	def _p_getApp(self):
		return self._app

	app = property( _p_getApp, doc=_( 'The gSym app (a Britefury.MainApp.MainApp)' ) )

	__doc__ = _( """gSym Scripting Environment
	GSymScriptEnvironment(app) -> scripting environment with @app as the app""" )


	
	
	
class MainAppPluginInterface (object):
	def __init__(self, app):
		self._app = app
		
		
	def registerNewDocumentFactory(self, menuLabel, newDocFn):
		self._app.registerNewDocumentFactory( menuLabel, newDocFn )
		
	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		self._app.registerImporter( menuLabel, fileType, filePattern, importFn )
		

		
class MainAppDocView (DocView.RefreshListener):
	def __init__(self, app):
		self._app = app
		
		self._elementTree = ElementTree()
		self._area = self._elementTree.getPresentationArea()
		self._area.getComponent().setPreferredSize( Dimension( 640, 480 ) )
		
		self._view = None
		
		
	def getComponent(self):
		return self._area.getComponent()
	
		
	def setUnit(self, unit):
		if unit is not None:
			self._view = self.createView( unit )
			self._view.setRefreshListener( self )
			self._refreshView()
			self._elementTree.getRoot().setChild( self._view.getRootView().getElement() )
		else:
			self._view = None
			
			textStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 12 ), Color( 0.0, 0.0, 0.5 ) )
			textElem = TextElement( textStyle, '<empty>' )
			rootElem = self._elementTree.getRoot()
			self._elementTree.getRoot().setChild( textElem )
			
	
	@abstractmethod
	def createView(self, unit):
		pass
		
			
	def _refreshView(self):
		if self._view is not None:
			t1 = datetime.now()
			self._view.refresh()
			t2 = datetime.now()
			print 'MainApp: REFRESH VIEW TIME = ', t2 - t1

	def _queueRefresh(self):
		class Run (Runnable):
			def run(r):
				self._refreshView()
		self._area.queueImmediateEvent( Run() )
	
	
	
	def createTreeExplorer(self):
		self._elementTree.createTreeExplorer()
	
		
	def reset(self):
		self._area.reset()
			
	def oneToOne(self):
		self._area.oneToOne()

		
		
	def onViewRequestRefresh(self, view):
		self._queueRefresh()
		
		

		
class MainAppDocViewNormal (MainAppDocView):
	def createView(self, unit):
		return viewUnit( unit, self._app._world, self._app._commandHistory )
		
		
class MainAppDocViewLisp (MainAppDocView):
	def createView(self, unit):
		return viewUnitLisp( unit, self._app._world, self._app._commandHistory )
		
		
		

		
def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		
		



class MainApp (object):
	def __init__(self, world, unit):
		document = GSymDocument( unit )   if unit is not None   else   None
		self._document = None
		self._commandHistory = None
		self._bUnsavedData = False
		
		self._world = world
		
		self._docView = MainAppDocViewNormal( self )

		
		
		
		# FILE -> NEW MENU
		
		self._newMenu = JMenu( 'New' )
		self._newMenu.add( _action( 'Empty', self._onNewEmpty ) )
		
		
		
		# FILE -> IMPORT MENU
		
		self._importMenu = JMenu( 'Import' )


		
		# FILE MENU
		
		fileMenu = JMenu( 'File' )
		fileMenu.add( self._newMenu )
		fileMenu.add( _action( 'Open', self._onOpen ) )
		fileMenu.add( _action( 'Save', self._onSave ) )
		fileMenu.add( self._importMenu )


		
		# EDIT MENU
		
		editMenu = JMenu( 'Edit' )
		editMenu.add( _action( 'Undo', self._onUndo ) )
		editMenu.getItem( 0 ).setAccelerator( KeyStroke.getKeyStroke( 'ctrl z' ) )
		editMenu.add( _action( 'Redo', self._onRedo ) )
		editMenu.getItem( 1 ).setAccelerator( KeyStroke.getKeyStroke( 'ctrl shift z' ) )

		
		
		# ACTIONS MENU
		
		self._actionsMenu = JMenu( 'Actions' )
		self._actionsMenu.add( _action( 'Transform...', self._onTransform ) )



		
		# VIEW MENU
		
		viewMenu = JMenu( 'View' )
		viewMenu.add( _action( 'Show LISP window', self._onShowLisp ) )
		viewMenu.add( _action( 'Show element tree explorer', self._onShowElementTreeExplorer ) )
		viewMenu.add( _action( 'Reset', self._onReset ) )
		viewMenu.add( _action( '1:1', self._onOneToOne ) )
		
		
		
		# SCRIPT MENU
		
		scriptMenu = JMenu( 'Script' )
		scriptMenu.add( _action( _( 'Script window' ), self._onScriptWindowMenuItem ) )
		
		def _testAction():
			self._documentRoot[2][1][2][1][1][1][1] = 'this'
		
		scriptMenu.add( _action( _( 'test' ), _testAction ) )
		
		menuBar = JMenuBar();
		menuBar.add( fileMenu )
		menuBar.add( editMenu )
		menuBar.add( self._actionsMenu )
		menuBar.add( viewMenu )
		menuBar.add( scriptMenu )

		
		
		self._initialise()
		
		
		
		# LOCATION AND FORMAT
		
		class _LocationBarListener (LocationBarListener):
			def _onLocation(listener_self, location, format):
				self._onLocation( location, format )

		self._locationBar = LocationBar( _LocationBarListener(), '', '' )
		
		
		
		
		
		# WINDOW
		
		windowPanel = JPanel()
		windowPanel.setLayout( BoxLayout( windowPanel, BoxLayout.Y_AXIS ) )
		#windowPanel.add( self._locationBar.getComponent() )
		windowPanel.add( self._docView.getComponent() )
		
		
		

		self._frame = JFrame( 'gSym' )
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._frame.setJMenuBar( menuBar )
		
		self._frame.add( windowPanel )
		
		self._frame.pack()
		
		
		
		
		
		#
		# LISP window
		#
		self._lispDocView = None
		self._lispFrame = None
		self._bLispWindowVisible = False

		
		# Set the document
		self.setDocument( document )
		
		

		#
		# Plugins
		#
		self._pluginInterface = MainAppPluginInterface( self )
		InitPlugins.initPlugins( self._pluginInterface )

		
		
		#
		# Script window
		#
		scriptBanner = _( "gSym scripting console (uses pyconsole by Yevgen Muntyan)\nPython %s\nType help(object) for help on an object\nThe gSym scripting environment is available via the local variable 'gsym'\n" ) % ( sys.version, )
		self._scriptEnv = GSymScriptEnvironment( self )
		#self._scriptConsole = Console( locals = { 'gsym' : self._scriptEnv }, banner=scriptBanner, use_rlcompleter=False )
		#self._scriptConsole.connect( 'command', self._p_onScriptPreCommand )
		#self._scriptConsole.connect_after( 'command', self._p_onScriptPostCommand )
		#self._scriptConsole.show()

		#self._scriptScrolledWindow = gtk.ScrolledWindow()
		#self._scriptScrolledWindow.set_policy( gtk.POLICY_AUTOMATIC, gtk.POLICY_AUTOMATIC )
		#self._scriptScrolledWindow.add( self._scriptConsole )
		#self._scriptScrolledWindow.set_size_request( 640, 480 )
		#self._scriptScrolledWindow.show()

		#self._scriptWindow = gtk.Window( gtk.WINDOW_TOPLEVEL )
		#self._scriptWindow.set_transient_for( self._window )
		#self._scriptWindow.add( self._scriptScrolledWindow )
		#self._scriptWindow.connect( 'delete-event', self._p_onScriptWindowDelete )
		#self._scriptWindow.set_title( _( 'gSym Script Window' ) )
		self._bScriptWindowVisible = False

		

	
	def run(self):
		self._frame.setVisible( True )

		
		
		
		
	def _initialise(self):
		pass
		


	def setDocument(self, document):
		if self._document is not None:
			print 'command history no longer tracking tracking a ', type( self._document.unit.content )
			self._commandHistory.stopTracking( self._document.unit.content )

		self._document = document
		
		#self._actionsMenu.removeAll()
		
		

		self._commandHistory = CommandHistory()
		
		class Listener (CommandHistoryListener):
			def onCommandHistoryChanged(_self, history):
				self._onCommandHistoryChanged( history )
		
		if self._document is not None:
			self._commandHistory.track( self._document.unit.content )
		self._commandHistory.setListener( Listener() )
		self._bUnsavedData = False
		

		unit = self._document.unit   if self._document is not None   else   None
		self._docView.setUnit( unit )
		
		self._setLispDocument()
			
			
	def _setLispDocument(self):
		if self._lispDocView is not None:
			unit = self._document.unit   if self._document is not None   else   None
			self._lispDocView.setUnit( unit )

			
			
			
			
	def _onCommandHistoryChanged(self, commandHistory):
		self._bUnsavedData = True

		
		
	def _onNewEmpty(self):
		bProceed = True
		if self._bUnsavedData:
			response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'New Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'New', 'Cancel' ], 'Cancel' )
			bProceed = response == JOptionPane.YES_OPTION
		if bProceed:
			self.setDocument( None )


	def registerNewDocumentFactory(self, menuLabel, newUnitFn):
		def _onNew():
			bProceed = True
			if self._bUnsavedData:
				response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'New Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'New', 'Cancel' ], 'Cancel' )
				bProceed = response == JOptionPane.YES_OPTION
			if bProceed:
				unit = newUnitFn()
				if unit  is not None:
					self.setDocument( GSymDocument( unit ) )

		self._newMenu.add( _action( menuLabel, _onNew ) )

		
		
		
		
	def _onOpen(self):
		bProceed = True
		if self._bUnsavedData:
			response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'Open Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Open', 'Cancel' ], 'Cancel' )
			bProceed = response == JOptionPane.YES_OPTION
		if bProceed:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showOpenDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						f = open( filename, 'r' )
						if f is not None:
							try:
								t1 = datetime.now()
								documentRoot = DMIOReader.readFromString( f.read(), self._world.resolver )
								t2 = datetime.now()
								documentRoot = DMNode.coerce( documentRoot )
								t3 = datetime.now()
								print 'Read SX time=%s, convert to DMNode time=%s'  %  ( t2 - t1, t3 - t2 )
								document = GSymDocument.read( self._world, documentRoot )
								self.setDocument( document )
							except IOError:
								pass


	def _onSave(self):
		filename = None
		bFinished = False
		while not bFinished:
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( 'gSym project (*.gsym)', [ 'gsym' ] ) )
			response = openDialog.showSaveDialog( self._frame )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filenameFromDialog = sf.getPath()
					if filenameFromDialog is not None:
						if os.path.exists( filenameFromDialog ):
							response = JOptionPane.showOptionDialog( self._frame, 'File already exists. Overwrite?', 'File already exists', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Overwrite', 'Cancel' ], 'Cancel' )
							if response == JFileChooser.APPROVE_OPTION:
								filename = filenameFromDialog
								bFinished = True
							else:
								bFinished = False
						else:
							filename = filenameFromDialog
							bFinished = True
					else:
						bFinished = True
				else:
					bFinished = True
			else:
				bFinished = True

		if filename is not None:
			self._writeFile( filename )
			return True
		else:
			return False


	def registerImporter(self, menuLabel, fileType, filePattern, importFn):
		def _onImport():
			bProceed = True
			if self._bUnsavedData:
				response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'Import', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Open', 'Cancel' ], 'Cancel' )
				bProceed = response == JOptionPane.YES_OPTION
			if bProceed:
				openDialog = JFileChooser()
				openDialog.setFileFilter( FileNameExtensionFilter( fileType, [ filePattern ] ) )
				response = openDialog.showDialog( self._frame, 'Import' )
				if response == JFileChooser.APPROVE_OPTION:
					sf = openDialog.getSelectedFile()
					if sf is not None:
						filename = sf.getPath()
						if filename is not None:
							t1 = datetime.now()
							unit = importFn( filename )
							t2 = datetime.now()
							if unit is not None:
								document = GSymDocument( unit )
								print 'MainApp: IMPORT TIME = %s'  %  ( t2 - t1, )
								self.setDocument( document )
		
		self._importMenu.add( _action( menuLabel, _onImport ) )

		
		
		
		
	def _writeFile(self, filename):
		if self._document is not None:
			f = open( filename, 'w' )
			if f is not None:
				f.write( DMIOWriter.writeAsString( self._document.write() ) )
				f.close()
				self._bUnsavedData = False


	def _onUndo(self):
		if self._commandHistory.canUndo():
			self._commandHistory.undo()

	def _onRedo(self):
		if self._commandHistory.canRedo():
			self._commandHistory.redo()


		

	def _onTransform(self):
		openDialog = JFileChooser()
		openDialog.setFileFilter( FileNameExtensionFilter( 'Python source (*.py)', [ 'py' ] ) )
		response = openDialog.showOpenDialog( self._frame )
		if response == JFileChooser.APPROVE_OPTION:
			sf = openDialog.getSelectedFile()
			if sf is not None:
				filename = sf.getPath()
				if filename is not None:
					env = {}
					execfile( filename, env )
					xFn = env['xform']
					
					if self._document is not None:
						transformUnit( self._document.unit, self._world, xFn )
							

					
	def _onShowLisp(self):
		self._bLispWindowVisible = not self._bLispWindowVisible
		if self._bLispWindowVisible:
			class _Listener (WindowListener):
				def windowActivated(listener, event):
					pass
				
				def windowClosed(listener, event):
					self._lispDocView = None
					self._lispFrame = None
					self._bLispWindowVisible = False
				
				def windowClosing(listener, event):
					pass
				
				def windowDeactivated(listener, event):
					pass
				
				def windowDeiconified(listener, event):
					pass
				
				def windowIconified(listener, event):
					pass
				
				def windowOpened(listener, event):
					pass
			
			
			self._lispDocView = MainAppDocViewLisp( self )
			self._lispFrame = JFrame( 'LISP View Window' )
			self._lispFrame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			self._lispFrame.add( self._lispDocView.getComponent() )
			self._lispFrame.pack()
			self._lispFrame.setVisible( True )
			self._setLispDocument()
		else:
			self._lispDocView = None
			self._lispFrame.dispose()
			self._lispFrame = None
	
	
	def _onShowElementTreeExplorer(self):
		self._docView.createTreeExplorer()


	def _onScriptWindowMenuItem(self):
		self._bScriptWindowVisible = not self._bScriptWindowVisible
		if self._bScriptWindowVisible:
			self._scriptWindow.show()
		else:
			self._scriptWindow.hide()


	def _onReset(self):
		self._docView.reset()
		if self._lispDocView is not None:
			self._lispDocView.reset()

	def _onOneToOne(self):
		self._docView.oneToOne()
		if self._lispDocView is not None:
			self._lispDocView.oneToOne()
			
			
			
	
	def _onLocation(self, location, format):
		self._changeLocation( location, format )
	

	def setLocation(self, location, format):
		self._locationBar.setLocationAndFormat( location, format )
		self._changeLocation( location )
		
		
	def _changeLocation(self, location, format):
		print 'MainApp._changeLocation: ', location, format
		

		

		
	#def _p_onScriptPreCommand(self, console, code):
		#self._commandHistory.freeze()

	#def _p_onScriptPostCommand(self, console, code):
		#self._commandHistory.thaw()




	


