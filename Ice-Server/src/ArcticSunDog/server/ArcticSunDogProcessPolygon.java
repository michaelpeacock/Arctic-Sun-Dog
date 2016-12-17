package ArcticSunDog.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ArcticSunDog.includes.PolygonStructure;

public class ArcticSunDogProcessPolygon extends TimerTask {
	private JSONObject previousData = null;
	private JSONObject currentData = null;
	public ArcticSunDogObserver observer = null;
	private ArrayList<PolygonStructure> arrayOfPolygons = new ArrayList<PolygonStructure>();
	
	public ArcticSunDogProcessPolygon() {
		observer = new ArcticSunDogObserver();
		previousData = new JSONObject();
		currentData = new JSONObject();
		updateJsonData();
	}

	@Override
	public void run() {
		System.out.println("HEY!");
		updateJsonData();
		if(hasChanged())
		{
			PolygonStructure polygon = new PolygonStructure();
			try {
				polygon.is_severe = currentData.getJSONObject("dangerous").getBoolean("dangerous");
				for(int i = 0; i < currentData.getJSONArray("latlon").length(); ++i)
				{
					JSONObject currentPoint = (JSONObject) currentData.getJSONArray("latlon").get(i);
					double currentLat = (double)currentPoint.get("latitude");
					double currentLon = (double)currentPoint.get("longitude");
					polygon.add(currentLat, currentLon);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			arrayOfPolygons.add(polygon);
			observer.onChange(arrayOfPolygons); // Return VectorList
		}
		previousData = currentData;
	}
	
	private void updateJsonData()
	{
		URL serverURL = null;
		try {
			serverURL = new URL("http://ec2-54-244-56-237.us-west-2.compute.amazonaws.com/Public/polygon.json");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		JSONTokener tokener = null;
		try {
			tokener = new JSONTokener(serverURL.openStream());
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
		try {
			currentData = new JSONObject(tokener);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasChanged()
	{
		if(previousData.toString().equals(currentData.toString()))
		{
			System.out.println("The JSON files are the Same");
			return false;
		}
		else
		{
			System.out.println("The JSON files HAVE CHANGED!");
			return true;
		}
	}
}
