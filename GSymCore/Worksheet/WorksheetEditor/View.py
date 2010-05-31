##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import os
from datetime import datetime

from java.awt.event import KeyEvent

from java.util.regex import Pattern

from javax.swing import AbstractAction
from javax.swing import JPopupMenu, JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from Britefury.Dispatch.ObjectNodeMethodDispatch import ObjectNodeDispatchMethod

from Britefury.gSym.View.GSymView import GSymViewObjectNodeDispatch

from Britefury.gSym.View.EditOperations import replace, replaceWithRange, replaceNodeContents, append, prepend, insertElement, insertRange, insertBefore, insertRangeBefore, insertAfter, insertRangeAfter


from Britefury.Util.NodeUtil import *

from BritefuryJ.AttributeTable import *

from BritefuryJ.DocPresent.Browser import Location
from BritefuryJ.DocPresent.StyleSheet import PrimitiveStyleSheet
from BritefuryJ.DocPresent import *

from BritefuryJ.GSym import GSymPerspective, GSymSubject, GSymRelativeLocationResolver
from BritefuryJ.GSym.View import PyGSymViewFragmentFunction


from GSymCore.Languages.Python25 import Python25

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet.WorksheetEditor.WorksheetEditorStyleSheet import WorksheetEditorStyleSheet
from GSymCore.Worksheet.WorksheetEditor.NodeEditor import *



_textInteractor = TextInteractor()



class WorksheetEditor (GSymViewObjectNodeDispatch):
	@ObjectNodeDispatchMethod( Schema.Worksheet )
	def Worksheet(self, ctx, styleSheet, inheritedState, node, title, contents):
		contentViews = ctx.mapPresentFragment( contents, styleSheet, inheritedState )
		emptyLine = PrimitiveStyleSheet.instance.paragraph( [ PrimitiveStyleSheet.instance.text( '' ) ] )
		emptyLine.addTreeEventListener( TextEditEvent, EmptyTreeEventListener.newListener() )
		contentViews += [ emptyLine ]

		w = styleSheet.worksheet( title, contentViews )
		w.addTreeEventListener( InsertPythonCodeEvent, WorksheetTreeEventListener.newListener() )
		return w
	
	
	@ObjectNodeDispatchMethod( Schema.Paragraph )
	def Paragraph(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.paragraph( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
	
	@ObjectNodeDispatchMethod( Schema.H1 )
	def H1(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h1( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
	
	@ObjectNodeDispatchMethod( Schema.H2 )
	def H2(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h2( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
	
	@ObjectNodeDispatchMethod( Schema.H3 )
	def H3(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h3( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
			
	@ObjectNodeDispatchMethod( Schema.H4 )
	def H4(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h4( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
	
	@ObjectNodeDispatchMethod( Schema.H5 )
	def H5(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h5( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p
	
	@ObjectNodeDispatchMethod( Schema.H6 )
	def H6(self, ctx, styleSheet, inheritedState, node, text):
		p = styleSheet.h6( text )
		p.addTreeEventListener( TextEditEvent, TextTreeEventListener.newListener() )
		p.addInteractor( _textInteractor )
		return p


	
	@ObjectNodeDispatchMethod( Schema.PythonCode )
	def PythonCode(self, ctx, styleSheet, inheritedState, node, code, showCode, codeEditable, showResult):
		codeView = ctx.presentFragmentWithPerspective( code, Python25.python25EditorPerspective )
		p = styleSheet.pythonCode( codeView )
		return p





class WorksheetEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() )
		else:
			return None
	

	
_viewFn = PyGSymViewFragmentFunction( WorksheetEditor() )
perspective = GSymPerspective( _viewFn, WorksheetEditorStyleSheet.instance, AttributeTable.instance, None, WorksheetEditorRelativeLocationResolver() )

	