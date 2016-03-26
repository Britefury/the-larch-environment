//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.Util.Profile;

import junit.framework.TestCase;
import BritefuryJ.Util.Profile.ProfileTimer;

public class ProfileTimerTest extends TestCase
{
	public void testOneTimer() throws InterruptedException
	{
		ProfileTimer.initProfiling();
		
		ProfileTimer x = new ProfileTimer();
		x.reset();
		x.start();
		Thread.sleep( 100 );
		x.stop();
		
		assertTrue( x.getTime() > 0.09 );
		
		ProfileTimer.shutdownProfiling();
	}



	public void testTwoTimers() throws InterruptedException
	{
		ProfileTimer.initProfiling();
		
		ProfileTimer x = new ProfileTimer();
		ProfileTimer y = new ProfileTimer();
		x.reset();
		y.reset();
		
		x.start();
		Thread.sleep( 100 );
		y.start();
		Thread.sleep( 50 );
		y.stop();
		Thread.sleep( 25 );
		x.stop();
		
		assertTrue( x.getTime() > 0.12 );
		assertTrue( y.getTime() > 0.04 );
		
		ProfileTimer.shutdownProfiling();
	}
}
