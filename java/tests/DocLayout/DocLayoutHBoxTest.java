package tests.DocLayout;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import BritefuryJ.DocLayout.DocLayoutNode;
import BritefuryJ.DocLayout.DocLayoutNodeHBox;
import BritefuryJ.DocLayout.DocLayoutNodeVBox;

public class DocLayoutHBoxTest extends DocLayoutTestBase
{
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected Vector<DocLayoutNode> makeTextNodes(String text, Color colour)
	{
		String[] words = text.split( " " );
		Vector<DocLayoutNode> nodes = new Vector<DocLayoutNode>();
		Font f = new Font( "Sans serif", Font.PLAIN, 12 );
		for (int i = 0; i < words.length; i++)
		{
			String word = words[i];
			if ( i != words.length-1 )
			{
				word = word + " ";
			}
			nodes.add( buildTextNode( word, f, colour ) );
		}
		return nodes;
	}
	
	protected DocLayoutNodeHBox makeParagraphHBox(String title, DocLayoutNodeHBox.Alignment alignment, double spacing, double padding, Color colour)
	{
		Vector<DocLayoutNode> children = makeTextNodes( title + ": " + textBlock, colour );
		DocLayoutNodeHBox box = new DocLayoutNodeHBox( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	

	
	
	protected DocLayoutNodeVBox makeTextVBox(DocLayoutNodeVBox.Alignment alignment, double spacing, double padding)
	{
		DocLayoutNode t1 = buildTextNode( "Vert1", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DocLayoutNode t2 = buildTextNode( "Vert2", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DocLayoutNode t3 = buildTextNode( "Vert3", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DocLayoutNode[] children = { t1, t2, t3 };
		DocLayoutNodeVBox box = new DocLayoutNodeVBox( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	
	protected DocLayoutNodeHBox makeTextHBox(String title, DocLayoutNodeHBox.Alignment alignment, double spacing, double padding)
	{
		DocLayoutNode c0 = buildTextNode( title, new Font( "Sans serif", Font.BOLD, 18 ), Color.red );
		DocLayoutNode c1 = makeTextVBox( DocLayoutNodeVBox.Alignment.LEFT, 0.0, 0.0 );
		DocLayoutNode c2 = buildTextNode( "Hello  ", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode c3 = buildTextNode( "abcdefghijklmnopqrstuvwxyz", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode[] children = { c0, c1, c2, c3 };
		DocLayoutNodeHBox box = new DocLayoutNodeHBox( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	
	
	protected DocLayoutNode createContentNode()
	{
		DocLayoutNode b1 = makeTextHBox( "TOP", DocLayoutNodeHBox.Alignment.TOP, 0.0, 0.0 );
		DocLayoutNode b2 = makeTextHBox( "CENTRE", DocLayoutNodeHBox.Alignment.CENTRE, 0.0, 0.0 );
		DocLayoutNode b3 = makeTextHBox( "BOTTOM", DocLayoutNodeHBox.Alignment.BOTTOM, 0.0, 0.0 );
		DocLayoutNode b4 = makeTextHBox( "EXPAND", DocLayoutNodeHBox.Alignment.EXPAND, 0.0, 0.0 );
		DocLayoutNode b5 = makeTextHBox( "BASELINES", DocLayoutNodeHBox.Alignment.BASELINES, 0.0, 0.0 );
		DocLayoutNode b6 = makeParagraphHBox( "PARAGRAPH", DocLayoutNodeHBox.Alignment.BASELINES, 0.0, 0.0, Color.black );
		DocLayoutNode[] children = { b1, b2, b3, b4, b5, b6 };
		DocLayoutNodeVBox box = new DocLayoutNodeVBox( DocLayoutNodeVBox.Alignment.EXPAND, 10.0, 0.0 );
		box.setChildren( children );
		return box;
	}
	
	
	public static void main(String[] args)
	{
		new DocLayoutHBoxTest();
	}
	
	
	protected String getTitle()
	{
		return "Layout: hbox test";
	}
}
