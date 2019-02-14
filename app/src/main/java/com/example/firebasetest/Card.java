package com.example.firebasetest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Card {
    public List<String> card_list;
    public Stack<String> card_deck;

    public Card() {
        card_list = Arrays.asList("R0","R00","R000","R2","R3","R4","R5","R6","R7","R8","R9","R10",
                "G0","G00","G000","G2","G3","G4","G5","G6","G7","G8","G9","G10",
                "W0","W00","W000","W2","W3","W4","W5","W6","W7","W8","W9","W10",
                "B0","B00","B000","B2","B3","B4","B5","B6","B7","B8","B9","B10",
                "Y0","Y00","Y000","Y2","Y3","Y4","Y5","Y6","Y7","Y8","Y9","Y10");
        Collections.shuffle(card_list);
        card_deck = new Stack<String>();
        for(int i = 0 ; i < 60; i++) {
            card_deck.push(card_list.get(i));
        }
    }
}
