package edu.spbu.matrix;

//import javafx.util.Pair;

import javax.xml.ws.Action;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix {
    public SparseMatrix() {
        sparse_Matr_hash = new HashMap<>();
    }

    public static class Pair<A, B> {
        private A first_of_Pair;
        private B second_of_Pair;

        public Pair(A first_of_Pair, B second_of_Pair) {
            this.first_of_Pair = first_of_Pair;
            this.second_of_Pair = second_of_Pair;
        }

        @Override
        public int hashCode() {
            int hashFirst = first_of_Pair != null ? first_of_Pair.hashCode() : 0;
            int hashSecond = second_of_Pair != null ? second_of_Pair.hashCode() : 0;
            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }

        @Override
        public boolean equals(Object next) {
            if (next instanceof Pair) {
                Pair nextPair = (Pair) next;
                return
                        ((this.first_of_Pair == nextPair.first_of_Pair ||
                                (this.first_of_Pair != null && nextPair.first_of_Pair != null &&
                                        this.first_of_Pair.equals(nextPair.first_of_Pair))) &&
                                (this.second_of_Pair == nextPair.second_of_Pair ||
                                        (this.second_of_Pair != null && nextPair.second_of_Pair != null &&
                                                this.second_of_Pair.equals(nextPair.second_of_Pair))));
            }
            return false;
        }

        private A getFirst() {
            return first_of_Pair;
        }

        private B getSecond() {
            return second_of_Pair;
        }
    }

    public HashMap<Pair<Integer, Integer>, Double> sparse_Matr_hash = new HashMap<>();


    public int rows = 0, cols = 0;

    public class Multiplier_Threads_Sparse extends Thread {
        SparseMatrix m2, m1;
        HashMap<Pair<Integer, Integer>, Double> current_Hash_Map;
        SparseMatrix result_Matrix;
        //int quarter;

        Multiplier_Threads_Sparse(SparseMatrix m1, SparseMatrix m2,
                                  HashMap<Pair<Integer, Integer>, Double> current_Hash_Map,
                                  HashMap<Pair<Integer, Integer>, Double> result) {
            super();
            this.current_Hash_Map = current_Hash_Map;
            this.m1 = m1;
            this.m2 = m2;
            this.result_Matrix.sparse_Matr_hash = result;
            //this.quarter = quarter;
        }

        @Override
        public void run() {
            for (Map.Entry<Pair<Integer, Integer>, Double> entry_First : current_Hash_Map.entrySet())
                for (Map.Entry<Pair<Integer, Integer>, Double> entry_Second : m2.sparse_Matr_hash.entrySet()) {
                    if (entry_First.getKey().getSecond().equals(entry_Second.getKey().getFirst())) {
                        int first = entry_First.getKey().getFirst();
                        int second = entry_Second.getKey().getSecond();
                        System.out.println(entry_First.getValue() * entry_Second.getValue() + result_Matrix.getElement(first, second));
                        if(!result_Matrix.sparse_Matr_hash.containsKey(new Pair<>(first, second))) {
                            System.out.println("fuck");
                            result_Matrix.cols++;
                            result_Matrix.rows++;
                        }
                        result_Matrix.sparse_Matr_hash.put(new Pair<>(first, second),
                                entry_First.getValue() * entry_Second.getValue() + result_Matrix.getElement(first, second));

                    }
                }

        }
    }


    @Override
    public SparseMatrix transposition() {
        SparseMatrix result_M = new SparseMatrix();
        result_M.cols = rows;
        result_M.rows = cols;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (getElement(i, j) != 0) {
                    result_M.sparse_Matr_hash.put(new Pair<>(j, i), getElement(i, j));            //just changing indices
                }
            }
        }

        return result_M;
    }

    public int getColumns() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public void toSize(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public double getElement(int i, int j) {
        Pair<Integer, Integer> pair = new Pair<>(i, j);
        return sparse_Matr_hash.get(pair) != null ? sparse_Matr_hash.get(pair) : 0.0;
    }


    public SparseMatrix(String fileName) {
        try {
            if (fileName.trim().length() == 0)
                return;
            String[] currentRowArr;
            Scanner matrix = new Scanner(new File(fileName));
            String currentRow;
            while (matrix.hasNextLine()) {
                double num;
                currentRow = matrix.nextLine();
                currentRowArr = currentRow.split(" ");
                for (int i = 0; i < currentRowArr.length; ++i)
                    if ((num = Integer.parseInt(currentRowArr[i])) != 0)
                        sparse_Matr_hash.put(new Pair<>(rows, i), num);
                ++rows;
                if (currentRowArr.length > cols)    //in sparse matrices there are "cut lines" - first line after mult
                    //may be able to contain several zero's, but it sparse matrices we can't save zero element
                    //so the next step is the deleting this elements. We don't know exactly how much zero elements
                    //every row contains
                    cols = currentRowArr.length;
            }
            matrix.close();
        } catch (FileNotFoundException e) {
            System.out.println(e + "for sparse matrices reading");
        }

    }

    /**
     * однопоточное умножение матриц
     * должно поддерживаться для всех 4-х вариантов
     *
     * @param o
     * @return
     */
    @Override
    public Matrix mul(Matrix o) {
        if (o instanceof SparseMatrix) {        //TODO SxS
            if (cols != ((SparseMatrix) o).rows) {
                System.out.println("first.cols != second.rows! EXTERMINATUS!!!!");
                System.exit(-1);
            }
            SparseMatrix result_Matrix = new SparseMatrix();
            int cols_of_Second = o.getColumns();
            //int rows_of_Second = o.getRows();
            result_Matrix.toSize(rows, cols_of_Second);
            for (Map.Entry<Pair<Integer, Integer>, Double> entry_First : this.sparse_Matr_hash.entrySet()) {
                for (Map.Entry<Pair<Integer, Integer>, Double> entry_Second : ((SparseMatrix) o).sparse_Matr_hash.entrySet()) {
                    if (entry_First.getKey().getSecond().equals(entry_Second.getKey().getFirst())) {
                        int first = entry_First.getKey().getFirst();
                        int second = entry_Second.getKey().getSecond();
                        result_Matrix.sparse_Matr_hash.put(new Pair<>(first, second),
                                entry_First.getValue() * entry_Second.getValue() + result_Matrix.getElement(first, second));
                        System.out.println(entry_First.getValue() * entry_Second.getValue() + result_Matrix.getElement(first, second));
                    }
                }
            }

            result_Matrix.sparse_Matr_hash.entrySet().removeIf(entry -> Math.abs(entry.getValue()) < 1.0E-06);
//            for(int i = 0; i < rows; ++i) {
//                for (int j = 0; j < cols; ++j) {
//                    int iterator_Searching_Next_Non_Empty = j;
//                    while(!result_Matrix.sparse_Matr_hash.containsKey(new Pair<>(i, iterator_Searching_Next_Non_Empty))
//                            && iterator_Searching_Next_Non_Empty < cols)
//                        ++iterator_Searching_Next_Non_Empty;
//                    if(iterator_Searching_Next_Non_Empty == cols)
//                        break;
//                    else if (iterator_Searching_Next_Non_Empty != j){ //iterator == j, if element != 0 and was added to the matrix from the start
//                        result_Matrix.sparse_Matr_hash.put(new Pair<>(i, j), result_Matrix.getElement(i, iterator_Searching_Next_Non_Empty));
//                        result_Matrix.sparse_Matr_hash.remove(new Pair<>(i, iterator_Searching_Next_Non_Empty));
//                    }
//                }
//            }
//            for(int i = 0; i < rows; ++i) { //filling empty cells
//                for (int j = 0; j < cols; ++j) {
//                    int iterator_Searching_Next_Non_Null = j;
//                    while( (!result_Matrix.sparse_Matr_hash.containsKey(new Pair<>(i, iterator_Searching_Next_Non_Null))
//                            || result_Matrix.getElement(i, iterator_Searching_Next_Non_Null) == 0)
//                            && iterator_Searching_Next_Non_Null < cols)
//                        ++iterator_Searching_Next_Non_Null;
//                    if(iterator_Searching_Next_Non_Null == cols)
//                        break;
//                    else if (iterator_Searching_Next_Non_Null != j){ //iterator == j, if element != 0 and was added to the matrix from the start
//                        result_Matrix.sparse_Matr_hash.put(new Pair<>(i, j), result_Matrix.getElement(i, iterator_Searching_Next_Non_Null));
//                        result_Matrix.sparse_Matr_hash.remove(new Pair<>(i, iterator_Searching_Next_Non_Null));
//                    }
//                }
//            }
            return result_Matrix;


        } else {            //TODO SxD
            int cols_of_Second = o.getColumns();
            double[][] result_D_Matrix = new double[rows][cols_of_Second];

            for (Map.Entry<Pair<Integer, Integer>, Double> entry_from_Sparse : sparse_Matr_hash.entrySet()) {
                int first = entry_from_Sparse.getKey().getFirst();
                int second = entry_from_Sparse.getKey().getSecond();
                for (int j = 0; j < cols_of_Second; ++j)
                    result_D_Matrix[first][j] += entry_from_Sparse.getValue() * o.getElement(second, j);
            }
            DenseMatrix Result_D_Matrix = new DenseMatrix(rows, cols_of_Second);
            Result_D_Matrix.DMatrix = result_D_Matrix;
            return Result_D_Matrix;
        }
    }

    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
    public Matrix dmul(Matrix o) throws IOException {
        Thread[] threads_Arr = new Multiplier_Threads_Sparse[4];
        //HashMap<Pair<Integer, Integer>, Double> curr_Hash_Map = new HashMap<>();
        SparseMatrix result_Matrix = new SparseMatrix();
        HashMap<Pair<Integer, Integer>, Double> temp_Hash = this.sparse_Matr_hash;
        int size_Matrix = this.sparse_Matr_hash.size();
        int i, count;
        for (i = 0; i < 4; ++i) {       // main cycle for multiplication
            count = 0;      //restricted condition for an exit from cycle
            HashMap<Pair<Integer, Integer>, Double> adding_Hash = new HashMap<>();      // quarter of the main hash_map

            for (Map.Entry<Pair<Integer, Integer>, Double> entry : temp_Hash.entrySet()) {    // dividing main hash_map to 4 parts
                if (count == size_Matrix / 4 - 1)
                    break;
                adding_Hash.put(entry.getKey(), entry.getValue());
                temp_Hash.remove(entry.getKey());
                ++count;
            }

            //handling case with the remainder
            if (i == 3 && !temp_Hash.isEmpty()) {      //it is the case if we have remainder of division by 4. The last "adding hash"
                // could have contain of 1 to 3 extra elements (by comparing to other hashes)
                for (Map.Entry<Pair<Integer, Integer>, Double> entry : temp_Hash.entrySet()) {
                    adding_Hash.put(entry.getKey(), entry.getValue());
                }
            }

            try {
                threads_Arr[i] = new Multiplier_Threads_Sparse(this, (SparseMatrix) o, adding_Hash,
                        result_Matrix.sparse_Matr_hash);
                threads_Arr[i].start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (i = 0; i < 4; ++i) {
            try {
                threads_Arr[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        for(Double obj: result_Matrix.sparse_Matr_hash.values())
            System.out.println(obj);
        result_Matrix.sparse_Matr_hash.entrySet().removeIf(entry -> Math.abs(entry.getValue()) < 1.0E-06);
        return result_Matrix;
    }

    /**
     * сравнивает с обоими вариантами
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof SparseMatrix) {
            SparseMatrix m1mulm2 = (SparseMatrix) o;
            if (this.cols == m1mulm2.getColumns() && this.rows == m1mulm2.getRows()) {
                for (Pair<Integer, Integer> expected : this.sparse_Matr_hash.keySet())
                    if (!m1mulm2.sparse_Matr_hash.containsKey(expected)      //hoping there will be no visiting to non-existing objects
                            || (m1mulm2.sparse_Matr_hash.get(expected) - this.sparse_Matr_hash.get(expected) >= 1.0E-6)) {
                        System.out.println("Objects are not equal");
                        return false;
                    }
                return true;
            }
            System.out.println("this.rows: " + this.getRows() + " this.cols: " + this.getColumns());
            System.out.println("multiplication.rows: " + m1mulm2.getRows() + " multiplication.cols: " + m1mulm2.getColumns());
            System.out.println("Sorry, sizes are not equal");
            return false;
        }
        System.out.println("Sorry, an instance of multiplication is not an instance of SparseMatrix");
        return false;
    }
}