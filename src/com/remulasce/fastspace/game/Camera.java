package com.remulasce.fastspace.game;

import java.util.ArrayList;

import com.remulasce.fastspace.C;


/** The Camera class isn't the typical Camera abstraction. It calculates how much
 * shake, offset, direction, etc the real camera should be set to, te get a sense of
 * motion. It's an interface and calculator between the simulation and renderer.
 * 
 * It's made to follow ap Player.
*/
public class Camera {
	
	private static final float CAMERA_PULLBACK	= 1.4f;	//Nominal distance from camera to ship
	public static final float CAMERA_FOV		= 60;

	// current positions for use by renderer //
	public float x, y, z;			//Actual game position of the camera
	public float tx, ty, tz;		//Camera look at point
	
	private ArrayList<CameraMove> moves;	//List of actions the camera should perform.
	private ArrayList<CameraMove> done;		//dead moves for deletion
	private float simTime = 0;
	
	
	private boolean absoluteTarget	= false;	//Keep looking at point instead of direction
	private float rx, ry, rz = 0;
	
	public Camera() {
		moves = new ArrayList<CameraMove>();
		done = new ArrayList<CameraMove>();
		reset();
	}
	
	public void reset() {
		moves.clear();
		done.clear();
		x = 0; y = 0; z = CAMERA_PULLBACK;
		rx = 0; ry = 0; rz = -1;
		setRelativeTargets();
	}
	
	public void pan(float dx, float dy, float dz) {
		x += dx; y += dy; z += dz;
		setRelativeTargets();
	}
	public void setXY(float x, float y) {
		this.x = x; this.y = y;
		setRelativeTargets();
	}
	public void setPosition(float x, float y, float z) {
		this.x = x; this.y = y; this.z = z;
		setRelativeTargets();
	}
	/** Set the camera direction to look at an absolute point. */
	public void setTarget(float tx, float ty, float tz) {
		absoluteTarget = true;
		this.tx = tx; this.ty = ty; this.tz = tz;
	}
	/** Set the camera direction relative to the object. ns will be normalized. */
	public void setDirection(float nx, float ny, float nz) {
		absoluteTarget = false;
		if (nx*nx+ny*ny+nz*nz > 1) {
			float l = (float)Math.sqrt(nx*nx+ny*ny+nz*nz);
			nx /= l; ny /= l; nz /= l;
		}
		rx = nx; ry = ny; rz = nz;

		setRelativeTargets();
	}
	
	public void addMove(CameraMove move) {
		moves.add(move);
		move.init(simTime);
	}
	
	/** more hack. Skewed to feed into Android matrix set, instead of actually
	 * just setting the view matrix the way I want it myself */
	private void setRelativeTargets() {
		if (absoluteTarget) { return; }
		tx = x+rx; ty = y+ry; tz = z+rz;
	}
	
	/** If you've been using the public floats directly, call this to
	 * update the relative t
	 * simtime being the current time of the simulation
	 */
	public void update(float simTime) {
		this.simTime = simTime;
		setRelativeTargets();
		for (CameraMove each : moves) {
			if (each.done(simTime)) {
				done.add(each);
			}
			x+=each.mx(simTime);
			y+=each.my(simTime);
			z+=each.mz(simTime);
		}
		
		//get rid of the done moves
		moves.removeAll(done);
		done.clear();
	}
	
	/** An action the camera should take, over a period of time.
	 * Add them together to find out what should actually happen.
	 */
	public abstract class CameraMove {
		abstract void init(float simTime);
		//t is also the current simulation time, in s.
		abstract public float mx(float t);
		abstract public float my(float t);
		abstract public float mz(float t);
		abstract public boolean done(float t); //return whether to kill
	}
	
	public class KnockMove extends CameraMove {
		//How much the camera should be jumped.
		//This will return the negative of these to return to original.
		private float kx, ky, kz = 0;
		private float sx, sy, sz = 0;	//How much we've actually moved by.
									//So we can make sure we get back exactly.
		private float startT;	//Sim time at which this was started.
		private float returnT;		//Time to return to start, in seconds,
								// after KNOCK_TIME has finished.
		
		
		
		//How many seconds it should take to get knocked back, before returning.
		private static final float KNOCK_TIME = .25f;
		
		
		//ks are how much should be knocked, t is how long to return to start (s).
		public KnockMove(float kx, float ky, float kz, float t) {
			this.kx = kx; this.ky = ky; this.kz = kz;
			returnT = t;
		}
		public void init(float simTime) {
			this.startT = simTime;
		}
		
		@Override
		public float mx(float t) {
			return 0;
		}

		@Override
		public float my(float t) {
			return 0;
		}
		
		@Override
		public float mz(float t) {
			if (t > startT + returnT + KNOCK_TIME) { return 0; }
			float move = 0;
			if (t - startT < KNOCK_TIME) {
				//Get us to current place we should be 
				move = Math.min(1,((t - startT) / KNOCK_TIME)) * kz - sz;
			}
			else {
				move = Math.max(0,(1 - (t-(startT+KNOCK_TIME)) / returnT)) * kz -sz;
			}
			sz += move;
			return move;
		}
		
		@Override
		public boolean done(float t) {
			if (t > startT + returnT + KNOCK_TIME) { C.log("cmove done");return true; }
			return false;
		}
		
	}
	
	
}
