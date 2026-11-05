package com.knot.provisioning.application;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private final CharArrayWriter writer;
    private static final Logger log = LogManager.getLogger(ResponseWrapper.class);
    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        writer = new CharArrayWriter();
    }

    @Override
    public PrintWriter getWriter() {
        return new PrintWriter(writer);
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return new ServletOutputStream() {
            @Override
            public void write(int b) {
                writer.write(b);
            }

			@Override
			public boolean isReady() {

				return true;
			}

			@Override
			public void setWriteListener(WriteListener listener) {
			
				log.info("no implementation");
			}
        };
    }

    public String getResponseBody() {
        return writer.toString();
    }

}
