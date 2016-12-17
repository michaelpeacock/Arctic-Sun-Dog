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
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.SurfacePolyline;

public class ShipLayer extends RenderableLayer {
	private boolean displayed = true;

	public final static String IMAGE_PATH = "resources/images/falcon.png";
	
	IceZoneLayer ice_layer;
	HeaderLayer header_layer;
	
	// Waypoints are a series of SurfaceCircles connected with a SurfacePolyLine
	// The first waypoint is the ship's current position
	ArrayList<LatLon> wp_latlon;
	SurfacePolyline wp_line;
	
	SurfaceImage ship_image;
	double ship_speed = 0.0;
	
	public ShipLayer() {
		this.setName("Ship Layer");
		Timer timer = new Timer();
		
		wp_latlon = new ArrayList<LatLon>();
		wp_line = new SurfacePolyline();
		ShapeAttributes attr = new BasicShapeAttributes();
		attr.setOutlineMaterial(Material.WHITE);
		attr.setInteriorMaterial(Material.WHITE);
		wp_line.setAttributes(attr);
		
		double lat = 58;
		double lon = -170;
		ship_image = new SurfaceImage(IMAGE_PATH, Sector.fromDegrees(lat-0.5, lat+0.5, lon-0.5, lon+0.5));
		addRenderable(ship_image);
		setFirstWaypoint(LatLon.fromDegrees(lat,  lon));
//		setShipPosition(LatLon.fromDegrees(58,  170));
				
		this.addRenderable(wp_line);
		
		drawWaypoints();
              
		// Disable picking for the layer because it covers the full sphere and
		// will override a terrain pick.
		this.setPickEnabled(false);

		timer.scheduleAtFixedRate(new TimerTask() {
			  @Override
			  public void run() {
					moveShip();
					drawWaypoints();
			    // Your database code here
			  }
			}, 10000, 2000);

	}
	
	private void setFirstWaypoint(LatLon pos) {
		if (wp_latlon.size() == 0)
		{
			wp_latlon.add(pos);
		}
		else
		{
			wp_latlon.set(0, pos);
		}
		System.out.printf("ship pos: %.3f, %.3f\n",
				wp_latlon.get(0).latitude.degrees,
				wp_latlon.get(0).longitude.degrees);
	}
	
	private void drawWaypoints() {
		if (null != ice_layer) {
			ice_layer.drawZones();
		}
		Iterator<Renderable> it = getActiveRenderables().iterator();
		while (it.hasNext())
		{
			Renderable r = it.next();
			if ((r != ship_image))
			{
				removeRenderable(r);
			}
		}
		wp_line.setLocations(wp_latlon);
		this.addRenderable(wp_line);

		Boolean severe = false;
		Boolean mild = false;
		
		for (int i=1; i<wp_latlon.size(); i++)
		{
			SurfaceCircle wp = new SurfaceCircle(wp_latlon.get(i), 5000);
			ShapeAttributes attr = new BasicShapeAttributes();
			attr.setOutlineMaterial(Material.WHITE);
			attr.setInteriorMaterial(Material.WHITE);
			wp.setAttributes(attr);
			addRenderable(wp);
			System.out.printf("comparing points %d and %d of %d\n", i-1, i, wp_latlon.size());
			ArrayList<LatLon> severe_points = ice_layer.getSevereIntersectionPoints(wp_latlon.get(i-1), wp_latlon.get(i));
			ArrayList<LatLon> mild_points = ice_layer.getMildIntersectionPoints(wp_latlon.get(i-1), wp_latlon.get(i));
			if (severe_points.size() > 0)
			{
				severe = true;
			}
			else if (mild_points.size() > 0)
			{
				mild = true;
			}
		}
		if (null != header_layer)
		{
			if (severe)
			{
				header_layer.setAlertText("!! IMPASSABLE ICE WARNING !!");
			}
			else if (mild)
			{
				header_layer.setAlertText("PASSABLE ICE WARNING");
			}
			else
			{
				header_layer.setAlertText("");
			}
		}
	}
	
	private void removeFirstWaypoint()	{
		wp_latlon.remove(1);
	}

	private void addWaypoint(LatLon latlon)	{
		wp_latlon.add(latlon);
		if (wp_latlon.size() == 2)
		{
			this.addRenderable(wp_line);
		}
		System.out.printf("new wp: %.3f, %.3f\n", 
				wp_latlon.get(wp_latlon.size()-1).latitude.degrees,
				wp_latlon.get(wp_latlon.size()-1).longitude.degrees);
	}
	
