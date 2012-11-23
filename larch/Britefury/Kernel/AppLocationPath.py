##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2008.
##-*************************
from java.awt import Color

from BritefuryJ.Pres.Primitive import *
from BritefuryJ.Pres.RichText import *
from BritefuryJ.Controls import *
from BritefuryJ.Graphics import FilledOutlinePainter
from BritefuryJ.StyleSheet import StyleSheet
from BritefuryJ.DefaultPerspective import DefaultPerspective


class _LocationPathEntry (object):
	def __init__(self, name, subject):
		self.__name = name
		self.__subject = subject

	def __present__(self, fragment, inheritedState):
		return Hyperlink( self.__name, self.__subject )

_arrowStyle = StyleSheet.style( Primitive.shapePainter( FilledOutlinePainter( Color( 0.5, 0.5, 0.5, 0.2 ), Color( 0.5, 0.5, 0.5, 0.8 ) ) )  )
_arrow = _arrowStyle.applyTo( Arrow( Arrow.Direction.RIGHT, 12.0 ) ).padX( 7.0 ).alignVCentre()


class LocationPath (object):
	def __init__(self, entries):
		self.__entries = entries


	def withPathEntry(self, name, subject):
		return LocationPath( self.__entries + [ _LocationPathEntry( name, subject ) ] )

	def __present__(self, fragment, inheritedState):
		contents = []
		if len( self.__entries ) > 0:
			contents.append( self.__entries[0] )
			for e in self.__entries[1:]:
				contents.append( _arrow )
				contents.append( LineBreak() )
				contents.append( e )
		return Paragraph( contents )


def appLinkheaderBar(subject, rightContents=[]):
	try:
		path = subject.appLocationPath
	except AttributeError:
		path = None
	left = []
	if path is not None:
		left.append( DefaultPerspective.instance.applyTo( path ) )
	return SplitLinkHeaderBar( left , rightContents )




