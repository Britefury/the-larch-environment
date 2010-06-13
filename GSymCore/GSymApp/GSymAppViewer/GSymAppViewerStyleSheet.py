##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod

from GSymCore.Utils.TabbedBox import TabbedBoxStyleSheet



class GSymAppViewerStyleSheet (StyleSheet):
	def __init__(self):
		super( GSymAppViewerStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'richTextStyle', RichTextStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'tabbedBoxStyle', TabbedBoxStyleSheet.instance )
		
		self.initAttr( 'appDocumentControlsAttrs', AttributeValues( border=FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ), hboxSpacing=20.0 ) )
		self.initAttr( 'dcumentListTableAttrs', AttributeValues( tableColumnSpacing=15.0, tableColumnExpand=False, tableRowSpacing=5.0, tableRowExpand=False ) )
		
		self.initAttr( 'controlsPadding', 5.0 )

		self.initAttr( 'appDocRightPadding', 30.0 )
	
		
	def newInstance(self):
		return ProjectEditorStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withRichTextStyleSheet(self, richTextStyle):
		return self.withAttrs( richTextStyle=richTextStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withTabbedBoxStyleSheet(self, tabbedBoxStyle):
		return self.withAttrs( tabbedBoxStyle=tabbedBoxStyle )
	
	
	def withAppDocumentControlsAttrs(self, appDocumentControlsAttrs):
		return self.withAttrs( appDocumentControlsAttrs=appDocumentControlsAttrs )
	
	def withPackageNameAttrs(self, attrs):
		return self.withAttrs( packageNameAttrs=packageNameAttrs )
	
	
	def withAppDocRightPadding(self, appDocRightPadding):
		return self.withAttrs( appDocRightPadding=appDocRightPadding )
	
	
	
	@DerivedAttributeMethod
	def appDocumentControlsStyle(self):
		return self['primitiveStyle'].withAttrValues( self['appDocumentControlsAttrs'] )
	
	@DerivedAttributeMethod
	def documentListTableStyle(self):
		return self['primitiveStyle'].withAttrValues( self['dcumentListTableAttrs'] )
	
	
	
	def _contentsList(self, controls, contentsLists, title):
		primitiveStyle = self['primitiveStyle']
		controlsPadding = self['controlsPadding']
		tabbedBoxStyle = self['tabbedBoxStyle']
		appDocumentControlsStyle = self.appDocumentControlsStyle()		
		documentListTableStyle = self.documentListTableStyle()

		controlsBox = appDocumentControlsStyle.hbox( [ c.padX( controlsPadding )   for c in controls ] )
		controlsBorder = appDocumentControlsStyle.border( controlsBox )

		openDocumentsSeparator = RichTextStyleSheet.instance.hseparator()
		
		docListBox = documentListTableStyle.rgrid( contentsLists )

		contentsBox = primitiveStyle.vbox( [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparator, docListBox.pad( 10.0, 2.0 ) ] )
		
		return tabbedBoxStyle.tabbedBox( title, contentsBox )
	
	

	def appState(self, openDocuments, consoles, onNewDoc, onOpenDoc, onNewConsole):
		def _onNewDoc(link, event):
			onNewDoc( link.getElement() )
		
		def _onOpenDoc(link, evnet):
			onOpenDoc( link.getElement() )
		
		def _onNewConsole(link, event):
			onNewConsole()
		
		
		primitiveStyle = self['primitiveStyle']
		richTextStyle = self['richTextStyle']
		controlsStyle = self['controlsStyle']
		
		appDocumentControlsStyle = self.appDocumentControlsStyle()		

		systemLink = controlsStyle.link( 'SYSTEM PAGE', Location( 'system' ) ).getElement()
		linkHeader = richTextStyle.linkHeaderBar( [ systemLink ] )
		
		title = richTextStyle.titleBar( 'gSym' )
		
		newLink = controlsStyle.link( 'NEW', _onNewDoc ).getElement()
		openLink = controlsStyle.link( 'OPEN', _onOpenDoc ).getElement()
		openDocumentsBox = self._contentsList( [ newLink, openLink ], openDocuments, 'Documents' )
		
		
		newConsoleLink = controlsStyle.link( 'NEW', _onNewConsole ).getElement()
		consolesBox = self._contentsList( [ newConsoleLink ], consoles, 'Python consoles' )
		
		
		contentBox = primitiveStyle.vbox( [ linkHeader, title, openDocumentsBox.pad( 10.0, 10.0 ).alignHLeft(), consolesBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()
	
	
	def appDocument(self, name, location, onSave, onSaveAs):
		def _onSave(link, event):
			onSave( link.getElement() )
			
		def _onSaveAs(link, event):
			onSaveAs( link.getElement() )
			
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		
		docLink = controlsStyle.link( name, location ).getElement().padX( 0.0, self['appDocRightPadding'] )
		saveLink = controlsStyle.link( 'SAVE', _onSave ).getElement()
		saveAsLink = controlsStyle.link( 'SAVE AS', _onSaveAs ).getElement()

		return primitiveStyle.gridRow( [ docLink, saveLink, saveAsLink ] )
		
	
	def appConsole(self, name, location):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		
		termLink = controlsStyle.link( name, location ).getElement().padX( 0.0, self['appDocRightPadding'] )

		return primitiveStyle.gridRow( [ termLink ] )
		
	

GSymAppViewerStyleSheet.instance = GSymAppViewerStyleSheet()
	
	
	