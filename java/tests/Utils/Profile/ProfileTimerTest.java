//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.Utils.Profile;

import BritefuryJ.Utils.Profile.ProfileTimer;
import junit.framework.TestCase;

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
