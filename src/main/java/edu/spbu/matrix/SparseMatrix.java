package edu.spbu.matrix;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;


/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix {
    public SparseMatrix(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
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
        SparseMatrix m1, m2, temp_Result;
        int first_Ptr, second_Ptr;

        Multiplier_Threads_Sparse(int first_Ptr, int second_Ptr, SparseMatrix m1, SparseMatrix m2,
                                  SparseMatrix temp_Result) {
            super();
            this.first_Ptr = first_Ptr;
            this.second_Ptr = second_Ptr;
            this.m1 = m1;
            this.m2 = m2;
            this.temp_Result = temp_Result;
        }

        @Override
        public void run() {
            for (Pair<Integer, Integer> pair: m1.sparse_Matr_hash.keySet() ) {
                for(int i = first_Ptr; i < first_Ptr + second_Ptr; ++i){
                    if(i < m2.cols){
                        Pair<Integer, Integer> temp_Pair = new Pair<>(pair.second_of_Pair, i);
                        if(m2.sparse_Matr_hash.containsKey(temp_Pair)){
                            Pair<Integer, Integer> temp_Gear = new Pair<>(pair.first_of_Pair, temp_Pair.second_of_Pair);
                            double t;
                            if(temp_Result.sparse_Matr_hash.containsKey(temp_Gear)){
                                t = temp_Result.sparse_Matr_hash.get(temp_Gear) +
                                        m1.sparse_Matr_hash.get(pair) * m2.sparse_Matr_hash.get(temp_Pair);
                            }
                            else{
                                t = m1.sparse_Matr_hash.get(pair) * m2.sparse_Matr_hash.get(temp_Pair);
                            }
                            temp_Result.sparse_Matr_hash.put(temp_Gear, t);
                        }
                    }
                }

            }
        }
    }


    @Override
    public SparseMatrix transposition() {
        SparseMatrix result_M = new SparseMatrix(cols, rows);
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
            SparseMatrix result_Matrix = new SparseMatrix(rows, o.getColumns());
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
        SparseMatrix result_Matrix = new SparseMatrix(this.rows, o.getColumns());   //we use this matrix only
            // in the end
        ArrayList<Thread> threads_Arr = new ArrayList<>();
        ArrayList<SparseMatrix> list_Matrices = new ArrayList<>();
        int separator = o.getColumns() / 4 +1;

        for (int i = 0; i < o.getColumns(); i += separator) {
            SparseMatrix temp_result = new SparseMatrix(o.getColumns(), rows);
            list_Matrices.add(temp_result);
            Multiplier_Threads_Sparse abc = new Multiplier_Threads_Sparse(i, separator, this, (SparseMatrix) o, temp_result);
            Thread newthread = new Thread(abc);
            threads_Arr.add(newthread);
            newthread.start();
        }
       for(Thread p : threads_Arr){
           try{
               p.join();
           }
           catch (InterruptedException e){
               e.printStackTrace();
           }
       }
       for(SparseMatrix sp_m : list_Matrices){
           for(Pair<Integer, Integer> pair : sp_m.sparse_Matr_hash.keySet()){
               if(result_Matrix.sparse_Matr_hash.containsKey(pair)){
                   double t = result_Matrix.sparse_Matr_hash.get(pair) + sp_m.sparse_Matr_hash.get(pair) ;
                   if (Math.abs(t) < 1.0E-06)
                   {
                      result_Matrix.sparse_Matr_hash.remove(pair);
                   } else {
                       result_Matrix.sparse_Matr_hash.put(pair, t);
                   }
               }
               else{
                   if(Math.abs(sp_m.sparse_Matr_hash.get(pair))>= 1.0E-06) {
                       result_Matrix.sparse_Matr_hash.put(pair, sp_m.sparse_Matr_hash.get(pair));
                       System.out.println(sp_m.sparse_Matr_hash.get(pair));
                   }
               }
           }
       }
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
                            || !(m1mulm2.sparse_Matr_hash.get(expected)).equals(this.sparse_Matr_hash.get(expected))) {
                        System.out.println("Objects are not equal");
                        System.out.println(m1mulm2.sparse_Matr_hash.get(expected) + " " + this.sparse_Matr_hash.get(expected)
                                + expected.getFirst() + expected.getSecond());
                        for(int i = 0; i < m1mulm2.rows; ++i)
                            for(int j = 0; j < m1mulm2.cols; ++j)
                                System.out.print(" m1mulm2: " + m1mulm2.getElement(i, j) + " ");
                        System.out.println();
                        for(int i = 0; i < this.rows; ++i)
                            for(int j = 0; j < this.cols; ++j)
                                System.out.print(" expected: " + this.getElement(i, j) + " ");
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