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
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.gloop.GloopOnChangeListener;
import io.gloop.drawed.model.Board;
import io.gloop.drawed.model.Line;
import io.gloop.drawed.model.Point;
import io.gloop.drawed.utils.ScreenUtil;

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

    private Board board;
    private List<Point> line;
    private SaveInBackgroundWorker worker;

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

    private void drawLines() {
        for (Line line : board.getLines()) {

            Line l = ScreenUtil.scale(line);

            if (l != null) {
                List<Point> points = l.getPoints();
                if (points.size() > 0) {

                    drawPaint.setColor(l.getColor());
                    float lineThickness = ScreenUtil.scale((float) line.getBrushSize());
                    drawPaint.setStrokeWidth(lineThickness);
//
//                    int i = 1;
//                    Point firstPoint;
//                    do {
//                        firstPoint = points.get(i++);
//                    } while (((int) firstPoint.getX()) == 0 || ((int) firstPoint.getY()) == 0);

                    Point firstPoint = points.get(0);
                    drawPath.moveTo(firstPoint.getX(), firstPoint.getY());

                    int size = points.size();
                    for (int i = 1; i < size; i++) {
                        Point point = points.get(i);

                        if (((int) point.getX()) == 0 || ((int) point.getY()) == 0)   // cast to int for correct equality check with 0
                            continue;

                        drawPath.lineTo(point.getX(), point.getY());
                    }
//                    Point lastPoint = points.get(size-1);
//                    drawPath.moveTo(lastPoint.getX(), lastPoint.getY());

                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                }
            }
        }
        drawPaint.setColor(paintColor);
        drawPaint.setStrokeWidth(brushSize);
//        invalidate();
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

                    // create new line and add to worker to save it in the background.
                    worker.addItem(new Line(line, paintColor, (int) ScreenUtil.normalize(brushSize)));
                    break;
                default:
                    return false;
            }
            //redraw
            invalidate();
        }
        return true;
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

    public void setBoard(final Board board, SaveInBackgroundWorker worker) {
        this.board = board;
        this.worker = worker;
        this.readOnly = this.board.isFreezeBoard();

        final Activity host = (Activity) getContext();

        this.board.removeOnChangeListeners();
        this.board.addOnChangeListener(new GloopOnChangeListener() {

            @Override
            public void onChange() {

                // TODO slows down the drawing on the app
                DrawingView.this.board.loadLocal();  // local because they are already pushed over the websocket.
                host.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        drawLines();
                    }
                });
            }
        });

        if (this.readOnly) {
            Snackbar snackbar = Snackbar.make(host.findViewById(R.id.item_detail_root), R.string.not_allowed_to_draw, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.board.removeOnChangeListeners();
    }
}