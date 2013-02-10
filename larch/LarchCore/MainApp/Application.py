##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from copy import copy

from javax.swing import JOptionPane

from BritefuryJ.Incremental import IncrementalValueMonitor

from Britefury.Kernel.Document import Document

from LarchCore.PythonConsole import Console



class AppState (object):
	def __init__(self):
		self._incr = IncrementalValueMonitor( self )
		
		self._openDocuments = []
		self._docToAppDoc = {}
		self._documentIDCounter = 1
		self._consoles = []
		
		
	def getOpenDocuments(self):
		self._incr.onAccess()
		return copy( self._openDocuments )
	
	def registerOpenDocument(self, document):
		self._documentIDCounter += 1
		appDocument = AppDocument( document, self )
		self._openDocuments.append( appDocument )
		self._docToAppDoc[document] = appDocument
		self._incr.onChanged()
		return appDocument

	def unregisterDocument(self, document):
		appDocument = self._docToAppDoc[document]
		del self._docToAppDoc[document]
		self._openDocuments.remove( appDocument )
		self._incr.onChanged()



	def refreshDocumentFile(self, world, inputPath, outputPath):
		# Read the document
		try:
			document = Document.readFile( world, inputPath )
		except:
			# Failed
			return False

		success = True

		# Register so that the import hooks can pick up its contents
		self.registerOpenDocument( document )

		# Get a list of modules that it defines
		try:
			moduleNames = document.contents.moduleNames
		except AttributeError:
			pass
		else:
			# Import each module
			for name in moduleNames:
				try:
					__import__( name )
				except:
					# Import failed; refresh was not successful
					success = False
					# Don't read any more modules
					break

			if success:
				# Successful; save into the output path
				document.saveAs( outputPath )

		# Close the document and un-register
		document.close()
		self.unregisterDocument( document )

		return success



	def hasUnsavedData(self):
		for doc in self._openDocuments:
			if doc.hasUnsavedData():
				return True
		return False


	def onCloseRequest(self, windowManager, window):
		if self.hasUnsavedData():
			response = JOptionPane.showOptionDialog( window.frame,
				'You have not saved your work. Close anyway?', 'Unsaved data', JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, None, [ 'Close', 'Cancel' ], 'Cancel' )
			return response == JOptionPane.YES_OPTION
		else:
			return True




	def getConsoles(self):
		self._incr.onAccess()
		return copy( self._consoles )
	
	def addConsole(self, console):
		self._consoles.append( console )
		self._incr.onChanged()
	
		
	
class AppDocument (object):
	def __init__(self, doc, appState):
		self._incr = IncrementalValueMonitor( self )
		
		self._doc = doc
		self.__appState = appState
		
		
		
	def getName(self):
		self._incr.onAccess()
		return self._doc.getDocumentName()
	
	def getDocument(self):
		self._incr.onAccess()
		return self._doc
	

	def hasUnsavedData(self):
		return self._doc.hasUnsavedData()


	@property
	def appState(self):
		return self.__appState
		
	

class AppConsole (object):
	def __init__(self, index):
		self._incr = IncrementalValueMonitor( self )
		
		self._index = index
		self._console = Console.Console( '<console%d>'  %  ( index, ) )


	def subject(self, enclosingSubject):
		return Console.ConsoleSubject( self._console, enclosingSubject )
		
		
	def getIndex(self):
		self._incr.onAccess()
		return self._index
	
	def getConsole(self):
		self._incr.onAccess()
		return self._console
		


