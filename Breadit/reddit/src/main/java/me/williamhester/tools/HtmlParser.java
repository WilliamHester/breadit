package me.williamhester.tools;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Stack;

/**
 * Created by William on 6/15/14.
 */
public class HtmlParser {

    public static SpannableStringBuilder parseHtml(String html) {
        Document document = Jsoup.parse(html);
        return generateString(document);
    }

    public static SpannableStringBuilder generateString(Node node) {
        if (node instanceof TextNode) {
            return new SpannableStringBuilder(((TextNode) node).text());
        }
        SpannableStringBuilder sb = new SpannableStringBuilder();
        List<Node> children = node.childNodes();
        for (Node n : children) {
            sb.append(generateString(n)).setSpan(getSpanFromTag(node), 0, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return sb;
    }

    public static Object getSpanFromTag(Node node) {
        if (node instanceof Element) {
            String tag = ((Element) node).tag().getName();
            if (tag.equalsIgnoreCase("p")) {

            } else if (tag.equalsIgnoreCase("pre")) {

            } else if (tag.equalsIgnoreCase("del")) {

            } else if (tag.equalsIgnoreCase("strong")) {
                return new StyleSpan(Typeface.BOLD);
            } else if (tag.equalsIgnoreCase("em")) {
                return new StyleSpan(Typeface.ITALIC);
            } else if (tag.equalsIgnoreCase("blockquote")) {

            } else if (tag.equalsIgnoreCase("a")) {
                Log.d("HtmlParser", node.attr("href"));

            } else if (tag.equalsIgnoreCase("ol")) {

            } else if (tag.equalsIgnoreCase("li")) {

            }
        }
        return null;
    }

}
