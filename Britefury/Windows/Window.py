##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys
import os

from datetime import datetime

from java.lang import Runnable
from javax.swing import AbstractAction, Action, TransferHandler, KeyStroke, BoxLayout, BorderFactory
from javax.swing import JComponent, JFrame, JMenuItem, JMenu, JMenuBar, JMenuItem, JPopupMenu, JOptionPane, JFileChooser, JOptionPane, JTextField, JLabel, JPanel
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Dimension, Color, KeyboardFocusManager
from java.awt.event import WindowListener, ActionListener, ActionEvent, KeyEvent
from java.beans import PropertyChangeListener


from BritefuryJ.ChangeHistory import ChangeHistory, ChangeHistoryListener

from BritefuryJ.Utils.Profile import ProfileTimer

from BritefuryJ.DocModel import DMIOReader, DMIOWriter, DMNode

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.Browser import *
from BritefuryJ.DocPresent.StyleParams import *


from BritefuryJ.Projection import Subject


from Britefury.Util.Abstract import abstractmethod


from Britefury.Kernel.World import World
from Britefury.Kernel.Document import Document



_bProfile = True





def _action(name, f):
	class Act (AbstractAction):
		def actionPerformed(action, event):
			f()
	return Act( name )
	
		
		

		
# Transfer action listener
class _TransferActionListener (ActionListener):
	def __init__(self):
		pass
		
	def actionPerformed(self, e):
		manager = KeyboardFocusManager.getCurrentKeyboardFocusManager()
		focusOwner = manager.getPermanentFocusOwner()
		if focusOwner is not None:
			action = e.getActionCommand()
			a = focusOwner.getActionMap().get( action )
			if a is not None:
				a.actionPerformed( ActionEvent( focusOwner, ActionEvent.ACTION_PERFORMED, None ) )
				
				

		
class _ModelSubject (Subject):
	def __init__(self, focus):
		super( _ModelSubject, self ).__init__( None )
		self._focus = focus

		
	def getFocus(self):
		return self._focus
	
	def getPerspective(self):
		return None
	
	def getTitle(self):
		return '[model]'

	
	

