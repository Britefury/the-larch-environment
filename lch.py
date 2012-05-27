##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import sys
import os

sys.path.append( os.path.join( os.getcwd(), 'larch' ) )
sys.path.append( os.path.join( os.getcwd(), 'bin' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'jericho-html-3.2.jar' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'svgSalamander.jar' ) )

from Britefury.app_larch import start_larch

if __name__ == '__main__':
	start_larch()
