package org.example.productservice.util;

import java.io.Serializable;

public class NullValue implements Serializable {
    public static final NullValue INSTANCE = new NullValue();
    private NullValue() {}
}
