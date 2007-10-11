##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import sys

from Britefury.I18n import i18n
i18n.initialise()



from Britefury.FileIO.IOXml import *

from Britefury.SheetGraph.SheetGraph import *

from Britefury.MainApp.MainApp import MainApp





if __name__ == '__main__':
	import pygtk
	pygtk.require( '2.0' )
	import gtk



	documentRoot = None

	if len( sys.argv ) == 2:
		filename = sys.argv[1]

		f = open( filename, 'r' )
		if f is not None:
			doc = InputXmlDocument()
			doc.parseFile( f )
			contentNode = doc.getContentNode()
			if contentNode.isValid():
				rootXmlNode = contentNode.getChild( 'doc_root' )
				if rootXmlNode.isValid():
					documentRoot = rootXmlNode.readObject()

	if documentRoot is None:
		documentRoot = MainApp.makeEmptyDocument()

	app = MainApp( documentRoot )

	gtk.main()
