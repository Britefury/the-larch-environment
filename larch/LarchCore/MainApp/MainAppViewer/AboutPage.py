##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2013.
##-*************************
from java.net import URI

from BritefuryJ.Pres.Primitive import Primitive, Image, Column, Row, Spacer

from BritefuryJ.Controls import Hyperlink

from BritefuryJ.Pres.RichText import Page, Body, Head, TitleBar, LinkHeaderBar, NormalText

from BritefuryJ.StyleSheet import StyleSheet


class AboutPage (object):
	def __present__(self, fragment, inheritedState):
		configurationLink = Hyperlink( 'CONFIGURATION PAGE', fragment.subject.world.configuration.subject() )
		linkHeader = LinkHeaderBar( [ configurationLink ])

		title = TitleBar( 'About' )

		splash = Image.systemImage( 'SplashScreen.png' )

		desc = NormalText( 'The Larch Environment was designed and written by Geoffrey French' )

		jythonLink = Hyperlink( 'Jython', URI( 'http://www.jython.org/' ) )
		jerichoLink = Hyperlink( 'Jericho HTML parser', URI( 'http://jericho.htmlparser.net' ) )
		salamanderLink = Hyperlink( 'SVG Salamander', URI( 'http://svgsalamander.java.net/' ) )
		googleFontsLink = Hyperlink('Google Fonts', URI('http://www.google.com/fonts'))

		jythonAcks = NormalText(['The Larch Environment incorporates the ', jythonLink, ' interpreter.'])
		libraryAcks = NormalText(['Larch incorporates the ', jerichoLink, ', and ', salamanderLink, ' libraries.'])
		fontAcks = NormalText(['Larch incorporates the following fonts (found at ', googleFontsLink, '): ',
				       'Arimo (by Steve Matteson), Lora (by Cyreal), Nobile (by Vernon Adams), Noto Sans (by Google), ',
				       'Open Sans (by Steve Matteson), PT Serif (by ParaType), and Source Sans Pro (by Paul D. Hunt)'])

		copyright = NormalText( '(C) copyright Geoffrey French 2008-2013' )

		homePage = Hyperlink( 'The Larch Environment website', URI( 'http://www.larchenvironment.com' ) )

		head = Head( [ linkHeader, title ] )
		body = Body( [ splash.padY( 20.0 ).alignHCentre(),
			       desc.padY( 10.0 ).alignHCentre(),
			       Spacer(0.0, 10.0),
			       jythonAcks.pad( 25.0, 5.0 ).alignHLeft(),
			       libraryAcks.pad( 25.0, 5.0 ).alignHLeft(),
			       fontAcks.pad(25.0,  5.0 ).alignHLeft(),
			       Spacer(0.0, 10.0),
			       Row( [ copyright.alignHLeft(), homePage.alignHRight() ] ).pad( 10.0, 10.0 ).alignHExpand() ] )
		return StyleSheet.style( Primitive.editable( False ) ).applyTo( Page( [ head, body ] ) )



