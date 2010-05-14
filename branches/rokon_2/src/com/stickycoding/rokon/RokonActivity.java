package com.stickycoding.rokon;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 * RokonActivity.java
 * The base Activity for the graphics engine to work from
 * @author Richard
 */
public class RokonActivity extends Activity {
	
	protected boolean engineCreated;
	protected Scene currentScene = null;
	protected boolean forceLandscape, forcePortrait, forceFullscreen;
	protected RokonSurfaceView surfaceView;
	
	public void onCreate() {};
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		Debug.print("Engine Activity created");
		onCreate();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Debug.print("Engine Activity received onPause()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Debug.print("Engine Activity received onResume()");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) { 
		return false;
	}
	
	/**
	 * Sets the currently active Scene to be rendered
	 * 
	 * @param scene a valid Scene
	 */
	public void setScene(Scene scene) {
		if(scene == null) {
			Debug.warning("RokonActivity.setScene", "Tried setting to a NULL Scene");
			currentScene = null;
			return;
		}
		currentScene = scene;
	}
	
	/**
	 * Fetches the currently active Scene object
	 * 
	 * @return NULL of no Scene is set
	 */
	public Scene getScene() {
		return currentScene;
	}
	
	/**
	 * Forces the engine to stick to a portrait screen, must be set before createEngine() 
	 */
	public void forcePortrait() {
		if(engineCreated) {
			Debug.warning("RokonActivity.forceFullscreen", "This function may only be called before createEngine()");
			return;
		}
		forcePortrait = true;
		forceLandscape = false;
	}
	
	/**
	 * Forces the engine to stick to a landscape screen, must be set before createEngine()
	 */
	public void forceLandscape() {
		if(engineCreated) {
			Debug.warning("RokonActivity.forceFullscreen", "This function may only be called before createEngine()");
			return;
		}
		forcePortrait = false;
		forceLandscape = true;
	}

	/**
	 * @return TRUE if the engine is being forced into portrait mode
	 */
	public boolean isForcePortrait() {
		return forcePortrait;
	}
	
	/**
	 * @return TRUE if the engine is being forced into landscape mode
	 */
	public boolean isForceLandscape() {
		return forceLandscape;
	}
	
	/**
	 * Forces the Activity to be shown fullscreen
	 *  ie, no titlebar
	 */
	public void forceFullscreen() {
		if(engineCreated) {
			Debug.warning("RokonActivity.forceFullscreen", "This function may only be called before createEngine()");
			return;
		}
		forceFullscreen = true;
	}
	
	/**
	 * Prepares the Activity for rendering
	 * Note that some functions may only be called before createEngine
	 */
	public void createEngine() {
		if(engineCreated) {
			Debug.warning("RokonActivity.createEngine", "Attempted to call createEngine for a second time");
			return;
		}
		if(forceFullscreen) {
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	        getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
		}
		if(forceLandscape) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		if(forcePortrait) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		Device.determine(this);
		surfaceView = new RokonSurfaceView(this);
		setContentView(surfaceView);
		engineCreated = true;
	}


	/**
	 * Returns the current draw priority
	 * 
	 * @return 0 by default
	 */
	public static int getDrawPriority() {
		return DrawPriority.drawPriority;
	}
	
	/**
	 * Sets the draw priority to be used
	 * If not set, or invalid parameters given, defaults to 0
	 * 
	 * @param drawPriority
	 */
	public static void setDrawPriority(int drawPriority) {
		if(drawPriority >= 0 && drawPriority <= 5) {
			DrawPriority.drawPriority = drawPriority;
		} else {
			Debug.warning("RokonActivity.setDrawPriotity", "Invalid draw priority (" + drawPriority + ") ... Defaulting to VBO_DRAWTEX_NORMAL");
			DrawPriority.drawPriority = DrawPriority.VBO_DRAWTEX_NORMAL;
		}
	}
}