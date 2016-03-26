//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.Incremental;


public class IncrementalValueMonitor extends IncrementalMonitor
{
	public IncrementalValueMonitor()
	{
		this( null );
	}
	
	public IncrementalValueMonitor(Object owner)
	{
		super( owner );
	}
	
	
	
	public void onAccess()
	{
		notifyRefreshed();
		onValueAccess();
	}
	
	public void onChanged()
	{
		notifyChanged();
	}
}
