##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

from BritefuryJ.DocPresent.Clipboard import *
from BritefuryJ.DocPresent.Selection import TextSelection
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StreamValue import StreamValueBuilder
from BritefuryJ.DocPresent import *

from BritefuryJ.Editor.Sequential import SequentialClipboardHandler, SelectionEditTreeEvent


from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingEditor

from LarchTools.PythonTools.SWYN import Schema




def isTopLevelFragment(fragment):
	raise NotImplementedError


class SWYNSyntaxRecognizingEditor (SyntaxRecognizingEditor):
	def __init__(self):
		super( SWYNSyntaxRecognizingEditor, self ).__init__()


	def getName(self):
		return 'SWYNEdit'


	def isClipboardEditLevelFragmentView(self, fragment):
		return isTopLevelFragment( fragment )


	def copyStructuralValue(self, x):
		return deepcopy( x )




SWYNSyntaxRecognizingEditor.instance = SWYNSyntaxRecognizingEditor()


