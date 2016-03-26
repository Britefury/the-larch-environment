##-*************************
##-* This source code is (C)copyright Geoffrey French 2008-2016 and is
##-* licensed under the MIT license, a copy of which can be found in
##-* the file 'COPYING'.
##-*************************

from BritefuryJ.LSpace.LSSpaceBin import SizeConstraint

from BritefuryJ.Pres.Primitive import Primitive, Label, Row, Column, Paragraph, FlowGrid, Blank
from BritefuryJ.Pres.UI import Section, SectionHeading1, SectionHeading2, SectionHeading3, ControlsRow

from BritefuryJ.Controls import ScrolledViewport, ResizeableBin

from LarchTools.PythonTools.GUIEditor.ComponentPalette import createPalette
from LarchTools.PythonTools.GUIEditor.CurrentComponentEditor import currentComponentEditor


#
#
#Control editor:
def _createTree():
	heading = SectionHeading1('Tree')
	return Section(heading, Blank())

def _createBottomPane(rootElement):
	return currentComponentEditor(rootElement)

#
#
#Side panel:

def _createSidePanel(rootElement):
	palette = createPalette()
	bottomPane = _createBottomPane(rootElement)
	palette = ScrolledViewport(palette.alignVTop().alignHPack(), 150.0, 150.0, None).alignHExpand().alignVExpand()
	bottomPane = ScrolledViewport(bottomPane.alignVTop().alignHPack(), 150.0, 150.0, None).alignHExpand().alignVExpand()
	bottomPane = ResizeableBin(bottomPane).resizeTop(350.0)
	return Column([palette.alignHExpand().alignVExpand(), bottomPane.alignHExpand().alignVExpand()])

def showSidePanel(rootElement):
	pm = rootElement.paneManager
	pm.rightEdgePane.setContent(_createSidePanel(rootElement), None, 250.0)

def hideSidePanel(rootElement):
	pm = rootElement.paneManager
	pm.rightEdgePane.clearContent()

