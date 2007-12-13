##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************
import os


print 'Manifest insertion script'
for dirname, subdirs, filenames in os.walk( '.' ):
    for filename in filenames:
        if filename.endswith( '.manifest' ):
            manifest = os.path.join( dirname, filename )
            target = os.path.join( dirname, filename.replace( '.manifest', '' ) )
            print 'Adding manifest to %s...' % ( target, )
            command = 'mt.exe -outputresource:%s;#2 -manifest %s' % ( target, manifest )
            pipe = os.popen( command )
            print pipe.read()
            pipe.close()
            print '\n'