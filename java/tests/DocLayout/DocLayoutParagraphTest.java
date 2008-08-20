package tests.DocLayout;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import BritefuryJ.DocLayout.DocLayoutNode;
import BritefuryJ.DocLayout.DocLayoutNodeLineBreak;
import BritefuryJ.DocLayout.DocLayoutNodeParagraph;
import BritefuryJ.DocLayout.DocLayoutNodeVBox;




public class DocLayoutParagraphTest extends DocLayoutTestBase
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
	
	protected Vector<DocLayoutNode> addLineBreaks(Vector<DocLayoutNode> nodesIn, int step)
	{
		Vector<DocLayoutNode> nodesOut = new Vector<DocLayoutNode>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( new DocLayoutNodeLineBreak() );
			}
		}
		return nodesOut;
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
	
	protected DocLayoutNodeParagraph makeParagraph(String title, DocLayoutNodeParagraph.Alignment alignment, double spacing, double padding, int lineBreakStep, Color colour)
	{
		Vector<DocLayoutNode> children = makeTextNodes( title + ": " + textBlock, colour );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		DocLayoutNodeParagraph box = new DocLayoutNodeParagraph( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	
	protected DocLayoutNodeParagraph makeParagraphWithNestedPara(String title, DocLayoutNodeParagraph.Alignment alignment, double spacing, double padding, int lineBreakStep, Color colour, Color nestedColour)
	{
		Vector<DocLayoutNode> children = makeTextNodes( title + ": " + textBlock, colour );
		children = addLineBreaks( children, lineBreakStep );
		children.insertElementAt( makeParagraph( title + " (inner)", alignment, spacing, padding, lineBreakStep, nestedColour ), children.size()/2 );
		DocLayoutNodeParagraph box = new DocLayoutNodeParagraph( alignment, spacing, padding );
		box.setChildren( children );
		return box;
	}
	
	
	protected DocLayoutNode createContentNode()
	{
		DocLayoutNode b1 = makeParagraph( "ONE-LINE", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 0, Color.black );
		DocLayoutNode b2 = makeParagraph( "PER-WORD", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 1, Color.black );
		DocLayoutNode b3 = makeParagraph( "EVERY-4-WORDS", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 4, Color.black );
		DocLayoutNode b4 = makeParagraphWithNestedPara( "NESTED-1", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 1, Color.black, Color.red );
		DocLayoutNode b5 = makeParagraphWithNestedPara( "NESTED-2", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 2, Color.black, Color.red );
		DocLayoutNode b6 = makeParagraphWithNestedPara( "NESTED-4", DocLayoutNodeParagraph.Alignment.BASELINES, 0.0, 0.0, 4, Color.black, Color.red );
		DocLayoutNode[] children = { b1, b2, b3, b4, b5, b6 };
		DocLayoutNodeVBox box = new DocLayoutNodeVBox( DocLayoutNodeVBox.Alignment.EXPAND, 10.0, 0.0 );
		box.setChildren( children );
		return box;
	}
	
	
	public static void main(String[] args)
	{
		new DocLayoutParagraphTest();
	}
	
	
	protected String getTitle()
	{
		return "Layout: paragraph test";
	}
}
