package net.orangebytes.slide.model;

import net.orangebytes.slide.preferences.GameState;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/// Singleton puzzle class, for storing the puzzle
public class Puzzle {
	
	/// The shared instance
	private static Puzzle sInstance = null;
	
	/// Whether or not a puzzle is in progress
	private boolean mPuzzleActive;
	
	/// Whether or not a puzzle is being shuffled
	private boolean mShuffling;
	
	/// Shuffling direction
	private int mLastDirection = -1;
	
	/// Shuffling mix count
	private int mMixCount = 0;
	
	/// The model for the tiles
	private PuzzleTile mPuzzleTiles[];
	
	
	private View  mView;
	private float mLastX;
	private float mLastY;
	private boolean mSliding;
	private boolean mBlock;
	
	
	/// Private constructor
	private Puzzle() {
		mPuzzleActive = false;
	}
	
	/// Private method for shuffling the puzzle recursively
	private void shufflePuzzle(final GameState pGameState) {
		
	    int cellCount = mPuzzleTiles.length;
	    int cell = (int) (Math.random()*(cellCount-1));
	    int dir = (int) (Math.random()*4);
	    
	    while(dir%2 == mLastDirection%2 && (Math.random()*100 < 80)) {
	        dir = (int) (Math.random()*4);
	    }

	    if (mPuzzleTiles[cell].canSlide(dir)) {
	        mLastDirection = dir;
	        mMixCount++;
	        
	        if(mMixCount >= pGameState.getSize() * 3){
	        	mMixCount = 0;
	        	mLastDirection = -1;
	        	mShuffling = false;
	        	mPuzzleActive = true;
	            return;
	        }
	        
	        mPuzzleTiles[cell].swap(dir, false);

	        final Handler handler = new Handler();
	        handler.postDelayed(new Runnable() {
	          @Override
	          public void run() {
	        	  shufflePuzzle(pGameState);
	          }
	        }, 110);
	    }else{
	    	shufflePuzzle(pGameState);
	    }
	}
	
	/// Singleton accessor
	public static Puzzle get() {
		if(sInstance == null) {
			sInstance = new Puzzle();
		}
		return sInstance;
	}
	
	/// Returns true if the puzzle is active
	public boolean isActive() {
		return mPuzzleActive;
	}
	
	/// Returns true if the puzzle is being shuffled
	public boolean isShuffling() {
		return mShuffling;
	}

	/// Generates the initial puzzle model links for a given puzzle size
	public void generateLinks(GameState pState) {
		mPuzzleActive = false;
		
		mPuzzleTiles = new PuzzleTile[pState.getSize()];
    	int index = 0;

    	for(int i = 0; i < pState.getX(); i++){
    		for(int j = 0; j< pState.getY(); j++){
    			mPuzzleTiles[index] = new PuzzleTile();
    			index++;
    		}
    	}
    	
    	index = 0;
    	for(int i = 0; i < pState.getX(); i++){
    		for(int j = 0; j< pState.getY(); j++){
                if(j > 0){
                	mPuzzleTiles[index].mNeighbours[1] = mPuzzleTiles[index-1];
                }
                if(j< pState.getY()-1){
                	mPuzzleTiles[index].mNeighbours[3] = mPuzzleTiles[index+1];
                }
                if(i > 0){
                	mPuzzleTiles[index].mNeighbours[0] = mPuzzleTiles[index-pState.getY()];
                }
                if(i< pState.getX()-1){
                	mPuzzleTiles[index].mNeighbours[2] = mPuzzleTiles[index+pState.getY()];
                }
                index++;
    		}
    	}
	}
	
	/// Links a set of views into the puzzle
	public void linkPuzzle(GameState pState, ImageView pViews[]) {
    	int index = 0;
    	for(int i = 0; i < pState.getX(); i++){
    		for(int j = 0; j< pState.getY(); j++){
                pViews[index].setTag(mPuzzleTiles[index]);
                mPuzzleTiles[index].setView(pViews[index]);
                
                if(index == pState.getSize()-1)
                	mPuzzleTiles[index].setEmpty(true);
                
                index++;
    		}
    	}
	}

	/// Shuffles a puzzle for a given game state
	public void shuffle(GameState pGameState) {
		mShuffling = true;
		mLastDirection = -1;
		mMixCount = 0;
		
		shufflePuzzle(pGameState);
	}
	
