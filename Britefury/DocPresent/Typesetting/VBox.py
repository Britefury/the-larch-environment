##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
from Britefury.DocPresent.Typesetting.Box import Box


class VBox (Box):
	def __init__(self, contents, glueType):
		w = reduce( lambda a, b: a + b.reqWidth(),  contents, 0.0 )
		h = reduce( lambda a, b: max( a, b.height() ),  contents, 0.0 )
		d = reduce( lambda a, b: max( a, b.depth() ),  contents, 0.0 )
		super( VBox, self ).__init__( w, h, d, contents, glueType )
	


