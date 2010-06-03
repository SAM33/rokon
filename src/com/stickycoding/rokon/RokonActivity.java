package com.stickycoding.rokon;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.stickycoding.rokon.device.Graphics;
import com.stickycoding.rokon.vbos.ArrayVBO;
import com.stickycoding.rokon.vbos.ElementVBO;
import com.stickycoding.rokon.vbos.VBO;

/**
 * RokonActivity.java
 * The base Activity for the graphics engine to work from
 * @author Richard
 */
public class RokonActivity extends Activity {
	
	protected static boolean engineCreated;
	protected static Scene currentScene = null;
	protected boolean forceLandscape, forcePortrait, forceFullscreen;
	protected RokonSurfaceView surfaceView;
	protected static boolean engineLoaded = false;
	protected static float gameWidth, gameHeight;
	protected static String graphicsPath;
	
	public void onCreate() {};
	public void onLoadComplete() { };
	
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		Debug.print("Engine Activity created");
		onCreate();
		if(!engineCreated) {
			Debug.error("The engine was not created");
			finish();
			return;
		}
	}
	
	private void createStatics() {
		Graphics.determine(this);
		Rokon.blendFunction = new BlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		Rokon.defaultVertexBuffer = new BufferObject();
		Rokon.defaultVertexBuffer.update(0, 0, 1, 1);
		Rokon.elementVBO = new ElementVBO(VBO.STATIC);
		Rokon.elementVBO.getBufferObject().updateRaw(new float[] { 0, 1, 1, 0, 1, 1 });
		Rokon.arrayVBO = new ArrayVBO(VBO.STATIC);
		Rokon.arrayVBO.update(0, 0, 1, 1);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Debug.print("Engine Activity received onPause()");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Rokon.currentActivity = this;
		Debug.print("Engine Activity received onResume()");
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(currentScene == null)
			return false;
		currentScene.handleTouch(event);
		return false;
	}
	
	/**
	 * Sets a default location for the graphics.
	 * All Texture objects created after this will have this prefix on their absolute path
	 * If the path you give is invalid, an error will not be raised until you try creating a Texture
	 * 
	 * @param path valid Sting path, with trailing slash (eg, "textures/")
	 */
	public void setGraphicsPath(String path) {
		graphicsPath = path;
	}
	
	/**
	 * @return the current default location for textures
	 */
	public String getGraphicsPath() {
		return graphicsPath;
	}
	
	/**
	 * Sets the game width of the OpenGL surface
	 * This must be called before createEngine
	 * 
	 * @param width
	 */
	public void setGameWidth(float width) {
		this.gameWidth = width;
		if(engineLoaded) {
			
		}
	}
	
	/**
	 * Sets the game height of the OpenGL surface
	 * This must be called before createEngine
	 * 
	 * @param height
	 */
	public void setGameHeight(float height) {
		this.gameHeight = height;
	}
	
	/**
	 * Sets the game width and height of the OpenGL surface
	 * This must be called before createEngine
	 * 
	 * @param width
	 * @param height
	 */
	public void setGameSize(float width, float height) {
		this.gameWidth = width;
		this.gameHeight = height;
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
		if(currentScene != null) {
			currentScene.onEndScene();
		}
		currentScene = scene;
		scene.onSetScene();
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
	 * This should be backed up by the correct android:screenSize parameter in AndroidManifest.xml
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
	 * This should be backed up by the correct android:screenSize parameter in AndroidManifest.xml
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
		createStatics();
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
			DrawPriority.drawPriority = DrawPriority.PRIORITY_VBO_DRAWTEX_NORMAL;
		}
	}
}