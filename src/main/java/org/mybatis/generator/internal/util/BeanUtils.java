package org.mybatis.generator.internal.util;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Method;

import java.io.*;
import java.util.List;
import java.util.Set;

/**
 * @author by 郑东(10259829) on 2019/11/18 15:05
 */
public class BeanUtils {
    public static <T> T deepClone(T object) {
        try {
            //将对象写入流中
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(object);
            //从流中取出
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static String firstToLower(String name) {
        if (name == null || name.length() < 1) {
            return name;
        }
        return name.substring(0, 1).toLowerCase() +
                (name.length() > 1 ? name.substring(1) : "");
    }

    public static String removeLastStr(String name, String relStr) {
        if (name == null || name.length() < 1) {
            return name;
        }
        int index = name.lastIndexOf(relStr);
        String result = name;
        if(index>0) {
            result = result.substring(0, index);

        }
        return result;
    }

}
