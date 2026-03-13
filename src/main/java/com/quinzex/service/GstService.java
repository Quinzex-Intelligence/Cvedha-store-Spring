package com.quinzex.service;

import com.quinzex.entity.Gst;
import com.quinzex.repository.GstRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GstService implements IGstInterface{

    private final GstRepo gstRepo;

    @Transactional
    @Override
    @PreAuthorize( "hasRole('SUPER_ADMIN')")
    public String addGst(int percentage){
       Gst gst = new Gst();
       gst.setGstPercentage(percentage);
       gstRepo.save(gst);
       return "Added GST successfully";
    }
    @Transactional
    @Override
    @PreAuthorize( "hasRole('SUPER_ADMIN')")
    public String editGst(Long gstID,int percentage) {
        Gst gst = gstRepo.findById(gstID).orElseThrow(() -> new RuntimeException("GST not found"));
        gst.setGstPercentage(percentage);

        return "GST updated successfully";
    }

    @Transactional(readOnly = true)
    @Override
    public List<Gst> findAll() {
        return gstRepo.findAll();
    }

}
