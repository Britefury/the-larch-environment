package BritefuryJ.DocPresent;

import BritefuryJ.DocPresent.DPWidget;
import BritefuryJ.DocPresent.Event.PointerButtonEvent;
import BritefuryJ.DocPresent.Input.PointerInterface;
import BritefuryJ.Math.Point2;





public class DndDrag
{
	protected PointerInterface pointer;
	
	protected int button;
	protected int modifiers;
	
	protected DPWidget srcWidget;
	protected Point2 srcLocalPos;
	
	protected Object beginData, dragData;
	
	protected boolean bInProgress;
	
	
	public DndDrag(DPWidget widget, PointerButtonEvent event)
	{
		pointer = event.pointer;
		button = event.button;
		modifiers = pointer.getModifiers();
		
		srcWidget = widget;
		srcLocalPos = pointer.getLocalPos().clone();
	}
	
	
	public int getButton()
	{
		return button;
	}
	
	public int getModifiers()
	{
		return modifiers;
	}
	
	
	public DPWidget getSourceWidget()
	{
		return srcWidget;
	}
	
	public Point2 getSourceLocalPosition()
	{
		return srcLocalPos;
	}
	
	
	public Object getBeginData()
	{
		return beginData;
	}
	
	public Object getDragData()
	{
		return dragData;
	}
}
