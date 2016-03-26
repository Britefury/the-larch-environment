//##************************
//##* This source code is (C)copyright Geoffrey French 2008-2016 and is
//##* licensed under the MIT license, a copy of which can be found in
//##* the file 'COPYING'.
//##************************
package BritefuryJ.LSpace;


public abstract class EditEvent
{
	private SequentialRichStringVisitor richStringVisitor;
	
	
	protected EditEvent()
	{
		richStringVisitor = new SequentialRichStringVisitor();
	}
	
	protected EditEvent(SequentialRichStringVisitor richStringVisitor)
	{
		this.richStringVisitor = richStringVisitor;
	}
	
	
	public SequentialRichStringVisitor getRichStringVisitor()
	{
		return richStringVisitor;
	}
}
