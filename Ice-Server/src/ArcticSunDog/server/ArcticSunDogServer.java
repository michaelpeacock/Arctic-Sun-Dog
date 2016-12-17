package ArcticSunDog.server;

import java.util.Timer;

public class ArcticSunDogServer extends Object{
	private static ArcticSunDogProcessPolygon instance = null;
	public ArcticSunDogServer() {
		// TODO Auto-generated constructor stub
	}

	public static ArcticSunDogObserver startServer(int frequency)
	{
		Timer timer = new Timer();
		timer.schedule(instance  = new ArcticSunDogProcessPolygon(), 0, 5000);
		return instance.observer;
	}

}
