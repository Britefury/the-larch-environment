##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from copy import deepcopy

from BritefuryJ.DocModel import DMNode

from BritefuryJ.LSpace.Clipboard import *
from BritefuryJ.LSpace.TextFocus import TextSelection
from BritefuryJ.LSpace.StyleParams import *
from BritefuryJ.LSpace import *

from BritefuryJ.Editor.Sequential import SequentialClipboardHandler, SelectionEditTreeEvent


from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingEditor

from LarchTools.PythonTools.SWYN import Schema




def isTopLevelFragment(fragment):
	model = fragment.model
	return isinstance( model, DMNode )  and  model.isInstanceOf( Schema.SWYNRegEx )


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


