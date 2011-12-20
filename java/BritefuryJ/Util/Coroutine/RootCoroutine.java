//##* This program is free software; you can use it, redistribute it and/or modify it
//##* under the terms of the GNU General Public License version 2 as published by the
//##* Free Software Foundation. The full text of the GNU General Public License
//##* version 2 can be found in the file named 'COPYING' that accompanies this
//##* program. This source code is (C)copyright Geoffrey French 2008-2010.
//##************************
package BritefuryJ.Util.Coroutine;

public class RootCoroutine extends CoroutineBase
{
	protected static final RootCoroutine root = new RootCoroutine();
	
	private Thread thread;
	
	
	private RootCoroutine()
	{
		super( "<root>" );
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
	public boolean isRunning()
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
