##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
import unittest
import sys
import os

sys.path.append( os.path.join( os.getcwd(), 'larch' ) )
sys.path.append( os.path.join( os.getcwd(), 'bin' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'jsoup-1.7.3.jar' ) )
sys.path.append( os.path.join( os.getcwd(), 'extlibs', 'svgSalamander.jar' ) )

import Britefury.Tests.BritefuryJ.Parser.Utils.Operators
import Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle
import Britefury.Tests.Britefury.Grammar.Grammar
import Britefury.Tests.Britefury.Dispatch.TestMethodDispatch
import Britefury.Tests.Britefury.Util.Test_TrackedList
import Britefury.Tests.Britefury.Util.Test_LiveList
#import Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor
import LarchCore.Languages.Python2.Schema
import LarchCore.Languages.Python2.CodeGenerator
import LarchCore.Languages.Python2.ASTGenerator
import LarchCore.Languages.Python2.Builder
import LarchCore.Languages.Python2.Python2Importer
import LarchCore.Languages.Python2.PythonEditor.Parser
import LarchCore.Languages.Java.JavaEditor.Parser
import LarchTools.PythonTools.VisualRegex.Parser
import LarchTools.PythonTools.VisualRegex.CodeGenerator
import LarchTools.PythonTools.GUIEditor.DataModel
import LarchTools.GrammarEditor.Parser
# import LarchTools.CodeSheets.DataModel


testModules = [ Britefury.Tests.BritefuryJ.Parser.Utils.Operators,
		Britefury.Tests.BritefuryJ.Isolation.Test_IsolationPickle,
		Britefury.Tests.Britefury.Grammar.Grammar,
		Britefury.Tests.Britefury.Dispatch.TestMethodDispatch,
		Britefury.Tests.Britefury.Util.Test_TrackedList,
		Britefury.Tests.Britefury.Util.Test_LiveList,
		#Britefury.Tests.Britefury.AttributeVisitor.TestAttributeVisitor,
		LarchCore.Languages.Python2.Schema,
		LarchCore.Languages.Python2.CodeGenerator,
		LarchCore.Languages.Python2.ASTGenerator,
		LarchCore.Languages.Python2.Builder,
		LarchCore.Languages.Python2.Python2Importer,
		LarchCore.Languages.Python2.PythonEditor.Parser,
		LarchCore.Languages.Java.JavaEditor.Parser,
		LarchTools.PythonTools.VisualRegex.Parser,
		LarchTools.PythonTools.VisualRegex.CodeGenerator,
		LarchTools.PythonTools.GUIEditor.DataModel,
		LarchTools.GrammarEditor.Parser,
		# LarchTools.CodeSheets.DataModel,
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
