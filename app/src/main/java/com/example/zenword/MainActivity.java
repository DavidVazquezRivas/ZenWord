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
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity {

    private Interface i;
    private static final int MAX_WORDS = 5;
    private final int MIN_LENGTH = 3;
    private final int MAX_LENGTH = 7;
    private int wordLength;

    /* S'empren un hashmap i un hashset perquè no hi ha necessitat de fer una recuperació ordenada
    * que es el gran problema d'aquests i les complexitats algoritmitques d'aquest son els més òptims*/
    private HashMap<Integer, HashSet<Word>> lengths;
    private final int[] wordsAmmount = new int[MAX_LENGTH - MIN_LENGTH + 1];
    /* S'empra un TreeSet perque s'ha de recuperar de forma ordenada*/
    private TreeMap<String, String> valids;
    private int nValids = 0;

    /* S'empra un TreeSet perque s'ha de recuperar de forma ordenada a dins cada longitud i un HasMap
    * per la complexitat dels seus mètodes*/
    private HashMap<Integer, TreeSet<Word>> solutions;
    /* S'empra un Treeset perquè es recuperaran les lletres ordenades, d'aquesta forma no es donaran
    * pistes sobre quina es la paraula triada, es pot sacrificar el O(1) de un HashSet per u O(logn)
    * perquè no tendrem moltes lletres */
    private TreeSet<Character> leters;
    /* S'empra un HashMap perquè amb la paraula s'ha de poder accedir a la posició d'aquest és més
    * no cal fer una recuperació ordenada i aquesta implementació té millors complexitats */
    private HashMap<String, Integer> hidden;
    /* S'empra un TreeMap perquè s'han de recuperar les paraules ordenades, el valor es si s'ha repetit
    * o no la paraula */
    private TreeMap<String, Boolean> found;
    private int nFound;

    private int ajudes;
    private int bonus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Podem estalviar llegir del fitxer per a cada partida a canvi de tenir el catàleg de longituds
        que ocupa molt d'espai
         */
        lengths = new HashMap<>();
        for (int i = MIN_LENGTH; i <= MAX_LENGTH; i++) {
            lengths.put(i, new HashSet<>());
        }
        try {
            WordReader reader = new WordReader(getResources().openRawResource(R.raw.paraules));
            Word word;
            while ((word = reader.read()) != null) {
                if (word.getLength() <= MAX_LENGTH && word.getLength() >= MIN_LENGTH) {
                    lengths.get(word.getLength()).add(word);
                    wordsAmmount[word.getLength() - MIN_LENGTH]++;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tests
        startGame();
        i.showWord("wrd", 0);
        i.showMessage("Hello message", true);
    }

    public void pressLetter(View v) {
        TextView word = findViewById(R.id.currentWord);

        Button button = (Button) v;
        if (word.getText().length() < wordLength) {
            word.setText(word.getText().toString() + button.getText().toString());
        }
    }

    public void send(View v) {
        // Save introduced word and reset
        TextView wordView = findViewById(R.id.currentWord);
        String input = wordView.getText().toString();
        wordView.setText("");

        if (hidden.containsKey(input)) { // Hidden should be a subset of valids
            i.showWord(input, hidden.get(input));
            found.put(valids.get(input), false);
            nFound++;
            hidden.remove(input);

            i.updateFound();
            i.showMessage("Has encertat una paraula", true);
        } else if (valids.containsKey(input)) {
            if (found.containsKey(valids.get(input))) {
                found.put(valids.get(input), true);

                i.updateFound();
                i.showMessage("Aquesta ja la tens", true);
            } else {
                found.put(valids.get(input), false);
                nFound++;
                i.updateFound();
                i.showMessage("Paraula valida! Tens un bonus", true);
                bonus++;
                if (bonus == 5) {
                    bonus = 0;
                    ajudes++;
                }
            }
        } else {
            i.showMessage("Paraula no vàlida", true);
        }

        if (hidden.isEmpty()) {
            i.showMessage("Enhorabona, has guanyat", true);
            disableViews(R.id.layout);
        }
    }

    public void restartGame(View v) {
        i.deleteViews();
        startGame();
    }

    private void startGame() {
        Random random = new Random();
        wordLength = random.nextInt(3) + 5;
        nValids = 0;
        nFound = 0;
        bonus = 0;
        ajudes = 0;
        TextView word = findViewById(R.id.currentWord);
        word.setText("");
        selectWords();
        i = new Interface(getApplicationContext());
        found = new TreeMap<>();
    }

    /**
     *
     * @param word1 chosen word
     * @param word2 word to check if it's solution
     * @return true if word2 can be formed with word1 letters, false otherwise
     */
    private boolean isSolutionWord(String word1, String word2) {
        /* No és un catàleg rellevant per guardar informació */
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

    private void enableViews(int parent) {
        ViewGroup parentView = findViewById(parent);
        int children = parentView.getChildCount();
        for (int i = 0; i < children; i++) {
            View child = parentView.getChildAt(i);
            if (child.getId() != R.id.bonusButton && child.getId() != R.id.clearButton) {
                child.setEnabled(true);
            }
        }
    }

    private void disableViews(int parent) {
        ViewGroup parentView = findViewById(parent);
        int children = parentView.getChildCount();
        System.out.println(children);
        for (int i = 0; i < children; i++) {
            View child = parentView.getChildAt(i);
            if (child.getId() != R.id.bonusButton && child.getId() != R.id.restartButton) {
                child.setEnabled(false);
            }
        }
    }

    private void selectWords() {
        // Choose word
        Random random = new Random();
        Iterator<Word> it = lengths.get(wordLength).iterator();
        Word chosen = null;
        for (int i = 0; i < random.nextInt(wordsAmmount[wordLength - MIN_LENGTH]); i++) {
            chosen = it.next();
        }
        leters = new TreeSet<>();
        for (char c : chosen.getSimple().toCharArray()) {
            leters.add(c);
        }

        // Find solutions
        solutions = new HashMap<>();
        valids = new TreeMap<>();
        for (int i = MIN_LENGTH; i <= MAX_LENGTH; i++) {
            solutions.put(i, new TreeSet<>());
        }

        int length = MIN_LENGTH;
        for (HashMap.Entry<Integer, HashSet<Word>> entry : lengths.entrySet()) {
            it = entry.getValue().iterator();
            while(it.hasNext()) {
                Word word = it.next();
                if (isSolutionWord(chosen.getSimple(), word.getSimple())) {
                    solutions.get(length).add(word);
                    valids.put(word.getSimple(), word.getFull());
                    nValids++;
                }
            }
            length++;
        }

        // Pick hidden words
        int pos = MAX_WORDS - 1;
        hidden = new HashMap<>();

        for (int i = wordLength; i >= MIN_LENGTH; i--) {
            if (!solutions.get(i).isEmpty()) {
                String key = solutions.get(i).last().getSimple();
                hidden.put(key, pos--);
            }
        }

        Iterator<Word> ite = solutions.get(MIN_LENGTH).iterator();
        while (pos >= 0 && ite.hasNext()) {
            String key = ite.next().getSimple();
            hidden.put(key, pos--);
        }

        // If nValids is not enough pick words again
        if (nValids < MAX_WORDS) {
            nValids = 0;
            selectWords();
        }
    }

    private class Interface {

        private final int X_GAP = 10;
        private final int Y_GAP = 20;
        private final TextView[][] textViews;
        private final Context context;
        private final int bgColor;
        private int usableWidth;
        private int letterSize;

        public Interface(Context context) {
            this.context = context;

            textViews = new TextView[MAX_WORDS][];

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

            // Show button letters
            Button[] letterButtons = new Button[MAX_LENGTH];
            Iterator<Character> it = leters.iterator();
            for (int i = 0; i < letterButtons.length; i++) {
                String buttonID = "letterButton" + (i + 1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                letterButtons[i] = findViewById(resID);

                if (i < wordLength) {
                    if (it.hasNext()) {
                        char letter = it.next();
                        letterButtons[i].setText(String.valueOf(letter).toUpperCase());
                    } else {
                        letterButtons[i].setText("");
                    }
                    letterButtons[i].setEnabled(true);
                    letterButtons[i].setVisibility(View.VISIBLE);
                } else {
                    letterButtons[i].setEnabled(false);
                    letterButtons[i].setVisibility(View.INVISIBLE);
                }
            }

            // Show amount of possible words
            TextView possibleWords = findViewById(R.id.possibleWords);
            possibleWords.setText("Has encertat 0 de " + nValids);
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

        public void deleteViews() {
            ConstraintLayout layout = findViewById(R.id.layout);

            for (int i = 0; i < MAX_WORDS; i++) {
                for (TextView textView : textViews[i]) {
                    if (textView != null) {
                        layout.removeView(textView);
                    }
                }
            }
        }

        public void updateFound() {
            TextView possibleWords = findViewById(R.id.possibleWords);
            SpannableStringBuilder foundString = new SpannableStringBuilder();

            for (Map.Entry<String, Boolean> entry : found.entrySet()) {
                String word = entry.getKey();

                foundString.append(word);

                if (entry.getValue()) {
                    int start = foundString.length() - word.length();
                    int end = foundString.length();
                    foundString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                foundString.append(", ");
            }

            if (foundString.length() > 2) {
                foundString.delete(foundString.length() - 2, foundString.length());
            }

            // Establecer el texto en el TextView
            possibleWords.setText("Has encertat " + nFound + " de " + nValids + ": ");
            possibleWords.append(foundString);
        }
    }
}