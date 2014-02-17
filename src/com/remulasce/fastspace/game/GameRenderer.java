package com.remulasce.fastspace.game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.remulasce.fastspace.C;
import com.remulasce.fastspace.DebugBounds;
import com.remulasce.fastspace.R;
import com.remulasce.fastspace.graphics.GUtils;
import com.remulasce.fastspace.graphics.shaders.OverlayShader;
import com.remulasce.fastspace.graphics.shaders.RockShader;
import com.remulasce.fastspace.graphics.shaders.TestShader;
import com.remulasce.fastspace.input.Joystick;

public class GameRenderer implements GLSurfaceView.Renderer {
	
	private static final boolean DEBUG_FPS = true;
	private static final long DEBUG_INTERVAL = 5000;
	
//	private static final float CAMERA_PULLBACK	= 1;
	private static final float CAMERA_ELEVATION	= .1f;

	//Attach vertex and fragment shaders to a Program and then use it
	private int[] shaders = new int[3];
	private static final int testShader		= 0;
	private static final int overlayShader	= 1;
	private static final int rockShader		= 2;

	
	
	//References to all loaded textures
	private int[]	textures = new int[3];
	private int		activeTexture	= 0;
	private final int	TEX_TEST			= 0;
	private final int	TEX_JOYSTICK_BACK	= 1;
	private final int	TEX_JOYSTICK_TOP	= 2;
	
	
	private Joystick		joystick;
	private DebugBounds		bounds;
	
	
	private GameWorld		world;
	private Context			context;

	private float[] mMatrix		= new float[16];	//Model matrix- 		object to world space
	private float[] vMatrix		= new float[16];	//View matrix-			world to camera space
	private float[] pMatrix		= new float[16];	//Projection matrix		camera to screen (2D) space
	private float[] vpMatrix	= new float[16];	//View projection-		only somethimes changed
	private float[] ovrMatrix	= new float[16];	//Overlay projection	2D orthoganal view, 1:1 pixels
	private float[] tmpMatrix	= new float[16];
	private float[] diagMatrix	= new float[16];	//Diagonal overview vp matrix
	
	private float[] curMat		= new float[16];	//Current view matrix to use
	private float[] lightPos	= {0,0,0};		//Light position
	
