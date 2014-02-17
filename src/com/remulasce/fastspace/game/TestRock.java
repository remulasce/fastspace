package com.remulasce.fastspace.game;

import com.remulasce.fastspace.graphics.ModelManager;

public class TestRock extends Rock {
	private static final float MODEL_RADIUS = 4;
	public TestRock(ModelManager models) {
		makeMesh(models);
		rad = MODEL_RADIUS;
	}
	protected void makeMesh(ModelManager manager) {
		mesh = manager.makeRock(1);
	}
	public void setSize(float size) {
		rad = size*MODEL_RADIUS;
		this.size = size;
	}
	
}
