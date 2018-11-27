package com.yonyou.ny.yht.importer;

import java.io.Serializable;

/**
 * @author lipangeng, Email:lipg@outlook.com
 * @version 1.0 on 2018/11/13 5:55 PM
 * @since 1.0 Created by lipangeng on 2018/11/13 5:55 PM. Email:lipg@outlook.com.
 */
public class Result implements Serializable {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
