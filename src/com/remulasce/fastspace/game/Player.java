package com.remulasce.fastspace.game;

import android.opengl.Matrix;

import com.remulasce.fastspace.C;
import com.remulasce.fastspace.R;
import com.remulasce.fastspace.graphics.ModelManager;

public class Player extends GameObject {
	
	private float level	= .1f;
	
	private float size	= .4f;		//In-game size
	private static final float MODEL_RADIUS	= 1f;	//Size of the model
	
	private float w		= 1f;
	private float h		= 1f;
	
	public static final float PLAYER_START_SPEED	= 14f;
	public static final float PLAYER_ACCELERATION	= 1f;	//1/2 unit/s/s
	//The Player doesn't actually move, but the simulation will move everything
	// else by this amount to do the same thing.
	public float vz			= PLAYER_START_SPEED;
	public float distance	= 0;		//The total distance travelled so far.
	
	public Player(ModelManager models) {
		makeMesh(models);
		rad = MODEL_RADIUS*size;
	}
	protected void makeMesh(ModelManager models) {
		mesh = models.getMesh(R.raw.m_testplayer);
	}
	
	public void hitsRock(Rock rock) {
		if (rock.hit) { C.log("player hit done rock, bad"); return; }
		rock.hit = true;

		
		vz -= rock.size*20;
		if (vz < 2) {
			vz = 2;
		}
		
	}
	
	
	
	@Override
	public void update(float d, GameWorld world) {
		if (x > world.WORLD_SIZE/2) { x =  world.WORLD_SIZE/2; }
		if (x < -world.WORLD_SIZE/2){ x = -world.WORLD_SIZE/2; }
		if (y > world.WORLD_SIZE/2) { y =  world.WORLD_SIZE/2; }
		if (y < -world.WORLD_SIZE/2){ y = -world.WORLD_SIZE/2; }
		
		distance += d*vz;
		vz += d*PLAYER_ACCELERATION;
	}
	/** Move the ship, and probably the camera with it. */
	public void steer(float x, float y, float d) {
		this.x += x*d; this.y += y*d;
	}

	@Override
	public void setTransform(float[] mMatrix) {
		Matrix.setIdentityM(mMatrix, 0);
		Matrix.translateM(mMatrix, 0, x, y, z);
		Matrix.scaleM(mMatrix, 0, size, size, size);
	}

}
