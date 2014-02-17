package com.remulasce.fastspace.graphics;

import java.io.InputStream;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.remulasce.fastspace.C;
import com.remulasce.fastspace.R;

/** Class to deal with loading of static models, so you don't have to reload from filesystem every
 * time you want the same old mesh.
 * 
 * This is custom per game
 */
public class ModelManager {
	private Context context;
	
	private static final boolean DEBUG_CLASS = false;
	
	private HashMap<Integer, Mesh> models = new HashMap<Integer, Mesh>();
	
	public ModelManager(Context context) {
		this.context = context;
	}
	/** Return the template mesh for this resource. Do not edit the mesh itself,
	 * this is just a reference to the once-loaded one. Copy it or do something else
	 * if you want to edit the mesh directly.
	 * 
	 * Will return a preloaded model if one has already been loaded, else
	 * WILL USE FILESYSTEM to load (and save) one. Will cause noticable pause
	 * if never preloaded.
	 * */
	public Mesh getMesh(int resId) {
		if (models.containsKey(resId)) {
			log("Loading "+resId+" from cache");
			return models.get(resId);
		}
		else {
			log("Loading "+resId+" from filesystem");
			Mesh tmp = new Mesh();
			Mesh.loadMesh(tmp, context, resId);
			models.put(resId, tmp);
			return tmp;
		}
	}
	/** You really shouldn't use this, especially more than once. */
	public InputStream openRawResource(int resId) {
		InputStream tmp = context.getResources().openRawResource(resId); 
		return tmp;
	}
	
	public int loadTexture(int texId) {
	    final int[] textureHandle = new int[1];
	 
	    GLES20.glEnable(GLES20.GL_TEXTURE_2D);
	    GLES20.glGenTextures(1, textureHandle, 0);
	 
	    if (textureHandle[0] != 0)
	    {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;   // No pre-scaling
	 
	        // Read in the resource
	        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texId, options);
	 
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	 
	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	 
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	 
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	    }
	 
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
	 
	    return textureHandle[0];
	}

	
	/** returns a rock of appropriate size for direct display.
	 * Rock looks good at this size (big rocks have more polys). */
	public Mesh makeRock(float size) {
		return getMesh(R.raw.m_testrock);
	}
	/** Returns a simple xy plane of 4 vertices, top left (0,0), bottom right (-w,-h) */
	public static Mesh makeOverlayRect(float w, float h) {
		Mesh tmp = new Mesh();
		tmp.vertices = new float[] {
				0,h,0,
				0,0,0,
				w,0,0,
				w,h,0
		};
		
		tmp.indices = new short[] {
				0, 1, 2,
				2, 3, 0
		};
		tmp.texCoords = new float[] {
				0, 1,
				0, 0,
				1, 0,
				1, 1
		};
		
		tmp.allocateTexture();
		tmp.allocateVertex();
		tmp.allocateIndex();

		return tmp;
	}
	private void log(String message) {
		if (DEBUG_CLASS) {
			C.log(message);
		}
	}
	
}
