package tests.GSymViewSwing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import BritefuryJ.GSymViewSwing.GSymViewDocument;
import BritefuryJ.GSymViewSwing.GSymViewEditorKit;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayout;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayoutNode;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayoutNodeBranch;
import BritefuryJ.GSymViewSwing.DocLayout.DocLayoutNodeLeaf;
import BritefuryJ.GSymViewSwing.ElementViewFactories.LabelViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.ParagraphViewFactory;
import BritefuryJ.GSymViewSwing.ElementViewFactories.VBoxViewFactory;

public class Test_GSymViewSwing
{
	private JFrame frame;
	private GSymViewDocument document;
	private DocLayout docLayout;
	private Style regular, bold, italic;
	
	
	public static void main(String[] args)
	{
		Test_GSymViewSwing t = new Test_GSymViewSwing();
		t.go();
	}
	
	
	
	protected Test_GSymViewSwing()
	{
		frame = new JFrame( "Text pane test " );
		
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		
		JEditorPane textPane = new JEditorPane();
		
		textPane.setPreferredSize( new Dimension( 640, 480 ) );
		
		GSymViewEditorKit editorKit = new GSymViewEditorKit();
		document = (GSymViewDocument)editorKit.createDefaultDocument();
		
		docLayout = document.getDocumentLayout();
		
		textPane.setEditorKit( editorKit );
		textPane.setDocument( document );
		
		JMenuBar menuBar = createMenus();
		
		frame.setJMenuBar( menuBar );
		
		frame.add( textPane );
		
		frame.pack();

	
	
		Style def = StyleContext.getDefaultStyleContext().getStyle( StyleContext.DEFAULT_STYLE );
		StyleConstants.setFontFamily( def, "SansSerif" );
		//StyleConstants.setFontSize( def, 24 );
		
		regular = document.addStyle( "regular", def );

		bold = document.addStyle( "bold", regular );
		StyleConstants.setBold( bold, true );

		italic = document.addStyle( "italic", regular );
		StyleConstants.setItalic( italic, true );
	}
	
	
	
	
	private JMenuBar createMenus()
	{
		Action fillAction = new AbstractAction( "Fill" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0)
			{
				testFill();
			}
		};
		
		Action fill2Action = new AbstractAction( "Fill2" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0)
			{
				testFill2();
			}
		};
		
		Action dumpAction = new AbstractAction( "Dump" )
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent arg0)
			{
				AbstractDocument.AbstractElement root = (AbstractDocument.AbstractElement)document.getDefaultRootElement(); 
				root.dump( System.out, 2 );
			}
		};
		
		
		
		JMenuItem fillItem = new JMenuItem( fillAction );
		JMenuItem fill2Item = new JMenuItem( fill2Action );
		JMenuItem dumpItem = new JMenuItem( dumpAction );
		
		JMenu testMenu = new JMenu( "Test" );
		testMenu.add( fillItem );
		testMenu.add( fill2Item );
		testMenu.add( dumpItem );
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add( testMenu );
		
		return menuBar;
	}
	
	
	private DocLayoutNode paramSpec()
	{
		DocLayoutNodeLeaf leaf0 = new DocLayoutNodeLeaf( "aaaaaaa,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf1 = new DocLayoutNodeLeaf( "bbbbbbb,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf2 = new DocLayoutNodeLeaf( "ccccccc,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf3 = new DocLayoutNodeLeaf( "ddddddd,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf4 = new DocLayoutNodeLeaf( "eeeeeee,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf5 = new DocLayoutNodeLeaf( "fffffff,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf6 = new DocLayoutNodeLeaf( "ggggggg", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf[] leaves = { leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 };
		DocLayoutNodeBranch branch = new DocLayoutNodeBranch( null, ParagraphViewFactory.viewFactory );
		branch.setChildren( leaves );
		return branch;
	}
	
	private DocLayoutNode callSpec(DocLayoutNode inner)
	{
		DocLayoutNodeLeaf openParen = new DocLayoutNodeLeaf( "(", bold, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf closeParen = new DocLayoutNodeLeaf( ")", bold, LabelViewFactory.viewFactory );
		DocLayoutNode[] innerOpen = { inner, openParen };
		DocLayoutNodeBranch innerOpenBranch = new DocLayoutNodeBranch( null, VBoxViewFactory.viewFactory );
		innerOpenBranch.setChildren( innerOpen );
		DocLayoutNode params = paramSpec();
		DocLayoutNode[] call = { innerOpenBranch, params, closeParen };
		DocLayoutNodeBranch callBranch = new DocLayoutNodeBranch( null, ParagraphViewFactory.viewFactory );
		callBranch.setChildren( call );
		return callBranch;
	}
	
	

	private DocLayoutNode getAttrSpec(DocLayoutNode inner, String attrName)
	{
		DocLayoutNodeLeaf dot = new DocLayoutNodeLeaf( ".", bold, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf attr = new DocLayoutNodeLeaf( attrName, italic, LabelViewFactory.viewFactory );
		DocLayoutNode[] innerDot = { inner, dot };
		DocLayoutNodeBranch innerDotBranch = new DocLayoutNodeBranch( null, VBoxViewFactory.viewFactory );
		innerDotBranch.setChildren( innerDot );
		DocLayoutNode[] getattr = { innerDotBranch, attr };
		DocLayoutNodeBranch getattrBranch = new DocLayoutNodeBranch( null, ParagraphViewFactory.viewFactory );
		getattrBranch.setChildren( getattr );
		return getattrBranch;
	}
	
	
	
	DocLayoutNode getattrSpecRecursive(DocLayoutNode inner, int level)
	{
		if ( level == 0 )
		{
			return inner;
		}
		else
		{
			return getattrSpecRecursive( getAttrSpec( inner, "attr" + String.valueOf( level ) ), level - 1 );
		}
	}
	


	public void testFill()
	{
		DocLayoutNodeLeaf leaf0 = new DocLayoutNodeLeaf( "a,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf1 = new DocLayoutNodeLeaf( "b,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf2 = new DocLayoutNodeLeaf( "c,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf3 = new DocLayoutNodeLeaf( "d,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf4 = new DocLayoutNodeLeaf( "e,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf5 = new DocLayoutNodeLeaf( "f,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf leaf6 = new DocLayoutNodeLeaf( "g,", italic, LabelViewFactory.viewFactory );
		DocLayoutNodeLeaf[] leaves = { leaf0, leaf1, leaf2, leaf3, leaf4, leaf5, leaf6 };
		DocLayoutNodeBranch branch = new DocLayoutNodeBranch( null, ParagraphViewFactory.viewFactory );
		branch.setChildren( leaves );
		
		DocLayoutNode[] subtree = { branch };
		
		docLayout.getRoot().setChildren( subtree );
		docLayout.refresh();
	}
	
	
	
	public void testFill2()
	{
		DocLayoutNode inner = new DocLayoutNodeLeaf( "this", bold, LabelViewFactory.viewFactory );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		inner = getattrSpecRecursive( inner, 4 );
		inner = callSpec( inner );
		
		DocLayoutNode[] subtree = { inner };
		
		docLayout.getRoot().setChildren( subtree );
		docLayout.refresh();
	}
	
	
	protected void go()
	{
		frame.setVisible( true );
	}
}
