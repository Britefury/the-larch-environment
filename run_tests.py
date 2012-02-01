##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2011.
##-*************************
import unittest
import sys

import Britefury.Tests.BritefuryJ.Parser.Utils.Operators
import Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle
import Britefury.Tests.Britefury.Grammar.Grammar
import Britefury.Tests.Britefury.Dispatch.TestMethodDispatch
import Britefury.Tests.Britefury.Util.Test_TrackedList
#import Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor
import LarchCore.Languages.Python25.CodeGenerator
import LarchCore.Languages.Python25.ASTGenerator
import LarchCore.Languages.Python25.Python25Importer
import LarchCore.Languages.Python25.PythonEditor.Parser
import LarchCore.Languages.Java.JavaEditor.Parser


testModules = [ Britefury.Tests.BritefuryJ.Parser.Utils.Operators,
		Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle,
		Britefury.Tests.Britefury.Grammar.Grammar,
		Britefury.Tests.Britefury.Dispatch.TestMethodDispatch,
		Britefury.Tests.Britefury.Util.Test_TrackedList,
		#Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor,
		LarchCore.Languages.Python25.CodeGenerator,
		LarchCore.Languages.Python25.ASTGenerator,
		LarchCore.Languages.Python25.Python25Importer,
		LarchCore.Languages.Python25.PythonEditor.Parser,
		LarchCore.Languages.Java.JavaEditor.Parser
		]


if __name__ == '__main__':
	modulesToTest = []
	modulesToTest[:] = testModules
	
	if len( sys.argv ) > 1:
		modulesToTest = []
		for a in sys.argv[1:]:
			x = None
			for m in testModules:
				name = m.__name__
				if a == name:
					x = m
					break
			
			if x is None:
				for m in testModules:
					name = m.__name__
					if name.endswith( a ):
						x = m
						break

			if x is None:
				print 'No test module %s'  %  a
			else:
				modulesToTest.append( x )

	
	print 'Testing:'
	for m in modulesToTest:
		print '\t' + m.__name__
				

	loader = unittest.TestLoader()

	#print 'Testing the following modules:'
	#for m in testModules:
		#print m.__name__
	suites = [ loader.loadTestsFromModule( module )   for module in modulesToTest ]

	runner = unittest.TextTestRunner()

	results = unittest.TestResult()

	overallSuite = unittest.TestSuite()
	overallSuite.addTests( suites )

	runner.run( overallSuite )
