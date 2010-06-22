##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location

from Britefury.AttributeTableUtils.DerivedAttributeMethod import DerivedAttributeMethod

from GSymCore.Languages.Python25.PythonEditor.PythonEditorStyleSheet import PythonEditorStyleSheet



class ExecutionStyleSheet (StyleSheet):
	def __init__(self):
		super( ExecutionStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		
		self.initAttr( 'labelAttrs', AttributeValues( fontSize=10 ) )

		self.initAttr( 'stdOutAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ), foreground=Color( 0.0, 0.5, 0.0 ) ) )
		self.initAttr( 'stdErrAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 1.0, 0.5, 0.0 ), Color.WHITE ), foreground=Color( 0.75, 0.375, 0.0 ) ) )
		self.initAttr( 'exceptionBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) )
		self.initAttr( 'resultBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) )
		
		self.initAttr( 'resultSpacing', 5.0 )
		
	
		
	def newInstance(self):
		return ExecutionStyleSheet()
	
	
	
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	
	def withLabelAttrs(self, labelAttrs):
		return self.withAttrs( labelAttrs=labelAttrs )
	

	def withStdOutAttrs(self, stdOutAttrs):
		return self.withAttrs( stdOutAttrs=stdOutAttrs )
	
	def withStdErrAttrs(self, stdErrAttrs):
		return self.withAttrs( stdErrAttrs=stdErrAttrs )
	
	def withExceptionBorderAttrs(self, exceptionBorderAttrs):
		return self.withAttrs( exceptionBorderAttrs=exceptionBorderAttrs )
	
	def withResultBorderAttrs(self, resultBorderAttrs):
		return self.withAttrs( resultBorderAttrs=resultBorderAttrs )
	
	
	def withResultSpacing(self, resultSpacing):
		return self.withAttrs( resultSpacing=resultSpacing )
	
	
	
	@DerivedAttributeMethod
	def labelStyle(self):
		return self['primitiveStyle'].withAttrValues( self['labelAttrs'] )

	
	@DerivedAttributeMethod
	def stdOutStyle(self):
		return self['primitiveStyle'].withAttrValues( self['stdOutAttrs'] )
	
	@DerivedAttributeMethod
	def stdErrStyle(self):
		return self['primitiveStyle'].withAttrValues( self['stdErrAttrs'] )
	
	@DerivedAttributeMethod
	def exceptionBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['exceptionBorderAttrs'] )
	
	@DerivedAttributeMethod
	def resultBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['resultBorderAttrs'] )
	
	@DerivedAttributeMethod
	def resultBoxStyle(self):
		return self['primitiveStyle'].withAttrs( resultSpacing=self['resultSpacing'] )
	
	

	def _textLines(self, labelText, text, textStyle):
		primitiveStyle = self['primitiveStyle']
		label = self.labelStyle().staticText( labelText )
		lines = primitiveStyle.vbox( [ textStyle.staticText( line )   for line in text.split( '\n' ) ] )
		return primitiveStyle.vbox( [ label, lines.padX( 5.0, 0.0 ) ] )
	
	
	def stdout(self, text):
		stdOutStyle = self.stdOutStyle()
		return stdOutStyle.border( self._textLines( 'STDOUT:', text, stdOutStyle ).alignHExpand() ).alignHExpand()
	
	def stderr(self, text):
		stdErrStyle = self.stdErrStyle()
		return stdErrStyle.border( self._textLines( 'STDERR:', text, stdErrStyle ).alignHExpand() ).alignHExpand()
		
	def exception(self, exceptionView):
		primitiveStyle = self['primitiveStyle']
		exceptionBorderStyle = self.exceptionBorderStyle()
		label = self.labelStyle().staticText( 'EXCEPTION:' )
		return exceptionBorderStyle.border( primitiveStyle.vbox( [ label, exceptionView.padX( 5.0, 0.0 ).alignHExpand() ] ).alignHExpand() ).alignHExpand()

	def result(self, resultView):
		resultBorderStyle = self.resultBorderStyle()
		return resultBorderStyle.border( PrimitiveStyleSheet.instance.paragraph( [ resultView ] ).alignHExpand() ).alignHExpand()
	
	
	def executionResult(self, stdoutText, stderrText, exceptionView, resultView):
		resultBoxStyle = self.resultBoxStyle()
		
		boxContents = []
		if stderrText is not None:
			boxContents.append( self.stderr( stderrText ).alignHExpand() )
		if exceptionView is not None:
			boxContents.append( self.exception( exceptionView ).alignHExpand() )
		if stdoutText is not None:
			boxContents.append( self.stdout( stdoutText ).alignHExpand() )
		if resultView is not None:
			boxContents.append( self.result( resultView ).alignHExpand() )
		
		if len( boxContents ) > 0:
			return resultBoxStyle.vbox( boxContents )
		else:
			return None


	def minimalExecutionResult(self, stdoutText, stderrText, exceptionView, resultView):
		if stdoutText is None  and  stderrText is None  and  exceptionView is None:
			if resultView is None:
				return None
			else:
				return PrimitiveStyleSheet.instance.paragraph( [ resultView.alignHExpand() ] ).alignHExpand()
		else:
			resultBoxStyle = self.resultBoxStyle()
			
			boxContents = []
			if stderrText is not None:
				boxContents.append( self.stderr( stderrText ).alignHExpand() )
			if exceptionView is not None:
				boxContents.append( self.exception( exceptionView ).alignHExpand() )
			if stdoutText is not None:
				boxContents.append( self.stdout( stdoutText ).alignHExpand() )
			if resultView is not None:
				boxContents.append( self.result( resultView ).alignHExpand() )
			
			if len( boxContents ) > 0:
				return resultBoxStyle.vbox( boxContents )
			else:
				return None




ExecutionStyleSheet.instance = ExecutionStyleSheet()
	
	
	