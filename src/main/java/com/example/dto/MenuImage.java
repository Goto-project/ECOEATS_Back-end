package com.example.dto;

import java.util.Date;

import lombok.Data;

@Data
public class MenuImage {
    int menuimageNo;  

    String filename; 
    String filetype;  
    long filesize;    
    byte[] filedata; 

    int menuNo;    

    Date regdate;     
}

