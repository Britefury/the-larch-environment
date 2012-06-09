##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2012.
##-*************************
from javax.swing import JOptionPane, JFileChooser
from javax.swing.filechooser import FileNameExtensionFilter

from java.awt import Color, Font, GraphicsEnvironment

import os
import sys

from BritefuryJ.Incremental import IncrementalValueMonitor

from BritefuryJ.Controls import *

from BritefuryJ.Graphics import *

from BritefuryJ.Pres import *
from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Pres.UI import *
from BritefuryJ.StyleSheet import *
from BritefuryJ.LSpace.Interactor import *
from BritefuryJ.LSpace.Input import Modifier

from BritefuryJ.Live import *

from BritefuryJ.GraphViz import GraphViz, Configuration as GraphVizConfiguration

from Britefury.Util.Abstract import abstractmethod

from Britefury.Config.UserConfig import loadUserConfig, saveUserConfig
from Britefury.Config import Configuration
from Britefury.Config.ConfigurationPage import ConfigurationPage




# Font chooser

_ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
_fontNames = sorted( [ font.name   for font in _ge.allFonts ] )

_nameStyle = StyleSheet.style( Primitive.fontSize( 10 ), Primitive.foreground( Color( 0.45, 0.45, 0.45 ) ) )
_hoverStyle = StyleSheet.style( Primitive.hoverBackground( FilledOutlinePainter( Color( 0.95, 0.95, 0.95 ), Color( 0.65, 0.65, 0.65 ) ) ) )
_headerNameStyle = StyleSheet.style( Primitive.fontItalic( True ) )

def _fontSample(fontName, sampleFn):
	sample = sampleFn( fontName )
	name = Label( fontName )
	return _hoverStyle( Bin( Column( [ sample, _nameStyle( name ).padX( 10.0, 0.0 ), ] ).pad( 5.0, 5.0 ) ) )


class _ChoiceInteractor (PushElementInteractor):
	def __init__(self, choiceValue, fontName):
		self.__value, self.__fontName = choiceValue, fontName

	def buttonPress(self, element, event):
		if event.button == 1:
			if Modifier.getKeyModifiers( event.modifiers ) == Modifier.CTRL:
				val = self.__value.getStaticValue()
				val += '; ' + self.__fontName
				self.__value.setLiteralValue( val )
			else:
				self.__value.setLiteralValue( self.__fontName )

	def buttonRelease(self, element, event):
		pass


def _chooserSample(choiceValue, fontName, sampleFn):
	s = _fontSample( fontName, sampleFn )
	return s.withElementInteractor( _ChoiceInteractor( choiceValue, fontName ) ).alignHExpand()

def _fontChooser(choiceValue, sampleFn):
	return FlowGrid( [ _chooserSample( choiceValue, name, sampleFn ) for name in _fontNames ] )


def _collapsibleFontChooser(title, choiceValue, sampleFn):
	expanded = LiveValue( False )

	@LiveFunction
	def currentChoice():
		return _headerNameStyle( Label( choiceValue.getValue() ) )

	header = Row( [ Label( title ), Spacer( 25.0, 0.0 ), currentChoice ] ).alignHPack()

	return DropDownExpander( header, ScrolledViewport( _fontChooser( choiceValue, sampleFn ), 800.0, 400.0, None ), expanded ).alignHExpand()





