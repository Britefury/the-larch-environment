##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *

from GSymCore.Utils.LinkHeader import LinkHeaderStyleSheet
from GSymCore.Utils.Title import TitleBarStyleSheet
from GSymCore.Utils.TabbedBox import TabbedBoxStyleSheet



class GSymAppViewerStyleSheet (StyleSheet):
	def __init__(self):
		super( GSymAppViewerStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'linkHeaderStyle', LinkHeaderStyleSheet.instance )
		self.initAttr( 'titleBarStyle', TitleBarStyleSheet.instance )
		self.initAttr( 'tabbedBoxStyle', TabbedBoxStyleSheet.instance )
		
		self.initAttr( 'appDocumentControlsAttrs', AttributeValues( border=FilledBorder( 5.0, 5.0, 5.0, 5.0, Color( 0.9, 0.9, 0.9 ) ), hboxSpacing=20.0 ) )
		self.initAttr( 'dcumentListTableAttrs', AttributeValues( tableColumnSpacing=15.0, tableColumnExpand=False, tableRowSpacing=5.0, tableRowExpand=False ) )
		
		self.initAttr( 'controlsPadding', 5.0 )

		self.initAttr( 'appDocRightPadding', 30.0 )
		self.initAttr( 'separatingLinePaddingX', 15.0 )
		self.initAttr( 'separatingLinePaddingY', 3.0 )
	
		
	def newInstance(self):
		return ProjectEditorStyleSheet()
	
	
	
	def withPrimitiveStyleSheet(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withControlsStyleSheet(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withLinkheaderStyleSheet(self, linkHeaderStyle):
		return self.withAttrs( linkHeaderStyle=linkHeaderStyle )
	
	def withTitlebarStyleSheet(self, titleBarStyle):
		return self.withAttrs( titleBarStyle=titleBarStyle )
	
	def withTabbedBoxStyleSheet(self, tabbedBoxStyle):
		return self.withAttrs( tabbedBoxStyle=tabbedBoxStyle )
	
	
	def withAppDocumentControlsAttrs(self, appDocumentControlsAttrs):
		return self.withAttrs( appDocumentControlsAttrs=appDocumentControlsAttrs )
	
	def withPackageNameAttrs(self, attrs):
		return self.withAttrs( packageNameAttrs=packageNameAttrs )
	
	
	def withAppDocRightPadding(self, appDocRightPadding):
		return self.withAttrs( appDocRightPadding=appDocRightPadding )
	
	
	
	@StyleSheetDerivedPyAttrFn
	def appDocumentControlsStyle(self):
		return self['primitiveStyle'].withAttrValues( self['appDocumentControlsAttrs'] )
	
	@StyleSheetDerivedPyAttrFn
	def documentListTableStyle(self):
		return self['primitiveStyle'].withAttrValues( self['dcumentListTableAttrs'] )
	
	
	

	def appState(self, openDocuments, onNew, onOpen):
		def _onNew(link, event):
			onNew()
			return True
		
		def _onOpen(link, evnet):
			onOpen()
			return True
		
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		linkHeaderStyle = self['linkHeaderStyle']
		titleBarStyle = self['titleBarStyle']
		tabbedBoxStyle = self['tabbedBoxStyle']
		
		appDocumentControlsStyle = self.appDocumentControlsStyle()		
		documentListTableStyle = self.documentListTableStyle()

		systemLink = controlsStyle.link( 'SYSTEM PAGE', 'system' ).getElement()
		linkHeader = linkHeaderStyle.linkHeaderBar( [ systemLink ] )
		
		title = titleBarStyle.titleBar( 'gSym' )
		
		controlsPadding = self['controlsPadding']
		newLink = controlsStyle.link( 'NEW', _onNew ).getElement()
		openLink = controlsStyle.link( 'OPEN', _onOpen ).getElement()
		controlsBox = appDocumentControlsStyle.hbox( [ newLink.padX( controlsPadding ), openLink.padX( controlsPadding ) ] )
		controlsBorder = appDocumentControlsStyle.border( controlsBox )

		
		lineStyle = primitiveStyle.withShapePainter( FillPainter( Color( 32, 87, 147 ) ) )
		openDocumentsSeparator = lineStyle.rectangle( 0.0, 1.0 ).alignHExpand().pad( self['separatingLinePaddingX'], self['separatingLinePaddingY'] ).alignHExpand()
		
		docListBox = documentListTableStyle.rgrid( openDocuments )

		openDocumentsContentsBox = primitiveStyle.vbox( [ controlsBorder.pad( 2.0, 2.0 ), openDocumentsSeparator, docListBox.pad( 10.0, 2.0 ) ] )
		
		openDocumentsBox = tabbedBoxStyle.tabbedBox( 'Documents', openDocumentsContentsBox )
		
		contentBox = primitiveStyle.vbox( [ linkHeader, title, openDocumentsBox.pad( 10.0, 10.0 ).alignHLeft() ] )
		
		return contentBox.alignHExpand()
	
	
	def appDocument(self, name, location, onSave, onSaveAs):
		def _onSave(link, event):
			onSave()
			return True
			
		def _onSaveAs(link, event):
			onSaveAs()
			return True
			
		
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		
		docLink = controlsStyle.link( name, location ).getElement().padX( 0.0, self['appDocRightPadding'] )
		saveLink = controlsStyle.link( 'SAVE', _onSave ).getElement()
		saveAsLink = controlsStyle.link( 'SAVE AS', _onSaveAs ).getElement()

		return primitiveStyle.gridRow( [ docLink, saveLink, saveAsLink ] )
		
	

GSymAppViewerStyleSheet.instance = GSymAppViewerStyleSheet()
	
	
	