package org.jivesoftware.smack.compression;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JzlibInputOutputStream extends XMPPInputOutputStream {

    private static Class<?> zoClass = null;
    private static Class<?> ziClass = null;

    static {
        try {
            zoClass = Class.forName("com.jcraft.jzlib.ZOutputStream");
            ziClass = Class.forName("com.jcraft.jzlib.ZInputStream");
        } catch (ClassNotFoundException e) {
        }
    }

    public JzlibInputOutputStream() {
        compressionMethod = "zlib";
    }

    @Override
    public boolean isSupported() {
        return (zoClass != null && ziClass != null);
    }

    @Override
    public InputStream getInputStream(InputStream inputStream) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?> constructor = ziClass.getConstructor(InputStream.class);
        Object in = constructor.newInstance(inputStream);

        Method method = ziClass.getMethod("setFlushMode", Integer.TYPE);
        method.invoke(in, 2);
        return (InputStream) in;
    }

    @Override
    public OutputStream getOutputStream(OutputStream outputStream) throws SecurityException, NoSuchMethodException,
            IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?> constructor = zoClass.getConstructor(OutputStream.class, Integer.TYPE);
        Object out = constructor.newInstance(outputStream, 9);

        Method method = zoClass.getMethod("setFlushMode", Integer.TYPE);
        method.invoke(out, 2);
        return (OutputStream) out;
    }
}
