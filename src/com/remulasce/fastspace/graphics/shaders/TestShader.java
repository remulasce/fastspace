package com.remulasce.fastspace.graphics.shaders;

import android.opengl.GLES20;

public class TestShader {
	public static int position	=0;
	public static int vpMatrix	=0;
	public static int mMatrix	=0;
	
	public static void setPointers(int program) {
		TestShader.position	= GLES20.glGetAttribLocation(program, "vPosition");
		TestShader.vpMatrix= GLES20.glGetUniformLocation(program, "vpMatrix");
		TestShader.mMatrix = GLES20.glGetUniformLocation(program, "mMatrix");
	}
}