class FontConfiguration (object):
	def __init__(self, generic='SansSerif', normal='SansSerif', heading='Serif', title='Serif', uiHeading='SansSerif'):
		self._generic = LiveValue( generic )
		self._normal = LiveValue( normal )
		self._heading = LiveValue( heading )
		self._title = LiveValue( title )
		self._uiHeading = LiveValue( uiHeading )


	def __getstate__(self):
		state = {}
		state['generic'] = self._generic.getStaticValue()
		state['normal'] = self._normal.getStaticValue()
		state['heading'] = self._heading.getStaticValue()
		state['title'] = self._title.getStaticValue()
		state['uiHeading'] = self._uiHeading.getStaticValue()
		return state

	def __setstate__(self, state):
		self._generic = LiveValue( state.get( 'generic', 'SansSerif' ) )
		self._normal = LiveValue( state.get( 'normal', 'SansSerif' ) )
		self._heading = LiveValue( state.get( 'standard', 'Serif' ) )
		self._title = LiveValue( state.get( 'standard', 'Serif' ) )
		self._uiHeading = LiveValue( state.get( 'standard', 'SansSerif' ) )


	def copyFrom(self, config):
		self._generic.setLiteralValue( config._generic.getStaticValue() )
		self._normal.setLiteralValue( config._normal.getStaticValue() )
		self._heading.setLiteralValue( config._heading.getStaticValue() )
		self._title.setLiteralValue( config._title.getStaticValue() )
		self._uiHeading.setLiteralValue( config._uiHeading.getStaticValue() )


	def _createStyleSheet(self):
		def suffix(fontName, defaultFont):
			if defaultFont in [ f.strip()   for f in fontName.split( ';' ) ]:
				return fontName
			elif fontName.strip() == '':
				return defaultFont
			else:
				return fontName + '; ' + defaultFont

		style = StyleSheet.style( Primitive.fontFace( suffix( self._generic.getValue(), 'SansSerif' ) ),
					RichText.titleTextAttrs( RichText.titleTextAttrs.defaultValue.withValues( Primitive.fontFace( suffix( self._title.getValue(), 'Serif' ) ) ) ),
		                        RichText.headingTextAttrs( RichText.headingTextAttrs.defaultValue.withValues( Primitive.fontFace( suffix( self._heading.getValue(), 'Serif' ) ) ) ),
		                        RichText.normalTextAttrs( RichText.normalTextAttrs.defaultValue.withValues( Primitive.fontFace( suffix( self._normal.getValue(), 'SansSerif' ) ) ) ),
		                        UI.uiTextAttrs( UI.uiTextAttrs.defaultValue.withValues( Primitive.fontFace( suffix( self._uiHeading.getValue(), 'SansSerif' ) ) ) ) )
		return style


	def apply(self):
		StyleValues.setRootStyleSheet( self._createStyleSheet() )


	def __present__(self, fragment, inheritedState):
		helpText = NormalText( 'Click to open the font choosers. Click on a font sample to choose it. Ctrl-click to select additional fonts.' )

		def titleSample(fontName, text='The quick brown fox'):
			style = StyleSheet.style( RichText.titleTextAttrs( RichText.titleTextAttrs.defaultValue.withValues( Primitive.fontFace( fontName ) ) ) )
			return style( TitleBar( text ) )
		titleChooser = _collapsibleFontChooser( 'Titles', self._title, titleSample )

		def headingSample(fontName, text='The quick brown fox jumps over the lazy dog'):
			style = StyleSheet.style( RichText.headingTextAttrs( RichText.headingTextAttrs.defaultValue.withValues( Primitive.fontFace( fontName ) ) ) )
			return style( Heading1( text ) )
		headingChooser = _collapsibleFontChooser( 'Headings', self._heading, headingSample )

		def normalSample(fontName, text='The quick brown fox jumps over the lazy dog'):
			style = StyleSheet.style( RichText.normalTextAttrs( RichText.normalTextAttrs.defaultValue.withValues( Primitive.fontFace( fontName ) ) ) )
			return style( NormalText( text ) )
		normalChooser = _collapsibleFontChooser( 'Normal text', self._normal, normalSample )

		def uiHeadingSample(fontName, text='The quick brown fox jumps over the lazy dog'):
			style = StyleSheet.style( UI.uiTextAttrs( UI.uiTextAttrs.defaultValue.withValues( Primitive.fontFace( fontName ) ) ) )
			return style( SectionHeading1( text ) )
		uiHeadingChooser = _collapsibleFontChooser( 'UI Headings', self._uiHeading, uiHeadingSample )

		def genericSample(fontName, text='The quick brown fox jumps over the lazy dog'):
			style = StyleSheet.style( Primitive.fontFace( fontName ) )
			return style( Label( text ) )
		genericChooser = _collapsibleFontChooser( 'Generic text', self._generic, genericSample )


		chooserColumn = self._chooserColumnStyle( Column( [ titleChooser, headingChooser, normalChooser, uiHeadingChooser, genericChooser ] ) )


		# Sample page
		@LiveFunction
		def samplePage():
			label = Label( 'Sample page:' )

			title = titleSample( self._title.getValue(), 'Example Page Title' )
			heading = headingSample( self._heading.getValue(), 'Main heading' )
			normal1 = normalSample( self._normal.getValue(), 'Normal text will appear like this.' )
			normal2 = normalSample( self._normal.getValue(), 'Paragraphs of normal text are used for standard content.' )
			ui1 = uiHeadingSample( self._uiHeading.getValue(), 'UI heading' )
			genericLabel = Label( 'Generic text (within controls, code, etc) will appear like this.' )
			buttons = self._buttonRowStyle( Row( [ Button.buttonWithLabel( 'Button {0}'.format( i ), None )   for i in xrange( 0, 5 ) ] ) )
			ui = Section( ui1, Column( [ genericLabel, Spacer( 0.0, 7.0 ), buttons ] ) )
			page = Page( [ title, Body( [ heading, normal1, normal2, ui ] ) ] )

			return Column( [ label, Spacer( 0.0, 15.0 ), page ] )


		return self._mainColumnStyle( Column( [ helpText, chooserColumn, samplePage ] ) )


	_mainColumnStyle = StyleSheet.style( Primitive.columnSpacing( 30.0 ) )
	_chooserColumnStyle = StyleSheet.style( Primitive.columnSpacing( 5.0 ) )
	_buttonRowStyle = StyleSheet.style( Primitive.rowSpacing( 10.0 ) )



