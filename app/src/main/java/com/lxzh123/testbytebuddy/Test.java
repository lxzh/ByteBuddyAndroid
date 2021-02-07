package com.lxzh123.testbytebuddy;

public class Test {
    private String innerValue;
    private Object innerObject;

    public String getInnerValue() {
        return innerValue;
    }

    public void setInnerValue(String innerValue) {
        this.innerValue = innerValue;
    }

    public Object getInnerObject() {
        return innerObject;
    }

    public void setInnerObject(Object innerObject) {
        this.innerObject = innerObject;
    }

    @Override
    public String toString() {
        return "Test{" +
                "innerValue='" + innerValue + '\'' +
                ", innerObject=" + innerObject +
                '}';
    }
}
