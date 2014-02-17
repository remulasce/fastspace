package com.remulasce.fastspace.game;

import java.util.Random;

import com.remulasce.fastspace.C;
import com.remulasce.fastspace.graphics.ModelManager;

public class Spawner {
	//Old spawn settings
	//Global general rock spawn probability modifier
	private static final float SPAWN_P_ALL		= .6f;
	//Probabilities of each rock type spawning, avg per second
	private static final float SPAWN_P_NEAR		= .5f  * SPAWN_P_ALL;
	private static final float SPAWN_P_MID		= 2f   * SPAWN_P_ALL;
	private static final float SPAWN_P_FAR		= 1f  * SPAWN_P_ALL;
	//How far away each size/distance rock really spawns
	private static final float SPAWN_D_NEAR		= -20;
	private static final float SPAWN_D_MID		= -50;
	private static final float SPAWN_D_FAR		= -100;
	//Base size of each rock
	private static final float SPAWN_S_NEAR		= .20f;
	private static final float SPAWN_S_MID		= .40f;
	private static final float SPAWN_S_FAR		= .8f;
	
	
	
	GameWorld		world;
	ModelManager	models;
	
	protected long		timePassed	= 0;	//Sim time passed total, in ms
	protected long		lastTime	= 0;	//Time at which last went throuh
	protected float		d			= 0;	//time since last go-through, ms
	protected boolean	enabled		= false;
	
	protected int		spawns		= 0;	//number of things spawned yet
	protected Random	rand		= new Random();
													
	public Spawner(GameWorld world, ModelManager models) {
		this.models		= models;
		this.world		= world;	
	}
	
	/** Destroys current world and restarts with new initial stuff. **/
	public void restart() {
		
		world.clearAll();
		timePassed	= 0;
		lastTime	= System.currentTimeMillis();
		
		Player player = new Player(models);
		world.setPlayer(player);
		
	}
	/** Start the spawner. Restart must have been called once to init. */
	public void start() {
		enabled		= true;
		lastTime	= System.currentTimeMillis();
	}
	/** Pause the spawner, will resume at start() */
	public void pause() {
		enabled		= false;
	}
	public void update() {
		if (enabled) {
			long t = System.currentTimeMillis();
			d			= t - lastTime;
			timePassed += d;
			lastTime	= t;
			
			//True random spawning
			//fairSpawn();
			
			//Player-response, cheating, adrenaline-max effortnevermattered spawn
			//Tries to keep the player adequately challenged, forces him to move,
			//	keeps him off the edge of the map, game-prescribed rest breaks,
			//	basically really sly, loathsome, slimy stuff I want to learn.
			fixedSpawn();
			
		}
		
	}

	//Note: This is "fixed" in the sense of, "rigged". Not unrandom, but cheating.
	/*
	 * Big explanation:
	 * 
	 * 	Random mean fun. It means random. We want fun. Instead of relying on
	 * random to give fun, we're going to use science, and use any methods
	 * necessary to ensure a fun time.
	 * 
	 *  That means: 		(- planned, + used)
	 *  
	 * +Appears random.
	 * 		The player should think he's getting a fair fight. Plus random is easy
	 * 		for me to do.
	 * +Gets harder.
	 * 		Early game should be easy. Later it should get harder.
	 * -No staying in one spot.
	 * 		That's lame. So we make sure a rock spawns in front of any non-moving
	 * 		player. 	
	 * -No hanging around the edges.
	 *		You don't get to use the edge case (Yes, pun. now shut up). You get a
	 *		rock to the face for trying that stuff.
	 * -No unwinnable situations.
	 *		Perfect play should mean playing forever. Also, it should never be too
	 *		hard to survive at early level, then get harder later. So no giant walls
	 *		of rock.
	 * -Catchup.
	 *		If you get beaned a bunch by rocks, we'll make it a bit easier to catch up.
	 *		Likewise, you can't get /too/ far ahead of what you should be at early.
	 * 
	 */
	
	private static final float S_DIST		= -40f;		//Distance from player to spawn
	private static final float S_PROB		= 2f;		//Global rock spawn rate modifier
	
	private void fixedSpawn() {
		
		
		float p		= 0;		//Total probability of creation
		float dp	= 0;		//Probality factor from distance from player
		float dv	= 1;		//Current speed factor. Faster needs more rocks
		
		float r = (float)Math.random()*100;				//Randomness probability
		
		//World size is total length, so rand is half what it would be if we had height etc. 
		float x	= (float)(rand.nextDouble()-.5)*(GameWorld.WORLD_SIZE);
		float y = (float)(rand.nextDouble()-.5)*(GameWorld.WORLD_SIZE);
		float s	= (float)(rand.nextDouble() * 1);		//Radius of rock
		
		dp		= Math.max(4-rockDistance(x,y), 0);		//Distance probability
		
		
		dv		= (world.player.vz)/Player.PLAYER_START_SPEED; 
		
		
		
		
		p 		= 1f*r + 80*dp;// - s;
		p		*= S_PROB * dv;
		
		float l = (float)Math.random()*100;
		if (world.rocks.size() > GameWorld.MAX_ROCKS) {
			C.log("Too many rocks on the field");
			return;
		}
		if (p*d/1000 > l) {
			TestRock add = new TestRock(models);
			add.x		= x;
			add.y		= y;
			add.z		= S_DIST;
			add.setSize(s);
			world.addRock(add);
		}
		
	}
	
	private void fairSpawn() {
		// Three places a rock can spawn:
		//	1. Far away for a small # of big rocks
		//	2. Mid for smaller numbers of mid rocks
		//	3. Close for lots of small rocks
		//
		// This keeps the play area fully populated with obstacles without
		//	overly inundating the GPU with things to draw
		////////////////////////////////////////////
		
		//Far spawns. Low r means more rock.
		double r = Math.random() / (world.player.vz / 12);
		if (r < SPAWN_P_FAR * d / 1000) {
			TestRock add	= new TestRock(models);
			add.z		= SPAWN_D_FAR;
			add.x = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.y = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.setSize((float) (SPAWN_S_FAR+Math.random()*.20f));
			world.addRock(add);
		}
		//Mid spawns
		r = Math.random() / (world.player.vz / 12);
		if (r < SPAWN_P_MID * d / 1000) {
			TestRock add	= new TestRock(models);
			add.z		= SPAWN_D_MID;
			add.x = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.y = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.setSize((float) (SPAWN_S_MID+Math.random()*.3f));
			world.addRock(add);
		}
		//Near spans
		r = Math.random() / (world.player.vz / 12);
		if (r < SPAWN_P_NEAR * d / 1000) {
			TestRock add	= new TestRock(models);
			add.z		= SPAWN_D_NEAR;
			add.x = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.y = (float)( GameWorld.WORLD_SIZE*Math.random())-GameWorld.WORLD_SIZE/2;
			add.setSize((float) (SPAWN_S_NEAR+Math.random()*.15f));
			world.addRock(add);
		}
	}
	
	private float rockDistance(float x, float y) {
		return (float) Math.sqrt(Math.pow(world.player.x-x, 2)+Math.pow(world.player.y-y,2));
	}
}
