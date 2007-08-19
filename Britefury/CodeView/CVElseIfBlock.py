##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import pygtk
pygtk.require( '2.0' )
import gtk

from Britefury.Sheet.Sheet import *
from Britefury.SheetGraph.SheetGraph import *

from Britefury.CodeViewTree.CVTElseIfBlock import CVTElseIfBlock

from Britefury.CodeView.CVStatement import *
from Britefury.CodeView.CVIfBlock import *

from Britefury.DocPresent.Toolkit.DTLabel import DTLabel



class CVElseIfBlock (CVIfBlock):
	treeNodeClass = CVTElseIfBlock


	treeNode = SheetRefField( CVTElseIfBlock )




	def _o_makeTitleLabel(self):
		return DTLabel( markup=_( 'E<span size="small">LIF</span>' ), font='Sans bold 11', colour=Colour3f( 0.0, 0.5, 0.0 ) )



