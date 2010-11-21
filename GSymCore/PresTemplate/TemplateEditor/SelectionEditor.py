##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from BritefuryJ.DocPresent.Clipboard import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent import *

from BritefuryJ.SequentialEditor import SequentialClipboardHandler, SelectionEditTreeEvent

from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder, StreamValue

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod




class TemplateSelectionEditTreeEvent (SelectionEditTreeEvent):
	def __init__(self, clipboardHandler, sourceElement):
		super( TemplateSelectionEditTreeEvent, self ).__init__( clipboardHandler, sourceElement )

		


class TemplateClipboardHandler (SequentialClipboardHandler):
	def __init__(self):
		super( TemplateClipboardHandler, self ).__init__()
		
		
	def isEditLevelFragmentView(self, fragment):
		return True
	
	
	def copyStructuralValue(self, x):
		return x.deepCopy()
	
	
	def createSelectionEditTreeEvent(self, sourceElement):
		return TemplateSelectionEditTreeEvent( self, sourceElement )

