package BritefuryJ.Math;

public class Xform2 {
	public double scale;
	public Vector2 translation;
	

	public Xform2()
	{
		scale = 1.0;
		translation = new Vector2();
	}
	
	public Xform2(double scale)
	{
		this.scale = scale;
		translation = new Vector2();
	}
	
	public Xform2(Vector2 translation)
	{
		this.scale = 1.0;
		this.translation = translation.clone();
	}
	
	public Xform2(double scale, Vector2 translation)
	{
		this.scale = scale;
		this.translation = translation.clone();
	}
	
	
	
	public Xform2 inverse()
	{
		double invScale = 1.0 / scale;
		return new Xform2( invScale, translation.mul( -invScale ) );
	}
	
	
	public Xform2 concat(Xform2 b)
	{
		return new Xform2( scale*b.scale, translation.mul( b.scale ).add( b.translation ) );
	}
	
	
	public Vector2 transform(Vector2 v)
	{
		return v.mul( scale );
	}

	public Point2 transform(Point2 p)
	{
		return new Point2( p.x * scale + translation.x,  p.y * scale + translation.y );
	}

	public AABox2 transform(AABox2 b)
	{
		return new AABox2( transform( b.getLower() ), transform( b.getUpper() ) );
	}
}
