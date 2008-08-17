##-*************************
##-* This program is free software; you can use it, redistribute it and/or modify it
##-* under the terms of the GNU General Public License version 2 as published by the
##-* Free Software Foundation. The full text of the GNU General Public License
##-* version 2 can be found in the file named 'COPYING' that accompanies this
##-* program. This source code is (C)copyright Geoffrey French 1999-2007.
##-*************************



from javax.swing import JFrame, JEditorPane, AbstractAction, JMenuItem, JMenu, JMenuBar
from java.awt import Dimension


from BritefuryJ.GSymViewSwing import *
from BritefuryJ.GSymViewSwing.DocLayout import *
from BritefuryJ.GSymViewSwing.ElementViewFactories import *






class MainApp (object):
	def __init__(self, documentRoot):
		self._frame = JFrame( 'gSym' )
		
		self._frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		self._editorPane = JEditorPane()
		
		self._editorPane.setPreferredSize( Dimension( 640, 480 ) )
		
		editorKit = GSymViewEditorKit()
		self._document = editorKit.createDefaultDocument()
		self._docLayout = self._document.getDocumentLayout();
		
		self._editorPane.setEditorKit( editorKit )
		self._editorPane.setDocument( self._document )
		
		self._menuBar = self.createMenus()
		
		self._frame.setJMenuBar( self._menuBar )
		
		self._frame.add( self._editorPane )
		
		self._frame.pack()

		
		
	def createAction(self, name, f):
		class Act (AbstractAction):
			def actionPerformed(self, event):
				f()
		return Act( name )
	
		
		
	def createMenus(self):
		def onFill():
			self.testFill()
			
		def onFill2():
			self.testFill2()
			
		def onDump():
			pass
		#	root = self._document.getDefaultRootElement()
		#	root.dump( java.System.out, 2 )
		
		
		fillItem = JMenuItem( self.createAction( 'Fill', onFill ) )
		fill2Item = JMenuItem( self.createAction( 'Fill2', onFill2 ) )
		dumpItem = JMenuItem( self.createAction( 'Dump', onDump ) )
		
		testMenu = JMenu( 'Test' )
		testMenu.add( fillItem )
		testMenu.add( fill2Item )
		testMenu.add( dumpItem )
		
		menuBar = JMenuBar()
		menuBar.add( testMenu )
		
		return menuBar
	
	
	
	def paramSpec(self):
		leaf0 = DocLayoutNodeLeaf( "aaaaaaa,", None, GlyphViewFactory.viewFactory )
		leaf1 = DocLayoutNodeLeaf( "bbbbbbb,", None, GlyphViewFactory.viewFactory )
		leaf2 = DocLayoutNodeLeaf( "ccccccc,", None, GlyphViewFactory.viewFactory )
		leaf3 = DocLayoutNodeLeaf( "ddddddd,", None, GlyphViewFactory.viewFactory )
		leaf4 = DocLayoutNodeLeaf( "eeeeeee,", None, GlyphViewFactory.viewFactory )
		leaf5 = DocLayoutNodeLeaf( "fffffff,", None, GlyphViewFactory.viewFactory )
		leaf6 = DocLayoutNodeLeaf( "ggggggg,", None, GlyphViewFactory.viewFactory )
		leaves = [ leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 ]
		branch = DocLayoutNodeBranch( None, ParagraphViewFactory.viewFactory )
		branch.setChildren( leaves )
		return branch
	
	def callSpec(self, inner):
		openParen = DocLayoutNodeLeaf( "(", None, GlyphViewFactory.viewFactory )
		closeParen = DocLayoutNodeLeaf( ")", None, GlyphViewFactory.viewFactory )
		innerOpen = [ inner, openParen ]
		innerOpenBranch = DocLayoutNodeBranch( None, VBoxViewFactory.viewFactory )
		innerOpenBranch.setChildren( innerOpen );
		params = self.paramSpec()
		call = [ innerOpenBranch, params, closeParen ]
		callBranch = DocLayoutNodeBranch( None, ParagraphViewFactory.viewFactory )
		callBranch.setChildren( call )
		return callBranch

	def getAttrSpec(self, inner, attrName):
		dot = DocLayoutNodeLeaf( ".", None, GlyphViewFactory.viewFactory )
		attr = DocLayoutNodeLeaf( attrName, None, GlyphViewFactory.viewFactory )
		innerDot = [ inner, dot ]
		innerDotBranch = DocLayoutNodeBranch( None, VBoxViewFactory.viewFactory )
		innerDotBranch.setChildren( innerDot )
		getattrChildren = [ innerDotBranch, attr ]
		getattrBranch = DocLayoutNodeBranch( None, ParagraphViewFactory.viewFactory )
		getattrBranch.setChildren( getattrChildren )
		return getattrBranch
	
	
	
	def getattrSpecRecursive(self, inner, level):
		if level == 0:
			return inner
		else:
			return self.getattrSpecRecursive( self.getAttrSpec( inner, "attr%d" % level ), level - 1 )
	


	def testFill(self):
		leaf0 = DocLayoutNodeLeaf( "a,", None, GlyphViewFactory.viewFactory )
		leaf1 = DocLayoutNodeLeaf( "b,", None, GlyphViewFactory.viewFactory )
		leaf2 = DocLayoutNodeLeaf( "c,", None, GlyphViewFactory.viewFactory )
		leaf3 = DocLayoutNodeLeaf( "d,", None, GlyphViewFactory.viewFactory )
		leaf4 = DocLayoutNodeLeaf( "e,", None, GlyphViewFactory.viewFactory )
		leaf5 = DocLayoutNodeLeaf( "f,", None, GlyphViewFactory.viewFactory )
		leaf6 = DocLayoutNodeLeaf( "g,", None, GlyphViewFactory.viewFactory )
		leaves = [ leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 ]
		branch = DocLayoutNodeBranch( None, ParagraphViewFactory.viewFactory )
		branch.setChildren( leaves )
		
		subtree = [ branch ]
		
		self._docLayout.getRoot().setChildren( subtree )
		self._docLayout.refresh()
	
	
	
	def testFill2(self):
		inner = DocLayoutNodeLeaf( "this", None, GlyphViewFactory.viewFactory )
		inner = self.getattrSpecRecursive( inner, 4 )
		inner = self.callSpec( inner )
		inner = self.getattrSpecRecursive( inner, 4 )
		inner = self.callSpec( inner )
		inner = self.getattrSpecRecursive( inner, 4 )
		inner = self.callSpec( inner )
		
		subtree = [ inner ]
		
		self._docLayout.getRoot().setChildren( subtree )
		self._docLayout.refresh()

	
	def run(self):
		self._frame.setVisible( True )
