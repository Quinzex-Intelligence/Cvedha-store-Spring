package com.quinzex.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "E-GST")
public class Gst {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int gstPercentage;
}
