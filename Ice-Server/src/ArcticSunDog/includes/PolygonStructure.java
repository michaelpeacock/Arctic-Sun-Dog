package ArcticSunDog.includes;

import java.util.ArrayList;

public class PolygonStructure {
	public boolean is_severe;

	private class MyPoint
	{
		MyPoint(double arg0, double arg1)
		{
			lat = arg0;
			lon = arg1;
		}
		
		double lat;
		double lon;
	};
	
	private ArrayList<MyPoint> points;
	
	public PolygonStructure() {
		is_severe = true;
		points = new ArrayList<MyPoint>();
	}
	
	public void add(double lat, double lon)
	{
		points.add(new MyPoint(lat, lon));
	}
	
	public boolean isTheIceSevere()
	{
		return is_severe;
	}
	
	public ArrayList<MyPoint> getPointsArray()
	{
		return points;
	}

}
