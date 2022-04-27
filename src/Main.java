import java.io.*;
import java.util.*;

public class Main {
    /** Метод высчитывает разницу между строками при помощь расстояния Левенштейна */
    private static int calculateDistance(String stringOne, String stringTwo) {
        stringOne = stringOne.toLowerCase();
        stringTwo = stringTwo.toLowerCase();

        int m = stringOne.length();
        int n = stringTwo.length();
        int[][] deltaM = new int[m+1][n+1];

        for(int i = 1;i <= m; i++) {
            deltaM[i][0] = i;
        }
        for(int j = 1;j <= n; j++) {
            deltaM[0][j] = j;
        }

        for(int j=1;j<=n;j++) {
            for(int i=1;i<=m;i++) {
                if(stringOne.charAt(i-1) == stringTwo.charAt(j-1)) {
                    deltaM[i][j] = deltaM[i-1][j-1];
                }
                else {
                    deltaM[i][j] = Math.min(deltaM[i-1][j]+1,
                            Math.min(deltaM[i][j-1]+1,
                                    deltaM[i-1][j-1]+1
                            )
                    );
                }
            }
        }
        return deltaM[m][n];
    }

    private static double calculateSimilarityCoef(String line1, String line2) {
        String[] wordsFromFirstLine = Arrays.stream(line1.split(" "))
                .filter(x -> x.length() > 3).toArray(String[]::new);
        String[] wordsFromSecondLine = Arrays.stream(line2.split(" "))
                .filter(x -> x.length() > 3).toArray(String[]::new);
        double coef;
        LinkedList<LineAndCoef> elements;
        LinkedList<LineAndSimilarLines> wordsWithElements = new LinkedList<>();

        //Для каждого слова из первой строки высчитываем разницу с словами из второй строки
        for (String word1: wordsFromFirstLine) {
            elements = new LinkedList<>();
            for (String word2: wordsFromSecondLine) {
                coef = calculateDistance(word1, word2);
                elements.add(new LineAndCoef(coef, word2));
            }
            Collections.sort(elements);
            wordsWithElements.add(new LineAndSimilarLines(word1, elements));
        }

        double minCoef = 0;
        int numberOfWordsInSecondLine = wordsFromSecondLine.length;

        //Находим самые похожие слова для второй строки и суммируем их разницу
        for (int i = 0; i < numberOfWordsInSecondLine; i++) {
            LineAndSimilarLines element = getElementWithMinSimilarityCoef(wordsWithElements);
            minCoef += element.getElements().get(0).getSimilarityCoef();
            removeSimilarLine(element.getElements().get(0).getSimilarLine(), wordsWithElements);
        }

        return minCoef;
    }

    private static LineAndSimilarLines getSimilarityCoefForLines(String line, ArrayList<String> lines) {
        int numberOfLines = lines.size();
        LinkedList<LineAndCoef> elements = new LinkedList<>();
        double similarityCoef;

        for (int i = 0; i < numberOfLines; i++) {
            similarityCoef = calculateSimilarityCoef(line, lines.get(i));
            elements.add(new LineAndCoef(similarityCoef, lines.get(i)));
        }
        Collections.sort(elements);
        return new LineAndSimilarLines(line, elements);
    }

    private static void removeSimilarLine(String similarLine, LinkedList<LineAndSimilarLines> linesWithSimilarLines) {
        int numberOfLines = linesWithSimilarLines.size();
        for (int i = 0; i < numberOfLines; i++) {
            LinkedList<LineAndCoef> elements = linesWithSimilarLines.get(i).getElements();
            elements.removeIf(element -> element.getSimilarLine().equals(similarLine));
        }
    }

