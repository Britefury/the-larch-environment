##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.StyleSheet import *




class LinkHeaderStyleSheet (StyleSheet):
	def __init__(self, prototype=None):
		if prototype is not None:
			super( LinkHeaderStyleSheet, self ).__init__( prototype )
		else:
			super( LinkHeaderStyleSheet, self ).__init__()
		
		primtiveStyle = PrimitiveStyleSheet.instance.withHBoxSpacing( 25.0 ).withBorder( EmptyBorder( 10.0, 10.0, 5.0, 1.0, None ) )
		self.initAttr( 'primitiveStyle', primtiveStyle )
		
		
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttr( 'primitiveStyle', primitiveStyle )
		
		
	def linkHeaderBar(self, links):
		primitiveStyle = self['primitiveStyle']
		return primitiveStyle.border( primitiveStyle.hbox( links ).alignHRight() ).alignHExpand()
	
	
LinkHeaderStyleSheet.instance = LinkHeaderStyleSheet()
