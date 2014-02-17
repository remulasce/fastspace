package com.remulasce.fastspace.graphics.shaders;

import android.opengl.GLES20;

public class RockShader {
	public static int a_vPos;
	public static int a_vNor;
	public static int u_mMat;
	public static int u_vpMat;
	public static int u_lPos;
	
	public static void setPointers(int program) {
		a_vPos		= GLES20.glGetAttribLocation(program, "a_vPos");
		a_vNor		= GLES20.glGetAttribLocation(program, "a_vNor");
		u_mMat		= GLES20.glGetUniformLocation(program, "u_mMat");
		u_vpMat		= GLES20.glGetUniformLocation(program, "u_vpMat");
		u_lPos		= GLES20.glGetUniformLocation(program, "u_lPos");
	}
}
