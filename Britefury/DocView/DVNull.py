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


from Britefury.Math.Math import Colour3f

from Britefury.Sheet.Sheet import *

from Britefury.DocView.DVBorderNode import *

from Britefury.DocPresent.Toolkit.DTBox import DTBox
from Britefury.DocPresent.Toolkit.DTLabel import DTLabel
from Britefury.DocPresent.Toolkit.DTDirection import DTDirection

#from Britefury.DocViewBehavior.DVBCreateExpressionBehavior import *


class DVNull (DVBorderNode):
	docNodeClass = type( None )


	#behaviors = [ DVBCreateExpressionBehavior() ]



	def __init__(self, docNode, view, docNodeKey):
		super( DVNull, self ).__init__( docNode, view, docNodeKey )
		self.widget.child = DTLabel( '<null>', font='Sans italic 11', colour=Colour3f( 0.7, 0.0, 0.0 ) )





	def startEditing(self):
		self.makeCurrent()
