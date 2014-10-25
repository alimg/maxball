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
import com.xsonsui.maxball.model.Vector2f;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, GameViewInterface {

    private RectF rect = new RectF();
    private Paint paintRed = new Paint();
    private Paint paintGray = new Paint();
    private Paint paintGreen = new Paint();
    private Paint paintLRed = new Paint();
    private Paint paintLBlue = new Paint();
    private Paint paintBlue = new Paint();
    private Paint paintWhite = new Paint();
    private Paint paintBlack = new Paint();
    private float touchStartX;
    private float touchStartY;
    private int touchStartId = -1;
    private GameInputListener gameInputListener;
    private int width;
    private int height;
    private boolean ready = false;
    private SurfaceHolder mHolder;
    private boolean touchState = false;
    private float touchLastX;
    private float touchLastY;
    private float touchInputMultiplier = 3;

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
        paintLBlue.setColor(0xaa6666ff);
        paintLRed.setColor(0xaaff6666);
        paintWhite.setColor(Color.WHITE);
        paintBlack.setColor(Color.BLACK);
        paintRed.setColor(Color.RED);
        paintBlue.setColor(Color.BLUE);

        paintBlack.setTextSize(60);
    }


    private void drawPlayer(Canvas canvas, Player p) {
        rect.set(p.position.x - p.radius,
                p.position.y - p.radius,
                p.position.x + p.radius,
                p.position.y + p.radius);
        if (p.team == Game.TEAM_RED)
            canvas.drawOval(rect, paintRed);
        else if (p.team == Game.TEAM_BLUE)
            canvas.drawOval(rect, paintBlue);
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
            canvas.save();
            canvas.translate(width/2.0f, height/2.0f);
            canvas.scale(width/Game.ARENA_WIDTH_2/2, width/Game.ARENA_WIDTH_2/2);
            canvas.drawRect(-Game.ARENA_WIDTH_2, -Game.ARENA_HEIGHT_2, Game.ARENA_WIDTH_2, Game.ARENA_HEIGHT_2, paintGreen);
            canvas.drawRect(-Game.ARENA_WIDTH_2, -Game.GOAL_AREA_SIZE,
                    -Game.ARENA_WIDTH_2+Game.GOAL_AREA_SIZE, Game.GOAL_AREA_SIZE, paintLRed );
            canvas.drawRect(Game.ARENA_WIDTH_2-0.0001f, -Game.GOAL_AREA_SIZE,
                    Game.ARENA_WIDTH_2-Game.GOAL_AREA_SIZE, Game.GOAL_AREA_SIZE, paintLBlue );
            synchronized (game) {
                for (Player p : game.players.values()) {
                    drawPlayer(canvas, p);
                }
                drawBall(canvas, game.ball);
            }
            canvas.restore();
            canvas.drawText("Score: "+game.scoreRed+"-"+game.scoreBlue,0,100,paintBlack);
            if (touchState) {
                Vector2f f = new Vector2f(touchLastX-touchStartX, touchLastY-touchStartY);
                if(f.length()>Game.MAX_PLAYER_FORCE/touchInputMultiplier) {
                    f.normalize();
                    f.multiply(Game.MAX_PLAYER_FORCE/touchInputMultiplier);
                }
                canvas.drawLine(touchStartX, touchStartY, f.x + touchStartX, f.y + touchStartY, paintBlack);
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
        int action = event.getActionMasked();

        int pointerId = event.getPointerId(event.getActionIndex());
        switch(action){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:

                if (event.getX() < getWidth()*0.8 && !touchState) {
                    touchStartX = event.getX();
                    touchStartY = event.getY();
                    touchStartId = pointerId;
                    touchState = true;
                } else {
                    gameInputListener.inputKick(1);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointerId == touchStartId) {
                    touchLastX = event.getX();
                    touchLastY = event.getY();
                    gameInputListener.inputMove(
                            (event.getX() - touchStartX)*touchInputMultiplier,
                            (event.getY() - touchStartY)*touchInputMultiplier);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                 if (pointerId == touchStartId && touchState) {
                    gameInputListener.inputMove(0, 0);
                    touchStartId =-1;
                    touchState = false;
                } else {
                    gameInputListener.inputKick(0);
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
