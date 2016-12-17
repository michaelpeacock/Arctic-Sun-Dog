package ArcticSunDog.server;

import java.util.ArrayList;
import java.util.Observable;

import ArcticSunDog.includes.PolygonStructure;

public class ArcticSunDogObserver extends Observable {

	
	public ArcticSunDogObserver() {
	}
	
	public void onChange(ArrayList<PolygonStructure> arrayOfPolygons)
	{
		setChanged();
		notifyObservers(arrayOfPolygons);
	}
}
