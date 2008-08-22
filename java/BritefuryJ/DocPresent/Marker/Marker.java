package BritefuryJ.DocPresent.Marker;

import BritefuryJ.DocPresent.DPLeaf;

public class Marker
{
	public enum Bias { START, END };
	
	
	protected DPLeaf widget;
	protected int position;
	protected Bias bias;
	protected MarkerListener listener;
	
	
	
	public Marker()
	{
		this.widget = null;
		this.position = 0;
		this.bias = Bias.START;
	}
	
	public Marker(DPLeaf widget, int position, Bias bias)
	{
		this.widget = widget;
		this.position = position;
		this.bias = bias;
	}
	
	
	public void setMarkerListener(MarkerListener listener)
	{
		this.listener = listener;
	}
	
	
	
	public DPLeaf getWidget()
	{
		return widget;
	}
	
	public int getPosition()
	{
		return position;
	}
	
	public Bias getBias()
	{
		return bias;
	}
	
	
	public void setPosition(int position)
	{
		this.position = position;
		changed();
	}
	
	public void setPositionAndBias(int position, Bias bias)
	{
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	public void set(DPLeaf widget, int position, Bias bias)
	{
		this.widget = widget;
		this.position = position;
		this.bias = bias;
		changed();
	}
	
	
	public int getIndex()
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	public static int getIndex(int position, Bias bias)
	{
		return bias == Bias.END  ?  position + 1  :  position;
	}
	
	
	
	protected void changed()
	{
		if ( listener != null )
		{
			listener.markerChanged( this );
		}
	}




	public Marker clone()
	{
		try {
			return (Marker)super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}
}
