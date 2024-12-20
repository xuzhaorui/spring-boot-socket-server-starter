package org.xuzhaorui.utils;


import org.xuzhaorui.exception.pre.ParsingException;


import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;

public class AnnotationUtils {



    // 开始查找
    public static <R> R findValue(Object obj, Class<? extends Annotation> annotationClass,List<String> paths) throws ParsingException {
        Object currentObject = obj;

            for (String fieldName : paths) {
                if (currentObject == null) {
                    throw new ParsingException(annotationClass.getName()  + "解析失败，请检查注解是否正确");
                }

                try {
                    Field field = currentObject.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true); // 允许访问私有字段
                    currentObject = field.get(currentObject); // 获取当前字段的值

                    // 如果到达最后一个字段，且字段上有annotationClass注解，返回其值
                    if (field.isAnnotationPresent(annotationClass)) {
                        return (R) currentObject; // 返回字段的值
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new ParsingException(annotationClass.getName()  + "解析失败" + e.getMessage());
                }

        }

        throw new ParsingException("url解析失败，请检查注解是否正确");

    }



//    // 查找字段及其嵌套对象
//    private static String findSocketUrlInFields(Class<?> clazz, Object obj) {
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            // 排除自带的系统字段属性
//            if (isUserDefinedField(field)) {
//                if (field.isAnnotationPresent(SocketUrl.class)) {
//                    // 如果字段上有@SocketUrl注解，获取其值
//                    field.setAccessible(true);
//                    try {
//                        String url = (String) field.get(obj);
//                        System.out.println("Found Socket URL: " + url);
//                        return url;
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    // 递归查找嵌套对象
//                    try {
//                        field.setAccessible(true);
//                        Object nestedObject = field.get(obj);
//                        if (nestedObject != null) {
//                            // 递归调用，检查嵌套对象的字段
//                            String nestedUrl = findSocketUrlInFields(nestedObject.getClass(), nestedObject);
//                            if (nestedUrl != null) {
//                                return nestedUrl; // 如果找到，返回结果
//                            }
//                        }
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//        return null;
//    }


    // 判断类是否为用户自定义类
    private static boolean isUserDefinedClass(Class<?> clazz) {
        return !clazz.getName().startsWith("java.") && !clazz.getName().startsWith("javax.");
    }

    // 判断字段是否是用户自定义的字段
    private static boolean isUserDefinedField(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        return !declaringClass.getName().startsWith("java.") && !declaringClass.getName().startsWith("javax.");
    }



//    // 主方法，开始查找
//    public static String findSocketUrl1(Object obj) {
//
//        if (obj == null) {
//            return null;
//        }
//        Class<?> clazz = obj.getClass();
//        return findSocketUrlInFields(clazz, obj);
//    }
//    public static String findSocketUrlInFieldNames(Class<?> clazz){
//        Field[] fields = clazz.getDeclaredFields();
//        for (Field field : fields) {
//            // 排除自带的系统字段属性
//            if (isUserDefinedField(field)) {
//                if (field.isAnnotationPresent(SocketUrl.class)) {
//                    return field.getName();// 如果字段上有@SocketUrl注解，获取其值
//
//                } else {
//                    //判断是否为自定义声名内嵌对象
//                    Class<?> fieldType = field.getType();
//                    if (isUserDefinedClass(fieldType)) {
//                        // 递归查找嵌套对象
//                        String socketUrlInFieldNames = findSocketUrlInFieldNames(fieldType);
//                        if (socketUrlInFieldNames != null) {
//                            return socketUrlInFieldNames; // 如果找到，返回结果
//                        }
//                    }
//
//                }
//            }
//        }
//        return null;
//    }


    public static <R> R findMarkAnnotationValue(Object obj, Class<? extends Annotation> annotationClass,List<String> paths) throws ParsingException {
        Object currentObject = obj;
        for (String fieldName : paths) {
            if (currentObject == null) {
                throw new ParsingException(annotationClass.getName()  + "解析失败，请检查注解是否正确");
            }

            try {
                Field field = currentObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true); // 允许访问私有字段
                currentObject = field.get(currentObject); // 获取当前字段的值

                // 如果到达最后一个字段，且字段上有annotationClass注解，返回其值
                if (field.isAnnotationPresent(annotationClass)) {
                    return (R) currentObject; // 返回字段的值
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ParsingException(annotationClass.getName()  + "解析失败" + e.getMessage());
            }
        }

        throw new ParsingException("url解析失败，请检查注解是否正确");

    }


    public static void modifyMarkAnnotationValue(Object obj, Class<? extends Annotation> annotationClass, List<String> paths, Object newValue) throws ParsingException {
        Object currentObject = obj;
        Field targetField = null;

        for (int i = 0; i < paths.size(); i++) {
            String fieldName = paths.get(i);

            if (currentObject == null) {
                throw new ParsingException(annotationClass.getName() + "解析失败，当前对象为null，请检查路径");
            }

            try {
                // 获取当前字段
                Field field = currentObject.getClass().getDeclaredField(fieldName);
                field.setAccessible(true); // 允许访问私有字段

                // 如果到了最后一个字段，且字段上有annotationClass注解，准备修改值
                if (i == paths.size() - 1) {
                    if (!field.isAnnotationPresent(annotationClass)) {
                        throw new ParsingException("字段" + fieldName + "未标记注解" + annotationClass.getName());
                    }
                    targetField = field; // 标记目标字段
                } else {
                    // 继续深入，获取字段的值作为下一层对象
                    currentObject = field.get(currentObject);
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ParsingException(annotationClass.getName() + "解析失败，字段" + fieldName + "访问出错：" + e.getMessage());
            }
        }

        // 确保我们找到了目标字段并进行赋值
        if (targetField != null) {
            try {
                targetField.set(currentObject, newValue); // 修改目标字段的值
            } catch (IllegalAccessException e) {
                throw new ParsingException("无法设置字段值：" + e.getMessage());
            }
        }
    }

}
