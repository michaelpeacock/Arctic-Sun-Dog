/*******************************************************************************
 * Copyright (c) 2016 ASRC Federal Mission Solutions.
 * All rights reserved. No warranty, explicit or implicit, provided. This 
 * program and the accompanying materials are proprietary and 
 * confidential. Unauthorized copying or distribution of this file, 
 * via any medium, is strictly prohibited without consent from
 * ASRC Federal Mission Solutions.
 *******************************************************************************/
package com.missionse.tacsit.layers;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import ArcticSunDog.includes.PolygonStructure;
import ArcticSunDog.server.ArcticSunDogObserver;
import ArcticSunDog.server.ArcticSunDogServer;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ScreenRelativeAnnotation;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;
import gov.nasa.worldwindx.examples.lineofsight.ExtrudedPolygonIntersection;

public class IceZoneLayer extends RenderableLayer implements Observer {
	
	private boolean displayed = true;

	private Globe globe;
	HighResolutionTerrain terrain;
	
	ArcticSunDogServer asd_server;

	public class IceZone {
		Boolean is_severe;
		
		
		public class IcePoint {
			IcePoint(double arg0, double arg1) {
				lat = arg0;
				lon = arg1;
			}
			double lat;
			double lon;
		}
		public ArrayList<IcePoint> points;
		public IceZone() {
			is_severe = true;
			points = new ArrayList<IcePoint>();
		}
		public void add(double lat, double lon) {
			points.add(new IcePoint(lat, lon));
		}
		public ArrayList<LatLon> findIntersectionPoints(LatLon p1, LatLon p2) {
			ArrayList<LatLon> result = new ArrayList<LatLon>();
			
			ArrayList<LatLon> poly_points = new ArrayList<LatLon>();

			for (int i=0; i<points.size(); i++) {
				
				poly_points.add(LatLon.fromDegrees(points.get(i).lat, points.get(i).lon));
			}
			ExtrudedPolygon ep = new ExtrudedPolygon(poly_points, 10e3);
			Vec4 vp1 = globe.computePointFromLocation(p1);
        	System.out.printf("p1: %.3f %.3f\n", p1.latitude.degrees, p1.longitude.degrees);
        	System.out.printf("vp1: %.3f %.3f %.3f %.3f\n", vp1.w, vp1.x, vp1.y, vp1.z);
			Vec4 vp2 = globe.computePointFromLocation(p2);
        	System.out.printf("p2: %.3f %.3f\n", p2.latitude.degrees, p2.longitude.degrees);
        	System.out.printf("vp2: %.3f %.3f %.3f %.3f\n", vp2.w, vp2.x, vp2.y, vp2.z);
            try {
                Line line = new Line(vp1, vp2.subtract3(vp1));
            	List<Intersection> intersections = ep.intersect(line, terrain);
            	if (intersections != null) {
                	System.out.println("intersection size: " + intersections.size());
                    for (Intersection intersection : intersections) {
                        Position pos = intersection.getIntersectionPosition();
                    	System.out.printf("intersection pos: %.3f %.3f\n", pos.latitude.degrees, pos.longitude.degrees);
            			SurfaceCircle ip = new SurfaceCircle(
            					LatLon.fromDegrees(pos.latitude.degrees, pos.longitude.degrees), 5000);
            			addRenderable(ip);
                        result.add(LatLon.fromDegrees(pos.latitude.degrees, pos.longitude.degrees));
                    }
            	}
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			return result;
		}
	}
	
	public ArrayList<IceZone> zones;
	
	public IceZoneLayer() {
		this.setName("Ice Zone Layer");
		
		zones = new ArrayList<IceZone>();
		
		// Disable picking for the layer because it covers the full sphere and
		// will override a terrain pick.
		this.setPickEnabled(false);
		
		IceZone zone1 = new IceZone();
		zone1.add(53, 175);
		zone1.add(54, 176);
		zone1.add(55, 177);
		zone1.add(53, 178);
		zone1.add(52, 176);
		zones.add(zone1);
		IceZone zone2 = new IceZone();
		zone2.is_severe = false;
		zone2.add(63, -175);
		zone2.add(64, -176);
		zone2.add(65, -177);
		zone2.add(63, -178);
		zone2.add(62, -176);
		zones.add(zone2);
		drawZones();
		
		asd_server = new ArcticSunDogServer();
		ArcticSunDogObserver observer = asd_server.startServer(5000);
		observer.addObserver(this);
	}
	
	public void setGlobe(Globe g) {
		globe = g;
        terrain = new HighResolutionTerrain(globe, 20d);
        System.out.println("terrain created");
	}
	
	public ArrayList<LatLon> getSevereIntersectionPoints(LatLon p1, LatLon p2) {
		ArrayList<LatLon> result = new ArrayList<LatLon>();
		for (int i=0; i<zones.size(); i++) {
			if (zones.get(i).is_severe) {
				result.addAll(zones.get(i).findIntersectionPoints(p1, p2));
			}
		}
		return result;
	}

	public ArrayList<LatLon> getMildIntersectionPoints(LatLon p1, LatLon p2) {
		ArrayList<LatLon> result = new ArrayList<LatLon>();
		for (int i=0; i<zones.size(); i++) {
			if (!zones.get(i).is_severe) {
				result.addAll(zones.get(i).findIntersectionPoints(p1, p2));
			}
		}
		return result;
	}

	public void drawZones() {
		Iterator<Renderable> it = getActiveRenderables().iterator();
		while (it.hasNext())
		{
			Renderable r = it.next();
			removeRenderable(r);
		}
		
		for (int i=0; i<zones.size(); i++) {
			
			ArrayList<LatLon> ll_list = new ArrayList<LatLon>();
			for (int i2=0; i2<zones.get(i).points.size(); i2++)
			{
				ll_list.add(LatLon.fromDegrees(zones.get(i).points.get(i2).lat, zones.get(i).points.get(i2).lon));
			}
			SurfacePolygon zone_poly = new SurfacePolygon();
			zone_poly.setLocations(ll_list);
			ShapeAttributes attr = new BasicShapeAttributes();
			if (!zones.get(i).is_severe) {
				attr.setOutlineMaterial(Material.YELLOW);
				attr.setInteriorMaterial(Material.YELLOW);
				attr.setInteriorOpacity(0.5);
			}
			else
			{
				attr.setOutlineMaterial(Material.RED);
				attr.setInteriorMaterial(Material.RED);
				attr.setInteriorOpacity(0.5);
			}
			zone_poly.setAttributes(attr);
			this.addRenderable(zone_poly);

		}
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		ArrayList<PolygonStructure> polys = (ArrayList<PolygonStructure>)arg;
		
		zones.clear();

        for (PolygonStructure poly : polys) {
			IceZone z = new IceZone();
			z.is_severe = poly.is_severe;
			for (PolygonStructure.MyPoint mp : poly.getPointsArray())
			{
			  z.add(mp.lat, mp.lon);
			}
			zones.add(z);
		}
        drawZones();
	}

}
