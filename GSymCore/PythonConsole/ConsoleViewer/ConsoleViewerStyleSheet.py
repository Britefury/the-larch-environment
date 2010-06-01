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
from GSymCore.Languages.Python25.Execution.ExecutionStyleSheet import ExecutionStyleSheet



class ConsoleViewerStyleSheet (StyleSheet):
	def __init__(self):
		super( ConsoleViewerStyleSheet, self ).__init__()
		
		self.initAttr( 'primitiveStyle', PrimitiveStyleSheet.instance )
		self.initAttr( 'controlsStyle', ControlsStyleSheet.instance )
		self.initAttr( 'pythonStyle', PythonEditorStyleSheet.instance )
		self.initAttr( 'executionStyle', ExecutionStyleSheet.instance )
		
		self.initAttr( 'labelAttrs', AttributeValues( fontSize=10 ) )

		self.initAttr( 'blockStyleAttrs', AttributeValues( vboxSpacing=2.0, border=SolidBorder( 1.0, 5.0, 15.0, 15.0, Color( 0.25, 0.25, 0.25 ), Color( 0.8, 0.8, 0.8 ) ) ) )
		
		self.initAttr( 'pythonModuleBorderAttrs', AttributeValues( border=SolidBorder( 1.0, 5.0, 10.0, 10.0, Color( 0.2, 0.4, 0.8 ), Color.WHITE ) ) )
		self.initAttr( 'dropPromptAttrs', AttributeValues( border=SolidBorder( 1.0, 3.0, 10.0, 10.0, Color( 0.0, 0.8, 0.0 ), Color.WHITE ) ) )
		
		self.initAttr( 'consoleBlockListSpacing', 5.0 )
		self.initAttr( 'consoleSpacing', 8.0 )
		
	
		
	def newInstance(self):
		return ConsoleViewerStyleSheet()
	
	
	
	def withPrimitiveStyle(self, primitiveStyle):
		return self.withAttrs( primitiveStyle=primitiveStyle )
	
	def withControlsStyle(self, controlsStyle):
		return self.withAttrs( controlsStyle=controlsStyle )
	
	def withPythonStyle(self, pythonStyle):
		return self.withAttrs( pythonStyle=pythonStyle )
	
	def withExecutionStyle(self, executionStyle):
		return self.withAttrs( executionStyle=executionStyle )
	
	
	def withLabelAttrs(self, labelAttrs):
		return self.withAttrs( labelAttrs=labelAttrs )
	

	def withBlockStyleAttrs(self, blockStyleAttrs):
		return self.withAttrs( blockStyleAttrs=blockStyleAttrs )
	
	
	def withPythonModuleBorderAttrs(self, pythonModuleBorderAttrs):
		return self.withAttrs( pythonModuleBorderAttrs=pythonModuleBorderAttrs )
	
	def withDropPromptAttrs(self, dropPromptAttrs):
		return self.withAttrs( dropPromptAttrs=dropPromptAttrs )
	
	
	def withConsoleSpacing(self, consoleSpacing):
		return self.withAttrs( consoleSpacing=consoleSpacing )
	
	def withConsoleBlockListSpacing(self, consoleBlockListSpacing):
		return self.withAttrs( consoleBlockListSpacing=consoleBlockListSpacing )
	
	
	
	@DerivedAttributeMethod
	def staticPythonStyle(self):
		return self['pythonStyle'].withPrimitiveStyle( self['pythonStyle']['primitiveStyle'].withNonEditable() )
	
	
	@DerivedAttributeMethod
	def labelStyle(self):
		return self['primitiveStyle'].withAttrValues( self['labelAttrs'] )

	
	@DerivedAttributeMethod
	def blockStyle(self):
		return self['primitiveStyle'].withAttrValues( self['blockStyleAttrs'] )
	
	@DerivedAttributeMethod
	def pythonModuleBorderStyle(self):
		return self['primitiveStyle'].withAttrValues( self['pythonModuleBorderAttrs'] )
	
	@DerivedAttributeMethod
	def dropPromptStyle(self):
		return self['primitiveStyle'].withAttrValues( self['dropPromptAttrs'] )
	
	
	@DerivedAttributeMethod
	def consoleBlockListStyle(self):
		return self['primitiveStyle'].withVBoxSpacing( self['consoleBlockListSpacing'] )
	
	@DerivedAttributeMethod
	def consoleStyle(self):
		return self['primitiveStyle'].withVBoxSpacing( self['consoleSpacing'] )
	
	

	def console(self, blocks, currentModule, currentModuleInteractor, currentModuleDropDest):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		consoleBlockListStyle = self.consoleBlockListStyle()
		consoleStyle = self.consoleStyle()
		pythonModuleBorderStyle = self.pythonModuleBorderStyle()
		
		currentModule = primitiveStyle.span( [ currentModule ] )
		currentModule.addInteractor( currentModuleInteractor )
		
		m = pythonModuleBorderStyle.border( currentModule.alignHExpand() ).alignHExpand()
		m.addDropDest( currentModuleDropDest )
		m.ensureVisible()
		
		dropPromptInsertionPoint = primitiveStyle.vbox( [] ).alignHExpand()
		
		if len( blocks ) > 0:
			blockList = consoleBlockListStyle.vbox( blocks ).alignHExpand()
			return consoleStyle.vbox( [ blockList.alignHExpand(), dropPromptInsertionPoint, m.alignHExpand() ] ).alignHExpand(), dropPromptInsertionPoint
		else:
			return consoleStyle.vbox( [ dropPromptInsertionPoint, m.alignVTop().alignHExpand() ] ).alignHExpand(), dropPromptInsertionPoint
		
		
	def dropPrompt(self, onAccept, onCancel):
		primitiveStyle = self['primitiveStyle']
		controlsStyle = self['controlsStyle']
		dropPromptStyle = self.dropPromptStyle()
		textEntry = controlsStyle.textEntry( 'var', onAccept, onCancel )
		prompt = primitiveStyle.staticText( 'Place node into a variable named: ' )
		return dropPromptStyle.border( primitiveStyle.paragraph( [ prompt.alignVCentre(), textEntry.getElement().alignVCentre() ] ) ), textEntry
		
	
	
	def consoleBlock(self, pythonModule, stdout, stderr, caughtException, result):
		executionStyle = self['executionStyle']
		blockStyle = self.blockStyle()
		pythonModuleBorderStyle = self.pythonModuleBorderStyle()
		
		blockContents = []
		blockContents.append( pythonModuleBorderStyle.border( pythonModule.alignHExpand() ).alignHExpand() )
		if stdout is not None:
			blockContents.append( executionStyle.stdout( stdout ) )
		if stderr is not None:
			blockContents.append( executionStyle.stderr( stderr ) )
		if caughtException is not None:
			blockContents.append( executionStyle.exception( caughtException ) )
		if result is not None:
			blockContents.append( executionStyle.result( result ) )
		blockVBox = blockStyle.vbox( blockContents ).alignHExpand()
		return blockStyle.border( blockVBox ).alignHExpand()
		
		
		
		
		
	

ConsoleViewerStyleSheet.instance = ConsoleViewerStyleSheet()
	
	
	