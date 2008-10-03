##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
import unittest
import sys

from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from Britefury.Tests.BritefuryJ.Parser.Utils import Operators



testModules = [ Operators ]


if __name__ == '__main__':
	if len( sys.argv ) > 1:
		testModules = [ module   for module in testModules   if module.__name__.split('.')[-1] in sys.argv[1:] ]


	loader = unittest.TestLoader()

	suites = [ loader.loadTestsFromModule( module )   for module in testModules ]

	runner = unittest.TextTestRunner()

	results = unittest.TestResult()

	overallSuite = unittest.TestSuite()
	overallSuite.addTests( suites )

	runner.run( overallSuite )
