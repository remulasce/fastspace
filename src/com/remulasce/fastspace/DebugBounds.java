package com.remulasce.fastspace;

import com.remulasce.fastspace.game.GameWorld;
import com.remulasce.fastspace.graphics.Mesh;

public class DebugBounds {
	public Mesh mesh = new Mesh();
	
	public static final float f = 0f;
	public static final float b = -100f;
	public static final float w = GameWorld.WORLD_SIZE/2;
	public static final float h = GameWorld.WORLD_SIZE/2;
	
	public DebugBounds() {
		mesh.vertices = new float[] {
				w, -h, f,
				w, -h, b,
				w, h, b,
				w, h, f,
				w, -h, b,
				-w, -h, b,
				-w, h, b,
				w, h, b,
				-w, -h, b,
				-w, -h, f,
				-w, h, f,
				-w, h, b,
				-w, -h, f,
				w, -h, f,
				w, h, f,
				-w, h, f,
		};
		mesh.allocateVertex();
	}
}
