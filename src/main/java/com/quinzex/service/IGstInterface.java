package com.quinzex.service;

import com.quinzex.entity.Gst;

import java.util.List;

public interface IGstInterface {
    String addGst(int percentage);

    String editGst(Long gstID, int percentage);

    List<Gst> findAll();
}

