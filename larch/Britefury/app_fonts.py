##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************
from java.awt import Font, GraphicsEnvironment
from java.io import File

import os

from BritefuryJ.Pres.Primitive import Label

from Britefury import app_in_jar




_jarFontNames = []


def _testJarEntryName(name):
	name = name.lower()
	return name.startswith('fonts/')  and  name.endswith('.ttf')

def _handleJarEntry(name, reader_fn):
	_jarFontNames.append(name)


app_in_jar.registerJarEntryHandler(_testJarEntryName, _handleJarEntry)




_localFontDirectories = [ 'fonts' ]




def _loadFontsFromJar():
	gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment()

	for fontName in _jarFontNames:
		stream = Label.getResourceAsStream('/' + fontName)
		if stream is None:
			print 'Warning: could not load font {0} from JAR'.format(fontName)
		else:
			font = Font.createFont(Font.TRUETYPE_FONT, stream)
			gEnv.registerFont(font)


def _loadFontsFromFileSystem():
	gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment()

	for fontsDir in _localFontDirectories:
		for dirpath, dirnames, filenames in os.walk( fontsDir ):
			for filename in filenames:
				if filename.lower().endswith('.ttf'):
					p = os.path.join(dirpath, filename)

					# Load the font from a file
					fontFile = File(p)
					if not fontFile.exists():
						raise RuntimeError, 'Could not get font file for {0}'.format(p)
					else:
						font = Font.createFont(Font.TRUETYPE_FONT, fontFile)
						gEnv.registerFont(font)



def loadFonts():
	if app_in_jar.startedFromJar():
		_loadFontsFromJar()
	else:
		_loadFontsFromFileSystem()