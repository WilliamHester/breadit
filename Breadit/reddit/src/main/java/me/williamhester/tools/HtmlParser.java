package me.williamhester.tools;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.ClickableSpan;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import me.williamhester.reddit.R;
import me.williamhester.ui.text.LinkSpan;
import me.williamhester.ui.text.SpoilerSpan;

/**
 * Created by William on 6/15/14.
 */
public class HtmlParser {

    private List<String> mLinks = new ArrayList<>();
    private SpannableStringBuilder mSpannableStringBuilder;

    public HtmlParser(String html) {
        mSpannableStringBuilder = parseHtml(html);
    }

    public @NonNull SpannableStringBuilder getSpannableString() {
        return mSpannableStringBuilder;
    }

    public List<String> getLinks() {
        return mLinks;
    }

    private SpannableStringBuilder parseHtml(String html) {
        if (html != null) {
            Document document = Jsoup.parse(html);
            SpannableStringBuilder sb = generateString(document);
            while (sb.length() > 0 && (sb.charAt(0) == '\n' || sb.charAt(0) == ' ')) {
                sb.delete(0, 1);
            }
            return sb;
        }
        return new SpannableStringBuilder().append("");
    }

    private SpannableStringBuilder generateString(Node node) {
        return generateString(node, new SpannableStringBuilder());
    }

    private SpannableStringBuilder generateString(Node node, SpannableStringBuilder sb) {
        if (node instanceof TextNode) {
            return new SpannableStringBuilder(((TextNode) node).text());
        }

        insertNewLine(node, sb);

        List<Node> children = node.childNodes();
        for (Node n : children) {
            sb.append(generateString(n));
        }

        if (sb.length() > 0) {
            sb.setSpan(getSpanFromTag(node), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    private Object getSpanFromTag(Node node) {
        if (node instanceof Element) {
            String tag = ((Element) node).tag().getName();
            if (tag.equalsIgnoreCase("code")) {
                return new TypefaceSpan("monospace");
            } else if (tag.equalsIgnoreCase("del")) {
                return new StrikethroughSpan();
            } else if (tag.equalsIgnoreCase("strong")) {
                return new StyleSpan(Typeface.BOLD);
            } else if (tag.equalsIgnoreCase("em")) {
                return new StyleSpan(Typeface.ITALIC);
            } else if (tag.equalsIgnoreCase("blockquote")) {
                return new QuoteSpan(Color.rgb(246, 128, 38));
            } else if (tag.equalsIgnoreCase("sup")) {
                return new SuperscriptSpan();
            } else if (tag.equalsIgnoreCase("a")) {
                String url = node.attr("href");
                if (url.equals("/spoiler")) {
                    return new SpoilerSpan();
                } else {
                    String s = node.attr("href");
                    mLinks.add(s);
                    return new LinkSpan(s);
                }
            } else if (tag.equalsIgnoreCase("li")) {
                return new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, Color.CYAN);
            }
        }
        return null;
    }

    private static void insertNewLine(Node node, SpannableStringBuilder sb) {
        if (node instanceof Element) {
            if (((Element) node).tagName().equalsIgnoreCase("li")
                    && (!(node.childNode(0) instanceof Element)
                    || !((Element) node.childNode(0)).tagName().equalsIgnoreCase("p"))) {
                sb.append("\n");
            } else if (((Element) node).tagName().equalsIgnoreCase("code")) {
                sb.append("    ");
            } else if (((Element) node).tagName().equalsIgnoreCase("p")
                    || ((Element) node).tagName().equalsIgnoreCase("pre")) {
                sb.append("\n");
            }
        }
    }

}
