package com.elapse.easyweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by YF_lala on 2018/5/16.
 */
@Deprecated
public class Province extends DataSupport {
    private int id;
    private String provinceName;
    private int provinceCode;

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

}
