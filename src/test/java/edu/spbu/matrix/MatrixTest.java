package edu.spbu.matrix;

import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MatrixTest {
    /**
     * ожидается 4 таких теста
     */
    @Test
    public void mulDD() throws IOException {
        DenseMatrix m1 = new DenseMatrix("m1_Dense.txt");
        DenseMatrix m2 = new DenseMatrix("m2_Dense.txt");
        Matrix expected = new DenseMatrix("result_DxD.txt");
        assertEquals(expected, m1.mul(m2));

    }

    @Test
    public void mulSS() throws IOException {
        SparseMatrix m1 = new SparseMatrix("m1_Sparse.txt");
        SparseMatrix m2 = new SparseMatrix("m2_Sparse.txt");
        Matrix expected = new SparseMatrix("result_SxS.txt");
        assertEquals(expected, m1.mul(m2));
    }
    @Test
    public void mulSD() throws IOException {
        SparseMatrix m1 = new SparseMatrix("m1_Sparse.txt");
        DenseMatrix m2 = new DenseMatrix("m2_Dense.txt");
        Matrix expected = new DenseMatrix("result_SxD.txt");
        assertEquals(expected, m1.mul(m2));
    }
    @Test
    public void mulDS() throws IOException {
        DenseMatrix m1 = new DenseMatrix("m1_Dense.txt");
        SparseMatrix m2 = new SparseMatrix("m2_Sparse.txt");
        Matrix expected = new DenseMatrix("result_DxS.txt");
        assertEquals(expected, m1.mul(m2));
    }

    @Test
    public void dmulDD() throws IOException {
        DenseMatrix m1 = new DenseMatrix("m1_Dense.txt");
        DenseMatrix m2 = new DenseMatrix("m2_Dense.txt");
        DenseMatrix expected = new DenseMatrix("result_DxD.txt");
        DenseMatrix result = (DenseMatrix) m1.dmul(m2);
        assertEquals(expected, result);
    }

    @Test
    public void dmulSS() throws IOException {
        SparseMatrix m1 = new SparseMatrix("m1_Sparse.txt");
        SparseMatrix m2 = new SparseMatrix("m2_Sparse.txt");
        SparseMatrix expected = new SparseMatrix("result_SxS.txt");
        SparseMatrix result = (SparseMatrix) m1.dmul(m2);
        assertEquals(expected, result);
    }
}




//for(int i =  0; i < result.getRows(); ++i) {
//        for (int j = 0; j < result.getColumns(); ++j) {
//        System.out.print(result.getElement(i, j) + " ");
//        }
//        System.out.println('\n');
//        }

