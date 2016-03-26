##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import sys
import os

sys.path.append( os.path.join( os.getcwd(), 'larch' ) )
sys.path.append( os.path.join( os.getcwd(), 'bin' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'jsoup-1.7.3.jar' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'svgSalamander.jar' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'jeromq-0.3.4.jar' ) )

from LarchCore.app_larch import start_larch

if __name__ == '__main__':
	start_larch()
