package com.remulasce.fastspace.graphics.shaders;

import android.opengl.GLES20;

public class OverlayShader {
	public static int a_vPos	=0;
	public static int u_mMat	=0;
	public static int u_vpMat	=0;
	public static int a_texCoord=0;
	public static int u_texture	=0;
	
	public static void setPointers(int program) {
		OverlayShader.a_vPos	= GLES20.glGetAttribLocation	(program, "a_vPos");
		OverlayShader.u_mMat	= GLES20.glGetUniformLocation	(program, "u_mMat");
		OverlayShader.u_vpMat	= GLES20.glGetUniformLocation	(program, "u_vpMat");
		OverlayShader.a_texCoord= GLES20.glGetAttribLocation	(program, "a_texCoord");
		OverlayShader.u_texture	= GLES20.glGetUniformLocation	(program, "u_texture");
	}
}
