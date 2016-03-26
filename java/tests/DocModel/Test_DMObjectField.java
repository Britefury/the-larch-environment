//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package tests.DocModel;

import junit.framework.TestCase;
import BritefuryJ.DocModel.DMObjectField;

public class Test_DMObjectField extends TestCase
{
	public void test_getName()
	{
		DMObjectField f = new DMObjectField( "hi" );
		assertEquals( f.getName(), "hi" );
	}
}
