package com.example.zenword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Interface constants and globals
    private static final int MAX_LETTERS = 7;
    private static final int MAX_WORDS = 5;
    private final int X_GAP = 10;
    private final int Y_GAP = 20;
    private int widthDisplay;
    private int usableWidth;
    private int wordLength = 6;
    private int letterSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createInterface();
    }

    private void createInterface () {
        // Get display size
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        widthDisplay = metrics.widthPixels;

        // Calculate letter and margin sizes
        View topGuide = findViewById(R.id.lettersTopGuide);
        View bottomGuide = findViewById(R.id.lettersBottomGuide);
        int topGuidePosition = ((ConstraintLayout.LayoutParams) topGuide.getLayoutParams()).guideBegin;
        int bottomGuidePosition = ((ConstraintLayout.LayoutParams) bottomGuide.getLayoutParams()).guideBegin;
        int usableHeight = bottomGuidePosition - topGuidePosition;

        usableWidth = widthDisplay - 100;
        letterSize = (usableWidth - (wordLength - 1) * X_GAP) / wordLength;
        while (letterSize * MAX_WORDS + Y_GAP * (MAX_WORDS - 1) > usableHeight) {
            letterSize -= 5;
        }

        // Create letter rows
        TextView[] prevRow = {};
        int letters = 3;
        for (int i = 0; i < MAX_WORDS; i++) {
            int prevId = i == 0 ? R.id.lettersTopGuide : prevRow[0].getId();
            prevRow = createTextViewsRow(prevId, letters);
            if (wordLength - letters == MAX_WORDS - (i + 1)) letters++;
        }
    }

    public TextView[] createTextViewsRow(int guide, int letters) {
        // Define TextView's colors
        int bgColor = Color.rgb(30, 50, 150);
        int fgColor = Color.rgb(255, 255, 255);

        // Calculate margin size
        int marginSize = usableWidth - (letterSize * letters + X_GAP * (letters - 1));

        // Generate ids
        int[] ids = new int[letters];
        for (int i = 0; i < letters; i++) {
            ids[i] = View.generateViewId();
        }

        ConstraintLayout layout = findViewById(R.id.layout);

        TextView[] row = new TextView[letters];

        for (int i = 0; i < letters; i++) {
            row[i] = new TextView(this);
            row[i].setId(ids[i]);
            row[i].setText("");
            row[i].setBackground(createRoundedBackground(bgColor, 20));
            row[i].setTextColor(fgColor);


            layout.addView(row[i]);
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(layout); // Clone existing constraints

        for (int i = 0; i < letters; i++) {
            // Top constraint
            constraintSet.connect(ids[i], ConstraintSet.TOP, guide, ConstraintSet.BOTTOM, Y_GAP);
            // Left constraints
            if (i == 0) {
                constraintSet.connect(ids[i], ConstraintSet.START, R.id.leftMargin, ConstraintSet.END, marginSize);
            } else {
                constraintSet.connect(ids[i], ConstraintSet.START, row[i - 1].getId(), ConstraintSet.END, X_GAP);
            }
            // Right constraint
            if (i == letters - 1) {
                constraintSet.connect(ids[i], ConstraintSet.END, R.id.rightMargin, ConstraintSet.START, marginSize);
            } else {
                constraintSet.connect(ids[i], ConstraintSet.END, row[i + 1].getId(), ConstraintSet.START, 0);
            }
            // Set size
            constraintSet.constrainWidth(ids[i], letterSize);
            constraintSet.constrainHeight(ids[i], letterSize);
        }

        constraintSet.applyTo(layout); // Apply all constraints at once

        return row;
    }

    private Drawable createRoundedBackground(int bgColor, float cornerRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bgColor);
        drawable.setCornerRadius(cornerRadius);
        return drawable;
    }

    public boolean isSolutionWord(String word1, String word2) {
        // TODO use our own map
        HashMap<Character, Integer> catalogue = new HashMap<>();

        for (char c : word1.toCharArray()) {
            if (catalogue.containsKey(c)) {
                catalogue.put(c, catalogue.get(c) + 1);
            } else {
                catalogue.put(c, 1);
            }
        }

        for (char c : word2.toCharArray()) {
            if (!catalogue.containsKey(c)) return false;

            int count = catalogue.get(c);
            if (count == 0) return false;

            catalogue.put(c, count - 1);
        }

        return true;
    }
}