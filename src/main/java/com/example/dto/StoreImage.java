package com.example.dto;

import java.util.Date;

import lombok.Data;

@Data
public class StoreImage {
    int storeimageNo;

    String filename;
    String filetype;
    long filesize;
    byte[] filedata;

    String storeId;

    Date regdate;
}
