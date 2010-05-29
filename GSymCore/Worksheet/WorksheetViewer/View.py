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


from GSymCore.GSymApp import DocumentManagement

from GSymCore.Worksheet import Schema
from GSymCore.Worksheet.WorksheetStyleSheet import WorksheetStyleSheet





class WorksheetEditor (GSymViewObjectNodeDispatch):
	@ObjectNodeDispatchMethod( Schema.Worksheet )
	def Worksheet(self, ctx, styleSheet, inheritedState, node, title, contents):
		contents = ctx.mapPresentFragment( contents, styleSheet, inheritedState )

		return styleSheet.worksheet( title, contents )

	
	
class WorksheetEditorRelativeLocationResolver (GSymRelativeLocationResolver):
	def resolveRelativeLocation(self, enclosingSubject, locationIterator):
		if locationIterator.getSuffix() == '':
			return enclosingSubject.withTitle( 'WS: ' + enclosingSubject.getTitle() )
		else:
			return None
	

	
_viewFn = PyGSymViewFragmentFunction( WorksheetEditor() )
perspective = GSymPerspective( _viewFn, WorksheetStyleSheet.instance, AttributeTable.instance, None, WorksheetEditorRelativeLocationResolver() )

	