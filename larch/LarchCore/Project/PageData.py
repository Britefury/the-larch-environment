##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import os
from copy import deepcopy

from datetime import datetime

from javax.swing import JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter


from BritefuryJ.DocModel import DMNode

from BritefuryJ.Controls import *
from BritefuryJ.ChangeHistory import Trackable

from Britefury.Util.Abstract import abstractmethod

from LarchCore.Project.ProjectPage import ProjectPage



class PageData (object):
	def __init__(self, contents=None):
		self.__change_history__ = None
		if contents is None:
			self.contents = self.makeEmptyContents()
		else:
			self.contents = contents
		self._contentsPostInit( self.contents )
	
			
	@abstractmethod
	def makeEmptyContents(self):
		pass
	
	def _contentsPostInit(self, contents):
		if isinstance( contents, DMNode ):
			contents.realiseAsRoot()



	@abstractmethod
	def exportAsString(self, filename):
		pass


	@abstractmethod
	def __new_subject__(self, document, enclosingSubject, path, importName, title):
		pass

	
	def __getstate__(self):
		return { 'contents' : self.contents }

	def __setstate__(self, state):
		self.__change_history__ = None
		self.contents = state['contents']
		self._contentsPostInit( self.contents )
	
	def __copy__(self):
		T = type( self )
		return T( self.contents )
	
	def __deepcopy__(self, memo):
		T = type( self )
		return T( deepcopy( self.contents, memo ) )

	
	def __get_trackable_contents__(self):
		return [ self.contents ]
			
	


#
#
# PAGE FACTORIES
#
#

class _PageFactory (object):
	def __init__(self, menuLabelText, pageDataFn, defaultPageName):
		self.menuLabelText = menuLabelText
		self._pageDataFn = pageDataFn
		self._defaultPageName = defaultPageName
	
	
	def newPage(self):
		return ProjectPage( self._defaultPageName, self._pageDataFn() )



_pageFactories = []

def registerPageFactory(menuLabelText, pageDataFn, defaultPageName):
	_pageFactories.append( _PageFactory( menuLabelText, pageDataFn, defaultPageName ) )

def newPageMenu(handleNewPageFn):
	def _make_newPage(pageFactory):
		def newPage(menuItem):
			page = pageFactory.newPage()
			handleNewPageFn( page )
		return newPage
	items = []
	for pageFactory in _pageFactories:
		items.append( MenuItem.menuItemWithLabel( pageFactory.menuLabelText, _make_newPage( pageFactory ) ) )
	return VPopupMenu( items )




#
#
# PAGE IMPORTERS
#
#

class _PageImporter (object):
	def __init__(self, menuLabelText, fileType, filePattern, importPageDataFn):
		self.menuLabelText = menuLabelText
		self.fileType = fileType
		self.filePattern = filePattern
		self._importPageDataFn = importPageDataFn
	
	
	def importPage(self, pageName, filename):
		pageData = self._importPageDataFn( filename )
		if pageData is not None:
			return ProjectPage( pageName, pageData )
		else:
			return None



_pageImporters = []

def registerPageImporter(menuLabelText, fileType, filePattern, importPageDataFn):
	_pageImporters.append( _PageImporter( menuLabelText, fileType, filePattern, importPageDataFn ) )


def importPageMenu(component, handleImportedPageFn):
	def _make_importPage(pageImporter):
		def _import(actionEvent):
			openDialog = JFileChooser()
			openDialog.setFileFilter( FileNameExtensionFilter( pageImporter.fileType, [ pageImporter.filePattern ] ) )
			response = openDialog.showDialog( component, 'Import' )
			if response == JFileChooser.APPROVE_OPTION:
				sf = openDialog.getSelectedFile()
				if sf is not None:
					filename = sf.getPath()
					if filename is not None:
						pageName = os.path.splitext( filename )[0]
						pageName = os.path.split( pageName )[1]
						t1 = datetime.now()
						page = pageImporter.importPage( pageName, filename )
						t2 = datetime.now()
						if page is not None:
							print 'ProjectEditor.View: IMPORT TIME = %s'  %  ( t2 - t1, )
							handleImportedPageFn( page )
		return _import

	items = []
	for pageImporter in _pageImporters:
		items.append( MenuItem.menuItemWithLabel( pageImporter.menuLabelText, _make_importPage( pageImporter ) ) )
	return VPopupMenu( items )


