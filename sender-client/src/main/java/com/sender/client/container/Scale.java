package com.sender.client.container;

/**
 * @author haibo Date: 17-9-7 Time: 下午8:33
 */
public enum Scale {

    DECIMALISM(10, "10进制"), HEXADECIMAL(16, "16进制");

    int code;

    String desc;

    Scale(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}