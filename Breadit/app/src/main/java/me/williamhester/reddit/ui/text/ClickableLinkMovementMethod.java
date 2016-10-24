package me.williamhester.reddit.ui.text;

import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by william on 12/21/14.
 */
public class ClickableLinkMovementMethod extends LinkMovementMethod {

  @Override
  public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer,
                              @NonNull MotionEvent event) {
    int action = event.getAction();
    if (action == MotionEvent.ACTION_UP ||
        action == MotionEvent.ACTION_DOWN) {
      int x = (int) event.getX();
      int y = (int) event.getY();
      x -= widget.getTotalPaddingLeft();
      y -= widget.getTotalPaddingTop();
      x += widget.getScrollX();
      y += widget.getScrollY();
      Layout layout = widget.getLayout();
      int line = layout.getLineForVertical(y);
      int off = layout.getOffsetForHorizontal(line, x);
      ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
      if (link.length != 0) {
        if (action == MotionEvent.ACTION_UP) {
          link[0].onClick(widget);
        } else {
          Selection.setSelection(buffer,
              buffer.getSpanStart(link[0]),
              buffer.getSpanEnd(link[0]));
        }
        return true;
      } else {
        widget.getParent().requestDisallowInterceptTouchEvent(false);
        ((View) widget.getParent()).onTouchEvent(event);
        return false;
      }
    } else {
      ((View) widget.getParent()).onTouchEvent(event);
      Selection.removeSelection(buffer);
    }
    return false;
  }
}
