package com.mygdx.hulaj.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.hulaj.GameClassHulaj;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		new LwjglApplication(new GameClassHulaj(), config);
		config.width = 640;//1280;
		config.height = 360;//720;
		//deb> config.resizable = false;
		/// d> config.fullscreen = true;

	}
}
