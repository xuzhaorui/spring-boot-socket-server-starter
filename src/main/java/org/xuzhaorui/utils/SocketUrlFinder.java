package org.xuzhaorui.utils;




import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SocketUrlFinder {

    // 存储字段的路径信息
    public static class FieldPath {
        private List<String> path;

        public List<String> getPath() {
            return path;
        }

        public void setPath(List<String> path) {
            this.path = path;
        }

        public FieldPath() {
            this.path = new ArrayList<>();
        }

        public void add(String fieldName) {
            path.add(fieldName);
        }

        @Override
        public String toString() {
            return String.join(" -> ", path);
        }
    }
    public static List<String> fieldLookingForMarkupAnnotationsCanBeEmbedded(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        List<String> socketUrlPaths = new ArrayList<>();
        FieldPath currentPath = new FieldPath();
        findSocketUrlInFieldNames(clazz, currentPath, socketUrlPaths,annotationClass);
        return socketUrlPaths; // 返回收集到的路径
    }
    public static String findAuthenticationFailureMethodName(Class<?> clazz,Class<? extends Annotation> annotationClass){
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(annotationClass))
                return method.getName();

        }
        return null;
    }

    private static void findSocketUrlInFieldNames(Class<?> clazz, FieldPath currentPath, List<String> socketUrlPaths,Class<? extends Annotation> annotationClass) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (!socketUrlPaths.isEmpty()) break;
            // 排除自带的系统字段属性
            if (isUserDefinedField(field)) {
                // 检查字段上是否有@SocketUrl注解
                if (field.isAnnotationPresent(annotationClass)) {
                    // 添加字段到路径并记录完整路径
                    currentPath.add(field.getName());
                    // 将路径元素拷贝到 socketUrlPaths
                    socketUrlPaths.addAll(currentPath.getPath()); // 命中路径
                    currentPath.path.remove(currentPath.path.size() - 1); // 回溯
                } else {
                    // 判断字段是否为自定义声明的内嵌对象
                    Class<?> fieldType = field.getType();
                    if (isUserDefinedClass(fieldType)) {
                        // 添加当前字段名到路径
                        currentPath.add(field.getName());
                        // 递归查找嵌套对象
                        findSocketUrlInFieldNames(fieldType, currentPath, socketUrlPaths,annotationClass);
                        // 移除当前字段名，进行回溯
                        currentPath.path.remove(currentPath.path.size() - 1);
                    }
                }
            }
        }
    }

    // 判断字段是否是用户自定义的字段
    private static boolean isUserDefinedField(Field field) {
        Class<?> declaringClass = field.getDeclaringClass();
        return !declaringClass.getName().startsWith("java.") && !declaringClass.getName().startsWith("javax.");
    }

    // 判断类是否为用户自定义类
    private static boolean isUserDefinedClass(Class<?> clazz) {
        return !clazz.getName().startsWith("java.") && !clazz.getName().startsWith("javax.");
    }
}
