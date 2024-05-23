package com.example.zenword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Interface i = new Interface(getApplicationContext(), 6);
        i.showWord("wrd", 0);
        i.showMessage("Hello message", true);
    }

    private boolean isSolutionWord(String word1, String word2) {
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

    private class Interface {

        private static final int MAX_WORDS = 5;
        private final int X_GAP = 10;
        private final int Y_GAP = 20;
        private final int wordLength;
        private final TextView[][] textViews;
        private final Context context;
        private final int bgColor;
        private int usableWidth;
        private int letterSize;

        public Interface(Context context, int wordLength) {
            this.context = context;
            this.wordLength = wordLength;

            textViews = new TextView[MAX_WORDS][wordLength];

            calculateSizes();

            // Randomize color (not light colors)
            Random random = new Random();
            int red = random.nextInt(150);
            int green = random.nextInt(150);
            int blue = random.nextInt(150);
            bgColor = Color.rgb(red, green, blue);

            // Set circle color
            ImageView imageView = findViewById(R.id.letterCircle);
            Drawable backgroundDrawable = imageView.getBackground();

            if (backgroundDrawable instanceof GradientDrawable) {
                int[] colors = {bgColor, Color.rgb(0, 0, 0)};
                ((GradientDrawable) backgroundDrawable).setColors(colors);
                imageView.setBackground(backgroundDrawable);
            }

            // Create letter rows
            int letters = 3;
            for (int i = 0; i < MAX_WORDS; i++) {
                int prevId = i == 0 ? R.id.lettersTopGuide : textViews[i - 1][0].getId();
                textViews[i] = createTextViewsRow(prevId, letters);
                if (wordLength - letters == MAX_WORDS - (i + 1)) letters++;
            }
        }

        private void calculateSizes() {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int widthDisplay = metrics.widthPixels;

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
        }

        private TextView[] createTextViewsRow(int guide, int letters) {
            // Define TextView's colors
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
                row[i] = new TextView(context);
                row[i].setId(ids[i]);
                row[i].setText("");
                row[i].setTextSize(35);
                row[i].setBackground(createRoundedBackground(bgColor, 20));
                row[i].setTextColor(fgColor);
                row[i].setGravity(Gravity.CENTER);
                row[i].setTypeface(row[i].getTypeface(), Typeface.BOLD);


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

        public void showWord(String s, int position) {
            final String ERROR_MSG = "The word doesn't fit in the position";
            if (position >= MAX_WORDS || position < 0) {
                throw new ArrayIndexOutOfBoundsException(ERROR_MSG);
            }

            for (int i = 0; i < s.length(); i++) {
                TextView letterView = textViews[position][i];
                if (letterView == null) {
                    throw new NullPointerException(ERROR_MSG);
                }
                letterView.setText(String.valueOf(s.charAt(i)).toUpperCase());
            }
        }

        public void showFirstLetter(String s, int position) {
            final String ERROR_MSG = "Incorrect position";
            if (position >= MAX_WORDS || position < 0) {
                throw new ArrayIndexOutOfBoundsException(ERROR_MSG);
            }

            TextView letterView = textViews[position][0];
            letterView.setText(String.valueOf(s.charAt(0)).toLowerCase());
        }

        public void showMessage(String s, boolean large) {
            int duration = large ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, s, duration);
            toast.show();
        }
    }
}