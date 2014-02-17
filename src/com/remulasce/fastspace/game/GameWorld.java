package com.remulasce.fastspace.game;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import com.remulasce.fastspace.input.Joystick;

public class GameWorld {
	/**Total width and height of the world.*/
	public static final int WORLD_SIZE	= 12;	//Woo, way smaller than expected
	public static final int MAX_ROCKS	= 100;	//Anti-lockup feature.
	
	public Semaphore lock = new Semaphore(1, true);
	
	public ArrayList<GameObject>	objects;
	public ArrayList<Rock>			rocks;
	public Player					player;
	
	public Joystick		joystick;	//meh hack
	public Camera		camera;		//But then wher's this stuff supposed to go?
	
	//Debug convenience. Directly set by delta x,y in input
	public float testX, testY = 0;
	public int curView = 0;
	
	public float gameTime	= 0;	//How long ingame has been played, in s
	
	public float cameradx	= 0;	//ratio offset the camera should be at
	public float camerady	= 0;
	
	
	public GameWorld() {
		this.objects	= new ArrayList<GameObject>();
		this.rocks		= new ArrayList<Rock>();
		this.joystick	= new Joystick(0,0);
		this.camera		= new Camera();
	}
	
	/** Clears everything. No saving. Player is null**/
	public void clearAll() {
		objects	.clear();
		rocks	.clear();
		player	= null;
	}
	
	public void setPlayer(Player player) {
		objects		.remove(player);
		this.player = player;
		objects		.add(player);
	}
	public void addRock(Rock rock) {
		objects	.add(rock);
		rocks	.add(rock);
	}
	
}
