package ArcticSunDog.server;

import java.util.Observable;

public class ArcticSunDogObserver extends Observable {

	public String _test = "";
	
	public ArcticSunDogObserver() {
	}
	
	public void onChange(String test)
	{
		_test = test;
		setChanged();
		notifyObservers(_test);
	}
}
