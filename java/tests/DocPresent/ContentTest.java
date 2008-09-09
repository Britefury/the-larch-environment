//##************************
//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 1999-2008.
//##************************
package tests.DocPresent;

import BritefuryJ.DocPresent.DPBin;
import BritefuryJ.DocPresent.DPEmpty;
import BritefuryJ.DocPresent.DPParagraph;
import BritefuryJ.DocPresent.DPText;
import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Marker.Marker;
import junit.framework.TestCase;

public class ContentTest extends TestCase
{
	public void testEmpty()
	{
		DPEmpty empty = new DPEmpty();
		
		assertEquals( empty.getContent(), "" );
		assertEquals( empty.getContentLength(), 0 );
	}
	
	public void testText()
	{
		DPText text = new DPText( "Hello" );
		
		assertEquals( text.getContent(), "Hello" );
		assertEquals( text.getContentLength(), 5 );
		
		
		text.setContent( "test" );

		assertEquals( text.getContent(), "test" );
		assertEquals( text.getContentLength(), 4 );
		
		text.insertContent( text.marker( 1, Marker.Bias.START ), "abc" );

		assertEquals( text.getContent(), "tabcest" );
		assertEquals( text.getContentLength(), 7 );
		
		text.removeContent( text.marker( 1, Marker.Bias.START ), 2 );

		assertEquals( text.getContent(), "tcest" );
		assertEquals( text.getContentLength(), 5 );
		
		text.replaceContent( text.marker( 1, Marker.Bias.START ), 2, "xyz" );

		assertEquals( text.getContent(), "txyzst" );
		assertEquals( text.getContentLength(), 6 );
	}
	
	
	public void testBin()
	{
		DPText t0 = new DPText( "abc" );

		DPBin b = new DPBin();
		
		b.setChild( t0 );

		assertEquals( b.getContent(), "abc" );
		assertEquals( b.getContentLength(), 3 );
	}
	
	
	public void testPara()
	{
		DPText t0 = new DPText( "abc" );
		DPText t1 = new DPText( "ghi" );
		DPText t2 = new DPText( "mno" );
		DPText t3 = new DPText( "stu" );
		
		DPParagraph p = new DPParagraph();
		DPWidget[] t = { t0, t1, t2, t3 };
		
		p.extend( t );
		
		
		assertEquals( p.getContent(), "abcghimnostu" );
		assertEquals( p.getContentLength(), 12 );
	}
	
	
	
