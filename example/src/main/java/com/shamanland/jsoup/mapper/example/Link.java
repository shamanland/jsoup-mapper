package com.shamanland.jsoup.mapper.example;

import com.shamanland.jsoup.mapper.JsoupAttributeValue;
import com.shamanland.jsoup.mapper.JsoupSelector;
import com.shamanland.jsoup.mapper.JsoupTextValue;

@JsoupSelector("a")
public class Link {
    private String href;
    private String text;

    public String getHref() {
        return href;
    }

    @JsoupAttributeValue("href")
    public void setHref(String href) {
        this.href = href;
    }

    public String getText() {
        return text;
    }

    @JsoupTextValue
    public void setText(String text) {
        this.text = text;
    }
}
