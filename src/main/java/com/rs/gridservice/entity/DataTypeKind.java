package com.rs.gridservice.entity;

/**
 * Bir DataType'in sistem tarafindan anlasilan sabit turu. "name" admin'in
 * verdigi goruntulenen ad (degistirilebilir), "kind" ise LOOKUP davranisini
 * (ve ileride baska tip-ozel render kurallarini) tetikleyen sabit anahtardir.
 */
public enum DataTypeKind {
    TEXT,
    NUMBER,
    DATE,
    BOOLEAN,
    LOOKUP
}
