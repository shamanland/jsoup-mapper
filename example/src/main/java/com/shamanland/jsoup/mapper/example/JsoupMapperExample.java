package com.shamanland.jsoup.mapper.example;

import com.shamanland.jsoup.mapper.JsoupMapper;
import com.shamanland.jsoup.mapper.JsoupMapperException;

import org.jsoup.Jsoup;

public class JsoupMapperExample {
    public static void main(String[] args) throws JsoupMapperException {
        String html = "<html><body><a href='http://blog.shamanland.com/'>ShamanLand.Com</a></body></html>";

        Link[] links = JsoupMapper.getInstance().readValue(Jsoup.parse(html), Link[].class);
        for (Link link : links) {
            System.out.print(link.getText());
            System.out.print(" ==> ");
            System.out.print(link.getHref());
            System.out.println();
        }
    }
}
