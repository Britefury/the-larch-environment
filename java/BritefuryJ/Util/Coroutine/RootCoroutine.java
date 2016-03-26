//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Util.Coroutine;

public class RootCoroutine extends CoroutineBase
{
	protected static final RootCoroutine root = new RootCoroutine();
	
	private Thread thread;
	
	
	private RootCoroutine()
	{
		super( "<root>", null );
	}
	
	
	
	public static RootCoroutine getRootCoroutine()
	{
		return root;
	}
	
	
	
	@Override
	protected void initialise()
	{
	}
	
	@Override
	protected Thread getThread()
	{
		return thread;
	}
	
	@Override
	protected boolean isRoot()
	{
		return true;
	}
	
	
	@Override
	public boolean hasStarted()
	{
		return true;
	}
	
	@Override
	public boolean isFinished()
	{
		return false;
	}
	
	
	@Override
	protected boolean isTerminated()
	{
		return false;
	}
	
	
	protected static RootCoroutine forThread(Thread t)
	{
		root.thread = t;
		return root;
	}
}
