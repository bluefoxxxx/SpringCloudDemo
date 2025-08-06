package org.example.userservice.util;

import java.io.Serializable;

public class NullValue implements Serializable {
    public static final NullValue INSTANCE = new NullValue();
    private NullValue() {}
}