	private void clearAllWaypoints() {
		LatLon ship_pos = wp_latlon.get(0);
		wp_latlon.clear();
		wp_latlon.add(ship_pos);
	}

	private void moveShip() {
		if (wp_latlon.size() > 1)
		{
			Angle distance = LatLon.greatCircleDistance(wp_latlon.get(0),  wp_latlon.get(1));
			System.out.println("distance to first waypoint is: " + distance.degrees);
			System.out.println("ship speed is: " + ship_speed);
			double lat1 = wp_latlon.get(0).latitude.degrees;
			double lon1 = wp_latlon.get(0).longitude.degrees;
			double lat2 = wp_latlon.get(1).latitude.degrees;
			double lon2 = wp_latlon.get(1).longitude.degrees;
			
			LatLon new_pos = wp_latlon.get(1);
			if (ship_speed > 0.0)
			{
				LatLon pos1 = wp_latlon.get(0);
				LatLon pos2 = wp_latlon.get(1);
				if (ship_speed < distance.degrees)
				{
					if (lon1 < 0.0)
					{
						pos1 = LatLon.fromDegrees(lat1, lon1 + 360.0);
					}
					if (lon2 < 0.0)
					{
						pos2 = LatLon.fromDegrees(lat2, lon2 + 360.0);
					}
					
					new_pos = LatLon.interpolate(ship_speed/distance.degrees, pos1, pos2);
					System.out.printf("pos1: %.3f, %.3f\n", 
							pos1.latitude.degrees,
							pos1.longitude.degrees);
					System.out.printf("pos2: %.3f, %.3f\n", 
							pos2.latitude.degrees,
							pos2.longitude.degrees);
					if (new_pos.longitude.degrees > 180.0)
					{
						new_pos = Position.fromDegrees(new_pos.latitude.degrees, new_pos.longitude.degrees - 360.0);
					}
					System.out.printf("new : %.3f, %.3f\n", 
							new_pos.latitude.degrees,
							new_pos.longitude.degrees);
				}

				ship_image.moveTo(Position.fromDegrees(new_pos.latitude.degrees, new_pos.longitude.degrees));

				setFirstWaypoint(new_pos);
				
				if (new_pos == wp_latlon.get(1))
				{
					removeFirstWaypoint();
				}
			}
		}
	}
	
	private void setShipPosition(LatLon pos) {
		ship_image.moveTo(Position.fromDegrees(pos.latitude.degrees, pos.longitude.degrees));
		System.out.printf("SHIP: %.3f, %.3f\n", 
				pos.latitude.degrees,
				pos.longitude.degrees);
		
		setFirstWaypoint(pos);
	}

	public void setIceLayer(IceZoneLayer ice_layer) {
		this.ice_layer = ice_layer;
	}
	
	public void setHeaderLayer(HeaderLayer header_layer) {
		this.header_layer = header_layer;
	}

	public KeyListener getKeyListener(WorldWindowGLJPanel wwd) {
	  return new KeyListener(){

		public void keyPressed(KeyEvent arg0) {}

		public void keyReleased(KeyEvent arg0) {}
		
		public void keyTyped(KeyEvent arg0) {

			// Add New Waypoint 'W' or 'w'
			if (arg0.getKeyChar() == 'w' || arg0.getKeyChar() == 'W')
			{
				addWaypoint(wwd.getCurrentPosition());
				drawWaypoints();
			}
			
			// Clear All Waypoints 'C' or 'c'
			else if (arg0.getKeyChar() == 'c' || arg0.getKeyChar() == 'C')
			{
				clearAllWaypoints();
				drawWaypoints();
			}
			
			// Set Ship Position 'S' or 's'
			else if (arg0.getKeyChar() == 's' || arg0.getKeyChar() == 'S')
			{
				setShipPosition(wwd.getCurrentPosition());
				drawWaypoints();
			}

			else if (arg0.getKeyChar() == '.' || arg0.getKeyChar() == '>')
			{
				ship_speed = ship_speed + 0.1;
				System.out.println("ship speed is: " + ship_speed);
			}
			
			else if (arg0.getKeyChar() == ',' || arg0.getKeyChar() == '<')
			{
				ship_speed = ship_speed - 0.1;
				if (ship_speed < 0.0)
				{
					ship_speed = 0.0;
				}
				System.out.println("ship speed is: " + ship_speed);
			}

		}
	  };
	}

}
