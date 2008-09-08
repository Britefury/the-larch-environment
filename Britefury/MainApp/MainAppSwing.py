##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from java.lang import Runnable
from javax.swing import JFrame, AbstractAction, JMenuItem, JMenu, JMenuBar, KeyStroke, JOptionPane, JFileChooser, JOptionPane
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Font, Color
from java.awt.event import WindowListener


from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.ElementTree import *
from BritefuryJ.DocPresent.StyleSheets import *

from BritefuryJ.Cell import CellListener


from Britefury.Event.QueuedEvent import queueEvent

from Britefury.CommandHistory.CommandHistory import CommandHistory

from Britefury.DocModel.DMList import DMList
from Britefury.DocModel.DMIO import readSX, writeSX

from Britefury.gSym.gSymWorld import GSymWorld
from Britefury.gSym.gSymEnvironment import GSymEnvironment
from Britefury.gSym.gSymDocument import loadDocument, newDocument, GSymDocumentViewContentHandler, GSymDocumentLISPViewContentHandler

from Britefury.Plugin import InitPlugins





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


		
class MainAppDocView (CellListener):	
	def __init__(self, app):
		self._app = app
		
		self._elementTree = ElementTree()
		self._area = self._elementTree.getPresentationArea()
		self._area.getComponent().setPreferredSize( Dimension( 640, 480 ) )
		
		self._view = None
		
		
	def getComponent(self):
		return self._area.getComponent()
	
		
	def setDocumentContent(self, documentRoot, contentHandler):
		if documentRoot is not None:
			self._view = loadDocument( self._app._world, documentRoot, contentHandler )
			self._view.refreshCell.addListener( self )
			self._view.refresh()
			self._elementTree.getRoot().setChild( self._view.getRootView().getElement() )
		else:
			self._view = None
			
			textStyle = TextStyleSheet( Font( 'SansSerif', Font.BOLD, 12 ), Color( 0.0, 0.0, 0.5 ) )
			textElem = TextElement( textStyle, '<empty>' )
			rootElem = self._elementTree.getRoot()
			self._elementTree.getRoot().setChild( textElem )
		
			
	def _refreshView(self):
		if self._view is not None:
			self._view.refresh()

	def _queueRefresh(self):
		class Run (Runnable):
			def run(r):
				self._refreshView()
		self._area.queueImmediateEvent( Run() )
		
		
	def reset(self):
		self._doc.reset()
			
	def oneToOne(self):
		self._doc.oneToOne()

		
		
	def onCellChanged(self, cell):
		self._queueRefresh()

	def onCellEvaluator(self, cell, oldEval, newEval):
		pass

	def onCellValidity(self, cell):
		pass
		
		

		
def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		
		



class MainApp (object):
	def __init__(self, documentRoot):
		self._documentRoot = None
		self._commandHistory = None
		self._bUnsavedData = False
		
		self._world = GSymWorld()
		
		self._docView = MainAppDocView( self )

		
		
		
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



		
		# VIEW MENU
		
		viewMenu = JMenu( 'View' )
		viewMenu.add( _action( 'Show LISP window', self._onShowLisp ) )
		
		
		
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
		
		

		self._frame = JFrame( 'gSym' )
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._frame.setJMenuBar( menuBar )
		
		self._frame.add( self._docView.getComponent() )
		
		self._frame.pack()
		
		
		
		
		
		#
		# LISP window
		#
		self._lispDocView = None
		self._lispFrame = None
		self._bLispWindowVisible = False

		
		# Set the document
		self.setDocument( documentRoot )
		
		

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
		


	def setDocument(self, documentRoot):
		self._documentRoot = documentRoot

		self._commandHistory = CommandHistory()
		if self._documentRoot is not None:
			self._commandHistory.track( self._documentRoot )
		self._commandHistory.changedSignal.connect( self._onCommandHistoryChanged )
		self._bUnsavedData = False
		
	
		self._actionsMenu.removeAll()
		
		contentHandler = GSymDocumentViewContentHandler( self._commandHistory )
		self._docView.setDocumentContent( documentRoot, contentHandler )
		
		self._setLispDocument()
			
			
	def _setLispDocument(self):
		if self._lispDocView is not None:		
			lispContentHandler = GSymDocumentLISPViewContentHandler( self._commandHistory )
			self._lispDocView.setDocumentContent( self._documentRoot, lispContentHandler )

			
			
			
			
	def _onCommandHistoryChanged(self, commandHistory):
		self._bUnsavedData = True

		
		
	def _onNewEmpty(self):
		bProceed = True
		if self._bUnsavedData:
			response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'New Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'New', 'Cancel' ], 'Cancel' )
			bProceed = response == JOptionPane.YES_OPTION
		if bProceed:
			documentRoot = None
			self.setDocument( documentRoot )


	def registerNewDocumentFactory(self, menuLabel, newDocFn):
		def _onNew():
			bProceed = True
			if self._bUnsavedData:
				response = JOptionPane.showOptionDialog( self._frame, 'You have not saved your work. Proceed?', 'New Project', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'New', 'Cancel' ], 'Cancel' )
				bProceed = response == JOptionPane.YES_OPTION
			if bProceed:
				content = newDocFn()
				if content is not None:
					documentRoot = newDocument( content )
					self.setDocument( documentRoot )

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
								documentRoot = readSX( f )
								documentRoot = DMList( documentRoot )
								self.setDocument( documentRoot )
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
							content = importFn( filename )
							if content is not None:
								documentRoot = newDocument( content )
								self.setDocument( documentRoot )
		
		self._importMenu.add( _action( menuLabel, _onImport ) )

		
		
		
		
	def _writeFile(self, filename):
		f = open( filename, 'w' )
		if f is not None:
			writeSX( f, self._documentRoot )
			f.close()
			self._bUnsavedData = False


	def _onUndo(self, sender):
		if self._commandHistory.canUndo():
			self._commandHistory.undo()

	def _onRedo(self, sender):
		if self._commandHistory.canRedo():
			self._commandHistory.redo()


		

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
			
			
			self._lispDocView = MainAppDocView( self )
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


	def _onScriptWindowMenuItem(self):
		self._bScriptWindowVisible = not self._bScriptWindowVisible
		if self._bScriptWindowVisible:
			self._scriptWindow.show()
		else:
			self._scriptWindow.hide()


	#def _p_onScriptPreCommand(self, console, code):
		#self._commandHistory.freeze()

	#def _p_onScriptPostCommand(self, console, code):
		#self._commandHistory.thaw()




	


