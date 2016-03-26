//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Dispatch;

public class DispatchError extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public DispatchError()
	{
		super();
	}
	
	public DispatchError(String message)
	{
		super( message );
	}
}
