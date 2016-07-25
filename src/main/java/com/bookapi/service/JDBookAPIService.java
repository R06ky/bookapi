package bookapi.service;

import bookapi.Application;
import bookapi.Config;
import bookapi.domain.Book;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by rockyren on 4/1/16.
 */
public class JDBookAPIService extends AbstractAPIService implements BookAPIService {

    final static String SEARCH_BY_TITLE = "http://search.jd.com/bookadvsearch?keyword=%s&enc=utf-8";
    final static String SEARCH_BY_ISBN = "http://search.jd.com/bookadvsearch?isbn=%s";
    final static String JD_DESC_URL = "http://d.3.cn/desc/%s";
    Config config;

    public JDBookAPIService(Map<String, String> requestParams, Config config) {
        super(requestParams);
        this.config = config;
    }

    private void setUrl() {
        String sUrl = null;
        String bookName = this.getRequestParams().get("name");
        String isbn = this.getRequestParams().get("isbn");

        if (StringUtils.isEmpty(sUrl)) {
            if (StringUtils.isEmpty(isbn)) {
                bookName = bookName.replace(" ", "+");
                sUrl = String.format(SEARCH_BY_TITLE, bookName);
            } else {
                sUrl = String.format(SEARCH_BY_ISBN, isbn);
            }
        }

        setUrl(getUrlBySearch(sUrl));
    }

    public String getUrlBySearch(String url) {
        Document document = getDocument(url);
        Element JDGoodsList = document.getElementById("J_goodsList");

        if (JDGoodsList == null) {
            return StringUtils.EMPTY;
        }

        Element bookUrl = JDGoodsList.select("a").first();
        for (Element element : JDGoodsList.select("li")) {
            if (element.outerHtml().contains("京东自营")) {
                bookUrl = element.select("a").first();
                break;
            }
        }
        if (bookUrl == null) {
            return StringUtils.EMPTY;
        }
        return "http:" + bookUrl.attr("href");
    }

    @Override
    public boolean buildBook() {
        setUrl();
        Document document = getDocument(getUrl());
        setBook(document2Book(document));
        return validated();
    }

    private Book document2Book(Document doc) {
        if (doc == null) {
            return Book.DEFAULT_BOOK;
        }
        Book book = new Book();
        Element name = doc.getElementById("name");
        book.setTitle(name == null ? StringUtils.EMPTY : name.select("h1").text());
        Elements proContent = doc.getElementsByClass("p-parameter");
        if (proContent != null) {
            Elements elements = proContent.select("ul").select("li");
            Iterator iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element element = (Element) iterator.next();
                String content = element.select("li").text();
                if (StringUtils.contains(content,"出版社")) {
                    book.setPublisher(element.select("li").attr("title"));
                }
                if (StringUtils.contains(content,"出版时间")) {
                    book.setPubdate(StringUtils.split(content, DDBookAPIService.DD_COLON).length > 0 ? StringUtils.split(content, DDBookAPIService.DD_COLON)[1] : StringUtils.EMPTY);
                }
                if (StringUtils.contains(content,"ISBN")) {
                    String isbn = StringUtils.split(content, DDBookAPIService.DD_COLON).length > 0 ? StringUtils.split(content, DDBookAPIService.DD_COLON)[1] : StringUtils.EMPTY;
                    if (isbn.length() > 10) {
                        book.setIsbn13(isbn);
                    } else {
                        book.setIsbn10(isbn);
                    }
                }
            }
        }
        ArrayList images = new ArrayList();

        Element imageEles = doc.getElementById("preview");
        if (imageEles != null) {
            for (Element imageEle : imageEles.select("img")) {
                String image = imageEle == null ? StringUtils.EMPTY : "http:" + imageEle.attr("src");
                String[] imagePre = image.split("jfs");
                if (config != null
                        && !StringUtils.isEmpty(config.getJdimgprefixfrom())
                        &&  (imagePre.length > 0 && imagePre[0].contains(config.getJdimgprefixfrom()))
                        && !StringUtils.isEmpty(config.getJdimgprefixto())) {
                    image = StringUtils.replaceOnce(image, config.getJdimgprefixfrom(),config.getJdimgprefixto());
                }
                images.add(image);
            }
        }
        book.setImages(images);

        ArrayList authorList = new ArrayList<String>();
        ArrayList translatorList = new ArrayList<String>();
        Element authorEle = doc.getElementById("p-author");
        if (authorEle != null) {
            for(Element a : authorEle.select("a")) {
                authorList.add(a.text());
            }
        }
        book.setAuthor(authorList);
        book.setTranslator(translatorList);
        String jdDesc = getJDDesc();
        if (!StringUtils.isEmpty(jdDesc)) {
            Document jdDescDoc = Jsoup.parse(jdDesc);
            Iterator<Element> iterator = jdDescDoc.select("h3").iterator();
            while (iterator.hasNext()) {
                Element element = iterator.next();
                String str = element.parent().parent().getElementsByClass("book-detail-content").html();
                if (config != null && !StringUtils.isEmpty(config.getJdemptydesc())
                        && StringUtils.containsIgnoreCase(config.getJdemptydesc(), str)) {
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("作者简介", element.text())) {
                    book.setAuthorIntro(str);
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("内容简介", element.text())) {
                    book.setSummary(str);
                    continue;
                }
                if (StringUtils.equalsIgnoreCase("目录", element.text())) {
                    book.setCatalog(str);
                    continue;
                }
            }
        }


        Elements as = doc.getElementsByClass("breadcrumb").select("a");
        StringBuilder categroy = new StringBuilder();
        for (int i = 0; i < as.size() - 1; i ++) {
            categroy.append(as.get(i).text());
            if (i < as.size() - 2) {
                categroy.append("|");
            }
        }
        book.setCategroy(categroy.toString());

        return book;
    }

    private String getJDDesc() {
        String desc = null;
        String jdID = getJDID();
        if (StringUtils.isEmpty(jdID)) {
            return desc;
        }
        String descUrl = String.format(JD_DESC_URL,jdID);
        try {
            String html = getJsoupHTML(descUrl, config != null && config.getJddestimeout() > 0 ? config.getJddestimeout() : AbstractAPIService.DEFAULT_TIME_OUT);
            if(!StringUtils.isEmpty(html)) {
                String jsonString = StringUtils.substring(html, StringUtils.indexOf(html, "(") + 1, StringUtils.lastIndexOf(html, ")"));
                JSONObject jsonObject = new JSONObject(jsonString);
                desc = jsonObject.getString("content");
            }
        } catch (Exception e) {
            Application.logger.error(descUrl, e);
        }
        return desc;
    }

    private String getJDID() {
        if (StringUtils.isEmpty(getUrl())) {
            return StringUtils.EMPTY;
        }
        String url = getUrl();
        String jdID = StringUtils.substring(url, StringUtils.lastIndexOf(url,"/")+1, StringUtils.lastIndexOf(url, "."));
        return jdID;
    }

    @Override
    public boolean validated() {
        boolean validation = getBook() != null && super.validated() && getBook().getImages() != null;
        if (!validation) {
            Application.logger.warn("JD cannot get the book, URL --> " + getUrl());
        }
        return validation;
    }
}
