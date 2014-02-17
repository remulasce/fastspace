package com.remulasce.fastspace.game;

import java.util.ArrayList;

import android.content.Context;

import com.remulasce.fastspace.C;
import com.remulasce.fastspace.input.InputEngine;

public class Simulation implements Runnable {
	
	private static final boolean	DEBUG_UPDATE_RUNTIME	= true;
	private static final int		DEBUG_DISP_INTERVAL		= 500;
	
	//Average speed you must maintain or die
	private static final float		DEATH_SPEED				= 16;
	
	private GameWorld	world;
	
	private InputEngine	input;
	private Context		context;
	private Spawner		spawner;
	
	private Thread		simThread;
	private boolean run = false;
	
	
	private float	d = 0;
	private long	lastUpdate = System.currentTimeMillis();
	private long	lastDebugDisp;
	
	public Simulation(GameWorld world, InputEngine input, Spawner spawner, Context context) {
		this.world	= world;
		this.input	= input;
		this.spawner= spawner;
		this.context= context;
		simThread = new Thread(this);
		
	}
	/** Start this game for the first time */
	public void setupSim() {
		C.log("Sim Setup");
		
		doSetup();
		//TODO something smart
		spawner		.restart();
		
	}
	/** Resume the game from the /pause/ed state. */
	public void resumeSim() {
		if (run) { return; }
		run			= true;
		spawner		.start();
		simThread	= new Thread(this);
		simThread	.start();
	}
	/** Pause the game, but do not destroy the data. For menu or background
	 * apps.
	 */
	public void pauseSim() {
		C.log("Pause Sim");
		spawner		.pause();
		run			= false;
	}
	
	public void run() {
		
		doSetup();
		
		while (run) {
			try {
				world.lock.acquire();
				
				updateClock();

				doSpawn();
				
				doInput();
				
				doUpdate();
				
				doCollis();
				
				doCamera();
				
				doCulling();
				
				world.lock.release();
				
				doLogging();
				
				try {
					Thread.sleep(Math.max(0, 10-(System.currentTimeMillis()-lastUpdate)));
				} catch (InterruptedException e) {
					C.log("bad in simulation sleep");
				}
				
			} catch (InterruptedException e1) {
				C.log("Interrupted exception in simulation: lock maybe not released");
			}
			
			
		}
		C.log("Sim Thread over");
	}
	
	protected void doInput() {
		input.prep();
		
		world.joystick.handleInput(input);
		
		float tx = world.joystick.dx;
		float ty = world.joystick.dy;
		
		if (true) {
			world.player.steer(
					(float)(15*tx*Math.sqrt(Math.abs(tx)+.1)),
					(float)(15*ty*Math.sqrt(Math.abs(ty)+.1)),
					d
					);
		}
		
		
		world.testX += input.dAvgTouchPoint.x;
		world.testY += input.dAvgTouchPoint.y;

		
		//DEBUG: Change view of scene
		if (input.newTouch && input.avgTouchPoint.x < 100) {
			world.curView++;
			if (world.curView > 1) {
				world.curView = 0;
			}
		}
		//DEBUG: Reset game
		if (input.newTouch && input.avgTouchPoint.y < 100 && input.avgTouchPoint.x > 100) {
			C.log("Debug Reset Game");
			pauseSim();
			setupSim();
			resumeSim();
		}
		
		
		input.reset();
		
	}
	
	protected void doUpdate() {
		world.gameTime += d;
		for (Rock each : world.rocks) {
			each.update(d, world);
			if (each.z > 3) {
				each.shouldDie = true;
			}
		}
		world.player.update(d, world);
		//Didn't go fast enough, died.
		if (world.player.distance/world.gameTime < DEATH_SPEED && world.gameTime > 8) {
			C.log("Game Over");
			pauseSim();
		}
	}
	
	protected void doCollis() {
		for (Rock each : world.rocks) {
			if (!each.hit && GameObject.collides(each, world.player)) {
				world.player.hitsRock(each);
				world.camera.addMove(world.camera.new KnockMove(0,0,-each.size,2));
			}
		}
	}
	
	protected void doCulling() {
		ArrayList<GameObject> die = new ArrayList<GameObject>();
		for (GameObject each : world.objects) {
			if (each.shouldDie) {
				die.add(each);
			}
		}
		world.objects.removeAll(die);
		world.rocks.removeAll(die);
	}
	
	protected void doSpawn() {
		spawner.update();
	}
	
	//The simulation calculates camera effects, like panning to follow the ship.
	protected void doCamera() {
		world.camera.x = world.player.x*.95f;
		world.camera.y = world.player.y*.95f;
	}
	
	protected void doSetup() {
		world.camera.reset();
		world.camera.setDirection(0,0,-1);
	}
	
	private void updateClock() {
		//Time since last update, for constant game speed
		d = (System.currentTimeMillis() - lastUpdate) / 1000F;
		lastUpdate = System.currentTimeMillis();
	}
	
	protected void doLogging() {
		if (DEBUG_UPDATE_RUNTIME && System.currentTimeMillis() > lastDebugDisp + DEBUG_DISP_INTERVAL) {
			float frameTime = (System.currentTimeMillis()-lastUpdate);
			C.log("Update ftime ms: "+ frameTime);
			C.log("Player speed: "+world.player.vz+" Avg: "+world.player.distance/world.gameTime);
//			C.log("spawnCounter: "+spawnCounter);
			lastDebugDisp = System.currentTimeMillis();
		}
	}
	
	
}
