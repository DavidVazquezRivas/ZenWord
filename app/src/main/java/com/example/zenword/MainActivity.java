package com.example.zenword;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.app.AlertDialog;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
    /* S'empra un HashMap perquè no cal recuperar les lletres ordenades així tenim les avantatges de
    * la complexitat d'un hash perquè no tendrem moltes lletres. Ha de ser map y no set perquè pot
    * haver lletres repetides*/
    private HashMap<Character, Integer> leters;
    /* S'empra un HashMap perquè amb la paraula s'ha de poder accedir a la posició d'aquest és més
    * no cal fer una recuperació ordenada i aquesta implementació té millors complexitats */
    private HashMap<String, Integer> hidden;
    /* S'empra un TreeMap perquè s'han de recuperar les paraules ordenades, el valor es si s'ha repetit
    * o no la paraula */
    private TreeMap<String, Boolean> found;
    private int nFound;
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

        startGame();
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
        String input = wordView.getText().toString().toLowerCase();
        wordView.setText("");

        if (hidden.containsKey(input)) { // Hidden should be a subset of valids
            i.showWord(input, hidden.get(input));
            found.put(valids.get(input), false);
            nFound++;
            hidden.remove(input);

            i.updateFound();
            i.showMessage("Has encertat una paraula", false);
        } else if (valids.containsKey(input)) {
            if (found.containsKey(valids.get(input))) {
                found.put(valids.get(input), true);

                i.updateFound();
                i.showMessage("Aquesta ja la tens", false);
            } else {
                found.put(valids.get(input), false);
                nFound++;
                bonus++;
                i.showMessage("Paraula valida! Tens un bonus", false);
                if (bonus % 5 == 0) {
                    Iterator<Map.Entry<String, Integer>> it = hidden.entrySet().iterator();

                    Map.Entry<String, Integer> entry = it.next();
                    String s = entry.getKey();
                    Integer pos = entry.getValue();
                    i.showFirstLetter(s, pos);
                }
                i.updateFound();
            }
        } else {
            i.showMessage("Paraula no vàlida", true);
        }

        if (hidden.isEmpty()) {
            i.showMessage("Enhorabona, has guanyat", true);
            bonus(null);
            disableViews(R.id.layout);
        } else {
            enableViews(R.id.layout);
        }
    }

    public void restartGame(View v) {
        i.deleteViews();
        startGame();
    }

    public void clear(View v) {
        TextView wordView = findViewById(R.id.currentWord);
        wordView.setText("");
    }

    public void bonus(View v) {
        String title = "Encertades";
        TextView tv = findViewById(R.id.possibleWords);
        String message = tv.getText().toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void random(View v) {
        i.reorganize();
    }

    private void startGame() {
        Random random = new Random();
        wordLength = random.nextInt(3) + 5;
        nValids = 0;
        nFound = 0;
        bonus = 0;
        TextView word = findViewById(R.id.currentWord);
        word.setText("");
        selectWords();
        i = new Interface(getApplicationContext());
        found = new TreeMap<>();
        enableViews(R.id.layout);
    }

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
        leters = new HashMap<>();
        for (char c : chosen.getSimple().toCharArray()) {
            if (leters.containsKey(c)) {
                leters.put(c, leters.get(c) + 1);
            } else {
                leters.put(c, 1);
            }
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
                    Log.d("valid key", word.getSimple());
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
                Log.d("hidden key", key);
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
        private Button[] letterButtons;

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
            letterButtons = new Button[MAX_LENGTH];
            Iterator<Map.Entry<Character, Integer>> it = leters.entrySet().iterator();

            for (int i = 0; i < letterButtons.length; i++) {
                String buttonID = "letterButton" + (i + 1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                letterButtons[i] = findViewById(resID);

                if (i < wordLength) {
                    if (it.hasNext()) {
                        Map.Entry<Character, Integer> entry = it.next();
                        char letter = entry.getKey();
                        int frequency = entry.getValue();

                        letterButtons[i].setText(String.valueOf(letter).toUpperCase());

                        while (frequency > 1) {
                            i++;
                            leters.put(letter, --frequency);
                            buttonID = "letterButton" + (i + 1);
                            resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                            letterButtons[i] = findViewById(resID);
                            letterButtons[i].setText(String.valueOf(letter).toUpperCase());
                        }

                        it.remove();

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
            possibleWords.setText("Has encertat " + nFound + " de " + nValids);

            // Show bonus
            Button bonusButton = findViewById(R.id.bonusButton);
            bonusButton.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            bonusButton.setTextSize(20);
            bonusButton.setTypeface(bonusButton.getTypeface(), Typeface.BOLD);
            bonusButton.setText(String.valueOf(bonus));
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
            // Concat words
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
            // Delete final ", "
            if (foundString.length() > 2) {
                foundString.delete(foundString.length() - 2, foundString.length());
            }

            possibleWords.setText("Has encertat " + nFound + " de " + nValids + ": ");
            possibleWords.append(foundString);

            // Update bonus button
            Button bonusButton = findViewById(R.id.bonusButton);
            bonusButton.setText(String.valueOf(bonus));
        }

        public void reorganize() {
            List<String> letters = new ArrayList<>();

            // Recopila las letras visibles en una lista
            for (int i = 0; i < wordLength; i++) {
                letters.add(letterButtons[i].getText().toString());
            }

            // Embaraja la lista usando el algoritmo de Fisher-Yates
            Random random = new Random();
            for (int i = letters.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Collections.swap(letters, i, j);
            }

            // Asigna las letras embarajadas de vuelta a los botones
            for (int i = 0; i < wordLength; i++) {
                letterButtons[i].setText(letters.get(i));
            }
        }

        public void showDialog(String title, String message) {

        }
    }
}