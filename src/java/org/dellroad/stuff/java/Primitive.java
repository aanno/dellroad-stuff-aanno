
/*
 * Copyright (C) 2011 Archie L. Cobbs. All rights reserved.
 *
 * $Id$
 */

package org.dellroad.stuff.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Simple utility enumeration for working Java {@link Class} instances representing primitive types.
 *
 * <p>
 * Instances of this class represent one of the eight Java primitive types.
 * <p/>
 */
public enum Primitive {

    BOOLEAN(Boolean.TYPE, Boolean.class, 'Z') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseBoolean();
        }
    },
    BYTE(Byte.TYPE, Byte.class, 'B') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseByte();
        }
    },
    CHARACTER(Character.TYPE, Character.class, 'C') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseCharacter();
        }
    },
    SHORT(Short.TYPE, Short.class, 'S') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseShort();
        }
    },
    INTEGER(Integer.TYPE, Integer.class, 'I') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseInteger();
        }
    },
    FLOAT(Float.TYPE, Float.class, 'F') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseFloat();
        }
    },
    LONG(Long.TYPE, Long.class, 'J') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseLong();
        }
    },
    DOUBLE(Double.TYPE, Double.class, 'D') {
        public <R> R visit(PrimitiveSwitch<R> pswitch) {
            return pswitch.caseDouble();
        }
    };

    private static final HashMap<Class<?>, Primitive> CLASS_MAP = new HashMap<Class<?>, Primitive>();

    static {
        for (Primitive prim : values()) {
            CLASS_MAP.put(prim.primType, prim);
            CLASS_MAP.put(prim.wrapType, prim);
        }
    }

    private final Class<?> primType;
    private final Class<?> wrapType;
    private final char letter;

    Primitive(Class<?> primType, Class<?> wrapType, char letter) {
        this.primType = primType;
        this.wrapType = wrapType;
        this.letter = letter;
    }

    public abstract <R> R visit(PrimitiveSwitch<R> pswitch);

    /**
     * Get the short name for this primitive type, e.g., "int".
     */
    public String getName() {
        return this.primType.getName();
    }

    /**
     * Get the long name for this primitive type, e.g., "Integer".
     */
    public String getLongName() {
        return this.wrapType.getSimpleName();
    }

    /**
     * Get the single character descriptor for this primitive type, e.g., "I".
     */
    public char getLetter() {
        return this.letter;
    }

    /**
     * Get the {@link Class} object representing this primitive type, e.g., {@code Integer.TYPE}.
     */
    public Class<?> getType() {
        return this.primType;
    }

    /**
     * Get the wrapper {@link Class} object for this primitive type, e.g., {@code Integer.class}.
     */
    public Class<?> getWrapperType() {
        return this.wrapType;
    }

    /**
     * Get the wrapper class' "unwrap" method for this primitive type, e.g., {@code Integer.intValue()}.
     */
    public Method getUnwrapMethod() {
        try {
            return this.wrapType.getMethod(getName() + "Value");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a string value using the type's {@code valueOf} method.
     *
     * @param string string representation of a value
     * @throws IllegalArgumentException if {@code string} cannot be parsed
     * @throws IllegalArgumentException if {@code string} is null
     */
    public Object parseValue(String string) {

        // Handle null
        if (string == null)
            throw new IllegalArgumentException("null string");

        // Special case: Character doesn't have a valueOf(String) method
        if (this == CHARACTER) {
            if (string.length() != 1)
                throw new IllegalArgumentException("not a character: \"" + string + "\"");
            return Character.valueOf(string.charAt(0));
        }

        // Find this wrapper class' valueOf() method
        Method method;
        try {
            method = this.wrapType.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("unexpected exception", e);
        }

        // Use it to parse the value
        try {
            return method.invoke(null, string);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("unexpected exception", e);
        } catch (InvocationTargetException e) {
            Throwable nested = e.getTargetException();
            if (nested instanceof IllegalArgumentException)
                throw (IllegalArgumentException)nested;
            throw new RuntimeException("unexpected exception", nested);
        }
    }

    /**
     * Get the value corresponding to the given Java primitive or primitive wrapper type.
     *
     * @return the {@link Primitive} corresponding to {@code c}, or {@code null}
     *         if {@code c} is not a primitive or primitive wrapper type
     */
    public static Primitive get(Class<?> c) {
        return CLASS_MAP.get(c);
    }
}

