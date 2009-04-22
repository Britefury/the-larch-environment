//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent.ElementTree;

import java.util.Arrays;

import BritefuryJ.DocPresent.ElementTree.BinElement;
import BritefuryJ.DocPresent.ElementTree.Element;
import BritefuryJ.DocPresent.ElementTree.EmptyElement;
import BritefuryJ.DocPresent.ElementTree.HBoxElement;
import BritefuryJ.DocPresent.ElementTree.ParagraphElement;
import BritefuryJ.DocPresent.ElementTree.TextElement;
import junit.framework.TestCase;

public class ElementContentTest extends TestCase
{
	public void testEmpty()
	{
		EmptyElement empty = new EmptyElement();
		
		assertEquals( empty.getTextRepresentation(), "" );
		assertEquals( empty.getTextRepresentationLength(), 0 );
	}
	
	public void testText()
	{
		TextElement text = new TextElement( "Hello" );
		
		assertEquals( text.getTextRepresentation(), "Hello" );
		assertEquals( text.getTextRepresentationLength(), 5 );
	}
	
	
	public void testBin()
	{
		TextElement t0 = new TextElement( "abc" );

		BinElement b = new BinElement();
		
		b.setChild( t0 );

		assertEquals( b.getTextRepresentation(), "abc" );
		assertEquals( b.getTextRepresentationLength(), 3 );
	}
	
	
	public void testPara()
	{
		TextElement t0 = new TextElement( "abc" );
		TextElement t1 = new TextElement( "ghi" );
		TextElement t2 = new TextElement( "mno" );
		TextElement t3 = new TextElement( "stu" );
		
		HBoxElement p = new HBoxElement();
		Element[] t = { t0, t1, t2, t3 };
		
		p.setChildren( Arrays.asList( t ) );
		
		
		assertEquals( p.getTextRepresentation(), "abcghimnostu" );
		assertEquals( p.getTextRepresentationLength(), 12 );
	}
	
	
	
