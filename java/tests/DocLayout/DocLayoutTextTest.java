package tests.DocLayout;

import java.awt.Color;
import java.awt.Font;

import BritefuryJ.DocLayout.DocLayoutNode;

public class DocLayoutTextTest extends DocLayoutTestBase
{
	protected DocLayoutNode createContentNode()
	{
		return buildTextNode( "Hello world", new Font( "Sans serif", Font.PLAIN, 12 ), Color.red );
	}
	
	
	public static void main(String[] args)
	{
		new DocLayoutTextTest();
	}


	protected String getTitle()
	{
		return "Layout: text test";
	}
}
