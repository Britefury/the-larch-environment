##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from BritefuryJ.Controls import *
from BritefuryJ.DocPresent.Combinators.ContextMenu import *

from GSymCore.PresTemplate.TemplateEditor.TextNodeEditor import PargraphRequest
from GSymCore.PresTemplate.TemplateEditor.PythonExpr import PythonExprRequest


def templateContextMenuFactory(element, menu):
	rootElement = element.getRootElement()

	
	def makeStyleFn(style):
		def _onLink(link, event):
			caret = rootElement.getCaret()
			if caret.isValid():
				caret.getElement().postTreeEvent( PargraphRequest( style ) )
		return _onLink
	
	normalStyle = Hyperlink( 'Normal', makeStyleFn( 'normal' ) )
	h1Style = Hyperlink( 'H1', makeStyleFn( 'h1' ) )
	h2Style = Hyperlink( 'H2', makeStyleFn( 'h2' ) )
	h3Style = Hyperlink( 'H3', makeStyleFn( 'h3' ) )
	h4Style = Hyperlink( 'H4', makeStyleFn( 'h4' ) )
	h5Style = Hyperlink( 'H5', makeStyleFn( 'h5' ) )
	h6Style = Hyperlink( 'H6', makeStyleFn( 'h6' ) )
	titleStyle = Hyperlink( 'Title', makeStyleFn( 'title' ) )
	styles = ControlsRow( [ normalStyle, h1Style, h2Style, h3Style, h4Style, h5Style, h6Style, titleStyle ] )
	menu.add( SectionColumn( [ SectionTitle( 'Style' ), styles ] ).alignHExpand() )
	
	
	def _onPythonExpr(link, event):
		caret = rootElement.getCaret()
		if caret.isValid():
			caret.getElement().postTreeEvent( PythonExprRequest() )

	newExpr = Hyperlink( 'Python expression', _onPythonExpr )
	codeControls = ControlsRow( [ newExpr ] )
	menu.add( SectionColumn( [ SectionTitle( 'Code' ), codeControls ] ).alignHExpand() )

	return True
