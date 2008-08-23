package tests.Math;

import BritefuryJ.Math.Point2;
import BritefuryJ.Math.Vector2;
import BritefuryJ.Math.Xform2;
import junit.framework.TestCase;

public class Test_Xform2 extends TestCase
{
	public void testTranslate()
	{
		Xform2 x = new Xform2( new Vector2( 10.0, 10.0 ) );
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 5.0, 5.0 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( 15.0, 15.0 ) ) );
	}

	public void testScale()
	{
		Xform2 x = new Xform2( 2.0 );
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 10.0, 10.0 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( 10.0, 10.0 ) ) );
	}

	public void testScaleTranslate()
	{
		Xform2 x = new Xform2( 5.0, new Vector2( 10.0, 10.0 ) );
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 25.0, 25.0 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( 35.0, 35.0 ) ) );
	}


	public void testTranslateInverse()
	{
		Xform2 x = new Xform2( new Vector2( 10.0, 10.0 ) ).inverse();
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 5.0, 5.0 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( -5.0, -5.0 ) ) );
	}

	public void testScaleInverse()
	{
		Xform2 x = new Xform2( 2.0 ).inverse();
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 2.5, 2.5 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( 2.5, 2.5 ) ) );
	}


	public void testScaleTranslateInverse()
	{
		Xform2 x = new Xform2( 5.0, new Vector2( 10.0, 10.0 ) ).inverse();
		
		assertTrue( x.transform( new Vector2( 5.0, 5.0 ) ).equals( new Vector2( 1.0, 1.0 ) ) );
		assertTrue( x.transform( new Point2( 5.0, 5.0 ) ).equals( new Point2( -1.0, -1.0 ) ) );
	}
	
	
	public void testConcat()
	{
		Xform2 a = new Xform2( 5.0, new Vector2( 10.0, 10.0 ) );
		Xform2 b = new Xform2( 2.0, new Vector2( 3.0, 3.0 ) );
		
		assertTrue( a.concat( b ).equals( new Xform2( 10.0, new Vector2( 23.0, 23.0 ) ) ) );
		assertFalse( b.concat( a ).equals( new Xform2( 10.0, new Vector2( 23.0, 23.0 ) ) ) );
		assertTrue( a.concat( a.inverse() ).equals( new Xform2() ) );
	}
}
