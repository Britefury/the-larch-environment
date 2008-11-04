from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from BritefuryJ.PatternMatch import PatternMatcher, Guard
from BritefuryJ.PatternMatch.Pattern import *

from Britefury.Transformation.Transformation import Transformation
from Britefury.Transformation.TransformationInterface import TransformationInterface

from Britefury.Dispatch.Dispatch import DispatchError

from GSymCore.Languages.Python25.Python25Importer import importPy25File
from GSymCore.Languages.Python25.IdentityTransformation import Python25IdentityTransformation




xs = importPy25File( 'GSymCore/Languages/Python25/Parser.py' )



def isProduction(node):
	return True


def buildRuleMethod(node, name, value):
	pass
	
	


# Need a way of transforming 1 statement into multiple statements


g1 = Guard( Pattern.toPattern( [ 'assignmentStmt', [ [ 'singleTarget', 'name' << Anything() ] ], 'value' << ( Anything() & isProduction ) ] ), buildRuleMethod )



class Transformation (TransformationInterface):
	def assignmentStmt(self, xform, node, targets, value):
		if len( targets ) == 1  and  isProduction( value ):
			return [ 'assignmentStmt', [ xform( t )   for t in targets ], xform( value ) ]
		else:
			raise DispatchError
		
		
	




xf = Transformation( Python25IdentityTransformation(), [] )

