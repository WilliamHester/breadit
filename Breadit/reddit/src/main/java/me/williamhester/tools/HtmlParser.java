package me.williamhester.tools;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.ui.text.LinkSpan;
import me.williamhester.ui.text.SpoilerSpan;

/**
 * This class parses unescaped HTML into a Spannable String. It also produces a list of links that
 * were found during the creation of the Spannable String.
 *
 * Created by William on 6/15/14.
 */
public class HtmlParser {

    private final List<Link> mLinks = new ArrayList<>();
    private SpannableStringBuilder mSpannableStringBuilder;

    public HtmlParser(String html) {
        mSpannableStringBuilder = parseHtml(html);
    }

    public @NonNull SpannableStringBuilder getSpannableString() {
        return mSpannableStringBuilder;
    }

    public @NonNull List<Link> getLinks() {
        return mLinks;
    }

    private SpannableStringBuilder parseHtml(String html) {
        if (html != null) {
            Document document = Jsoup.parse(html);
            SpannableStringBuilder sb = generateString(document, new SpannableStringBuilder());
            while (sb.length() > 0 && (sb.charAt(0) == '\n' || sb.charAt(0) == ' ')) {
                sb.delete(0, 1);
            }
            return sb;
        }
        return new SpannableStringBuilder().append("");
    }

    private SpannableStringBuilder generateString(Node node, SpannableStringBuilder ssb) {
        if (node instanceof TextNode) {
            return ssb.append(((TextNode) node).text());
        }

        insertNewLine(node, ssb);

        List<Node> children = node.childNodes();
        for (Node n : children) {
            ssb.append(generateString(n, new SpannableStringBuilder()));
        }

        if (ssb.length() > 0) {
            ssb.setSpan(getSpanFromTag(node, ssb), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }

    private Object getSpanFromTag(Node node, SpannableStringBuilder ssb) {
        if (node instanceof Element) {
            String tag = ((Element) node).tag().getName().toLowerCase();
            switch (tag) {
                case "code":
                    return new TypefaceSpan("monospace");
                case "del":
                    return new StrikethroughSpan();
                case "strong":
                    return new StyleSpan(Typeface.BOLD);
                case "em":
                    return new StyleSpan(Typeface.ITALIC);
                case "blockquote":
                    return new QuoteSpan(Color.rgb(246, 128, 38));
                case "sup":
                    return new SuperscriptSpan();
                case "a":
                    String url = node.attr("href");
                    if (url.equals("/spoiler")) {
                        return new SpoilerSpan();
                    }
                    if (url.equals("/s") || url.equals("#s")) {
                        String spoiler = node.attr("title");
                        ssb.append(' ');
                        ssb.append(spoiler);
                        ssb.setSpan(new SpoilerSpan(), ssb.length() - spoiler.length(), ssb.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        return new UnderlineSpan();
                    }
                    mLinks.add(new Link(ssb.toString(), url));
                    return new LinkSpan(url);
                case "li":
                    return new BulletSpan(BulletSpan.STANDARD_GAP_WIDTH, 0xfff68026);
                default:
//                    Log.e("HtmlParser", "Unhandled tag: " + tag);
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
            } else if (((Element) node).tagName().equalsIgnoreCase("p")
                    || ((Element) node).tagName().equalsIgnoreCase("pre")) {
                sb.append("\n");
            }
        }
    }

    public static class Link {
        private String mTitle;
        private String mLink;

        public Link(String title, String link) {
            mTitle = title;
            mLink = link;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getLink() {
            return mLink;
        }
    }

}
