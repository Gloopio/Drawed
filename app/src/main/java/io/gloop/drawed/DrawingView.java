package io.gloop.drawed;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Callable;

import io.gloop.Gloop;
import io.gloop.GloopLogger;
import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;
import io.gloop.drawed.model.UserInfo;
import io.gloop.drawed.utils.LineUtil;
import io.gloop.drawed.utils.ScreenUtil;
import io.gloop.permissions.GloopGroup;

public class DrawingView extends View {

    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;
    //brush sizes
    private float brushSize;
    //erase flag
    private boolean erase = false;

    private boolean readOnly = false;

    private int lineSize;

    private UserInfo userInfo;
    private Board board;
    private List<Point> line;
    private Point erasePoint;

    private boolean run = true;
    private boolean isSelfChanging = false;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDrawing();
    }

    //setup drawing
    private void setupDrawing() {
        //prepare for drawing and setup paint stroke properties
        brushSize = getResources().getInteger(R.integer.medium_size);
        drawPath = new Path();
        drawPaint = new Paint();
        paintColor = 0xFF660000;
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.board.removeOnChangeListeners();
    }

    //size assigned to view
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);

        if (board != null)
            drawLines();
    }

    //draw the view - will be called after touch event
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    //register user touches as drawing action
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!erase) {
            if (!this.readOnly) {
                float touchX = event.getX();
                float touchY = event.getY();

                if (((int) touchX) == 0 || ((int) touchY) == 0)
                    return false;

                //respond to down, move and up events
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        line = new ArrayList<>();
                        drawPath.moveTo(touchX, touchY);
                        line.add(new Point(touchX, touchY));
                        break;
                    case MotionEvent.ACTION_MOVE:
                        drawPath.lineTo(touchX, touchY);
                        line.add(new Point(touchX, touchY));
                        break;
                    case MotionEvent.ACTION_UP:
                        drawPath.lineTo(touchX, touchY);
                        line.add(new Point(touchX, touchY));
                        drawCanvas.drawPath(drawPath, drawPaint);
                        drawPath.reset();

                        Line newLine = new Line(line, paintColor, (int) brushSize);
                        newLine.setBrushSize((int) ScreenUtil.normalize(newLine.getBrushSize()));
                        newLine = ScreenUtil.normalize(newLine);
                        board.addLine(newLine);

                        // create new line and add to worker to save it in the background.
//                        SaveInBackgroundWorker.getInstance().addItem(board, line, paintColor, brushSize);
                        BackgroundService.schedule(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                synchronized (board) {
//                                    board.addLine(newLine);
                                    isSelfChanging = true;

                                    if (!board.getMembers().containsKey(userInfo.getEmail())) {
                                        if (userInfo.getImageURL() != null) {
                                            board.addMember(userInfo.getEmail(), userInfo.getImageURL().toString());
                                        } else
                                            board.addMember(userInfo.getEmail(), null);

                                        GloopLogger.i("Found board.");

                                        // if PUBLIC board add your self to the group.
                                        GloopGroup group = Gloop
                                                .all(GloopGroup.class)
                                                .where()
                                                .equalsTo("objectId", board.getOwner())
                                                .first();

                                        if (group != null) {
                                            GloopLogger.i("GloopGroup found add myself to group and save");
                                            group.addMember(Gloop.getOwner().getUserId());
                                            group.save();

                                            if (userInfo.getImageURL() != null)
                                                board.addMember(userInfo.getEmail(), userInfo.getImageURL().toString());
                                            else
                                                board.addMember(userInfo.getEmail(), null);

                                        } else {
                                            GloopLogger.e("GloopGroup not found!");
                                        }
                                    }

                                    board.save();

                                    GloopLogger.i("Object saved");
                                    isSelfChanging = false;
                                }

                                return null;
                            }
                        });


                        break;
                    default:
                        return false;
                }
                //redraw
                invalidate();
            }

        } else {    // delete lines
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    erasePoint = new Point(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    deleteIntersectedLine(board.getLines(), erasePoint, new Point(event.getX(), event.getY()));
                    erasePoint = new Point(event.getX(), event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    deleteIntersectedLine(board.getLines(), erasePoint, new Point(event.getX(), event.getY()));
                    erasePoint = new Point(event.getX(), event.getY());
                    break;
                default:
                    return false;
            }
            invalidate();
        }
        return true;
    }

    public void deleteIntersectedLine(final List<Line> lines, final Point erasePointP, final Point pointP) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Point erasePoint = ScreenUtil.normalize(erasePointP);
                Point point = ScreenUtil.normalize(pointP);

                List<Line> linesToRemove = new ArrayList<>();

                synchronized (board) {
                    board.loadLocal();
                    for (Line line : board.getLines()) {
                        if (line == null)
                            continue;
                        List<Point> points = line.getPoints();
                        for (int i = 0; i < points.size() - 1; i++) {
                            if (LineUtil.intersect(points.get(i), points.get(i + 1), erasePoint, point)) {
                                linesToRemove.add(line);
                                GloopLogger.i("Line found ");
                                break;
                            }
                        }
                        GloopLogger.i("iterate");
                    }

                    if (linesToRemove.size() > 0) {
                        board.getLines().removeAll(linesToRemove);

                        isSelfChanging = true;
                        board.save();
                        isSelfChanging = false;
                    }
                }
                if (linesToRemove.size() >0 ) {
                    drawLines();
                }
            }
        }).start();
    }

    private void drawLines() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (lineSize != board.getLines().size()) {
                        GloopLogger.i("Draw");
                        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//                    List<Line> tmp;
//                        synchronized (board) {

//                            board.loadLocal();
//                        tmp = new ArrayList<>(board.getLines());
//                    }

                            for (Line line : board.getLines()) {

                                Line l = ScreenUtil.scale(line);

                                if (l != null) {
                                    List<Point> points = l.getPoints();
                                    if (points.size() > 0) {

                                        Paint drawPaint = new Paint();
                                        drawPaint.setAntiAlias(true);
                                        drawPaint.setStyle(Paint.Style.STROKE);
                                        drawPaint.setStrokeJoin(Paint.Join.ROUND);
                                        drawPaint.setStrokeCap(Paint.Cap.ROUND);

                                        Path drawPath = new Path();


                                        drawPaint.setColor(l.getColor());
                                        float lineThickness = ScreenUtil.scale((float) line.getBrushSize());
                                        drawPaint.setStrokeWidth(lineThickness);

                                        Point firstPoint = points.get(0);
                                        drawPath.moveTo(firstPoint.getX(), firstPoint.getY());

                                        int size = points.size();
                                        for (int i = 1; i < size; i++) {
                                            Point point = points.get(i);

                                            if (((int) point.getX()) == 0 || ((int) point.getY()) == 0)   // cast to int for correct equality check with 0
                                                continue;

                                            drawPath.lineTo(point.getX(), point.getY());
                                        }

                                        drawCanvas.drawPath(drawPath, drawPaint);
                                        drawPath.reset();
                                    }
                                }
                            }
                        }
                        drawPaint.setColor(paintColor);
                        drawPaint.setStrokeWidth(brushSize);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        });
