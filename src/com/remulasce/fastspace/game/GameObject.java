package com.remulasce.fastspace.game;

import com.remulasce.fastspace.graphics.Mesh;

public abstract class GameObject {
	public float	x, y, z;
	public Mesh		mesh;
	public float	rad;		//Collision radius
	public boolean	shouldDie;
	
	public abstract void update(float d, GameWorld world);
	public abstract void setTransform(float[] mMatrix);
	
	
	/** returns 2D distance between objects. xy only. **/
	public static float distance(GameObject a, GameObject b) {
		return (float)Math.sqrt(Math.pow(a.x-b.x,2)+Math.pow(a.y-b.y,2)+Math.pow(a.z-b.z, 2));
	}
	/** Returns whether objects hit, via circle boundaries. No square roots, so 
	 * faster than using distance() comaprison.
	 */
	public static boolean collides(GameObject a, GameObject b) {
		float dx = a.x-b.x; float dy = a.y-b.y; float dz = a.z-b.z;
		return (dx*dx+dy*dy+dz*dz < (a.rad+b.rad)*(a.rad+b.rad));
	}
}