	public void testStructure()
	{
		DPText ta0 = new DPText( "abc" );
		DPText ta1 = new DPText( "ghi" );
		DPText ta2 = new DPText( "mno" );
		DPText ta3 = new DPText( "stu" );

		DPParagraph pa = new DPParagraph();
		DPWidget[] ta = { ta0, ta1, ta2, ta3 };
		pa.extend( ta );
		
		DPEmpty e = new DPEmpty();
		

	
		DPText tb0 = new DPText( "vw" );
		DPText tb1 = new DPText( "xy" );
		DPText tb2 = new DPText( "z" );

		DPParagraph pb = new DPParagraph();
		DPWidget[] tb = { tb0, tb1, tb2 };
		pb.extend( tb );
		
		DPBin b = new DPBin();
		b.setChild( pb );

		
		
		DPText tx = new DPText( "11" );
		DPText ty = new DPText( "22" );
		
		
		DPParagraph root = new DPParagraph();
		DPWidget[] rootChildren = { pa, e, b, tx, ty };
		root.extend( rootChildren );
		
		
		// Test getContent() and getContentLength() of all widgets
		assertEquals( ta0.getContent(), "abc" );
		assertEquals( ta0.getContentLength(), 3 );
		
		assertEquals( pa.getContent(), "abcghimnostu" );
		assertEquals( pa.getContentLength(), 12 );

		assertEquals( e.getContent(), "" );
		assertEquals( e.getContentLength(), 0 );

		assertEquals( pb.getContent(), "vwxyz" );
		assertEquals( pb.getContentLength(), 5 );

		assertEquals( b.getContent(), "vwxyz" );
		assertEquals( b.getContentLength(), 5 );

		assertEquals( root.getContent(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getContentLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( pa.getContentOffsetOfChild( ta0 ), 0 );
		assertEquals( pa.getContentOffsetOfChild( ta1 ), 3 );
		assertEquals( pa.getContentOffsetOfChild( ta2 ), 6 );
		assertEquals( pa.getContentOffsetOfChild( ta3 ), 9 );

		assertEquals( b.getContentOffsetOfChild( pb ), 0 );
		
		assertEquals( root.getContentOffsetOfChild( pa ), 0 );
		assertEquals( root.getContentOffsetOfChild( e ), 12 );
		assertEquals( root.getContentOffsetOfChild( b ), 12 );
		assertEquals( root.getContentOffsetOfChild( tx ), 17 );
		assertEquals( root.getContentOffsetOfChild( ty ), 19 );
		
		// Test getChildAtContentPosition() for containers
		DPWidget[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getChildAtContentPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( b.getChildAtContentPosition( 0 ), pb );
		assertEquals( b.getChildAtContentPosition( 4 ), pb );
		assertEquals( b.getChildAtContentPosition( 5 ), null );

		assertEquals( root.getChildAtContentPosition( 0 ), pa );
		assertEquals( root.getChildAtContentPosition( 1 ), pa );
		assertEquals( root.getChildAtContentPosition( 11 ), pa );
		assertEquals( root.getChildAtContentPosition( 12 ), b );
		assertEquals( root.getChildAtContentPosition( 16 ), b );
		assertEquals( root.getChildAtContentPosition( 17 ), tx );
		assertEquals( root.getChildAtContentPosition( 18 ), tx );
		assertEquals( root.getChildAtContentPosition( 19 ), ty );
		assertEquals( root.getChildAtContentPosition( 20 ), ty );
		assertEquals( root.getChildAtContentPosition( 21 ), null );

		
		// Test getLeafAtContentPosition() for widgets
		assertSame( ta0.getLeafAtContentPosition( 0 ), ta0 );
		
		assertNull( e.getLeafAtContentPosition( 0 ) );
		
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getLeafAtContentPosition( i ), getChildAtContentPositionResultsPA[i] );
		}

		assertEquals( root.getLeafAtContentPosition( 0 ), ta0 );
		assertEquals( root.getLeafAtContentPosition( 1 ), ta0 );
		assertEquals( root.getLeafAtContentPosition( 2 ), ta0 );
		assertEquals( root.getLeafAtContentPosition( 3 ), ta1 );
		assertEquals( root.getLeafAtContentPosition( 11 ), ta3 );
		assertEquals( root.getLeafAtContentPosition( 12 ), tb0 );
		assertEquals( root.getLeafAtContentPosition( 13 ), tb0 );
		assertEquals( root.getLeafAtContentPosition( 14 ), tb1 );
		assertEquals( root.getLeafAtContentPosition( 15 ), tb1 );
		assertEquals( root.getLeafAtContentPosition( 16 ), tb2 );
		assertEquals( root.getLeafAtContentPosition( 17 ), tx );
		assertEquals( root.getLeafAtContentPosition( 18 ), tx );
		assertEquals( root.getLeafAtContentPosition( 19 ), ty );
		assertEquals( root.getLeafAtContentPosition( 20 ), ty );
		assertEquals( root.getLeafAtContentPosition( 21 ), null );
		
		
		
		// Test getContentOffsetInSubtree() for widgets
		assertEquals( ta0.getContentOffsetInSubtree( pa ), 0 );
		assertEquals( ta3.getContentOffsetInSubtree( pa ), 9 );
		assertEquals( ta0.getContentOffsetInSubtree( root ), 0 );
		assertEquals( ta3.getContentOffsetInSubtree( root ), 9 );
		assertEquals( tb0.getContentOffsetInSubtree( pb ), 0 );
		assertEquals( tb2.getContentOffsetInSubtree( pb ), 4 );
		assertEquals( tb0.getContentOffsetInSubtree( b ), 0 );
		assertEquals( tb2.getContentOffsetInSubtree( b ), 4 );
		assertEquals( tb0.getContentOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getContentOffsetInSubtree( root ), 16 );
	}
}
