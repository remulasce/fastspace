package com.remulasce.fastspace.game;

import android.opengl.Matrix;

public class Rock extends GameObject {

	public float size	= .1f;
	//Rocks used to move. Now the player moves.
//	private float vZ = 10;
	
	
	
	public boolean hit		= false;	//Whether this rock has already hit player
	
	@Override
	public void update(float d, GameWorld world) {
		z += world.player.vz * d;
	}

	@Override
	public void setTransform(float[] mMatrix) {
		Matrix.setIdentityM	(mMatrix, 0);
		Matrix.translateM	(mMatrix, 0, x, y, z);
		Matrix.scaleM		(mMatrix, 0, size, size, size);
//		Matrix.rotateM		(mMatrix, 0, rot, 0, 0, 1);
	}

}