	public void testStructure()
	{
		TextElement ta0 = new TextElement( "abc" );
		TextElement ta1 = new TextElement( "ghi" );
		TextElement ta2 = new TextElement( "mno" );
		TextElement ta3 = new TextElement( "stu" );

		HBoxElement pa = new HBoxElement();
		Element[] ta = { ta0, ta1, ta2, ta3 };
		pa.setChildren( Arrays.asList( ta ) );
		
		EmptyElement e = new EmptyElement();
		

	
		TextElement tb0 = new TextElement( "vw" );
		TextElement tb1 = new TextElement( "xy" );
		TextElement tb2 = new TextElement( "z" );

		HBoxElement pb = new HBoxElement();
		Element[] tb = { tb0, tb1, tb2 };
		pb.setChildren( Arrays.asList( tb ) );
		
		BinElement b = new BinElement();
		b.setChild( pb );

		
		
		TextElement tx = new TextElement( "11" );
		TextElement ty = new TextElement( "22" );
		
		
		HBoxElement root = new HBoxElement();
		Element[] rootChildren = { pa, e, b, tx, ty };
		root.setChildren( Arrays.asList( rootChildren ) );
		
		
		// Test getContent() and getContentLength() of all widgets
		assertEquals( ta0.getTextRepresentation(), "abc" );
		assertEquals( ta0.getTextRepresentationLength(), 3 );
		
		assertEquals( pa.getTextRepresentation(), "abcghimnostu" );
		assertEquals( pa.getTextRepresentationLength(), 12 );

		assertEquals( e.getTextRepresentation(), "" );
		assertEquals( e.getTextRepresentationLength(), 0 );

		assertEquals( pb.getTextRepresentation(), "vwxyz" );
		assertEquals( pb.getTextRepresentationLength(), 5 );

		assertEquals( b.getTextRepresentation(), "vwxyz" );
		assertEquals( b.getTextRepresentationLength(), 5 );

		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta0 ), 0 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta1 ), 3 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta2 ), 6 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta3 ), 9 );

		assertEquals( b.getTextRepresentationOffsetOfChild( pb ), 0 );
		
		assertEquals( root.getTextRepresentationOffsetOfChild( pa ), 0 );
		assertEquals( root.getTextRepresentationOffsetOfChild( e ), 12 );
		assertEquals( root.getTextRepresentationOffsetOfChild( b ), 12 );
		assertEquals( root.getTextRepresentationOffsetOfChild( tx ), 17 );
		assertEquals( root.getTextRepresentationOffsetOfChild( ty ), 19 );
		
		// Test getChildAtContentPosition() for containers
		Element[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getChildAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( b.getChildAtTextRepresentationPosition( 0 ), pb );
		assertEquals( b.getChildAtTextRepresentationPosition( 4 ), pb );
		assertEquals( b.getChildAtTextRepresentationPosition( 5 ), null );

		assertEquals( root.getChildAtTextRepresentationPosition( 0 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 1 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 11 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 12 ), b );
		assertEquals( root.getChildAtTextRepresentationPosition( 16 ), b );
		assertEquals( root.getChildAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getChildAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getChildAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getChildAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getChildAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for widgets
		assertSame( ta0.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		
		assertNull( e.getLeafAtTextRepresentationPosition( 0 ) );
		
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 2 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 3 ), ta1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 13 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 14 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 15 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );
		
		assertEquals( b.getLeafAtTextRepresentationPosition( 0 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 1 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 2 ), tb1 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 3 ), tb1 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 4 ), tb2 );
		
		
		// Test getContentOffsetInSubtree() for widgets
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( b ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( b ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( root ), 16 );
	}



	public void testParagraphStructure()
	{
		TextElement ta0 = new TextElement( "abc" );
		TextElement ta1 = new TextElement( "ghi" );
		TextElement ta2 = new TextElement( "mno" );
		TextElement ta3 = new TextElement( "stu" );

		ParagraphElement pa = new ParagraphElement();
		Element[] ta = { ta0, ta1, ta2, ta3 };
		pa.setChildren( Arrays.asList( ta ) );
		
		EmptyElement e = new EmptyElement();
		

	
		TextElement tb0 = new TextElement( "vw" );
		TextElement tb1 = new TextElement( "xy" );
		TextElement tb2 = new TextElement( "z" );

		ParagraphElement pb = new ParagraphElement();
		Element[] tb = { tb0, tb1, tb2 };
		pb.setChildren( Arrays.asList( tb ) );
		
		
		TextElement tx = new TextElement( "11" );
		TextElement ty = new TextElement( "22" );
		
		
		ParagraphElement root = new ParagraphElement();
		Element[] rootChildren = { pa, e, pb, tx, ty };
		root.setChildren( Arrays.asList( rootChildren ) );
		
		
		// Test getContent() and getContentLength() of all widgets
		assertEquals( ta0.getTextRepresentation(), "abc" );
		assertEquals( ta0.getTextRepresentationLength(), 3 );
		
		assertEquals( pa.getTextRepresentation(), "abcghimnostu" );
		assertEquals( pa.getTextRepresentationLength(), 12 );

		assertEquals( e.getTextRepresentation(), "" );
		assertEquals( e.getTextRepresentationLength(), 0 );

		assertEquals( pb.getTextRepresentation(), "vwxyz" );
		assertEquals( pb.getTextRepresentationLength(), 5 );

		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta0 ), 0 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta1 ), 3 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta2 ), 6 );
		assertEquals( pa.getTextRepresentationOffsetOfChild( ta3 ), 9 );

		assertEquals( root.getTextRepresentationOffsetOfChild( pa ), 0 );
		assertEquals( root.getTextRepresentationOffsetOfChild( e ), 12 );
		assertEquals( root.getTextRepresentationOffsetOfChild( tx ), 17 );
		assertEquals( root.getTextRepresentationOffsetOfChild( ty ), 19 );
		
		// Test getChildAtContentPosition() for containers
		Element[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getChildAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( root.getChildAtTextRepresentationPosition( 0 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 1 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 11 ), pa );
		assertEquals( root.getChildAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getChildAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getChildAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getChildAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getChildAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for widgets
		assertSame( ta0.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		
		assertNull( e.getLeafAtTextRepresentationPosition( 0 ) );
		
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 2 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 3 ), ta1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 13 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 14 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 15 ), tb1 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );
		
		
		
		// Test getContentOffsetInSubtree() for widgets
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( root ), 16 );
		
		
		
		// Check that paragraph is working correctly
		assertNotNull( root.getWidget() );
		assertNull( pa.getWidget() );
		assertNull( pb.getWidget() );
	}
}
