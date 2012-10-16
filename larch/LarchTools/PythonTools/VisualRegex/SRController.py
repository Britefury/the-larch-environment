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


from BritefuryJ.Editor.SyntaxRecognizing import SyntaxRecognizingController

from LarchTools.PythonTools.VisualRegex import Schema




def isTopLevelFragment(fragment):
	model = fragment.model
	return isinstance( model, DMNode )  and  model.isInstanceOf( Schema.PythonRegEx )


class VisualRegexSyntaxRecognizingController (SyntaxRecognizingController):
	def isClipboardEditLevelFragmentView(self, fragment):
		return isTopLevelFragment( fragment )




VisualRegexSyntaxRecognizingController.instance = VisualRegexSyntaxRecognizingController( 'VREEdit' )