//                    }
                } catch (ConcurrentModificationException e) {
                    e.printStackTrace();
                    drawLines();
                }
            }
        }).start();
    }


    //update color

    public void setColor(String newColor) {
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
    }

    //set brush size
    public void setBrushSize(float newSize) {
        brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        brushSize = ScreenUtil.scale(brushSize);
        drawPaint.setStrokeWidth(brushSize);
    }

    //set erase true or false
    public void setErase(boolean isErase) {
        erase = isErase;
        if (erase)
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else
            drawPaint.setXfermode(null);
    }

    //start new drawing
    public void startNew() {
        drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        this.board.clear();
        invalidate();
    }

    public void setBoard(final Board board, UserInfo userInfo) {
        this.board = board;
        this.userInfo = userInfo;
        this.readOnly = this.board.isFreezeBoard();


        final Activity host = (Activity) getContext();

        this.isSelfChanging = false;
        this.lineSize = -1;
        this.board.removeOnChangeListeners();
        this.board.addOnChangeListener(new GloopOnChangeListener() {

            @Override
            public void onChange() {
                GloopLogger.i("Called");
                if (!isSelfChanging) {
                    GloopLogger.i("Board has changed");
                    DrawingView.this.board.loadLocal();  // local because they are already pushed over the websocket.
                    host.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawLines();
                        }
                    });
                }
            }
        });

        if (this.readOnly) {
            Snackbar snackbar = Snackbar.make(host.findViewById(R.id.item_detail_root), R.string.not_allowed_to_draw, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }
}