    private static LineAndSimilarLines getElementWithMinSimilarityCoef(LinkedList<LineAndSimilarLines> linesWithSimilarLines) {
        int size = linesWithSimilarLines.size();
        double minCoef = Double.MAX_VALUE;
        double currentCoef;
        LineAndSimilarLines elementWithMinCoef = new LineAndSimilarLines();

        if (linesWithSimilarLines.get(0).getElements().size() != 0) {
            for (int i = 0; i < size; i++) {
                currentCoef = linesWithSimilarLines.get(i).getElements().get(0).getSimilarityCoef();
                if (minCoef >= currentCoef && linesWithSimilarLines.get(i).getElements().size() != 0
                        && !linesWithSimilarLines.get(i).isSimilarLineDetected()) {
                    minCoef = currentCoef;
                    elementWithMinCoef = linesWithSimilarLines.get(i);
                }
            }
        }
        return elementWithMinCoef;
    }

    private static HashMap<String, String> getSimilarLines(LinkedList<LineAndSimilarLines> linesWithSimilarLines) {
        int size = Math.min(linesWithSimilarLines.size(), linesWithSimilarLines.get(0).getElements().size());
        HashMap<String, String> result = new HashMap<>();

        for (int i = 0; i < size; i++) {
            LineAndSimilarLines elementWithMinCoef = getElementWithMinSimilarityCoef(linesWithSimilarLines);
            String line = elementWithMinCoef.getLine();
            String similarLine = elementWithMinCoef.getElements().get(0).getSimilarLine();
            result.put(line, similarLine);
            removeSimilarLine(similarLine, linesWithSimilarLines);
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        Scanner in = null;
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
        try {
            in = new Scanner(new File("input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File input.txt not found");
        }
        int n = in.nextInt();
        ArrayList<String> linesFromFirstPartOfFile = new ArrayList<>();
        in.nextLine();

        for (int i = 0; i < n; i++) {
            linesFromFirstPartOfFile.add(in.nextLine());
        }

        int m = in.nextInt();
        in.nextLine();
        ArrayList<String> linesFromSecondPartOfFile = new ArrayList<>();
        for (int i = 0; i < m; i++) {
            linesFromSecondPartOfFile.add(in.nextLine());
        }

        LinkedList<LineAndSimilarLines> linesWithSimilarLines = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            LineAndSimilarLines currentLineWithSimilarLines = getSimilarityCoefForLines(linesFromFirstPartOfFile.get(i),
                    linesFromSecondPartOfFile);
            linesWithSimilarLines.add(currentLineWithSimilarLines);
        }

        HashMap<String, String> similarLines = getSimilarLines(linesWithSimilarLines);

        for (String line: linesFromFirstPartOfFile) {
            if (similarLines.containsKey(line)) {
                writer.write(line + " : " + similarLines.get(line) + '\n');
            } else {
                writer.write(line + " : ?\n");
            }
        }
        writer.close();
    }

    private static class LineAndSimilarLines {
        private String line;
        private LinkedList <LineAndCoef> elements;
        private boolean isSimilarLineDetected;

        public LineAndSimilarLines() {}

        public LineAndSimilarLines(String line, LinkedList<LineAndCoef> elements) {
            this.line = line;
            this.elements = elements;
            isSimilarLineDetected = false;
        }

        public String getLine() {
            return line;
        }

        public LinkedList<LineAndCoef> getElements() {
            return elements;
        }

        public boolean isSimilarLineDetected() {
            return isSimilarLineDetected;
        }
    }

    private static class LineAndCoef implements Comparable{
        private double similarityCoef;
        private String similarLine;

        public LineAndCoef(double similarityCoef, String similarLine) {
            this.similarityCoef = similarityCoef;
            this.similarLine = similarLine;
        }

        @Override
        public int compareTo(Object o) {
            LineAndCoef element = (LineAndCoef) o;

            if (this.similarityCoef < element.similarityCoef) {
                return -1;
            } else if (this.similarityCoef > element.similarityCoef) {
                return 1;
            }

            return 0;
        }

        public double getSimilarityCoef() {
            return similarityCoef;
        }

        public String getSimilarLine() {
            return similarLine;
        }
    }
}
