package BritefuryJ.LSpace;

import BritefuryJ.Math.Point2;


public class InsertionPoint
{
	private int index;
	private Point2 startPoint, endPoint;


	public InsertionPoint(int index, Point2 startPoint, Point2 endPoint)
	{
		this.index = index;
		this.startPoint = startPoint;
		this.endPoint = endPoint;
	}

	public InsertionPoint(int index, Point2 line[])
	{
		this.index = index;
		this.startPoint = line[0];
		this.endPoint = line[1];
	}


	public int getIndex()
	{
		return index;
	}

	public Point2 getStartPoint()
	{
		return startPoint;
	}

	public Point2 getEndPoint()
	{
		return endPoint;
	}
}
