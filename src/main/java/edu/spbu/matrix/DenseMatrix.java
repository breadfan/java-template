package edu.spbu.matrix;

import java.util.Scanner;
import java.util.ArrayList;
import java.io.*;

/**
 * Плотная матрица
 */
public class DenseMatrix implements Matrix {

    public DenseMatrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.DMatrix = new double[rows][cols];
    }
    /**
     * загружает матрицу из файла
     *
     * @param fileName
     */
    public int rows, cols;
    public double[][] DMatrix;

    public static class Multiplier_Threads_Dense extends Thread {
        DenseMatrix m1 = null, m2 = null;
        double[][] result = null;
        int quarter = 0;

        public Multiplier_Threads_Dense() {
            super();
        }

        public Multiplier_Threads_Dense(DenseMatrix m1, DenseMatrix m2, double[][] result, int quarter) {
            super();
            this.m1 = m1;
            this.m2 = m2;
            this.quarter = quarter;
            this.result = result;
        }


        @Override
        public void run() {
            int cols_Num_first = m1.getColumns();
            int rows_Num_first = m1.getRows();
            int cols_Num_second = m2.getColumns();


            for (int k = quarter * rows_Num_first / 4; k < (quarter + 1) * rows_Num_first / 4; ++k) {
                for (int j = 0; j < cols_Num_second; ++j) {
                    for (int i = 0; i < cols_Num_first; ++i) {
                        result[k][j] += m1.getElement(k, i) * m2.getElement(i, j); //Done
                    }
                }
            }

        }
    }

    public Matrix transposition() throws IOException {
        DenseMatrix transposed_Matrix = new DenseMatrix(rows, cols);
        double[][] temp_Transposed = new double[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                temp_Transposed[j][i] = DMatrix[i][j];
            }
        }
        transposed_Matrix.DMatrix = temp_Transposed;
        return transposed_Matrix;
    }


    public int getColumns() {
        return cols;
    }

    public int getRows() {
        return rows;
    }


    public double getElement(int i, int j) {
        return DMatrix[i][j];
    }


    public DenseMatrix(String fileName) throws IOException {                //writing matrix to local memory from file
        if (fileName.trim().length() == 0)
            return;
        int i, j;
        ArrayList<Double> ReadMatrix = new ArrayList<>();
        String[] currentRowArr;
        Scanner matrix = new Scanner(new File(fileName));
        String currentRow = "1";
        while (matrix.hasNextLine() && currentRow.trim().length() != 0) {
            ++rows;
            currentRow = matrix.nextLine();
            currentRowArr = currentRow.split(" ");
            for (i = 0; i < currentRowArr.length; ++i)
                ReadMatrix.add(Double.parseDouble(currentRowArr[i]));
            if (rows == 1)
                cols = currentRowArr.length;
        }
        matrix.close();
        rows = ReadMatrix.size() / cols;
        DMatrix = new double[rows][cols];
        int k = 0;
        for (i = 0; i < cols; ++i) {
            for (j = 0; j < rows; ++j, ++k) {
                DMatrix[i][j] = ReadMatrix.get(k);
            }
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
    public Matrix mul(Matrix o) throws IOException{
       if(o instanceof DenseMatrix) {
           int rows_first = getRows();
           //int cols_first = getColumns();        //cols_first == rows_second
           int rows_second = o.getRows();
           int cols_second = o.getColumns();

           DenseMatrix MultiplicationResult = new DenseMatrix(rows_first, cols_second);

           for (int k = 0; k < rows_first; ++k) {
               for (int j = 0; j < cols_second; ++j) {
                   for (int i = 0; i < rows_second; ++i) {
                       MultiplicationResult.DMatrix[k][j] += DMatrix[k][i] * o.getElement(i, j); //Done
                   }
               }
           }

           return MultiplicationResult;
       }
       else {
           return ((o.transposition()).mul(this.transposition())).transposition();
       } // (AB)^T = B^T * AT   ==>   DS = (S^T * D^T)^T
    }

    /**
     * многопоточное умножение матриц
     *
     * @param o
     * @return
     */
    @Override
    public Matrix dmul(Matrix o) throws IOException {
        if (o instanceof DenseMatrix) {
            Thread[] threads_Array = new Multiplier_Threads_Dense[4];
            //need to concatenate all of matrices to one matrix
            int cols_of_Second = o.getColumns();
            double[][] result_Matrix = new double[rows][cols_of_Second];

            DenseMatrix new_Matrix = new DenseMatrix(rows, cols_of_Second);

            for (int i = 0; i < 4; ++i) {
                threads_Array[i] = new Multiplier_Threads_Dense(this, (DenseMatrix) o, result_Matrix, i);
                threads_Array[i].start();
            }
            for (int i = 0; i < 4; ++i) {
                try {
                    threads_Array[i].join();
                } catch (Exception e) {
                    System.out.println("GOTCHA!!!!!!!");
                }

            }

            new_Matrix.DMatrix = result_Matrix;
            return new_Matrix;
        } else
            return ((o.transposition()).dmul(this.transposition())).transposition();     //ref to a SxD
            // (AB)^T = B^T * AT   ==>   DS = (S^T * D^T)^T
    }



    @Override
    public boolean equals(Object o) {
        if (o instanceof Matrix) {
            DenseMatrix M = (DenseMatrix) o;
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) {
                    if (DMatrix[i][j] != M.getElement(i, j)) {
                        System.out.println("YOU'VE SHITTED UP (with elements now)");
                        return false;
                    }
                }
            return true;
        }
        System.out.println("Wrong instance");
        return false;

    }

}
