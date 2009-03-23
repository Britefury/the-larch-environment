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

import Britefury.Dispatch.MethodDispatch
#import Britefury.Dispatch.PatternDispatch
import Britefury.Dispatch.Dispatch
import Britefury.Transformation.Transformation
import Britefury.Tests.BritefuryJ.Parser.Utils.Operators
import Britefury.Tests.BritefuryJ.PatternMatch.Test_PatternMatcher
import Britefury.Tests.Britefury.Grammar.Grammar
import Britefury.Tests.Britefury.Dispatch.TestNodeMethodDispatch
import GSymCore.Languages.Python25.CodeGenerator
import GSymCore.Languages.Python25.Parser
import GSymCore.Languages.Python25.Parser2
import GSymCore.Languages.Python25.IdentityTransformation
import GSymCore.Languages.Java.Parser
import GSymCore.Languages.LISP.Parser2


testModules = [ Britefury.Dispatch.MethodDispatch, Britefury.Dispatch.Dispatch,
		Britefury.Transformation.Transformation,
		Britefury.Tests.BritefuryJ.Parser.Utils.Operators, Britefury.Tests.BritefuryJ.PatternMatch.Test_PatternMatcher,
		Britefury.Tests.Britefury.Grammar.Grammar, Britefury.Tests.Britefury.Dispatch.TestNodeMethodDispatch,
		GSymCore.Languages.Python25.CodeGenerator, GSymCore.Languages.Python25.Parser, GSymCore.Languages.Python25.Parser2, GSymCore.Languages.Python25.IdentityTransformation,
		GSymCore.Languages.Java.Parser,
		GSymCore.Languages.LISP.Parser2 ]


if __name__ == '__main__':
	if len( sys.argv ) > 1:
		moduleNames = [ m.__name__   for m in testModules ]
		
		for a in sys.argv[1:]:
			if a not in moduleNames:
				print 'No test module %s'  %  a
		testModules = [ module   for module in testModules   if module.__name__ in sys.argv[1:] ]


	loader = unittest.TestLoader()

	#print 'Testing the following modules:'
	#for m in testModules:
		#print m.__name__
	suites = [ loader.loadTestsFromModule( module )   for module in testModules ]

	runner = unittest.TextTestRunner()

	results = unittest.TestResult()

	overallSuite = unittest.TestSuite()
	overallSuite.addTests( suites )

	runner.run( overallSuite )
