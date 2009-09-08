//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008.
//##************************
package tests.DocModel;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import BritefuryJ.DocModel.DMList;
import BritefuryJ.DocModel.DMNode;

public class Test_DMNode extends Test_DMNode_base
{
	public void testParentRef()
	{
		DMList ij = new DMList();
		ij.extend( Arrays.asList( new Object[] { "i", "j" } ) );

		DMList xs = new DMList();
		xs.extend( Arrays.asList( new Object[] { ij, "a", "b" } ) );
		WeakReference<DMList> refXs = new WeakReference<DMList>( xs );

		assertSame( refXs.get(), xs );
		cmpNodeParentsLive( ij, new DMNode[] { xs } );
		
		xs = null;
		System.gc();

		assertSame( refXs.get(), xs );
		cmpNodeParentsLive( ij, new DMNode[] { null } );
	}
}