class Window (object):
	def __init__(self, windowManager, commandConsoleFactory, location=Location( '' )):
		self._windowManager = windowManager
		
		class _BrowserListener (TabbedBrowser.TabbedBrowserListener):
			def createNewBrowserWindow(_self, location):
				self._createNewWindow( location )
				
				
		self._browser = TabbedBrowser( self._windowManager._browserContext.getPageLocationResolver(), _BrowserListener(), location, commandConsoleFactory )
		self._browser.getComponent().setPreferredSize( Dimension( 800, 600 ) )

		
		class _ChangeHistoryListener (ChangeHistoryListener):
			def onChangeHistoryChanged(_self, history):
				self._onChangeHistoryChanged( history )
		
		self._browser.setChangeHistoryListener( _ChangeHistoryListener() )
		
		
		self.onCloseRequest = None
		
		
		
		# NEW MENU
		
		newMenu = JMenu( 'New' )
		newMenu.add( _action( 'New tab', self._onNewTab ) )
		newMenu.add( _action( 'New window', self._onNewWindow ) )
		
		
		
		# EDIT MENU
		
		transferActionListener = _TransferActionListener()
		
		editMenu = JMenu( 'Edit' )
		
		self._editUndoItem = JMenuItem( 'Undo' )
		undoAction = _action( 'undo', self._onUndo )
		self._editUndoItem.setActionCommand( undoAction.getValue( Action.NAME ) )
		self._editUndoItem.addActionListener( undoAction )
		self._editUndoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK ) )
		self._editUndoItem.setMnemonic( KeyEvent.VK_U )
		editMenu.add( self._editUndoItem )

		self._editRedoItem = JMenuItem( 'Redo' )
		redoAction = _action( 'redo', self._onRedo )
		self._editRedoItem.setActionCommand( redoAction.getValue( Action.NAME ) )
		self._editRedoItem.addActionListener( redoAction )
		self._editRedoItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y, ActionEvent.CTRL_MASK ) )
		self._editRedoItem.setMnemonic( KeyEvent.VK_R )
		editMenu.add( self._editRedoItem )

		
		editMenu.addSeparator()
		
		editCutItem = JMenuItem( 'Cut' )
		editCutItem.setActionCommand( TransferHandler.getCutAction().getValue( Action.NAME ) )
		editCutItem.addActionListener( transferActionListener )
		editCutItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) )
		editCutItem.setMnemonic( KeyEvent.VK_T )
		editMenu.add( editCutItem )
		
		editCopyItem = JMenuItem( 'Copy' )
		editCopyItem.setActionCommand( TransferHandler.getCopyAction().getValue( Action.NAME ) )
		editCopyItem.addActionListener( transferActionListener )
		editCopyItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) )
		editCopyItem.setMnemonic( KeyEvent.VK_C )
		editMenu.add( editCopyItem )
		
		editPasteItem = JMenuItem( 'Paste' )
		editPasteItem.setActionCommand( TransferHandler.getPasteAction().getValue( Action.NAME ) )
		editPasteItem.addActionListener( transferActionListener )
		editPasteItem.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) )
		editPasteItem.setMnemonic( KeyEvent.VK_P )
		editMenu.add( editPasteItem )
		

		
		
		# VIEW MENU
		
		viewMenu = JMenu( 'View' )
		viewMenu.add( _action( 'View document model', self._onViewDocModel ) )
		viewMenu.add( _action( 'Show element tree explorer', self._onShowElementTreeExplorer ) )
		self._showUndoHistoryItem = JMenuItem( 'Show undo history' )
		self._showUndoHistoryItem.addActionListener( _action( 'Show undo history', self._onShowUndoHistory ) )
		viewMenu.add( self._showUndoHistoryItem )
		viewMenu.add( _action( 'Reset', self._onReset ) )
		viewMenu.add( _action( '1:1', self._onOneToOne ) )
		
		
		
		menuBar = JMenuBar()
		menuBar.add( newMenu )
		menuBar.add( editMenu )
		menuBar.add( viewMenu )

		
		
		# MAIN PANEL
		
		windowPanel = JPanel()
		windowPanel.setLayout( BoxLayout( windowPanel, BoxLayout.Y_AXIS ) )
		windowPanel.add( self._browser.getComponent() )
		
		
		
		
		# WINDOW
		
		class _WindowLister (WindowListener):
			def windowActivated(listenerSelf, event):
				pass

			def windowClosed(listenerSelf, event):
				pass

			def windowClosing(listenerSelf, event):
				if self.onCloseRequest is not None:
					self.onCloseRequest( self )
			
			def windowDeactivated(listenerSelf, event):
				pass
			
			def windowDeiconified(listenerSelf, event):
				pass
			
			def windowIconified(listenerSelf, event):
				pass
			
			def windowOpened(listenerSelf, event):
				pass
			

		self._frame = JFrame( 'The Larch Environment' )
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._frame.setJMenuBar( menuBar )
		
		self._frame.add( windowPanel )
		self._frame.addWindowListener( _WindowLister() )
		self._frame.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE )
		
		self._frame.pack()
		
		
		# Cause command history controls to refresh
		self._onChangeHistoryChanged( None )
		
		
		

	
	def show(self):
		self._frame.setVisible( True )
		
	def getFrame(self):
		return self._frame
	
	
	def close(self):
		self._frame.dispose()
		
		
	def setCloseRequestListener(self, listener):
		self.onCloseRequest = listener

		
		
		
		
	def _onChangeHistoryChanged(self, changeHistory):
		if changeHistory is not None:
			self._editUndoItem.setEnabled( changeHistory.canUndo() )
			self._editRedoItem.setEnabled( changeHistory.canRedo() )
			self._showUndoHistoryItem.setEnabled( True )
		else:
			self._editUndoItem.setEnabled( False )
			self._editRedoItem.setEnabled( False )
			self._showUndoHistoryItem.setEnabled( False )
			
		
		
			
			
		
		
		
	
	def getWorld(self):
		return self._windowManager.getWorld()

	
	def getBrowserContext(self):
		return self._windowManager.getBrowserContext()

	
	
	
	def _onNewTab(self):
		self._browser.openLocationInNewTab( '' )
	
	
	def _onNewWindow(self):
		self._createNewWindow( Location( '' ) )
		
		
	def _createNewWindow(self, location):
		self._windowManager._createNewWindow( location )
	
	
	
	def _onUndo(self):
		changeHistoryController = self._browser.getChangeHistoryController()
		if changeHistoryController.canUndo():
			changeHistoryController.undo()

	def _onRedo(self):
		changeHistoryController = self._browser.getChangeHistoryController()
		if changeHistoryController.canRedo():
			changeHistoryController.redo()


		

	def _onViewDocModel(self):
		currentLoc = self._browser.getCurrentBrowserLocation()
		browserContext = self._windowManager._browserContext
		focus = browserContext.resolveLocationAsSubject( currentLoc ).getFocus()
		modelSubject = _ModelSubject( focus )
		modelLoc = browserContext.getLocationForObject( modelSubject )
		self._browser.openLocationInNewWindow( modelLoc )
	
	
	def _onShowElementTreeExplorer(self):
		currentTab = self._browser.getCurrentBrowser()
		treeExplorer = currentTab.getRootElement().treeExplorer()
		location = self._windowManager._browserContext.getLocationForObject( treeExplorer )
		self._browser.openLocationInNewWindow( location )


	def _onShowUndoHistory(self):
		changeHistoryController = self._browser.getChangeHistoryController()
		if changeHistoryController is not None:
			location = self._windowManager._browserContext.getLocationForObject( changeHistoryController )
			self._browser.openLocationInNewWindow( location )


	def _onReset(self):
		self._browser.viewportReset()

	def _onOneToOne(self):
		self._browser.viewportOneToOne()




	


