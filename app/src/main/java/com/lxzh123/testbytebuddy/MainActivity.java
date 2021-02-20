package com.lxzh123.testbytebuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.android.AndroidClassLoadingStrategy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "ByteBuddy";
    private Test test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String pkgName = getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = getPackageManager().getPackageInfo(pkgName, PackageManager.GET_ACTIVITIES);
            Log.i(TAG, "packageName test before hook:" + pkgInfo.packageName);
        } catch (Exception e) {

        }
        Object objectOriginal = new String("Hahaha");

        test = new Test();
        test.setInnerObject(objectOriginal);
        Log.d(TAG, "test before hook=" + test);

        Context context = this;
        ClassLoadingStrategy strategy = new AndroidClassLoadingStrategy.Wrapping(context.getDir(
                "generated",
                Context.MODE_PRIVATE));

        Class<?> dynamicType = new ByteBuddy(ClassFileVersion.JAVA_V7)
                .subclass(Object.class)
                .method(ElementMatchers.named("toString"))
                .intercept(FixedValue.value("Hello World!"))
                .make()
                .load(getClass().getClassLoader(), strategy)
                .getLoaded();
        Object newObject = null;
        try {
            newObject = dynamicType.newInstance();
//            Log.d(TAG, "type toString()=" + dynamicType.newInstance().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Field field = Test.class.getDeclaredField("innerObject");
            field.setAccessible(true);
            field.set(test, newObject);
        } catch (Exception e) {

        }
        Log.d(TAG, "test after hook=" + test);



        Object originBaseTmp = null;
        Class<?> ContextWrapperClass = null;
        Class<?> ContextImplClass = null;
        Field mBase = null;
        try{
            ContextWrapperClass = Class.forName("android.content.ContextWrapper");
            ContextImplClass = Class.forName("android.app.ContextImpl");
            mBase = ContextWrapperClass.getDeclaredField("mBase");
            mBase.setAccessible(true);
            originBaseTmp = mBase.get(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final Object originBase = originBaseTmp;
        Class<?> dynamicType1 = new ByteBuddy()
                .subclass(ContextWrapperClass)
                .method(ElementMatchers.named("getPackageName"))
//                .intercept(FixedValue.value("Hello World!"))
                .intercept(MethodDelegation.to(Interceptor.class))
                .make()
                .load(getClass().getClassLoader(), strategy)
                .getLoaded();
        Object newObject1 = null;
        try {
            Constructor constructor = dynamicType1.getConstructor(Context.class);
            newObject1 = constructor.newInstance(originBase);
//            newObject1 = dynamicType1.newInstance();
            mBase.set(this, newObject1);
//            Log.d(TAG, "type toString()=" + dynamicType.newInstance().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "getPackageName test:" + this.getPackageName());
        Log.i(TAG, "getPackageCodePath test:" + this.getPackageCodePath());
        Log.i(TAG, "packageName test:" + pkgInfo.packageName);

    }

    public static class Interceptor {
        /**
         * The interception method to be applied.
         *
         * @param zuper A proxy to call the super method to validate the functioning og creating an auxiliary type.
         * @return The value to be returned by the instrumented {@link Object#toString()} method.
         * @throws Exception If an exception occurs.
         */
        public static String intercept(@SuperCall Callable<String> zuper) throws Exception {
            printStackTrace();
            String toString = zuper.call();
            if (toString.equals("com.lxzh123.testbytebuddy")) {
                return "HelloWorld";
            }
            return "Hahaha";
        }
    }

    public static void printStackTrace() {
//        printStackTrace(System.err);
        try {
            StringBuilder builder = new StringBuilder();
            builder.append(TAG);
            builder.append("\n");
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for (StackTraceElement traceElement : trace) {
                builder.append("\tat " + traceElement);
                builder.append("\n");
            }
            Log.i(TAG, builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}