	private float test = 0;


	
	
	
	
	
	
	
	long last = System.currentTimeMillis();
	long lastPerfDisp = 0;
	long frameTime = 0;
	
	
	public GameRenderer(GameWorld world, Context context) {
		this.world		= world;
		this.context	= context;
		
		bounds = new DebugBounds();
		this.joystick	= world.joystick;
	}

	
	
	
	@Override
	public void onDrawFrame(GL10 gl) {
		
		
		try {
			world.lock.acquire();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
			GLES20.glEnable(GLES20.GL_CULL_FACE);
			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
			GLES20.glDepthFunc(GLES20.GL_LEQUAL);

			
			doDebug();
			
			
			calculateCamera();
			
			calculateLighting();
			
			
			drawTests();
			
			drawRocks();
			
			drawUI();
			
			
			
			
			
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		world.lock.release();
		
	}




	private void calculateLighting() {
		lightPos[0] = world.player.x;
		lightPos[1] = world.player.y;
		lightPos[2] = 1;
	}

	protected void drawRocks() {
		GLES20.glUseProgram(shaders[rockShader]);
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		
		GLES20.glEnableVertexAttribArray(RockShader.a_vNor);
		GLES20.glEnableVertexAttribArray(RockShader.a_vPos);
		
		GLES20.glUniformMatrix4fv		(RockShader.u_vpMat, 1, false, curMat, 0);
		GLES20.glUniform3fv				(RockShader.u_lPos, 1, lightPos, 0);
		
		for (GameObject each : world.rocks) {
			each.setTransform(mMatrix);
			
			GLES20.glVertexAttribPointer	(RockShader.a_vPos, 3, GLES20.GL_FLOAT, false, 0, each.mesh.vb);
			GLES20.glVertexAttribPointer	(RockShader.a_vNor, 3, GLES20.GL_FLOAT, false, 0, each.mesh.nb);
			GLES20.glUniformMatrix4fv		(RockShader.u_mMat, 1, false, mMatrix, 0);
			
			GLES20.glDrawArrays			(GLES20.GL_TRIANGLES, 0, each.mesh.vertices.length/3);
		}
		
		
		world.player.setTransform(mMatrix);
		
		GLES20.glVertexAttribPointer		(RockShader.a_vPos, 3, GLES20.GL_FLOAT, false, 0, world.player.mesh.vb);
		GLES20.glVertexAttribPointer		(RockShader.a_vNor, 3, GLES20.GL_FLOAT, false, 0, world.player.mesh.nb);
		GLES20.glUniformMatrix4fv		(RockShader.u_mMat, 1, false, mMatrix, 0);
		
		GLES20.glDrawArrays			(GLES20.GL_TRIANGLES, 0, world.player.mesh.vertices.length/3);

		GLES20.glDisableVertexAttribArray(RockShader.a_vNor);
		GLES20.glDisableVertexAttribArray(RockShader.a_vPos);
		
	}
	
	private void drawTests() {
		GLES20.glUseProgram(shaders[testShader]);
		
		GLES20.glEnableVertexAttribArray(TestShader.position);
		GLES20.glUniformMatrix4fv		(TestShader.vpMatrix, 1, false, curMat, 0);
		
		/*
		world.player.setTransform(mMatrix);
		
		GLES20.glVertexAttribPointer		(TestShader.position, 3, GLES20.GL_FLOAT, false, 0, world.player.mesh.vb);
		GLES20.glUniformMatrix4fv		(TestShader.mMatrix, 1, false, mMatrix, 0);
		
		GLES20.glDrawArrays			(GLES20.GL_TRIANGLES, 0, world.player.mesh.vertices.length/3);
		
		*/
		/*
		Matrix.setIdentityM(mMatrix, 0);
		//gaah hack
		for (int ii = 0; ii < 4; ii++) {
			GLES20.glVertexAttribPointer	(TestShader.position, 3, GLES20.GL_FLOAT, false, 0, bounds.mesh.vb);
			GLES20.glUniformMatrix4fv		(TestShader.mMatrix, 1, false, mMatrix, 0);
			
			GLES20.glDrawArrays			(GLES20.GL_LINE_LOOP, 4*ii, 4);
			
		}
		
		*/
		
		GLES20.glDisableVertexAttribArray(TestShader.position);
		
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		
	}
	
	
	protected void drawUI() {
		GLES20.glUseProgram(shaders[overlayShader]);
		
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		
		GLES20.glEnableVertexAttribArray(OverlayShader.a_texCoord);
		GLES20.glEnableVertexAttribArray(OverlayShader.a_vPos);
		
		//Setup 2D orthogonal view
		GLES20.glUniformMatrix4fv(OverlayShader.u_vpMat, 1, false, ovrMatrix, 0);
		
		
		//==Draw joystick background==
		//Set position
		joystick.setTransformBack(mMatrix);
		//Use background texture
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[TEX_JOYSTICK_BACK]);
		GLES20.glUniform1i(OverlayShader.u_texture, 0);
		//Setup mesh
		GLES20.glVertexAttribPointer(OverlayShader.a_vPos, 3, GLES20.GL_FLOAT, false, 0, joystick.backMesh.vb);
		GLES20.glUniformMatrix4fv(OverlayShader.u_mMat, 1, false, mMatrix, 0);
		GLES20.glVertexAttribPointer(OverlayShader.a_texCoord, 2, GLES20.GL_FLOAT, false, 0, joystick.backMesh.tb);
		

		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, joystick.backMesh.ib);
//		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);
		
		
		//==Draw joystick top==
		joystick.setTransformTop(mMatrix);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[TEX_JOYSTICK_TOP]);
		
		GLES20.glVertexAttribPointer(OverlayShader.a_vPos, 3, GLES20.GL_FLOAT, false, 0, joystick.topMesh.vb);
		GLES20.glUniformMatrix4fv(OverlayShader.u_mMat, 1, false, mMatrix, 0);
		GLES20.glVertexAttribPointer(OverlayShader.a_texCoord, 2, GLES20.GL_FLOAT, false, 0, joystick.backMesh.tb);
		
		GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, joystick.backMesh.ib);
		
		
		
		
		GLES20.glDisableVertexAttribArray(OverlayShader.a_texCoord);
		GLES20.glDisableVertexAttribArray(OverlayShader.a_vPos);
	}
	
	
	/** Sets the v transform matrix for the main camera, and applies it to the p as well. */
	private void calculateCamera() {
		//Setup perspective matrix
		/*
		Matrix.setLookAtM(vMatrix, 0,
				world.cameradx, world.camerady+CAMERA_ELEVATION, CAMERA_PULLBACK,
				0, CAMERA_ELEVATION, -1000,
				0, 1, 0);
				*/
		world.camera.update(world.gameTime);
		Matrix.setLookAtM(vMatrix, 0,
				world.camera.x, world.camera.y, world.camera.z,
				world.camera.tx, world.camera.ty, world.camera.tz,
				0, 1, 0);
		Matrix.multiplyMM(vpMatrix, 0, pMatrix, 0, vMatrix, 0);
		
	}
	
	
	
	
	
	private void doDebug() {
		test += System.currentTimeMillis() - last;
		if (DEBUG_FPS) {
			frameTime = System.currentTimeMillis() - last;
			if(System.currentTimeMillis() > lastPerfDisp + DEBUG_INTERVAL) {
				C.log("GPU Ftime ms: "+Long.toString(frameTime)+" FPS: "+1000/frameTime);
				lastPerfDisp = System.currentTimeMillis();
			}
		}
		last = System.currentTimeMillis();
		//Debug camera views
		if (world.curView == 0) {
			curMat = vpMatrix;
		}
		else {
			curMat = diagMatrix;
		}
	}
	
	
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glViewport(0, 0, width, height);
		
		
		GUtils.perspectiveM		(pMatrix, 0, Camera.CAMERA_FOV, ((float)width) / height, .1f, world.camera.z+140);
		calculateCamera();		//Does the v & vp matrix for the main camera
		
		
		
		//Diagonal debug view
		Matrix.setLookAtM(diagMatrix, 0,
				-12, 16, 20,
				0, 0, -10,
				0, 1, 0
				);
		
		Matrix.multiplyMM(diagMatrix, 0, pMatrix, 0, diagMatrix, 0);
		
		//Setup orthogonal overlay matrix
		Matrix.orthoM(tmpMatrix, 0, 0, width, height, 0, 1, 20);
		Matrix.multiplyMM(ovrMatrix, 0, tmpMatrix, 0, vMatrix, 0);
		
		C.SCR_HEIGHT	= height;
		C.SCR_WIDTH		= width;
		
		
		//Keep joystick in bottom right
		world.joystick.setPos(width-Joystick.BACK_SIZE-40, height-Joystick.BACK_SIZE-75);
		
		
	}
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES20.glClearColor(.0f, .0f, .0f, 1f);
		
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_TEXTURE_2D);
		
		
		
		loadTextures();
		loadShaders();
	}
	
	private void loadShaders() {
		loadResShader(testShader, R.raw.sv_test, R.raw.sf_test);
		TestShader.setPointers(shaders[testShader]);
		loadResShader(overlayShader, R.raw.sv_overlay, R.raw.sf_overlay);
		OverlayShader.setPointers(shaders[overlayShader]);
		loadResShader(rockShader, R.raw.sv_rock_gouraud, R.raw.sf_rock_gouraud);
		RockShader.setPointers(shaders[rockShader]);
	}
	
	private void loadTextures() {
		textures[TEX_TEST]			= GUtils.loadTexture(context, R.raw.t_test);
		textures[TEX_JOYSTICK_TOP]	= GUtils.loadTexture(context, R.raw.t_joystick_top);
		textures[TEX_JOYSTICK_BACK]	= GUtils.loadTexture(context, R.raw.t_joystick_background);
	}
	
	private void loadResShader(int name, int vertex, int fragment) {
		int vertexShader	= GUtils.loadShader(context, GLES20.GL_VERTEX_SHADER,	vertex);
		int fragmentShader	= GUtils.loadShader(context, GLES20.GL_FRAGMENT_SHADER,	fragment);
		
		shaders[name] = GLES20.glCreateProgram();			//makes a new program, and returns its ID, presumably
		GLES20.glAttachShader(shaders[name], vertexShader);
		GLES20.glAttachShader(shaders[name], fragmentShader);
		GLES20.glLinkProgram (shaders[name]);
	}

	
	
}
