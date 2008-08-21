package tests.DocPresent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JFrame;

import BritefuryJ.DocPresent.DPLineBreak;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPPresentationArea;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPVBox;
import BritefuryJ.DocPresent.DPWidget;


public class DPParagraphTest
{
	static String textBlock = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
	
	protected Vector<DPWidget> makeTextNodes(String text, Color colour)
	{
		String[] words = text.split( " " );
		Vector<DPWidget> nodes = new Vector<DPWidget>();
		Font f = new Font( "Sans serif", Font.PLAIN, 12 );
		for (int i = 0; i < words.length; i++)
		{
			String word = words[i];
			if ( i != words.length-1 )
			{
				word = word + " ";
			}
			nodes.add( new DPText( word, f, colour ) );
		}
		return nodes;
	}
	
	protected Vector<DPWidget> addLineBreaks(Vector<DPWidget> nodesIn, int step)
	{
		Vector<DPWidget> nodesOut = new Vector<DPWidget>();
		for (int i = 0; i < nodesIn.size(); i++)
		{
			nodesOut.add( nodesIn.get( i ) );
			if ( step <= 1  ||  i % step == (step-1) )
			{
				nodesOut.add( new DPLineBreak() );
			}
		}
		return nodesOut;
	}
	
	
	protected DPVBox makeTextVBox(DPVBox.Alignment alignment, double spacing, double padding)
	{
		DPWidget t1 = new DPText( "Vert1", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DPWidget t2 = new DPText( "Vert2", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DPWidget t3 = new DPText( "Vert3", new Font( "Sans serif", Font.PLAIN, 12 ), Color.blue );
		DPWidget[] children = { t1, t2, t3 };
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, alignment, spacing, false, padding );
		box.extend( children );
		return box;
	}
	
	protected DPParagraph makeParagraph(String title, DPParagraph.Alignment alignment, double spacing, double padding, double indentation, int lineBreakStep, Color colour)
	{
		Vector<DPWidget> children = makeTextNodes( title + ": " + textBlock, colour );
		if ( lineBreakStep > 0 )
		{
			children = addLineBreaks( children, lineBreakStep );
		}
		DPParagraph box = new DPParagraph( alignment, spacing, padding, indentation );
		box.extend( children );
		return box;
	}
	
	protected DPParagraph makeParagraphWithNestedPara(String title, DPParagraph.Alignment alignment, double spacing, double padding, double indentation, int lineBreakStep, Color colour, Color nestedColour)
	{
		Vector<DPWidget> children = makeTextNodes( title + ": " + textBlock, colour );
		children = addLineBreaks( children, lineBreakStep );
		children.insertElementAt( makeParagraph( title + " (inner)", alignment, spacing, padding, indentation, lineBreakStep, nestedColour ), children.size()/2 );
		DPParagraph box = new DPParagraph( alignment, spacing, padding, indentation );
		box.extend( children );
		return box;
	}
	
	
	protected DPWidget createContentNode()
	{
		DPWidget b1 = makeParagraph( "ONE-LINE", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 0, Color.black );
		DPWidget b2 = makeParagraph( "PER-WORD", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 1, Color.black );
		DPWidget b3 = makeParagraph( "EVERY-4-WORDS", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 4, Color.black );
		DPWidget b4 = makeParagraphWithNestedPara( "NESTED-1", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 1, Color.black, Color.red );
		DPWidget b5 = makeParagraphWithNestedPara( "NESTED-2", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 2, Color.black, Color.red );
		DPWidget b6 = makeParagraphWithNestedPara( "NESTED-4", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 0.0, 4, Color.black, Color.red );
		DPWidget b7 = makeParagraph( "PER-WORD INDENTED", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 50.0, 1, Color.black );
		DPWidget b8 = makeParagraphWithNestedPara( "NESTED-2-INDENTED", DPParagraph.Alignment.BASELINES, 0.0, 0.0, 50.0, 2, Color.black, Color.red );
		DPWidget[] children = { b1, b2, b3, b4, b5, b6, b7, b8 };
		DPVBox box = new DPVBox( DPVBox.Typesetting.NONE, DPVBox.Alignment.EXPAND, 10.0, false, 0.0 );
		box.extend( children );
		return box;
	}



	public DPParagraphTest()
	{
		JFrame frame = new JFrame( "HBox test" );

		//This stops the app on window close.
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		DPPresentationArea area = new DPPresentationArea();
	     
	     
	     
	     
		area.setChild( createContentNode() );
	     
	     
	     
		area.getComponent().setPreferredSize( new Dimension( 640, 480 ) );
		frame.add( area.getComponent() );
		frame.pack();
		frame.setVisible(true);
	}
	
	
	public static void main(String[] args)
	{
		new DPParagraphTest();
	}
}
