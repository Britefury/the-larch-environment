##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Font, Color

from BritefuryJ.AttributeTable import *
from BritefuryJ.DocPresent import *
from BritefuryJ.DocPresent.StyleParams import *
from BritefuryJ.DocPresent.StyleSheet import *
from BritefuryJ.DocPresent.Controls import *
from BritefuryJ.DocPresent.Border import *
from BritefuryJ.DocPresent.Painter import *
from BritefuryJ.DocPresent.Browser import Location

from GSymCore.Languages.Python25.PythonEditor.PythonEditorStyleSheet import PythonEditorStyleSheet



class TerminalViewerStyleSheet (StyleSheet):
	def __init__(self):
		super( TerminalViewerStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'pythonStyle', PythonEditorStyleSheet.instance )
		
		self.initAttr( 'labelAttrs', AttributeValues( font=Font( 'Sans serif', Font.PLAIN, 10 ) ) )

		self.initAttr( 'blockStyleAttrs', AttributeValues( vboxSpacing=2.0, border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) ) )
		
		self.initAttr( 'pythonModuleBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) ) )
		self.initAttr( 'stdOutAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ), foreground=Color( 0.0, 0.5, 0.0 ) ) )
		self.initAttr( 'stdErrAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.8, 0.4, 0.0 ), Color.WHITE ), foreground=Color( 0.5, 0.25, 0.0 ) ) )
		self.initAttr( 'exceptionBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.8, 0.0, 0.0 ), Color( 1.0, 0.9, 0.9 ) ) ) )
		self.initAttr( 'resultBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 5.0, 5.0, Color( 0.0, 0.0, 0.8 ), Color.WHITE ) ) )
		
		self.initAttr( 'terminalBlockListSpacing', 5.0 )
		self.initAttr( 'terminalSpacing', 8.0 )
	
		
	def newInstance(self):
		return ProjectEditorStyleSheet()
	
	
	
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withControlsStyle(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withPythonStyle(self, pythonStyle):
		return self.withAttrs( pythonStyle=pythonStyle )
	
	
	def withLabelAttrs(self, labelAttrs):
		return self.withAttrs( labelAttrs=labelAttrs )
	

	def withBlockStyleAttrs(self, blockStyleAttrs):
		return self.withAttrs( blockStyleAttrs=blockStyleAttrs )
	
	
	def withPythonModuleBorderAttrs(self, pythonModuleBorderAttrs):
		return self.withAttrs( pythonModuleBorderAttrs=pythonModuleBorderAttrs )
	
	def withStdOutAttrs(self, stdOutAttrs):
		return self.withAttrs( stdOutAttrs=stdOutAttrs )
	
	def withStdErrAttrs(self, stdErrAttrs):
		return self.withAttrs( stdErrAttrs=stdErrAttrs )
	
	def withExceptionBorderAttrs(self, exceptionBorderAttrs):
		return self.withAttrs( exceptionBorderAttrs=exceptionBorderAttrs )
	
	def withResultBorderAttrs(self, resultBorderAttrs):
		return self.withAttrs( resultBorderAttrs=resultBorderAttrs )
	
	
	def withTerminalSpacing(self, terminalSpacing):
		return self.withAttrs( terminalSpacing=terminalSpacing )
	
	def withTerminalBlockListSpacing(self, terminalBlockListSpacing):
		return self.withAttrs( terminalBlockListSpacing=terminalBlockListSpacing )
	
	
	
	@AttributeTableDerivedPyAttrFn
	def staticPythonStyle(self):
		return self['pythonStyle'].withPrimitiveStyle( self['pythonStyle']['primitiveStyle'].withNonEditable() )
	
	
	@AttributeTableDerivedPyAttrFn
	def labelStyle(self):
		return self['primitiveStyle'].withAttrValues( self['labelAttrs'] )

	
	@AttributeTableDerivedPyAttrFn
	def blockStyle(self):
		return self['primitiveStyle'].withAttrValues( self['blockStyleAttrs'] )
	
	@AttributeTableDerivedPyAttrFn
	def pythonModuleBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonModuleBorderAttrs'] )
	
	@AttributeTableDerivedPyAttrFn
	def stdOutStyle(self):
		return self['primitiveStyle'].withAttrValues( self['stdOutAttrs'] )
	
	@AttributeTableDerivedPyAttrFn
	def stdErrStyle(self):
		return self['primitiveStyle'].withAttrValues( self['stdErrAttrs'] )
	
	@AttributeTableDerivedPyAttrFn
	def exceptionBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['exceptionBorderAttrs'] )
	
	@AttributeTableDerivedPyAttrFn
	def resultBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['resultBorderAttrs'] )
	
	
	@AttributeTableDerivedPyAttrFn
	def terminalBlockListStyle(self):
		return self['primitiveStyle'].withVBoxSpacing( self['terminalBlockListSpacing'] )
	
	@AttributeTableDerivedPyAttrFn
	def terminalStyle(self):
		return self['primitiveStyle'].withVBoxSpacing( self['terminalSpacing'] )
	
	

	def terminal(self, blocks, currentModule, currentModuleInteractor):
		primitiveStyle = self['primitiveStyle']
		terminalBlockListStyle = self.terminalBlockListStyle()
		terminalStyle = self.terminalStyle()
		pythonModuleBorderStyle = self.pythonModuleBorderStyle()
		
		currentModule = primitiveStyle.span( [ currentModule ] )
		currentModule.addInteractor( currentModuleInteractor )
		
		m = pythonModuleBorderStyle.border( primitiveStyle.vbox( [ currentModule.alignHExpand() ] ) ).alignHExpand()
		
		if len( blocks ) > 0:
			blockList = terminalBlockListStyle.vbox( blocks ).alignHExpand()
			return terminalStyle.vbox( [ blockList, m ] ).alignHExpand()
		else:
			return m
	
	
	def _textLines(self, labelText, text, textStyle):
		primitiveStyle = self['primitiveStyle']
		label = self.labelStyle().staticText( labelText )
		lines = primitiveStyle.vbox( [ textStyle.staticText( line )   for line in text.split( '\n' ) ] )
		return primitiveStyle.vbox( [ label, lines.padX( 5.0, 0.0 ) ] )
	
	def _exception(self, labelText, exception):
		primitiveStyle = self['primitiveStyle']
		label = self.labelStyle().staticText( labelText )
		return primitiveStyle.vbox( [ label, exception.padX( 5.0, 0.0 ).alignHExpand() ] )
	
	def terminalBlock(self, pythonModule, stdout, stderr, caughtException, result):
		blockStyle = self.blockStyle()
		pythonModuleBorderStyle = self.pythonModuleBorderStyle()
		stdOutStyle = self.stdOutStyle()
		stfErrStyle = self.stdErrStyle()
		exceptionBorderStyle = self.exceptionBorderStyle()
		resultBorderStyle = self.resultBorderStyle()
		
		blockContents = []
		pythonModuleBox = PrimitiveStyleSheet.instance.vbox( [ pythonModule ] )
		blockContents.append( pythonModuleBorderStyle.border( pythonModuleBox.alignHExpand() ).alignHExpand() )
		if stdout is not None:
			blockContents.append( stdOutStyle.border( self._textLines( 'STDOUT:', stdout, stdOutStyle ).alignHExpand() ).alignHExpand() )
		if stderr is not None:
			blockContents.append( stdErrStyle.border( self._textLines( 'STDERR:', stderr, stdErrStyle ).alignHExpand() ).alignHExpand() )
		if caughtException is not None:
			blockContents.append( exceptionBorderStyle.border( self._exception( 'EXCEPTION:', PrimitiveStyleSheet.instance.paragraph( [ caughtException ] ) ) ).alignHExpand() )
		if result is not None:
			resultBox = PrimitiveStyleSheet.instance.paragraph( [ result ] )
			blockContents.append( resultBorderStyle.border( resultBox.alignHExpand() ).alignHExpand() )
		blockVBox = blockStyle.vbox( blockContents ).alignHExpand()
		return blockStyle.border( blockVBox ).alignHExpand()
		
		
		
		
		
	

TerminalViewerStyleSheet.instance = TerminalViewerStyleSheet()
	
	
	