package com.shamanland.jsoup.mapper;

import java.io.IOException;

public class JsoupMapperException extends IOException {
    static final long serialVersionUID = 1367894521678452367L;

    public JsoupMapperException() {
        super();
    }

    public JsoupMapperException(String detailMessage) {
        super(detailMessage);
    }

    public JsoupMapperException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsoupMapperException(Throwable cause) {
        super(cause);
    }
}
