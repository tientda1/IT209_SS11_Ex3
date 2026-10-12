package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {

    @Test
    void addReturnsSum() {
        assertEquals(5, App.add(2, 3));
    }

    @Test
    void multiplyReturnsProduct() {
        assertEquals(20, App.multiply(4, 5));
    }
}
