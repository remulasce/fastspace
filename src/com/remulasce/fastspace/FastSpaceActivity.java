package com.remulasce.fastspace;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

import com.remulasce.fastspace.game.GameRenderer;
import com.remulasce.fastspace.game.GameWorld;
import com.remulasce.fastspace.game.Simulation;
import com.remulasce.fastspace.game.Spawner;
import com.remulasce.fastspace.graphics.ModelManager;
import com.remulasce.fastspace.input.EngineTouchListener;
import com.remulasce.fastspace.input.InputEngine;

public class FastSpaceActivity extends Activity {
	
	private FastSpaceGLView	glView;

	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        glView	= new FastSpaceGLView(this);
        glView.onCreate();
        setContentView(glView);
    }
    
    @Override
    protected void onStart() {
    	super	.onStart();
    	glView	.onStart();
    }
    @Override
    protected void onResume() {
    	super	.onResume();
    	glView	.onResume();
    }
    @Override
    protected void onRestart() {
    	super	.onRestart();
    	glView	.onRestart();
    }
    @Override
    protected void onPause() {
    	super	.onPause();
    	glView	.onPause();
    }
    @Override
    protected void onStop() {
    	super	.onStop();
    	glView	.onStop();
    }
}

class FastSpaceGLView extends GLSurfaceView {
	
	private GameWorld		world;
	private InputEngine		input;
	private Renderer		renderer;
	private ModelManager	models;
	private Simulation		simulation;
	private Spawner			spawner;
	
	
	
	public FastSpaceGLView(Context context) {
		super(context);
		C.log("Creating View");
		
		world		= new GameWorld();
		models		= new ModelManager(this.getContext());
		spawner		= new Spawner(world, models);
		input		= new InputEngine();
		simulation	= new Simulation(world, input, spawner, context);
		
		renderer	= new GameRenderer(world, this.getContext());
		
		setOnTouchListener(new EngineTouchListener(input));
		
		this.setEGLContextClientVersion(2);
		setRenderer(renderer);
		
		
	}
	public void onStop() {
		simulation.pauseSim();
	}
	public void onRestart() {
		// TODO Auto-generated method stub
		
	}
	public void onCreate() {
		simulation.setupSim();
	}
	
	public void onStart() {
	}
	@Override
	public void onResume() {
		super.onResume();
		C.log("Resuming");
		simulation.resumeSim();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		C.log("Pausing");
		simulation.pauseSim();
	}
	
}