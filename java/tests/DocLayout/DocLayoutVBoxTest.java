package tests.DocLayout;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocLayout.DocLayoutNode;
import BritefuryJ.DocLayout.DocLayoutNodeVBox;

public class DocLayoutVBoxTest extends DocLayoutTestBase
{
	protected DocLayoutNodeVBox makeTextBox(String title, DocLayoutNodeVBox.Alignment alignment, double spacing, double padding)
	{
		DocLayoutNode t0 = buildTextNode( title, new Font( "Sans serif", Font.BOLD, 18 ), Color.red );
		DocLayoutNode t1 = buildTextNode( "Hello", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode t2 = buildTextNode( "world", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode t3 = buildTextNode( "foo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode t4 = buildTextNode( "bar", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode t5 = buildTextNode( "moo", new Font( "Sans serif", Font.PLAIN, 12 ), Color.black );
		DocLayoutNode[] children = { t0, t1, t2, t3, t4, t5 };
		DocLayoutNodeVBox box = new DocLayoutNodeVBox( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	
	
	protected DocLayoutNode createContentNode()
	{
		DocLayoutNode b1 = makeTextBox( "LEFT", DocLayoutNodeVBox.Alignment.LEFT, 0.0, 0.0 );
		DocLayoutNode b2 = makeTextBox( "CENTRE", DocLayoutNodeVBox.Alignment.CENTRE, 0.0, 0.0 );
		DocLayoutNode b3 = makeTextBox( "RIGHT", DocLayoutNodeVBox.Alignment.RIGHT, 0.0, 0.0 );
		DocLayoutNode b4 = makeTextBox( "EXPAND", DocLayoutNodeVBox.Alignment.EXPAND, 0.0, 0.0 );
		DocLayoutNode[] children = { b1, b2, b3, b4 };
		DocLayoutNodeVBox box = new DocLayoutNodeVBox( DocLayoutNodeVBox.Alignment.EXPAND, 10.0, 0.0 );
		box.setChildren( children );
		return box;
	}
	
	
	public static void main(String[] args)
	{
		new DocLayoutVBoxTest();
	}
	
	
	protected String getTitle()
	{
		return "Layout: vbox test";
	}
}
