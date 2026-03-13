package com.quinzex.repository;

import com.quinzex.entity.Gst;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GstRepo extends JpaRepository<Gst, Long> {

    Optional<Gst> findTopByOrderByIdDesc();
}
