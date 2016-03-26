//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util;


public interface UnaryFn
{
	Object invoke(Object x);

	
	
	public static final UnaryFn identity = new UnaryFn()
	{
		@Override
		public Object invoke(Object x)
		{
			return x;
		}
	};
}