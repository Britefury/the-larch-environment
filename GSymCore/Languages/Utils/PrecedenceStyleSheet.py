##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2010.
##-*************************
from java.awt import Color

from Britefury.Kernel.Abstract import abstractmethod

from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.ListView import *
from BritefuryJ.DocPresent.Border import *



class PrecedenceStyleSheet (StyleSheet):
	def __init__(self):
		super( PrecedenceStyleSheet, self ).__init__()
		
		self.initAttr( 'outerPrecedence', None )
		
	
	def newInstance(self):
		return PrecedenceStyleSheet()
	
	
	def withOuterPrecedence(self, outerPrecedence):
		return self.withAttr( 'outerPrecedence', outerPrecedence )


	def _numParensRequired(self, precedence):
		outerPrec = self['outerPrecedence']
		if precedence is not None  and  outerPrec is not None  and  precedence > outerPrec:
			return 1
		else:
			return 0
	
	
	def getNumParens(self, precedence, numAdditionalParens):
		return self._numParensRequired( precedence ) + numAdditionalParens

PrecedenceStyleSheet.instance = PrecedenceStyleSheet()



class PrecedenceStyleSheetText (PrecedenceStyleSheet):
	def __init__(self):
		super( PrecedenceStyleSheetText, self ).__init__()
		
		self.initAttr( 'openParen', '(' )
		self.initAttr( 'closeParen', ')' )
		
		
	def newInstance(self):
		return PrecedenceStyleSheetText()


	def applyParens(self, primtiveStyleSheet, child, precedence, numAdditionalParens):
		numParens = self.getNumParens( precedence, numAdditionalParens )
		if numParens == 0:
			return child
		else:
			openParen = self['openParen']
			closeParen = self['closeParen']
			return primtiveStyleSheet.span( [ primtiveStyleSheet.text( openParen )   for i in xrange( 0, numParens ) ]  +  [ child ]  +  [ primtiveStyleSheet.text( closeParen )   for i in xrange( 0, numParens ) ] )

PrecedenceStyleSheetText.instance = PrecedenceStyleSheetText()



