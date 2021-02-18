package com.orm.util;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.annotation.Ignore;
import com.orm.annotation.Table;
import com.orm.helper.ManifestHelper;
import com.orm.helper.NamingHelper;
import com.orm.util.classdiscovery.DexClassScanner;
import com.orm.util.classdiscovery.ResourcesClassScanner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ReflectionUtil {

    private ReflectionUtil() { }

    public static List<Field> getTableFields(Class table) {
        List<Field> fieldList = SugarConfig.getFields(table);
        if (fieldList != null) return fieldList;

        if (ManifestHelper.isDebugEnabled()) {
            Log.d("Sugar", "Fetching properties");
        }
        List<Field> typeFields = new ArrayList<>();

        getAllFields(typeFields, table);

        List<Field> toStore = new ArrayList<>();
        for (Field field : typeFields) {
            if (!field.isAnnotationPresent(Ignore.class) && !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                toStore.add(field);
            }
        }

        SugarConfig.setFields(table, toStore);
        return toStore;
    }

    private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        Collections.addAll(fields, type.getDeclaredFields());

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    public static void addFieldValueToColumn(ContentValues values, Field column, Object object,
                                             Map<Object, Long> entitiesMap) {
        column.setAccessible(true);
        Class<?> columnType = column.getType();
        try {
            String columnName = NamingHelper.toColumnName(column);
            Object columnValue = column.get(object);

            if (columnType.isAnnotationPresent(Table.class)) {
                Field field;
                try {
                    field = columnType.getDeclaredField("id");
                    field.setAccessible(true);
                    if (columnValue != null) {
                        values.put(columnName, String.valueOf(field.get(columnValue)));
                    } else {
                        values.putNull(columnName);
                    }
                } catch (NoSuchFieldException e) {
                    if (entitiesMap.containsKey(columnValue)) {
                        values.put(columnName, entitiesMap.get(columnValue));
                    }
                }
            } else if (SugarRecord.class.isAssignableFrom(columnType)) {
                values.put(columnName,
                        (columnValue != null)
                                ? String.valueOf(((SugarRecord) columnValue).getId())
                                : "0");
            } else {
                if (columnType.equals(Short.class) || columnType.equals(short.class)) {
                    values.put(columnName, (Short) columnValue);
                } else if (columnType.equals(Integer.class) || columnType.equals(int.class)) {
                    values.put(columnName, (Integer) columnValue);
                } else if (columnType.equals(Long.class) || columnType.equals(long.class)) {
                    values.put(columnName, (Long) columnValue);
                } else if (columnType.equals(Float.class) || columnType.equals(float.class)) {
                    values.put(columnName, (Float) columnValue);
                } else if (columnType.equals(Double.class) || columnType.equals(double.class)) {
                    values.put(columnName, (Double) columnValue);
                } else if (columnType.equals(Boolean.class) || columnType.equals(boolean.class)) {
                    values.put(columnName, (Boolean) columnValue);
                } else if (columnType.equals(BigDecimal.class)) {
                    try {
                        values.put(columnName, column.get(object).toString());
                    } catch (NullPointerException e) {
                        values.putNull(columnName);
                    }
                } else if (Timestamp.class.equals(columnType)) {
                    try {
                        values.put(columnName, ((Timestamp) column.get(object)).getTime());
                    } catch (NullPointerException e) {
                        values.put(columnName, (Long) null);
                    }
                } else if (Date.class.equals(columnType)) {
                    try {
                        values.put(columnName, ((Date) column.get(object)).getTime());
                    } catch (NullPointerException e) {
                        values.put(columnName, (Long) null);
                    }
                } else if (Calendar.class.equals(columnType)) {
                    try {
                        values.put(columnName, ((Calendar) column.get(object)).getTimeInMillis());
                    } catch (NullPointerException e) {
                        values.put(columnName, (Long) null);
                    }
                } else if (columnType.equals(byte[].class)) {
                    if (columnValue == null) {
                        values.put(columnName, "".getBytes());
                    } else {
                        values.put(columnName, (byte[]) columnValue);
                    }
                } else if (columnType.equals(List.class)) {
                    //ignore
                } else {
                    if (columnValue == null) {
                        values.putNull(columnName);
                    } else if (columnType.isEnum()) {
                        values.put(columnName, ((Enum) columnValue).name());
                    } else {
                        values.put(columnName, String.valueOf(columnValue));
                    }
                }
            }

        } catch (IllegalAccessException e) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.e("Sugar", e.getMessage());
            }
        }
    }

    public static void setFieldValueFromCursor(Cursor cursor, Field field, Object object) {
        field.setAccessible(true);
        try {
            Class fieldType = field.getType();
            String colName = NamingHelper.toColumnName(field);

            int columnIndex = cursor.getColumnIndex(colName);

            //TODO auto upgrade to add new columns
            if (columnIndex < 0) {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.e("SUGAR", "Invalid colName, you should upgrade database");
                }
                return;
            }

            if (cursor.isNull(columnIndex)) {
                return;
            }

            if (colName.equalsIgnoreCase("id")) {
                long cid = cursor.getLong(columnIndex);
                field.set(object, cid);
            } else if (fieldType.equals(long.class) || fieldType.equals(Long.class)) {
                field.set(object,
                        cursor.getLong(columnIndex));
            } else if (fieldType.equals(String.class)) {
                String val = cursor.getString(columnIndex);
                field.set(object, val != null && val.equals("null") ? null : val);
            } else if (fieldType.equals(double.class) || fieldType.equals(Double.class)) {
                field.set(object,
                        cursor.getDouble(columnIndex));
            } else if (fieldType.equals(boolean.class) || fieldType.equals(Boolean.class)) {
                field.set(object,
                        cursor.getString(columnIndex).equals("1"));
            } else if (fieldType.equals(int.class) || fieldType.equals(Integer.class)) {
                field.set(object,
                        cursor.getInt(columnIndex));
            } else if (fieldType.equals(float.class) || fieldType.equals(Float.class)) {
                field.set(object,
                        cursor.getFloat(columnIndex));
            } else if (fieldType.equals(short.class) || fieldType.equals(Short.class)) {
                field.set(object,
                        cursor.getShort(columnIndex));
            } else if (fieldType.equals(BigDecimal.class)) {
                String val = cursor.getString(columnIndex);
                field.set(object, val != null && val.equals("null") ? null : new BigDecimal(val));
            } else if (fieldType.equals(Timestamp.class)) {
                long l = cursor.getLong(columnIndex);
                field.set(object, new Timestamp(l));
            } else if (fieldType.equals(Date.class)) {
                long l = cursor.getLong(columnIndex);
                field.set(object, new Date(l));
            } else if (fieldType.equals(Calendar.class)) {
                long l = cursor.getLong(columnIndex);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(l);
                field.set(object, c);
            } else if (fieldType.equals(byte[].class)) {
                byte[] bytes = cursor.getBlob(columnIndex);
                if (bytes == null) {
                    field.set(object, "".getBytes());
                } else {
                    field.set(object, cursor.getBlob(columnIndex));
                }
            } else if (Enum.class.isAssignableFrom(fieldType)) {
                try {
                    Method valueOf = field.getType().getMethod("valueOf", String.class);
                    String strVal = cursor.getString(columnIndex);
                    Object enumVal = valueOf.invoke(field.getType(), strVal);
                    field.set(object, enumVal);
                } catch (Exception e) {
                    if (ManifestHelper.isDebugEnabled()) {
                        Log.e("Sugar", "Enum cannot be read from Sqlite3 database. Please check the type of field " + field.getName());
                    }
                }
            } else {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.e("Sugar", "Class cannot be read from Sqlite3 database. Please check the type of field " + field.getName() + "(" + field.getType().getName() + ")");
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            if (ManifestHelper.isDebugEnabled()) {
                Log.e("field set error", e.getMessage());
            }
        }
    }

    private static Field getDeepField(String fieldName, Class<?> type) throws NoSuchFieldException {
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class superclass = type.getSuperclass();
            if (superclass != null) {
                return getDeepField(fieldName, superclass);
            } else {
                throw e;
            }
        }
    }

    public static void setFieldValueForId(Object object, Long value) {
        try {
            Field field = getDeepField("id", object.getClass());
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Set<Class<?>> getDomainClasses() {
        String domainPackageName = ManifestHelper.getDomainPackageName();

        Set<String> allCandidateClasses = new HashSet<>();
        try {
            allCandidateClasses.addAll(new DexClassScanner().getAllFullyQualifiedClassNamesInPackage(domainPackageName));
        } catch (NullPointerException e) {
            allCandidateClasses.addAll(getAllClassesRunningWithRobolectric(domainPackageName));
        }

        return getAllClassesThatRepresentSugarTables(allCandidateClasses);
    }

    private static Set<String> getAllClassesRunningWithRobolectric(String domainPackageName) {
        ResourcesClassScanner resourcesClassScanner = new ResourcesClassScanner(Thread.currentThread().getContextClassLoader());
        return resourcesClassScanner.getAllFullyQualifiedClassNamesInPackage(domainPackageName);
    }

    private static Set<Class<?>> getAllClassesThatRepresentSugarTables(Set<String> candidateQualifiedClassNames) {
        Set<Class<?>> tableClasses = new HashSet<>();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        for (String candidateClassName : candidateQualifiedClassNames) {
            Class<?> discoveredClass = null;
            try {
                discoveredClass = Class.forName(candidateClassName, true, contextClassLoader);
            } catch (Throwable e) {
                if (ManifestHelper.isDebugEnabled()) {
                    Log.e("Sugar", "Unable to load class: " + candidateClassName, e);
                }
            }

            if (discoveredClass != null &&
                    isSugarRecordDescendantOrIsAnnotated(discoveredClass) &&
                    !Modifier.isAbstract(discoveredClass.getModifiers())) {

                if (ManifestHelper.isDebugEnabled()) {
                    Log.i("Sugar", "Discovered domain class : " + candidateClassName);
                }
                tableClasses.add(discoveredClass);
            }
        }

        return tableClasses;
    }

    private static boolean isSugarRecordDescendantOrIsAnnotated(Class<?> discoveredClass) {
        return (SugarRecord.class.isAssignableFrom(discoveredClass) &&
                !SugarRecord.class.equals(discoveredClass)) ||
                discoveredClass.isAnnotationPresent(Table.class);
    }
}
