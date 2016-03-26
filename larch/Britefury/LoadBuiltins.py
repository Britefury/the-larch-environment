##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************

def loadBuiltins(module):
	exec 'from Britefury.builtins import *' in module.__dict__
