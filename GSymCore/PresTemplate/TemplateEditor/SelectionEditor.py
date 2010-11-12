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

from BritefuryJ.SequentialEditor import SequentialEditHandler, SequentialBuffer, SelectionEditTreeEvent

from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder, StreamValue

from Britefury.gSym.View.TreeEventListenerObjectDispatch import TreeEventListenerObjectDispatch, ObjectDispatchMethod




class TemplateBuffer (SequentialBuffer):
	pass



_templateBufferDataFlavor = LocalDataFlavor( TemplateBuffer )



		
class TemplateSelectionEditTreeEvent (SelectionEditTreeEvent):
	def __init__(self, editHandler, sourceElement):
		super( TemplateSelectionEditTreeEvent, self ).__init__( editHandler, sourceElement )

		


class TemplateEditHandler (SequentialEditHandler):
	def __init__(self):
		super( TemplateEditHandler, self ).__init__( _templateBufferDataFlavor )
		
		
	def isEditLevelFragmentView(self, fragment):
		return True
	
	
	def createSelectionBuffer(self, stream):
		return TemplateBuffer( stream )
	
	
	def filterTextForImport(self, text):
		return text
		
		
	def copyStructuralValue(self, x):
		return x.deepCopy()
	
	
	def createSelectionEditTreeEvent(self, sourceElement):
		return TemplateSelectionEditTreeEvent( self, sourceElement )
	
	
	def canShareSelectionWith(self, editHandler):
		return False

