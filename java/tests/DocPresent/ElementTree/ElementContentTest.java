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
		
		assertEquals( empty.getContent(), "" );
		assertEquals( empty.getContentLength(), 0 );
	}
	
	public void testText()
	{
		TextElement text = new TextElement( "Hello" );
		
		assertEquals( text.getContent(), "Hello" );
		assertEquals( text.getContentLength(), 5 );
	}
	
	
	public void testBin()
	{
		TextElement t0 = new TextElement( "abc" );

		BinElement b = new BinElement();
		
		b.setChild( t0 );

		assertEquals( b.getContent(), "abc" );
		assertEquals( b.getContentLength(), 3 );
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
		
		
		assertEquals( p.getContent(), "abcghimnostu" );
		assertEquals( p.getContentLength(), 12 );
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
		Element[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
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
		assertEquals( ta0.getContent(), "abc" );
		assertEquals( ta0.getContentLength(), 3 );
		
		assertEquals( pa.getContent(), "abcghimnostu" );
		assertEquals( pa.getContentLength(), 12 );

		assertEquals( e.getContent(), "" );
		assertEquals( e.getContentLength(), 0 );

		assertEquals( pb.getContent(), "vwxyz" );
		assertEquals( pb.getContentLength(), 5 );

		assertEquals( root.getContent(), "abcghimnostuvwxyz1122" );
		assertEquals( root.getContentLength(), 21 );
		
		
		// Test getContentOffsetOfChild() for containers
		assertEquals( pa.getContentOffsetOfChild( ta0 ), 0 );
		assertEquals( pa.getContentOffsetOfChild( ta1 ), 3 );
		assertEquals( pa.getContentOffsetOfChild( ta2 ), 6 );
		assertEquals( pa.getContentOffsetOfChild( ta3 ), 9 );

		assertEquals( root.getContentOffsetOfChild( pa ), 0 );
		assertEquals( root.getContentOffsetOfChild( e ), 12 );
		assertEquals( root.getContentOffsetOfChild( tx ), 17 );
		assertEquals( root.getContentOffsetOfChild( ty ), 19 );
		
		// Test getChildAtContentPosition() for containers
		Element[] getChildAtContentPositionResultsPA = { ta0, ta0, ta0, ta1, ta1, ta1, ta2, ta2, ta2, ta3, ta3, ta3, null };
		for (int i = 0; i < getChildAtContentPositionResultsPA.length; i++)
		{
			assertSame( pa.getChildAtContentPosition( i ), getChildAtContentPositionResultsPA[i] );
		}
		
		assertEquals( root.getChildAtContentPosition( 0 ), pa );
		assertEquals( root.getChildAtContentPosition( 1 ), pa );
		assertEquals( root.getChildAtContentPosition( 11 ), pa );
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
		assertEquals( tb0.getContentOffsetInSubtree( root ), 12 );
		assertEquals( tb2.getContentOffsetInSubtree( root ), 16 );
		
		
		
		// Check that paragraph is working correctly
		assertNotNull( root.getWidget() );
		assertNull( pa.getWidget() );
		assertNull( pb.getWidget() );
	}
}
