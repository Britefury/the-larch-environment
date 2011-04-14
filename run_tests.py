##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import unittest
import sys

import Britefury.Dispatch.Dispatch
import Britefury.Dispatch.MethodDispatch
import Britefury.Tests.BritefuryJ.Parser.Utils.Operators
import Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle
import Britefury.Tests.Britefury.Grammar.Grammar
import Britefury.Tests.Britefury.Dispatch.TestObjectMethodDispatch
import Britefury.Tests.Britefury.Dispatch.TestDMObjectNodeMethodDispatch
import Britefury.Tests.Britefury.Util.Test_TrackedList
#import Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor
import GSymCore.Languages.Python25.CodeGenerator
import GSymCore.Languages.Python25.Python25Importer
import GSymCore.Languages.Python25.PythonEditor.Parser
import GSymCore.Languages.Java.JavaEditor.Parser


testModules = [ Britefury.Dispatch.Dispatch,
		Britefury.Dispatch.MethodDispatch, 
		Britefury.Tests.BritefuryJ.Parser.Utils.Operators,
		Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle,
		Britefury.Tests.Britefury.Grammar.Grammar,
		Britefury.Tests.Britefury.Dispatch.TestObjectMethodDispatch,
		Britefury.Tests.Britefury.Dispatch.TestDMObjectNodeMethodDispatch,
		Britefury.Tests.Britefury.Util.Test_TrackedList,
		#Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor,
		GSymCore.Languages.Python25.CodeGenerator,
		GSymCore.Languages.Python25.Python25Importer,
		GSymCore.Languages.Python25.PythonEditor.Parser,
		GSymCore.Languages.Java.JavaEditor.Parser
		]


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