	/// Returns true if the puzzle has been solved
	public boolean isSolved(GameState pState) {
		if(!isActive() && !isShuffling())
			return false;
		
		int index = 0;
    	for(int i = 0; i < pState.getX(); i++){
    		for(int j = 0; j< pState.getY(); j++){
    			
                if(j > 0){
                	if(mPuzzleTiles[index].mNeighbours[1] != mPuzzleTiles[index-1])
                		return false;
                }
                if(j< pState.getY()-1){
                	if(mPuzzleTiles[index].mNeighbours[3] != mPuzzleTiles[index+1])
                		return false;
                }
                if(i > 0){
                	if(mPuzzleTiles[index].mNeighbours[0] != mPuzzleTiles[index-pState.getY()])
                		return false;
                }
                if(i< pState.getX()-1){
                	if(mPuzzleTiles[index].mNeighbours[2] != mPuzzleTiles[index+pState.getY()])
                		return false;
                }
                index++;
		    }
    	}  
    	
    	mPuzzleActive = false;
    	return true;
	}
	
	
	public void touchDown(View pView, float pX, float pY) {
		mSliding = false;
		mView = pView;
		mLastX = pX;
		mLastY = pY;
		mBlock = false;
		
		Log.d("TouchDown", "Sliding: " + mSliding + ", X: " + pX + ", " + pY);
	}
	
	public void touchMove(float pX, float pY) {
		Log.d("TouchMove", "X: " + pX + ", " + "Y: " + pY);
		
		if(mView != null && !mBlock) {
			Log.d("TouchMove", "Has view");
			PuzzleTile p = (PuzzleTile)mView.getTag();
			
			if(p.isEmpty())
				return;
			
			mSliding = true;
			
			Log.d("TouchMove", "Has puzzle tile");
			float deltaX = (pX - mLastX);
			float deltaY = (pY - mLastY);

			Log.d("TouchMove", "deltaX: " + deltaX + ", deltaY: " + deltaY);
			mLastX = pX;
			mLastY = pY;
			
			if(deltaX < 0) {
				if(p.canSlide(0)){
					Log.d("TouchMove", "sliding left");
					mLastDirection = 0;
					mBlock = p.slide(0, (int) deltaX);
					
					return;
				}
			} else {
				if(p.canSlide(2)) {
					Log.d("TouchMove", "sliding right");
					mLastDirection = 2;
					mBlock = p.slide(2, (int) deltaX);
					
					return;
				}
			}
			
			if(deltaY < 0) {
				if(p.canSlide(1)) {
					Log.d("TouchMove", "sliding up");
					mLastDirection = 1;
					mBlock = p.slide(1, (int) deltaY);
					
					return;
				}
			}
			else {
				if(p.canSlide(3)) {
					Log.d("TouchMove", "sliding down");
					mLastDirection = 3;
					mBlock = p.slide(3, (int) deltaY);
					
					return;
				}
			}
		}
	}
	
	public boolean touchFinished(float pX, float pY) {
		Log.d("TouchFinished", "X: " + pX + ", " + "Y: " + pY);
		if(mBlock)
			return true;
		
		if(mView != null && mSliding) {
			PuzzleTile p = (PuzzleTile)mView.getTag();
			
			float halfWay = mView.getWidth() / 3;
			float deltaX = (mView.getLeft() - p.getRealLayout().leftMargin);
			float deltaY = (mView.getTop() - p.getRealLayout().topMargin);
			Log.d("TouchFinished", "Half: " + halfWay + ", deltaX: " + deltaX + ", deltaY: " +deltaY );
			
			if(mLastDirection == 0 || mLastDirection == 2) {
				if(Math.abs(deltaX) >= halfWay ) {
					p.swap(mLastDirection, true);
					Log.d("TouchFinished", "swapping in direction: " + mLastDirection);
					return true;
				} else if(Math.abs(deltaX) >= 10) {
					p.unslide(mLastDirection);
					Log.d("TouchFinished", "unsliding in direction: " + mLastDirection);
					return false;
				}
			} else {
				if(Math.abs(deltaY) >= halfWay) {
					p.swap(mLastDirection, true);
					Log.d("TouchFinished", "swapping in direction: " + mLastDirection);
					return true;
				} else if(Math.abs(deltaY) >= 10) { 
					p.unslide(mLastDirection);
					Log.d("TouchFinished", "unsliding in direction: " + mLastDirection);
					return false;
				}
			}
			
			Log.d("TouchFinished", "Searching for a tap");
    		for(int i = 0; i<4; i++) {
    			if (p.canSlide(i)){
    				p.swap(i, false);
    				Log.d("TouchFinished", "Sliding in direction:" + i);
    				return true;
    			}
    		}
		}
		
		return false;
	}
}