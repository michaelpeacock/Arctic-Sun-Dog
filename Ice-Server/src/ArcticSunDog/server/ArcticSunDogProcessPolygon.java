package ArcticSunDog.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ArcticSunDogProcessPolygon extends TimerTask {
	private JSONObject previousData = null;
	private JSONObject currentData = null;
	public ArcticSunDogObserver observer = null;
	
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
			observer.onChange("Ooooh it CHANGED!");
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
