package com.stickycoding.Rokon;

import java.util.ArrayList;
import java.util.Iterator;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Scene {
	
	private ArrayList<TextureAtlas> _textureAtlas;
	
	public Rokon rokon;
	private Background _background;
	private boolean _hasBackground;
	private int _layerCount, _maxEntityCount;
	private Entity[][] _entity;
	private Entity[] _touchable;
	
	private Entity[] _drawOrderEntity;
	
	private int _drawOrder[];
	
	private Window _backgroundWindow;
	private Window[] _window;
	
	private boolean _hasChildScene, _childSceneModal;
	private Scene _childScene;
	
	private boolean _checkForTouchables, _checkForTouchableLayers;
	
	public Bundle lastBundle, thisBundle;
	
	private boolean _isChild;
	private Scene _parentScene;
	
	public void onCreate() { }
	public void onDestroy() { }
	public void onPause() { }
	public void onResume() { }
	public void onGameLoop() { }
	public void onSceneLoad(Bundle incomingBundle) { }
	public void onSceneExit() { }
	public void onChildClosed(Bundle childBundle) { }
	
	public void onPreDraw(GL10 gl) { }
	public void onPostBackgroundDraw(GL10 gl) { }
	public void onPreDraw(GL10 gl, int layer) { }
	public void onPostDraw(GL10 gl) { }
	public void onPostDraw(GL10 gl, int layer) { }

	public void onIncomingCall() { }
	public void onNotification() { }

	public void onTouch(int x, int y, MotionEvent event) { }
	public void onTouchDown(int x, int y, MotionEvent event) { }
	public void onTouchMove(int x, int y, MotionEvent event) { }
	public void onTouchUp(int x, int y, MotionEvent event) { }
	
	public void onTouch(Entity entity, int x, int y, MotionEvent event) { }
	public void onTouchDown(Entity entity, int x, int y, MotionEvent event) { }
	public void onTouchUp(Entity entity, int x, int y, MotionEvent event) { }
	public void onTouchMove(Entity entity, int x, int y, MotionEvent event) { }
	
	public void onKeyDown(int keyCode, KeyEvent event) { }
	public void onKeyUp(int keyCode, KeyEvent event) { }
	
	protected Scene(int layerCount, int maxEntityCount) {
		rokon = Rokon.rokon;
		_layerCount = layerCount;
		_drawOrder = new int[_layerCount];
		_maxEntityCount = maxEntityCount;
		_entity = new Entity[_layerCount][];
		for(int i = 0; i < _layerCount; i++)
			_entity[i] = new Entity[_maxEntityCount];
		_drawOrderEntity = new Entity[_maxEntityCount];
		_textureAtlas = new ArrayList<TextureAtlas>();
		_touchable = new Entity[Constants.MAX_TOUCHABLE_ENTITIES];
		_window = new Window[_layerCount];
	}
	
	public void setDrawOrder(int layer, int order) {
		_drawOrder[layer] = order;
	}
	
	public int getDrawOrder(int layer) {
		return _drawOrder[layer];
	}
	
	public Scene() {
		this(Rokon.getDefaultLayerCount(), Rokon.getDefaultMaxEntityCount());
	}
	
	public void setWindow(int layer, Window window) {
		_window[layer] = window;
	}
	
	public void removeWindow(int layer) {
		_window[layer] = null;
	}
	
	public Window getWindow(int layer) {
		return _window[layer];
	}
	
	public void setBackgroundWindow(Window window) {
		_backgroundWindow = window;
	}
	
	public void removeBackgroundWindow() {
		_backgroundWindow = null;
	}
	
	public Window getBackgroundWindow() {
		return _backgroundWindow;
	}
	
	public int getMaxEntityCount() { 
		return _maxEntityCount;
	}
	
	public int getLayerCount() {
		return _layerCount;
	}
	
	public Rokon getRokon() {
		return rokon;
	}
	
	public void add(Entity entity, int layer) {
		for(int i = 0; i < _maxEntityCount; i++)
			if(_entity[layer][i] == null) {
				_entity[layer][i] = entity;
				return;
			}
		Debug.warning("Not enough room for Entity, layer=" + layer);
	}
	
	public void add(Entity entity) {
		add(entity, 0);
	}
	
	public void remove(Entity entity) {
		entity.remove();
	}
	
	public void clear() {
		for(int i = 0; i < _layerCount; i++)
			clear(i);
	}
	
	public void clear(int layer) {
		for(int i = 0; i < _maxEntityCount; i++)
			if(_entity[layer][i] != null) 
				_entity[layer][i] = null;
	}
	
	public void setBackground(Background background) { 
		_background = background;
		_hasBackground = (background != null);
	}
	
	public Background getBackground() {
		return _background;
	}
	
	public boolean hasBackground() {
		return _hasBackground;
	}
	
	public void removeBackground() {
		_background = null;
		_hasBackground = false;
	} 
	
	protected void onDraw(GL10 gl) {
		onPreDraw(gl);
		if(_hasBackground) {
			if(_backgroundWindow != null) {
				gl.glPushMatrix();
				_backgroundWindow.onDraw(gl);
				_background.onDraw(gl);
				onPostBackgroundDraw(gl);
				gl.glPopMatrix();
			} else {
				_background.onDraw(gl);
				onPostBackgroundDraw(gl);
			}
		}
		for(int i = 0; i < _layerCount; i++) {
			if(_window[i] != null) {
				gl.glPushMatrix();
				_window[i].onDraw(gl);
				onPreDraw(gl, i);
				if(_drawOrder[i] == DrawOrder.NORMAL) { 
					for(int j = 0; j < _maxEntityCount; j++)
						if(_entity[i][j] != null) {
							_entity[i][j].onUpdate();
							_entity[i][j].onDraw(gl);
						}
				} else if(_drawOrder[i] == DrawOrder.TOP_TO_BOTTOM) {
					
					for(int j = 0; j < _maxEntityCount; j++) {		

						int lowestY = 0, lowestYIndex = -1;
						
						//Loop through all of them, find next lowest one
						for(int k = 0; k < _maxEntityCount; k++) {
							if(_entity[i][k] != null) {
								
								//If it is already chosen, skip it
								boolean isChosen = false;
								for(int l = 0; l < _maxEntityCount; l++)
									if(_drawOrderEntity[l] == _entity[i][k])
										isChosen = true;
								
								//If it is not chosen, check
								if(!isChosen) {
									if(lowestYIndex == -1 || (_entity[i][k].getY() < lowestY)) {
										lowestY = _entity[i][k].getY();
										lowestYIndex = k;
									}
								}
							}
						}
						
						//If lowest index is not -1, add
						if(lowestYIndex > -1) {
							_drawOrderEntity[j] = _entity[i][lowestYIndex];
						} else
							break;
					}
					
					
					//Draw these
					for(int j = 0; j < _maxEntityCount; j++)
						if(_drawOrderEntity[j] != null) {
							_drawOrderEntity[j].onUpdate();
							_drawOrderEntity[j].onDraw(gl);
						}
					
					//Remove from queue
					for(int k = 0; k < _maxEntityCount; k++) {
						_drawOrderEntity[k] = null;
					}
				}
				onPostDraw(gl, i);
				gl.glPopMatrix();
			} else {
				onPreDraw(gl, i);
				for(int j = 0; j < _maxEntityCount; j++)
					if(_entity[i][j] != null) {
						_entity[i][j].onUpdate();
						_entity[i][j].onDraw(gl);
					}
				onPostDraw(gl, i);
			}
		}
		onPostDraw(gl);
		if(_hasChildScene)
			_childScene.onDraw(gl);
	}
	
	protected void loadScene(Bundle incomingBundle) {
		lastBundle = incomingBundle;
		thisBundle = new Bundle();
		for (Iterator<TextureAtlas> it = _textureAtlas.iterator(); it.hasNext(); ) {
			TextureAtlas atlas = it.next();
			if(!atlas.isOnHardware()) {
				TextureManager.load(atlas);
			}
		}
		onSceneLoad(thisBundle);
		System.gc();
	}
	
	protected Bundle destroyScene() {
		if(_childScene != null)
			_childScene.destroyScene();
		if(_parentScene != null) {
			for (Iterator<TextureAtlas> it = _textureAtlas.iterator(); it.hasNext(); ) {
				TextureAtlas atlas = it.next();
				if(!_parentScene.usesAtlas(atlas))
					TextureManager.remove(atlas);
			}
		} else 
			TextureManager.removeAll();
		return thisBundle;
	}
	
	protected boolean usesAtlas(TextureAtlas atlas) {
		return _textureAtlas.contains(atlas);
	}
	
	public void addTextureAtlas(TextureAtlas atlas) {
		_textureAtlas.add(atlas);
	}
	
	public void removeTextureAtlas(TextureAtlas atlas) {
		_textureAtlas.remove(atlas);
	}
	
	public void clearTextureAtlas() {
		_textureAtlas.clear();
	}
	
	public void changeScene(Scene nextScene) {
		Rokon.rokon.setScene(nextScene);
	}
	
	public void checkForTouchables() {
		_checkForTouchables = true;
	}
	
	public void checkForTouchables(boolean checkLayers) {
		_checkForTouchables = true;
		_checkForTouchableLayers = checkLayers;
	}
	
	public boolean isCheckingForTouchables() {
		return _checkForTouchables;
	}
	
	public boolean isCheckingForTouchableLayers() {
		return _checkForTouchableLayers;
	}
	
	public void addTouchable(Entity entity) {
		for(int i = 0; i < _touchable.length; i++)
			if(_touchable[i] == null) {
				_touchable[i] = entity;
				return;
			}
		Debug.warning("Too many touchable's in the scene");
	}
	
	public void removeTouchable(Entity entity) {
		for(int i = 0; i < _touchable.length; i++)
			if(_touchable[i] == entity)
				_touchable[i] = null;
	}
	
	protected void handleTouch(int x, int y, MotionEvent event) {
		if(_hasChildScene) {
			_childScene.handleTouch(x, y, event);
			if(_childSceneModal)
				return;
		}
		onTouch(x, y, event);
		switch(event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				onTouchDown(x, y, event);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(x, y, event);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(x, y, event);
				break;
		}
		if(_checkForTouchables) {
			for(int i = 0; i < _touchable.length; i++)
				if(_touchable[i] != null) {
					if(MathHelper.coordInRect(x, y, _touchable[i].getX() - _touchable[i].getTouchBorder(), _touchable[i].getY() - _touchable[i].getTouchBorder(), _touchable[i].getWidth() + _touchable[i].getTouchBorder() + _touchable[i].getTouchBorder(), _touchable[i].getHeight() + _touchable[i].getTouchBorder() + _touchable[i].getTouchBorder())) {
						switch(event.getAction()) {
							case MotionEvent.ACTION_DOWN:
								_touchable[i].onTouch(x, y, event);
								_touchable[i].onTouchDown(x, y, event);
								onTouch(_touchable[i], x, y, event);
								onTouchDown(_touchable[i], x, y, event);
								_touchable[i].setTouchOn();
								break;
							case MotionEvent.ACTION_MOVE:
								_touchable[i].onTouch(x, y, event);
								_touchable[i].onTouchMove(x, y, event);
								onTouch(_touchable[i], x, y, event);
								onTouchMove(_touchable[i], x, y, event);
								break;
							case MotionEvent.ACTION_UP:
								_touchable[i].onTouch(x, y, event);
								_touchable[i].onTouchUp(x, y, event);
								_touchable[i].setTouchOff();
								onTouch(_touchable[i], x, y, event);
								onTouchUp(_touchable[i], x, y, event);
								break;
						}
					} else if(_touchable[i].isTouchOn()) {
						_touchable[i].setTouchOff();
						_touchable[i].onTouchExit();
					}
				}
		}
		if(_checkForTouchableLayers)
			for(int i = 0; i < _layerCount; i++)
				for(int j = 0; j < _maxEntityCount; j++)
					if(_entity[i][j] != null)
						if(MathHelper.coordInRect(x, y, _entity[i][j].getX() - _entity[i][j].getTouchBorder(), _entity[i][j].getY() - _entity[i][j].getTouchBorder(), _entity[i][j].getWidth() + _entity[i][j].getTouchBorder() + _entity[i][j].getTouchBorder(), _entity[i][j].getHeight() + _entity[i][j].getTouchBorder() + _entity[i][j].getTouchBorder())) {
							switch(event.getAction()) {
								case MotionEvent.ACTION_DOWN:
									_entity[i][j].onTouch(x, y, event);
									_entity[i][j].onTouchDown(x, y, event);
									_entity[i][j].setTouchOn();
									onTouchDown(_entity[i][j], x, y, event);
									break;
								case MotionEvent.ACTION_MOVE:
									_entity[i][j].onTouch(x, y, event);
									_entity[i][j].onTouchMove(x, y, event);
									onTouchMove(_entity[i][j], x, y, event);
									break;
								case MotionEvent.ACTION_UP:
									_entity[i][j].onTouch(x, y, event);
									_entity[i][j].onTouchUp(x, y, event);
									_entity[i][j].setTouchOff();
									onTouchUp(_entity[i][j], x, y, event);
									break;
							} 
						} else if(_entity[i][j].isTouchOn()) {
							_entity[i][j].setTouchOff();
							_entity[i][j].onTouchExit();
						}
	}
	
	public void setChildScene(Scene scene, boolean modal) {
		_childScene = scene;
		_childSceneModal = modal;
		_childScene.setParent(this);
		_childScene.loadScene(thisBundle);
		_hasChildScene = true;
	}
	
	public void removeChildScene() {
		_childScene = null;
		_hasChildScene = false;
	}
	
	public Scene getChildScene() {
		return _childScene;
	}
	
	protected void handleKeyDown(int keyCode, KeyEvent event) {
		if(_hasChildScene) {
			_childScene.handleKeyDown(keyCode, event);
			if(_childSceneModal)
				return;
		}
		onKeyDown(keyCode, event);
	}
	
	protected void handleKeyUp(int keyCode, KeyEvent event) {
		if(_hasChildScene) {
			_childScene.handleKeyUp(keyCode, event);
			if(_childSceneModal)
				return;
		}
		onKeyUp(keyCode, event);
	}
	
	public boolean isChild() {
		return _isChild;
	}
	
	protected void setParent(Scene parent) {
		_parentScene = parent;
		lastBundle = _parentScene.thisBundle;
		_isChild = true;
	}
	
	protected void childClosed() {
		_childScene = null;
		_hasChildScene = false;
		onChildClosed(_childScene.thisBundle);
	}
	
	public void closeScene() {
		if(_parentScene != null) {
			_parentScene.childClosed();
			destroyScene();
		}
	}
	
	protected void handleGameLoop() {
		if(_hasChildScene) {
			_childScene.handleGameLoop();
			if(_childSceneModal)
				return;
		}
		onGameLoop();
	}

}