_basicFontConfig = FontConfiguration()
_windowsFontConfig = FontConfiguration( generic='DejaVu Sans; SansSerif', normal='DejaVu Sans; SansSerif', heading='Perpetua; Serif', title='Lucida Bright; Serif', uiHeading='Dotum; Gulim; SansSerif' )
_linuxFontConfig = FontConfiguration( generic='SansSerif', normal='SansSerif', heading='Un Batang; Serif', title='Norasi; Bitstream Charter; Serif', uiHeading='DejaVu Sans ExtraLight; Sawasdee; SansSerif' )
_macFontConfig = FontConfiguration( generic='Geneva; SansSerif', normal='Geneva; SansSerif', heading='Perpetua; Serif', title='CalistoMT; Serif', uiHeading='Gulim; SansSerif' )




_platformNameBasic = 'basic'
_platformNameWindows = 'windows'
_platformNameLinux = 'linux'
_platformNameMac = 'mac'

_configByPlatform = {
	_platformNameBasic : _basicFontConfig,
	_platformNameWindows : _windowsFontConfig,
	_platformNameLinux : _linuxFontConfig,
	_platformNameMac : _macFontConfig }


_fontConfigChoices = [
	( None, 'Platform default' ),
	( _platformNameWindows, 'Windows' ),
	( _platformNameLinux, 'Linux' ),
	( _platformNameMac, 'Mac' ),
	( _platformNameBasic, 'Basic' )
]



def _getCurrentPlatformName():
	osName = sys.registry['os.name']
	if osName.startswith( 'Windows' ):
		return _platformNameWindows
	elif osName.startswith( 'Linux' ):
		return _platformNameLinux
	elif osName.startswith( 'Mac' ):
		return _platformNameMac
	else:
		return _platformNameBasic



def _fontConfigForName(config):
	if isinstance( config, FontConfiguration ):
		return config
	elif isinstance( config, str )  or  isinstance( config, unicode ):
		# config is a name
		return _configByPlatform.get( config, _basicFontConfig )
	elif config is None:
		name = _getCurrentPlatformName()
		return _configByPlatform.get( name, _basicFontConfig )
	else:
		raise TypeError, 'invalid config type'



class FontConfigurationPage (ConfigurationPage):
	def __init__(self):
		super( FontConfigurationPage, self ).__init__()
		self._config = None
		self._userConfig = LiveValue( self._config )


	def __getstate__(self):
		state = super( FontConfigurationPage, self ).__getstate__()
		state['config'] = self._config
		return state

	def __setstate__(self, state):
		super( FontConfigurationPage, self ).__setstate__( state )
		self._config = state.get( 'config' )
		self._userConfig = LiveValue( self._config )


	def getSubjectTitle(self):
		return '[CFG] Fonts'

	def getTitleText(self):
		return 'Font Configuration'

	def getLinkText(self):
		return 'Fonts'



	def apply(self):
		config = _fontConfigForName(self._config)
		config.apply()


	def __present_contents__(self, fragment, inheritedState):
		def _onApply(button, event):
			self._config = self._userConfig.getStaticValue()
			self.apply()

		def _onRevert(button, event):
			self._userConfig.setLiteralValue( self._config )


		choices = [ Label( c[1] )   for c in _fontConfigChoices ]
		choices.append( Label( 'Custom' ) )


		@LiveFunction
		def indexOfChoice():
			config = self._userConfig.getValue()
			if isinstance( config, FontConfiguration ):
				return len( choices ) - 1
			else:
				try:
					return [ c[0]   for c in _fontConfigChoices ].index( self._userConfig.getValue() )
				except ValueError:
					return 0

		@LiveFunction
		def footer():
			config = self._userConfig.getValue()
			if isinstance( config, FontConfiguration ):
				return config
			else:
				return Blank()


		def _onChoice(menu, prevChoice, choice):
			if choice == len( choices ) - 1:
				# Custom
				if prevChoice != choice:
					prevConfig = _fontConfigChoices[prevChoice][0]
					prevConfig = _fontConfigForName( prevConfig )
					config = FontConfiguration()
					config.copyFrom( prevConfig )
					self._userConfig.setLiteralValue( config )
			else:
				self._userConfig.setLiteralValue( _fontConfigChoices[choice][0] )

		applyButton = Button.buttonWithLabel( 'Apply', _onApply )
		revertButton = Button.buttonWithLabel( 'Revert', _onRevert )
		buttons = Row( [ applyButton, Spacer( 15.0, 0.0 ), revertButton ] ).alignHPack()

		configMenu = OptionMenu( choices, indexOfChoice, _onChoice )

		chooserHeader = Row( [ Label( 'Choose a font configuration:' ), Spacer( 25.0, 0.0 ), configMenu ] ).alignHPack()

		return self._pageColumnStyle( Column( [ buttons, chooserHeader, footer ] ) )

	_pageColumnStyle = StyleSheet.style( Primitive.columnSpacing( 25.0 ) )





_fontConfigFilename = 'fonts'



def _loadFontConfig():
	return loadUserConfig( _fontConfigFilename )


def saveFontConfig():
	saveUserConfig( _fontConfigFilename, _fontConfig )




_fontConfig = None


def initFontConfig():
	global _fontConfig

	if _fontConfig is None:
		_fontConfig = _loadFontConfig()

		if _fontConfig is None:
			_fontConfig = FontConfigurationPage()

		_fontConfig.apply()

		Configuration.registerSystemConfigurationPage( _fontConfig )
