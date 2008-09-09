##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************


import sys



if __name__ == '__main__':
	if len( sys.argv ) != 2:
		print 'Usage:'
		print '\t%s <test_name>'  %  sys.argv[0]
	
	testName = sys.argv[1]
	
	__import__( 'Britefury.Tests.' + testName, globals(), locals(), [], -1 )