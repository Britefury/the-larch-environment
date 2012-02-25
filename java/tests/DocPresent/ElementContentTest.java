//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent;

import java.util.Arrays;

import junit.framework.TestCase;
import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPBlank;
import BritefuryJ.DocPresent.DPElement;
import BritefuryJ.DocPresent.DPHiddenText;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPRow;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.PresentationComponent;

public class ElementContentTest extends TestCase
{
	public void testEmpty()
	{
		DPBlank empty = new DPBlank( );
		
		assertEquals( empty.getTextRepresentation(), "" );
		assertEquals( empty.getTextRepresentationLength(), 0 );
	}
	
	public void testText()
	{
		DPText text = new DPText( "Hello" );
		
		assertEquals( text.getTextRepresentation(), "Hello" );
		assertEquals( text.getTextRepresentationLength(), 5 );
	}
	
	
	public void testBin()
	{
		DPText t0 = new DPText( "abc" );

		DPBin b = new DPBin( );
		
		b.setChild( t0 );
		
		PresentationComponent component = new PresentationComponent();
		component.getRootElement().setChild( b );

		assertEquals( b.getTextRepresentation(), "abc" );
		assertEquals( b.getTextRepresentationLength(), 3 );
	}
	
	
	public void testPara()
	{
		DPText t0 = new DPText( "abc" );
		DPText t1 = new DPText( "ghi" );
		DPText t2 = new DPText( "mno" );
		DPText t3 = new DPText( "stu" );
		
		DPRow p = new DPRow( );
		DPElement[] t = { t0, t1, t2, t3 };
		
		p.setChildren( Arrays.asList( t ) );
		
		PresentationComponent component = new PresentationComponent();
		component.getRootElement().setChild( p );
	
		assertEquals( p.getTextRepresentation(), "abcghimnostu" );
		assertEquals( p.getTextRepresentationLength(), 12 );
	}
	
	
	
	public void testStructure()
	{
		DPText ta0 = new DPText( "abc" );
		DPText ta1 = new DPText( "ghi" );
		DPText ta2 = new DPText( "mno" );
		DPText ta3 = new DPText( "stu" );

		DPRow pa = new DPRow( );
		DPElement[] ta = { ta0, ta1, ta2, ta3 };
		pa.setChildren( Arrays.asList( ta ) );
		
		DPHiddenText e = new DPHiddenText( );
		

	
		DPText tb0 = new DPText( "vw" );
		DPText tb1 = new DPText( "xy" );
		DPText tb2 = new DPText( "z" );

		DPRow pb = new DPRow( );
		DPElement[] tb = { tb0, tb1, tb2 };
		pb.setChildren( Arrays.asList( tb ) );
		
		DPBin b = new DPBin( );
		b.setChild( pb );

		
		
		DPText tx = new DPText( "11" );
		DPText ty = new DPText( "22" );
		
		
		DPRow root = new DPRow( );
		DPElement[] rootChildren = { pa, e, b, tx, ty };
		root.setChildren( Arrays.asList( rootChildren ) );
		
		
		PresentationComponent component = new PresentationComponent();
		component.getRootElement().setChild( root );

		
		// Test getContent() and getContentLength() of all elements
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
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta1.getTextRepresentationOffsetInSubtree( pa ), 3 );
		assertEquals( ta2.getTextRepresentationOffsetInSubtree( pa ), 6 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );

		assertEquals( pb.getTextRepresentationOffsetInSubtree( b ), 0 );
		
		assertEquals( pa.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( e.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( b.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tx.getTextRepresentationOffsetInSubtree( root ), 17 );
		assertEquals( ty.getTextRepresentationOffsetInSubtree( root ), 19 );
		
		// Test getChildAtContentPosition() for containers
		DPElement[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			System.out.println( i );
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( b.getLeafAtTextRepresentationPosition( 0 ), tb0 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 4 ), tb2 );
		assertEquals( b.getLeafAtTextRepresentationPosition( 5 ), null );

		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ),ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for elements
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
		
		
		// Test getContentOffsetInSubtree() for elements
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
		DPText ta0 = new DPText( "abc" );
		DPText ta1 = new DPText( "ghi" );
		DPText ta2 = new DPText( "mno" );
		DPText ta3 = new DPText( "stu" );

		DPParagraph pa = new DPParagraph( );
		DPElement[] ta = { ta0, ta1, ta2, ta3 };
		pa.setChildren( Arrays.asList( ta ) );
		
		DPHiddenText e = new DPHiddenText( );
		

	
		DPText tb0 = new DPText( "vw" );
		DPText tb1 = new DPText( "xy" );
		DPText tb2 = new DPText( "z" );

		DPParagraph pb = new DPParagraph( );
		DPElement[] tb = { tb0, tb1, tb2 };
		pb.setChildren( Arrays.asList( tb ) );
		
		
		DPText tx = new DPText( "11" );
		DPText ty = new DPText( "22" );
		
		
		DPParagraph root = new DPParagraph( );
		DPElement[] rootChildren = { pa, e, pb, tx, ty };
		root.setChildren( Arrays.asList( rootChildren ) );
		
		
		PresentationComponent component = new PresentationComponent();
		component.getRootElement().setChild( root );

		
		assertEquals( root.getTextRepresentation(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getTextRepresentationLength(), 21 );

		
		// Test getContent() and getContentLength() of all elements
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
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta1.getTextRepresentationOffsetInSubtree( pa ), 3 );
		assertEquals( ta2.getTextRepresentationOffsetInSubtree( pa ), 6 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );

		assertEquals( pa.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( e.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tx.getTextRepresentationOffsetInSubtree( root ), 17 );
		assertEquals( ty.getTextRepresentationOffsetInSubtree( root ), 19 );

		// Test getChildAtContentPosition() for containers
		DPElement[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtTextRepresentationPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( root.getLeafAtTextRepresentationPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtTextRepresentationPosition( 17 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 18 ), tx );
		assertEquals( root.getLeafAtTextRepresentationPosition( 19 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 20 ), ty );
		assertEquals( root.getLeafAtTextRepresentationPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for elements
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
		
		
		
		// Test getContentOffsetInSubtree() for elements
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getTextRepresentationOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getTextRepresentationOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getTextRepresentationOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getTextRepresentationOffsetInSubtree( root ), 16 );
	}
}
