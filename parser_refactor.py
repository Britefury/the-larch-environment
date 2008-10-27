from Britefury.InitBritefuryJ import initBritefuryJ
initBritefuryJ()

from BritefuryJ.PatternMatch import PatternMatcher, Guard
from BritefuryJ.PatternMatch.Pattern import *

from GSymCore.Languages.Python25.Python25Importer import importPy25File




xs = importPy25File( 'GSymCore/Languages/Python25/Parser.py' )

print xs
