package com.xsonsui.maxball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.xsonsui.maxball.game.GameInputListener;
import com.xsonsui.maxball.game.GameViewInterface;
import com.xsonsui.maxball.model.Ball;
import com.xsonsui.maxball.model.Game;
import com.xsonsui.maxball.model.Player;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, GameViewInterface {

    private RectF rect = new RectF();
    private Paint paintRed = new Paint();
    private Paint paintGray = new Paint();
    private Paint paintGreen = new Paint();
    private Paint paintWhite = new Paint();
    private float touchStartX;
    private float touchStartY;
    private int touchStartInd = -1;
    private GameInputListener gameInputListener;
    private int width;
    private int height;
    private boolean ready = false;
    private SurfaceHolder mHolder;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public GameView(Context context) {
        super(context);

        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        paintGray.setColor(Color.GRAY);
        paintGreen.setColor(Color.GREEN);
        paintWhite.setColor(Color.WHITE);
        paintRed.setColor(Color.RED);
    }


    private void drawPlayer(Canvas canvas, Player p) {
        rect.set(p.position.x - p.radius,
                p.position.y - p.radius,
                p.position.x + p.radius,
                p.position.y + p.radius);
        canvas.drawOval(rect, paintRed);
    }

    private void drawBall(Canvas canvas, Ball p) {
        rect.set(p.position.x - p.radius,
                p.position.y - p.radius,
                p.position.x + p.radius,
                p.position.y + p.radius);
        canvas.drawOval(rect, paintGray);
    }

    public void draw(Game game) {
        if (!ready) {
            return;
        }
        if(mHolder.getSurface().isValid()){
            Canvas canvas = mHolder.lockCanvas();

            canvas.drawRect(0,0,width,height, paintWhite);
            canvas.translate(width/2.0f, height/2.0f);
            canvas.scale(width/Game.ARENA_WIDTH_2/2, width/Game.ARENA_WIDTH_2/2);
            canvas.drawRect(-Game.ARENA_WIDTH_2, -Game.ARENA_HEIGHT_2,Game.ARENA_WIDTH_2, Game.ARENA_HEIGHT_2, paintGreen);
            synchronized (game) {
                for (Player p : game.players.values()) {
                    drawPlayer(canvas, p);
                }
                drawBall(canvas, game.ball);
            }
            mHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void setGameInputListener(GameInputListener listener){
        gameInputListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameInputListener == null)
            return false;
        int action = event.getAction();
        switch(action){
            case MotionEvent.ACTION_DOWN:

                if(event.getX() < getWidth()/2) {
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    touchStartInd = event.getActionIndex();
                } else {
                    gameInputListener.inputKick(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getActionIndex()==touchStartInd) {
                    gameInputListener.inputMove(
                            event.getX() - touchStartX,
                            event.getY() - touchStartY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(event.getActionIndex()==touchStartInd) {
                    gameInputListener.inputMove(0, 0);
                    touchStartInd=-1;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            case MotionEvent.ACTION_OUTSIDE:
                break;
            default:
        }
        return true; //processed
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        this.ready = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder,  int format, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        this.ready = false;
    }

}
